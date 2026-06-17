# Sprint 025 - backend
# -----------------------------------------------

## Goal
Implement curiosity anti-repetition tracking by child, topic, curiosity, cycle number, and viewed timestamp.

## Status
status: completed
started_at: 2026-06-17
closed_at: 2026-06-17
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [x] Create `RegisterCuriosityViewedUseCase`.
- [x] Create `GetViewedCuriositiesUseCase`.
- [x] Create `ResetCuriosityCycleUseCase`.
- [x] Create `CuriosityViewedRepository`.
- [x] Implement service methods for registering and querying viewed curiosities.
- [x] Store explicit `viewedAt` independently from audit fields.
- [x] Keep `cycleNumber` as part of anti-repetition logic.

### Persistence
- [x] Create `CuriosityViewedJpaEntity`.
- [x] Create Spring Data repository.
- [x] Create persistence adapter.
- [x] Enforce no duplicate `childProfileId`, `topicId`, `curiosityId`, and `cycleNumber` rows.

### Tests
- [x] Unit test curiosity view registration.
- [x] Unit test duplicate curiosity in same cycle is rejected or ignored consistently.
- [x] Unit test viewed curiosities are filtered by child and topic.
- [x] Unit test reset advances or creates the next cycle.
- [x] Unit test `viewedAt` is preserved.

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
- Created CuriosityViewed domain model
- Created RegisterCuriosityViewedUseCase inbound port
- Created GetViewedCuriositiesUseCase inbound port
- Created ResetCuriosityCycleUseCase inbound port
- Created CuriosityViewedRepository outbound port
- Created CuriosityViewedJpaEntity with unique constraint on (childProfileId, topicId, curiosityId, cycleNumber)
- Created CuriosityViewedJpaRepository with Spring Data queries
- Created CuriosityViewedPersistenceAdapter with toDomain/toJpa mappers
- Created CuriosityViewedService implementing all 3 use cases
- Updated TrackingModuleConfiguration with new beans
- Created 7 unit tests (all passing):
  - registerView_createsRecord
  - registerView_duplicateInSameCycle_ignored
  - getViewedCuriosities_filteredByChildAndTopic
  - resetCycle_returnsNextNumber
  - resetCycle_empty_returnsZero
  - viewedAt_preservedIndependently
  - getViewedCuriosities_withCycleNumber

incomplete_tasks:
- None

contract_changes:
- Created curiosity_viewed table with unique constraint

learnings:
- Cycle starts at 0, resetCycle returns max+1 (next cycle number)
- registerView is idempotent - duplicates in same cycle are ignored
- viewedAt is explicit LocalDateTime, not from BaseEntity audit fields

next_sprint_suggestions:
- Implement achievement detection after attempts
- Implement learning path progress tracking
- Implement topic selection optimization based on performance bands
- Implement dashboard read API for tracking data
