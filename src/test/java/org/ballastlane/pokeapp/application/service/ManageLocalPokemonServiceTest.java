package org.ballastlane.pokeapp.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.CreateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.UpdateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.out.LocalPokemonRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageLocalPokemonServiceTest {

  @Mock private LocalPokemonRepositoryPort repository;

  private ManageLocalPokemonService service;

  @BeforeEach
  void setUp() {
    service = new ManageLocalPokemonService(repository);
  }

  @Test
  void shouldListWithTrimmedFilter() {
    // given
    var query = new PageQuery(0, 10);
    var page = new PageResult<>(List.of(PokemonFixtures.storedPikachu()), 0, 10, 1);
    given(repository.findAll(query, "pika")).willReturn(page);

    // when / then
    assertThat(service.list(query, "  pika ")).isEqualTo(page);
  }

  @Test
  void shouldIgnoreBlankFilter() {
    var query = new PageQuery(0, 10);
    var page = new PageResult<LocalPokemon>(List.of(), 0, 10, 0);
    given(repository.findAll(query, null)).willReturn(page);

    assertThat(service.list(query, "  ")).isEqualTo(page);
  }

  @Test
  void shouldReturnPokemon_whenFound() {
    given(repository.findById(1L)).willReturn(Optional.of(PokemonFixtures.storedPikachu()));

    assertThat(service.get(1L).pokeApiId()).isEqualTo(25);
  }

  @Test
  void shouldThrowNotFound_whenGettingMissingPokemon() {
    given(repository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(99L)).isInstanceOf(PokemonNotFoundException.class);
  }

  @Test
  void shouldCreateManualPokemon_whenNoPokeApiId() {
    // given
    given(repository.save(any())).willReturn(PokemonFixtures.storedPikachu());

    // when
    service.create(new CreateLocalPokemonCommand(null, PokemonFixtures.pikachuAttributes()));

    // then
    verify(repository, never()).existsByPokeApiId(org.mockito.ArgumentMatchers.anyInt());
    verify(repository).save(any());
  }

  @Test
  void shouldRejectCreate_whenPokeApiIdAlreadyStored() {
    given(repository.existsByPokeApiId(25)).willReturn(true);

    assertThatThrownBy(
            () ->
                service.create(
                    new CreateLocalPokemonCommand(25, PokemonFixtures.pikachuAttributes())))
        .isInstanceOf(PokemonAlreadySyncedException.class);
  }

  @Test
  void shouldApplyChanges_whenVersionMatches() {
    // given
    var existing = PokemonFixtures.storedPikachu();
    var changes =
        new PokemonAttributes(
            "pikachu",
            "Pikachu MX",
            "Kanto",
            "Mouse",
            0.4,
            6.0,
            null,
            null,
            List.of("electric"),
            List.of("static"),
            List.of("mascot"));
    given(repository.findById(1L)).willReturn(Optional.of(existing));
    given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

    // when
    var updated = service.update(new UpdateLocalPokemonCommand(1L, 0L, changes));

    // then
    var captor = ArgumentCaptor.forClass(LocalPokemon.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().id()).isEqualTo(1L);
    assertThat(updated.attributes().localName()).isEqualTo("Pikachu MX");
    assertThat(updated.attributes().tags()).containsExactly("mascot");
  }

  @Test
  void shouldThrowNotFound_whenUpdatingMissingPokemon() {
    given(repository.findById(42L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpdateLocalPokemonCommand(42L, 0L, PokemonFixtures.pikachuAttributes())))
        .isInstanceOf(PokemonNotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldRejectUpdate_whenVersionIsStale() {
    given(repository.findById(1L)).willReturn(Optional.of(PokemonFixtures.storedPikachu()));

    assertThatThrownBy(
            () ->
                service.update(
                    new UpdateLocalPokemonCommand(1L, 5L, PokemonFixtures.pikachuAttributes())))
        .isInstanceOf(StaleVersionException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldDelete_whenPokemonExists() {
    given(repository.deleteById(1L)).willReturn(true);

    service.delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  void shouldThrowNotFound_whenDeletingMissingPokemon() {
    given(repository.deleteById(1L)).willReturn(false);

    assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(PokemonNotFoundException.class);
  }
}
