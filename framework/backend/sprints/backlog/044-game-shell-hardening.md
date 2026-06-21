# Sprint 044 - backend
# -----------------------------------------------

## Goal
Harden the FEAT-007 game shell implementation with boundary checks, contract verification, and manual end-to-end validation using the fake engine.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Boundary Review
- [ ] Verify concrete engines do not depend on tracking, avatar, WebSocket, repositories, or other modules.
- [ ] Verify `GameOrchestrator` is the only game component calling tracking ports.
- [ ] Verify game state is not persisted in the game module.
- [ ] Verify session heartbeat is accessed through a session port/use case.
- [ ] Verify content is accessed through content ports/use cases.

### Contract Review
- [ ] Verify `docs/contracts/api/websocket.json` includes all FEAT-007 events.
- [ ] Verify `GAME_DIFFICULTY_CHANGED` is emitted/represented when tracking returns difficulty change.
- [ ] Verify achievement unlock payloads contain frontend-ready fields.
- [ ] Verify no unintended public REST endpoints were added.

### Test Review
- [ ] Run all game unit tests.
- [ ] Run relevant tracking tests affected by `GameSessionSummary` and achievements.
- [ ] Run relevant session heartbeat tests.
- [ ] Run full backend test suite if practical.
- [ ] Add missing negative tests discovered during review.

### Manual Validation
- [ ] Start a fake game.
- [ ] Move it from `WAITING` to `IN_PROGRESS`.
- [ ] Process one correct action.
- [ ] Process one action that changes difficulty using a mocked/stubbed tracking response if possible.
- [ ] Complete the fake game and confirm memory cleanup.
- [ ] Reconnect during active game and confirm state sync.
- [ ] Reconnect after completion and confirm no-active-game sync.
- [ ] Trigger abandonment and confirm summary final status `ABANDONED`.

## Manual Tests
- Use the checklist in the Manual Validation section.
- Record any command, endpoint, or fixture used in the sprint review.

## Risks
- Contract drift between backend and frontend can remain hidden until integration.
- Docker/Testcontainers may not be available for integration tests.
- Manual testing may require temporary dev-only fixtures; remove or isolate them before completing the sprint.

## Dependencies
- Sprint 041 completed.
- Sprint 042 completed.
- Sprint 043 completed.

## Agent Instruction
- This is a hardening sprint, not a feature expansion sprint.
- Do not add real minigames.
- Do not change feature scope unless a test or boundary violation requires it.
- Document skipped tests and why they were skipped.

## Notes
This sprint closes the FEAT-007 shell before work starts on concrete minigame engines.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
