package org.ballastlane.pokeapp.application.service;

import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.SyncPokemonUseCase;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PokemonIdentifier;

public class SyncPokemonService implements SyncPokemonUseCase {

  private final PokemonCatalogPort catalog;
  private final LocalPokemonRepositoryPort repository;

  public SyncPokemonService(PokemonCatalogPort catalog, LocalPokemonRepositoryPort repository) {
    this.catalog = Objects.requireNonNull(catalog);
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public LocalPokemon sync(String idOrName) {
    var identifier = PokemonIdentifier.of(idOrName);
    var detail =
        catalog
            .findDetail(identifier.value())
            .orElseThrow(() -> PokemonNotFoundException.inCatalog(identifier.value()));
    if (repository.existsByPokeApiId(detail.id())) {
      throw new PokemonAlreadySyncedException(detail.id());
    }
    return repository.save(LocalPokemon.fromCatalog(detail));
  }
}
