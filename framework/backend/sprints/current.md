# Sprint 007 - backend
# -----------------------------------------------

## Goal
Implement the Session domain layer for FEAT-002: fix the missing `updated_at` migration, pure domain models for FamilySession and ChildSession, hexagonal ports, token generation utility, services (PIN auth, session lifecycle, single-session enforcement, heartbeat, revocation), SessionProperties config binding, and full unit test coverage â€” no persistence or HTTP layer.

## Status
status: completed
started_at: 2026-05-06 00:00:00
closed_at: 2026-05-06 00:00:00
blocked_by:
waiting_for:

## Tasks

### Migration fix (deferred from Sprint 006)
- [x] Create `migrations/007__add_updated_at_to_session_tables.xml` â€” add `updated_at` (TIMESTAMPTZ nullable) to both `family_session` and `child_session` tables; `BaseEntity` maps `@LastModifiedDate` to `updated_at` and Hibernate schema validation will fail without it
- [x] Register `007__add_updated_at_to_session_tables.xml` in `db.changelog-master.xml` after `006__create_child_session.xml`

### Token utility
- [x] Create `shared/security/TokenGenerator.java` â€” plain utility class, static methods only, no `@Component`; `generateRawToken()` â†’ `SecureRandom` 32 bytes â†’ Base64url (43 chars, no padding); `hashToken(String rawToken)` â†’ `MessageDigest("SHA-256")` â†’ lowercase hex (64 chars); must never log or return the raw token from `hashToken`

### Domain Models
- [x] Create `session/model/FamilySessionStatus.java` â€” enum: `ACTIVE, EXPIRED, CLOSED, REVOKED`
- [x] Create `session/model/FamilySession.java` â€” plain Java class; fields: `Long id`, `String tokenHash`, `String tokenType` (default `"opaque"`), `Long familyId`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`, `LocalDateTime expiresAt` (nullable), `boolean revoked`, `String createdByIp`, `String deviceId`, `FamilySessionStatus status`
- [x] Create `session/model/FamilySessionResult.java` â€” Java record: `String rawToken`, `FamilySession session` â€” sole transient holder of the raw token; never persisted or logged
- [x] Create `session/model/ChildSessionStatus.java` â€” enum: `ACTIVE, EXPIRED, EXPELLED, CLOSED`
- [x] Create `session/model/ChildSession.java` â€” plain Java class; fields: `Long id`, `Long childProfileId`, `Long familyId`, `LocalDateTime startedAt`, `LocalDateTime endedAt` (nullable), `Integer durationSeconds` (nullable â€” computed at close), `ChildSessionStatus status`, `LocalDateTime lastActivityAt`, `int heartbeatIntervalSeconds`, `String connectionMeta` (JSON string, nullable), `String persistedGameStateRef` (nullable)

### Ports â€” In (Use Cases)
- [x] Create `session/ports/in/FamilySessionUseCase.java` â€” interface:
    - `FamilySessionResult authenticate(Long familyId, String rawPin)`
    - `void logout(String rawToken)`
    - `void revokeAllByFamily(Long familyId)`
    - `FamilySession getByToken(String rawToken)` â€” throws `SessionException` if not found or revoked
- [x] Create `session/ports/in/ChildSessionUseCase.java` â€” interface:
    - `ChildSession openSession(Long childProfileId, Long familyId, int heartbeatInterval, String connectionMeta)`
    - `ChildSession closeSession(Long id)`
    - `ChildSession expelChild(Long id)`
    - `void recordHeartbeat(Long id)` â€” throws `SessionException` if session not ACTIVE
    - `List<ChildSession> getActiveSessions(Long familyId)`
    - `void expireInactiveSessions(LocalDateTime cutoff)`

### Ports â€” Out (Repository Interfaces)
- [x] Create `session/ports/out/FamilySessionRepository.java` â€” interface:
    - `Optional<FamilySession> findByTokenHash(String tokenHash)`
    - `List<FamilySession> findActiveByFamilyId(Long familyId)`
    - `FamilySession save(FamilySession session)`
    - `void saveAll(List<FamilySession> sessions)`
- [x] Create `session/ports/out/ChildSessionRepository.java` â€” interface:
    - `Optional<ChildSession> findById(Long id)`
    - `Optional<ChildSession> findActiveByChildProfileId(Long childProfileId)`
    - `List<ChildSession> findActiveByFamilyId(Long familyId)`
    - `List<ChildSession> findExpirableSessions(LocalDateTime cutoff)`
    - `ChildSession save(ChildSession session)`
    - `void saveAll(List<ChildSession> sessions)`

### Services
- [x] Create `session/service/FamilySessionService.java` â€” `@Service @Transactional`, implements `FamilySessionUseCase`:
    - `authenticate(familyId, rawPin)`: load `Family` via `FamilyRepository.findFamily()` (throw `ResourceNotFoundException` if missing); `BCryptPasswordEncoder.matches(rawPin, family.getPinHash())` â€” throw `SessionException("Invalid PIN")` on mismatch; generate `rawToken`, hash it, build `FamilySession` (status=ACTIVE, revoked=false, createdAt=now), save, return `new FamilySessionResult(rawToken, session)`
    - `logout(rawToken)`: hash token, `findByTokenHash` (throw `SessionException` if empty or revoked), set `status=REVOKED`, `revoked=true`, `updatedAt=now`, save
    - `revokeAllByFamily(familyId)`: `findActiveByFamilyId`, set each to `REVOKED` + `revoked=true` + `updatedAt=now`, `saveAll`
    - `getByToken(rawToken)`: hash, find, throw `SessionException` if not found or `revoked=true`, return session
- [x] Create `session/service/ChildSessionService.java` â€” `@Service @Transactional`, implements `ChildSessionUseCase`:
    - `openSession(childProfileId, familyId, heartbeatInterval, connectionMeta)`: `findActiveByChildProfileId` â€” if present, close it (`durationSeconds = seconds(startedAt, now)`, `endedAt=now`, `status=CLOSED`, save) in same transaction; create new session (`status=ACTIVE`, `startedAt=now`, `lastActivityAt=now`), save
    - `closeSession(id)`: find by id (throw `ResourceNotFoundException` if missing), compute `durationSeconds`, set `endedAt=now`, `status=CLOSED`, save
    - `expelChild(id)`: find, compute duration, `endedAt=now`, `status=EXPELLED`, save
    - `recordHeartbeat(id)`: find, throw `SessionException` if `status != ACTIVE`, set `lastActivityAt=now`, save
    - `getActiveSessions(familyId)`: delegate to `findActiveByFamilyId`
    - `expireInactiveSessions(cutoff)`: `findExpirableSessions(cutoff)`, set each to `EXPIRED`, `saveAll`

### Application config binding
- [x] Create `shared/config/SessionProperties.java` â€” `@ConfigurationProperties(prefix = "app.session")` + `@Component`; fields: `int defaultHeartbeatIntervalSeconds` (default 30), `int heartbeatGraceMultiplier` (default 2), `int retentionDays` (default 30)

### Unit Tests
- [x] Unit test: `TokenGeneratorTest` â€” `generateRawToken()` returns 43-char string; `hashToken()` returns 64-char lowercase hex; two consecutive calls to `generateRawToken()` differ; `hashToken(hashToken(x))` â‰  `hashToken(x)` (non-idempotent â€” raw token and its hash produce different hashes)
- [x] Unit test: `FamilySessionServiceTest` â€” mock `FamilyRepository` + `FamilySessionRepository`; test: authenticate happy path returns non-null rawToken; authenticate wrong PIN â†’ `SessionException`; authenticate no family â†’ `ResourceNotFoundException`; logout valid token â†’ session `REVOKED`; logout already-revoked token â†’ `SessionException`; `revokeAllByFamily` marks all active sessions `REVOKED`
- [x] Unit test: `ChildSessionServiceTest` â€” mock `ChildSessionRepository`; test: `openSession` no prior session â†’ new ACTIVE session; `openSession` with prior ACTIVE â†’ prior CLOSED, new ACTIVE; `closeSession` computes positive `durationSeconds`; `expelChild` â†’ `EXPELLED`; `recordHeartbeat` updates `lastActivityAt`; `recordHeartbeat` on non-ACTIVE â†’ `SessionException`; `expireInactiveSessions` marks all results EXPIRED

## Risks
- `007__add_updated_at_to_session_tables.xml` must be applied before Sprint 008 creates JPA entities that extend `BaseEntity`; skipping it causes immediate `SchemaManagementException` on app startup.
- `TokenGenerator.hashToken` uses `MessageDigest` which is not thread-safe when shared as an instance â€” use a new instance per call or use the static factory `MessageDigest.getInstance("SHA-256")` inside the method body.
- `FamilySessionService.authenticate` uses `BCryptPasswordEncoder.matches` â€” the encoder is instantiated inline (`new BCryptPasswordEncoder()`) as in `FamilyService`; do not declare it as a Bean to avoid touching `SecurityConfig` this sprint.
- `revokeAllByFamily` is `@Transactional` and calls `saveAll` â€” if called from `FamilyService.updateFamily` (PIN change), the outer `@Transactional` propagates, keeping both operations in one transaction.
- `FamilySessionResult.rawToken` must not appear in `toString()` output â€” if using a Java record, override `toString()` to omit the field, or use a plain class.

## Dependencies
- Sprint 006 completed: migrations 005/006 applied; `spring-boot-starter-websocket` on classpath; `app.session.*` in `application.yml`.
- Sprint 002 completed: `SessionException` (HTTP 401), `ResourceNotFoundException` (HTTP 404), `AbstractValidator` available.
- `FamilyRepository` port (Sprint 004) injectable into `FamilySessionService`.

## Agent Instruction
- All session classes go under `es.vargontoc.educational.framework.session`.
- `TokenGenerator` in `es.vargontoc.educational.framework.shared.security` â€” plain static utility, no Spring annotations.
- `SessionProperties` in `es.vargontoc.educational.framework.shared.config`.
- Domain models are pure Java: no `@Entity`, no Spring imports. Enums are standalone classes in `session/model/`.
- Services use constructor injection. Unit tests use `@ExtendWith(MockitoExtension.class)` â€” no Spring context.
- Migration `007__add_updated_at_to_session_tables.xml` uses `<addColumn>` tag (valid in XSD 4.27) â€” do NOT use `addCheckConstraint` (lessons from Sprint 004).
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
Migration fix added to this sprint because:
- `005` and `006` are already applied without `updated_at`.
- Sprint 008 JPA entities extend `BaseEntity` â†’ Hibernate `validate` will fail without the column.
- A corrective migration is the right approach (never modify applied migrations).

### Migration 007 shape
```xml
<changeSet id="007__add_updated_at_to_session_tables" author="vargontoc">
    <addColumn tableName="family_session">
        <column name="updated_at" type="TIMESTAMPTZ"/>
    </addColumn>
    <addColumn tableName="child_session">
        <column name="updated_at" type="TIMESTAMPTZ"/>
    </addColumn>
</changeSet>
```

### Package layout after this sprint
```
session/
  model/
    FamilySession.java
    FamilySessionResult.java
    FamilySessionStatus.java
    ChildSession.java
    ChildSessionStatus.java
  ports/
    in/
      FamilySessionUseCase.java
      ChildSessionUseCase.java
    out/
      FamilySessionRepository.java
      ChildSessionRepository.java
  service/
    FamilySessionService.java
    ChildSessionService.java
shared/
  config/
    SessionProperties.java
  security/
    TokenGenerator.java
```

## Review

completed_tasks:
- Added corrective migration `007__add_updated_at_to_session_tables.xml` and registered it in the master changelog.
- Added pure session domain models, standalone status enums, and raw-token-safe `FamilySessionResult`.
- Added inbound and outbound hexagonal ports for family and child session use cases.
- Added `TokenGenerator` with SecureRandom Base64url raw tokens and SHA-256 lowercase hex hashing.
- Added `FamilySessionService` for PIN authentication, token lookup, logout, and family-wide revocation.
- Added `ChildSessionService` for single-active-session enforcement, close, expel, heartbeat, lookup, and expiration.
- Added `SessionProperties` binding for `app.session` defaults.
- Added unit tests for token generation and both session services.
- Updated Spring context tests with mock session ports until Sprint 008 provides persistence adapters.
incomplete_tasks:
- None.
contract_changes:
- None. No REST or WebSocket endpoints were added or modified in this sprint.
learnings:
- Session services are Spring beans before persistence adapters exist, so Spring context tests need mocked session ports during the domain-only sprint.
- `FamilySessionResult.toString()` intentionally omits the raw token to avoid accidental log exposure.
- Full test run passes; Testcontainers integration tests are skipped when Docker is unavailable.
next_sprint_suggestions:
- Implement Sprint 008 persistence adapters, JPA entities, DTOs, REST controllers, token filter, and integration coverage.
- Replace temporary test-only session repository mocks once real adapters exist.