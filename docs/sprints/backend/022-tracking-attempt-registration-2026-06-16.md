# Sprint 022 - backend
# -----------------------------------------------

## Goal
Implement internal activity attempt registration without summaries, adaptive difficulty, REST endpoints, WebSocket events, or game integration.

## Status
status: completed
started_at: 2026-06-16
closed_at: 2026-06-16
blocked_by:
waiting_for:

## Tasks

### Ports And Service
- [x] Create `RegisterActivityAttemptUseCase` under `tracking/ports/in`.
- [x] Create `ActivityAttemptRepository` under `tracking/ports/out`.
- [x] Create `ActivityAttemptService` under `tracking/service`.
- [x] Validate required fields: `childProfileId`, `activityId`, `childSessionId`, `topicId`, `difficultyLevelId`, and `result`.
- [x] Accept optional `responseTimeMs`.
- [x] Accept optional `attemptContext` as JSON/text.
- [x] Return the persisted attempt or a small result object with the attempt ID.

### Persistence
- [x] Create `ActivityAttemptJpaEntity`.
- [x] Create Spring Data repository for attempts.
- [x] Create persistence adapter implementing `ActivityAttemptRepository`.
- [x] Map domain model to JPA entity and back.

### Configuration
- [x] Register the service bean in the backend configuration style used by existing modules.
- [x] Keep the use case internal; do not expose a REST endpoint.

### Tests
- [x] Unit test successful attempt registration.
- [x] Unit test missing `childProfileId` is rejected.
- [x] Unit test missing `activityId` is rejected.
- [x] Unit test missing `result` is rejected.
- [x] Unit test `attemptContext` is accepted and preserved.
- [x] Persistence adapter test if the module has an existing pattern for persistence tests.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Adding a public registration endpoint would expose runtime-only operations too early.
- Over-validating content/session existence here could make the first sprint too large.

## Dependencies
- Sprint 021 completed.

## Agent Instruction
- Keep this sprint limited to registering `ActivityAttempt`.
- Do not update `ActivitySummary` or `TopicSummary` yet.
- Do not implement adaptive difficulty.
- Do not add dashboard APIs.
- Do not emit WebSocket events.

## Notes
Future game engines will call this use case internally when they exist.

## Review

completed_tasks:
- Created `RegisterActivityAttemptUseCase` port interface
- Created `ActivityAttemptRepository` port interface
- Created `ActivityAttemptService` implementing the use case
- Created `ActivityAttemptValidator` for input validation (required fields)
- Created `ActivityAttemptJpaEntity` extending BaseEntity
- Created `ActivityAttemptJpaRepository` (Spring Data)
- Created `ActivityAttemptPersistenceAdapter` with toDomain/toJpa mappers
- Created `TrackingModuleConfiguration` for DI wiring
- Created `AttemptRegistrationResult` record for return value
- Created `ActivityAttemptServiceTest` with 5 unit tests
- Created `ActivityAttemptPersistenceAdapterTest` with 2 unit tests

incomplete_tasks:
- None

contract_changes:
- None (internal use case, no REST endpoint)

learnings:
- Used `AttemptRegistrationResult` record to return attempt ID and timestamp
- JPA auditing via BaseEntity handles createdAt/updatedAt automatically
- AttemptContext stored as TEXT column

next_sprint_suggestions:
- Implement curiosity viewing registration (CuriosityViewed)
- Implement topic summary updates after activity attempts
- Implement achievement detection after attempts
- Implement learning path progress tracking
