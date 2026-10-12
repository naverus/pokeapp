package org.ballastlane.pokeapp.infrastructure.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.PokemonJpaEntity;
import org.ballastlane.pokeapp.infrastructure.persistence.repository.PokemonJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LocalPokemonPersistenceAdapter implements LocalPokemonRepositoryPort {

  private final PokemonJpaRepository repository;

  public LocalPokemonPersistenceAdapter(PokemonJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public LocalPokemon save(LocalPokemon pokemon) {
    var entity = pokemon.id() == null ? newEntity(pokemon) : existingEntity(pokemon);
    PokemonEntityMapper.copyAttributes(pokemon.attributes(), entity);
    try {
      return PokemonEntityMapper.toDomain(repository.saveAndFlush(entity));
    } catch (DataIntegrityViolationException ex) {
      if (pokemon.pokeApiId() != null) {
        throw new PokemonAlreadySyncedException(pokemon.pokeApiId());
      }
      throw ex;
    } catch (OptimisticLockingFailureException ex) {
      throw new StaleVersionException(pokemon.id(), ex);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LocalPokemon> findById(long id) {
    return repository.findById(id).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByPokeApiId(int pokeApiId) {
    return repository.existsByPokeApiId(pokeApiId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LocalPokemon> findByPokeApiId(int pokeApiId) {
    return repository.findByPokeApiId(pokeApiId).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Integer> findExistingPokeApiIds(Collection<Integer> pokeApiIds) {
    return Set.copyOf(repository.findExistingPokeApiIds(pokeApiIds));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<LocalPokemon> findAll(PageQuery query, String nameFilter) {
    var pageable = PageRequest.of(query.page(), query.size(), Sort.by("id"));
    var page =
        nameFilter == null
            ? repository.findAll(pageable)
            : repository.searchByName(nameFilter, pageable);
    return new PageResult<>(
        page.getContent().stream().map(PokemonEntityMapper::toDomain).toList(),
        query.page(),
        query.size(),
        page.getTotalElements());
  }

  @Override
  @Transactional
  public boolean deleteById(long id) {
    if (!repository.existsById(id)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }

  private static PokemonJpaEntity newEntity(LocalPokemon pokemon) {
    var entity = new PokemonJpaEntity();
    entity.setPokeApiId(pokemon.pokeApiId());
    return entity;
  }

  private PokemonJpaEntity existingEntity(LocalPokemon pokemon) {
    var entity =
        repository
            .findById(pokemon.id())
            .orElseThrow(() -> PokemonNotFoundException.locally(pokemon.id()));
    long storedVersion = entity.getVersion();
    if (storedVersion != pokemon.version()) {
      throw new StaleVersionException(pokemon.id(), pokemon.version(), storedVersion);
    }
    return entity;
  }
}
