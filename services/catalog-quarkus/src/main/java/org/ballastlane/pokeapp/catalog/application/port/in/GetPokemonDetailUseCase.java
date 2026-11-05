package org.ballastlane.pokeapp.catalog.application.port.in;

import io.smallrye.mutiny.Uni;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonDetail;

/** US02: detailed catalog view of one Pokemon. */
public interface GetPokemonDetailUseCase {

  Uni<PokemonDetail> getDetail(String idOrName);
}
