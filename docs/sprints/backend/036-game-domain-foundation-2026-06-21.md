# Sprint 036 - backend
# -----------------------------------------------

## Goal
Create the pure game domain model required by the FEAT-007 shell without WebSocket, tracking, avatar, or persistence behavior.

## Status
status: closed
started_at: 2026-06-21
closed_at: 2026-06-21
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [x] Create game package following backend hexagonal structure.
- [x] Add `GameStatus` enum: `WAITING`, `STARTING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`.
- [x] Add normalized `ActionResultType`: `CORRECT`, `INCORRECT`, `TIMEOUT`.
- [x] Add flexible progress metric model: `ProgressMetricType`: `SCORE`, `PERCENTAGE`, `STREAK`, `STARS`.
- [x] Add `GameState` model with `gameId`, `childSessionId`, `activityId`, `difficultyLevelId`, status, score/metric, attempts, timestamps, `sequenceNumber`, `systemEventPending`, and engine payload.
- [x] Add transient `GameSession` accumulator model.
- [x] Add `ActionResult` model with result type, response time, recommended avatar event type, new state, completion flag, and attempt context.

### Age 3-4 Rules
- [x] Add domain-level documentation or tests for no hard failure states.
- [x] Ensure there is no `PAUSED` status.
- [x] Ensure `TIMEOUT` can be represented without marking the whole game failed.

### Tests
- [x] Unit test allowed status enum does not include `PAUSED`.
- [x] Unit test `GameState` can represent sequence and system event fields.
- [x] Unit test progress metric variants serialize or map cleanly if DTO mapping exists.
- [x] Unit test `GameSession` can accumulate game states.
- [x] Unit test `GameSession` can track totals.
- [x] Unit test `ActionResult` for CORRECT, INCORRECT, TIMEOUT, and completed scenarios.

## Manual Tests
- Not required. This is a domain-only sprint.

## Risks
- Adding infrastructure too early would make the shell harder for juniors to reason about.
- Over-modeling engine payloads could block concrete motors later.

## Dependencies
- FEAT-007 approved as proposal/source of truth.

## Agent Instruction
- Keep all classes framework-free where possible.
- Do not add JPA entities or migrations for game state.
- Do not call tracking, session, content, avatar, or WebSocket.
- Keep names in English.

## Notes
This sprint establishes the vocabulary for later game shell sprints.

## Review

completed_tasks:
- All 7 domain model tasks completed
- All 3 age 3-4 rules tasks completed
- All 6 test tasks completed
- 586 total tests pass with 0 failures

incomplete_tasks:
- None

contract_changes:
- New `game` module created with hexagonal structure
- `GameStatus` enum: `WAITING`, `STARTING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED` (no PAUSED)
- `ActionResultType` enum: `CORRECT`, `INCORRECT`, `TIMEOUT`
- `ProgressMetricType` enum: `SCORE`, `PERCENTAGE`, `STREAK`, `STARS`
- `GameState` domain model with all required fields
- `GameSession` transient accumulator model
- `ActionResult` domain model

learnings:
- Domain-only sprints can be completed quickly with clear requirements.
- Keeping models framework-free maintains flexibility for future infrastructure.

next_sprint_suggestions:
- Continue with game shell implementation following FEAT-007.
- Consider game engine integration or WebSocket lifecycle management.
