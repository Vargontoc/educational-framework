# Sprint 039 - backend
# -----------------------------------------------

## Goal
Implement the basic game lifecycle orchestration from game creation to `IN_PROGRESS`, `COMPLETED`, or `ABANDONED` without WebSocket transport.

## Status
status: closed
started_at: 2026-06-22
closed_at: 2026-06-22
blocked_by:
waiting_for:

## Tasks

### Orchestrator Lifecycle
- [x] Add `GameOrchestrator` application service.
- [x] Start a game by loading content activity and difficulty through ports.
- [x] Initialize game state through a selected `GameEngine`.
- [x] Store active state in `GameStateRegistry`.
- [x] Transition `WAITING` to `STARTING` when the child is ready.
- [x] Transition `STARTING` to `IN_PROGRESS`.
- [x] Transition to `COMPLETED` when the engine reports completion.
- [x] Transition to `ABANDONED` for explicit abandon requests.

### Tests
- [x] Unit test game start creates `WAITING` state.
- [x] Unit test ready flow reaches `IN_PROGRESS`.
- [x] Unit test completion removes state from registry.
- [x] Unit test abandoned game removes state from registry.
- [x] Unit test unsupported engine type returns a clear error.

## Manual Tests
- Optional: run a dev/test fixture that starts a fake game and advances it to `IN_PROGRESS`.
- Confirm logs or returned state show the expected lifecycle transitions.

## Risks
- Adding WebSocket now would make lifecycle tests harder.
- Loading content directly from persistence would break boundaries.

## Dependencies
- Sprint 035 completed.
- Sprint 037 completed.
- Sprint 038 completed.

## Agent Instruction
- Keep this sprint transport-free: no WebSocket controller yet.
- Use fake/test engine for orchestration tests.
- Keep transitions explicit and deterministic.

## Notes
This sprint is the first real game shell orchestration step.

## Review

completed_tasks:
- All 8 orchestrator lifecycle tasks completed
- All 5 test tasks completed
- 624 total tests pass with 0 failures (10 new tests)

incomplete_tasks:
- None

contract_changes:
- New `GameOrchestrator` port interface in `game/ports/in/`
- New `GameOrchestratorService` implementation in `game/service/`
- New exception classes: `GameLifecycleException`, `GameNotFoundException`, `InvalidStateTransitionException`, `EngineNotAvailableException`
- New `FakeGameEngine` in `game/engine/` for dev profile fallback
- Fixed: `GameCatalogService` now has `@Service` annotation (was missing)

learnings:
- Engine resolution deferred to `readyGame` since engine is only needed when game actually starts
- Dev profile uses FakeGameEngine as fallback; prod profile throws `EngineNotAvailableException`

next_sprint_suggestions:
- Sprint 040: Game Action Processing - process game actions and return results
- Sprint 041: Game WebSocket Contract - add transport layer
