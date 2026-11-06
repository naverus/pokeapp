package org.pokeapp.catalog.application.service;

import io.smallrye.mutiny.Uni;
import java.util.Objects;
import org.pokeapp.catalog.application.port.in.BrowsePokemonUseCase;
import org.pokeapp.catalog.application.port.out.PokemonCatalogPort;
import org.pokeapp.catalog.domain.model.PageQuery;
import org.pokeapp.catalog.domain.model.PageResult;
import org.pokeapp.catalog.domain.model.PokemonSummary;

public class BrowsePokemonService implements BrowsePokemonUseCase {

  private final PokemonCatalogPort catalog;

  public BrowsePokemonService(PokemonCatalogPort catalog) {
    this.catalog = Objects.requireNonNull(catalog);
  }

  @Override
  public Uni<PageResult<PokemonSummary>> browse(PageQuery query) {
    return catalog.findPage(query);
  }
}
