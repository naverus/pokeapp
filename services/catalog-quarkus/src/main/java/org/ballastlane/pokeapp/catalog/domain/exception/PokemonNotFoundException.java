package org.ballastlane.pokeapp.catalog.domain.exception;

public class PokemonNotFoundException extends DomainException {

  private PokemonNotFoundException(String message) {
    super(message);
  }

  public static PokemonNotFoundException inCatalog(String idOrName) {
    return new PokemonNotFoundException(
        "Pokemon '%s' was not found in PokeAPI".formatted(idOrName));
  }
}
