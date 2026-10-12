package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PageQueryTest {

  @Test
  void shouldComputeOffset_whenValid() {
    assertThat(new PageQuery(2, 20).offset()).isEqualTo(40);
  }

  @ParameterizedTest
  @CsvSource({"-1, 20, page", "0, 0, size", "0, 101, size"})
  void shouldReject_whenOutOfRange(int page, int size, String field) {
    assertThatThrownBy(() -> new PageQuery(page, size))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey(field));
  }

  @Test
  void shouldComputeTotalPages_whenPageResultCreated() {
    var result = new PageResult<>(List.of("a", "b"), 0, 2, 5);

    assertThat(result.totalPages()).isEqualTo(3);
    assertThat(new PageResult<>(List.of(), 0, 10, 0).totalPages()).isZero();
  }

  @Test
  void shouldMapContent_whenPageResultMapped() {
    var result = new PageResult<>(List.of(1, 2), 1, 2, 10).map(String::valueOf);

    assertThat(result.content()).containsExactly("1", "2");
    assertThat(result.page()).isEqualTo(1);
    assertThat(result.totalElements()).isEqualTo(10);
  }
}
