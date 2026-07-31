# Sprint 003 - backend
# -----------------------------------------------

## Goal
Implement the global `@RestControllerAdvice` exception handler that maps all `AppException` subclasses and unhandled exceptions to consistent `ApiResponse` error responses - prerequisite for all future REST endpoint sprints.

## Status
status: completed
started_at: 2026-04-30 00:00:00
closed_at: 2026-04-30 20:20:00
blocked_by:
waiting_for:

## Tasks
- [x] Create `shared/web/GlobalExceptionHandler.java` - `@RestControllerAdvice` with a catch-all `@ExceptionHandler(AppException.class)` that returns `ResponseEntity<ApiResponse<Void>>` using `exception.getStatus()` and `ApiResponse.error(exception.getMessage())`
- [x] Add explicit `@ExceptionHandler` methods for each subtype: `ResourceNotFoundException` -> 404, `ValidationException` -> 400, `ConflictException` -> 409, `SessionException` -> 401 - Spring picks the most specific match, explicit handlers guarantee correct status even if the hierarchy changes
- [x] Add `@ExceptionHandler(Exception.class)` catch-all for unhandled exceptions -> 500 with a generic safe message; log with `log.error("Unhandled exception", e)` - never expose stack trace in the response body
- [x] Unit test: `GlobalExceptionHandlerTest` - instantiate the handler directly and assert each `@ExceptionHandler` method returns the expected `HttpStatus` and `ApiResponse` shape (no Spring context needed)
- [x] Update `docs/contracts/api/openapi.json` - create the file with the standard error response schema (`ApiResponse<Void>` shape for 400, 401, 404, 409, 500) so the frontend layer has a contract baseline
- [x] Configure CORS in `shared/config/SecurityConfig.java` - allow origins from `application.yml` property `app.cors.allowed-origins` (list), methods GET/POST/PATCH/DELETE/OPTIONS, headers `Content-Type` and `Authorization`, credentials allowed; add the property with a dev default (`http://localhost:5173`) to `application.yml`

## Risks
- `GlobalExceptionHandler` must be in a package Spring component-scan covers - `shared/web/` under the root `es.vargontoc.educational.framework` package is picked up automatically.
- If both `AppException` and a subtype handler exist, Spring resolves to the most specific - verified by tests, not assumed.
- The generic `Exception` handler must log server-side and return only a safe message; leaking stack traces or internal paths is a security issue even in a private app.
- Creating `openapi.json` now (error schema only) avoids a breaking-change surprise when Sprint 004 adds paths - the frontend layer can start depending on the file.

## Dependencies
- Sprint 002 completed: `AppException` hierarchy and `ApiResponse` must exist - this handler references both.
- No new Liquibase migrations needed.

## Agent Instruction
- Place the handler at `es.vargontoc.educational.framework.shared.web.GlobalExceptionHandler`.
- Return type must always be `ResponseEntity<ApiResponse<Void>>` - never a raw string or exception object.
- Use `@Slf4j` (Lombok) for logging; log unhandled exceptions at ERROR level with the full exception as the second argument.
- Test by instantiating `GlobalExceptionHandler` directly and calling each method - no `@SpringBootTest` or `@WebMvcTest` needed since methods are plain Java.
- `openapi.json` must include at minimum: `info`, `paths: {}`, and a `components.schemas.ApiResponse` with the error shape. Paths will be populated in future sprints.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
This sprint is a prerequisite for FEAT-001 (Family Module). Without a global handler, every controller method would need its own try/catch to return `ApiResponse` error shapes.

Suggested sequence after this sprint:
- Sprint 004: Family Module - domain layer (DB schema, domain models, ports, services, validators, unit tests)
- Sprint 005: Family Module - infrastructure layer (persistence adapters, controllers, DTOs, integration tests)

## Review

completed_tasks:
  - Implemented `GlobalExceptionHandler` with explicit handlers for exception subtypes, a fallback `AppException` handler, and a safe 500 handler with server-side logging.
  - Added direct unit tests in `GlobalExceptionHandlerTest` covering all exception handler methods and response shape.
  - Configured CORS in `SecurityConfig` using `app.cors.allowed-origins` from configuration.
  - Added default CORS property to `application.yml`.
  - Created `docs/contracts/api/openapi.json` baseline contract with shared `ApiResponse` error schema.
incomplete_tasks:
  - None.
contract_changes:
  - Added `docs/contracts/api/openapi.json` with error response schema baseline.
learnings:
  - Explicit subtype handlers protect status mapping from future hierarchy changes.
  - Defining OpenAPI early gives frontend a stable contract baseline before endpoint growth.
next_sprint_suggestions:
  - Start Sprint 004 with Family Module domain layer and reuse this shared error response contract in new endpoints.
