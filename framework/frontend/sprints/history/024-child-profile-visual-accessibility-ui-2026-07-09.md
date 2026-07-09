# Sprint 024 - frontend
# -----------------------------------------------

## Goal

Implement the FEAT-012 adult-facing visual accessibility selector in the child profile edit modal, rendering backend `colorVisionMode` values with descriptive UI labels and persisting the selected enum.

## Status

status: completed
started_at: 2026-07-09
closed_at: 2026-07-09
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [x] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` before editing.
- [x] Review Sprint 023 output and confirm `colorVisionMode` is available in child profile types/services.
- [x] Review existing child edit modal layout, validation, submit, and i18n patterns.
- [x] Do not implement preview swatches in this sprint beyond the selector itself.
- [x] Do not implement recognition COLOR content rendering in this sprint.

### Selector UI
- [x] Add a visual accessibility toggle to the child profile edit modal.
- [x] Render toggle off when `colorVisionMode` is `NONE`.
- [x] Render toggle on when `colorVisionMode` is not `NONE`.
- [x] Toggle off must select `NONE`.
- [x] Toggle on must show selectable backend modes distinct from `NONE`.
- [x] Render each mode with a descriptive main label and technical subtitle.
- [x] Add a non-diagnostic helper text: `Este ajuste no sustituye una valoracion oftalmologica`.
- [x] Add a `No estoy seguro` action that selects `DEUTERANOMALY`.
- [x] Do not use clinical language as the primary label.

### Persistence Flow
- [x] Initialize selector state from `ChildProfileResponse.colorVisionMode`.
- [x] Include selected `colorVisionMode` in child profile update payload.
- [x] Preserve existing name, birthday, avatar, TTS, and agent update behavior.
- [x] On successful save, refresh or update local child profile state with backend response.
- [x] On failed save, preserve the form state and show existing adult-facing error handling.

### Visual, Responsive, And Accessibility
- [x] Keep layout aligned with the adult Parent Control panel visual language.
- [x] Add all visible labels, helper text, and aria labels to i18n.
- [x] Ensure selector options are keyboard operable.
- [x] Ensure selected state is not represented by color alone.
- [x] Ensure touch targets are at least 44px.
- [x] Verify modal remains usable on tablet landscape, mobile landscape, and portrait overlay contexts.

### Testing And Verification
- [x] Add or update component tests if a harness exists for the child edit modal.
- [x] Verify `NONE` renders as toggle off.
- [x] Verify each non-`NONE` backend value renders as a selectable option.
- [x] Verify `No estoy seguro` selects `DEUTERANOMALY`.
- [x] Verify saving sends the exact backend enum value.
- [x] Verify existing TTS/agent/name/birthday submit behavior still works.
- [x] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Medical framing**: labels could read like diagnosis.
  Mitigation: use descriptive product labels and keep technical terms secondary.
- **Form regression**: adding selector state may break existing profile fields.
  Mitigation: keep selector state local to the edit form and verify existing fields.
- **Backend enum mismatch**: UI labels may accidentally send labels instead of enum values.
  Mitigation: separate display labels from submitted `colorVisionMode` values.

## Dependencies

- Sprint 023 - child profile color vision contract.
- `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` - source feature.
- Existing Children section child edit modal.

## Agent Instruction

- Implement only the child profile visual accessibility selector UI and persistence.
- Do not implement backend changes.
- Do not change backend contracts.
- Do not implement preview swatches or COLOR catalog rendering in this sprint.
- Keep all visible strings and aria labels in i18n.
- Keep code, comments, and documentation in English.

## Notes

- This sprint should make the parent able to configure and save `colorVisionMode` from the child profile edit modal.

## Review

completed_tasks:
- Add colorVisionMode ref and colorVisionModes array to EditChildModal.vue
- Add toggle to control NONE vs non-NONE mode
- Add radio group selector with 5 backend enum values
- Add CSS shape icons (circle, triangle, square, diamond, gray pattern)
- Add helper text and "No estoy seguro" action
- Add 13 new i18n strings in es.ts
- Update watchers and submit payload to include colorVisionMode
- Verify npm run build passes

incomplete_tasks:

contract_changes:
- src/i18n/es.ts: Add 13 new color vision strings
- src/components/panel/EditChildModal.vue: Add selector UI, state, and submit handling

learnings:
- Toggle can control the selected mode directly (NONE vs DEUTERANOMALY for toggle on)
- CSS shapes with distinct forms satisfy the "no usar unicamente color" requirement
- Radio group with labels provides good accessibility without custom component

next_sprint_suggestions:
- Sprint 025 - add a visual preview for the selected color vision mode.

## Implementation Details

### Files Modified

#### src/i18n/es.ts
Added 13 new strings under `panel.children.editModal`:
- colorVisionLabel, colorVisionHelper, colorVisionNotSure, colorVisionNotSureHint
- colorVisionNoneLabel, colorVisionNoneSubtitle
- colorVisionProtanopiaLabel, colorVisionProtanopiaSubtitle
- colorVisionDeuteranomalyLabel, colorVisionDeuteranomalySubtitle
- colorVisionDeuteranopiaLabel, colorVisionDeuteranopiaSubtitle
- colorVisionTritanopiaLabel, colorVisionTritanopiaSubtitle
- colorVisionAchromatopsiaLabel, colorVisionAchromatopsiaSubtitle

#### src/components/panel/EditChildModal.vue
- Added ColorVisionMode import
- Added colorVisionMode ref and colorVisionModes constant array
- Updated watchers to sync colorVisionMode
- Updated submit payload to include colorVisionMode
- Added color-vision-field section with toggle, helper text, selector, and not-sure button
- Added CSS styles for shapes and selector layout

### Design Details

| Element | Implementation |
|---------|---------------|
| Toggle | Controls NONE vs DEUTERANOMALY when turned on |
| Selector | Radio group with 5 mode options |
| Shape icons | CSS-only (circle, triangle, square, diamond, gray pattern) |
| Accessibility | Native radio inputs with visible focus styles |
| Touch targets | All interactive elements meet 44px minimum |

### Build Verification
- npm run build: passed
- PanelControlView CSS: 30.31 kB (up from 27.61 kB)
- PanelControlView JS: 37.84 kB (up from 35.24 kB)

## Notes

- This sprint makes the parent able to configure and save `colorVisionMode` from the child profile edit modal.
- Sprint 025 will add a visual preview showing color samples for the selected mode.
