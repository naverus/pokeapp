package org.ballastlane.pokeapp.application.service;

import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonIdentifier;

public class GetPokemonDetailService implements GetPokemonDetailUseCase {

  private final PokemonCatalogPort catalog;

  public GetPokemonDetailService(PokemonCatalogPort catalog) {
    this.catalog = Objects.requireNonNull(catalog);
  }

  @Override
  public PokemonDetail getDetail(String idOrName) {
    var identifier = PokemonIdentifier.of(idOrName);
    return catalog
        .findDetail(identifier.value())
        .orElseThrow(() -> PokemonNotFoundException.inCatalog(identifier.value()));
  }
}
