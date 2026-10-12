package org.ballastlane.pokeapp.domain.exception;

import java.util.Map;

public class DomainValidationException extends DomainException {

  private final Map<String, String> errors;

  public DomainValidationException(Map<String, String> errors) {
    super("Validation failed: " + errors);
    this.errors = Map.copyOf(errors);
  }

  public Map<String, String> errors() {
    return errors;
  }
}
