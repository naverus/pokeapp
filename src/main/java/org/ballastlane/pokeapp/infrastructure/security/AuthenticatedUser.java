package org.ballastlane.pokeapp.infrastructure.security;

import org.ballastlane.pokeapp.domain.model.Role;

/** Identity extracted from a verified access token. */
public record AuthenticatedUser(String username, Role role) {}
