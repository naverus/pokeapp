package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

class PokemonAttributesTest {

  @Test
  void shouldNormalizeTextAndCollections_whenCreated() {
    // when
    var attributes =
        new PokemonAttributes(
            "  Pikachu ",
            "  ",
            " Kanto ",
            "Mouse Pokemon",
            0.4,
            6.0,
            "https://img/25.png",
            "Electric mouse",
            List.of(" Electric "),
            List.of("Static", "lightning-rod"),
            List.of("Starter", "starter", " Fan-Favorite "));

    // then
    assertThat(attributes.name()).isEqualTo("Pikachu");
    assertThat(attributes.localName()).isNull();
    assertThat(attributes.region()).isEqualTo("Kanto");
    assertThat(attributes.types()).containsExactly("electric");
    assertThat(attributes.abilities()).containsExactly("static", "lightning-rod");
    assertThat(attributes.tags()).containsExactly("starter", "fan-favorite");
  }

  @Test
  void shouldTreatNullCollectionsAsEmpty_whenCreated() {
    // when
    var attributes =
        new PokemonAttributes("Mew", null, null, null, 0.4, 4.0, null, null, null, null, null);

    // then
    assertThat(attributes.types()).isEmpty();
    assertThat(attributes.abilities()).isEmpty();
    assertThat(attributes.tags()).isEmpty();
  }

  @Test
  void shouldCollectAllViolations_whenAttributesAreInvalid() {
    // given
    var tooManyTags = Collections.nCopies(11, "x").stream().map(t -> t + Math.random()).toList();

    // when / then
    assertThatThrownBy(
            () ->
                new PokemonAttributes(
                    " ",
                    "a".repeat(101),
                    "r".repeat(51),
                    null,
                    -1,
                    -2,
                    null,
                    "d".repeat(1001),
                    List.of(),
                    List.of(),
                    tooManyTags))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            ex ->
                assertThat(ex.errors())
                    .containsOnlyKeys(
                        "name",
                        "localName",
                        "region",
                        "heightM",
                        "weightKg",
                        "description",
                        "tags"));
  }

  @Test
  void shouldRejectTooLongTag_whenCreated() {
    assertThatThrownBy(
            () ->
                new PokemonAttributes(
                    "Mew",
                    null,
                    null,
                    null,
                    0.4,
                    4.0,
                    null,
                    null,
                    null,
                    null,
                    List.of("t".repeat(31))))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey("tags"));
  }
}
