package org.ballastlane.pokeapp.application.port.in;

import java.time.Instant;
import org.ballastlane.pokeapp.domain.model.Role;

public interface AuthenticateUserUseCase {

  AuthResult authenticate(LoginCommand command);

  record LoginCommand(String username, String password) {}

  record AuthResult(String accessToken, Instant expiresAt, String username, Role role) {}
}
