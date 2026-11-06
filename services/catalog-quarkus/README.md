# catalog-quarkus

Public Pokemon catalog microservice (Quarkus 3.39, Java 21, Vert.x WebClient, Caffeine cache).
Same HTTP/JSON contract as the Spring monolith (tag `spring-monolith`). Port **8082**.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/pokemon?page=0&size=20` | `{content,page,size,totalElements,totalPages}`, `Cache-Control: max-age=300`; page >= 0, size 1-100 |
| GET | `/api/v1/pokemon/{idOrName}` | detail with stats + evolution chain; 404 if unknown |
| GET | `/actuator/health` (+ `/liveness`, `/readiness`) | Actuator-style `{"status":"UP"}` alias for nginx/compose |
| GET | `/q/health`, `/q/health/ready` | SmallRye Health (Docker HEALTHCHECK uses `/q/health/ready`) |
| GET | `/q/openapi`, `/swagger-ui` | OpenAPI |

Errors are `application/problem+json` (400 validation with `errors` map, 404, 503 when PokeAPI is down, 500).

## Configuration

| Env var | Default |
|---|---|
| `POKEAPP_POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` |
| `POKEAPP_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |

Timeouts / concurrency (`pokeapp.pokeapi.*`, 3s connect, 10s read, 16 concurrent entries per page) and cache
sizes/TTL (5000 entries, 6h) live in `src/main/resources/application.properties`.

## Run

```bash
./gradlew quarkusDev                 # dev mode on :8082 (Java 21 toolchain is auto-provisioned)
./gradlew test                       # unit + @QuarkusTest (in-process PokeAPI stub on :18089)
./gradlew quarkusBuild               # -> build/quarkus-app/quarkus-run.jar
java -jar build/quarkus-app/quarkus-run.jar

docker build -t pokeapp-catalog-quarkus .
docker run --rm -p 8082:8082 pokeapp-catalog-quarkus
```

## Layout

`domain` (copied from the monolith) -> `application` (use cases + `PokemonCatalogPort`, now returning Mutiny `Uni`)
-> `infrastructure/pokeapi` (`PokeApiClient` on Vert.x WebClient with `@CacheResult`, `PokeApiCatalogAdapter`, `PokeApiMapper`)
-> `web` (JAX-RS resource, DTOs, RFC 7807 mappers). `config/UseCaseConfig` wires services into CDI.
