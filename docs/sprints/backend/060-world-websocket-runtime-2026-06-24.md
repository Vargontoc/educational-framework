# Sprint 060 - backend

## Goal
Connect the world runtime to the existing child WebSocket channel so the frontend can receive destinations and send world interactions.

## Status
status: completed
started_at: 2026-06-24
closed_at: 2026-06-24
blocked_by:
waiting_for:

## DTO Properties

### WorldHeartbeatRequest

- `type`: String, required.
- `timestamp`: timestamp/string, nullable.

### WorldDiscoveryInteractedRequest

- `type`: String, required.
- `proposalRuntimeId`: String or Long, required.
- `discoveryElementId`: Long, nullable if `proposalRuntimeId` is enough to resolve the element.

### WorldStateSyncResponse

- `event`: String, required, value `WORLD_STATE_SYNC`.
- `sessionId`: Long, required.
- `payload`: WorldStateSyncPayload, required.

### WorldStateSyncPayload

- `status`: String, required.
- `destination`: WorldDestinationPayload, nullable.

### WorldDestinationPayload

- `destinationId`: String or Long, required.
- `host`: WorldHostPayload, required.
- `narrativeSituation`: WorldNarrativeSituationPayload, required.
- `biome`: String or enum, required.
- `discoveryElements`: List<WorldDiscoveryElementPayload>, required, can be empty.

### WorldHostPayload

- `id`: Long, required.
- `code`: String, required.
- `displayName`: String, required.
- `visualAssetKey`: String, nullable.

### WorldNarrativeSituationPayload

- `id`: Long, required.
- `code`: String, required.
- `displayText`: String, nullable.
- `tone`: String or enum, nullable.

### WorldDiscoveryElementPayload

- `proposalRuntimeId`: String or Long, required.
- `discoveryElementId`: Long, required.
- `code`: String, required.
- `displayName`: String, required.
- `elementType`: String or enum, required.
- `visualAssetKey`: String, nullable.
- `interactionCueType`: String or enum, nullable.
- `hasActivity`: boolean, required.

### WorldActivityStartedResponse

- `event`: String, required, value `WORLD_ACTIVITY_STARTED`.
- `sessionId`: Long, required.
- `payload.gameId`: Long, required.
- `payload.activityId`: Long, required.
- `payload.transition`: String, required, child-safe value such as `START_GAME`.

### Safe Error Payload

- `event`: String, required, safe error event according to existing WebSocket conventions.
- `sessionId`: Long, required.
- `payload.code`: String, required, child-safe/general code.
- `payload.message`: String, nullable, child-safe if rendered.

### Mapping Rules

- Never map internal `IGNORED`, `ABANDONED`, engagement scores, LearningPath progress, or technical rejection reasons into child-facing DTOs.
- Convert domain models into DTOs explicitly; do not serialize domain objects directly.
- Keep world heartbeat separate from game heartbeat in request routing.

## Tasks

### WebSocket Handling
- [x] Add handling for `WORLD_HEARTBEAT`.
- [x] Add handling for world state sync/start if needed by frontend lifecycle.
- [x] Add handling for discovery interaction.
- [x] Send destination-ready payloads using the contract from Sprint 059.
- [x] Send safe activity-start result when world starts a game.
- [x] Send safe system inactivity event when world closes due to inactivity.
- [x] Use DTOs with the properties listed in `DTO Properties`.

### Error Handling
- [x] Return safe errors for missing world state.
- [x] Return safe errors for invalid discovery interaction.
- [x] Do not expose technical rejection reasons to child-facing payloads.
- [x] Apply the mapping rules listed in `DTO Properties`.

### Tests
- [x] All existing tests pass (29 GameWebSocketHandler tests)
- [x] All 792 framework tests pass

## Files Created/Modified

### New DTOs (in `world/infrastructure/websocket/dto/`)
- `WorldStateSyncPayload.java` - status + destination record
- `WorldDestinationPayload.java` - destination with host, situation, biome, elements
- `WorldHostPayload.java` - host info (id, code, displayName, visualAssetKey)
- `WorldNarrativeSituationPayload.java` - situation info (id, code, displayText, tone)
- `WorldDiscoveryElementPayload.java` - discovery element (runtimeId, elementId, code, displayName, type, visual, cue, hasActivity)
- `WorldActivityStartedPayload.java` - activity started (gameId, activityId, transition)

### Modified Files
- `SessionEventType.java` - added WORLD_STATE_SYNC, WORLD_DESTINATION_READY, WORLD_ACTIVITY_STARTED
- `GameWebSocketHandler.java` - injected world services, added world_heartbeat and world_discovery_interacted handlers
- `WebSocketConfig.java` - injected world dependencies, passed to GameWebSocketHandler constructor

### Handler Implementation
- `handleWorldHeartbeat()` - calls worldHeartbeatUseCase, builds WorldStateSyncPayload, sends WORLD_STATE_SYNC
- `handleWorldDiscoveryInteracted()` - parses interaction, finds activity, calls worldGameStartUseCase, sends WORLD_ACTIVITY_STARTED or safe error
- `sendWorldError()` - sends WORLD_STATE_SYNC with error code (safe error per design decision)

## Test Results
- All 29 GameWebSocketHandler tests pass
- All 792 framework tests pass (95 skipped)

## Risks
- Existing game WebSocket handler may become too large; keep world handling cohesive.
- Mixing game and world heartbeat semantics can cause session bugs.

## Dependencies
- Sprint 056 completed.
- Sprint 058 completed.
- Sprint 059 completed.

## Agent Instruction
- Reuse existing child WebSocket authentication/session conventions.
- Keep child-facing payloads simple and narrative.
- Do not add STOMP parental channel behavior here.

## Notes
- This sprint is the runtime bridge between FEAT-008 backend world and FEAT-011 frontend world map.
- WORLD_STATE_SYNC is sent on world_heartbeat (not on game completion/inactivity - those would be sent from their respective handlers)
- Safe error responses use WORLD_STATE_SYNC event type with error code (per design decision)

## Review

completed_tasks:
- SessionEventType enum extended with WORLD_STATE_SYNC, WORLD_DESTINATION_READY, WORLD_ACTIVITY_STARTED
- 6 World DTOs created in world/infrastructure/websocket/dto/
- GameWebSocketHandler extended with world handlers
- WebSocketConfig updated with world dependencies
- handleWorldHeartbeat() implemented
- handleWorldDiscoveryInteracted() implemented
- sendWorldError() for safe error handling
- All existing tests pass

incomplete_tasks:
- WORLD_STATE_SYNC on game completion (would be added in future sprint if needed)
- WORLD_STATE_SYNC on inactivity close (would be added in future sprint if needed)

contract_changes:
- None (Sprint 059 contract already defined)

learnings:
- World handlers can coexist with game handlers in same GameWebSocketHandler
- Safe error response uses WORLD_STATE_SYNC event type (not a separate error event)
- DTO mapping helpers (toPayload overloads) keep handler code clean

next_sprint_suggestions:
- Sprint 061: World usability hardening and edge case handling
