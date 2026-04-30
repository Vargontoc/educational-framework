# ADR-008 — Shared Module: Generic Classes and Utilities
# ──────────────────────────────────────────────

## Status
status:        accepted
date:          2026-04-30
superseded_by: —

## Context
Within the modular monolithic architecture based on Spring Boot, a shared module is required to host generic classes, interfaces, exceptions, API responses, and utilities reused across multiple backend components. This module, named `shared`, maintains code consistency, prevents duplication, and provides a single source of truth for cross-cutting concerns such as base entities, validations, standardised responses, and error handling.

The module lives under `es.vargontoc.educational.framework.shared` and is consumed by domain modules (TTS, agent, etc.) that require generic building blocks without owning business logic.

## Decision
A `shared` module will be created inside the backend under the package `es.vargontoc.educational.framework.shared`. It will contain:

- **`BaseEntity`** — abstract `@MappedSuperclass` for common JPA fields:
    - `id`: `Long` (mapped to `bigint`, auto-generated via sequence)
    - `createdAt`: `LocalDateTime` (JPA Auditing, non-updatable)
    - `updatedAt`: `LocalDateTime` (JPA Auditing)
- **`IValidator<T>`** — generic functional interface with a single `validate(T target)` method.
- **`AbstractValidator<T>`** — abstract class implementing `IValidator<T>` with reusable guard methods: `requireNonNull`, `requireNonBlank`, `requireMaxLength`, `requirePositive`.
- **Custom exceptions** — all extend `AppException` (base `RuntimeException` with `HttpStatus`):
    - `AppException` — base; holds `HttpStatus` for global exception handler mapping
    - `ResourceNotFoundException` — HTTP 404
    - `ValidationException` — HTTP 400
    - `SessionException` — HTTP 401
    - `ConflictException` — HTTP 409
- **`ApiResponse<T>`** — standard response envelope returned to the frontend; static factory methods: `ok`, `created`, `error`.

This module has no external dependencies beyond Spring and Java SE, and contains no business logic.

## Consequences

positive:
  - Eliminates code duplication across domain modules by centralising common building blocks.
  - Enforces consistent API response shape and error handling across all endpoints.
  - `AbstractValidator` makes domain-specific validators easy to write with minimal boilerplate.
  - `BaseEntity` ensures all persisted entities share the same auditing columns without repetition.

negative:
  - Requires upfront design effort to keep `shared` truly generic — risk of "util dumping ground" if discipline is not maintained.
  - Any breaking change to `ApiResponse` or `AppException` propagates to all consuming modules.

neutral:
  - Does not affect TTS or AI integration logic directly; provides infrastructure that eases their development.

## Alternatives considered

alternative: Inline generic classes in each domain module
reason_rejected: Leads to code duplication and inconsistent error/response handling across domains.

alternative: Use a separate Maven module for shared code
reason_rejected: Unnecessary for a modular monolith; adds build complexity without benefit.

alternative: Use only `BaseEntity` and skip validators and exceptions
reason_rejected: Does not address the need for consistent API response shape and reusable validation guards.

## References
- ADR-007: Backend Stack — Spring Boot + Spring AI
- ADR-004: TTS Service — Coqui TTS on-premise
- FEAT-001: 300-char limit on `content_text`
- FEAT-002: `agent_name` sanitization
- FEAT-003: Prosody presets by age
- FEAT-004, FEAT-005, FEAT-006: Finite and curated catalog content