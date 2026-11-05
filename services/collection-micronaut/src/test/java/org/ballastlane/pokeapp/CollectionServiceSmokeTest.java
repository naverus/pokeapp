package org.ballastlane.pokeapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.micronaut.context.annotation.Property;
import io.micronaut.json.JsonMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MySQLContainer;

/** End-to-end smoke test: real MySQL (Flyway), real JWTs minted like auth-vertx, stubbed PokeAPI. */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "micronaut.http.client.read-timeout", value = "30s")
class CollectionServiceSmokeTest implements TestPropertyProvider {

  private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";
  private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
  private static HttpServer pokeApi;

  @Inject EmbeddedServer server;
  @Inject JsonMapper json;

  private final HttpClient http = HttpClient.newHttpClient();

  @Override
  public Map<String, String> getProperties() {
    MYSQL.start();
    startPokeApiStub();
    return Map.of(
        "micronaut.server.port", "-1",
        "datasources.default.url", MYSQL.getJdbcUrl(),
        "datasources.default.username", MYSQL.getUsername(),
        "datasources.default.password", MYSQL.getPassword(),
        "micronaut.security.token.jwt.signatures.secret.generator.secret", SECRET,
        "pokeapp.pokeapi.base-url",
            "http://localhost:" + pokeApi.getAddress().getPort() + "/api/v2");
  }

  @AfterAll
  void stop() {
    if (pokeApi != null) {
      pokeApi.stop(0);
    }
    MYSQL.stop();
  }

  @Test
  void healthIsPublic() throws Exception {
    assertThat(call("GET", "/actuator/health", null, null).statusCode()).isEqualTo(200);
  }

  @Test
  void requiresAuthentication() throws Exception {
    var response = call("GET", "/api/v1/local-pokemon", null, null);
    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.headers().firstValue("content-type").orElse(""))
        .contains("application/problem+json");
  }

  @Test
  void rejectsTokenSignedWithAnotherSecretOrIssuer() throws Exception {
    var wrongIssuer = token("ash", "USER", "other", SECRET);
    var wrongSecret = token("ash", "USER", "pokeapp", "another-secret-another-secret-0123456789");
    assertThat(call("GET", "/api/v1/local-pokemon", null, wrongIssuer).statusCode()).isEqualTo(401);
    assertThat(call("GET", "/api/v1/local-pokemon", null, wrongSecret).statusCode()).isEqualTo(401);
  }

  @Test
  void listsSeededPokemonAndEnforcesRoles() throws Exception {
    var user = token("ash", "USER", "pokeapp", SECRET);
    var list = call("GET", "/api/v1/local-pokemon?page=0&size=5", null, user);
    assertThat(list.statusCode()).isEqualTo(200);
    var body = read(list);
    assertThat(body).containsKeys("content", "page", "size", "totalElements", "totalPages");
    assertThat((java.util.List<?>) body.get("content")).hasSize(5);

    var filtered = read(call("GET", "/api/v1/local-pokemon?name=PIKA", null, user));
    assertThat((java.util.List<?>) filtered.get("content")).hasSize(1);

    assertThat(call("DELETE", "/api/v1/local-pokemon/1", null, user).statusCode()).isEqualTo(403);
    assertThat(call("POST", "/api/v1/admin/sync-jobs", null, user).statusCode()).isEqualTo(403);
    var stored = call("GET", "/api/v1/local-pokemon/stored-ids?pokeApiIds=1,4,999", null, user);
    assertThat(read(stored).get("pokeApiIds")).isEqualTo(java.util.List.of(1, 4));
    var invalid = call("GET", "/api/v1/local-pokemon/stored-ids?pokeApiIds=0", null, user);
    assertThat(invalid.statusCode()).isEqualTo(400);
  }

  @Test
  void adminCrudWithOptimisticLocking() throws Exception {
    var admin = token("admin", "ADMIN", "pokeapp", SECRET);
    var create =
        call(
            "POST",
            "/api/v1/local-pokemon",
            """
            {"name":"custom-mon","heightM":1.2,"weightKg":30.5,"types":["fire"],"tags":["x"]}
            """,
            admin);
    assertThat(create.statusCode()).isEqualTo(201);
    var created = read(create);
    assertThat(create.headers().firstValue("location").orElse(""))
        .endsWith("/api/v1/local-pokemon/" + created.get("id"));
    assertThat(created.get("pokeApiId")).isNull();
    assertThat(created.get("createdAt")).isInstanceOf(String.class);
    var id = created.get("id");

    var update =
        call(
            "PUT",
            "/api/v1/local-pokemon/" + id,
            """
            {"version":0,"name":"custom-mon2","heightM":1.3,"weightKg":30.5,"types":["fire","water"]}
            """,
            admin);
    assertThat(update.statusCode()).isEqualTo(200);
    assertThat(read(update).get("version")).isEqualTo(1);

    var stale =
        call(
            "PUT",
            "/api/v1/local-pokemon/" + id,
            """
            {"version":0,"name":"again","heightM":1.3,"weightKg":30.5}
            """,
            admin);
    assertThat(stale.statusCode()).isEqualTo(409);

    var invalid = call("PUT", "/api/v1/local-pokemon/" + id, "{\"version\":1,\"name\":\"\"}", admin);
    assertThat(invalid.statusCode()).isEqualTo(400);
    assertThat(read(invalid).get("errors").toString()).contains("name", "heightM");

    var malformed = call("PUT", "/api/v1/local-pokemon/" + id, "{not json", admin);
    assertThat(malformed.statusCode()).isEqualTo(400);

    assertThat(call("DELETE", "/api/v1/local-pokemon/" + id, null, admin).statusCode())
        .isEqualTo(204);
    assertThat(call("GET", "/api/v1/local-pokemon/" + id, null, admin).statusCode())
        .isEqualTo(404);
  }

  @Test
  void syncsSinglePokemonOnce() throws Exception {
    var user = token("ash", "USER", "pokeapp", SECRET);
    var first = call("POST", "/api/v1/local-pokemon/sync/200", null, user);
    assertThat(first.statusCode()).isEqualTo(201);
    assertThat(first.headers().firstValue("location")).isPresent();
    assertThat(read(first).get("name")).isEqualTo("mon-200");
    assertThat(call("POST", "/api/v1/local-pokemon/sync/200", null, user).statusCode())
        .isEqualTo(409);
    assertThat(call("POST", "/api/v1/local-pokemon/sync/9999", null, user).statusCode())
        .isEqualTo(404);
  }

  @Test
  void restocksARange() throws Exception {
    var admin = token("admin", "ADMIN", "pokeapp", SECRET);
    assertThat(call("GET", "/api/v1/admin/sync-jobs/latest", null, admin).statusCode())
        .isIn(200, 204);
    var start = call("POST", "/api/v1/admin/sync-jobs", "{\"fromId\":300,\"toId\":304}", admin);
    assertThat(start.statusCode()).isEqualTo(202);
    var job = read(start);
    assertThat(job.get("requestedBy")).isEqualTo("admin");
    assertThat(job.get("total")).isEqualTo(5);

    Map<String, Object> current = job;
    for (int i = 0; i < 100 && "RUNNING".equals(current.get("status")); i++) {
      Thread.sleep(200);
      current = read(call("GET", "/api/v1/admin/sync-jobs/" + job.get("id"), null, admin));
    }
    assertThat(current.get("status")).isEqualTo("COMPLETED");
    assertThat(current.get("inserted")).isEqualTo(5);

    var invalid = call("POST", "/api/v1/admin/sync-jobs", "{\"fromId\":5,\"toId\":1}", admin);
    assertThat(invalid.statusCode()).isEqualTo(400);
    assertThat(call("GET", "/api/v1/admin/sync-jobs/" + java.util.UUID.randomUUID(), null, admin)
            .statusCode())
        .isEqualTo(404);
  }

  // ---------------------------------------------------------------------------------------------

  private HttpResponse<String> call(String method, String path, String body, String token)
      throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create(server.getURL() + path))
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));
    if (body != null) {
      builder.header("Content-Type", "application/json");
    }
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> read(HttpResponse<String> response) throws Exception {
    return json.readValue(response.body(), Map.class);
  }

  private static String token(String user, String role, String issuer, String secret)
      throws Exception {
    var enc = Base64.getUrlEncoder().withoutPadding();
    var header = enc.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    var now = Instant.now().getEpochSecond();
    var payload =
        enc.encodeToString(
            ("{\"iss\":\"%s\",\"sub\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}"
                    .formatted(issuer, user, role, now, now + 3600))
                .getBytes(StandardCharsets.UTF_8));
    var mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    var signature = enc.encodeToString(mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8)));
    return header + "." + payload + "." + signature;
  }

  private static void startPokeApiStub() {
    try {
      pokeApi = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
      var pokemon = Pattern.compile("/api/v2/pokemon/(\\d+)");
      var species = Pattern.compile("/api/v2/pokemon-species/(\\d+)");
      pokeApi.createContext(
          "/api/v2",
          exchange -> {
            var path = exchange.getRequestURI().getPath();
            String body = null;
            var p = pokemon.matcher(path);
            var s = species.matcher(path);
            if (p.matches() && Integer.parseInt(p.group(1)) < 1000) {
              var id = p.group(1);
              body =
                  """
                  {"id":%s,"name":"mon-%s","height":7,"weight":69,
                   "sprites":{"front_default":"f.png","other":{"official-artwork":{"front_default":"a.png"}}},
                   "types":[{"slot":1,"type":{"name":"grass","url":"x"}}],
                   "abilities":[{"slot":1,"ability":{"name":"overgrow","url":"x"}}],
                   "stats":[{"base_stat":45,"stat":{"name":"hp","url":"x"}}],
                   "species":{"name":"mon-%s","url":"http://localhost/api/v2/pokemon-species/%s/"}}
                  """
                      .formatted(id, id, id, id);
            } else if (s.matches()) {
              body =
                  """
                  {"id":%s,"name":"mon","genera":[{"genus":"Seed Pokémon","language":{"name":"en","url":"x"}}],
                   "flavor_text_entries":[{"flavor_text":"A strange\\nseed.","language":{"name":"en","url":"x"}}],
                   "evolution_chain":null}
                  """
                      .formatted(s.group(1));
            }
            var bytes = (body == null ? "{}" : body).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(body == null ? 404 : 200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
          });
      pokeApi.start();
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
