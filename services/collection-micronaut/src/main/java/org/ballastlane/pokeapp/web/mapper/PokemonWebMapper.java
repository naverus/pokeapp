package org.ballastlane.pokeapp.web.mapper;

import java.util.function.Function;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.CreateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.LocalPokemonResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PageResponse;
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
}
