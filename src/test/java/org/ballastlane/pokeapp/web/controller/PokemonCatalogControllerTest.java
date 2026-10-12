package org.ballastlane.pokeapp.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ballastlane.pokeapp.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.config.SecurityConfig;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.ballastlane.pokeapp.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PokemonCatalogController.class)
@Import(SecurityConfig.class)
class PokemonCatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private BrowsePokemonUseCase browsePokemon;
  @MockitoBean private GetPokemonDetailUseCase getPokemonDetail;
  @MockitoBean private JwtTokenService tokenService;

  @Test
  void shouldBrowseAnonymously_withDefaultPaging() throws Exception {
    given(browsePokemon.browse(new PageQuery(0, 20)))
        .willReturn(new PageResult<>(List.of(PokemonFixtures.pikachuSummary()), 0, 20, 1302));

    mockMvc
        .perform(get("/api/v1/pokemon"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "max-age=300"))
        .andExpect(jsonPath("$.content[0].name").value("pikachu"))
        .andExpect(jsonPath("$.content[0].category").value("Mouse Pokemon"))
        .andExpect(jsonPath("$.content[0].weightKg").value(6.0))
        .andExpect(jsonPath("$.content[0].abilities[0]").value("static"))
        .andExpect(jsonPath("$.totalPages").value(66));
  }

  @Test
  void shouldReturn400_whenPageNegative() throws Exception {
    mockMvc
        .perform(get("/api/v1/pokemon?page=-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.page").exists());
  }

  @Test
  void shouldReturnDetail_withStatsAndEvolutionChain() throws Exception {
    given(getPokemonDetail.getDetail("pikachu")).willReturn(PokemonFixtures.pikachuDetail());

    mockMvc
        .perform(get("/api/v1/pokemon/pikachu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats[0].name").value("hp"))
        .andExpect(jsonPath("$.totalBaseStats").value(90))
        .andExpect(jsonPath("$.evolutionChain[2].name").value("raichu"))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturn404_whenCatalogHasNoSuchPokemon() throws Exception {
    given(getPokemonDetail.getDetail("agumon"))
        .willThrow(PokemonNotFoundException.inCatalog("agumon"));

    mockMvc.perform(get("/api/v1/pokemon/agumon")).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn503_whenPokeApiDown() throws Exception {
    given(browsePokemon.browse(new PageQuery(0, 20)))
        .willThrow(new CatalogUnavailableException("PokeAPI is currently unavailable", null));

    mockMvc
        .perform(get("/api/v1/pokemon"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.title").value("PokeAPI unavailable"));
  }
}
