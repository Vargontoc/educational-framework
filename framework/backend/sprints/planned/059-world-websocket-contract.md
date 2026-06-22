# Sprint 059 - backend
# -----------------------------------------------

## Goal
Document World Map WebSocket events and payloads in `docs/contracts/api/websocket.json`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Contract Events
- [ ] Define client-to-backend `WORLD_HEARTBEAT`.
- [ ] Define client-to-backend discovery interaction event, such as `WORLD_DISCOVERY_INTERACTED`.
- [ ] Define backend-to-client `WORLD_STATE_SYNC`.
- [ ] Define backend-to-client `WORLD_DESTINATION_READY`.
- [ ] Define backend-to-client `WORLD_DISCOVERY_PROPOSED` if needed by frontend.
- [ ] Define backend-to-client `WORLD_ACTIVITY_STARTED` or transition result if game launch succeeds.
- [ ] Reuse existing system events where appropriate, especially `SYSTEM_INACTIVITY`.

### Payload Rules
- [ ] Include `childSessionId` or session correlation according to existing WebSocket convention.
- [ ] Include host/situation/discovery metadata needed by frontend.
- [ ] Exclude hidden progress, diagnostics, `IGNORED`, `ABANDONED`, and engagement labels from child-facing payloads.
- [ ] Document that visual signal remains primary and avatar/audio is optional reinforcement.

### Tests
- [ ] Validate `websocket.json` is valid JSON.
- [ ] Add DTO mapping tests if DTOs are introduced in this sprint.

## Manual Tests
- Inspect `docs/contracts/api/websocket.json` with frontend expectations.
- Confirm the child-facing contract contains no diagnostic fields.

## Risks
- Contract drift can block FEAT-011 frontend implementation.
- Exposing tracking outcomes to the child frontend would violate FEAT-008.

## Dependencies
- Sprint 054 completed.
- Sprint 055 completed.
- Sprint 058 recommended.

## Agent Instruction
- This is a contract sprint; keep runtime changes minimal.
- Update only `docs/contracts/api/websocket.json` if no code DTOs are required yet.
- Keep event names stable and consistent with existing game WebSocket conventions.

## Notes
Frontend must not infer progression or engagement from hidden backend data.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
