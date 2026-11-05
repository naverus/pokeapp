package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SyncJobTest {

  private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
  private static final Instant END = Instant.parse("2026-01-01T00:01:00Z");

  @Test
  void shouldStartRunningWithZeroCounters_whenStarted() {
    // when
    var job = SyncJob.start(new SyncRange(1, 10), "admin", START);

    // then
    assertThat(job.id()).isNotNull();
    assertThat(job.status()).isEqualTo(SyncJobStatus.RUNNING);
    assertThat(job.isRunning()).isTrue();
    assertThat(job.total()).isEqualTo(10);
    assertThat(job.processed()).isZero();
    assertThat(job.requestedBy()).isEqualTo("admin");
    assertThat(job.startedAt()).isEqualTo(START);
    assertThat(job.finishedAt()).isNull();
  }

  @Test
  void shouldCountEachOutcome_whenItemsRecorded() {
    // given
    var job = SyncJob.start(new SyncRange(1, 3), "admin", START);

    // when
    var progressed = job.recordInserted().recordUpdated().recordFailed(3, "boom");

    // then
    assertThat(progressed.inserted()).isEqualTo(1);
    assertThat(progressed.updated()).isEqualTo(1);
    assertThat(progressed.failed()).isEqualTo(1);
    assertThat(progressed.processed()).isEqualTo(3);
    assertThat(progressed.errors()).containsExactly("#3: boom");
    assertThat(job.processed()).isZero();
  }

  @Test
  void shouldCapErrorMessages_whenManyItemsFail() {
    // given
    var job = SyncJob.start(new SyncRange(1, 30), "admin", START);

    // when
    for (int id = 1; id <= 30; id++) {
      job = job.recordFailed(id, "boom");
    }

    // then
    assertThat(job.failed()).isEqualTo(30);
    assertThat(job.errors()).hasSize(SyncJob.MAX_ERRORS).first().isEqualTo("#1: boom");
  }

  @Test
  void shouldFinish_whenCompletedOrFailed() {
    // given
    var job = SyncJob.start(new SyncRange(1, 3), "admin", START);

    // when
    var completed = job.complete(END);
    var failed = job.fail("database down", END);

    // then
    assertThat(completed.status()).isEqualTo(SyncJobStatus.COMPLETED);
    assertThat(completed.finishedAt()).isEqualTo(END);
    assertThat(completed.isRunning()).isFalse();
    assertThat(failed.status()).isEqualTo(SyncJobStatus.FAILED);
    assertThat(failed.errors()).containsExactly("database down");
  }
}
