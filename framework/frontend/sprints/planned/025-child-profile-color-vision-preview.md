# Sprint 025 - frontend
# -----------------------------------------------

## Goal

Add a simple, non-diagnostic visual preview to the child profile visual accessibility selector so parents can see the effect of the selected `colorVisionMode` without needing to understand technical terminology.

## Status

status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` design principles.
- [ ] Review Sprint 024 selector implementation before editing.
- [ ] Review backend seed reference values in FEAT-012 for sample colors and non-chromatic differentiators.
- [ ] Do not change child profile service contracts in this sprint.
- [ ] Do not implement recognition COLOR game/content rendering in this sprint.

### Preview UI
- [ ] Add a compact preview area below the selected visual accessibility option.
- [ ] Update preview immediately when the selected `colorVisionMode` changes.
- [ ] Show representative samples for `RED`, `BLUE`, `GREEN`, and `YELLOW`.
- [ ] Include non-chromatic differentiators for each sample, such as icon, shape, pattern, or text label.
- [ ] Ensure preview for `NONE` shows default colors.
- [ ] Ensure preview for `ACHROMATOPSIA` remains understandable without color.
- [ ] Keep preview clearly framed as an accessibility display aid, not a test.

### Data Source And Fallback
- [ ] Prefer accessible color metadata from an existing frontend catalog source if already available.
- [ ] If no catalog source exists yet, keep a small local preview-only mapping based on FEAT-012 seed reference values.
- [ ] Isolate any local preview fallback so it is not treated as content source of truth.
- [ ] Do not hardcode preview values into child profile services or global stores.

### Visual, Responsive, And Accessibility
- [ ] Ensure preview samples have text labels or aria labels.
- [ ] Ensure preview does not rely on color alone.
- [ ] Ensure preview layout does not make the edit modal overflow awkwardly on mobile.
- [ ] Keep adult panel visual language and avoid child GameView styling.
- [ ] Add all visible strings and aria labels to i18n.

### Testing And Verification
- [ ] Add or update component tests if a harness exists for the selector/modal.
- [ ] Verify each backend enum changes the preview.
- [ ] Verify `No estoy seguro` updates preview to `DEUTERANOMALY`.
- [ ] Verify preview has labels or non-chromatic differentiators for every sample.
- [ ] Verify preview does not alter submit payload semantics.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Preview mistaken for diagnosis**: samples could look like a test.
  Mitigation: keep explanatory copy non-diagnostic and do not ask the parent to score or validate perception.
- **Hardcoded content drift**: local preview values may diverge from backend seed data.
  Mitigation: isolate fallback as preview-only and prefer catalog metadata when available.
- **Modal clutter**: preview could make the edit modal too dense.
  Mitigation: keep preview compact and responsive.

## Dependencies

- Sprint 024 - child profile visual accessibility selector UI.
- `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` - source feature and seed reference.

## Agent Instruction

- Implement only the child profile visual preview.
- Do not implement backend changes.
- Do not change backend contracts.
- Do not implement recognition COLOR content rendering here.
- Keep preview fallback values local and clearly scoped if needed.
- Keep all visible strings and aria labels in i18n.
- Keep code, comments, and documentation in English.

## Notes

- This sprint improves parent understanding of the selected mode while keeping diagnosis and content rendering out of scope.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
- Sprint 026 - render COLOR recognition content using accessible catalog metadata when available.
