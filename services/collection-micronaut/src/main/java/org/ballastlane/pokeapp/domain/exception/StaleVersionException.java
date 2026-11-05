package org.ballastlane.pokeapp.domain.exception;

public class StaleVersionException extends DomainException {

  public StaleVersionException(long id, long expectedVersion, long actualVersion) {
    super(
        "Pokemon %d was modified by someone else (expected version %d, current version %d)"
            .formatted(id, expectedVersion, actualVersion));
  }

  public StaleVersionException(long id, Throwable cause) {
    super("Pokemon %d was modified concurrently".formatted(id), cause);
  }
}
