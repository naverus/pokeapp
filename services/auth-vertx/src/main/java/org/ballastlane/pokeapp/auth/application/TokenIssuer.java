package org.ballastlane.pokeapp.auth.application;

import java.time.Instant;
import org.ballastlane.pokeapp.auth.domain.User;

public interface TokenIssuer {

  IssuedToken issue(User user);

  record IssuedToken(String value, Instant expiresAt) {}
}
