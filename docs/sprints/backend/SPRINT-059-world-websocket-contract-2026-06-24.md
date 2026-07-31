# Sprint 059 - backend

## Goal
Document World Map WebSocket events and payloads in `docs/contracts/api/websocket.json`.

## Status
status: completed
started_at: 2026-06-24
closed_at: 2026-06-24
blocked_by:
waiting_for:

## Contract Updates Summary

Updated `docs/contracts/api/websocket.json` from v1.5.0 to v1.6.0 with the following changes:

### Incoming Messages (client → server)

Added to `GameClientMessage` discriminator:
- `WORLD_HEARTBEAT`: Heartbeat to keep world state alive
  - `type`: `"world_heartbeat"` (const)
  - `timestamp`: optional ISO-8601
- `WORLD_DISCOVERY_INTERACTED`: Child tapped a discovery element
  - `type`: `"world_discovery_interacted"` (const)
  - `proposalRuntimeId`: string
  - `discoveryElementId`: int64

### Outgoing Events (server → client)

Added to `SessionEvent.event` enum:
- `WORLD_STATE_SYNC`: World state synchronization
- `WORLD_DESTINATION_READY`: Destination arrived with discovery elements
- `WORLD_ACTIVITY_STARTED`: Activity launched from world

### New Schemas Added

- `WorldStateSyncPayload`: status (ACTIVE/CLOSED/NO_STATE) + destination
- `WorldDestinationPayload`: destinationId, host, narrativeSituation, biome, discoveryElements[]
- `WorldHostPayload`: id, code, displayName, visualAssetKey?
- `WorldNarrativeSituationPayload`: id, code, displayText?, tone?
- `WorldDiscoveryElementPayload`: proposalRuntimeId, discoveryElementId, code, displayName, elementType, visualAssetKey?, interactionCueType?, hasActivity
- `WorldActivityStartedPayload`: gameId, activityId, transition (START_GAME)

### Child-Safe Design

All world payloads exclude forbidden fields:
- `ignored`, `abandoned`, `lowEngagement`, `engagementScore`
- `diagnosis`, `learningPathProgress`, `completedStepIds`
- Raw rejection codes: `ACTIVITY_INACTIVE`, `PROFILE_BLOCKED`

## Tasks

### Contract Events
- [x] Define client-to-backend `WORLD_HEARTBEAT`.
- [x] Define client-to-backend discovery interaction event `WORLD_DISCOVERY_INTERACTED`.
- [x] Define backend-to-client `WORLD_STATE_SYNC`.
- [x] Define backend-to-client `WORLD_DESTINATION_READY`.
- [x] Define backend-to-client `WORLD_ACTIVITY_STARTED` (note: WORLD_DISCOVERY_PROPOSED deferred to runtime sprint).

### Payload Rules
- [x] Include `sessionId` correlation following existing WebSocket convention.
- [x] Include host/situation/discovery metadata needed by frontend.
- [x] Exclude hidden progress, diagnostics, `IGNORED`, `ABANDONED`, and engagement labels from child-facing payloads.
- [x] Document payloads using the properties listed in `Contract Payload Properties`.
- [x] Verify forbidden child-facing fields are absent from all world payloads.

### Tests
- [x] Validate `websocket.json` is valid JSON.
- [x] No DTO mapping tests needed (contract-only sprint per instruction).

## Manual Tests
- [x] JSON validated successfully.
- [ ] Frontend review pending (contract inspection by FE developer).

## Test Results
- No code changes (contract-only sprint).
- All 792 framework tests pass unchanged.

## Risks
- Contract drift can block FEAT-011 frontend implementation.
- Exposing tracking outcomes to the child frontend would violate FEAT-008.

## Dependencies
- Sprint 054 completed.
- Sprint 055 completed.
- Sprint 057 completed.
- Sprint 058 completed.

## Agent Instruction
- This is a contract sprint; keep runtime changes minimal.
- Update only `docs/contracts/api/websocket.json` if no code DTOs are required yet.
- Keep event names stable and consistent with existing game WebSocket conventions.

## Notes
- Frontend must not infer progression or engagement from hidden backend data.
- WORLD_DISCOVERY_PROPOSED (server-initiated proposal) deferred to Sprint 060 runtime implementation.
- World WebSocket reuses existing `/ws/game` endpoint and `auth` message flow per existing game session.

## Review

completed_tasks:
- WORLD_HEARTBEAT incoming message defined
- WORLD_DISCOVERY_INTERACTED incoming message defined
- WORLD_STATE_SYNC outgoing event defined
- WORLD_DESTINATION_READY outgoing event defined
- WORLD_ACTIVITY_STARTED outgoing event defined
- WorldStateSyncPayload schema defined
- WorldDestinationPayload schema defined
- WorldHostPayload schema defined
- WorldNarrativeSituationPayload schema defined
- WorldDiscoveryElementPayload schema defined
- WorldActivityStartedPayload schema defined
- JSON validated successfully
- Contract version bumped to 1.6.0

incomplete_tasks:
- WORLD_DISCOVERY_PROPOSED (server-initiated, deferred to Sprint 060)

contract_changes:
- docs/contracts/api/websocket.json: v1.5.0 → v1.6.0

learnings:
- Extending existing /ws/game channel keeps auth simple (reuses existing session)
- Discriminator pattern (type field) works well for incoming message variants
- Child-safe payload design naturally maps to existing domain models with minimal exposure

next_sprint_suggestions:
- Sprint 060: World websocket runtime implementation (handler, DTOs, state sync)
