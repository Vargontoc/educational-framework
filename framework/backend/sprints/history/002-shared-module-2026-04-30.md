# Sprint 002 - backend
# -----------------------------------------------

## Goal
Implement the `shared` module per ADR-008: `BaseEntity`, exception hierarchy, `IValidator`/`AbstractValidator`, and `ApiResponse` — zero business logic, consumed by all future domain modules.

## Status
status: completed
started_at: 2026-04-30 00:00:00
closed_at: 2026-04-30 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Enable JPA Auditing: add `@EnableJpaAuditing` to `EducationalFrameworkApplication.java`
- [x] Create `shared/model/BaseEntity.java` — abstract `@MappedSuperclass` with `id` (Long, sequence), `createdAt`, `updatedAt` via `@CreatedDate` / `@LastModifiedDate`
- [x] Create `shared/exception/AppException.java` — base `RuntimeException` holding `HttpStatus`
- [x] Create `shared/exception/ResourceNotFoundException.java` — extends `AppException`, HTTP 404
- [x] Create `shared/exception/ValidationException.java` — extends `AppException`, HTTP 400
- [x] Create `shared/exception/SessionException.java` — extends `AppException`, HTTP 401
- [x] Create `shared/exception/ConflictException.java` — extends `AppException`, HTTP 409
- [x] Create `shared/validation/IValidator.java` — `@FunctionalInterface`, single method `validate(T target)`
- [x] Create `shared/validation/AbstractValidator.java` — implements `IValidator<T>`; guard methods: `requireNonNull`, `requireNonBlank`, `requireMaxLength`, `requirePositive`
- [x] Create `shared/api/ApiResponse.java` — generic record with static factories: `ok(T data)`, `created(T data)`, `error(String message)`, `error(String message, List<String> errors)`
- [x] Unit test: `AppExceptionTest` — verify each subclass sets the correct `HttpStatus`
- [x] Unit test: `AbstractValidatorTest` — verify each guard method throws `ValidationException` on invalid input and passes on valid input
- [x] Unit test: `ApiResponseTest` — verify factory methods produce correct `success`, `data`, `message` state

## Risks
- `@EnableJpaAuditing` must be present before any entity using `@CreatedDate`/`@LastModifiedDate` is persisted — missing annotation causes a silent null in auditing columns.
- `BaseEntity` must NOT be annotated with `@Entity` — it is a `@MappedSuperclass`; adding `@Entity` would try to create a `base_entity` table.
- `ApiResponse` as a Java record cannot be subclassed — design it as complete and final from the start.

## Dependencies
- Sprint 001 completed: Spring Boot context starts, Liquibase baseline applied.
- No new Liquibase migrations needed — `BaseEntity` is a `@MappedSuperclass`, not a table.
- No contract changes — this sprint adds no REST endpoints.

## Agent Instruction
- All new classes go under `es.vargontoc.educational.framework.shared`.
- Domain model rule: `BaseEntity` must not import Spring Web or Spring Security annotations — JPA Auditing only.
- `AppException` and subclasses must not reference Spring MVC — HTTP status is stored as `org.springframework.http.HttpStatus` but no web layer dependency is introduced (HttpStatus is in `spring-web`, already a transitive dependency).
- `AbstractValidator` guard methods must throw `ValidationException`, never return boolean — callers get a clean exception instead of a null-check chain.
- `ApiResponse` must be the only class returned from all REST controllers in future sprints — never expose domain models or JPA entities directly.
- Every new class requires at least one unit test (JUnit 5, no Mockito needed for pure logic).
- No Spring context needed for unit tests in this sprint — use plain `@Test` without `@SpringBootTest`.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
Sprint triggered by ADR-008 (`docs/architecture/decisions/ADR-008-Shared-Module.md`).

### Package layout after this sprint
```
shared/
  api/
    ApiResponse.java
  config/
    OpenApiConfig.java         (Sprint 001)
    SecurityConfig.java        (Sprint 001)
  exception/
    AppException.java
    ConflictException.java
    ResourceNotFoundException.java
    SessionException.java
    ValidationException.java
  model/
    BaseEntity.java
  validation/
    AbstractValidator.java
    IValidator.java
```

### BaseEntity design
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq")
    @SequenceGenerator(name = "default_seq", allocationSize = 1)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Exception hierarchy
```
RuntimeException
  └── AppException(message, HttpStatus)
        ├── ResourceNotFoundException  → 404
        ├── ValidationException        → 400
        ├── SessionException           → 401
        └── ConflictException          → 409
```

### ApiResponse shape
```java
public record ApiResponse<T>(boolean success, T data, String message, List<String> errors) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> created(T data) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
    public static <T> ApiResponse<T> error(String message, List<String> errors) { ... }
}
```

## Review

completed_tasks:
    - @EnableJpaAuditing added to EducationalFrameworkApplication.
    - BaseEntity: abstract @MappedSuperclass, Long id (sequence, allocationSize=1), LocalDateTime createdAt/updatedAt via JPA Auditing.
    - Exception hierarchy: AppException (base, holds HttpStatus) + ResourceNotFoundException (404), ValidationException (400), SessionException (401), ConflictException (409).
    - IValidator<T>: @FunctionalInterface with single validate(T) method.
    - AbstractValidator<T>: implements IValidator<T>, guard methods requireNonNull, requireNonBlank, requireMaxLength, requirePositive — all throw ValidationException on failure.
    - ApiResponse<T>: Java record with static factories ok, created, error (message only), error (message + errors list).
    - Unit tests: AppExceptionTest (4 assertions), AbstractValidatorTest (10 assertions), ApiResponseTest (4 assertions). No Spring context required.

incomplete_tasks:
    none

contract_changes:
    none — no REST endpoints added; openapi.json not affected.

learnings:
    - AbstractValidator uses a private static inner class in tests to expose protected methods without subclassing overhead in production code.
    - ApiResponse as a Java record is immutable and non-extensible by design — all variations covered by static factories.
    - BaseEntity uses @SequenceGenerator with allocationSize=1 to keep IDs sequential and predictable in single-node deployments.

next_sprint_suggestions:
    - Sprint 003: Global exception handler (@RestControllerAdvice) that maps AppException subclasses to ApiResponse error responses.
    - Sprint 004: First domain implementation (TTS or agent child) using BaseEntity, validators, and ApiResponse.
