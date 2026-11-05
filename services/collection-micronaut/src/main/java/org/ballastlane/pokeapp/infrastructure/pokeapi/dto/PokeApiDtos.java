package org.ballastlane.pokeapp.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

/** Records mirroring the subset of PokeAPI v2 JSON consumed by the application. */
public final class PokeApiDtos {

  private PokeApiDtos() {}

  @Serdeable
  public record NamedResource(String name, String url) {}

  @Serdeable
  public record PokemonList(int count, List<NamedResource> results) {}

  @Serdeable
  public record Pokemon(
      int id,
      String name,
      int height,
      int weight,
      Sprites sprites,
      List<TypeSlot> types,
      List<AbilitySlot> abilities,
      List<StatEntry> stats,
      NamedResource species) {}

  @Serdeable
  public record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {}

  @Serdeable
  public record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork) {}

  @Serdeable
  public record Artwork(@JsonProperty("front_default") String frontDefault) {}

  @Serdeable
  public record TypeSlot(int slot, NamedResource type) {}

  @Serdeable
  public record AbilitySlot(int slot, NamedResource ability) {}

  @Serdeable
  public record StatEntry(@JsonProperty("base_stat") int baseStat, NamedResource stat) {}

  @Serdeable
  public record Species(
      int id,
      String name,
      List<Genus> genera,
      @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
      @JsonProperty("evolution_chain") NamedResource evolutionChain) {}

  @Serdeable
  public record Genus(String genus, NamedResource language) {}

  @Serdeable
  public record FlavorText(
      @JsonProperty("flavor_text") String flavorText, NamedResource language) {}

  @Serdeable
  public record EvolutionChain(int id, ChainLink chain) {}

  @Serdeable
  public record ChainLink(
      NamedResource species, @JsonProperty("evolves_to") List<ChainLink> evolvesTo) {}
}
