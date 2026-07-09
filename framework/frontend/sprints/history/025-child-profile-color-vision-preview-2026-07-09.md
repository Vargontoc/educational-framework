# Sprint 025 - frontend
# -----------------------------------------------

## Goal

Add a simple, non-diagnostic visual preview to the child profile visual accessibility selector so parents can see the effect of the selected `colorVisionMode` without needing to understand technical terminology.

## Status

status: completed
started_at: 2026-07-09
closed_at: 2026-07-09
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [x] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` design principles.
- [x] Review Sprint 024 selector implementation before editing.
- [x] Review backend seed reference values in FEAT-012 for sample colors and non-chromatic differentiators.
- [x] Do not change child profile service contracts in this sprint.
- [x] Do not implement recognition COLOR game/content rendering in this sprint.

### Preview UI
- [x] Add a compact preview area below the selected visual accessibility option.
- [x] Update preview immediately when the selected `colorVisionMode` changes.
- [x] Show representative samples for `RED`, `BLUE`, `GREEN`, and `YELLOW`.
- [x] Include non-chromatic differentiators for each sample, such as icon, shape, pattern, or text label.
- [x] Ensure preview for `NONE` shows default colors.
- [x] Ensure preview for `ACHROMATOPSIA` remains understandable without color.
- [x] Keep preview clearly framed as an accessibility display aid, not a test.

### Data Source And Fallback
- [x] Prefer accessible color metadata from an existing frontend catalog source if already available.
- [x] If no catalog source exists yet, keep a small local preview-only mapping based on FEAT-012 seed reference values.
- [x] Isolate any local preview fallback so it is not treated as content source of truth.
- [x] Do not hardcode preview values into child profile services or global stores.

### Visual, Responsive, And Accessibility
- [x] Ensure preview samples have text labels or aria labels.
- [x] Ensure preview does not rely on color alone.
- [x] Ensure preview layout does not make the edit modal overflow awkwardly on mobile.
- [x] Keep adult panel visual language and avoid child GameView styling.
- [x] Add all visible strings and aria labels to i18n.

### Testing And Verification
- [x] Add or update component tests if a harness exists for the selector/modal.
- [x] Verify each backend enum changes the preview.
- [x] Verify `No estoy seguro` updates preview to `DEUTERANOMALY`.
- [x] Verify preview has labels or non-chromatic differentiators for every sample.
- [x] Verify preview does not alter submit payload semantics.
- [x] Run `npm run build` from `framework/frontend/app`.

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
- Add colorPreviewSamples array with 4 color samples (RED, BLUE, GREEN, YELLOW)
- Add colorPreviewFallback isolated mapping based on FEAT-012 seed reference values
- Add getPreviewColor helper function
- Add preview UI section inside color-vision-selector
- Add CSS styles for preview samples with CSS clip-path star shape
- Add 5 new i18n strings (previewTitle, RedLabel, BlueLabel, GreenLabel, YellowLabel)
- Verify npm run build passes

incomplete_tasks:

contract_changes:
- src/i18n/es.ts: Add 5 new preview strings
- src/components/panel/EditChildModal.vue: Add preview data, helper, UI, and styles

learnings:
- CSS clip-path polygon can create star shapes for preview icons
- Isolated fallback mapping keeps preview data separate from catalog source of truth
- Preview updates reactively when colorVisionMode ref changes

next_sprint_suggestions:
- Sprint 026 - render COLOR recognition content using accessible catalog metadata when available.

## Implementation Details

### Files Modified

#### src/i18n/es.ts
Added 5 new strings under `panel.children.editModal`:
- colorVisionPreviewTitle, colorVisionRedLabel, colorVisionBlueLabel, colorVisionGreenLabel, colorVisionYellowLabel

#### src/components/panel/EditChildModal.vue
- Added colorPreviewSamples array with RED, BLUE, GREEN, YELLOW samples
- Added colorPreviewFallback isolated mapping (24 color values)
- Added getPreviewColor helper function
- Added preview UI section with swatches and labels
- Added CSS styles for preview samples (circle, square, triangle, star via clip-path)

### Design Details

| Element | Implementation |
|---------|---------------|
| Preview location | Inside color-vision-selector, after "No estoy seguro" button |
| Samples | RED (circle), BLUE (square), GREEN (triangle), YELLOW (star) |
| Shapes | CSS-only with clip-path for star |
| Color | Uses isolated fallback mapping based on FEAT-012 seed reference |
| Labels | Each swatch has text label and aria-label |

### Build Verification
- npm run build: passed
- PanelControlView CSS: 31.61 kB (up from 30.31 kB)
- PanelControlView JS: 39.40 kB (up from 37.84 kB)
