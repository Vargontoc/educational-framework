# Sprint 036 - backend
# -----------------------------------------------

## Goal
Create the pure game domain model required by the FEAT-007 shell without WebSocket, tracking, avatar, or persistence behavior.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create game package following backend hexagonal structure.
- [ ] Add `GameStatus` enum: `WAITING`, `STARTING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`.
- [ ] Add normalized `ActionResultType`: `CORRECT`, `INCORRECT`, `TIMEOUT`.
- [ ] Add flexible progress metric model: `SCORE`, `PERCENTAGE`, `STREAK`, `STARS`.
- [ ] Add `GameState` model with `gameId`, `childSessionId`, `activityId`, `difficultyLevelId`, status, score/metric, attempts, timestamps, `sequenceNumber`, `systemEventPending`, and engine payload.
- [ ] Add transient `GameSession` accumulator model.
- [ ] Add `ActionResult` model with result type, response time, recommended avatar event type, new state, completion flag, and attempt context.

### Age 3-4 Rules
- [ ] Add domain-level documentation or tests for no hard failure states.
- [ ] Ensure there is no `PAUSED` status.
- [ ] Ensure `TIMEOUT` can be represented without marking the whole game failed.

### Tests
- [ ] Unit test allowed status enum does not include `PAUSED`.
- [ ] Unit test `GameState` can represent sequence and system event fields.
- [ ] Unit test progress metric variants serialize or map cleanly if DTO mapping exists.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
