# Sprint 024 - frontend
# -----------------------------------------------

## Goal

Implement the FEAT-012 adult-facing visual accessibility selector in the child profile edit modal, rendering backend `colorVisionMode` values with descriptive UI labels and persisting the selected enum.

## Status

status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` before editing.
- [ ] Review Sprint 023 output and confirm `colorVisionMode` is available in child profile types/services.
- [ ] Review existing child edit modal layout, validation, submit, and i18n patterns.
- [ ] Do not implement preview swatches in this sprint beyond the selector itself.
- [ ] Do not implement recognition COLOR content rendering in this sprint.

### Selector UI
- [ ] Add a visual accessibility toggle to the child profile edit modal.
- [ ] Render toggle off when `colorVisionMode` is `NONE`.
- [ ] Render toggle on when `colorVisionMode` is not `NONE`.
- [ ] Toggle off must select `NONE`.
- [ ] Toggle on must show selectable backend modes distinct from `NONE`.
- [ ] Render each mode with a descriptive main label and technical subtitle.
- [ ] Add a non-diagnostic helper text: `Este ajuste no sustituye una valoracion oftalmologica`.
- [ ] Add a `No estoy seguro` action that selects `DEUTERANOMALY`.
- [ ] Do not use clinical language as the primary label.

### Persistence Flow
- [ ] Initialize selector state from `ChildProfileResponse.colorVisionMode`.
- [ ] Include selected `colorVisionMode` in child profile update payload.
- [ ] Preserve existing name, birthday, avatar, TTS, and agent update behavior.
- [ ] On successful save, refresh or update local child profile state with backend response.
- [ ] On failed save, preserve the form state and show existing adult-facing error handling.

### Visual, Responsive, And Accessibility
- [ ] Keep layout aligned with the adult Parent Control panel visual language.
- [ ] Add all visible labels, helper text, and aria labels to i18n.
- [ ] Ensure selector options are keyboard operable.
- [ ] Ensure selected state is not represented by color alone.
- [ ] Ensure touch targets are at least 44px.
- [ ] Verify modal remains usable on tablet landscape, mobile landscape, and portrait overlay contexts.

### Testing And Verification
- [ ] Add or update component tests if a harness exists for the child edit modal.
- [ ] Verify `NONE` renders as toggle off.
- [ ] Verify each non-`NONE` backend value renders as a selectable option.
- [ ] Verify `No estoy seguro` selects `DEUTERANOMALY`.
- [ ] Verify saving sends the exact backend enum value.
- [ ] Verify existing TTS/agent/name/birthday submit behavior still works.
- [ ] Run `npm run build` from `framework/frontend/app`.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
- Sprint 025 - add a visual preview for the selected color vision mode.
