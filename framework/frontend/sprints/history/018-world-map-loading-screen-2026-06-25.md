# Sprint 018 - frontend
# -----------------------------------------------

## Goal

Implement the FEAT-011 World Map loading and backend-driven render shell: keep GameView in loading until a valid backend world payload arrives, render the map only from contracted world events, and show the generic child-safe error screen when world cannot be loaded. Backend implementation is out of scope.

## Status

status: completed
started_at: 2026-06-25
closed_at: 2026-06-25
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md` before editing.
- [x] Review `docs/product/features/backend/FEAT-008-World-Module.md` for frontend/backend responsibility boundaries.
- [x] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for GameView entry, WebSocket lifecycle, and terminal events.
- [x] Review `docs/contracts/api/websocket.json` for `AUTH_ACK`, `WORLD_DESTINATION_READY`, `WORLD_STATE_SYNC`, and world payload schemas.
- [x] Confirm `docs/contracts/api/openapi.json` has no child-facing World Map REST endpoint and do not add REST world calls.

### Loading Flow
- [x] Keep GameView in a child-facing loading state while waiting for world state.
- [x] Use the existing avatar placeholder image during loading.
- [x] Do not render a provisional or local fallback World Map during loading.
- [x] After WebSocket open, send the existing contracted `auth` message with `childSessionId`.
- [x] After `AUTH_ACK`, wait for a valid world payload before rendering the map.

### Backend World Payload Handling
- [x] Handle `WORLD_DESTINATION_READY` as the primary map-render event.
- [x] Handle `WORLD_STATE_SYNC` with `status: ACTIVE` and a valid `destination` as a map-render event.
- [x] Validate that required destination fields are present before rendering.
- [x] Do not fabricate `destinationId`, `host`, `narrativeSituation`, `biome`, `discoveryElements`, or proposal identifiers.
- [x] Use placeholders only for missing or unknown visual assets, not for missing world data.

### Generic Child-Safe Error Screen
- [x] Create or prepare a generic child-safe error screen pattern inside GameView.
- [x] Show the avatar placeholder image.
- [x] Do not show technical child-facing error text.
- [x] Do not show a provisional World Map in error state.
- [x] Enter this error state on invalid/missing world payload, `NO_WORLD_STATE`, `INACTIVE_CLOSED` without terminal navigation, loading timeout, or WebSocket close before valid world state.
- [x] Ensure this pattern can later be reused for vertical orientation handling.

### Verification
- [x] Verify GameView does not show a map while world state is loading.
- [x] Verify valid `WORLD_DESTINATION_READY` renders the map shell.
- [x] Verify valid active `WORLD_STATE_SYNC` renders the map shell.
- [x] Verify invalid/missing world state shows generic child-safe error screen.
- [x] Verify no world REST call is added.
- [x] Run `npm run build` from `framework/frontend/app`.

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
- Review FEAT-011-World-Map..md
- Review FEAT-008-World-Module.md
- Review FEAT-007-Game-View-Shell.md
- Review websocket.json contract
- Confirm no REST world endpoints
- Add world payload types to api.ts
- Update GameView state machine to 'preparing | world_loading | world_active | error'
- Add world heartbeat management
- Add world loading timeout (10 seconds)
- Handle WORLD_DESTINATION_READY event
- Handle WORLD_STATE_SYNC event (ACTIVE status)
- Handle WORLD_ACTIVITY_STARTED event (no minigame rendering)
- Wire terminal events to enterErrorState()
- Implement generic child-safe error screen
- Add error audio once-per-error-entry
- Add discovery element click handler with contract validation
- Add discovery element CSS (whitish pulse per FEAT-011)
- Verify npm run build passes

incomplete_tasks:

contract_changes: none

learnings:
- SESSION_CONNECTED in doEventGame no longer transitions to 'ready' - world state controls rendering
- AUTH_ACK transitions to 'world_loading' and starts both heartbeat and world loading timer
- Valid world payload starts world_heartbeat and transitions to 'world_active'
- Invalid/timeout/close enters error state, plays error audio once
- WORLD_ACTIVITY_STARTED returns to loading state (no minigame)
- Discovery elements positioned on right side, host on left side
- Error audio same GAME_AVATAR_EVENT pattern - plays once per error entry

next_sprint_suggestions:
- FEAT-011 continues: add host visual rendering with real assets
- FEAT-011 continues: add actual discovery element visual assets
- FEAT-011 continues: add biome-based background theming
- FEAT-011 continues: prepare useAnimationStore for avatar states

## Implementation Details

### Files Modified

#### src/shared/types/api.ts
- Added world types: WorldHostPayload, WorldNarrativeSituationPayload, WorldDiscoveryElementPayload, WorldDestinationPayload, WorldStateSyncPayload, WorldActivityStartedPayload
- Extended SessionEventType with WORLD_DESTINATION_READY, WORLD_STATE_SYNC, WORLD_ACTIVITY_STARTED

#### src/views/GameView.vue
- Updated state machine: 'preparing' | 'world_loading' | 'world_active' | 'error'
- Added reactive state: worldState, hasPlayedErrorAudio
- Added timers: worldHeartbeatTimer, worldLoadingTimer
- Added WORLD_LOADING_TIMEOUT_MS = 10000 (10 seconds)
- Added world heartbeat management functions
- Added world loading timer functions
- Added enterErrorState() function
- Added isValidDestination() validation
- Added handleWorldDestinationReady() handler
- Added handleWorldStateSync() handler
- Added handleWorldActivityStarted() handler
- Added handleDiscoveryClick() for sending world_discovery_interacted
- Added getDiscoveryStyle() for positioning discovery elements
- Updated template: loader for preparing/world_loading, world_active shows host + discoveries, error shows avatar
- Added CSS: .game-view__host, .game-view__discovery, .game-view__discovery--has-activity, .game-view__error, .game-view__error-avatar
- Added @keyframes discovery-pulse

### State Machine

```
preparing → (ws open) → world_loading → (AUTH_ACK + timer)
                                      ↓
                         (valid world payload) → world_active
                         (invalid/timeout/close) → error → home
                         
world_active → (WORLD_ACTIVITY_STARTED) → world_loading
world_active → (terminal event / ws close) → error → home
```

### Build Verification
- npm run build: passed
- GameView CSS: 3.82 kB (up from 2.91 kB)
- GameView JS: 6.64 kB (up from 4.30 kB)
