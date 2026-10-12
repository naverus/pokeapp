package org.ballastlane.pokeapp.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.application.port.out.SyncJobStorePort;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.exception.SyncAlreadyRunningException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncJobStatus;
import org.ballastlane.pokeapp.domain.model.SyncRange;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkSyncPokemonServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PokemonCatalogPort catalog;
  @Mock private LocalPokemonRepositoryPort repository;

  private final FakeSyncJobStore store = new FakeSyncJobStore();
  private final List<Runnable> queued = new ArrayList<>();

  private BulkSyncPokemonService service;

  @BeforeEach
  void setUp() {
    service = newService(Runnable::run);
  }

  @Test
  void shouldInsertMissingAndRefreshStoredPokemon_whenJobRuns() {
    // given
    given(catalog.findDetail("1")).willReturn(Optional.of(detail(1, "bulbasaur")));
    given(catalog.findDetail("2")).willReturn(Optional.of(detail(2, "ivysaur")));
    given(repository.findByPokeApiId(1)).willReturn(Optional.empty());
    given(repository.findByPokeApiId(2)).willReturn(Optional.of(stored(2, "old-ivysaur")));
    given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

    // when
    var started = service.start(new SyncRange(1, 2), "admin");

    // then
    assertThat(started.status()).isEqualTo(SyncJobStatus.RUNNING);
    var finished = service.find(started.id()).orElseThrow();
    assertThat(finished.status()).isEqualTo(SyncJobStatus.COMPLETED);
    assertThat(finished.inserted()).isEqualTo(1);
    assertThat(finished.updated()).isEqualTo(1);
    assertThat(finished.failed()).isZero();
    assertThat(finished.finishedAt()).isEqualTo(NOW);
    assertThat(finished.requestedBy()).isEqualTo("admin");

    var captor = ArgumentCaptor.forClass(LocalPokemon.class);
    verify(repository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues().get(0).id()).isNull();
    var refreshed = captor.getAllValues().get(1);
    assertThat(refreshed.id()).isEqualTo(2L);
    assertThat(refreshed.attributes().name()).isEqualTo("ivysaur");
    assertThat(refreshed.attributes().tags()).containsExactly("starter");
  }

  @Test
  void shouldContinueAndCountFailures_whenSomeIdsFail() {
    // given
    given(catalog.findDetail("1")).willReturn(Optional.empty());
    given(catalog.findDetail("2")).willThrow(new CatalogUnavailableException("PokeAPI down", null));
    given(catalog.findDetail("3")).willReturn(Optional.of(detail(3, "venusaur")));
    given(repository.findByPokeApiId(3)).willReturn(Optional.empty());
    given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

    // when
    var job = service.start(new SyncRange(1, 3), "admin");

    // then
    var finished = service.find(job.id()).orElseThrow();
    assertThat(finished.status()).isEqualTo(SyncJobStatus.COMPLETED);
    assertThat(finished.inserted()).isEqualTo(1);
    assertThat(finished.failed()).isEqualTo(2);
    assertThat(finished.errors()).containsExactly("#1: not found in PokeAPI", "#2: PokeAPI down");
  }

  @Test
  void shouldRetryOnce_whenStoredPokemonChangedConcurrently() {
    // given
    given(catalog.findDetail("2")).willReturn(Optional.of(detail(2, "ivysaur")));
    given(repository.findByPokeApiId(2)).willReturn(Optional.of(stored(2, "ivysaur")));
    given(repository.save(any()))
        .willThrow(new StaleVersionException(2L, 0L, 1L))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    var job = service.start(new SyncRange(2, 2), "admin");

    // then
    var finished = service.find(job.id()).orElseThrow();
    assertThat(finished.updated()).isEqualTo(1);
    assertThat(finished.failed()).isZero();
    verify(repository, times(2)).findByPokeApiId(2);
  }

  @Test
  void shouldCountFailure_whenConflictPersistsAfterRetry() {
    // given
    given(catalog.findDetail("2")).willReturn(Optional.of(detail(2, "ivysaur")));
    given(repository.findByPokeApiId(2)).willReturn(Optional.of(stored(2, "ivysaur")));
    given(repository.save(any())).willThrow(new StaleVersionException(2L, 0L, 1L));

    // when
    var job = service.start(new SyncRange(2, 2), "admin");

    // then
    var finished = service.find(job.id()).orElseThrow();
    assertThat(finished.failed()).isEqualTo(1);
    verify(repository, times(2)).save(any());
  }

  @Test
  void shouldRejectStart_whenAnotherJobIsRunning() {
    // given
    service = newService(queued::add);
    var running = service.start(new SyncRange(1, 5), "admin");

    // when / then
    assertThatThrownBy(() -> service.start(new SyncRange(1, 5), "admin"))
        .isInstanceOf(SyncAlreadyRunningException.class)
        .hasMessageContaining(running.id().toString());
    assertThat(service.latest()).contains(running);
  }

  @Test
  void shouldMarkJobFailed_whenExecutorRejectsIt() {
    // given
    service =
        newService(
            task -> {
              throw new RejectedExecutionException("shutting down");
            });

    // when
    var job = service.start(new SyncRange(1, 5), "admin");

    // then
    assertThat(job.status()).isEqualTo(SyncJobStatus.FAILED);
    assertThat(service.find(job.id()).orElseThrow().status()).isEqualTo(SyncJobStatus.FAILED);
  }

  @Test
  void shouldMarkJobFailed_whenStoreBreaksMidway() {
    // given
    given(catalog.findDetail("1")).willReturn(Optional.of(detail(1, "bulbasaur")));
    given(repository.findByPokeApiId(1)).willReturn(Optional.empty());
    given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
    store.failOnProgress = true;

    // when
    var job = service.start(new SyncRange(1, 2), "admin");

    // then
    var finished = service.find(job.id()).orElseThrow();
    assertThat(finished.status()).isEqualTo(SyncJobStatus.FAILED);
    assertThat(finished.errors()).anyMatch(error -> error.contains("store unavailable"));
  }

  private BulkSyncPokemonService newService(Executor executor) {
    return new BulkSyncPokemonService(
        catalog, repository, store, executor, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static PokemonDetail detail(int id, String name) {
    var pikachu = PokemonFixtures.pikachuDetail();
    return new PokemonDetail(
        id,
        name,
        pikachu.imageUrl(),
        pikachu.category(),
        pikachu.heightM(),
        pikachu.weightKg(),
        List.of("grass"),
        List.of("overgrow"),
        pikachu.stats(),
        pikachu.description(),
        List.of());
  }

  private static LocalPokemon stored(int pokeApiId, String name) {
    var base = PokemonFixtures.pikachuAttributes();
    var attributes =
        new PokemonAttributes(
            name,
            base.localName(),
            base.region(),
            base.category(),
            base.heightM(),
            base.weightKg(),
            base.spriteUrl(),
            base.description(),
            base.types(),
            base.abilities(),
            base.tags());
    return new LocalPokemon((long) pokeApiId, pokeApiId, attributes, 0L, NOW, NOW);
  }

  private static final class FakeSyncJobStore implements SyncJobStorePort {

    private final Map<UUID, SyncJob> jobs = new ConcurrentHashMap<>();
    private UUID latestId;
    private boolean failOnProgress;

    @Override
    public SyncJob save(SyncJob job) {
      if (failOnProgress && job.processed() > 0 && job.isRunning()) {
        throw new IllegalStateException("store unavailable");
      }
      if (jobs.put(job.id(), job) == null) {
        latestId = job.id();
      }
      return job;
    }

    @Override
    public Optional<SyncJob> findById(UUID id) {
      return Optional.ofNullable(jobs.get(id));
    }

    @Override
    public Optional<SyncJob> findLatest() {
      return latestId == null ? Optional.empty() : findById(latestId);
    }

    @Override
    public Optional<SyncJob> findRunning() {
      return jobs.values().stream().filter(SyncJob::isRunning).findFirst();
    }
  }
}
