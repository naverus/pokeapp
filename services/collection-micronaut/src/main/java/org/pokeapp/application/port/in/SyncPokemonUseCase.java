package org.pokeapp.application.port.in;

import org.pokeapp.domain.model.LocalPokemon;

/** US03: replicate a catalog Pokemon into the local store. */
public interface SyncPokemonUseCase {

  LocalPokemon sync(String idOrName);
}
