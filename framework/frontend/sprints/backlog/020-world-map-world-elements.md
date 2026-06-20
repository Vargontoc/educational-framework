# Sprint 020 - frontend
# -----------------------------------------------

## Goal

Add frontend-only World Map visual elements for the three FEAT-011 layers: living world, simple interactive elements, and discovery elements with visual-first organic signaling. Backend implementation is out of scope for this sprint.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `World Elements` and `Visual Philosophy`.
- [ ] Review Sprint 018 static layout output.
- [ ] Review Sprint 019 animation store output if available.
- [ ] Review existing CSS token names before adding styles.
- [ ] Do not change backend code or backend contracts in this sprint.

### Living World Layer
- [ ] Add decorative living world elements such as clouds, flowers, grass, birds, or leaves using CSS and existing assets only.
- [ ] Ensure living world elements move slowly and predictably.
- [ ] Ensure living world elements do not look tappable.
- [ ] Ensure living world elements do not react to touch.
- [ ] Ensure living world elements do not emit glow or call-to-action signals.

### Simple Interactive Elements
- [ ] Add a small set of simple interactive placeholder elements.
- [ ] Give simple interactive elements very soft idle movement or no idle movement.
- [ ] Ensure they do not start minigames.
- [ ] Ensure they do not affect progression.
- [ ] Keep touch areas at least `64px`.
- [ ] Add immediate local visual reaction on tap only if it is simple and non-blocking.

### Discovery Elements
- [ ] Add one or more discovery element placeholders.
- [ ] Apply a breathing glow as the primary autonomous visual signal.
- [ ] Ensure discovery elements do not look like UI buttons.
- [ ] Ensure discovery interaction is optional.
- [ ] Do not launch minigames in this sprint.
- [ ] Do not encode engine type or activity type in the visual appearance.

### Accessibility And UX Constraints
- [ ] Keep visible copy sparse or absent in child World Map elements.
- [ ] Do not use audio as the only signal.
- [ ] Do not rely only on color for interaction state.
- [ ] Avoid urgent motion, giant arrows, mission markers, HUD signs, and level-like indicators.
- [ ] Keep animations interruptible and non-blocking.

### Verification
- [ ] Verify the three layers are visually distinguishable but not explicitly labeled to the child.
- [ ] Verify discovery elements are identifiable without audio.
- [ ] Verify simple interactive elements do not look like discovery elements.
- [ ] Verify no minigame starts from any element.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Elements look like buttons**: placeholders may feel like UI controls instead of world objects.
  Mitigation: use organic shapes, soft movement, and no button styling.
- **Too much motion**: decorative elements may compete with discovery signals.
  Mitigation: keep living world movement slow, peripheral, and predictable.
- **Scope creep into gameplay**: discovery elements may start launching activities.
  Mitigation: keep all reactions local and frontend-only in this sprint.

## Dependencies

- Sprint 018 - static Discovery Walk layout.
- Sprint 019 - optional animation store preparation.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- Existing design tokens and assets.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not add minigame launch behavior.
- Do not add backend-driven map content.
- Do not persist interaction or engagement data.
- Do not infer child interest, ability, fatigue, or learning state.
- Keep all code, comments, and documentation in English.

## Notes

- Discovery elements are visual entry points only in this sprint.
- Backend will later decide which activity a discovery element contains.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
