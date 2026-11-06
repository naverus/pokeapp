package org.pokeapp.application.port.out;

import java.util.Optional;
import org.pokeapp.domain.model.PokemonDetail;

/** External source of Pokemon data (implemented by the PokeAPI adapter). */
public interface PokemonCatalogPort {

  Optional<PokemonDetail> findDetail(String idOrName);
}
