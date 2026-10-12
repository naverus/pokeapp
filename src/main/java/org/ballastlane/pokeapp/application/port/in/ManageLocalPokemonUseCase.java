package org.ballastlane.pokeapp.application.port.in;

import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;

/** CRUD over locally stored Pokemon, including US04 (update). */
public interface ManageLocalPokemonUseCase {

  PageResult<LocalPokemon> list(PageQuery query, String nameFilter);

  LocalPokemon get(long id);

  LocalPokemon create(CreateLocalPokemonCommand command);

  LocalPokemon update(UpdateLocalPokemonCommand command);

  void delete(long id);

  record CreateLocalPokemonCommand(Integer pokeApiId, PokemonAttributes attributes) {}

  record UpdateLocalPokemonCommand(long id, long expectedVersion, PokemonAttributes attributes) {}
}
