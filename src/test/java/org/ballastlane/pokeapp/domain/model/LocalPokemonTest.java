package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.junit.jupiter.api.Test;

class LocalPokemonTest {

  @Test
  void shouldCopyCatalogData_whenCreatedFromCatalogDetail() {
    // given
    var detail = PokemonFixtures.pikachuDetail();

    // when
    var local = LocalPokemon.fromCatalog(detail);

    // then
    assertThat(local.id()).isNull();
    assertThat(local.pokeApiId()).isEqualTo(25);
    assertThat(local.attributes().name()).isEqualTo("pikachu");
    assertThat(local.attributes().category()).isEqualTo("Mouse Pokemon");
    assertThat(local.attributes().heightM()).isEqualTo(0.4);
    assertThat(local.attributes().weightKg()).isEqualTo(6.0);
    assertThat(local.attributes().spriteUrl()).isEqualTo(detail.imageUrl());
    assertThat(local.attributes().types()).containsExactly("electric");
    assertThat(local.attributes().abilities()).containsExactly("static", "lightning-rod");
    assertThat(local.attributes().tags()).isEmpty();
    assertThat(local.version()).isZero();
  }

  @Test
  void shouldKeepIdentityAndReplaceAttributes_whenUpdated() {
    // given
    var created = Instant.parse("2026-01-01T00:00:00Z");
    var local = new LocalPokemon(7L, 25, PokemonFixtures.pikachuAttributes(), 3L, created, created);
    var changes =
        new PokemonAttributes(
            "Pikachu",
            "Pikachu ES",
            "Kanto",
            "Mouse",
            0.4,
            6.1,
            null,
            "desc",
            List.of("electric"),
            List.of("static"),
            List.of("starter"));

    // when
    var updated = local.withAttributes(changes);

    // then
    assertThat(updated.id()).isEqualTo(7L);
    assertThat(updated.pokeApiId()).isEqualTo(25);
    assertThat(updated.version()).isEqualTo(3L);
    assertThat(updated.createdAt()).isEqualTo(created);
    assertThat(updated.attributes()).isEqualTo(changes);
  }

  @Test
  void shouldCreateManualEntryWithoutPokeApiId_whenCreatedManually() {
    // when
    var local = LocalPokemon.create(null, PokemonFixtures.pikachuAttributes());

    // then
    assertThat(local.id()).isNull();
    assertThat(local.pokeApiId()).isNull();
    assertThat(local.version()).isZero();
  }
}
