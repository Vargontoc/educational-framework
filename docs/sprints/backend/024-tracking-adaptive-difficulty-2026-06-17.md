# Sprint 024 - backend
# -----------------------------------------------

## Goal
Implement adaptive difficulty evaluation after attempt registration using accuracy, response speed, timeout penalties, minimum attempts, and cooldown rules.

## Status
status: completed
started_at: 2026-06-17
closed_at: 2026-06-17
blocked_by:
waiting_for:

## Tasks

### Service
- [x] Create adaptive difficulty service inside the tracking module.
- [x] Read recent attempts by child and activity using the configured sliding window.
- [x] Read current difficulty from `ActivitySummary.currentDifficultyLevelId`.
- [x] Read `DifficultyLevel.adaptiveThresholdConfig` from content through a port, not direct persistence access.
- [x] Calculate accuracy score.
- [x] Calculate response speed score against `targetResponseTimeMs`.
- [x] Calculate timeout penalty using `timeoutRateThresholdPercent` and `timeoutPenaltyWeight`.
- [x] Calculate internal `adaptiveScore` from 0 to 100.
- [x] Respect `minAttemptsBeforeChange`.
- [x] Respect `cooldownAttempts`.
- [x] Return an adaptive result object without emitting WebSocket events.

### Configuration Keys
- [x] Support `slidingWindowAttempts`.
- [x] Support `increaseThresholdPercent`.
- [x] Support `decreaseThresholdPercent`.
- [x] Support `minAttemptsBeforeChange`.
- [x] Support `cooldownAttempts`.
- [x] Support `targetResponseTimeMs`.
- [x] Support `timeoutRateThresholdPercent`.
- [x] Support `timeoutPenaltyWeight`.
- [x] Support `accuracyWeight`.
- [x] Support `speedWeight`.

### Tests
- [x] Unit test no change when there are fewer than minimum attempts.
- [x] Unit test difficulty increases with high accuracy and fast response time.
- [x] Unit test difficulty is maintained with high accuracy but slow response time.
- [x] Unit test difficulty decreases with low accuracy.
- [x] Unit test difficulty decreases with frequent timeouts.
- [x] Unit test cooldown prevents repeated changes.
- [x] Unit test malformed config fails with a clear validation exception.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Direct content persistence access would violate module boundaries.
- Too much scoring complexity would make this hard for a junior to validate.
- Difficulty changes must not be emitted over WebSocket by tracking.

## Dependencies
- Sprint 023 completed.
- Content difficulty levels expose `adaptiveThresholdConfig`.

## Agent Instruction
- Keep the algorithm deterministic and easy to test.
- Do not implement game orchestration.
- Do not emit `GAME_DIFFICULTY_CHANGED`.
- Do not add dashboard APIs in this sprint.

## Notes
The future game module will call this service and decide whether to emit WebSocket events.

## Review

completed_tasks:
- Created AdaptiveDifficultyAction enum (INCREASE/DECREASE/MAINTAIN)
- Created AdaptiveDifficultyResult record
- Created DifficultyLevelConfigPort interface
- Created DifficultyLevelConfigPortImpl adapter (uses content's DifficultyLevelRepository)
- Created AdaptiveDifficultyProperties with 10 configuration keys
- Created AdaptiveDifficultyService with core algorithm
- Modified ActivityAttemptRepository to add findRecentByChildAndActivity
- Modified ActivityAttemptJpaRepository with derived query
- Modified ActivityAttemptPersistenceAdapter to implement findRecentByChildAndActivity
- Modified ActivityAttemptService to call AdaptiveDifficultyService.evaluate()
- Modified TrackingModuleConfiguration with new beans
- Modified ActivitySummary to add attemptsSinceLastDifficultyChange field
- Modified ActivitySummaryJpaEntity with attemptsSinceLastDifficultyChange column
- Modified ActivitySummaryPersistenceAdapter to map attemptsSinceLastDifficultyChange
- Modified SummaryUpdateService to increment attemptsSinceLastDifficultyChange
- Updated application.yml with app.tracking.adaptive-difficulty.* keys
- Created 7 unit tests for AdaptiveDifficultyService (all passing)
- Updated ActivityAttemptServiceTest to mock AdaptiveDifficultyService
- All 25 tracking tests pass

incomplete_tasks:
- None

contract_changes:
- ActivitySummary: added attemptsSinceLastDifficultyChange field
- ActivityAttemptRepository: added findRecentByChildAndActivity method

learnings:
- Used simple string parsing for JSON config instead of adding Gson dependency
- List.of() returns immutable lists - must use new ArrayList<>(List.of()) for mutable lists in tests
- Mockito any() for int primitive requires anyInt() not any()

next_sprint_suggestions:
- Implement curiosity viewing registration (CuriosityViewed)
- Implement achievement detection after attempts
- Implement learning path progress tracking
- Implement topic selection optimization based on performance bands
