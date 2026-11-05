package org.ballastlane.pokeapp.catalog.application.service;

import io.smallrye.mutiny.Uni;
import java.util.Objects;
import org.ballastlane.pokeapp.catalog.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.catalog.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.catalog.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonIdentifier;

public class GetPokemonDetailService implements GetPokemonDetailUseCase {

  private final PokemonCatalogPort catalog;

  public GetPokemonDetailService(PokemonCatalogPort catalog) {
    this.catalog = Objects.requireNonNull(catalog);
  }

  @Override
  public Uni<PokemonDetail> getDetail(String idOrName) {
    var identifier = PokemonIdentifier.of(idOrName);
    return catalog
        .findDetail(identifier.value())
        .map(
            detail ->
                detail.orElseThrow(() -> PokemonNotFoundException.inCatalog(identifier.value())));
  }
}
