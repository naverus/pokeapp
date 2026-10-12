package org.ballastlane.pokeapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests carrying a valid {@code Authorization: Bearer} token. Invalid or missing
 * tokens leave the request anonymous; authorization rules then decide between public access and
 * 401.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenService tokenService;

  public JwtAuthenticationFilter(JwtTokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      tokenService
          .verify(header.substring(BEARER_PREFIX.length()).strip())
          .ifPresent(this::authenticate);
    }
    chain.doFilter(request, response);
  }

  private void authenticate(AuthenticatedUser user) {
    var authority = new SimpleGrantedAuthority("ROLE_" + user.role().name());
    var authentication =
        new UsernamePasswordAuthenticationToken(user.username(), null, List.of(authority));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
