package org.ballastlane.pokeapp.catalog.application.port.out;

import io.smallrye.mutiny.Uni;
import java.util.Optional;
import org.ballastlane.pokeapp.catalog.domain.model.PageQuery;
import org.ballastlane.pokeapp.catalog.domain.model.PageResult;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonSummary;

/** External source of Pokemon data (implemented by the PokeAPI adapter). */
public interface PokemonCatalogPort {

  Uni<PageResult<PokemonSummary>> findPage(PageQuery query);

  Uni<Optional<PokemonDetail>> findDetail(String idOrName);
}
