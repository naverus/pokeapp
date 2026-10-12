package org.ballastlane.pokeapp.domain.model;

import java.util.List;

/**
 * Editable data of a locally stored Pokemon, including the proprietary fields (localName, region,
 * tags) that do not exist in PokeAPI.
 */
public record PokemonAttributes(
    String name,
    String localName,
    String region,
    String category,
    double heightM,
    double weightKg,
    String spriteUrl,
    String description,
    List<String> types,
    List<String> abilities,
    List<String> tags) {

  public static final int MAX_NAME = 100;
  public static final int MAX_REGION = 50;
  public static final int MAX_CATEGORY = 100;
  public static final int MAX_SPRITE_URL = 500;
  public static final int MAX_DESCRIPTION = 1000;
  public static final int MAX_TAGS = 10;
  public static final int MAX_TAG_LENGTH = 30;
  public static final int MAX_TYPES = 5;
  public static final int MAX_ABILITIES = 10;

  public PokemonAttributes {
    name = Text.trimToNull(name);
    localName = Text.trimToNull(localName);
    region = Text.trimToNull(region);
    category = Text.trimToNull(category);
    spriteUrl = Text.trimToNull(spriteUrl);
    description = Text.trimToNull(description);
    types = Text.normalizedDistinct(types);
    abilities = Text.normalizedDistinct(abilities);
    tags = Text.normalizedDistinct(tags);

    new ValidationErrors()
        .check(name != null, "name", "must not be blank")
        .check(
            Text.fits(name, MAX_NAME), "name", "must be at most %d characters".formatted(MAX_NAME))
        .check(
            Text.fits(localName, MAX_NAME),
            "localName",
            "must be at most %d characters".formatted(MAX_NAME))
        .check(
            Text.fits(region, MAX_REGION),
            "region",
            "must be at most %d characters".formatted(MAX_REGION))
        .check(
            Text.fits(category, MAX_CATEGORY),
            "category",
            "must be at most %d characters".formatted(MAX_CATEGORY))
        .check(heightM >= 0, "heightM", "must be zero or positive")
        .check(weightKg >= 0, "weightKg", "must be zero or positive")
        .check(
            Text.fits(spriteUrl, MAX_SPRITE_URL),
            "spriteUrl",
            "must be at most %d characters".formatted(MAX_SPRITE_URL))
        .check(
            Text.fits(description, MAX_DESCRIPTION),
            "description",
            "must be at most %d characters".formatted(MAX_DESCRIPTION))
        .check(
            types.size() <= MAX_TYPES,
            "types",
            "must contain at most %d entries".formatted(MAX_TYPES))
        .check(
            abilities.size() <= MAX_ABILITIES,
            "abilities",
            "must contain at most %d entries".formatted(MAX_ABILITIES))
        .check(
            tags.size() <= MAX_TAGS, "tags", "must contain at most %d entries".formatted(MAX_TAGS))
        .check(
            tags.stream().allMatch(tag -> tag.length() <= MAX_TAG_LENGTH),
            "tags",
            "each tag must be at most %d characters".formatted(MAX_TAG_LENGTH))
        .throwIfAny();
  }
}
