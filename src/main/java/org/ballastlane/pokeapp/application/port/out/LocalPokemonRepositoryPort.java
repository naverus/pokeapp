package org.ballastlane.pokeapp.application.port.out;

import java.util.Optional;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;

public interface LocalPokemonRepositoryPort {

  /**
   * Inserts a new Pokemon or updates an existing one. Implementations must reject updates whose
   * {@link LocalPokemon#version()} does not match the stored version.
   */
  LocalPokemon save(LocalPokemon pokemon);

  Optional<LocalPokemon> findById(long id);

  boolean existsByPokeApiId(int pokeApiId);

  Optional<LocalPokemon> findByPokeApiId(int pokeApiId);

  PageResult<LocalPokemon> findAll(PageQuery query, String nameFilter);

  boolean deleteById(long id);
}
