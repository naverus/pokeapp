---
name: tdd-testing
description: Test-driven development workflow and testing standards for the PokeApp (Spring Boot backend and React frontend) - red/green/refactor loop, which test type per layer, naming, tooling and coverage gates. Use when writing or changing production code, adding tests, fixing bugs, or when the user mentions TDD, tests, coverage, JUnit, Mockito, Vitest or Testing Library.
---

# TDD and Testing (PokeApp)

## Red / Green / Refactor loop

```
- [ ] RED: write the smallest failing test that expresses the next behaviour
- [ ] Run it and confirm it fails for the expected reason
- [ ] GREEN: write the minimum production code to pass
- [ ] Run the whole relevant test class
- [ ] REFACTOR: remove duplication, improve names; tests stay green
- [ ] Repeat; run `./gradlew check` (or `npm test`) before finishing
```

Bug fixes start with a test that reproduces the bug.

## Test type per layer (backend)

| Layer | Test type | Tools | Spring context |
|-------|-----------|-------|----------------|
| `domain` | Pure unit | JUnit 5, AssertJ | none |
| `application.service` | Unit with mocked output ports | Mockito (`@ExtendWith(MockitoExtension.class)`) | none |
| `infrastructure.pokeapi` | Adapter test against stubbed HTTP | WireMock (`WireMockExtension`) | none (build `RestClient` manually) |
| `infrastructure.persistence` | Slice test with real MySQL | `@DataJpaTest` + Testcontainers `MySQLContainer` (`@ServiceConnection`), Flyway enabled | JPA slice |
| `infrastructure.security` | Unit | JUnit | none |
| `web` | Controller slice | `@WebMvcTest`, `MockMvc`, `@MockitoBean` input ports, `spring-security-test` | MVC slice |
| architecture | Rules | ArchUnit | none |
| end-to-end | Smoke | `@SpringBootTest` + Testcontainers (skipped automatically when Docker is unavailable via `@Testcontainers(disabledWithoutDocker = true)`) | full |

Never use `@SpringBootTest` for something a unit or slice test can cover.

## Conventions

- Test class mirrors production class: `SyncPokemonService` -> `SyncPokemonServiceTest`.
- Method names: `should<Outcome>_when<Condition>` (e.g. `shouldThrowNotFound_whenPokemonDoesNotExist`).
- Body structure with `// given`, `// when`, `// then` comments.
- AssertJ only (`assertThat`, `assertThatThrownBy`); no JUnit `assertEquals`.
- Test data via small builder/factory classes in `src/test/java/.../fixtures` (e.g. `PokemonFixtures.pikachu()`).
- Web tests assert status, JSON body (`jsonPath`) and `ProblemDetail` fields for errors (400, 401, 403, 404, 409).

## Coverage gate

`./gradlew check` runs `jacocoTestCoverageVerification` with a minimum of **80% line coverage** (excluding `config`, the `*Application` class and DTO records). A failing gate means more tests, not a lower threshold.

## Frontend

- Vitest + React Testing Library + `@testing-library/user-event`; HTTP stubbed with MSW (`src/test/server.ts`).
- Test behaviour from the user's perspective: query by role/label, not by class names or test ids.
- Each feature folder has tests for its hooks/components; render with `renderWithProviders` (QueryClient, Router, Theme, Auth).
- Tests must not print React `act()` or key warnings - treat warnings as failures.
- Run with `npm test` (`vitest run`).
