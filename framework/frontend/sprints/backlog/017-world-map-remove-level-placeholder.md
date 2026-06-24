# Sprint 017 - frontend
# -----------------------------------------------

## Goal

Remove the discarded tile-based World Map placeholder from `GameView` and leave a clean loading/error-ready base for the backend-driven `FEAT-011 - World Map Discovery Walk`. Backend implementation is out of scope for this sprint.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md` before editing.
- [ ] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for existing GameView shell constraints.
- [ ] Review `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` for current GameView audio lifecycle behavior.
- [ ] Review current `framework/frontend/app/src/views/GameView.vue` before changing markup.
- [ ] Do not change backend code or backend contracts in this sprint.

### Remove Level Placeholder Visuals
- [ ] Remove the current visual path based on `done`, `available`, and `locked` nodes from `GameView`.
- [ ] Remove CSS classes that only support the discarded path/nodes if they are no longer used.
- [ ] Ensure GameView does not render `locked`, `completed`, or `available` visual states.
- [ ] Ensure GameView does not render engine icons, level tiles, numbered levels, unlock stars, or progress percentages.
- [ ] Keep the existing full-viewport child-facing GameView shell.

### Preserve Existing Lifecycle
- [ ] Preserve the existing route guard behavior for `/game/:childId`.
- [ ] Preserve current WebSocket open, heartbeat, message, cleanup, and terminal event behavior.
- [ ] Preserve `GAME_AVATAR_EVENT` greeting/farewell behavior from FEAT-010.
- [ ] Preserve the preparing/loading state transition behavior.
- [ ] Do not introduce new backend data requirements.

### Minimal Replacement State
- [ ] Render only the existing child-facing loading state after removing the tile placeholder unless valid backend world state is already handled by a later sprint.
- [ ] Do not add a provisional map, meadow fallback, local host, or local discovery elements in this sprint.
- [ ] Keep current child visual register.
- [ ] Keep visible copy sparse and child-friendly.
- [ ] Keep all visible copy and aria labels in i18n if any copy changes.

### Verification
- [ ] Verify GameView renders without level-selector visuals.
- [ ] Verify no local fallback World Map is introduced.
- [ ] Verify no child-facing technical errors appear.
- [ ] Verify no document scrollbars are introduced.
- [ ] Verify desktop/tablet landscape and mobile landscape manually.
- [ ] Run `npm run build` from `framework/frontend/app`.

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

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
