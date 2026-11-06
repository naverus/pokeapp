package org.pokeapp.infrastructure.security;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
import java.nio.charset.StandardCharsets;

/** Fails fast at startup when the shared HS256 secret is missing or too short. */
@Context
public class JwtSecretValidator {

  static final int MIN_SECRET_BYTES = 32;

  public JwtSecretValidator(
      @Value("${micronaut.security.token.jwt.signatures.secret.generator.secret:}") String secret) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "POKEAPP_JWT_SECRET must be at least %d bytes".formatted(MIN_SECRET_BYTES));
    }
  }
}
