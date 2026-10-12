package org.ballastlane.pokeapp.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

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

  public record StoredPokeApiIdsResponse(List<Integer> pokeApiIds) {}

  public record LocalPokemonResponse(
      long id,
      Integer pokeApiId,
      String name,
      String localName,
      String region,
      String category,
      double heightM,
      double weightKg,
      String spriteUrl,
      String description,
      List<String> types,
      List<String> abilities,
      List<String> tags,
      long version,
      Instant createdAt,
      Instant updatedAt) {}

  public record CreateLocalPokemonRequest(
      @Positive Integer pokeApiId,
      @NotBlank @Size(max = 100) String name,
      @Size(max = 100) String localName,
      @Size(max = 50) String region,
      @Size(max = 100) String category,
      @NotNull @PositiveOrZero @DecimalMax("100.0") Double heightM,
      @NotNull @PositiveOrZero @DecimalMax("10000.0") Double weightKg,
      @Size(max = 500) String spriteUrl,
      @Size(max = 1000) String description,
      @Size(max = 5) List<@NotBlank @Size(max = 30) String> types,
      @Size(max = 10) List<@NotBlank @Size(max = 50) String> abilities,
      @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags) {}

  public record UpdateLocalPokemonRequest(
      @NotNull @PositiveOrZero Long version,
      @NotBlank @Size(max = 100) String name,
      @Size(max = 100) String localName,
      @Size(max = 50) String region,
      @Size(max = 100) String category,
      @NotNull @PositiveOrZero @DecimalMax("100.0") Double heightM,
      @NotNull @PositiveOrZero @DecimalMax("10000.0") Double weightKg,
      @Size(max = 500) String spriteUrl,
      @Size(max = 1000) String description,
      @Size(max = 5) List<@NotBlank @Size(max = 30) String> types,
      @Size(max = 10) List<@NotBlank @Size(max = 50) String> abilities,
      @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags) {}
}
