package org.pokeapp.catalog.web.dto;

import java.util.List;

/** HTTP contract of the catalog endpoints; field names match the Spring monolith exactly. */
public final class PokemonDtos {

  private PokemonDtos() {}

  public record PageResponse<T>(
      List<T> content, int page, int size, long totalElements, int totalPages) {}

  public record PokemonSummaryResponse(
      int id,
      String name,
      String spriteUrl,
      String category,
      double weightKg,
      List<String> abilities,
      List<String> types) {}

  public record StatResponse(String name, int baseValue) {}

  public record EvolutionStageResponse(int speciesId, String name, int stage, String spriteUrl) {}

  public record PokemonDetailResponse(
      int id,
      String name,
      String imageUrl,
      String category,
      double heightM,
      double weightKg,
      List<String> types,
      List<String> abilities,
      List<StatResponse> stats,
      int totalBaseStats,
      String description,
      List<EvolutionStageResponse> evolutionChain) {}
}
