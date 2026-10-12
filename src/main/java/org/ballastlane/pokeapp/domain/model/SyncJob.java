package org.ballastlane.pokeapp.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Progress snapshot of a bulk PokeAPI-to-local synchronization. Every transition is a copy. */
public record SyncJob(
    UUID id,
    SyncJobStatus status,
    SyncRange range,
    String requestedBy,
    int inserted,
    int updated,
    int failed,
    List<String> errors,
    Instant startedAt,
    Instant finishedAt) {

  public static final int MAX_ERRORS = 20;

  public SyncJob {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(range, "range");
    Objects.requireNonNull(startedAt, "startedAt");
    errors = List.copyOf(errors);
  }

  public static SyncJob start(SyncRange range, String requestedBy, Instant now) {
    return new SyncJob(
        UUID.randomUUID(),
        SyncJobStatus.RUNNING,
        range,
        requestedBy,
        0,
        0,
        0,
        List.of(),
        now,
        null);
  }

  public int total() {
    return range.size();
  }

  public int processed() {
    return inserted + updated + failed;
  }

  public boolean isRunning() {
    return status == SyncJobStatus.RUNNING;
  }

  public SyncJob recordInserted() {
    return new SyncJob(
        id, status, range, requestedBy, inserted + 1, updated, failed, errors, startedAt, null);
  }

  public SyncJob recordUpdated() {
    return new SyncJob(
        id, status, range, requestedBy, inserted, updated + 1, failed, errors, startedAt, null);
  }

  public SyncJob recordFailed(int pokeApiId, String reason) {
    return new SyncJob(
        id,
        status,
        range,
        requestedBy,
        inserted,
        updated,
        failed + 1,
        appendError("#%d: %s".formatted(pokeApiId, reason)),
        startedAt,
        null);
  }

  public SyncJob complete(Instant now) {
    return new SyncJob(
        id,
        SyncJobStatus.COMPLETED,
        range,
        requestedBy,
        inserted,
        updated,
        failed,
        errors,
        startedAt,
        now);
  }

  public SyncJob fail(String reason, Instant now) {
    return new SyncJob(
        id,
        SyncJobStatus.FAILED,
        range,
        requestedBy,
        inserted,
        updated,
        failed,
        appendError(reason),
        startedAt,
        now);
  }

  private List<String> appendError(String message) {
    if (errors.size() >= MAX_ERRORS) {
      return errors;
    }
    var copy = new ArrayList<>(errors);
    copy.add(message);
    return copy;
  }
}
