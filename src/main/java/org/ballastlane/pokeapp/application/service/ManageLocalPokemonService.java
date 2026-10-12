package org.ballastlane.pokeapp.application.service;

import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;

public class ManageLocalPokemonService implements ManageLocalPokemonUseCase {

  private final LocalPokemonRepositoryPort repository;

  public ManageLocalPokemonService(LocalPokemonRepositoryPort repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public PageResult<LocalPokemon> list(PageQuery query, String nameFilter) {
    var filter = nameFilter == null || nameFilter.isBlank() ? null : nameFilter.strip();
    return repository.findAll(query, filter);
  }

  @Override
  public LocalPokemon get(long id) {
    return repository.findById(id).orElseThrow(() -> PokemonNotFoundException.locally(id));
  }

  @Override
  public LocalPokemon create(CreateLocalPokemonCommand command) {
    var pokeApiId = command.pokeApiId();
    if (pokeApiId != null && repository.existsByPokeApiId(pokeApiId)) {
      throw new PokemonAlreadySyncedException(pokeApiId);
    }
    return repository.save(LocalPokemon.create(pokeApiId, command.attributes()));
  }

  @Override
  public LocalPokemon update(UpdateLocalPokemonCommand command) {
    var existing = get(command.id());
    if (existing.version() != command.expectedVersion()) {
      throw new StaleVersionException(command.id(), command.expectedVersion(), existing.version());
    }
    return repository.save(existing.withAttributes(command.attributes()));
  }

  @Override
  public void delete(long id) {
    if (!repository.deleteById(id)) {
      throw PokemonNotFoundException.locally(id);
    }
  }
}
