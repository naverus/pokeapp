package org.pokeapp.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.pokeapp.application.port.in.SyncPokemonUseCase;
import org.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.pokeapp.application.port.out.PokemonCatalogPort;
import org.pokeapp.application.port.out.SyncJobStorePort;
import org.pokeapp.application.service.BulkSyncPokemonService;
import org.pokeapp.application.service.ManageLocalPokemonService;
import org.pokeapp.application.service.SyncPokemonService;

/** Wires framework-free use cases to their driven adapters. */
@Factory
public class UseCaseFactory {

  @Singleton
  Clock clock() {
    return Clock.systemUTC();
  }

  /** Background restocks run on virtual threads; the service allows a single running job. */
  @Singleton
  @Named("restockExecutor")
  @Bean(preDestroy = "shutdown")
  ExecutorService restockExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Singleton
  ManageLocalPokemonUseCase manageLocalPokemonUseCase(LocalPokemonRepositoryPort repository) {
    return new ManageLocalPokemonService(repository);
  }

  @Singleton
  SyncPokemonUseCase syncPokemonUseCase(
      PokemonCatalogPort catalog, LocalPokemonRepositoryPort repository) {
    return new SyncPokemonService(catalog, repository);
  }

  @Singleton
  BulkSyncPokemonUseCase bulkSyncPokemonUseCase(
      PokemonCatalogPort catalog,
      LocalPokemonRepositoryPort repository,
      SyncJobStorePort jobs,
      @Named("restockExecutor") ExecutorService executor,
      Clock clock) {
    return new BulkSyncPokemonService(catalog, repository, jobs, executor, clock);
  }
}
