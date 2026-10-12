package org.ballastlane.pokeapp.application.port.out;

import java.util.Optional;
import java.util.UUID;
import org.ballastlane.pokeapp.domain.model.SyncJob;

public interface SyncJobStorePort {

  /** Inserts or replaces the snapshot with the same id. */
  SyncJob save(SyncJob job);

  Optional<SyncJob> findById(UUID id);

  /** The most recently started job, whatever its status. */
  Optional<SyncJob> findLatest();

  Optional<SyncJob> findRunning();
}
