package org.ballastlane.pokeapp.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Records mirroring the subset of PokeAPI v2 JSON consumed by the application. */
public final class PokeApiDtos {

  private PokeApiDtos() {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record NamedResource(String name, String url) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PokemonList(int count, List<NamedResource> results) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
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

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Artwork(@JsonProperty("front_default") String frontDefault) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TypeSlot(int slot, NamedResource type) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record AbilitySlot(int slot, NamedResource ability) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StatEntry(@JsonProperty("base_stat") int baseStat, NamedResource stat) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Species(
      int id,
      String name,
      List<Genus> genera,
      @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
      @JsonProperty("evolution_chain") NamedResource evolutionChain) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Genus(String genus, NamedResource language) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record FlavorText(
      @JsonProperty("flavor_text") String flavorText, NamedResource language) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EvolutionChain(int id, ChainLink chain) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ChainLink(
      NamedResource species, @JsonProperty("evolves_to") List<ChainLink> evolvesTo) {}
}
