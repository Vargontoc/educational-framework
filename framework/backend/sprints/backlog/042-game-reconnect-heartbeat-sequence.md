# Sprint 042 - backend
# -----------------------------------------------

## Goal
Implement game reconnect, heartbeat, and sequence-number behavior on top of the game shell.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Reconnect
- [ ] Add orchestrator method for `GAME_RECONNECT` by `childSessionId`.
- [ ] Return full active state when a game exists in `WAITING`, `STARTING`, or `IN_PROGRESS`.
- [ ] Return an explicit no-active-game sync result when no active game exists.
- [ ] Increment `sequenceNumber` for each `GAME_STATE_SYNC`.

### Heartbeat
- [ ] Add orchestrator method for `GAME_HEARTBEAT`.
- [ ] Update `GameState.lastActivityAt`.
- [ ] Call session heartbeat/activity use case to update `ChildSession.lastActivityAt`.
- [ ] Keep heartbeat response lightweight; no full state update unless needed.

### Sequence Numbers
- [ ] Increment `sequenceNumber` for each `GAME_STATE_UPDATE` and `GAME_STATE_SYNC`.
- [ ] Ignore duplicate `GAME_ACTION` messages with already-processed sequence number.
- [ ] Force `GAME_STATE_SYNC` when a gap is detected.

### Tests
- [ ] Unit test reconnect returns active state.
- [ ] Unit test reconnect returns no-active-game result.
- [ ] Unit test heartbeat updates game and session activity timestamps.
- [ ] Unit test duplicate action is ignored.
- [ ] Unit test sequence gap forces sync.

## Manual Tests
- Start a fake game through a dev/test flow.
- Simulate reconnect for the same child session.
- Confirm full state sync is returned.
- Simulate reconnect after completion and confirm no-active-game sync.

## Risks
- Incorrect sequence handling can make the child frontend appear stuck.
- Updating game heartbeat but not session heartbeat can cause unexpected session expiration.

## Dependencies
- Sprint 034 completed.
- Sprint 038 completed.
- Sprint 040 completed.
- Sprint 041 completed if payload DTOs are used in tests.

## Agent Instruction
- Keep implementation deterministic and heavily unit tested.
- Do not add Redis or persistent game state.
- Treat process restart as no active game, as documented in FEAT-007.

## Notes
This sprint implements the resilience behavior needed for tablets and unstable home networks.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
