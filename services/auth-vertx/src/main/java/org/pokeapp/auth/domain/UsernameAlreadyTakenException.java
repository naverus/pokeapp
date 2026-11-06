package org.pokeapp.auth.domain;

public class UsernameAlreadyTakenException extends DomainException {

  public UsernameAlreadyTakenException(String username) {
    super("Username '%s' is already taken".formatted(username));
  }
}
