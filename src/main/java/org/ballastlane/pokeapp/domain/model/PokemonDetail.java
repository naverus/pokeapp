package org.ballastlane.pokeapp.domain.model;

import java.util.List;

/** Full catalog data of a Pokemon (US02). */
public record PokemonDetail(
    int id,
    String name,
    String imageUrl,
    String category,
    double heightM,
    double weightKg,
    List<String> types,
    List<String> abilities,
    List<Stat> stats,
    String description,
    List<EvolutionStage> evolutionChain) {

  public PokemonDetail {
    types = Text.immutable(types);
    abilities = Text.immutable(abilities);
    stats = stats == null ? List.of() : List.copyOf(stats);
    evolutionChain = evolutionChain == null ? List.of() : List.copyOf(evolutionChain);
  }

  public int totalBaseStats() {
    return stats.stream().mapToInt(Stat::baseValue).sum();
  }
}
