package org.ballastlane.pokeapp.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.domain.model.User;
import org.ballastlane.pokeapp.support.MySqlTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs adapters against real MySQL with the Flyway schema and seed data applied. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  MySqlTestcontainersConfig.class,
  LocalPokemonPersistenceAdapter.class,
  UserPersistenceAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker = true)
class PersistenceAdaptersTest {

  @Autowired private LocalPokemonPersistenceAdapter pokemonAdapter;
  @Autowired private UserPersistenceAdapter userAdapter;

  private static PokemonAttributes attributes(String name, List<String> tags) {
    return new PokemonAttributes(
        name,
        null,
        "Johto",
        "Test Pokemon",
        1.0,
        10.0,
        null,
        "desc",
        List.of("normal"),
        List.of("run-away"),
        tags);
  }

  @Test
  void shouldExposeSeededPokemonWithCollections() {
    var pikachu = pokemonAdapter.findById(4L).orElseThrow();

    assertThat(pikachu.pokeApiId()).isEqualTo(25);
    assertThat(pikachu.attributes().types()).containsExactly("electric");
    assertThat(pikachu.attributes().abilities()).containsExactly("static", "lightning-rod");
    assertThat(pikachu.attributes().tags()).containsExactly("mascot", "fan-favorite");
    assertThat(pikachu.version()).isZero();
  }

  @Test
  void shouldInsertAndReadBack_whenSavingNewPokemon() {
    // when
    var saved =
        pokemonAdapter.save(LocalPokemon.create(9001, attributes("sentret", List.of("scout"))));

    // then
    assertThat(saved.id()).isNotNull();
    assertThat(saved.createdAt()).isNotNull();
    assertThat(pokemonAdapter.existsByPokeApiId(9001)).isTrue();
    assertThat(pokemonAdapter.findById(saved.id()).orElseThrow().attributes().tags())
        .containsExactly("scout");
  }

  @Test
  void shouldFindByPokeApiId_whenRowExists() {
    var seeded = pokemonAdapter.findByPokeApiId(25).orElseThrow();

    assertThat(seeded.attributes().name()).isEqualTo("pikachu");
    assertThat(pokemonAdapter.findByPokeApiId(999_999)).isEmpty();
  }

  @Test
  void shouldIncrementVersion_whenUpdatingWithCurrentVersion() {
    // given
    var saved = pokemonAdapter.save(LocalPokemon.create(9002, attributes("hoothoot", List.of())));

    // when
    var updated =
        pokemonAdapter.save(saved.withAttributes(attributes("hoothoot", List.of("night", "owl"))));

    // then
    assertThat(updated.version()).isEqualTo(saved.version() + 1);
    assertThat(updated.attributes().tags()).containsExactly("night", "owl");
  }

  @Test
  void shouldRejectStaleVersion_whenSomeoneUpdatedFirst() {
    // given
    var saved = pokemonAdapter.save(LocalPokemon.create(9003, attributes("ledyba", List.of())));
    pokemonAdapter.save(saved.withAttributes(attributes("ledyba", List.of("first"))));

    // when / then
    assertThatThrownBy(
            () ->
                pokemonAdapter.save(saved.withAttributes(attributes("ledyba", List.of("second")))))
        .isInstanceOf(StaleVersionException.class);
  }

  @Test
  void shouldTranslateUniqueViolation_whenPokeApiIdDuplicated() {
    pokemonAdapter.save(LocalPokemon.create(9004, attributes("spinarak", List.of())));

    assertThatThrownBy(
            () -> pokemonAdapter.save(LocalPokemon.create(9004, attributes("spinarak", List.of()))))
        .isInstanceOf(PokemonAlreadySyncedException.class);
  }

  @Test
  void shouldThrowNotFound_whenUpdatingMissingRow() {
    var ghost = new LocalPokemon(123456L, null, attributes("ghost", List.of()), 0L, null, null);

    assertThatThrownBy(() -> pokemonAdapter.save(ghost))
        .isInstanceOf(PokemonNotFoundException.class);
  }

  @Test
  void shouldPaginateAndFilterByName() {
    var firstPage = pokemonAdapter.findAll(new PageQuery(0, 3), null);
    var filtered = pokemonAdapter.findAll(new PageQuery(0, 10), "CHAR");

    assertThat(firstPage.content()).hasSize(3);
    assertThat(firstPage.totalElements()).isGreaterThanOrEqualTo(10);
    assertThat(filtered.content())
        .extracting(pokemon -> pokemon.attributes().name())
        .containsExactly("charmander", "charizard");
  }

  @Test
  void shouldDeleteRowAndReportMissingRows() {
    var saved =
        pokemonAdapter.save(LocalPokemon.create(9005, attributes("chinchou", List.of("x"))));

    assertThat(pokemonAdapter.deleteById(saved.id())).isTrue();
    assertThat(pokemonAdapter.findById(saved.id())).isEmpty();
    assertThat(pokemonAdapter.deleteById(saved.id())).isFalse();
  }

  @Test
  void shouldFindSeededUsersWithWorkingPasswordHashes() {
    var admin = userAdapter.findByUsername("admin").orElseThrow();
    var ash = userAdapter.findByUsername("ash").orElseThrow();
    var encoder = new BCryptPasswordEncoder();

    assertThat(admin.role()).isEqualTo(Role.ADMIN);
    assertThat(encoder.matches("admin123", admin.passwordHash())).isTrue();
    assertThat(encoder.matches("pikachu123", ash.passwordHash())).isTrue();
  }

  @Test
  void shouldSaveUserAndRejectDuplicates() {
    var saved = userAdapter.save(User.newUser("brock", "hash"));

    assertThat(saved.id()).isNotNull();
    assertThat(userAdapter.existsByUsername("brock")).isTrue();
    assertThatThrownBy(() -> userAdapter.save(User.newUser("brock", "hash")))
        .isInstanceOf(UsernameAlreadyTakenException.class);
  }
}
