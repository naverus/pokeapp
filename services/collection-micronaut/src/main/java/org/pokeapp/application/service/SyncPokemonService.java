package org.pokeapp.application.service;

import java.util.Objects;
import org.pokeapp.application.port.in.SyncPokemonUseCase;
import org.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.pokeapp.application.port.out.PokemonCatalogPort;
import org.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.pokeapp.domain.exception.PokemonNotFoundException;
import org.pokeapp.domain.model.LocalPokemon;
import org.pokeapp.domain.model.PokemonIdentifier;

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
