package org.ballastlane.pokeapp.catalog.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.ballastlane.pokeapp.catalog.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.catalog.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.catalog.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.catalog.application.service.BrowsePokemonService;
import org.ballastlane.pokeapp.catalog.application.service.GetPokemonDetailService;

/** Wires the framework-free application services into the CDI container. */
public class UseCaseConfig {

  @Produces
  @ApplicationScoped
  BrowsePokemonUseCase browsePokemon(PokemonCatalogPort catalog) {
    return new BrowsePokemonService(catalog);
  }

  @Produces
  @ApplicationScoped
  GetPokemonDetailUseCase getPokemonDetail(PokemonCatalogPort catalog) {
    return new GetPokemonDetailService(catalog);
  }
}
