package org.ballastlane.pokeapp.infrastructure.pokeapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig
class PokeApiClientCachingTest {

  @RegisterExtension
  static WireMockExtension pokeApi =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Configuration
  @EnableCaching
  static class TestConfig {

    @Bean
    CacheManager cacheManager() {
      return new CaffeineCacheManager();
    }

    @Bean
    PokeApiClient pokeApiClient() {
      return new PokeApiClient(RestClient.builder(), PokeApiStubs.propertiesFor(pokeApi));
    }
  }

  @Autowired private PokeApiClient client;

  @Test
  void shouldHitPokeApiOnce_whenSamePokemonRequestedTwice() {
    // given
    PokeApiStubs.stubJson(pokeApi, "/api/v2/pokemon/pikachu", "pokemon-pikachu.json");

    // when
    client.fetchPokemon("pikachu");
    client.fetchPokemon("pikachu");

    // then
    pokeApi.verify(exactly(1), getRequestedFor(urlEqualTo("/api/v2/pokemon/pikachu")));
  }

  @Test
  void shouldNotCacheMisses_whenPokeApiReturnsNotFound() {
    // given
    pokeApi.stubFor(
        get(urlEqualTo("/api/v2/pokemon/agumon")).willReturn(aResponse().withStatus(404)));

    // when
    assertThatThrownBy(() -> client.fetchPokemon("agumon"))
        .isInstanceOf(PokeApiNotFoundException.class);
    assertThatThrownBy(() -> client.fetchPokemon("agumon"))
        .isInstanceOf(PokeApiNotFoundException.class);

    // then
    pokeApi.verify(exactly(2), getRequestedFor(urlEqualTo("/api/v2/pokemon/agumon")));
  }
}
