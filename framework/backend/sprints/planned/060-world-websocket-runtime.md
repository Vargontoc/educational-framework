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

## Tasks

### WebSocket Handling
- [ ] Add handling for `WORLD_HEARTBEAT`.
- [ ] Add handling for world state sync/start if needed by frontend lifecycle.
- [ ] Add handling for discovery interaction.
- [ ] Send destination-ready payloads using the contract from Sprint 059.
- [ ] Send safe activity-start result when world starts a game.
- [ ] Send safe system inactivity event when world closes due to inactivity.

### Error Handling
- [ ] Return safe errors for missing world state.
- [ ] Return safe errors for invalid discovery interaction.
- [ ] Do not expose technical rejection reasons to child-facing payloads.

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
