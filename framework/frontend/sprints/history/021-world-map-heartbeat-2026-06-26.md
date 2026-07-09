# Sprint 021 - frontend
# -----------------------------------------------

## Goal

Implement FEAT-011 contracted world runtime messages in GameView: `world_heartbeat`, `world_discovery_interacted`, and safe handling of `WORLD_ACTIVITY_STARTED` without rendering minigames. Backend implementation is out of scope.

## Status

status: completed
started_at: 2026-06-26
closed_at: 2026-06-26
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `World Interaction`.
- [x] Review `docs/contracts/api/websocket.json` for `WorldHeartbeatMessage`, `WorldDiscoveryInteractedMessage`, and `WORLD_ACTIVITY_STARTED`.
- [x] Review existing GameView heartbeat cleanup from FEAT-007 and FEAT-010.
- [x] Do not change backend code or backend contracts in this sprint.

### World Heartbeat
- [x] Start `world_heartbeat` only after WebSocket auth succeeded and valid active world state was received.
- [x] Send `world_heartbeat` periodically using the contracted message type.
- [x] Include optional timestamp only if implementation needs it and it remains contract-compatible.
- [x] Stop `world_heartbeat` on GameView unmount.
- [x] Stop `world_heartbeat` on WebSocket close or error.
- [x] Stop `world_heartbeat` on terminal session events.
- [x] Stop `world_heartbeat` when entering the generic child-safe error screen.
- [x] Stop `world_heartbeat` when `WORLD_ACTIVITY_STARTED` begins transition.

### Discovery Interaction
- [x] Send `world_discovery_interacted` when the child taps a backend discovery element with `hasActivity: true`.
- [x] Include the exact backend `proposalRuntimeId`.
- [x] Include the exact backend `discoveryElementId`.
- [x] Do not send the message for decorative local elements.
- [x] Do not send the message for discovery elements missing required identifiers.
- [x] Debounce or guard repeated taps if needed to avoid duplicate starts for the same proposal.

### Activity Started Handling
- [x] Handle `WORLD_ACTIVITY_STARTED` without rendering a minigame.
- [x] Move the UI to a safe transition/loading state using the avatar placeholder.
- [x] Preserve child-safe non-technical UI if the following minigame feature is not implemented yet.
- [x] Do not send `game_start` directly from frontend in this sprint.

### Verification
- [x] Verify `world_heartbeat` is not sent before active world state.
- [x] Verify `world_heartbeat` stops on cleanup/error/transition.
- [x] Verify tapping backend discovery element sends one valid `world_discovery_interacted` message.
- [x] Verify decorative/local elements never send backend discovery messages.
- [x] Verify `WORLD_ACTIVITY_STARTED` does not break the UI without minigames.
- [x] Run `npm run build` from `framework/frontend/app`.

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
- Review FEAT-011-World-Map..md World Interaction section
- Review websocket.json for world client messages
- Review existing heartbeat cleanup from previous sprints
- Add sentProposals Set for duplicate proposal protection
- Update handleDiscoveryClick to check/add proposalKey before sending
- Add GAME_COMPLETED handler to clear sentProposals
- Add GAME_ABANDONED handler to clear sentProposals
- Verify npm run build passes

incomplete_tasks:

contract_changes: none

learnings:
- sentProposals tracks "${proposalRuntimeId}-${discoveryElementId}" to prevent duplicate sends
- GAME_COMPLETED and GAME_ABANDONED clear sentProposals when activity ends
- WORLD_ACTIVITY_STARTED does NOT clear sentProposals (child may return)
- Error state does NOT clear sentProposals (keep same proposals)
- Proposal tracking is per-session, cleared on session end

next_sprint_suggestions:
- FEAT-011 continues: minigame rendering when WORLD_ACTIVITY_STARTED received
- FEAT-011 continues: handle minigame completion/abandonment flow
- FEAT-011 continues: return to world after minigame ends

## Implementation Details

### Files Modified

#### src/views/GameView.vue
- Added `sentProposals = new Set<string>()` for duplicate proposal tracking
- Updated `handleDiscoveryClick` to generate proposalKey and check before sending
- Added `case 'GAME_COMPLETED':` and `case 'GAME_ABANDONED':` handlers that call `sentProposals.clear()`

### Event Flow

```
Discovery tap → proposalKey check → if not in set, add and send world_discovery_interacted
GAME_COMPLETED → sentProposals.clear()
GAME_ABANDONED → sentProposals.clear()
WORLD_ACTIVITY_STARTED → UI transitions to loading, sentProposals NOT cleared
Error state → sentProposals NOT cleared
```

### Build Verification
- npm run build: passed
- GameView JS: 8.75 kB (up from 8.59 kB)
