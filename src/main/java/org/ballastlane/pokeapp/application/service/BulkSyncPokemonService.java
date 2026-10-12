package org.ballastlane.pokeapp.application.service;

import java.lang.System.Logger.Level;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.ballastlane.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.application.port.out.SyncJobStorePort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.exception.SyncAlreadyRunningException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncRange;

public class BulkSyncPokemonService implements BulkSyncPokemonUseCase {

  private static final System.Logger log = System.getLogger(BulkSyncPokemonService.class.getName());
  private static final int MAX_ATTEMPTS = 2;

  private enum Outcome {
    INSERTED,
    UPDATED
  }

  private final PokemonCatalogPort catalog;
  private final LocalPokemonRepositoryPort repository;
  private final SyncJobStorePort jobs;
  private final Executor executor;
  private final Clock clock;
  private final Object startLock = new Object();

  public BulkSyncPokemonService(
      PokemonCatalogPort catalog,
      LocalPokemonRepositoryPort repository,
      SyncJobStorePort jobs,
      Executor executor,
      Clock clock) {
    this.catalog = Objects.requireNonNull(catalog);
    this.repository = Objects.requireNonNull(repository);
    this.jobs = Objects.requireNonNull(jobs);
    this.executor = Objects.requireNonNull(executor);
    this.clock = Objects.requireNonNull(clock);
  }

  @Override
  public SyncJob start(SyncRange range, String requestedBy) {
    Objects.requireNonNull(range, "range");
    SyncJob job;
    synchronized (startLock) {
      jobs.findRunning()
          .ifPresent(
              running -> {
                throw new SyncAlreadyRunningException(running.id());
              });
      job = jobs.save(SyncJob.start(range, requestedBy, clock.instant()));
    }
    try {
      executor.execute(() -> run(job));
    } catch (RejectedExecutionException ex) {
      return jobs.save(job.fail("Could not schedule the synchronization", clock.instant()));
    }
    return job;
  }

  @Override
  public Optional<SyncJob> find(UUID id) {
    return jobs.findById(id);
  }

  @Override
  public Optional<SyncJob> latest() {
    return jobs.findLatest();
  }

  private void run(SyncJob job) {
    var current = job;
    try {
      for (int pokeApiId : job.range().ids().toArray()) {
        current = jobs.save(syncOne(current, pokeApiId));
      }
      jobs.save(current.complete(clock.instant()));
    } catch (RuntimeException ex) {
      log.log(Level.ERROR, "Pokemon synchronization " + job.id() + " aborted", ex);
      jobs.save(current.fail("Synchronization aborted: " + ex.getMessage(), clock.instant()));
    }
  }

  private SyncJob syncOne(SyncJob job, int pokeApiId) {
    try {
      var detail = catalog.findDetail(String.valueOf(pokeApiId));
      if (detail.isEmpty()) {
        return job.recordFailed(pokeApiId, "not found in PokeAPI");
      }
      return upsert(detail.get()) == Outcome.INSERTED ? job.recordInserted() : job.recordUpdated();
    } catch (RuntimeException ex) {
      return job.recordFailed(pokeApiId, ex.getMessage());
    }
  }

  /** Re-reads and retries once when a concurrent edit or insert wins the race. */
  private Outcome upsert(PokemonDetail detail) {
    for (int attempt = 1; ; attempt++) {
      try {
        var existing = repository.findByPokeApiId(detail.id());
        if (existing.isEmpty()) {
          repository.save(LocalPokemon.fromCatalog(detail));
          return Outcome.INSERTED;
        }
        repository.save(existing.get().refreshFromCatalog(detail));
        return Outcome.UPDATED;
      } catch (StaleVersionException | PokemonAlreadySyncedException ex) {
        if (attempt >= MAX_ATTEMPTS) {
          throw ex;
        }
      }
    }
  }
}
