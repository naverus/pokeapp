package org.ballastlane.pokeapp.domain.exception;

public class PokemonNotFoundException extends DomainException {

  private PokemonNotFoundException(String message) {
    super(message);
  }

  public static PokemonNotFoundException inCatalog(String idOrName) {
    return new PokemonNotFoundException(
        "Pokemon '%s' was not found in PokeAPI".formatted(idOrName));
  }

  public static PokemonNotFoundException locally(long id) {
    return new PokemonNotFoundException(
        "Pokemon with id %d was not found in the local store".formatted(id));
  }
}
