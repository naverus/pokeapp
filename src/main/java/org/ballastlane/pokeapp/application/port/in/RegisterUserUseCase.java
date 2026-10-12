package org.ballastlane.pokeapp.application.port.in;

import org.ballastlane.pokeapp.domain.model.User;

public interface RegisterUserUseCase {

  User register(RegisterUserCommand command);

  record RegisterUserCommand(String username, String password) {}
}
