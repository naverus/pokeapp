package org.ballastlane.pokeapp.application.port.in;

import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;

/** US01: browse the PokeAPI catalog page by page. */
public interface BrowsePokemonUseCase {

  PageResult<PokemonSummary> browse(PageQuery query);
}
