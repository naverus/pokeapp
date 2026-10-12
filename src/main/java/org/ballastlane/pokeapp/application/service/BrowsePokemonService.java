package org.ballastlane.pokeapp.application.service;

import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;

public class BrowsePokemonService implements BrowsePokemonUseCase {

  private final PokemonCatalogPort catalog;

  public BrowsePokemonService(PokemonCatalogPort catalog) {
    this.catalog = Objects.requireNonNull(catalog);
  }

  @Override
  public PageResult<PokemonSummary> browse(PageQuery query) {
    return catalog.findPage(query);
  }
}
