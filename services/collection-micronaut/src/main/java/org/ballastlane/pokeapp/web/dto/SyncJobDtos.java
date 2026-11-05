package org.ballastlane.pokeapp.web.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.ballastlane.pokeapp.domain.model.SyncJobStatus;

public final class SyncJobDtos {

  private SyncJobDtos() {}

  /** Inclusive PokeAPI id range; bounds are validated by the domain. */
  @Serdeable
  @Introspected
  public record StartSyncJobRequest(@NotNull Integer fromId, @NotNull Integer toId) {}

  @Serdeable
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
