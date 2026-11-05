package org.ballastlane.pokeapp.web.mapper;

import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncRange;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.StartSyncJobRequest;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.SyncJobResponse;

public final class SyncJobWebMapper {

  private SyncJobWebMapper() {}

  public static SyncRange toRange(StartSyncJobRequest request) {
    return request == null ? SyncRange.DEFAULT : new SyncRange(request.fromId(), request.toId());
  }

  public static SyncJobResponse toResponse(SyncJob job) {
    return new SyncJobResponse(
        job.id(),
        job.status(),
        job.range().fromId(),
        job.range().toId(),
        job.total(),
        job.processed(),
        job.inserted(),
        job.updated(),
        job.failed(),
        job.errors(),
        job.requestedBy(),
        job.startedAt(),
        job.finishedAt());
  }
}
