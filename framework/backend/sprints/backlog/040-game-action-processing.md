# Sprint 040 - backend
# -----------------------------------------------

## Goal
Process game actions through the engine, register attempts in tracking, update difficulty, and finalize completed games without WebSocket transport.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Action Processing
- [ ] Add orchestrator method for processing a child action against an active game.
- [ ] Validate the game is `IN_PROGRESS` before processing gameplay actions.
- [ ] Delegate action rules to `GameEngine.processAction(...)`.
- [ ] Call tracking `registerAttempt(...)` with normalized result, optional `topicId`, response time, difficulty, and attempt context.
- [ ] Update `GameState.difficultyLevel` when tracking returns a new difficulty.
- [ ] Prepare an internal output that indicates whether `GAME_DIFFICULTY_CHANGED` should be emitted later.
- [ ] Handle `unlockedAchievements[]` from tracking in the orchestrator response.
- [ ] When complete, call `evaluateGameCompletionAchievements(...)` and `registerGameSessionSummary(...)`.
- [ ] Remove completed state from the registry.

### Tests
- [ ] Unit test correct action calls tracking registration.
- [ ] Unit test difficulty change updates game state.
- [ ] Unit test unlocked achievements are returned to caller.
- [ ] Unit test completed action registers game session summary.
- [ ] Unit test actions are rejected if game is not `IN_PROGRESS`.

## Manual Tests
- Optional: run a dev/test fixture that processes one fake action.
- Confirm the fake tracking adapter receives the expected normalized attempt data.

## Risks
- Coupling to concrete tracking implementation can make unit tests difficult.
- Forgetting to persist the summary on completion would break dashboard recent sessions.

## Dependencies
- Sprint 032 completed.
- Sprint 033 completed.
- Sprint 039 completed.

## Agent Instruction
- Keep this transport-free; do not add WebSocket handling.
- Mock tracking ports in unit tests.
- Do not let engines call tracking directly.

## Notes
This sprint implements the core backend behavior behind `GAME_ACTION`, but not the WebSocket endpoint itself.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
