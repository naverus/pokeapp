package org.pokeapp.auth.domain;

public class InvalidCredentialsException extends DomainException {

  public InvalidCredentialsException() {
    super("Invalid username or password");
  }
}
