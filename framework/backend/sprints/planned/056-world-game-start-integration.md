# Sprint 056 - backend
# -----------------------------------------------

## Goal
Integrate `world` with `GameOrchestrator.startGame(childSessionId, activityId)` to start games from discovery elements safely.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Game Start
- [ ] Add world use case to start the activity tied to the current pending proposal.
- [ ] Call `GameOrchestrator.startGame(childSessionId, activityId)` or the real internal equivalent.
- [ ] Resolve proposal as `STARTED` only when game start succeeds or is accepted by the game shell.
- [ ] Return a world-safe result that lets frontend transition to game.

### Rejection Handling
- [ ] Handle blocked profile rejection without showing technical error to child.
- [ ] Handle inactive activity rejection by selecting an alternative compatible activity when possible.
- [ ] Handle already-active-game rejection with a safe no-op or transition to existing game state.
- [ ] If no alternative exists, continue with decorative/narrative destination.

### Tests
- [ ] Unit test successful game start resolves proposal as `STARTED`.
- [ ] Unit test rejected inactive activity falls back safely.
- [ ] Unit test blocked profile does not expose technical error.
- [ ] Unit test already active game is handled safely.
- [ ] Unit test no direct dependency from game to world exists.

## Manual Tests
- Use fake/dev game engine.
- Generate a world destination with a discovery element.
- Trigger the discovery interaction.
- Confirm game starts or the world continues safely if rejected.

## Risks
- Existing game start contract may differ between WebSocket and internal orchestrator; adapt to the real code, not the older draft contract.
- Incorrect rejection handling can block the child's walk.

## Dependencies
- Sprint 050 completed if game event/result shape changes are needed.
- Sprint 055 completed.
- Sprint 039-044 completed.

## Agent Instruction
- World decides activity, not difficulty.
- Do not pass `difficultyLevelId` unless the current game port explicitly requires it.
- Never expose raw rejection reasons as child-facing messages.

## Notes
FEAT-008 is aligned with the implemented game shell where `activityId` is the main game start input.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
