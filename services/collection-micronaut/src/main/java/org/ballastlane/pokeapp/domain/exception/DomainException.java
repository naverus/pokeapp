package org.ballastlane.pokeapp.domain.exception;

/** Base type for every business rule violation raised by the core. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
