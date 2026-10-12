package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

class PokemonDetailTest {

  @Test
  void shouldExposeImmutableCollections() {
    var stats = new ArrayList<>(List.of(new Stat("hp", 35)));
    var detail =
        new PokemonDetail(
            25,
            "pikachu",
            "img",
            "Mouse",
            0.4,
            6.0,
            List.of("electric"),
            List.of("static"),
            stats,
            "desc",
            List.of(new EvolutionStage(25, "pikachu", 1, "img")));

    stats.add(new Stat("attack", 55));

    assertThat(detail.stats()).hasSize(1);
    assertThat(detail.totalBaseStats()).isEqualTo(35);
  }

  @Test
  void shouldRejectNegativeBaseStat() {
    assertThatThrownBy(() -> new Stat("hp", -1)).isInstanceOf(DomainValidationException.class);
  }

  @Test
  void shouldTreatNullCollectionsAsEmptyInSummary() {
    var summary = new PokemonSummary(1, "bulbasaur", null, null, 6.9, null, null);

    assertThat(summary.abilities()).isEmpty();
    assertThat(summary.types()).isEmpty();
  }
}
