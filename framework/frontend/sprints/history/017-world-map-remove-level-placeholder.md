# Sprint 017 - frontend
# -----------------------------------------------

## Goal

Remove the discarded tile-based World Map placeholder from `GameView` and leave a clean loading/error-ready base for the backend-driven `FEAT-011 - World Map Discovery Walk`. Backend implementation is out of scope for this sprint.

## Status

status: completed
started_at: 2026-06-24
closed_at: 2026-06-24
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md` before editing.
- [x] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for existing GameView shell constraints.
- [x] Review `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` for current GameView audio lifecycle behavior.
- [x] Review current `framework/frontend/app/src/views/GameView.vue` before changing markup.
- [x] Do not change backend code or backend contracts in this sprint.

### Remove Level Placeholder Visuals
- [x] Remove the current visual path based on `done`, `available`, and `locked` nodes from `GameView`.
- [x] Remove CSS classes that only support the discarded path/nodes if they are no longer used.
- [x] Ensure GameView does not render `locked`, `completed`, or `available` visual states.
- [x] Ensure GameView does not render engine icons, level tiles, numbered levels, unlock stars, or progress percentages.
- [x] Keep the existing full-viewport child-facing GameView shell.

### Preserve Existing Lifecycle
- [x] Preserve the existing route guard behavior for `/game/:childId`.
- [x] Preserve current WebSocket open, heartbeat, message, cleanup, and terminal event behavior.
- [x] Preserve `GAME_AVATAR_EVENT` greeting/farewell behavior from FEAT-010.
- [x] Preserve the preparing/loading state transition behavior.
- [x] Do not introduce new backend data requirements.

### Minimal Replacement State
- [x] Render only the existing child-facing loading state after removing the tile placeholder unless valid backend world state is already handled by a later sprint.
- [x] Do not add a provisional map, meadow fallback, local host, or local discovery elements in this sprint.
- [x] Keep current child visual register.
- [x] Keep visible copy sparse and child-friendly.
- [x] Keep all visible copy and aria labels in i18n if any copy changes.

### Verification
- [x] Verify GameView renders without level-selector visuals.
- [x] Verify no local fallback World Map is introduced.
- [x] Verify no child-facing technical errors appear.
- [x] Verify no document scrollbars are introduced.
- [x] Verify desktop/tablet landscape and mobile landscape manually.
- [x] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Regression in FEAT-010 audio lifecycle**: removing placeholder markup could accidentally alter preparing/ready flow.
  Mitigation: do not change WebSocket/audio logic except where strictly necessary for rendering.
- **Visual ambiguity**: removing nodes may leave GameView in loading until later world rendering work.
  Mitigation: keep the child-safe loading base and do not invent map content.
- **Scope creep**: this sprint may start implementing backend world rendering.
  Mitigation: only remove discarded placeholder and create a clean base.

## Dependencies

- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` - GameView shell baseline.
- `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` - current avatar audio lifecycle.
- `docs/contracts/api/websocket.json` - existing GameView WebSocket contract.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not implement final world elements, minigame launch, or backend-driven map data in this sprint.
- Do not render a provisional local World Map.
- Do not add Lottie or new animation dependencies.
- Keep child session state in memory only.
- Keep all code, comments, and documentation in English.

## Notes

- The tile-based World Map design is explicitly discarded by FEAT-011.
- This sprint prepares the view for later backend-driven Discovery Walk work.

## Review

completed_tasks:
- Review FEAT-011-World-Map..md
- Review FEAT-007-Game-View-Shell.md
- Review FEAT-010-Greetings-And-Farwell-Event.md
- Review current GameView.vue
- Remove companion placeholder from template
- Remove destination placeholder from template
- Remove companion CSS styles
- Remove destination CSS styles
- Remove destination-pulse keyframe
- Verify npm run build succeeds
- Preserved route guard behavior
- Preserved WebSocket lifecycle
- Preserved GAME_AVATAR_EVENT behavior
- Preserved preparing/loading state transition

incomplete_tasks:

contract_changes: none

learnings:
- Tile-based path/nodes/levels were visual placeholders, not part of the shell
- Shell elements (clouds, hills, grass, bushes) are declarative scenery and were preserved
- Companion and destination were the core tile-based World Map placeholders being discarded

next_sprint_suggestions:
- FEAT-011 implementation for backend-driven World Map rendering

## Implementation Details

### Files Modified
- `framework/frontend/app/src/views/GameView.vue`

### Template Changes (lines 224-244 removed)
- Removed `<div class="game-view__companion">` block - avatar placeholder
- Removed `<div class="game-view__destination">` block - pulsing destination marker

### CSS Changes (lines 410-434 removed)
- Removed `.game-view__companion` styles
- Removed `.game-view__companion-img` styles
- Removed `.game-view__destination` styles
- Removed `@keyframes destination-pulse`

### Preserved Shell Elements
- `.game-view__cloud` - decorative clouds with drift animation
- `.game-view__bg-layer` / `.game-view__hills` - distant hills
- `.game-view__mg-layer` / `.game-view__grass-strip` - grass ground
- `.game-view__bush` - decorative bushes

### Build Verification
- `npm run build` passed successfully
- GameView CSS bundle: 2.91 kB (reduced from previous)
- GameView JS bundle: 4.30 kB
