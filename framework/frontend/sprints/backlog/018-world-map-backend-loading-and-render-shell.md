# Sprint 018 - frontend
# -----------------------------------------------

## Goal

Implement the FEAT-011 World Map loading and backend-driven render shell: keep GameView in loading until a valid backend world payload arrives, render the map only from contracted world events, and show the generic child-safe error screen when world cannot be loaded. Backend implementation is out of scope.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md` before editing.
- [ ] Review `docs/product/features/backend/FEAT-008-World-Module.md` for frontend/backend responsibility boundaries.
- [ ] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for GameView entry, WebSocket lifecycle, and terminal events.
- [ ] Review `docs/contracts/api/websocket.json` for `AUTH_ACK`, `WORLD_DESTINATION_READY`, `WORLD_STATE_SYNC`, and world payload schemas.
- [ ] Confirm `docs/contracts/api/openapi.json` has no child-facing World Map REST endpoint and do not add REST world calls.

### Loading Flow
- [ ] Keep GameView in a child-facing loading state while waiting for world state.
- [ ] Use the existing avatar placeholder image during loading.
- [ ] Do not render a provisional or local fallback World Map during loading.
- [ ] After WebSocket open, send the existing contracted `auth` message with `childSessionId`.
- [ ] After `AUTH_ACK`, wait for a valid world payload before rendering the map.

### Backend World Payload Handling
- [ ] Handle `WORLD_DESTINATION_READY` as the primary map-render event.
- [ ] Handle `WORLD_STATE_SYNC` with `status: ACTIVE` and a valid `destination` as a map-render event.
- [ ] Validate that required destination fields are present before rendering.
- [ ] Do not fabricate `destinationId`, `host`, `narrativeSituation`, `biome`, `discoveryElements`, or proposal identifiers.
- [ ] Use placeholders only for missing or unknown visual assets, not for missing world data.

### Generic Child-Safe Error Screen
- [ ] Create or prepare a generic child-safe error screen pattern inside GameView.
- [ ] Show the avatar placeholder image.
- [ ] Do not show technical child-facing error text.
- [ ] Do not show a provisional World Map in error state.
- [ ] Enter this error state on invalid/missing world payload, `NO_WORLD_STATE`, `INACTIVE_CLOSED` without terminal navigation, loading timeout, or WebSocket close before valid world state.
- [ ] Ensure this pattern can later be reused for vertical orientation handling.

### Verification
- [ ] Verify GameView does not show a map while world state is loading.
- [ ] Verify valid `WORLD_DESTINATION_READY` renders the map shell.
- [ ] Verify valid active `WORLD_STATE_SYNC` renders the map shell.
- [ ] Verify invalid/missing world state shows generic child-safe error screen.
- [ ] Verify no world REST call is added.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Provisional map regression**: frontend may render local world content before backend state arrives.
  Mitigation: keep loading until valid world payload; error screen instead of fallback map.
- **Contract drift**: frontend may assume fields not documented in WebSocket contract.
  Mitigation: use only `WorldDestinationPayload` and `WorldStateSyncPayload` fields.
- **Child technical error exposure**: loading failure may leak backend/WebSocket details.
  Mitigation: use the generic child-safe error screen.

## Dependencies

- Sprint 017 - discarded level placeholder removed.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/backend/FEAT-008-World-Module.md` - backend world responsibilities.
- `docs/contracts/api/websocket.json` - runtime world contract.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not render a local fallback map when backend world state is missing.
- Do not call `/api/v1/dev/content/*` for child World Map rendering.
- Keep all code, comments, and documentation in English.

## Notes

- This sprint establishes that backend world state is required before drawing the map.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
