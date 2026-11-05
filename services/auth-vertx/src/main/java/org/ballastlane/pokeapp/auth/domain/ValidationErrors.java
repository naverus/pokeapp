package org.ballastlane.pokeapp.auth.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/** Accumulates every rule violation so callers receive all errors in one response. */
public final class ValidationErrors {

  private final Map<String, String> errors = new LinkedHashMap<>();

  public ValidationErrors check(boolean valid, String field, String message) {
    if (!valid) {
      errors.putIfAbsent(field, message);
    }
    return this;
  }

  public void throwIfAny() {
    if (!errors.isEmpty()) {
      throw new DomainValidationException(errors);
    }
  }

  public static void require(boolean valid, String field, String message) {
    new ValidationErrors().check(valid, field, message).throwIfAny();
  }
}
