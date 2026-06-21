# Sprint 041 - backend
# -----------------------------------------------

## Goal
Define the game WebSocket message DTOs and update `docs/contracts/api/websocket.json` for FEAT-007 shell events.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Event Contract
- [ ] Define client-to-backend events: `GAME_READY`, `GAME_ACTION`, `GAME_RECONNECT`, `GAME_HEARTBEAT`.
- [ ] Define backend-to-client events: `GAME_STATE_UPDATE`, `GAME_STATE_SYNC`, `GAME_DIFFICULTY_CHANGED`, `GAME_ACHIEVEMENT_UNLOCKED`, `GAME_COMPLETED`.
- [ ] Define system-to-client events used by game: `SYSTEM_EXPELLED`, `SYSTEM_BLOCKED`, `SYSTEM_INACTIVITY`, `SYSTEM_AGENT_DOWN`, `SYSTEM_AUDIO_DISABLED`.
- [ ] Document required fields including `childSessionId`, `gameId`, `sequenceNumber`, payload type, and timestamps where needed.

### DTOs
- [ ] Add backend DTOs for incoming game messages.
- [ ] Add backend DTOs for outgoing game messages.
- [ ] Keep DTOs separate from domain models.

### Contract File
- [ ] Update `docs/contracts/api/websocket.json` with FEAT-007 events.
- [ ] Include direction, event name, payload schema, and sequencing rules.

### Tests
- [ ] Unit test DTO mapping from domain result to outgoing event payload.
- [ ] Validate `websocket.json` is valid JSON.
- [ ] Add contract test if the project already has a contract validation pattern.

## Manual Tests
- Open `docs/contracts/api/websocket.json` and verify all FEAT-007 events are documented.
- Confirm frontend can identify direction and required payload fields without reading backend code.

## Risks
- Contract drift can block frontend game view implementation.
- Exposing domain internals directly in DTOs can make future engine payload changes harder.

## Dependencies
- Sprint 036 completed.
- Sprint 040 recommended for final event payload shape.

## Agent Instruction
- This is a contract sprint, not a runtime WebSocket sprint.
- Do not implement WebSocket connection handling here unless minimal mapping tests require it.
- Keep payloads small and child-friendly frontend oriented.

## Notes
This sprint gives frontend a stable event vocabulary before transport behavior is implemented.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
