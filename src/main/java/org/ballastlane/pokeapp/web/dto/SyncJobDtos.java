package org.ballastlane.pokeapp.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.ballastlane.pokeapp.domain.model.SyncJobStatus;

public final class SyncJobDtos {

  private SyncJobDtos() {}

  /** Inclusive PokeAPI id range; bounds are validated by the domain. */
  public record StartSyncJobRequest(@NotNull Integer fromId, @NotNull Integer toId) {}

  public record SyncJobResponse(
      UUID id,
      SyncJobStatus status,
      int fromId,
      int toId,
      int total,
      int processed,
      int inserted,
      int updated,
      int failed,
      List<String> errors,
      String requestedBy,
      Instant startedAt,
      Instant finishedAt) {}
}
