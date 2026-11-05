# API acceptance checklist (preserve across rewrite)

## Auth (Vert.x)

- [ ] `POST /api/v1/auth/register` → 201 UserResponse; 400 validation; 409 username taken
- [ ] `POST /api/v1/auth/login` → TokenResponse; 401 bad credentials

## Catalog (Quarkus)

- [ ] `GET /api/v1/pokemon?page&size` → PageResponse; Cache-Control max-age=300; 503 if PokeAPI down
- [ ] `GET /api/v1/pokemon/{idOrName}` → PokemonDetailResponse; 404 unknown

## Collection (Micronaut)

- [ ] `GET /api/v1/local-pokemon?page&size&name` → paginated local list
- [ ] `GET /api/v1/local-pokemon/stored-ids?pokeApiIds=` → list of stored ids
- [ ] `GET /api/v1/local-pokemon/{id}` → 404 if missing
- [ ] `POST /api/v1/local-pokemon/sync/{idOrName}` → 201 + Location; 409 if already synced
- [ ] `PUT /api/v1/local-pokemon/{id}` → version optimistic lock; 409 stale
- [ ] `POST /api/v1/local-pokemon` → ADMIN create
- [ ] `DELETE /api/v1/local-pokemon/{id}` → ADMIN 204
- [ ] `POST /api/v1/admin/sync-jobs` → 202; body optional fromId/toId
- [ ] `GET /api/v1/admin/sync-jobs/{id}` / `latest`

## Errors

Every error is RFC 7807 (`application/problem+json`) with `title`, `detail`, `status`; validation includes `errors` map.
