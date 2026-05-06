# Sprint 008 - backend
# -----------------------------------------------

## Goal
Complete FEAT-002 (Session Module) REST layer: JPA entities, persistence adapters, DTOs, REST controllers for auth and child session management, opaque token security filter, scheduled jobs for session expiration and archival, and integration tests.

## Status
status: active
started_at: 2026-05-06 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### JPA Entities
- [ ] Create `session/infrastructure/persistence/FamilySessionJpaEntity.java` — `@Entity @Table(name = "family_session")`, extends `BaseEntity`; fields: `tokenHash` (`@Column(name = "token_hash", unique = true)`), `tokenType`, `familyId` (`@Column(name = "family_id")`), `expiresAt`, `revoked`, `createdByIp` (`@Column(name = "created_by_ip")`), `deviceId` (`@Column(name = "device_id")`), `status` (String — map from `FamilySessionStatus` enum as string)
- [ ] Create `session/infrastructure/persistence/ChildSessionJpaEntity.java` — `@Entity @Table(name = "child_session")`, extends `BaseEntity`; fields: `childProfileId` (`@Column(name = "child_profile_id")`), `familyId`, `startedAt`, `endedAt`, `durationSeconds`, `status`, `lastActivityAt` (`@Column(name = "last_activity_at")`), `heartbeatIntervalSeconds` (`@Column(name = "heartbeat_interval_seconds")`), `connectionMeta` (`@Column(name = "connection_meta", columnDefinition = "TEXT")`), `persistedGameStateRef`

### Spring Data Repositories
- [ ] Create `session/infrastructure/persistence/FamilySessionJpaRepository.java` — `JpaRepository<FamilySessionJpaEntity, Long>` with:
    - `Optional<FamilySessionJpaEntity> findByTokenHash(String tokenHash)`
    - `List<FamilySessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status)`
- [ ] Create `session/infrastructure/persistence/ChildSessionJpaRepository.java` — `JpaRepository<ChildSessionJpaEntity, Long>` with:
    - `Optional<ChildSessionJpaEntity> findByChildProfileIdAndStatus(Long childProfileId, String status)`
    - `List<ChildSessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status)`
    - `List<ChildSessionJpaEntity> findByStatusAndLastActivityAtBefore(String status, LocalDateTime cutoff)`

### Persistence Adapters
- [ ] Create `session/infrastructure/persistence/FamilySessionPersistenceAdapter.java` — `@Repository`, implements `FamilySessionRepository`; static `toDomain` / `toJpa` mappers; `findActiveByFamilyId` passes `"ACTIVE"` as status string; `saveAll` calls `jpaRepository.saveAll()` then maps back to domain
- [ ] Create `session/infrastructure/persistence/ChildSessionPersistenceAdapter.java` — `@Repository`, implements `ChildSessionRepository`; `findActiveByChildProfileId` passes `"ACTIVE"`; `findExpirableSessions(cutoff)` calls `findByStatusAndLastActivityAtBefore("ACTIVE", cutoff)`

### DTOs
- [ ] Create `session/infrastructure/dto/LoginRequest.java` — record: `String pin`
- [ ] Create `session/infrastructure/dto/LoginResponse.java` — record: `String token`, `Long sessionId`, `Long familyId`, `LocalDateTime createdAt` — `token` is the raw opaque token; this is the **only** response that ever contains it
- [ ] Create `session/infrastructure/dto/LogoutRequest.java` — record: `String token` (optional — if null, token is taken from `Authorization` header in controller)
- [ ] Create `session/infrastructure/dto/OpenChildSessionRequest.java` — record: `Long childProfileId`, `Integer heartbeatIntervalSeconds` (nullable — defaults to `SessionProperties.defaultHeartbeatIntervalSeconds`)
- [ ] Create `session/infrastructure/dto/ChildSessionResponse.java` — record: `Long id`, `Long childProfileId`, `Long familyId`, `String status`, `LocalDateTime startedAt`, `LocalDateTime endedAt` (nullable), `Integer durationSeconds` (nullable), `LocalDateTime lastActivityAt`
- [ ] Create `session/infrastructure/dto/FamilySessionResponse.java` — record: `Long id`, `Long familyId`, `String status`, `LocalDateTime createdAt`, `LocalDateTime expiresAt` (nullable) — never includes `tokenHash`

### REST Controllers
- [ ] Create `session/infrastructure/web/AuthController.java` — `@RestController @RequestMapping("/api/v1/auth")`; inject `FamilyUseCase` (to load family by checking it exists) and `FamilySessionUseCase`:
    - `POST /login` — `@RequestBody LoginRequest`; load family via `familyUseCase.getFamily()`; call `familySessionUseCase.authenticate(family.getId(), req.pin())`; return `201 ApiResponse<LoginResponse>` with the raw token; **this is the only endpoint that returns a raw token**
    - `POST /logout` — extract raw token from `Authorization: Bearer <token>` header; call `familySessionUseCase.logout(rawToken)`; return `204`
- [ ] Create `session/infrastructure/web/ChildSessionController.java` — `@RestController @RequestMapping("/api/v1/sessions/children")`; inject `ChildSessionUseCase` and `SessionProperties`:
    - `POST /` — open child session; resolve heartbeat interval from request or `SessionProperties` default; extract `connectionMeta` from `HttpServletRequest` (IP + `User-Agent` header); return `201 ApiResponse<ChildSessionResponse>`
    - `GET /` — get active sessions for logged-in family (family resolved via token filter); return `200 ApiResponse<List<ChildSessionResponse>>`
    - `DELETE /{id}` — close session; return `204`
    - `DELETE /{id}/expel` — expel child; return `204`
    - `POST /{id}/heartbeat` — record heartbeat; return `204`

### Token Security Filter
- [ ] Create `shared/security/TokenAuthenticationFilter.java` — extends `OncePerRequestFilter`; extracts `Authorization: Bearer <token>` header; hashes the token via `TokenGenerator.hashToken()`; calls `FamilySessionUseCase.getByToken(rawToken)` to validate; if valid, sets a `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` with `familyId` as principal; if not valid or header absent, continues the filter chain without setting auth (Spring Security's `anyRequest().authenticated()` will reject it)
- [ ] Register `TokenAuthenticationFilter` in `SecurityConfig` — add `.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` to the `HttpSecurity` builder; add `/api/v1/auth/login` to `permitAll()` list alongside existing public paths

### Scheduled Jobs
- [ ] Create `session/infrastructure/scheduler/SessionExpirationJob.java` — `@Component`; `@Scheduled(cron = "0 */5 * * * *")`; inject `ChildSessionUseCase` and `SessionProperties`; compute `cutoff = now - (defaultHeartbeatIntervalSeconds * heartbeatGraceMultiplier) seconds`; call `expireInactiveSessions(cutoff)`; log at INFO level: how many sessions expired
- [ ] Create `session/infrastructure/scheduler/SessionArchivalJob.java` — `@Component`; `@Scheduled(cron = "0 0 2 * * *")`; inject `ChildSessionRepository` and `FamilySessionRepository`; delete or archive sessions with `ended_at < now - retentionDays`; log structured event at INFO; idempotent (safe to run multiple times)
- [ ] Add `@EnableScheduling` to `EducationalFrameworkApplication.java`

### Integration Tests
- [ ] Create `AuthControllerTest` — `@SpringBootTest` + Testcontainers; set up family first; test: POST /login with correct PIN → 201 + token in body; POST /login with wrong PIN → 401; POST /logout with valid token → 204; POST /logout with invalid token → 401; second call to POST /login returns a **different** token
- [ ] Create `ChildSessionControllerTest` — set up family + child profile; test: POST /sessions/children → 201; second POST for same child closes old session and creates new one; DELETE /{id} → 204 with status=CLOSED; DELETE /{id}/expel → 204 with status=EXPELLED; POST /{id}/heartbeat → 204; GET / returns active sessions

### Contract Updates
- [ ] Update `docs/contracts/api/openapi.json` — add `/api/v1/auth/login`, `/api/v1/auth/logout`, `/api/v1/sessions/children` paths with request/response schemas; include `LoginResponse` schema noting that `token` is only present on login
- [ ] Update `docs/contracts/api/websocket.json` — add the STOMP channel configuration (topic/channel names from ADR-009) even if the WebSocket handler itself is deferred to a future sprint

## Risks
- `TokenAuthenticationFilter` must not throw on missing `Authorization` header — it should continue the filter chain and let Spring Security reject unauthenticated requests to protected endpoints. Throwing in the filter causes a 500 instead of a 401.
- `ChildSessionController` needs the `familyId` of the authenticated user. The `TokenAuthenticationFilter` sets `familyId` as principal in the `SecurityContextHolder` — controllers retrieve it via `(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal()`.
- `SessionArchivalJob` runs nightly and deletes rows — verify it handles the case where `ended_at` is null (sessions that never closed) gracefully; exclude ACTIVE sessions from archival.
- `@EnableScheduling` enables all `@Scheduled` beans globally — if test profiles run scheduled jobs, they may interfere with test data. Consider a `@ConditionalOnProperty(name = "app.session.scheduling.enabled", matchIfMissing = true)` guard on the scheduler beans.
- `LoginResponse.token` is the only place the raw token appears — assert in `AuthControllerTest` that GET /family does NOT return any session token.
- Existing `AbstractIntegrationTest` uses mock session repositories — remove those mocks once real adapters are wired in this sprint.

## Dependencies
- Sprint 007 completed: `FamilySessionService`, `ChildSessionService`, `TokenGenerator`, `SessionProperties`, all ports implemented.
- Sprint 006 completed: migrations 005/006 applied; `spring-boot-starter-websocket` on classpath.
- Sprint 003 completed: `GlobalExceptionHandler` maps `SessionException` → 401 automatically.

## Agent Instruction
- `TokenAuthenticationFilter` must call `filterChain.doFilter(request, response)` in the `finally` block — never swallow the chain.
- `AuthController.login` must NOT log the raw token — only log `session.id` and `session.familyId`.
- `ChildSessionController` extracts `familyId` from `SecurityContextHolder`, not from the request body.
- Persistence adapters use the same static mapper pattern as the family module — `toDomain(entity)` and `toJpa(domain)`.
- `SessionArchivalJob` must be idempotent: running it twice in the same window must not fail or double-delete.
- Integration tests: authenticate before each protected endpoint call; use the token from `LoginResponse` in the `Authorization: Bearer` header of subsequent requests.
- JPA entities extend `BaseEntity` — do NOT re-declare `id`, `createdAt`, or `updatedAt` (inherited); migration 007 already added `updated_at` to both tables.
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
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
