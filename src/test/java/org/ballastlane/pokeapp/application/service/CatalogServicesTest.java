package org.ballastlane.pokeapp.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogServicesTest {

  @Mock private PokemonCatalogPort catalog;

  @Test
  void shouldReturnCatalogPage_whenBrowsing() {
    // given
    var query = new PageQuery(0, 20);
    var page = new PageResult<>(List.of(PokemonFixtures.pikachuSummary()), 0, 20, 1302);
    given(catalog.findPage(query)).willReturn(page);

    // when
    var result = new BrowsePokemonService(catalog).browse(query);

    // then
    assertThat(result).isEqualTo(page);
  }

  @Test
  void shouldReturnDetail_whenPokemonExists() {
    // given
    given(catalog.findDetail("pikachu")).willReturn(Optional.of(PokemonFixtures.pikachuDetail()));

    // when
    var detail = new GetPokemonDetailService(catalog).getDetail(" Pikachu ");

    // then
    assertThat(detail.id()).isEqualTo(25);
  }

  @Test
  void shouldThrowNotFound_whenPokemonDoesNotExistInCatalog() {
    // given
    given(catalog.findDetail("missingno")).willReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> new GetPokemonDetailService(catalog).getDetail("missingno"))
        .isInstanceOf(PokemonNotFoundException.class)
        .hasMessageContaining("missingno");
  }

  @Test
  void shouldRejectInvalidIdentifier_withoutCallingCatalog() {
    assertThatThrownBy(() -> new GetPokemonDetailService(catalog).getDetail("../etc"))
        .isInstanceOf(DomainValidationException.class);
    verify(catalog, never()).findDetail(any());
  }
}
