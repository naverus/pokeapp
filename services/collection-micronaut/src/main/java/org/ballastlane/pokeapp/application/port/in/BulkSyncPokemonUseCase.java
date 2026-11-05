package org.ballastlane.pokeapp.application.port.in;

import java.util.Optional;
import java.util.UUID;
import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncRange;

/**
 * US03 (admin, on demand): replicate a range of catalog Pokemon into the local store in the
 * background, inserting missing ones and refreshing the catalog fields of stored ones.
 */
public interface BulkSyncPokemonUseCase {

  /** Starts a background job and returns its initial RUNNING snapshot. */
  SyncJob start(SyncRange range, String requestedBy);

  Optional<SyncJob> find(UUID id);

  Optional<SyncJob> latest();
}
