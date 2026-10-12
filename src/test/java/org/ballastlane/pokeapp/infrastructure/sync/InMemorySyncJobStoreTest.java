package org.ballastlane.pokeapp.infrastructure.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncRange;
import org.junit.jupiter.api.Test;

class InMemorySyncJobStoreTest {

  private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

  private final InMemorySyncJobStore store = new InMemorySyncJobStore();

  @Test
  void shouldBeEmpty_whenNothingSaved() {
    assertThat(store.findLatest()).isEmpty();
    assertThat(store.findRunning()).isEmpty();
  }

  @Test
  void shouldTrackLatestAndRunningJob_whenSnapshotsSaved() {
    // given
    var first = store.save(SyncJob.start(SyncRange.DEFAULT, "admin", START).complete(START));
    var second = store.save(SyncJob.start(SyncRange.DEFAULT, "admin", START.plusSeconds(1)));

    // when
    var progressed = store.save(second.recordInserted());

    // then
    assertThat(store.findById(first.id())).contains(first);
    assertThat(store.findLatest()).contains(progressed);
    assertThat(store.findRunning()).contains(progressed);
  }

  @Test
  void shouldEvictOldestFinishedJobs_whenRetentionExceeded() {
    // given
    var oldest = store.save(SyncJob.start(SyncRange.DEFAULT, "admin", START).complete(START));

    // when
    for (int i = 1; i <= InMemorySyncJobStore.MAX_RETAINED_JOBS; i++) {
      store.save(SyncJob.start(SyncRange.DEFAULT, "admin", START.plusSeconds(i)).complete(START));
    }

    // then
    assertThat(store.findById(oldest.id())).isEmpty();
    assertThat(store.findLatest()).isPresent();
  }
}
