package org.pokeapp.auth.infrastructure;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.pokeapp.auth.application.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

/** BCrypt ($2a$, strength 10) - compatible with Spring's {@code BCryptPasswordEncoder}. */
public class BCryptPasswordHasher implements PasswordHasher {

  private static final int STRENGTH = 10;

  private final Vertx vertx;

  public BCryptPasswordHasher(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<String> hash(String rawPassword) {
    return vertx.executeBlocking(() -> BCrypt.hashpw(rawPassword, BCrypt.gensalt(STRENGTH)), false);
  }

  @Override
  public Future<Boolean> matches(String rawPassword, String passwordHash) {
    return vertx.executeBlocking(
        () -> {
          try {
            return BCrypt.checkpw(rawPassword, passwordHash);
          } catch (IllegalArgumentException invalidHash) {
            return false;
          }
        },
        false);
  }
}
