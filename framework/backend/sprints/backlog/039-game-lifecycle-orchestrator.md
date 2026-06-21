# Sprint 039 - backend
# -----------------------------------------------

## Goal
Implement the basic game lifecycle orchestration from game creation to `IN_PROGRESS`, `COMPLETED`, or `ABANDONED` without WebSocket transport.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Orchestrator Lifecycle
- [ ] Add `GameOrchestrator` application service.
- [ ] Start a game by loading content activity and difficulty through ports.
- [ ] Initialize game state through a selected `GameEngine`.
- [ ] Store active state in `GameStateRegistry`.
- [ ] Transition `WAITING` to `STARTING` when the child is ready.
- [ ] Transition `STARTING` to `IN_PROGRESS`.
- [ ] Transition to `COMPLETED` when the engine reports completion.
- [ ] Transition to `ABANDONED` for explicit abandon requests.

### Tests
- [ ] Unit test game start creates `WAITING` state.
- [ ] Unit test ready flow reaches `IN_PROGRESS`.
- [ ] Unit test completion removes state from registry.
- [ ] Unit test abandoned game removes state from registry.
- [ ] Unit test unsupported engine type returns a clear error.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
