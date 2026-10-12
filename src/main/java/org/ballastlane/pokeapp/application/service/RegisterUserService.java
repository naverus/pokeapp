package org.ballastlane.pokeapp.application.service;

import java.util.Objects;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase;
import org.ballastlane.pokeapp.application.port.out.PasswordHasherPort;
import org.ballastlane.pokeapp.application.port.out.UserRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.ballastlane.pokeapp.domain.model.PasswordPolicy;
import org.ballastlane.pokeapp.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepositoryPort users;
  private final PasswordHasherPort hasher;

  public RegisterUserService(UserRepositoryPort users, PasswordHasherPort hasher) {
    this.users = Objects.requireNonNull(users);
    this.hasher = Objects.requireNonNull(hasher);
  }

  @Override
  public User register(RegisterUserCommand command) {
    PasswordPolicy.validate(command.password());
    var candidate = User.newUser(command.username(), "pending");
    if (users.existsByUsername(candidate.username())) {
      throw new UsernameAlreadyTakenException(candidate.username());
    }
    return users.save(User.newUser(candidate.username(), hasher.hash(command.password())));
  }
}
