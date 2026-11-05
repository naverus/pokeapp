package org.ballastlane.pokeapp.auth.application;

import io.vertx.core.Future;

public interface PasswordHasher {

  Future<String> hash(String rawPassword);

  Future<Boolean> matches(String rawPassword, String passwordHash);
}
