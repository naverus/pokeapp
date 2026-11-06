---
name: clean-architecture
description: Enforces Clean Architecture (ports and adapters) in the PokeApp polyglot backends (Vert.x auth, Quarkus catalog, Micronaut collection) - layer/package map, dependency rule, ports, boundary mapping. Use when adding or modifying any Java class, package, use case, controller, repository, adapter or external integration in this project.
---

# Clean Architecture (PokeApp polyglot backends)

Three Gradle services under `services/`. Each keeps Clean Architecture packages; frameworks stay in the outer ring.

| Service | Root package | HTTP stack |
|---------|--------------|------------|
| `services/auth-vertx` | `org.pokeapp.auth` | Vert.x Web |
| `services/catalog-quarkus` | `org.pokeapp.catalog` | Quarkus REST + Vert.x WebClient |
| `services/collection-micronaut` | `org.pokeapp` | Micronaut |

## Layer map (per service)

| Package | Contains | May depend on |
|---------|----------|---------------|
| `domain` | Entities/value objects, domain rules, domain exceptions | JDK only |
| `application` | `port.in` / use cases, `port.out`, `service` implementations | `domain` |
| `infrastructure` | Driven adapters: DB, PokeAPI, JWT, sync store | `application`, `domain`, frameworks |
| `web` (or Vert.x handlers / Quarkus resources) | Driving adapters: HTTP, DTOs, problem+json | `application`, `domain`, HTTP framework |
| `config` | Wiring / properties | everything |

Dependency rule: arrows point inward. `domain` and `application` never import Vert.x, Quarkus, Micronaut, JPA or Jackson.

## Rules

1. **Use cases are plain Java** (no framework annotations in `application`).
2. **One input port per use case**; HTTP layer depends on ports, not concrete services when practical.
3. **Output ports named by capability** (`PokemonCatalogPort`, `UserRepository`, …).
4. **Map at every boundary** (HTTP DTOs ≠ domain ≠ persistence entities ≠ PokeAPI DTOs).
5. **Domain exceptions** → RFC 7807 in the HTTP error mapper only.
6. **Collection owns Flyway** for the shared MySQL schema; auth only reads/writes `users`.
7. **Shared JWT contract** — see `shared/jwt-contract.md` (HS256, issuer `pokeapp`, claim `role`).

## Adding a feature

```
- [ ] Domain + unit test
- [ ] Port + use case + mocked unit test
- [ ] Adapter + integration/smoke test
- [ ] HTTP layer + problem+json mapping
- [ ] Wire in that service's DI / main verticle
- [ ] Keep API checklist in shared/api-checklist.md green
```

Historical Spring monolith: git tag `spring-monolith`.
