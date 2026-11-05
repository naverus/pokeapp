package org.ballastlane.pokeapp.domain.exception;

public class SyncAlreadyRunningException extends DomainException {

  public SyncAlreadyRunningException() {
    super("A restock is already in progress. Hang tight until it finishes!");
  }
}
