package org.pokeapp.catalog.web;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import org.pokeapp.catalog.StubPokeApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PokemonCatalogResourceTest {

  private static StubPokeApi pokeApi;

  @Inject CacheManager cacheManager;

  @BeforeAll
  static void startStub() throws IOException {
    pokeApi = StubPokeApi.start();
  }

  @AfterAll
  static void stopStub() {
    pokeApi.stop();
  }

  @BeforeEach
  void resetState() {
    pokeApi.reset();
    cacheManager
        .getCacheNames()
        .forEach(name -> cacheManager.getCache(name).orElseThrow().invalidateAll().await().indefinitely());
  }

  @Test
  void browseReturnsPageWithMonolithContractAndClientCaching() {
    given()
        .when()
        .get("/api/v1/pokemon?page=0&size=20")
        .then()
        .statusCode(200)
        .header("Cache-Control", equalTo("max-age=300"))
        .contentType(containsString("application/json"))
        .body("page", equalTo(0))
        .body("size", equalTo(20))
        .body("totalElements", equalTo(2))
        .body("totalPages", equalTo(1))
        .body("content.name", contains("bulbasaur", "ivysaur"))
        .body("content[0].id", equalTo(1))
        .body("content[0].spriteUrl", equalTo("https://sprites/1.png"))
        .body("content[0].category", equalTo("Seed Pok\u00e9mon"))
        .body("content[0].weightKg", equalTo(6.9f))
        .body("content[0].abilities", contains("overgrow"))
        .body("content[0].types", contains("grass", "poison"));
  }

  @Test
  void browseUsesDefaultsWhenParamsAreMissing() {
    given()
        .when()
        .get("/api/v1/pokemon")
        .then()
        .statusCode(200)
        .body("page", equalTo(0))
        .body("size", equalTo(20));
  }

  @Test
  void browseBeyondLastPageReturnsEmptyContent() {
    given()
        .when()
        .get("/api/v1/pokemon?page=5&size=20")
        .then()
        .statusCode(200)
        .body("content.size()", equalTo(0))
        .body("totalElements", equalTo(2));
  }

  @Test
  void browseRejectsInvalidPaging() {
    given()
        .when()
        .get("/api/v1/pokemon?page=-1&size=101")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("title", equalTo("Validation failed"))
        .body("status", equalTo(400))
        .body("instance", equalTo("/api/v1/pokemon"))
        .body("errors.page", equalTo("must be zero or positive"))
        .body("errors.size", equalTo("must be between 1 and 100"));
  }

  @Test
  void browseRejectsNonNumericPaging() {
    given()
        .when()
        .get("/api/v1/pokemon?page=abc")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("errors.page", equalTo("must be an integer"));
  }

  @Test
  void detailReturnsStatsAndEvolutionChain() {
    given()
        .when()
        .get("/api/v1/pokemon/Bulbasaur")
        .then()
        .statusCode(200)
        .header("Cache-Control", equalTo("max-age=300"))
        .body("id", equalTo(1))
        .body("name", equalTo("bulbasaur"))
        .body("imageUrl", equalTo("https://art/1.png"))
        .body("category", equalTo("Seed Pok\u00e9mon"))
        .body("heightM", equalTo(0.7f))
        .body("weightKg", equalTo(6.9f))
        .body("types", contains("grass", "poison"))
        .body("abilities", contains("overgrow"))
        .body("stats.name", contains("hp", "attack"))
        .body("stats.baseValue", contains(45, 49))
        .body("totalBaseStats", equalTo(94))
        .body("description", equalTo("A strange seed was planted on its back."))
        .body("evolutionChain.speciesId", contains(1, 2))
        .body("evolutionChain.name", contains("bulbasaur", "ivysaur"))
        .body("evolutionChain.stage", contains(1, 2))
        .body(
            "evolutionChain[1].spriteUrl",
            equalTo(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png"));
  }

  @Test
  void detailOfUnknownPokemonIsProblemJson404() {
    given()
        .when()
        .get("/api/v1/pokemon/missingno")
        .then()
        .statusCode(404)
        .contentType("application/problem+json")
        .body("title", equalTo("Resource not found"))
        .body("status", equalTo(404))
        .body("detail", equalTo("Pokemon 'missingno' was not found in PokeAPI"))
        .body("$", not(hasKey("errors")));
  }

  @Test
  void detailRejectsMalformedIdentifier() {
    given()
        .when()
        .get("/api/v1/pokemon/bad_name!")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("errors.idOrName", containsString("positive id or a name"));
  }

  @Test
  void pokeApiOutageIsProblemJson503() {
    pokeApi.setDown(true);

    given()
        .when()
        .get("/api/v1/pokemon")
        .then()
        .statusCode(503)
        .contentType("application/problem+json")
        .body("title", equalTo("PokeAPI unavailable"))
        .body("status", equalTo(503));

    given().when().get("/api/v1/pokemon/1").then().statusCode(503);
  }

  @Test
  void unreachablePokeApiIsAlso503AndRecoversWhenBackUp() {
    pokeApi.setDown(true);
    given().when().get("/api/v1/pokemon/1").then().statusCode(503);

    pokeApi.setDown(false);
    given().when().get("/api/v1/pokemon/1").then().statusCode(200);
  }

  @Test
  void successfulUpstreamResponsesAreCached() {
    given().when().get("/api/v1/pokemon?page=0&size=20").then().statusCode(200);
    int upstreamAfterFirst = pokeApi.totalHits();

    given().when().get("/api/v1/pokemon?page=0&size=20").then().statusCode(200);
    given().when().get("/api/v1/pokemon/bulbasaur").then().statusCode(200);

    assertThat(upstreamAfterFirst).isPositive();
    // detail reuses cached pokemon + species, and fetches the evolution chain once
    assertThat(pokeApi.totalHits()).isEqualTo(upstreamAfterFirst + 1);
    assertThat(pokeApi.hits("/pokemon-species/1")).isEqualTo(1);
  }

  @Test
  void unknownPokemonIsNotCached() {
    given().when().get("/api/v1/pokemon/missingno").then().statusCode(404);
    given().when().get("/api/v1/pokemon/missingno").then().statusCode(404);

    assertThat(pokeApi.hits("/pokemon/missingno")).isEqualTo(2);
  }

  @Test
  void actuatorHealthAliasReportsUp() {
    given().when().get("/actuator/health").then().statusCode(200).body("status", equalTo("UP"));
    given()
        .when()
        .get("/actuator/health/readiness")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Test
  void quarkusHealthReportsUp() {
    given().when().get("/q/health").then().statusCode(200).body("status", equalTo("UP"));
  }

  @Test
  void corsAllowsConfiguredOriginsOnly() {
    given()
        .header("Origin", "http://localhost:3000")
        .when()
        .get("/api/v1/pokemon/1")
        .then()
        .statusCode(200)
        .header("Access-Control-Allow-Origin", equalTo("http://localhost:3000"));
  }
}
