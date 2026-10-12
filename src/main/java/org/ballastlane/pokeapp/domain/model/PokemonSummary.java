package org.ballastlane.pokeapp.domain.model;

import java.util.List;

/** Card data shown when browsing the PokeAPI catalog (US01). */
public record PokemonSummary(
    int id,
    String name,
    String spriteUrl,
    String category,
    double weightKg,
    List<String> abilities,
    List<String> types) {

  public PokemonSummary {
    abilities = Text.immutable(abilities);
    types = Text.immutable(types);
  }
}
