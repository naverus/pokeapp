# collection-micronaut

Micronaut 4 (Java 21) collection service: local Pokemon CRUD, single sync from PokeAPI and admin bulk
"Restock" sync jobs. It is the **Flyway owner** of the full schema (`users` + `pokemon` tables and demo seed)
and validates JWTs issued by `auth-vertx` (HS256, issuer `pokeapp`, claim `role`, shared secret).

Port **8083**. Health: `GET /actuator/health` (public).

## Environment

| Variable | Default | Notes |
|----------|---------|-------|
| `POKEAPP_DB_URL` | `jdbc:mysql://localhost:3307/pokeapp` | |
| `POKEAPP_DB_USERNAME` / `POKEAPP_DB_PASSWORD` | `pokeapp` / `pokeapp` | |
| `POKEAPP_JWT_SECRET` | dev secret | min 32 bytes, same value in every service |
| `POKEAPP_JWT_TTL` | `2h` | informational (tokens are issued by auth-vertx) |
| `POKEAPP_POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | |
| `POKEAPP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | comma separated |
| `PORT` | `8083` | |

## Run

```bash
# MySQL (compose from the repo root)  docker compose up -d mysql
./gradlew run                    # needs JDK 21 (toolchain) ; or ./gradlew installDist
./gradlew test                   # unit tests + Testcontainers smoke test (needs Docker)
docker build -t collection-micronaut . && docker run -p 8083:8083 \
  -e POKEAPP_DB_URL=jdbc:mysql://host.docker.internal:3307/pokeapp collection-micronaut
```

## Endpoints

| Method | Path | Role |
|--------|------|------|
| GET | `/api/v1/local-pokemon?page&size&name` | USER+ |
| GET | `/api/v1/local-pokemon/stored-ids?pokeApiIds=1,4,25` | USER+ |
| GET | `/api/v1/local-pokemon/{id}` | USER+ |
| POST | `/api/v1/local-pokemon/sync/{idOrName}` | USER+ (201 + Location) |
| PUT | `/api/v1/local-pokemon/{id}` | USER+ (body carries `version`) |
| POST | `/api/v1/local-pokemon` | ADMIN |
| DELETE | `/api/v1/local-pokemon/{id}` | ADMIN (204) |
| POST | `/api/v1/admin/sync-jobs` | ADMIN (202, optional `{"fromId","toId"}`) |
| GET | `/api/v1/admin/sync-jobs/{id}` , `/latest` | ADMIN (`latest` -> 204 when none) |

Errors are RFC 7807 `application/problem+json`.
