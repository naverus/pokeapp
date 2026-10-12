---
name: code-quality
description: Code quality conventions for the PokeApp Java/Spring Boot backend and React/TypeScript/MUI frontend - naming, immutability, error handling, logging, formatting (Spotless, ESLint, Prettier) and a review checklist. Use when writing, refactoring or reviewing any code in this project.
---

# Code Quality (PokeApp)

## Java / Spring Boot

- Java 21 features: `record` for DTOs, commands and value objects; `switch` expressions; `var` only when the type is obvious.
- Immutability by default: `final` fields, `List.copyOf(...)` in record compact constructors, no setters in the domain.
- Constructor injection only (no field `@Autowired`). One public constructor.
- No Lombok anywhere - records cover the boilerplate.
- Small classes, single responsibility; methods under ~20 lines; no boolean flag parameters.
- Names reveal intent: `findByPokeApiId`, not `get2`. Packages are singular nouns (`mapper`, `error`).
- `Optional` only as a return type, never as a field or parameter.
- Errors: throw domain exceptions; map them once in `GlobalExceptionHandler` to RFC 7807 `ProblemDetail` (`type`, `title`, `status`, `detail`, plus `errors` map for validation). Never return `null` bodies or stack traces.
- Logging via SLF4J (`private static final Logger log = LoggerFactory.getLogger(X.class)`), parameterized messages, no `System.out`, never log passwords or tokens.
- Configuration via `@ConfigurationProperties` records (`pokeapp.*`), no hard-coded URLs or secrets; secrets come from environment variables.
- REST: plural nouns, `/api/v1` prefix, correct verbs and statuses (`201` + `Location` on create, `204` on delete), paginated responses use `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`).
- Formatting: `./gradlew spotlessApply` (google-java-format). `./gradlew check` must pass with no compiler warnings.

## React / TypeScript / MUI

- Strict TypeScript (`"strict": true`), no `any`; API types live in `shared/api/types.ts`.
- Feature-based folders: `src/features/<feature>/{components,hooks,api}`; cross-cutting code in `src/shared`.
- Server state with TanStack Query (query keys in one `queryKeys` object); local UI state with `useState`; auth in `AuthContext`. No duplicated server state in context.
- Components are function components, small and presentational where possible; data fetching inside custom hooks (`usePokemonPage`, `useUpdateLocalPokemon`).
- Forms with `react-hook-form` + `zod`; display server-side field errors returned in `ProblemDetail.errors`.
- MUI: theme in `src/shared/theme.ts`, responsive layouts with `Grid`/`Stack` breakpoints, `sx` for one-off styles. Images have `alt` text; interactive elements are accessible.
- Zero browser console warnings: stable `key`s, no uncontrolled-to-controlled switches, no missing dialog titles.
- `npm run lint` (ESLint) and `npm run format` (Prettier) must be clean.

## Review checklist

```
- [ ] Follows clean-architecture skill (dependency rule, boundary mapping)
- [ ] Tests written first / alongside (tdd-testing skill); coverage gate green
- [ ] Inputs validated, errors mapped to proper HTTP status + ProblemDetail
- [ ] No secrets, no sensitive data in logs
- [ ] Names are intention-revealing; no dead code or commented-out code
- [ ] Formatter and linters pass; no compiler or browser console warnings
```
