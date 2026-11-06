# Shared JWT contract (all polyglot services)

Preserved from the Spring monolith (`spring-monolith` tag).

| Field | Value |
|-------|-------|
| Algorithm | HS256 |
| Issuer | `pokeapp` |
| Subject | username (lowercase) |
| Claim `role` | `USER` or `ADMIN` |
| TTL | `POKEAPP_JWT_TTL` (default `2h`) |
| Secret | `POKEAPP_JWT_SECRET` (min 32 bytes, shared by all services) |
| Header | `Authorization: Bearer <token>` |

## Token response (login)

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresAt": "2026-01-01T00:00:00Z",
  "username": "ash",
  "role": "USER"
}
```

## Auth rules

| Path | Access |
|------|--------|
| `POST /api/v1/auth/register`, `POST /api/v1/auth/login` | public |
| `GET /api/v1/pokemon/**` | public |
| `GET\|PUT\|POST /api/v1/local-pokemon/**` (except create/delete) | authenticated USER+ |
| `POST /api/v1/local-pokemon`, `DELETE /api/v1/local-pokemon/{id}` | ADMIN |
| `/api/v1/admin/**` | ADMIN |

## Demo users (Flyway seed)

| User | Password | Role |
|------|----------|------|
| `ash` | `pikachu123` | USER |
| `admin` | `admin123` | ADMIN |
