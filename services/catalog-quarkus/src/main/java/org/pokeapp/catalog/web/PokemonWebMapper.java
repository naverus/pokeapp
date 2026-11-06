package org.pokeapp.catalog.web;

import java.util.function.Function;
import org.pokeapp.catalog.domain.model.EvolutionStage;
import org.pokeapp.catalog.domain.model.PageResult;
import org.pokeapp.catalog.domain.model.PokemonDetail;
import org.pokeapp.catalog.domain.model.PokemonSummary;
import org.pokeapp.catalog.domain.model.Stat;
import org.pokeapp.catalog.web.dto.PokemonDtos.EvolutionStageResponse;
import org.pokeapp.catalog.web.dto.PokemonDtos.PageResponse;
import org.pokeapp.catalog.web.dto.PokemonDtos.PokemonDetailResponse;
import org.pokeapp.catalog.web.dto.PokemonDtos.PokemonSummaryResponse;
import org.pokeapp.catalog.web.dto.PokemonDtos.StatResponse;

public final class PokemonWebMapper {

  private PokemonWebMapper() {}

  public static <T, R> PageResponse<R> toPageResponse(
      PageResult<T> page, Function<? super T, ? extends R> mapper) {
    PageResult<R> mapped = page.<R>map(mapper);
    return new PageResponse<>(
        mapped.content(),
        mapped.page(),
        mapped.size(),
        mapped.totalElements(),
        mapped.totalPages());
  }

  public static PokemonSummaryResponse toResponse(PokemonSummary summary) {
    return new PokemonSummaryResponse(
        summary.id(),
        summary.name(),
        summary.spriteUrl(),
        summary.category(),
        summary.weightKg(),
        summary.abilities(),
        summary.types());
  }

  public static PokemonDetailResponse toResponse(PokemonDetail detail) {
    return new PokemonDetailResponse(
        detail.id(),
        detail.name(),
        detail.imageUrl(),
        detail.category(),
        detail.heightM(),
        detail.weightKg(),
        detail.types(),
        detail.abilities(),
        detail.stats().stream().map(PokemonWebMapper::toResponse).toList(),
        detail.totalBaseStats(),
        detail.description(),
        detail.evolutionChain().stream().map(PokemonWebMapper::toResponse).toList());
  }

  private static StatResponse toResponse(Stat stat) {
    return new StatResponse(stat.name(), stat.baseValue());
  }

  private static EvolutionStageResponse toResponse(EvolutionStage stage) {
    return new EvolutionStageResponse(
        stage.speciesId(), stage.name(), stage.stage(), stage.spriteUrl());
  }
}
