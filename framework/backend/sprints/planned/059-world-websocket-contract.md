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

## Contract Payload Properties

### Incoming: WORLD_HEARTBEAT

- `type`: String, required, value `world_heartbeat` or existing contract naming convention.
- `timestamp`: timestamp/string, optional client timestamp.

### Incoming: WORLD_DISCOVERY_INTERACTED

- `type`: String, required, value `world_discovery_interacted` or existing contract naming convention.
- `proposalRuntimeId`: String or Long, required.
- `discoveryElementId`: Long, required if frontend has it.

### Outgoing: WORLD_STATE_SYNC

- `event`: String, required, value `WORLD_STATE_SYNC`.
- `sessionId`: Long, required, following existing child WebSocket convention.
- `payload.status`: String, required, safe world runtime status.
- `payload.destination`: World destination payload, nullable when no active world state exists.

### Outgoing: WORLD_DESTINATION_READY

- `event`: String, required, value `WORLD_DESTINATION_READY`.
- `sessionId`: Long, required.
- `payload.destinationId`: String or Long, required.
- `payload.host`: object, required, with `id`, `code`, `displayName`, `visualAssetKey?`.
- `payload.narrativeSituation`: object, required, with `id`, `code`, `displayText?`, `tone?`.
- `payload.biome`: String or enum, required.
- `payload.discoveryElements`: array, required, can be empty.

### Outgoing Discovery Element Payload

- `proposalRuntimeId`: String or Long, required.
- `discoveryElementId`: Long, required.
- `code`: String, required.
- `displayName`: String, required.
- `elementType`: String or enum, required.
- `visualAssetKey`: String, nullable.
- `interactionCueType`: String or enum, nullable.
- `hasActivity`: boolean, required.

### Outgoing: WORLD_ACTIVITY_STARTED

- `event`: String, required, value `WORLD_ACTIVITY_STARTED`.
- `sessionId`: Long, required.
- `payload.gameId`: Long, required.
- `payload.activityId`: Long, required.
- `payload.transition`: String, required, child-safe value such as `START_GAME`.

### Forbidden Child-Facing Fields

The WebSocket contract must not expose these fields in child-facing payloads:

- `ignored`
- `abandoned`
- `lowEngagement`
- `engagementScore`
- `diagnosis`
- `learningPathProgress`
- `completedStepIds`
- raw technical rejection reasons such as `ACTIVITY_INACTIVE` or `PROFILE_BLOCKED`

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
- [ ] Document payloads using the properties listed in `Contract Payload Properties`.
- [ ] Verify forbidden child-facing fields are absent from all world payloads.

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
