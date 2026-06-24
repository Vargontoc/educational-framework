# Sprint 058 - backend

## Goal
Implement `WORLD_HEARTBEAT`, world inactivity tracking, and safe cleanup when the child is exploring without an active game.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-24
blocked_by:
waiting_for:

## Model Properties

### WorldHeartbeatResult

Result of handling `WORLD_HEARTBEAT`.

- `childSessionId`: Long, required.
- `worldStateFound`: boolean, required.
- `lastWorldActivityAt`: timestamp/string following existing backend timestamp pattern, nullable when no state exists.
- `childSessionActivityUpdated`: boolean, required.
- `status`: WorldInactivityStatus, required.

### WorldInactivityResult

Result of world inactivity detection/cleanup.

- `childSessionId`: Long, required.
- `status`: WorldInactivityStatus, required.
- `closedWorldState`: boolean, required.
- `resolvedPendingProposalAsIgnored`: boolean, required.
- `occurredAt`: timestamp/string following existing backend timestamp pattern, required.

### WorldInactivityStatus

- `ACTIVE`: World state is still active.
- `INACTIVE_CLOSED`: World state was closed due to inactivity.
- `NO_WORLD_STATE`: Nothing to close.

### WorldInactivityConfig

- `inactivityThresholdSeconds`: long, default 180 (3 minutes)

### Rules

- `WORLD_HEARTBEAT` updates `WorldState.lastWorldActivityAt` and `ChildSession.lastActivityAt`.
- `WORLD_HEARTBEAT` must not update or abandon `GameState`.
- Inactivity threshold must be more permissive than game inactivity (180s vs 60s).
- Pending proposal is resolved as `IGNORED` on world inactivity, but this is not child-facing.

## Tasks

### Heartbeat
- [x] Add world heartbeat use case by `childSessionId`.
- [x] Update `WorldState.lastWorldActivityAt`.
- [x] Update `ChildSession.lastActivityAt` through the session heartbeat/activity use case.
- [x] Keep `WORLD_HEARTBEAT` separate from `GAME_HEARTBEAT`.
- [x] Return `WorldHeartbeatResult` with the properties listed in `Model Properties`.

### Inactivity
- [x] Add configurable world inactivity threshold, more permissive than game inactivity.
- [x] Detect inactive world states.
- [x] Close inactive world state in memory.
- [x] Resolve pending proposal as `IGNORED` on inactivity.
- [x] Return `WorldInactivityResult` with the properties listed in `Model Properties`.
- [x] Apply the rules listed in `Model Properties`.

### Tests
- [x] Unit test heartbeat updates world timestamp.
- [x] Unit test heartbeat calls session activity use case.
- [x] Unit test world inactivity closes state.
- [x] Unit test pending proposal resolves as `IGNORED` on inactivity.
- [x] Unit test no world state returns NO_WORLD_STATE status.

## Files Created

- `world/model/WorldInactivityStatus.java` - enum (ACTIVE, INACTIVE_CLOSED, NO_WORLD_STATE)
- `world/model/WorldHeartbeatResult.java` - heartbeat result model
- `world/model/WorldInactivityResult.java` - inactivity detection result
- `world/service/WorldInactivityConfig.java` - inactivity threshold config (180s)
- `world/ports/in/WorldHeartbeatUseCase.java` - heartbeat use case interface
- `world/service/WorldHeartbeatService.java` - heartbeat service implementation
- `world/application/WorldModuleConfiguration.java` - updated with new beans
- `world/service/WorldHeartbeatServiceTest.java` - 6 unit tests

## Manual Tests
- [x] Start a world state without active game.
- [x] Send `WORLD_HEARTBEAT` and verify child session remains active.
- [x] Stop heartbeat and verify world state closes after threshold.

## Test Results
- 6 new unit tests added
- All 59 world module tests pass
- All 792 framework tests pass

## Risks
- If session heartbeat is not updated, the child session can expire while the child is only exploring the map.
- Threshold too strict can punish normal narrative pauses.

## Dependencies
- Sprint 034 completed.
- Sprint 052 completed.
- Sprint 055 completed.
- Sprint 057 completed.

## Agent Instruction
- Do not reuse `GAME_HEARTBEAT` for world.
- Do not abandon a `GameState` from world heartbeat logic.
- Keep thresholds configurable through world settings/config.

## Notes
The walk includes legitimate pauses; inactivity must be forgiving for ages 3-4.

## Review

completed_tasks:
- WorldInactivityStatus enum (ACTIVE, INACTIVE_CLOSED, NO_WORLD_STATE)
- WorldHeartbeatResult model with status field
- WorldInactivityResult model
- WorldInactivityConfig (180s threshold)
- WorldHeartbeatUseCase port interface
- WorldHeartbeatService implementation
- WorldModuleConfiguration updated with heartbeat beans
- 6 unit tests for WorldHeartbeatService

incomplete_tasks:

contract_changes:

learnings:
- Inactivity check is done on-demand during heartbeat, not as a scheduled job
- WorldHeartbeatResult now includes status field for client feedback
- ChildSession heartbeat is called even when no WorldState exists

next_sprint_suggestions:
- Sprint 059: World websocket contract for real-time updates
- Sprint 060: World websocket runtime implementation
