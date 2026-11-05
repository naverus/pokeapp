package org.ballastlane.pokeapp.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncPokemonServiceTest {

  @Mock private PokemonCatalogPort catalog;
  @Mock private LocalPokemonRepositoryPort repository;

  private SyncPokemonService service;

  @BeforeEach
  void setUp() {
    service = new SyncPokemonService(catalog, repository);
  }

  @Test
  void shouldPersistCatalogPokemon_whenNotStoredYet() {
    // given
    given(catalog.findDetail("pikachu")).willReturn(Optional.of(PokemonFixtures.pikachuDetail()));
    given(repository.existsByPokeApiId(25)).willReturn(false);
    given(repository.save(any())).willReturn(PokemonFixtures.storedPikachu());

    // when
    var stored = service.sync("Pikachu");

    // then
    var captor = ArgumentCaptor.forClass(LocalPokemon.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().pokeApiId()).isEqualTo(25);
    assertThat(captor.getValue().id()).isNull();
    assertThat(stored.id()).isEqualTo(1L);
  }

  @Test
  void shouldRejectDuplicate_whenPokemonAlreadySynced() {
    // given
    given(catalog.findDetail("pikachu")).willReturn(Optional.of(PokemonFixtures.pikachuDetail()));
    given(repository.existsByPokeApiId(25)).willReturn(true);

    // when / then
    assertThatThrownBy(() -> service.sync("pikachu"))
        .isInstanceOf(PokemonAlreadySyncedException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldThrowNotFound_whenCatalogHasNoSuchPokemon() {
    // given
    given(catalog.findDetail("agumon")).willReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> service.sync("agumon")).isInstanceOf(PokemonNotFoundException.class);
  }
}
