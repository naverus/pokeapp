package org.ballastlane.pokeapp.web.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.ballastlane.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.ballastlane.pokeapp.config.SecurityConfig;
import org.ballastlane.pokeapp.domain.exception.SyncAlreadyRunningException;
import org.ballastlane.pokeapp.domain.model.SyncJob;
import org.ballastlane.pokeapp.domain.model.SyncRange;
import org.ballastlane.pokeapp.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(SyncJobController.class)
@Import(SecurityConfig.class)
class SyncJobControllerTest {

  private static final String URL = "/api/v1/admin/sync-jobs";
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private BulkSyncPokemonUseCase bulkSync;
  @MockitoBean private JwtTokenService tokenService;

  private static RequestPostProcessor admin() {
    return user("admin").roles("ADMIN");
  }

  @Test
  void shouldReturn401_whenAnonymous() throws Exception {
    mockMvc.perform(post(URL)).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn403_whenUserIsNotAdmin() throws Exception {
    mockMvc.perform(post(URL).with(user("ash").roles("USER"))).andExpect(status().isForbidden());
    mockMvc
        .perform(get(URL + "/latest").with(user("ash").roles("USER")))
        .andExpect(status().isForbidden());
    verify(bulkSync, never()).start(any(), anyString());
  }

  @Test
  void shouldStartJobWithRequestedRange_andReturn202WithLocation() throws Exception {
    // given
    var job = SyncJob.start(new SyncRange(1, 20), "admin", NOW);
    given(bulkSync.start(new SyncRange(1, 20), "admin")).willReturn(job);

    // when / then
    mockMvc
        .perform(
            post(URL)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromId\": 1, \"toId\": 20}"))
        .andExpect(status().isAccepted())
        .andExpect(header().string("Location", endsWith(URL + "/" + job.id())))
        .andExpect(jsonPath("$.status").value("RUNNING"))
        .andExpect(jsonPath("$.total").value(20))
        .andExpect(jsonPath("$.processed").value(0))
        .andExpect(jsonPath("$.requestedBy").value("admin"));
  }

  @Test
  void shouldUseDefaultRange_whenBodyOmitted() throws Exception {
    given(bulkSync.start(SyncRange.DEFAULT, "admin"))
        .willReturn(SyncJob.start(SyncRange.DEFAULT, "admin", NOW));

    mockMvc
        .perform(post(URL).with(admin()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.fromId").value(1))
        .andExpect(jsonPath("$.toId").value(151));
  }

  @Test
  void shouldReturn400_whenRangeInvalid() throws Exception {
    mockMvc
        .perform(
            post(URL)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromId\": 50, \"toId\": 10}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.toId").exists());

    mockMvc
        .perform(
            post(URL)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toId\": 3}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.fromId").exists());
  }

  @Test
  void shouldReturn409_whenJobAlreadyRunning() throws Exception {
    given(bulkSync.start(SyncRange.DEFAULT, "admin"))
        .willThrow(new SyncAlreadyRunningException(UUID.randomUUID()));

    mockMvc
        .perform(post(URL).with(admin()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Conflict"));
  }

  @Test
  void shouldReturnJobProgress_whenFound() throws Exception {
    var job = SyncJob.start(new SyncRange(1, 3), "admin", NOW).recordInserted().recordUpdated();
    given(bulkSync.find(job.id())).willReturn(Optional.of(job));

    mockMvc
        .perform(get(URL + "/" + job.id()).with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.processed").value(2))
        .andExpect(jsonPath("$.inserted").value(1))
        .andExpect(jsonPath("$.updated").value(1));
  }

  @Test
  void shouldReturn404_whenJobUnknown() throws Exception {
    var id = UUID.randomUUID();
    given(bulkSync.find(id)).willReturn(Optional.empty());

    mockMvc
        .perform(get(URL + "/" + id).with(admin()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void shouldReturnLatestJobOr204() throws Exception {
    given(bulkSync.latest()).willReturn(Optional.empty());
    mockMvc.perform(get(URL + "/latest").with(admin())).andExpect(status().isNoContent());

    var job = SyncJob.start(SyncRange.DEFAULT, "admin", NOW).complete(NOW);
    given(bulkSync.latest()).willReturn(Optional.of(job));
    mockMvc
        .perform(get(URL + "/latest").with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.id").value(job.id().toString()));
  }
}
