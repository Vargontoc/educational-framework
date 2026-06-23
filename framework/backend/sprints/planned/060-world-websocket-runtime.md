# Sprint 060 - backend
# -----------------------------------------------

## Goal
Connect the world runtime to the existing child WebSocket channel so the frontend can receive destinations and send world interactions.

## Status
status: planned
started_at:
closed_at:
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
- [ ] Add handling for `WORLD_HEARTBEAT`.
- [ ] Add handling for world state sync/start if needed by frontend lifecycle.
- [ ] Add handling for discovery interaction.
- [ ] Send destination-ready payloads using the contract from Sprint 059.
- [ ] Send safe activity-start result when world starts a game.
- [ ] Send safe system inactivity event when world closes due to inactivity.
- [ ] Use DTOs with the properties listed in `DTO Properties`.

### Error Handling
- [ ] Return safe errors for missing world state.
- [ ] Return safe errors for invalid discovery interaction.
- [ ] Do not expose technical rejection reasons to child-facing payloads.
- [ ] Apply the mapping rules listed in `DTO Properties`.

### Tests
- [ ] Unit test world heartbeat WebSocket handling.
- [ ] Unit test destination sync WebSocket response.
- [ ] Unit test discovery interaction starts game or returns safe fallback.
- [ ] Unit test ignored proposal is not exposed to frontend.
- [ ] Unit test invalid message returns safe error.

## Manual Tests
- Open a child session locally.
- Request/sync world state.
- Receive one destination payload.
- Send discovery interaction.
- Confirm game starts or world continues safely.

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
This sprint is the runtime bridge between FEAT-008 backend world and FEAT-011 frontend world map.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
