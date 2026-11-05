package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PokemonIdentifierTest {

  @Test
  void shouldNormalizeNameToLowerCase() {
    assertThat(PokemonIdentifier.of("  Mr-Mime ").value()).isEqualTo("mr-mime");
  }

  @Test
  void shouldAcceptNumericId() {
    assertThat(PokemonIdentifier.of("25").value()).isEqualTo("25");
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "  ", "../admin", "pika chu", "0", "-1"})
  void shouldRejectInvalidIdentifiers(String raw) {
    assertThatThrownBy(() -> PokemonIdentifier.of(raw))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey("idOrName"));
  }
}
