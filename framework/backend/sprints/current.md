# Sprint 058 - backend
# -----------------------------------------------

## Goal
Implement `WORLD_HEARTBEAT`, world inactivity tracking, and safe cleanup when the child is exploring without an active game.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Model Properties

### WorldHeartbeatResult

Result of handling `WORLD_HEARTBEAT`.

- `childSessionId`: Long, required.
- `worldStateFound`: boolean, required.
- `lastWorldActivityAt`: timestamp/string following existing backend timestamp pattern, nullable when no state exists.
- `childSessionActivityUpdated`: boolean, required.

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

### Rules

- `WORLD_HEARTBEAT` updates `WorldState.lastWorldActivityAt` and `ChildSession.lastActivityAt`.
- `WORLD_HEARTBEAT` must not update or abandon `GameState`.
- Inactivity threshold must be more permissive than game inactivity.
- Pending proposal is resolved as `IGNORED` on world inactivity, but this is not child-facing.

## Tasks

### Heartbeat
- [ ] Add world heartbeat use case by `childSessionId`.
- [ ] Update `WorldState.lastWorldActivityAt`.
- [ ] Update `ChildSession.lastActivityAt` through the session heartbeat/activity use case.
- [ ] Keep `WORLD_HEARTBEAT` separate from `GAME_HEARTBEAT`.
- [ ] Return `WorldHeartbeatResult` with the properties listed in `Model Properties`.

### Inactivity
- [ ] Add configurable world inactivity threshold, more permissive than game inactivity.
- [ ] Detect inactive world states.
- [ ] Close inactive world state in memory.
- [ ] Resolve pending proposal as `IGNORED` on inactivity.
- [ ] Return `WorldInactivityResult` with the properties listed in `Model Properties`.
- [ ] Apply the rules listed in `Model Properties`.

### Tests
- [ ] Unit test heartbeat updates world timestamp.
- [ ] Unit test heartbeat calls session activity use case.
- [ ] Unit test world inactivity closes state.
- [ ] Unit test pending proposal resolves as `IGNORED` on inactivity.
- [ ] Unit test narrative pauses do not trigger inactivity with default threshold.

## Manual Tests
- Start a world state without active game.
- Send `WORLD_HEARTBEAT` and verify child session remains active.
- Stop heartbeat and verify world state closes after threshold.

## Risks
- If session heartbeat is not updated, the child session can expire while the child is only exploring the map.
- Threshold too strict can punish normal narrative pauses.

## Dependencies
- Sprint 034 completed.
- Sprint 052 completed.
- Sprint 055 completed.

## Agent Instruction
- Do not reuse `GAME_HEARTBEAT` for world.
- Do not abandon a `GameState` from world heartbeat logic.
- Keep thresholds configurable through world settings/config.

## Notes
The walk includes legitimate pauses; inactivity must be forgiving for ages 3-4.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
