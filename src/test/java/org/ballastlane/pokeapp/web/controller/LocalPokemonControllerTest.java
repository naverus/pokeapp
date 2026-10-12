package org.ballastlane.pokeapp.web.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.UpdateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.SyncPokemonUseCase;
import org.ballastlane.pokeapp.config.SecurityConfig;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.fixtures.PokemonFixtures;
import org.ballastlane.pokeapp.infrastructure.security.AuthenticatedUser;
import org.ballastlane.pokeapp.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(LocalPokemonController.class)
@Import(SecurityConfig.class)
class LocalPokemonControllerTest {

  private static final String VALID_UPDATE =
      """
      {"version": 0, "name": "pikachu", "localName": "Pikachu MX", "region": "Kanto",
       "heightM": 0.4, "weightKg": 6.0, "types": ["electric"], "tags": ["mascot"]}
      """;

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ManageLocalPokemonUseCase manageLocalPokemon;
  @MockitoBean private SyncPokemonUseCase syncPokemon;
  @MockitoBean private JwtTokenService tokenService;

  private static RequestPostProcessor ash() {
    return user("ash").roles("USER");
  }

  private static RequestPostProcessor admin() {
    return user("admin").roles("ADMIN");
  }

  @Test
  void shouldReturn401ProblemDetail_whenNoTokenProvided() throws Exception {
    mockMvc
        .perform(get("/api/v1/local-pokemon"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.instance").value("/api/v1/local-pokemon"));
  }

  @Test
  void shouldAuthenticateWithBearerToken() throws Exception {
    // given
    given(tokenService.verify("good-token"))
        .willReturn(Optional.of(new AuthenticatedUser("ash", Role.USER)));
    given(manageLocalPokemon.list(new PageQuery(0, 20), null))
        .willReturn(new PageResult<>(List.of(PokemonFixtures.storedPikachu()), 0, 20, 1));

    // when / then
    mockMvc
        .perform(get("/api/v1/local-pokemon").header("Authorization", "Bearer good-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("pikachu"))
        .andExpect(jsonPath("$.content[0].tags[0]").value("starter"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void shouldReturn401_whenBearerTokenInvalid() throws Exception {
    mockMvc
        .perform(get("/api/v1/local-pokemon").header("Authorization", "Bearer forged"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldPassNameFilterAndPaging() throws Exception {
    given(manageLocalPokemon.list(new PageQuery(1, 5), "char"))
        .willReturn(new PageResult<>(List.of(), 1, 5, 5));

    mockMvc
        .perform(get("/api/v1/local-pokemon?page=1&size=5&name=char").with(ash()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  void shouldReturn400_whenPageSizeOutOfRange() throws Exception {
    mockMvc
        .perform(get("/api/v1/local-pokemon?size=500").with(ash()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.size").exists());
  }

  @Test
  void shouldReturn400_whenIdIsNotNumeric() throws Exception {
    mockMvc
        .perform(get("/api/v1/local-pokemon/abc").with(ash()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnSortedStoredIds_whenCheckingMembership() throws Exception {
    given(manageLocalPokemon.findStoredPokeApiIds(List.of(1, 25, 4))).willReturn(Set.of(25, 1));

    mockMvc
        .perform(get("/api/v1/local-pokemon/stored-ids?pokeApiIds=1,25,4").with(ash()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pokeApiIds[0]").value(1))
        .andExpect(jsonPath("$.pokeApiIds[1]").value(25));
  }

  @Test
  void shouldRejectStoredIdsLookup_whenAnonymousOrInvalid() throws Exception {
    given(manageLocalPokemon.findStoredPokeApiIds(List.of(0)))
        .willThrow(new DomainValidationException(Map.of("pokeApiIds", "invalid")));

    mockMvc
        .perform(get("/api/v1/local-pokemon/stored-ids?pokeApiIds=1"))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/v1/local-pokemon/stored-ids").with(ash()))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/local-pokemon/stored-ids?pokeApiIds=abc").with(ash()))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/local-pokemon/stored-ids?pokeApiIds=0").with(ash()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.pokeApiIds").value("invalid"));
  }

  @Test
  void shouldReturnPokemon_whenFound() throws Exception {
    given(manageLocalPokemon.get(1L)).willReturn(PokemonFixtures.storedPikachu());

    mockMvc
        .perform(get("/api/v1/local-pokemon/1").with(ash()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pokeApiId").value(25))
        .andExpect(jsonPath("$.region").value("Kanto"))
        .andExpect(jsonPath("$.version").value(0));
  }

  @Test
  void shouldReturn404ProblemDetail_whenMissing() throws Exception {
    given(manageLocalPokemon.get(99L)).willThrow(PokemonNotFoundException.locally(99L));

    mockMvc
        .perform(get("/api/v1/local-pokemon/99").with(ash()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(
            jsonPath("$.detail").value("Pokemon with id 99 was not found in the local store"));
  }

  @Test
  void shouldSyncAndReturn201WithLocation() throws Exception {
    given(syncPokemon.sync("pikachu")).willReturn(PokemonFixtures.storedPikachu());

    mockMvc
        .perform(post("/api/v1/local-pokemon/sync/pikachu").with(ash()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/api/v1/local-pokemon/1")))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void shouldReturn409_whenAlreadySynced() throws Exception {
    given(syncPokemon.sync("pikachu")).willThrow(new PokemonAlreadySyncedException(25));

    mockMvc
        .perform(post("/api/v1/local-pokemon/sync/pikachu").with(ash()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void shouldUpdateAndReturn200() throws Exception {
    given(manageLocalPokemon.update(any())).willReturn(PokemonFixtures.storedPikachu());

    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE))
        .andExpect(status().isOk());

    verify(manageLocalPokemon)
        .update(
            eq(
                new UpdateLocalPokemonCommand(
                    1L,
                    0L,
                    new org.ballastlane.pokeapp.domain.model.PokemonAttributes(
                        "pikachu",
                        "Pikachu MX",
                        "Kanto",
                        null,
                        0.4,
                        6.0,
                        null,
                        null,
                        List.of("electric"),
                        List.of(),
                        List.of("mascot")))));
  }

  @Test
  void shouldReturn400WithFieldErrors_whenPayloadInvalid() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"heightM\": -1, \"weightKg\": 5}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.errors.name").exists())
        .andExpect(jsonPath("$.errors.heightM").exists())
        .andExpect(jsonPath("$.errors.version").exists());
    verify(manageLocalPokemon, never()).update(any());
  }

  @Test
  void shouldReturn400_whenJsonMalformed() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenDomainRulesRejectPayload() throws Exception {
    given(manageLocalPokemon.update(any()))
        .willThrow(
            new DomainValidationException(Map.of("tags", "must contain at most 10 entries")));

    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.tags").value("must contain at most 10 entries"));
  }

  @Test
  void shouldReturn409_whenVersionStale() throws Exception {
    given(manageLocalPokemon.update(any())).willThrow(new StaleVersionException(1L, 0L, 2L));

    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn404_whenUpdatingMissingPokemon() throws Exception {
    given(manageLocalPokemon.update(any())).willThrow(PokemonNotFoundException.locally(1L));

    mockMvc
        .perform(
            put("/api/v1/local-pokemon/1")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldForbidCreate_forRegularUsers() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/local-pokemon")
                .with(ash())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"missingno\", \"heightM\": 1, \"weightKg\": 1}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void shouldCreate_forAdmins() throws Exception {
    given(manageLocalPokemon.create(any())).willReturn(PokemonFixtures.storedPikachu());

    mockMvc
        .perform(
            post("/api/v1/local-pokemon")
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"missingno\", \"heightM\": 1, \"weightKg\": 1}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
  }

  @Test
  void shouldForbidDelete_forRegularUsers() throws Exception {
    mockMvc
        .perform(delete("/api/v1/local-pokemon/1").with(ash()))
        .andExpect(status().isForbidden());
    verify(manageLocalPokemon, never()).delete(1L);
  }

  @Test
  void shouldDeleteAndReturn204_forAdmins() throws Exception {
    mockMvc
        .perform(delete("/api/v1/local-pokemon/1").with(admin()))
        .andExpect(status().isNoContent());
    verify(manageLocalPokemon).delete(1L);
  }

  @Test
  void shouldReturn404_whenDeletingMissingPokemon() throws Exception {
    willThrow(PokemonNotFoundException.locally(7L)).given(manageLocalPokemon).delete(7L);

    mockMvc
        .perform(delete("/api/v1/local-pokemon/7").with(admin()))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn500ProblemWithoutInternals_whenUnexpectedFailure() throws Exception {
    given(manageLocalPokemon.get(1L))
        .willThrow(new IllegalStateException("db password is hunter2"));

    mockMvc
        .perform(get("/api/v1/local-pokemon/1").with(ash()))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
  }
}
