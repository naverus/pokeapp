package org.ballastlane.pokeapp.application.port.in;

import org.ballastlane.pokeapp.domain.model.PokemonDetail;

/** US02: detailed catalog view of one Pokemon. */
public interface GetPokemonDetailUseCase {

  PokemonDetail getDetail(String idOrName);
}
