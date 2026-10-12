package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SyncRangeTest {

  @Test
  void shouldCoverFirstGeneration_whenDefaultRangeUsed() {
    // when
    var range = SyncRange.DEFAULT;

    // then
    assertThat(range.fromId()).isEqualTo(1);
    assertThat(range.toId()).isEqualTo(151);
    assertThat(range.size()).isEqualTo(151);
  }

  @Test
  void shouldListEveryIdInOrder_whenIterated() {
    // when
    var ids = new SyncRange(4, 7).ids().boxed().toList();

    // then
    assertThat(ids).containsExactly(4, 5, 6, 7);
  }

  @ParameterizedTest
  @CsvSource({"0, 10, fromId", "10, 5, toId", "1, 1026, toId", "1, 501, toId"})
  void shouldRejectRange_whenOutOfBounds(int fromId, int toId, String field) {
    // when / then
    assertThatThrownBy(() -> new SyncRange(fromId, toId))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey(field));
  }
}
