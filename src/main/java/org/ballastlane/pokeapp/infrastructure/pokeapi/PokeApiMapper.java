package org.ballastlane.pokeapp.infrastructure.pokeapi;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.ballastlane.pokeapp.domain.model.EvolutionStage;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;
import org.ballastlane.pokeapp.domain.model.Stat;
import org.ballastlane.pokeapp.infrastructure.pokeapi.dto.PokeApiDtos;

final class PokeApiMapper {

  static final String SPRITE_BASE_URL =
      "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
  private static final String ENGLISH = "en";

  private PokeApiMapper() {}

  static PokemonSummary toSummary(PokeApiDtos.Pokemon pokemon, PokeApiDtos.Species species) {
    return new PokemonSummary(
        pokemon.id(),
        pokemon.name(),
        spriteOf(pokemon),
        genusOf(species),
        decimetricToUnit(pokemon.weight()),
        abilitiesOf(pokemon),
        typesOf(pokemon));
  }

  static PokemonDetail toDetail(
      PokeApiDtos.Pokemon pokemon, PokeApiDtos.Species species, PokeApiDtos.EvolutionChain chain) {
    return new PokemonDetail(
        pokemon.id(),
        pokemon.name(),
        artworkOf(pokemon),
        genusOf(species),
        decimetricToUnit(pokemon.height()),
        decimetricToUnit(pokemon.weight()),
        typesOf(pokemon),
        abilitiesOf(pokemon),
        statsOf(pokemon),
        descriptionOf(species),
        evolutionStagesOf(chain));
  }

  static int idFromUrl(String url) {
    var trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
  }

  /** PokeAPI reports height in decimetres and weight in hectograms. */
  private static double decimetricToUnit(int value) {
    return value / 10.0;
  }

  private static String spriteOf(PokeApiDtos.Pokemon pokemon) {
    return Optional.ofNullable(pokemon.sprites())
        .map(PokeApiDtos.Sprites::frontDefault)
        .orElseGet(() -> artworkOf(pokemon));
  }

  private static String artworkOf(PokeApiDtos.Pokemon pokemon) {
    var sprites = pokemon.sprites();
    if (sprites == null) {
      return null;
    }
    return Optional.ofNullable(sprites.other())
        .map(PokeApiDtos.OtherSprites::officialArtwork)
        .map(PokeApiDtos.Artwork::frontDefault)
        .orElse(sprites.frontDefault());
  }

  private static List<String> typesOf(PokeApiDtos.Pokemon pokemon) {
    return nullSafe(pokemon.types()).stream()
        .sorted(Comparator.comparingInt(PokeApiDtos.TypeSlot::slot))
        .map(slot -> slot.type().name())
        .toList();
  }

  private static List<String> abilitiesOf(PokeApiDtos.Pokemon pokemon) {
    return nullSafe(pokemon.abilities()).stream()
        .sorted(Comparator.comparingInt(PokeApiDtos.AbilitySlot::slot))
        .map(slot -> slot.ability().name())
        .toList();
  }

  private static List<Stat> statsOf(PokeApiDtos.Pokemon pokemon) {
    return nullSafe(pokemon.stats()).stream()
        .map(entry -> new Stat(entry.stat().name(), entry.baseStat()))
        .toList();
  }

  private static String genusOf(PokeApiDtos.Species species) {
    return nullSafe(species.genera()).stream()
        .filter(genus -> isEnglish(genus.language()))
        .map(PokeApiDtos.Genus::genus)
        .findFirst()
        .orElse(null);
  }

  private static String descriptionOf(PokeApiDtos.Species species) {
    return nullSafe(species.flavorTextEntries()).stream()
        .filter(entry -> isEnglish(entry.language()))
        .map(entry -> entry.flavorText().replaceAll("[\\f\\n\\r\\u00ad\\s]+", " ").strip())
        .findFirst()
        .orElse(null);
  }

  private static List<EvolutionStage> evolutionStagesOf(PokeApiDtos.EvolutionChain chain) {
    if (chain == null || chain.chain() == null) {
      return List.of();
    }
    record Node(PokeApiDtos.ChainLink link, int stage) {}
    var stages = new ArrayList<EvolutionStage>();
    var queue = new ArrayDeque<Node>();
    queue.add(new Node(chain.chain(), 1));
    while (!queue.isEmpty()) {
      var node = queue.poll();
      var speciesId = idFromUrl(node.link().species().url());
      stages.add(
          new EvolutionStage(
              speciesId,
              node.link().species().name(),
              node.stage(),
              SPRITE_BASE_URL + speciesId + ".png"));
      nullSafe(node.link().evolvesTo())
          .forEach(next -> queue.add(new Node(next, node.stage() + 1)));
    }
    return stages;
  }

  private static boolean isEnglish(PokeApiDtos.NamedResource language) {
    return language != null && ENGLISH.equals(language.name());
  }

  private static <T> List<T> nullSafe(List<T> values) {
    return values == null ? List.of() : values;
  }
}
