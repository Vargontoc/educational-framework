# Sprint 018 - frontend
# -----------------------------------------------

## Goal

Implement the static Discovery Walk visual layout for `FEAT-011` inside `GameView`: horizontal meadow scenery, placeholder destination area, and avatar/NPC placeholder support without backend map data. Backend implementation is out of scope for this sprint.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md` before editing.
- [ ] Review Sprint 017 output before building on the clean GameView base.
- [ ] Review `docs/design/design_decisions_v1.docx` ADR-001, ADR-003, and ADR-005 if design context is needed.
- [ ] Review available frontend assets under `framework/frontend/app/src/assets`.
- [ ] Do not change backend code or backend contracts in this sprint.

### Static Discovery Walk Layout
- [ ] Build a full-viewport horizontal meadow scene inside GameView ready state.
- [ ] Keep sky and grass visual register aligned with existing design tokens.
- [ ] Add background, middle-ground, and foreground visual structure without implying levels.
- [ ] Add a placeholder destination area for the current host/NPC.
- [ ] Add an avatar or NPC placeholder using existing assets only.
- [ ] Ensure the placeholder can later be replaced by final assets without changing the main layout structure.
- [ ] Do not add final host animations.

### No Level Selector Semantics
- [ ] Do not render a path made of nodes or tiles.
- [ ] Do not render visible progress, locked state, completed state, or available state.
- [ ] Do not render engine icons or activity-type labels.
- [ ] Do not render arrows, mission markers, HUD signs, or urgent call-to-action indicators.

### Responsive Layout
- [ ] Keep the layout usable in tablet landscape.
- [ ] Keep the layout usable in mobile landscape.
- [ ] Preserve existing portrait rotation behavior.
- [ ] Avoid document scrollbars.
- [ ] Keep GameView fixed to the viewport.

### Verification
- [ ] Verify the layout visually reads as a walk, not a level selector.
- [ ] Verify the placeholder destination area is visible but not a checkpoint tile.
- [ ] Verify the avatar/NPC placeholder is visible and non-blocking.
- [ ] Verify no backend call is added for map content.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Accidental level-map appearance**: scenery could still feel like a progress route.
  Mitigation: avoid nodes, connected paths, numbers, engine icons, and locked/completed states.
- **Asset limitation**: final host assets do not exist yet.
  Mitigation: use placeholders and keep replacement boundaries clear.
- **Responsive drift**: full-screen scenery may break on mobile landscape.
  Mitigation: manually verify landscape sizes and preserve fixed viewport behavior.

## Dependencies

- Sprint 017 - clean World Map base.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - design tokens and child visual baseline.
- Existing assets under `framework/frontend/app/src/assets`.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not add new assets unless explicitly approved.
- Do not add Lottie or new animation dependencies.
- Keep the implementation static and local to GameView for this sprint.
- Keep all code, comments, and documentation in English.

## Notes

- This sprint intentionally does not implement world element interactions.
- This sprint intentionally does not implement automatic scroll movement.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
