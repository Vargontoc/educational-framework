# Sprint 022 - backend
# -----------------------------------------------

## Goal
Implement internal activity attempt registration without summaries, adaptive difficulty, REST endpoints, WebSocket events, or game integration.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Service
- [ ] Create `RegisterActivityAttemptUseCase` under `tracking/ports/in`.
- [ ] Create `ActivityAttemptRepository` under `tracking/ports/out`.
- [ ] Create `ActivityAttemptService` under `tracking/service`.
- [ ] Validate required fields: `childProfileId`, `activityId`, `childSessionId`, `topicId`, `difficultyLevelId`, and `result`.
- [ ] Accept optional `responseTimeMs`.
- [ ] Accept optional `attemptContext` as JSON/text.
- [ ] Return the persisted attempt or a small result object with the attempt ID.

### Persistence
- [ ] Create `ActivityAttemptJpaEntity`.
- [ ] Create Spring Data repository for attempts.
- [ ] Create persistence adapter implementing `ActivityAttemptRepository`.
- [ ] Map domain model to JPA entity and back.

### Configuration
- [ ] Register the service bean in the backend configuration style used by existing modules.
- [ ] Keep the use case internal; do not expose a REST endpoint.

### Tests
- [ ] Unit test successful attempt registration.
- [ ] Unit test missing `childProfileId` is rejected.
- [ ] Unit test missing `activityId` is rejected.
- [ ] Unit test missing `result` is rejected.
- [ ] Unit test `attemptContext` is accepted and preserved.
- [ ] Persistence adapter test if the module has an existing pattern for persistence tests.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
