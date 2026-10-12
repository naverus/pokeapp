package org.ballastlane.pokeapp.config;

import java.time.Clock;
import java.util.concurrent.Executor;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase;
import org.ballastlane.pokeapp.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase;
import org.ballastlane.pokeapp.application.port.in.SyncPokemonUseCase;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.application.port.out.PasswordHasherPort;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.application.port.out.SyncJobStorePort;
import org.ballastlane.pokeapp.application.port.out.TokenIssuerPort;
import org.ballastlane.pokeapp.application.port.out.UserRepositoryPort;
import org.ballastlane.pokeapp.application.service.AuthenticateUserService;
import org.ballastlane.pokeapp.application.service.BrowsePokemonService;
import org.ballastlane.pokeapp.application.service.BulkSyncPokemonService;
import org.ballastlane.pokeapp.application.service.GetPokemonDetailService;
import org.ballastlane.pokeapp.application.service.ManageLocalPokemonService;
import org.ballastlane.pokeapp.application.service.RegisterUserService;
import org.ballastlane.pokeapp.application.service.SyncPokemonService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires framework-free use cases to their driven adapters. */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  BrowsePokemonUseCase browsePokemonUseCase(PokemonCatalogPort catalog) {
    return new BrowsePokemonService(catalog);
  }

  @Bean
  GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonCatalogPort catalog) {
    return new GetPokemonDetailService(catalog);
  }

  @Bean
  SyncPokemonUseCase syncPokemonUseCase(
      PokemonCatalogPort catalog, LocalPokemonRepositoryPort repository) {
    return new SyncPokemonService(catalog, repository);
  }

  /**
   * Runs jobs on Boot's lifecycle-managed {@code applicationTaskExecutor} (virtual threads). A
   * dedicated {@code Executor} bean would make Boot back off from creating that executor.
   */
  @Bean
  BulkSyncPokemonUseCase bulkSyncPokemonUseCase(
      PokemonCatalogPort catalog,
      LocalPokemonRepositoryPort repository,
      SyncJobStorePort jobs,
      @Qualifier("applicationTaskExecutor") Executor executor,
      Clock clock) {
    return new BulkSyncPokemonService(catalog, repository, jobs, executor, clock);
  }

  @Bean
  ManageLocalPokemonUseCase manageLocalPokemonUseCase(LocalPokemonRepositoryPort repository) {
    return new ManageLocalPokemonService(repository);
  }

  @Bean
  RegisterUserUseCase registerUserUseCase(UserRepositoryPort users, PasswordHasherPort hasher) {
    return new RegisterUserService(users, hasher);
  }

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
    return new AuthenticateUserService(users, hasher, tokens);
  }
}
