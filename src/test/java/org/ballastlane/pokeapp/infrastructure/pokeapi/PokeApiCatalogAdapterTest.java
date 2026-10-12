package org.ballastlane.pokeapp.infrastructure.pokeapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.model.EvolutionStage;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class PokeApiCatalogAdapterTest {

  @RegisterExtension
  static WireMockExtension pokeApi =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private PokeApiCatalogAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new PokeApiCatalogAdapter(
            PokeApiStubs.clientFor(pokeApi), PokeApiStubs.propertiesFor(pokeApi));
  }

  @Test
  void shouldMapCatalogPage_withSpriteCategoryMassAndAbilities() {
    // given
    PokeApiStubs.stubPikachu(pokeApi);

    // when
    var page = adapter.findPage(new PageQuery(0, 1));

    // then
    assertThat(page.totalElements()).isEqualTo(1302);
    assertThat(page.totalPages()).isEqualTo(1302);
    assertThat(page.content())
        .singleElement()
        .satisfies(
            summary -> {
              assertThat(summary.id()).isEqualTo(25);
              assertThat(summary.spriteUrl()).endsWith("/sprites/pokemon/25.png");
              assertThat(summary.category()).isEqualTo("Mouse Pokémon");
              assertThat(summary.weightKg()).isEqualTo(6.0);
              assertThat(summary.abilities()).containsExactly("static", "lightning-rod");
              assertThat(summary.types()).containsExactly("electric");
            });
  }

  @Test
  void shouldMapDetail_withStatsDescriptionAndEvolutionChain() {
    // given
    PokeApiStubs.stubPikachu(pokeApi);

    // when
    var detail = adapter.findDetail("pikachu").orElseThrow();

    // then
    assertThat(detail.imageUrl()).endsWith("/official-artwork/25.png");
    assertThat(detail.heightM()).isEqualTo(0.4);
    assertThat(detail.stats())
        .containsExactly(new Stat("hp", 35), new Stat("attack", 55), new Stat("speed", 90));
    assertThat(detail.description())
        .isEqualTo(
            "When several of these POKéMON gather, their electricity could build and cause lightning storms.");
    assertThat(detail.evolutionChain())
        .extracting(EvolutionStage::name, EvolutionStage::stage, EvolutionStage::speciesId)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("pichu", 1, 172),
            org.assertj.core.groups.Tuple.tuple("pikachu", 2, 25),
            org.assertj.core.groups.Tuple.tuple("raichu", 3, 26));
  }

  @Test
  void shouldReturnEmpty_whenPokeApiRespondsNotFound() {
    pokeApi.stubFor(
        get(urlEqualTo("/api/v2/pokemon/agumon")).willReturn(aResponse().withStatus(404)));

    assertThat(adapter.findDetail("agumon")).isEmpty();
  }

  @Test
  void shouldThrowCatalogUnavailable_whenPokeApiFails() {
    pokeApi.stubFor(
        get(urlEqualTo("/api/v2/pokemon?offset=0&limit=5"))
            .willReturn(aResponse().withStatus(503)));

    assertThatThrownBy(() -> adapter.findPage(new PageQuery(0, 5)))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void shouldThrowCatalogUnavailable_whenListedPokemonCannotBeFetched() {
    PokeApiStubs.stubJson(pokeApi, "/api/v2/pokemon?offset=0&limit=1", "pokemon-list.json");
    pokeApi.stubFor(
        get(urlEqualTo("/api/v2/pokemon/pikachu")).willReturn(aResponse().withStatus(404)));

    assertThatThrownBy(() -> adapter.findPage(new PageQuery(0, 1)))
        .isInstanceOf(CatalogUnavailableException.class)
        .hasMessageContaining("pikachu");
  }
}
