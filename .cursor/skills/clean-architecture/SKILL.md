---
name: clean-architecture
description: Enforces Clean Architecture (ports and adapters) in the PokeApp Spring Boot backend - layer/package map, dependency rule, ports, boundary mapping and ArchUnit rules. Use when adding or modifying any Java class, package, use case, controller, repository, adapter or external integration in this project.
---

# Clean Architecture (PokeApp backend)

Single Gradle module. Layers are packages under `org.ballastlane.pokeapp`, and the dependency rule is enforced by ArchUnit (`ArchitectureTest`).

## Layer map

| Package | Contains | May depend on |
|---------|----------|---------------|
| `domain` | Entities/value objects (records or final classes), domain rules, domain exceptions | JDK only |
| `application` | `port.in` (use case interfaces + commands/queries), `port.out` (driven ports), `service` (use case implementations) | `domain` |
| `infrastructure` | Driven adapters: `pokeapi` (HTTP client), `persistence` (JPA), `security` (JWT, BCrypt), `sync` (in-memory job store) | `application`, `domain`, frameworks |
| `web` | Driving adapters: REST controllers, request/response DTOs, exception handler, web mappers | `application`, `domain`, Spring Web |
| `config` | Spring `@Configuration` wiring use cases into beans, properties | everything |

Dependency rule: arrows always point inward. `domain` and `application` never import `org.springframework..`, `jakarta.persistence..`, `com.fasterxml..` or anything from `web`/`infrastructure`. Keep the core framework-free.

## Rules

1. **Use cases are plain Java classes.** No `@Service`/`@Transactional` in `application`. They are instantiated in `config.UseCaseConfig`. Transactions are handled in the persistence adapter (or by a transactional decorator in `config`).
2. **One input port per use case** (`BrowsePokemonUseCase`, `SyncPokemonUseCase`, ...). Controllers depend on input port interfaces, never on service implementations.
3. **Output ports are named after the capability**, not the technology: `PokemonCatalogPort` (not `PokeApiClient`), `LocalPokemonRepositoryPort`, `PasswordHasherPort`, `TokenIssuerPort`.
4. **Map at every boundary.** Each layer owns its models:
   - `web`: `*Request`/`*Response` records with Bean Validation; mapping in `web..mapper`.
   - `infrastructure.persistence`: `*JpaEntity`; mapping in the adapter's mapper.
   - `infrastructure.pokeapi`: `*Dto` records mirroring PokeAPI JSON.
   - Never return JPA entities or PokeAPI DTOs from a port.
5. **Domain exceptions** extend `DomainException` (`PokemonNotFoundException`, `PokemonAlreadySyncedException`, `InvalidCredentialsException`, ...). Only `web.error.GlobalExceptionHandler` translates them to HTTP status codes.
6. **Validation lives in two places**: syntactic validation (required, size, format) on web DTOs; business invariants inside domain constructors/methods. Never only in the controller.
7. **Framework annotations stay in the outer ring**: `@Cacheable` on the PokeAPI adapter, `@Transactional` on persistence adapters, security in `infrastructure.security` + `config`.

## Adding a feature (checklist)

```
- [ ] Domain model/rule + unit test (domain)
- [ ] Input port interface + command/query record (application.port.in)
- [ ] Output port(s) if new external capability (application.port.out)
- [ ] Use case implementation + unit test with mocked ports (application.service)
- [ ] Adapter implementing output port + integration test (infrastructure)
- [ ] Controller + DTOs + mapper + @WebMvcTest (web)
- [ ] Bean wiring (config)
- [ ] ArchitectureTest still green
```

## ArchUnit rules (keep in `src/test/java/.../architecture/ArchitectureTest.java`)

- `layeredArchitecture()`: Domain, Application, Infrastructure, Web, Config; Domain accessed only by Application/Infrastructure/Web/Config; Application accessed only by Infrastructure/Web/Config; Infrastructure and Web accessed only by Config.
- `noClasses().that().resideInAnyPackage("..domain..", "..application..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.fasterxml..")`.
- Controllers (`..web..` annotated `@RestController`) must not depend on `..application.service..`.
- Classes named `*JpaEntity` must reside in `..infrastructure.persistence..`.
