package org.ballastlane.pokeapp.infrastructure.persistence;

import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
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
import org.hibernate.exception.ConstraintViolationException;

@Singleton
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
    } catch (RuntimeException ex) {
      if (isOptimisticLock(ex) && pokemon.id() != null) {
        throw new StaleVersionException(pokemon.id(), ex);
      }
      if (isConstraintViolation(ex) && pokemon.pokeApiId() != null) {
        throw new PokemonAlreadySyncedException(pokemon.pokeApiId());
      }
      throw ex;
    }
  }

  @Override
  @ReadOnly
  public Optional<LocalPokemon> findById(long id) {
    return repository.findById(id).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @ReadOnly
  public boolean existsByPokeApiId(int pokeApiId) {
    return repository.existsByPokeApiId(pokeApiId);
  }

  @Override
  @ReadOnly
  public Optional<LocalPokemon> findByPokeApiId(int pokeApiId) {
    return repository.findByPokeApiId(pokeApiId).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @ReadOnly
  public Set<Integer> findExistingPokeApiIds(Collection<Integer> pokeApiIds) {
    return Set.copyOf(repository.findExistingPokeApiIds(pokeApiIds));
  }

  @Override
  @ReadOnly
  public PageResult<LocalPokemon> findAll(PageQuery query, String nameFilter) {
    var pageable = Pageable.from(query.page(), query.size(), Sort.of(Sort.Order.asc("id")));
    var page =
        nameFilter == null
            ? repository.findAll(pageable)
            : repository.searchByName(nameFilter, pageable);
    return new PageResult<>(
        page.getContent().stream().map(PokemonEntityMapper::toDomain).toList(),
        query.page(),
        query.size(),
        page.getTotalSize());
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

  private static boolean isOptimisticLock(Throwable ex) {
    return causedBy(ex, io.micronaut.data.exceptions.OptimisticLockException.class)
        || causedBy(ex, jakarta.persistence.OptimisticLockException.class)
        || causedBy(ex, org.hibernate.StaleStateException.class);
  }

  private static boolean isConstraintViolation(Throwable ex) {
    return causedBy(ex, ConstraintViolationException.class)
        || causedBy(ex, SQLIntegrityConstraintViolationException.class);
  }

  private static boolean causedBy(Throwable ex, Class<? extends Throwable> type) {
    for (var current = ex; current != null; current = current.getCause()) {
      if (type.isInstance(current)) {
        return true;
      }
      if (current.getCause() == current) {
        break;
      }
    }
    return false;
  }
}
