# Sprint 008 - backend
# -----------------------------------------------

## Goal
Complete FEAT-002 (Session Module) REST layer: JPA entities, persistence adapters, DTOs, REST controllers for auth and child session management, opaque token security filter, scheduled jobs for session expiration and archival, and integration tests.

## Status
status: completed
started_at: 2026-05-06 00:00:00
closed_at: 2026-05-06 00:00:00
blocked_by:
waiting_for:

## Tasks

### JPA Entities
- [x] Create `session/infrastructure/persistence/FamilySessionJpaEntity.java` â€” `@Entity @Table(name = "family_session")`, extends `BaseEntity`; fields: `tokenHash` (`@Column(name = "token_hash", unique = true)`), `tokenType`, `familyId` (`@Column(name = "family_id")`), `expiresAt`, `revoked`, `createdByIp` (`@Column(name = "created_by_ip")`), `deviceId` (`@Column(name = "device_id")`), `status` (String â€” map from `FamilySessionStatus` enum as string)
- [x] Create `session/infrastructure/persistence/ChildSessionJpaEntity.java` â€” `@Entity @Table(name = "child_session")`, extends `BaseEntity`; fields: `childProfileId` (`@Column(name = "child_profile_id")`), `familyId`, `startedAt`, `endedAt`, `durationSeconds`, `status`, `lastActivityAt` (`@Column(name = "last_activity_at")`), `heartbeatIntervalSeconds` (`@Column(name = "heartbeat_interval_seconds")`), `connectionMeta` (`@Column(name = "connection_meta", columnDefinition = "TEXT")`), `persistedGameStateRef`

### Spring Data Repositories
- [x] Create `session/infrastructure/persistence/FamilySessionJpaRepository.java` â€” `JpaRepository<FamilySessionJpaEntity, Long>` with:
    - `Optional<FamilySessionJpaEntity> findByTokenHash(String tokenHash)`
    - `List<FamilySessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status)`
- [x] Create `session/infrastructure/persistence/ChildSessionJpaRepository.java` â€” `JpaRepository<ChildSessionJpaEntity, Long>` with:
    - `Optional<ChildSessionJpaEntity> findByChildProfileIdAndStatus(Long childProfileId, String status)`
    - `List<ChildSessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status)`
    - `List<ChildSessionJpaEntity> findByStatusAndLastActivityAtBefore(String status, LocalDateTime cutoff)`

### Persistence Adapters
- [x] Create `session/infrastructure/persistence/FamilySessionPersistenceAdapter.java` â€” `@Repository`, implements `FamilySessionRepository`; static `toDomain` / `toJpa` mappers; `findActiveByFamilyId` passes `"ACTIVE"` as status string; `saveAll` calls `jpaRepository.saveAll()` then maps back to domain
- [x] Create `session/infrastructure/persistence/ChildSessionPersistenceAdapter.java` â€” `@Repository`, implements `ChildSessionRepository`; `findActiveByChildProfileId` passes `"ACTIVE"`; `findExpirableSessions(cutoff)` calls `findByStatusAndLastActivityAtBefore("ACTIVE", cutoff)`

### DTOs
- [x] Create `session/infrastructure/dto/LoginRequest.java` â€” record: `String pin`
- [x] Create `session/infrastructure/dto/LoginResponse.java` â€” record: `String token`, `Long sessionId`, `Long familyId`, `LocalDateTime createdAt` â€” `token` is the raw opaque token; this is the **only** response that ever contains it
- [x] Create `session/infrastructure/dto/LogoutRequest.java` â€” record: `String token` (optional â€” if null, token is taken from `Authorization` header in controller)
- [x] Create `session/infrastructure/dto/OpenChildSessionRequest.java` â€” record: `Long childProfileId`, `Integer heartbeatIntervalSeconds` (nullable â€” defaults to `SessionProperties.defaultHeartbeatIntervalSeconds`)
- [x] Create `session/infrastructure/dto/ChildSessionResponse.java` â€” record: `Long id`, `Long childProfileId`, `Long familyId`, `String status`, `LocalDateTime startedAt`, `LocalDateTime endedAt` (nullable), `Integer durationSeconds` (nullable), `LocalDateTime lastActivityAt`
- [x] Create `session/infrastructure/dto/FamilySessionResponse.java` â€” record: `Long id`, `Long familyId`, `String status`, `LocalDateTime createdAt`, `LocalDateTime expiresAt` (nullable) â€” never includes `tokenHash`

### REST Controllers
- [x] Create `session/infrastructure/web/AuthController.java` â€” `@RestController @RequestMapping("/api/v1/auth")`; inject `FamilyUseCase` (to load family by checking it exists) and `FamilySessionUseCase`:
    - `POST /login` â€” `@RequestBody LoginRequest`; load family via `familyUseCase.getFamily()`; call `familySessionUseCase.authenticate(family.getId(), req.pin())`; return `201 ApiResponse<LoginResponse>` with the raw token; **this is the only endpoint that returns a raw token**
    - `POST /logout` â€” extract raw token from `Authorization: Bearer <token>` header; call `familySessionUseCase.logout(rawToken)`; return `204`
- [x] Create `session/infrastructure/web/ChildSessionController.java` â€” `@RestController @RequestMapping("/api/v1/sessions/children")`; inject `ChildSessionUseCase` and `SessionProperties`:
    - `POST /` â€” open child session; resolve heartbeat interval from request or `SessionProperties` default; extract `connectionMeta` from `HttpServletRequest` (IP + `User-Agent` header); return `201 ApiResponse<ChildSessionResponse>`
    - `GET /` â€” get active sessions for logged-in family (family resolved via token filter); return `200 ApiResponse<List<ChildSessionResponse>>`
    - `DELETE /{id}` â€” close session; return `204`
    - `DELETE /{id}/expel` â€” expel child; return `204`
    - `POST /{id}/heartbeat` â€” record heartbeat; return `204`

### Token Security Filter
- [x] Create `shared/security/TokenAuthenticationFilter.java` â€” extends `OncePerRequestFilter`; extracts `Authorization: Bearer <token>` header; hashes the token via `TokenGenerator.hashToken()`; calls `FamilySessionUseCase.getByToken(rawToken)` to validate; if valid, sets a `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` with `familyId` as principal; if not valid or header absent, continues the filter chain without setting auth (Spring Security's `anyRequest().authenticated()` will reject it)
- [x] Register `TokenAuthenticationFilter` in `SecurityConfig` â€” add `.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` to the `HttpSecurity` builder; add `/api/v1/auth/login` to `permitAll()` list alongside existing public paths

### Scheduled Jobs
- [x] Create `session/infrastructure/scheduler/SessionExpirationJob.java` â€” `@Component`; `@Scheduled(cron = "0 */5 * * * *")`; inject `ChildSessionUseCase` and `SessionProperties`; compute `cutoff = now - (defaultHeartbeatIntervalSeconds * heartbeatGraceMultiplier) seconds`; call `expireInactiveSessions(cutoff)`; log at INFO level: how many sessions expired
- [x] Create `session/infrastructure/scheduler/SessionArchivalJob.java` â€” `@Component`; `@Scheduled(cron = "0 0 2 * * *")`; inject `ChildSessionRepository` and `FamilySessionRepository`; delete or archive sessions with `ended_at < now - retentionDays`; log structured event at INFO; idempotent (safe to run multiple times)
- [x] Add `@EnableScheduling` to `EducationalFrameworkApplication.java`

### Integration Tests
- [x] Create `AuthControllerTest` â€” `@SpringBootTest` + Testcontainers; set up family first; test: POST /login with correct PIN â†’ 201 + token in body; POST /login with wrong PIN â†’ 401; POST /logout with valid token â†’ 204; POST /logout with invalid token â†’ 401; second call to POST /login returns a **different** token
- [x] Create `ChildSessionControllerTest` â€” set up family + child profile; test: POST /sessions/children â†’ 201; second POST for same child closes old session and creates new one; DELETE /{id} â†’ 204 with status=CLOSED; DELETE /{id}/expel â†’ 204 with status=EXPELLED; POST /{id}/heartbeat â†’ 204; GET / returns active sessions

### Contract Updates
- [x] Update `docs/contracts/api/openapi.json` â€” add `/api/v1/auth/login`, `/api/v1/auth/logout`, `/api/v1/sessions/children` paths with request/response schemas; include `LoginResponse` schema noting that `token` is only present on login
- [x] Update `docs/contracts/api/websocket.json` â€” add the STOMP channel configuration (topic/channel names from ADR-009) even if the WebSocket handler itself is deferred to a future sprint

## Risks
- `TokenAuthenticationFilter` must not throw on missing `Authorization` header â€” it should continue the filter chain and let Spring Security reject unauthenticated requests to protected endpoints. Throwing in the filter causes a 500 instead of a 401.
- `ChildSessionController` needs the `familyId` of the authenticated user. The `TokenAuthenticationFilter` sets `familyId` as principal in the `SecurityContextHolder` â€” controllers retrieve it via `(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal()`.
- `SessionArchivalJob` runs nightly and deletes rows â€” verify it handles the case where `ended_at` is null (sessions that never closed) gracefully; exclude ACTIVE sessions from archival.
- `@EnableScheduling` enables all `@Scheduled` beans globally â€” if test profiles run scheduled jobs, they may interfere with test data. Consider a `@ConditionalOnProperty(name = "app.session.scheduling.enabled", matchIfMissing = true)` guard on the scheduler beans.
- `LoginResponse.token` is the only place the raw token appears â€” assert in `AuthControllerTest` that GET /family does NOT return any session token.
- Existing `AbstractIntegrationTest` uses mock session repositories â€” remove those mocks once real adapters are wired in this sprint.

## Dependencies
- Sprint 007 completed: `FamilySessionService`, `ChildSessionService`, `TokenGenerator`, `SessionProperties`, all ports implemented.
- Sprint 006 completed: migrations 005/006 applied; `spring-boot-starter-websocket` on classpath.
- Sprint 003 completed: `GlobalExceptionHandler` maps `SessionException` â†’ 401 automatically.

## Agent Instruction
- `TokenAuthenticationFilter` must call `filterChain.doFilter(request, response)` in the `finally` block â€” never swallow the chain.
- `AuthController.login` must NOT log the raw token â€” only log `session.id` and `session.familyId`.
- `ChildSessionController` extracts `familyId` from `SecurityContextHolder`, not from the request body.
- Persistence adapters use the same static mapper pattern as the family module â€” `toDomain(entity)` and `toJpa(domain)`.
- `SessionArchivalJob` must be idempotent: running it twice in the same window must not fail or double-delete.
- Integration tests: authenticate before each protected endpoint call; use the token from `LoginResponse` in the `Authorization: Bearer` header of subsequent requests.
- JPA entities extend `BaseEntity` â€” do NOT re-declare `id`, `createdAt`, or `updatedAt` (inherited); migration 007 already added `updated_at` to both tables.
- Status columns are stored as `VARCHAR` strings in the DB; mappers convert between `FamilySessionStatus`/`ChildSessionStatus` enum and their `.name()` string.
- After all tasks, update both contract files and mark sprint as `completed`.

## Notes
### Package layout after this sprint
```
session/
  infrastructure/
    persistence/
      FamilySessionJpaEntity.java
      ChildSessionJpaEntity.java
      FamilySessionJpaRepository.java
      ChildSessionJpaRepository.java
      FamilySessionPersistenceAdapter.java
      ChildSessionPersistenceAdapter.java
    web/
      AuthController.java
      ChildSessionController.java
    dto/
      LoginRequest.java
      LoginResponse.java
      LogoutRequest.java
      OpenChildSessionRequest.java
      ChildSessionResponse.java
      FamilySessionResponse.java
    scheduler/
      SessionExpirationJob.java
      SessionArchivalJob.java
shared/
  security/
    TokenAuthenticationFilter.java
```

### SecurityConfig public paths after this sprint
```
/actuator/health
/v3/api-docs/**
/swagger-ui/**
/api/v1/family/**
/api/v1/auth/login
```
All other requests require a valid `Authorization: Bearer <token>` header.

## Review

completed_tasks:
- Added session JPA entities, Spring Data repositories, and persistence adapters for family and child sessions.
- Added corrective migration `008__add_created_at_to_child_session.xml` because `ChildSessionJpaEntity` extends `BaseEntity` and requires `created_at`.
- Added auth and child session DTOs plus REST controllers for login, logout, child session open/list/close/expel/heartbeat.
- Added `TokenAuthenticationFilter` and wired it before `UsernamePasswordAuthenticationFilter`; only `/api/v1/auth/login` remains public under auth.
- Added scheduled expiration and archival jobs with `app.session.scheduling.enabled` guard and enabled scheduling on the application.
- Updated `docs/contracts/api/openapi.json` with auth/session endpoints and bearer security scheme.
- Updated `docs/contracts/api/websocket.json` with baseline STOMP/native WebSocket channels.
- Added integration test classes for auth and child session controllers.
incomplete_tasks:
- None.
contract_changes:
- `openapi.json`: added `/api/v1/auth/login`, `/api/v1/auth/logout`, `/api/v1/sessions/children`, child session actions, request/response schemas, and bearer auth.
- `websocket.json`: added `/ws/parent`, `/topic/family/{familyId}/sessions`, and `/ws/game` channel baseline.
learnings:
- `child_session` needed `created_at` in addition to Sprint 007 `updated_at` because the Sprint 008 JPA entity inherits `BaseEntity`.
- Integration tests compile and are ready, but Testcontainers tests are skipped in this environment because Docker is unavailable.
- The token filter must clear context and continue the chain for invalid/missing tokens so Spring Security returns 401 cleanly.
next_sprint_suggestions:
- Add real WebSocket handlers and constants for event names so clients and server stop using raw strings.
- Run the full Testcontainers integration suite with Docker available before merging.
