package org.ballastlane.pokeapp.auth.application;

import io.vertx.core.Future;
import java.util.Optional;
import org.ballastlane.pokeapp.auth.domain.User;

public interface UserRepository {

  Future<Boolean> existsByUsername(String username);

  Future<Optional<User>> findByUsername(String username);

  /** Persists a new user. Fails with {@code UsernameAlreadyTakenException} on duplicates. */
  Future<User> save(User user);
}
