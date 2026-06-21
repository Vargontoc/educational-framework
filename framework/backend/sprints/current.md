# Sprint 034 - backend
# -----------------------------------------------

## Goal
Expose a small session use case that lets the game shell update `ChildSession.lastActivityAt` from `GAME_HEARTBEAT`.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Session Use Case
- [ ] Add or reuse an internal use case for recording child session heartbeat/activity.
- [ ] Accept `childSessionId` and current timestamp.
- [ ] Reject missing or inactive child sessions.
- [ ] Update `ChildSession.lastActivityAt`.
- [ ] Return enough information for game to know whether the session is still active.

### Boundary
- [ ] Expose the use case through a session port that game can consume later.
- [ ] Do not make game read session persistence directly.
- [ ] Keep existing session heartbeat behavior compatible.

### Tests
- [ ] Unit test active session heartbeat updates `lastActivityAt`.
- [ ] Unit test expired, expelled, or closed sessions are rejected.
- [ ] Unit test unknown session id is rejected.
- [ ] Integration test persistence update if Testcontainers is available.

## Manual Tests
- Start backend locally.
- Create/open a child session using existing session flow.
- Trigger the heartbeat use case through an existing endpoint or test fixture.
- Confirm `lastActivityAt` changes.

## Risks
- Creating a second heartbeat source could make inactivity behavior inconsistent.
- Direct game-to-session persistence access would break module boundaries.

## Dependencies
- Session module implemented through Sprint 008.
- FEAT-007 heartbeat flow.

## Agent Instruction
- Keep this sprint inside session/application boundaries.
- Do not implement `GAME_HEARTBEAT` WebSocket handling yet.
- Prefer reusing existing heartbeat logic instead of adding duplicate code.

## Notes
This prepares the coordination required by FEAT-007 between `GameState.lastActivityAt` and `ChildSession.lastActivityAt`.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
