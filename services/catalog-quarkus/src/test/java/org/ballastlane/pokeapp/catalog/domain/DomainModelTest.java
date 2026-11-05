package org.ballastlane.pokeapp.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.ballastlane.pokeapp.catalog.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.catalog.domain.model.PageQuery;
import org.ballastlane.pokeapp.catalog.domain.model.PageResult;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.Test;

class DomainModelTest {

  @Test
  void pageQueryComputesOffset() {
    assertThat(new PageQuery(3, 20).offset()).isEqualTo(60);
  }

  @Test
  void pageQueryReportsEveryViolation() {
    assertThatThrownBy(() -> new PageQuery(-1, 0))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            ex -> assertThat(ex.errors()).containsKeys("page", "size"));
  }

  @Test
  void pageResultComputesTotalPages() {
    assertThat(new PageResult<>(List.of("a"), 0, 20, 41).totalPages()).isEqualTo(3);
    assertThat(new PageResult<>(List.<String>of(), 0, 20, 0).totalPages()).isZero();
  }

  @Test
  void identifierNormalisesAndValidates() {
    assertThat(PokemonIdentifier.of("  Mr-Mime ").value()).isEqualTo("mr-mime");
    assertThat(PokemonIdentifier.of("25").value()).isEqualTo("25");
    assertThatThrownBy(() -> PokemonIdentifier.of("0")).isInstanceOf(DomainValidationException.class);
    assertThatThrownBy(() -> PokemonIdentifier.of("a b"))
        .isInstanceOf(DomainValidationException.class);
    assertThatThrownBy(() -> PokemonIdentifier.of(null))
        .isInstanceOf(DomainValidationException.class);
  }
}
