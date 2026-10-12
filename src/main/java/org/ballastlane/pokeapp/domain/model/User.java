package org.ballastlane.pokeapp.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record User(Long id, String username, String passwordHash, Role role, Instant createdAt) {

  private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_.-]{3,30}$");

  public User {
    username = username == null ? null : username.strip().toLowerCase(Locale.ROOT);
    ValidationErrors.require(
        username != null && USERNAME.matcher(username).matches(),
        "username",
        "must be 3-30 characters: letters, digits, '.', '_' or '-'");
    Objects.requireNonNull(passwordHash, "passwordHash");
    Objects.requireNonNull(role, "role");
  }

  public static User newUser(String username, String passwordHash) {
    return new User(null, username, passwordHash, Role.USER, null);
  }
}
