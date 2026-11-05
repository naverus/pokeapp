# Polyglot backend: interview talking points

This POC replaces a Spring Boot monolith with three services. The job description says *Quarkus, Vert.x, Micronaut, **or** Kotlin* — you do **not** need all of them in production. You **do** need to show you can pick the right tool for a POC and explain trade-offs.

## Why not one process with three frameworks?

Application frameworks (Quarkus, Micronaut, Spring) each own DI, HTTP, config and lifecycle. Running more than one in a single JVM is an anti-pattern. Vert.x is a toolkit: it can stand alone or sit under Quarkus.

## Service → stack mapping

| Service | Choice | Why this fit |
|---------|--------|--------------|
| Auth | **Vert.x** | Small surface (2 POSTs), event-loop I/O to MySQL, JWT/BCrypt without a full app framework. Shows you can work “close to the metal”. |
| Catalog | **Quarkus** + Vert.x WebClient | Outbound-heavy, cacheable reads against PokeAPI. Quarkus shines at fast startup, reactive HTTP and native-image path; Vert.x stays visible in the client. |
| Collection | **Micronaut** | JPA + Flyway + JWT security + background sync jobs. Compile-time DI and AOT story contrasts with Quarkus’s build-time approach — same problem space, different framework. |

## Comparison cheat sheet

| Topic | Vert.x | Quarkus | Micronaut | Spring Boot (retired) |
|-------|--------|---------|-----------|------------------------|
| Model | Toolkit / verticles | Framework (build-time) | Framework (compile-time DI) | Framework (runtime DI) |
| Startup | Very fast, tiny | Very fast; native optional | Fast; native optional | Heavier classic startup |
| Reactive | First-class | Vert.x under the hood | Optional (Reactor-like) | WebFlux optional |
| DI | Manual / verticle wiring | CDI (ArC) | Annotation processing | Spring IoC |
| When to pick | Edge, gateways, high concurrency glue | Cloud-native Java, k8s, GraalVM | Microservices wanting AOT without full Quarkus stack | Broad ecosystem, team familiarity |

## Production story (what you’d say next)

1. **POC:** polyglot as here — prove APIs and UX.
2. **Harden:** one primary framework for the long-lived domain (usually Quarkus *or* Micronaut *or* Spring), keep Vert.x where event-loop I/O wins.
3. **Ops:** shared JWT secret → move to JWKS / Azure AD; Flyway stays in one owner; gateway becomes Azure App Gateway / ingress; add observability (OpenTelemetry).

## Demo script (≈ 5 minutes)

1. `docker compose up --build` → open `:3000`.
2. Login as `ash` / `pikachu123` → browse catalog (Quarkus) → add to collection (Micronaut).
3. Login as `admin` / `admin123` → Restock Collection.
4. Show `services/auth-vertx`, `catalog-quarkus`, `collection-micronaut` and nginx routes in `frontend/nginx.conf`.
5. Point to tag `spring-monolith` as the previous single-stack baseline.

## Shared contracts

- [shared/jwt-contract.md](../shared/jwt-contract.md)
- [shared/api-checklist.md](../shared/api-checklist.md)
