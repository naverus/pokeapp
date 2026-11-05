package org.ballastlane.pokeapp.domain.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;

/** Accumulates every rule violation so callers receive all errors in one response. */
final class ValidationErrors {

  private final Map<String, String> errors = new LinkedHashMap<>();

  ValidationErrors check(boolean valid, String field, String message) {
    if (!valid) {
      errors.putIfAbsent(field, message);
    }
    return this;
  }

  void throwIfAny() {
    if (!errors.isEmpty()) {
      throw new DomainValidationException(errors);
    }
  }

  static void require(boolean valid, String field, String message) {
    new ValidationErrors().check(valid, field, message).throwIfAny();
  }
}
