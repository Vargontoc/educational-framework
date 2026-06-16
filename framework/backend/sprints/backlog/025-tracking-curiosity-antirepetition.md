# Sprint 025 - backend
# -----------------------------------------------

## Goal
Implement curiosity anti-repetition tracking by child, topic, curiosity, cycle number, and viewed timestamp.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [ ] Create `RegisterCuriosityViewedUseCase`.
- [ ] Create `GetViewedCuriositiesUseCase`.
- [ ] Create `ResetCuriosityCycleUseCase`.
- [ ] Create `CuriosityViewedRepository`.
- [ ] Implement service methods for registering and querying viewed curiosities.
- [ ] Store explicit `viewedAt` independently from audit fields.
- [ ] Keep `cycleNumber` as part of anti-repetition logic.

### Persistence
- [ ] Create `CuriosityViewedJpaEntity`.
- [ ] Create Spring Data repository.
- [ ] Create persistence adapter.
- [ ] Enforce no duplicate `childProfileId`, `topicId`, `curiosityId`, and `cycleNumber` rows.

### Tests
- [ ] Unit test curiosity view registration.
- [ ] Unit test duplicate curiosity in same cycle is rejected or ignored consistently.
- [ ] Unit test viewed curiosities are filtered by child and topic.
- [ ] Unit test reset advances or creates the next cycle.
- [ ] Unit test `viewedAt` is preserved.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Reset behavior can become ambiguous if cycle advancement is not deterministic.
- Using only audit timestamps would make future statistics less explicit.

## Dependencies
- Sprint 021 completed.

## Agent Instruction
- Do not implement content topic selection in this sprint.
- Do not add REST endpoints unless explicitly needed by a later consumer.
- Keep this as internal business logic.

## Notes
Future content/game flows will use this to avoid repeating curiosities until a cycle is complete.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
