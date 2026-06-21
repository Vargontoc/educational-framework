# Sprint 043 - backend
# -----------------------------------------------

## Goal
Implement system-event priority, abandonment flow, and per-game action serialization for the FEAT-007 shell.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### System Events
- [ ] Add orchestrator method to handle `SYSTEM_EXPELLED` for an active child session.
- [ ] Add orchestrator method to handle `SYSTEM_BLOCKED` for an active child session.
- [ ] Add orchestrator method to handle `SYSTEM_INACTIVITY` for an active child session.
- [ ] Mark `GameState.systemEventPending = true` before applying abandonment.
- [ ] Transition active game state to `ABANDONED`.
- [ ] Register `GameSessionSummary` with final status `ABANDONED`.
- [ ] Remove abandoned state from registry.

### Action Priority
- [ ] Before applying an `ActionResult`, check `systemEventPending` again.
- [ ] Do not register tracking attempts for actions discarded due to a pending system event.
- [ ] Do not emit state-update output for discarded actions.

### Serialization
- [ ] Add simple per-`gameId` guard or lock so only one `GAME_ACTION` is processed at a time.
- [ ] Release the guard on success and failure.
- [ ] Add tests for concurrent action attempts if practical.

### Tests
- [ ] Unit test expelled session abandons active game.
- [ ] Unit test blocked session abandons active game.
- [ ] Unit test inactivity abandons active game.
- [ ] Unit test pending system event prevents tracking attempt registration.
- [ ] Unit test abandoned summary is registered exactly once.

## Manual Tests
- Start a fake game through a dev/test flow.
- Trigger a simulated expel/block/inactivity event.
- Confirm the game is abandoned, removed from memory, and a summary is recorded.

## Risks
- Race conditions can cause attempts after expulsion to be recorded.
- Missing summary registration would hide abandoned sessions from parental dashboard.
- Locking too broadly could block unrelated games.

## Dependencies
- Sprint 032 completed.
- Sprint 040 completed.
- Sprint 042 recommended.

## Agent Instruction
- Keep locking scoped to `gameId` only.
- Prefer simple synchronization over complex async infrastructure for v1.
- Do not add a `PAUSED` state.

## Notes
This sprint protects the child experience when parental/system events happen during gameplay.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
