package org.ballastlane.pokeapp.domain.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class Text {

  private Text() {}

  static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    var trimmed = value.strip();
    return trimmed.isEmpty() ? null : trimmed;
  }

  static boolean fits(String value, int maxLength) {
    return value == null || value.length() <= maxLength;
  }

  static List<String> normalizedDistinct(List<String> values) {
    if (values == null) {
      return List.of();
    }
    return values.stream()
        .map(Text::trimToNull)
        .filter(Objects::nonNull)
        .map(value -> value.toLowerCase(Locale.ROOT))
        .distinct()
        .toList();
  }

  static List<String> immutable(List<String> values) {
    return values == null ? List.of() : List.copyOf(values);
  }
}
