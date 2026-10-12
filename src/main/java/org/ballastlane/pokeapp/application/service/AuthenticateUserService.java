package org.ballastlane.pokeapp.application.service;

import java.util.Locale;
import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase;
import org.ballastlane.pokeapp.application.port.out.PasswordHasherPort;
import org.ballastlane.pokeapp.application.port.out.TokenIssuerPort;
import org.ballastlane.pokeapp.application.port.out.UserRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.InvalidCredentialsException;

public class AuthenticateUserService implements AuthenticateUserUseCase {

  private final UserRepositoryPort users;
  private final PasswordHasherPort hasher;
  private final TokenIssuerPort tokens;

  public AuthenticateUserService(
      UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
    this.users = Objects.requireNonNull(users);
    this.hasher = Objects.requireNonNull(hasher);
    this.tokens = Objects.requireNonNull(tokens);
  }

  @Override
  public AuthResult authenticate(LoginCommand command) {
    if (command.username() == null || command.password() == null) {
      throw new InvalidCredentialsException();
    }
    var username = command.username().strip().toLowerCase(Locale.ROOT);
    var user =
        users
            .findByUsername(username)
            .filter(candidate -> hasher.matches(command.password(), candidate.passwordHash()))
            .orElseThrow(InvalidCredentialsException::new);
    var token = tokens.issue(user);
    return new AuthResult(token.value(), token.expiresAt(), user.username(), user.role());
  }
}
