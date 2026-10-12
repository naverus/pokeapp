package org.ballastlane.pokeapp.application.port.out;

import java.time.Instant;
import org.ballastlane.pokeapp.domain.model.User;

public interface TokenIssuerPort {

  IssuedToken issue(User user);

  record IssuedToken(String value, Instant expiresAt) {}
}
