package org.ballastlane.pokeapp.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.ballastlane.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.StartSyncJobRequest;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.SyncJobResponse;
import org.ballastlane.pokeapp.web.mapper.SyncJobWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/admin/sync-jobs")
@Tag(name = "Pokemon synchronization", description = "On-demand bulk synchronization (ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class SyncJobController {

  private final BulkSyncPokemonUseCase bulkSync;

  public SyncJobController(BulkSyncPokemonUseCase bulkSync) {
    this.bulkSync = bulkSync;
  }

  @PostMapping
  @Operation(
      summary =
          "US03 - Start a background sync of a PokeAPI id range (default 1-151): inserts missing"
              + " Pokemon and refreshes catalog fields of stored ones")
  public ResponseEntity<SyncJobResponse> start(
      @Valid @RequestBody(required = false) StartSyncJobRequest request, Principal principal) {
    var job = bulkSync.start(SyncJobWebMapper.toRange(request), principal.getName());
    var location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/admin/sync-jobs/{id}")
            .buildAndExpand(job.id())
            .toUri();
    return ResponseEntity.accepted().location(location).body(SyncJobWebMapper.toResponse(job));
  }

  @GetMapping("/latest")
  @Operation(summary = "Most recent sync job, or 204 when none has run since startup")
  public ResponseEntity<SyncJobResponse> latest() {
    return bulkSync
        .latest()
        .map(job -> ResponseEntity.ok(SyncJobWebMapper.toResponse(job)))
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Progress of a sync job")
  public SyncJobResponse get(@PathVariable UUID id) {
    return bulkSync
        .find(id)
        .map(SyncJobWebMapper::toResponse)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Sync job %s was not found".formatted(id)));
  }
}
