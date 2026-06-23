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

## Model Properties

### WorldGameStartResult

Safe result returned by `world` after trying to start a game from a discovery element.

- `childSessionId`: Long, required.
- `activityId`: Long, required.
- `gameId`: Long, nullable; present only when game start succeeds or existing game is reused.
- `status`: WorldGameStartStatus, required.
- `safeFallbackDestination`: WorldDestination, nullable, used when the walk should continue without game.

### WorldGameStartStatus

- `STARTED`: Game was started successfully.
- `EXISTING_GAME_ACTIVE`: A game already exists and frontend should transition/sync safely.
- `FALLBACK_DESTINATION`: Game could not start, but world built a safe non-technical fallback.
- `BLOCKED`: Profile/session is blocked; child-facing payload must still be safe and non-technical.

### Internal Rejection Reasons

These can exist internally for logs/tests but must not be sent as child-facing labels.

- `PROFILE_BLOCKED`
- `ACTIVITY_INACTIVE`
- `ACTIVE_GAME_EXISTS`
- `NO_ALTERNATIVE_ACTIVITY`
- `ENGINE_UNAVAILABLE`

### Rules

- `world` decides `activityId`, not `difficultyLevelId`.
- Technical rejection reasons are internal only.
- If game start is rejected, the child should see a continued walk or safe transition, not an error.

## Tasks

### Game Start
- [ ] Add world use case to start the activity tied to the current pending proposal.
- [ ] Call `GameOrchestrator.startGame(childSessionId, activityId)` or the real internal equivalent.
- [ ] Resolve proposal as `STARTED` only when game start succeeds or is accepted by the game shell.
- [ ] Return `WorldGameStartResult` with the properties listed in `Model Properties`.

### Rejection Handling
- [ ] Handle blocked profile rejection without showing technical error to child.
- [ ] Handle inactive activity rejection by selecting an alternative compatible activity when possible.
- [ ] Handle already-active-game rejection with a safe no-op or transition to existing game state.
- [ ] If no alternative exists, continue with decorative/narrative destination.
- [ ] Apply the rules listed in `Model Properties`.

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
