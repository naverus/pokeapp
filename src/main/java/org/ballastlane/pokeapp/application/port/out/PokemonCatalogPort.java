package org.ballastlane.pokeapp.application.port.out;

import java.util.Optional;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;

/** External source of Pokemon data (implemented by the PokeAPI adapter). */
public interface PokemonCatalogPort {

  PageResult<PokemonSummary> findPage(PageQuery query);

  Optional<PokemonDetail> findDetail(String idOrName);
}
