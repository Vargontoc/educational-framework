# Sprint 021 - frontend
# -----------------------------------------------

## Goal

Implement FEAT-011 contracted world runtime messages in GameView: `world_heartbeat`, `world_discovery_interacted`, and safe handling of `WORLD_ACTIVITY_STARTED` without rendering minigames. Backend implementation is out of scope.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `World Interaction`.
- [ ] Review `docs/contracts/api/websocket.json` for `WorldHeartbeatMessage`, `WorldDiscoveryInteractedMessage`, and `WORLD_ACTIVITY_STARTED`.
- [ ] Review existing GameView heartbeat cleanup from FEAT-007 and FEAT-010.
- [ ] Do not change backend code or backend contracts in this sprint.

### World Heartbeat
- [ ] Start `world_heartbeat` only after WebSocket auth succeeded and valid active world state was received.
- [ ] Send `world_heartbeat` periodically using the contracted message type.
- [ ] Include optional timestamp only if implementation needs it and it remains contract-compatible.
- [ ] Stop `world_heartbeat` on GameView unmount.
- [ ] Stop `world_heartbeat` on WebSocket close or error.
- [ ] Stop `world_heartbeat` on terminal session events.
- [ ] Stop `world_heartbeat` when entering the generic child-safe error screen.
- [ ] Stop `world_heartbeat` when `WORLD_ACTIVITY_STARTED` begins transition.

### Discovery Interaction
- [ ] Send `world_discovery_interacted` when the child taps a backend discovery element with `hasActivity: true`.
- [ ] Include the exact backend `proposalRuntimeId`.
- [ ] Include the exact backend `discoveryElementId`.
- [ ] Do not send the message for decorative local elements.
- [ ] Do not send the message for discovery elements missing required identifiers.
- [ ] Debounce or guard repeated taps if needed to avoid duplicate starts for the same proposal.

### Activity Started Handling
- [ ] Handle `WORLD_ACTIVITY_STARTED` without rendering a minigame.
- [ ] Move the UI to a safe transition/loading state using the avatar placeholder.
- [ ] Preserve child-safe non-technical UI if the following minigame feature is not implemented yet.
- [ ] Do not send `game_start` directly from frontend in this sprint.

### Verification
- [ ] Verify `world_heartbeat` is not sent before active world state.
- [ ] Verify `world_heartbeat` stops on cleanup/error/transition.
- [ ] Verify tapping backend discovery element sends one valid `world_discovery_interacted` message.
- [ ] Verify decorative/local elements never send backend discovery messages.
- [ ] Verify `WORLD_ACTIVITY_STARTED` does not break the UI without minigames.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Heartbeat leak**: world heartbeat may continue after leaving the map.
  Mitigation: centralize cleanup and stop it in every terminal/error path.
- **Duplicate proposal start**: repeated taps may send duplicate interactions.
  Mitigation: guard per proposal runtime id after first send.
- **Scope creep into games**: `WORLD_ACTIVITY_STARTED` may trigger premature engine rendering.
  Mitigation: transition only; no minigame rendering in this sprint.

## Dependencies

- Sprint 020 - backend world elements rendering.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/contracts/api/websocket.json` - world client messages and events.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not send discovery interaction for local/decorative elements.
- Do not render minigames.
- Do not record or persist engagement data locally.
- Keep all code, comments, and documentation in English.

## Notes

- Backend owns proposal lifecycle and ActivityProposalLog.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
