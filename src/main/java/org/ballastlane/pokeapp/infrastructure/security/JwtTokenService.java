package org.ballastlane.pokeapp.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.ballastlane.pokeapp.application.port.out.TokenIssuerPort;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Issues and verifies HS256-signed JWT access tokens. */
@Component
public class JwtTokenService implements TokenIssuerPort {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
  private static final String ROLE_CLAIM = "role";
  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey key;
  private final SecurityProperties properties;
  private final Clock clock;

  public JwtTokenService(SecurityProperties properties, Clock clock) {
    var secret =
        properties.jwtSecret() == null
            ? new byte[0]
            : properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "pokeapp.security.jwt-secret must be at least %d bytes".formatted(MIN_SECRET_BYTES));
    }
    this.key = Keys.hmacShaKeyFor(secret);
    this.properties = properties;
    this.clock = clock;
  }

  @Override
  public IssuedToken issue(User user) {
    var issuedAt = clock.instant();
    var expiresAt = issuedAt.plus(properties.tokenTtl());
    var token =
        Jwts.builder()
            .issuer(properties.issuer())
            .subject(user.username())
            .claim(ROLE_CLAIM, user.role().name())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    return new IssuedToken(token, expiresAt);
  }

  public Optional<AuthenticatedUser> verify(String token) {
    try {
      var claims =
          Jwts.parser()
              .verifyWith(key)
              .requireIssuer(properties.issuer())
              .clock(() -> Date.from(clock.instant()))
              .build()
              .parseSignedClaims(token)
              .getPayload();
      var role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));
      return Optional.of(new AuthenticatedUser(claims.getSubject(), role));
    } catch (JwtException | IllegalArgumentException ex) {
      log.debug("Rejected access token: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}
