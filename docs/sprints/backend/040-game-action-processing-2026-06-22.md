# Sprint 040 - backend
# -----------------------------------------------

## Goal
Process game actions through the engine, register attempts in tracking, update difficulty, and finalize completed games without WebSocket transport.

## Status
status: closed
started_at: 2026-06-22
closed_at: 2026-06-22
blocked_by:
waiting_for:

## Tasks

### Action Processing
- [x] Add orchestrator method for processing a child action against an active game.
- [x] Validate the game is `IN_PROGRESS` before processing gameplay actions.
- [x] Delegate action rules to `GameEngine.processAction(...)`.
- [x] Call tracking `registerAttempt(...)` with normalized result, optional `topicId`, response time, difficulty, and attempt context.
- [x] Update `GameState.difficultyLevel` when tracking returns a new difficulty.
- [x] Prepare an internal output that indicates whether `GAME_DIFFICULTY_CHANGED` should be emitted later.
- [x] Handle `unlockedAchievements[]` from tracking in the orchestrator response.
- [x] When complete, call `evaluateGameCompletionAchievements(...)` and `registerGameSessionSummary(...)`.
- [x] Remove completed state from the registry.

### Tests
- [x] Unit test correct action calls tracking registration.
- [x] Unit test difficulty change updates game state.
- [x] Unit test unlocked achievements are returned to caller.
- [x] Unit test completed action registers game session summary.
- [x] Unit test actions are rejected if game is not `IN_PROGRESS`.

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
- All 9 action processing tasks completed
- All 5 test tasks completed
- 626 total tests pass with 0 failures (12 new tests for this sprint)

incomplete_tasks:
- None

contract_changes:
- `GameOrchestrator.processAction()` signature changed from `(Long, String)` to `(Long, String, Long, Integer)` to include topicId and responseTimeMs
- New `ActionProcessingResult` DTO with full result details
- Tracking integration: `RegisterActivityAttemptUseCase`, `AdaptiveDifficultyService`, `EvaluateGameCompletionAchievementsUseCase`, `RegisterGameSessionSummaryUseCase`
- `GameModuleConfiguration` updated with new dependencies

learnings:
- Tracking operations wrapped in try-catch to not block gameplay if tracking fails
- Using `isNull()` and `any()` matchers properly for nullable parameters
- Dev profile continues to use FakeGameEngine as fallback

next_sprint_suggestions:
- Sprint 041: Game WebSocket Contract - add transport layer for game actions
