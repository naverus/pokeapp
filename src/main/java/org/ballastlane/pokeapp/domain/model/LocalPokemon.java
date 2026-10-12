package org.ballastlane.pokeapp.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** A Pokemon replicated into (or created in) the local relational store. */
public record LocalPokemon(
    Long id,
    Integer pokeApiId,
    PokemonAttributes attributes,
    long version,
    Instant createdAt,
    Instant updatedAt) {

  public LocalPokemon {
    Objects.requireNonNull(attributes, "attributes");
  }

  public static LocalPokemon create(Integer pokeApiId, PokemonAttributes attributes) {
    return new LocalPokemon(null, pokeApiId, attributes, 0L, null, null);
  }

  public static LocalPokemon fromCatalog(PokemonDetail detail) {
    var attributes =
        new PokemonAttributes(
            detail.name(),
            null,
            null,
            detail.category(),
            detail.heightM(),
            detail.weightKg(),
            detail.imageUrl(),
            detail.description(),
            detail.types(),
            detail.abilities(),
            List.of());
    return create(detail.id(), attributes);
  }

  /** Replaces the PokeAPI-owned fields and keeps the proprietary ones (localName, region, tags). */
  public LocalPokemon refreshFromCatalog(PokemonDetail detail) {
    var refreshed =
        new PokemonAttributes(
            detail.name(),
            attributes.localName(),
            attributes.region(),
            detail.category(),
            detail.heightM(),
            detail.weightKg(),
            detail.imageUrl(),
            detail.description(),
            detail.types(),
            detail.abilities(),
            attributes.tags());
    return withAttributes(refreshed);
  }

  public LocalPokemon withAttributes(PokemonAttributes changes) {
    return new LocalPokemon(id, pokeApiId, changes, version, createdAt, updatedAt);
  }
}
