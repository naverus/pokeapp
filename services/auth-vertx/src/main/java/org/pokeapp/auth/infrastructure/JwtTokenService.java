package org.pokeapp.auth.infrastructure;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.pokeapp.auth.application.TokenIssuer;
import org.pokeapp.auth.domain.User;

/** Issues HS256-signed JWT access tokens (issuer, subject=username, claim "role"). */
public class JwtTokenService implements TokenIssuer {

  private static final String ROLE_CLAIM = "role";
  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey key;
  private final String issuer;
  private final Duration ttl;
  private final Clock clock;

  public JwtTokenService(String secret, String issuer, Duration ttl, Clock clock) {
    var bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "POKEAPP_JWT_SECRET must be at least %d bytes".formatted(MIN_SECRET_BYTES));
    }
    this.key = Keys.hmacShaKeyFor(bytes);
    this.issuer = issuer;
    this.ttl = ttl;
    this.clock = clock;
  }

  @Override
  public IssuedToken issue(User user) {
    var issuedAt = clock.instant();
    var expiresAt = issuedAt.plus(ttl);
    var token =
        Jwts.builder()
            .issuer(issuer)
            .subject(user.username())
            .claim(ROLE_CLAIM, user.role().name())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    return new IssuedToken(token, expiresAt);
  }
}
