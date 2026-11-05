package org.ballastlane.pokeapp.web.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import java.util.UUID;
import org.ballastlane.pokeapp.application.port.in.BulkSyncPokemonUseCase;
import org.ballastlane.pokeapp.domain.exception.SyncJobNotFoundException;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.StartSyncJobRequest;
import org.ballastlane.pokeapp.web.dto.SyncJobDtos.SyncJobResponse;
import org.ballastlane.pokeapp.web.mapper.SyncJobWebMapper;

/** Restock Collection: on-demand bulk import from the Pokedex (ADMIN). */
@Controller("/api/v1/admin/sync-jobs")
@Secured("ADMIN")
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class SyncJobController {

  private final BulkSyncPokemonUseCase bulkSync;

  public SyncJobController(BulkSyncPokemonUseCase bulkSync) {
    this.bulkSync = bulkSync;
  }

  @Post
  public HttpResponse<SyncJobResponse> start(
      HttpRequest<?> request,
      @Body @Nullable @Valid StartSyncJobRequest body,
      Authentication authentication) {
    var job = bulkSync.start(SyncJobWebMapper.toRange(body), authentication.getName());
    var location = LocationUris.of(request, "/api/v1/admin/sync-jobs/" + job.id());
    return HttpResponse.accepted(location).body(SyncJobWebMapper.toResponse(job));
  }

  @Get("/latest")
  public HttpResponse<SyncJobResponse> latest() {
    return bulkSync
        .latest()
        .map(job -> HttpResponse.ok(SyncJobWebMapper.toResponse(job)))
        .orElseGet(HttpResponse::noContent);
  }

  @Get("/{id}")
  public SyncJobResponse get(@PathVariable UUID id) {
    return bulkSync
        .find(id)
        .map(SyncJobWebMapper::toResponse)
        .orElseThrow(() -> new SyncJobNotFoundException(id));
  }
}
