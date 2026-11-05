package org.ballastlane.pokeapp.catalog.web;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import org.ballastlane.pokeapp.catalog.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.catalog.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.catalog.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.catalog.domain.model.PageQuery;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/pokemon")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Catalog", description = "Live PokeAPI data (public, cached)")
public class PokemonCatalogResource {

  private static final CacheControl CLIENT_CACHE = clientCache();

  private final BrowsePokemonUseCase browsePokemon;
  private final GetPokemonDetailUseCase getPokemonDetail;

  @Inject
  public PokemonCatalogResource(
      BrowsePokemonUseCase browsePokemon, GetPokemonDetailUseCase getPokemonDetail) {
    this.browsePokemon = browsePokemon;
    this.getPokemonDetail = getPokemonDetail;
  }

  @GET
  @Operation(summary = "US01 - Browse Pokemon with sprite, category, mass and abilities")
  public Uni<Response> browse(
      @Parameter(schema = @Schema(type = SchemaType.INTEGER, defaultValue = "0"))
          @QueryParam("page")
          @DefaultValue("0")
          String page,
      @Parameter(schema = @Schema(type = SchemaType.INTEGER, defaultValue = "20"))
          @QueryParam("size")
          @DefaultValue("20")
          String size) {
    var query = pageQuery(page, size);
    return browsePokemon
        .browse(query)
        .map(
            result ->
                Response.ok(PokemonWebMapper.toPageResponse(result, PokemonWebMapper::toResponse))
                    .cacheControl(CLIENT_CACHE)
                    .build());
  }

  @GET
  @Path("/{idOrName}")
  @Operation(summary = "US02 - Image, stats, description and evolution chain of a Pokemon")
  public Uni<Response> detail(@PathParam("idOrName") String idOrName) {
    return getPokemonDetail
        .getDetail(idOrName)
        .map(
            detail ->
                Response.ok(PokemonWebMapper.toResponse(detail))
                    .cacheControl(CLIENT_CACHE)
                    .build());
  }

  /** Same validation behaviour as the monolith: every bad field is reported in one 400. */
  private static PageQuery pageQuery(String rawPage, String rawSize) {
    var errors = new LinkedHashMap<String, String>();
    var page = parseInt(rawPage, "page", errors);
    var size = parseInt(rawSize, "size", errors);
    if (!errors.isEmpty()) {
      throw new DomainValidationException(errors);
    }
    return new PageQuery(page, size);
  }

  private static int parseInt(String raw, String field, LinkedHashMap<String, String> errors) {
    try {
      return Integer.parseInt(raw.strip());
    } catch (NumberFormatException ex) {
      errors.put(field, "must be an integer");
      return 0;
    }
  }

  private static CacheControl clientCache() {
    var cacheControl = new CacheControl();
    cacheControl.setMaxAge(300);
    cacheControl.setNoTransform(false);
    return cacheControl;
  }
}
