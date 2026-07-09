# Sprint 023 - frontend
# -----------------------------------------------

## Goal

Align frontend child profile types, services, and payload handling with backend `colorVisionMode` so FEAT-012 can be implemented without UI or rendering scope creep.

## Status

status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` end to end.
- [ ] Review existing Children section and child profile edit flow before editing.
- [ ] Review existing child profile API service/store/types.
- [ ] Review backend enum names from `ColorVisionMode` and do not rename them in frontend payloads.
- [ ] Do not implement visual selector UI in this sprint.
- [ ] Do not implement accessible COLOR content rendering in this sprint.

### Contract And Types
- [ ] Add or update frontend `ColorVisionMode` typing with exact backend values: `NONE`, `PROTANOPIA`, `DEUTERANOMALY`, `DEUTERANOPIA`, `TRITANOPIA`, `ACHROMATOPSIA`.
- [ ] Ensure `ChildProfileResponse` includes required `colorVisionMode`.
- [ ] Ensure create child request can send optional `colorVisionMode` without breaking existing create flows.
- [ ] Ensure update child request can send `colorVisionMode` when provided.
- [ ] Preserve existing child profile values when the edit form does not change `colorVisionMode`.
- [ ] Do not map backend enums to alternate internal enum names.

### Service And Store Alignment
- [ ] Update child profile service methods to pass through `colorVisionMode` unchanged.
- [ ] Update child profile store/state hydration to keep `colorVisionMode` from backend responses.
- [ ] Ensure missing legacy `colorVisionMode` data defaults safely to `NONE` only at the frontend boundary if needed for rendering.
- [ ] Avoid broad model rewrites outside child profile request/response handling.

### Testing And Verification
- [ ] Add or update a minimal service/store test if a test harness exists for child profile API handling.
- [ ] Verify update payload can include each supported `colorVisionMode` enum value unchanged.
- [ ] Verify create payload remains valid when `colorVisionMode` is omitted.
- [ ] Verify existing Children section still loads child profiles.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Enum drift**: frontend could invent labels or values that backend rejects.
  Mitigation: use exact backend enum values and keep labels out of contract types.
- **Regression in child profile edit**: changing shared request models may break existing TTS/agent/name/birthday updates.
  Mitigation: limit changes to typed request/response handling and verify existing update flow.
- **Premature UI coupling**: service changes may bake in product labels.
  Mitigation: keep this sprint contract-only.

## Dependencies

- `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` - source feature.
- `framework/backend/sprints/history/062-child-profile-color-vision-mode-2026-07-07.md` - backend profile contract.
- Existing frontend Children section and child profile edit implementation.

## Agent Instruction

- Implement only frontend contract/type/service alignment for `colorVisionMode`.
- Do not implement backend changes.
- Do not change backend contracts.
- Do not implement the visual accessibility selector UI yet.
- Do not hardcode accessible color seed values in services.
- Keep all visible strings and aria labels in i18n if any are touched.
- Keep code, comments, and documentation in English.

## Notes

- This sprint should leave the app ready for the UI sprint to consume `colorVisionMode` safely.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
- Sprint 024 - render visual accessibility selector in the child profile edit modal.
