# Sprint 024 - backend
# -----------------------------------------------

## Goal
Implement adaptive difficulty evaluation after attempt registration using accuracy, response speed, timeout penalties, minimum attempts, and cooldown rules.

## Status
status: active
started_at: 2026-06-17
closed_at:
blocked_by:
waiting_for:

## Tasks

### Service
- [ ] Create adaptive difficulty service inside the tracking module.
- [ ] Read recent attempts by child and activity using the configured sliding window.
- [ ] Read current difficulty from `ActivitySummary.currentDifficultyLevelId`.
- [ ] Read `DifficultyLevel.adaptiveThresholdConfig` from content through a port, not direct persistence access.
- [ ] Calculate accuracy score.
- [ ] Calculate response speed score against `targetResponseTimeMs`.
- [ ] Calculate timeout penalty using `timeoutRateThresholdPercent` and `timeoutPenaltyWeight`.
- [ ] Calculate internal `adaptiveScore` from 0 to 100.
- [ ] Respect `minAttemptsBeforeChange`.
- [ ] Respect `cooldownAttempts`.
- [ ] Return an adaptive result object without emitting WebSocket events.

### Configuration Keys
- [ ] Support `slidingWindowAttempts`.
- [ ] Support `increaseThresholdPercent`.
- [ ] Support `decreaseThresholdPercent`.
- [ ] Support `minAttemptsBeforeChange`.
- [ ] Support `cooldownAttempts`.
- [ ] Support `targetResponseTimeMs`.
- [ ] Support `timeoutRateThresholdPercent`.
- [ ] Support `timeoutPenaltyWeight`.
- [ ] Support `accuracyWeight`.
- [ ] Support `speedWeight`.

### Tests
- [ ] Unit test no change when there are fewer than minimum attempts.
- [ ] Unit test difficulty increases with high accuracy and fast response time.
- [ ] Unit test difficulty is maintained with high accuracy but slow response time.
- [ ] Unit test difficulty decreases with low accuracy.
- [ ] Unit test difficulty decreases with frequent timeouts.
- [ ] Unit test cooldown prevents repeated changes.
- [ ] Unit test malformed config fails with a clear validation exception.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
