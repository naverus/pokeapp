package org.ballastlane.pokeapp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.ballastlane.pokeapp.support.MySqlTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Full stack: HTTP, security, use cases, PokeAPI adapter (WireMock) and MySQL (Testcontainers). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MySqlTestcontainersConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class PokeAppEndToEndTest {

  @RegisterExtension
  static WireMockExtension pokeApi =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void pokeApiUrl(DynamicPropertyRegistry registry) {
    registry.add("pokeapp.pokeapi.base-url", () -> pokeApi.baseUrl() + "/api/v2");
  }

  @Autowired private TestRestTemplate rest;

  @Test
  void shouldSyncUpdateAndDeleteThroughTheApi() throws IOException {
    stub("/api/v2/pokemon/raichu", "pokemon-pikachu.json", "\"id\": 25", "\"id\": 26");
    stub("/api/v2/pokemon-species/25", "species-25.json", null, null);
    stub("/api/v2/evolution-chain/10", "evolution-chain-10.json", null, null);

    // public catalog
    var detail = rest.getForEntity("/api/v1/pokemon/raichu", JsonNode.class);
    assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detail.getBody().path("evolutionChain")).hasSize(3);

    // protected routes reject anonymous users
    assertThat(rest.getForEntity("/api/v1/local-pokemon", JsonNode.class).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);

    var userToken = login("ash", "pikachu123");
    var adminToken = login("admin", "admin123");

    // US03 sync
    var synced = exchange(HttpMethod.POST, "/api/v1/local-pokemon/sync/raichu", userToken, null);
    assertThat(synced.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var id = synced.getBody().path("id").asLong();
    assertThat(
            exchange(HttpMethod.POST, "/api/v1/local-pokemon/sync/raichu", userToken, null)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);

    // US04 update with custom fields
    var update =
        Map.of(
            "version",
            0,
            "name",
            "raichu",
            "localName",
            "Raichu MX",
            "region",
            "Kanto",
            "heightM",
            0.8,
            "weightKg",
            30.0,
            "tags",
            java.util.List.of("electric-team"));
    var updated = exchange(HttpMethod.PUT, "/api/v1/local-pokemon/" + id, userToken, update);
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody().path("localName").asText()).isEqualTo("Raichu MX");
    assertThat(updated.getBody().path("version").asLong()).isEqualTo(1);
    assertThat(
            exchange(HttpMethod.PUT, "/api/v1/local-pokemon/" + id, userToken, update)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
    assertThat(
            exchange(HttpMethod.PUT, "/api/v1/local-pokemon/999999", userToken, update)
                .getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);

    // delete requires ADMIN
    assertThat(
            exchange(HttpMethod.DELETE, "/api/v1/local-pokemon/" + id, userToken, null)
                .getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(
            exchange(HttpMethod.DELETE, "/api/v1/local-pokemon/" + id, adminToken, null)
                .getStatusCode())
        .isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void shouldRegisterThenLogin() {
    var register =
        rest.postForEntity(
            "/api/v1/auth/register",
            Map.of("username", "gary", "password", "eevee1234"),
            JsonNode.class);

    assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(login("gary", "eevee1234")).isNotBlank();
  }

  private String login(String username, String password) {
    var response =
        rest.postForEntity(
            "/api/v1/auth/login",
            Map.of("username", username, "password", password),
            JsonNode.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody().path("accessToken").asText();
  }

  private org.springframework.http.ResponseEntity<JsonNode> exchange(
      HttpMethod method, String url, String token, Object body) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return rest.exchange(url, method, new HttpEntity<>(body, headers), JsonNode.class);
  }

  private static void stub(String url, String resource, String search, String replacement)
      throws IOException {
    try (var stream = PokeAppEndToEndTest.class.getResourceAsStream("/pokeapi/" + resource)) {
      var body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      if (search != null) {
        body =
            body.replace(search, replacement)
                .replace(
                    "\"name\": \"pikachu\",\n  \"height\"", "\"name\": \"raichu\",\n  \"height\"");
      }
      pokeApi.stubFor(
          get(urlEqualTo(url))
              .willReturn(
                  aResponse()
                      .withHeader("Content-Type", "application/json; charset=utf-8")
                      .withBody(body)));
    }
  }
}
