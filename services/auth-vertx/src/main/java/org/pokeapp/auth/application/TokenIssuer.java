package org.pokeapp.auth.application;

import java.time.Instant;
import org.pokeapp.auth.domain.User;

public interface TokenIssuer {

  IssuedToken issue(User user);

  record IssuedToken(String value, Instant expiresAt) {}
}
