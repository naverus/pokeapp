package org.pokeapp.auth.application;

import io.vertx.core.Future;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import org.pokeapp.auth.domain.InvalidCredentialsException;
import org.pokeapp.auth.domain.PasswordPolicy;
import org.pokeapp.auth.domain.Role;
import org.pokeapp.auth.domain.User;
import org.pokeapp.auth.domain.UsernameAlreadyTakenException;

/** Register and login use cases. */
public class AuthService {

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final TokenIssuer tokens;

  public AuthService(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
    this.users = Objects.requireNonNull(users);
    this.hasher = Objects.requireNonNull(hasher);
    this.tokens = Objects.requireNonNull(tokens);
  }

  public record AuthResult(String accessToken, Instant expiresAt, String username, Role role) {}

  public Future<User> register(String username, String password) {
    User candidate;
    try {
      PasswordPolicy.validate(password);
      candidate = User.newUser(username, "pending");
    } catch (RuntimeException e) {
      return Future.failedFuture(e);
    }
    return users
        .existsByUsername(candidate.username())
        .compose(
            exists ->
                exists
                    ? Future.failedFuture(new UsernameAlreadyTakenException(candidate.username()))
                    : hasher.hash(password))
        .compose(hash -> users.save(User.newUser(candidate.username(), hash)));
  }

  public Future<AuthResult> login(String username, String password) {
    if (username == null || password == null) {
      return Future.failedFuture(new InvalidCredentialsException());
    }
    var normalized = username.strip().toLowerCase(Locale.ROOT);
    return users
        .findByUsername(normalized)
        .compose(
            found -> {
              if (found.isEmpty()) {
                return Future.failedFuture(new InvalidCredentialsException());
              }
              var user = found.get();
              return hasher
                  .matches(password, user.passwordHash())
                  .compose(
                      ok -> {
                        if (!ok) {
                          return Future.failedFuture(new InvalidCredentialsException());
                        }
                        var token = tokens.issue(user);
                        return Future.succeededFuture(
                            new AuthResult(
                                token.value(), token.expiresAt(), user.username(), user.role()));
                      });
            });
  }
}
