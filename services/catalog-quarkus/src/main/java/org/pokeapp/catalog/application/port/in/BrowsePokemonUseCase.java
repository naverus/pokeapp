package org.pokeapp.catalog.application.port.in;

import io.smallrye.mutiny.Uni;
import org.pokeapp.catalog.domain.model.PageQuery;
import org.pokeapp.catalog.domain.model.PageResult;
import org.pokeapp.catalog.domain.model.PokemonSummary;

/** US01: browse the PokeAPI catalog page by page. */
public interface BrowsePokemonUseCase {

  Uni<PageResult<PokemonSummary>> browse(PageQuery query);
}
