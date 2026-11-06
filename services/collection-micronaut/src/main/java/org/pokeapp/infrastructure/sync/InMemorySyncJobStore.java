package org.pokeapp.infrastructure.sync;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.pokeapp.application.port.out.SyncJobStorePort;
import org.pokeapp.domain.model.SyncJob;


/**
 * Keeps job snapshots in memory: progress is lost on restart and is not shared between instances,
 * which is acceptable for a single-node deployment. Only the most recent jobs are retained.
 */
@Singleton
public class InMemorySyncJobStore implements SyncJobStorePort {

  static final int MAX_RETAINED_JOBS = 20;

  private final Map<UUID, SyncJob> jobs = new ConcurrentHashMap<>();
  private final AtomicReference<UUID> latestId = new AtomicReference<>();

  @Override
  public SyncJob save(SyncJob job) {
    if (jobs.put(job.id(), job) == null) {
      latestId.set(job.id());
      evictOldFinishedJobs();
    }
    return job;
  }

  @Override
  public Optional<SyncJob> findById(UUID id) {
    return Optional.ofNullable(jobs.get(id));
  }

  @Override
  public Optional<SyncJob> findLatest() {
    return Optional.ofNullable(latestId.get()).map(jobs::get);
  }

  @Override
  public Optional<SyncJob> findRunning() {
    return jobs.values().stream().filter(SyncJob::isRunning).findFirst();
  }

  private void evictOldFinishedJobs() {
    if (jobs.size() <= MAX_RETAINED_JOBS) {
      return;
    }
    jobs.values().stream()
        .filter(job -> !job.isRunning())
        .sorted((left, right) -> left.startedAt().compareTo(right.startedAt()))
        .limit(jobs.size() - MAX_RETAINED_JOBS)
        .map(SyncJob::id)
        .toList()
        .forEach(jobs::remove);
  }
}
