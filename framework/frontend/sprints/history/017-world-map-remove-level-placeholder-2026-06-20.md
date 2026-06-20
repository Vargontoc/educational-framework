# Sprint 017 - frontend
# -----------------------------------------------

## Goal

Remove the discarded tile-based World Map placeholder from `GameView` and leave a clean frontend base for `FEAT-011 - World Map Discovery Walk`. Backend implementation is out of scope for this sprint.

## Status

status: completed
started_at: 2026-06-20
closed_at: 2026-06-20
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
- [x] Preserve the preparing/ready state transition behavior.
- [x] Do not introduce new backend data requirements.

### Minimal Replacement State
- [x] Render a simple empty World Map base after preparation.
- [x] Keep current sky/grass child visual register.
- [x] Keep visible copy sparse and child-friendly.
- [x] Keep all visible copy and aria labels in i18n if any copy changes.

### Verification
- [x] Verify GameView renders without level-selector visuals.
- [x] Verify no child-facing technical errors appear.
- [x] Verify no document scrollbars are introduced.
- [x] Verify desktop/tablet landscape and mobile landscape manually.
- [x] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Regression in FEAT-010 audio lifecycle**: removing placeholder markup could accidentally alter preparing/ready flow.
  Mitigation: do not change WebSocket/audio logic except where strictly necessary for rendering.
- **Visual ambiguity**: removing nodes may leave GameView looking empty.
  Mitigation: keep a minimal World Map base that is intentionally non-level-based.
- **Scope creep**: this sprint may start implementing full world elements.
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
- Do not add Lottie or new animation dependencies.
- Keep child session state in memory only.
- Keep all code, comments, and documentation in English.

## Notes

- The tile-based World Map design is explicitly discarded by FEAT-011.
- This sprint prepares the view for later Discovery Walk work.

## Review

completed_tasks: All tasks completed. Removed path/nodes markup, removed orphaned CSS classes (.game-view__path, .game-view__node*, .game-view__card, @keyframes call-attention), fixed aria-labelledby reference, prefixed unused hasAudio with underscore.

incomplete_tasks:

contract_changes: none

learnings: The unused hasAudio parameter in doEventGame was a pre-existing TypeScript error surfaced by the build. Keeping markup/CSS removal strictly separated from script logic confirmed no FEAT-010 regression risk.

next_sprint_suggestions: Sprint 018 - Static Discovery Walk layout