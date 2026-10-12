# Generative AI exercise: Task management API

## 1. Prompt

Tool: Cursor (Agent mode). I first gave the agent the three project skills in `.cursor/skills`, so the generated code follows the same architecture, testing and quality rules as PokeApp.

```text
Role: senior Java backend engineer.
Context: Spring Boot 3.5, Java 21, Gradle, MySQL 8 + Flyway, Spring Security (JWT already exists;
the authenticated principal name is the username). A `users` table and `User` domain model already exist.
Follow the rules in .cursor/skills/clean-architecture, tdd-testing and code-quality.

Task: build a REST API for task management.
- Task fields: id, title (required, 1-120 chars), description (optional, max 2000),
  status (TODO | IN_PROGRESS | DONE, default TODO), due_date (optional ISO date, not in the past on create),
  owner (the authenticated user), version, created_at, updated_at.
- Endpoints under /api/v1/tasks:
  POST (201 + Location), GET list (paginated, filter by status, sort by dueDate),
  GET /{id}, PUT /{id} (full update with version), PATCH /{id}/status, DELETE /{id} (204).
- Users can only see and modify their own tasks; return 404 (not 403) for other users' tasks
  so their existence is not revealed.
- Errors as RFC 7807 ProblemDetail: 400 with field errors, 404, 409 on stale version.

Deliver in this order, using TDD:
1. Domain model + unit tests (rules: title trimmed, status transitions DONE -> TODO forbidden).
2. Use cases with input/output ports + Mockito tests.
3. JPA adapter + Flyway migration V3__create_tasks.sql + @DataJpaTest with Testcontainers.
4. Controller + DTOs + @WebMvcTest covering 201/400/401/404/409.
Do not put Spring annotations in domain/application. Show me the test first for each step.
```

Why the prompt is built this way:

- **Explicit constraints** (lengths, enum, status codes, ownership rule) leave the model nothing to guess.
- **The architecture rules come from the skills** instead of being repeated, which keeps the prompt short and the output consistent with the rest of the code.
- **The steps follow TDD order.** Smaller steps mean each diff is easy to review and output quality degrades less.
- **The ownership edge case is stated up front** (404 for another user's task). Models usually miss it otherwise.

## 2. Representative output (after review)

Domain:

```java
public record Task(Long id, String ownerUsername, String title, String description,
                   TaskStatus status, LocalDate dueDate, long version) {

  public Task {
    title = title == null ? null : title.strip();
    if (title == null || title.isEmpty() || title.length() > 120) {
      throw new DomainValidationException(Map.of("title", "must be 1-120 characters"));
    }
    if (description != null && description.length() > 2000) {
      throw new DomainValidationException(Map.of("description", "must be at most 2000 characters"));
    }
    status = status == null ? TaskStatus.TODO : status;
  }

  public Task moveTo(TaskStatus next) {
    if (status == TaskStatus.DONE && next == TaskStatus.TODO) {
      throw new DomainValidationException(Map.of("status", "a DONE task cannot go back to TODO"));
    }
    return new Task(id, ownerUsername, title, description, next, dueDate, version);
  }
}
```

Use case (plain Java, owner-scoped lookups):

```java
public class ManageTasksService implements ManageTasksUseCase {

  private final TaskRepositoryPort tasks;
  private final Clock clock;

  @Override
  public Task create(String owner, CreateTaskCommand command) {
    if (command.dueDate() != null && command.dueDate().isBefore(LocalDate.now(clock))) {
      throw new DomainValidationException(Map.of("dueDate", "must not be in the past"));
    }
    return tasks.save(new Task(null, owner, command.title(), command.description(),
        command.status(), command.dueDate(), 0));
  }

  @Override
  public Task get(String owner, long id) {
    return tasks.findByIdAndOwner(id, owner).orElseThrow(() -> new TaskNotFoundException(id));
  }

  @Override
  public Task update(String owner, UpdateTaskCommand command) {
    var current = get(owner, command.id());
    if (current.version() != command.version()) {
      throw new StaleVersionException(command.id(), command.version(), current.version());
    }
    return tasks.save(current.withDetails(command.title(), command.description(),
        command.status(), command.dueDate()));
  }
}
```

Controller test:

```java
@Test
void shouldReturn404_whenTaskBelongsToAnotherUser() throws Exception {
  given(manageTasks.get("ash", 7L)).willThrow(new TaskNotFoundException(7L));

  mockMvc.perform(get("/api/v1/tasks/7").with(user("ash").roles("USER")))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.title").value("Resource not found"));
}
```

## 3. How I validated the AI's suggestions

1. **Read every diff before accepting it.** I checked that each change followed the skills: no Spring imports in `domain` or `application`, and mapping at every boundary.
2. **Ran the gates.** `./gradlew check` runs ArchUnit, JaCoCo (80% minimum) and Spotless, so a wrong layer dependency or untested code fails the build, not the review.
3. **Confirmed the tests failed first.** For each test the model wrote, I ran it before the implementation existed to make sure it went red. Tests that pass from the start usually assert nothing.
4. **Tested against real infrastructure.** Testcontainers MySQL catches mistakes that H2 hides, such as Hibernate's naming strategy mapping `heightM` to `heightm`. That exact bug happened while building PokeApp and was caught by schema validation in the container test.
5. **Ran manual smoke tests** with curl against `docker compose up` for every status code listed in the README.

## 4. Corrections I made to the AI output

| AI suggestion | Problem | Correction |
|---------------|---------|------------|
| `@Service` and `@Transactional` on use cases | Breaks the framework-free core | Plain classes wired in `config`; transactions moved to the persistence adapter |
| Returned the JPA entity from the controller | Leaks persistence details and risks lazy-loading errors (open-in-view is disabled) | Response records plus mappers |
| `findById(id)` followed by an `owner.equals(...)` check that returned 403 | Reveals that other users' tasks exist; the check is easy to forget | `findByIdAndOwner`, which returns 404 |
| `catch (Exception e) { return ResponseEntity.status(500).body(e.getMessage()); }` | Leaks internals | Central `GlobalExceptionHandler` that returns ProblemDetail with a generic 500 message |
| No `version` field on updates | Concurrent edits silently overwrite each other | `@Version` plus a version check in the use case, returning 409 |
| `LocalDate.now()` called directly | Untestable and time-zone dependent | Injected `Clock` |
| Testcontainers version pinned by the Boot BOM | Fails on Docker Engine 29 (minimum API version 1.44) | Override `testcontainers.version` to 1.21.4 |

## 5. Edge cases, authentication and validation

- **Authentication:** the stateless JWT filter populates the `SecurityContext`, and the controller passes `authentication.getName()` to the use case. Unauthenticated requests get a 401 ProblemDetail from a custom entry point.
- **Authorization:** every query is scoped to the owner, and admin-only operations are restricted by role in the `SecurityFilterChain`.
- **Validation:** Bean Validation on DTOs handles syntax (sizes, required fields, enum parsing). The domain enforces invariants (trimmed title, allowed status transitions, due date not in the past). All violations come back together in one `errors` map.
- **Malformed input:** malformed JSON or an unknown enum value returns 400. A non-numeric id returns 400 (type mismatch). Page size is limited to 100.
- **Concurrency:** stale versions return 409. A unique-constraint race at the database level is translated into a domain conflict instead of a 500.
