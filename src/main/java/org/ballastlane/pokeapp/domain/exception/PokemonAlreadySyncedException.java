package org.ballastlane.pokeapp.domain.exception;

public class PokemonAlreadySyncedException extends DomainException {

  public PokemonAlreadySyncedException(int pokeApiId) {
    super("Pokemon with PokeAPI id %d is already stored locally".formatted(pokeApiId));
  }
}
