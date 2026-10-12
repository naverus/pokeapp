package org.ballastlane.pokeapp.infrastructure.pokeapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.web.client.RestClient;

final class PokeApiStubs {

  private PokeApiStubs() {}

  static PokeApiProperties propertiesFor(WireMockExtension server) {
    return new PokeApiProperties(
        server.baseUrl() + "/api/v2", Duration.ofSeconds(1), Duration.ofSeconds(2), 4);
  }

  static PokeApiClient clientFor(WireMockExtension server) {
    return new PokeApiClient(RestClient.builder(), propertiesFor(server));
  }

  static void stubPikachu(WireMockExtension server) {
    stubJson(server, "/api/v2/pokemon?offset=0&limit=1", "pokemon-list.json");
    stubJson(server, "/api/v2/pokemon/pikachu", "pokemon-pikachu.json");
    stubJson(server, "/api/v2/pokemon-species/25", "species-25.json");
    stubJson(server, "/api/v2/evolution-chain/10", "evolution-chain-10.json");
  }

  static void stubJson(WireMockExtension server, String url, String resource) {
    server.stubFor(
        get(urlEqualTo(url))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBody(read(resource))));
  }

  private static String read(String resource) {
    try (var stream = PokeApiStubs.class.getResourceAsStream("/pokeapi/" + resource)) {
      if (stream == null) {
        throw new IllegalStateException("Missing test resource " + resource);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
