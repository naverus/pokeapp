package org.ballastlane.pokeapp.web.mapper;

import java.util.function.Function;
import org.ballastlane.pokeapp.domain.model.EvolutionStage;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;
import org.ballastlane.pokeapp.domain.model.Stat;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.CreateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.EvolutionStageResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.LocalPokemonResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PageResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PokemonDetailResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PokemonSummaryResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.StatResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.UpdateLocalPokemonRequest;

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

  public static LocalPokemonResponse toResponse(LocalPokemon pokemon) {
    var attributes = pokemon.attributes();
    return new LocalPokemonResponse(
        pokemon.id(),
        pokemon.pokeApiId(),
        attributes.name(),
        attributes.localName(),
        attributes.region(),
        attributes.category(),
        attributes.heightM(),
        attributes.weightKg(),
        attributes.spriteUrl(),
        attributes.description(),
        attributes.types(),
        attributes.abilities(),
        attributes.tags(),
        pokemon.version(),
        pokemon.createdAt(),
        pokemon.updatedAt());
  }

  public static PokemonAttributes toAttributes(CreateLocalPokemonRequest request) {
    return new PokemonAttributes(
        request.name(),
        request.localName(),
        request.region(),
        request.category(),
        request.heightM(),
        request.weightKg(),
        request.spriteUrl(),
        request.description(),
        request.types(),
        request.abilities(),
        request.tags());
  }

  public static PokemonAttributes toAttributes(UpdateLocalPokemonRequest request) {
    return new PokemonAttributes(
        request.name(),
        request.localName(),
        request.region(),
        request.category(),
        request.heightM(),
        request.weightKg(),
        request.spriteUrl(),
        request.description(),
        request.types(),
        request.abilities(),
        request.tags());
  }

  private static StatResponse toResponse(Stat stat) {
    return new StatResponse(stat.name(), stat.baseValue());
  }

  private static EvolutionStageResponse toResponse(EvolutionStage stage) {
    return new EvolutionStageResponse(
        stage.speciesId(), stage.name(), stage.stage(), stage.spriteUrl());
  }
}
