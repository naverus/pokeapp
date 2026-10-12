package org.ballastlane.pokeapp.domain.exception;

import java.util.UUID;

public class SyncAlreadyRunningException extends DomainException {

  public SyncAlreadyRunningException(UUID runningJobId) {
    super("A Pokemon synchronization is already running (job %s)".formatted(runningJobId));
  }
}
