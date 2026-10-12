package org.ballastlane.pokeapp.infrastructure.pokeapi;

/** Signals a 404 from PokeAPI; thrown (not returned) so that misses are never cached. */
class PokeApiNotFoundException extends RuntimeException {

  PokeApiNotFoundException(String path) {
    super("PokeAPI resource not found: " + path);
  }
}
