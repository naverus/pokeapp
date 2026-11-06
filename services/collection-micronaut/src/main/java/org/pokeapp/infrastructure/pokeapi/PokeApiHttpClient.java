package org.pokeapp.infrastructure.pokeapi;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import java.util.Optional;
import org.pokeapp.infrastructure.pokeapi.dto.PokeApiDtos;

/** Declarative client over PokeAPI v2 (a 404 maps to an empty Optional); base URL comes from {@code pokeapp.pokeapi.base-url}. */
@Client("${pokeapp.pokeapi.base-url}")
public interface PokeApiHttpClient {

  @Get("/pokemon/{idOrName}")
  Optional<PokeApiDtos.Pokemon> fetchPokemon(String idOrName);

  @Get("/pokemon-species/{speciesId}")
  Optional<PokeApiDtos.Species> fetchSpecies(int speciesId);

  @Get("/evolution-chain/{chainId}")
  Optional<PokeApiDtos.EvolutionChain> fetchEvolutionChain(int chainId);
}
