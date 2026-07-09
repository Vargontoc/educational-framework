# Sprint 026 - frontend
# -----------------------------------------------

## Goal

Use accessible color catalog metadata when rendering COLOR recognition content so visual values and non-chromatic differentiators match the active child `colorVisionMode`.

## Status

status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-012-Vision-Accesibility-Child-Profile.md` backend seed reference values.
- [ ] Review `docs/product/features/backend/FEAT-009-Recognition-Engine.md` accessibility notes if needed.
- [ ] Review frontend recognition/content catalog consumption before editing.
- [ ] Review existing child session/profile state access in GameView or recognition flow.
- [ ] Do not implement backend endpoints or seed changes.

### Catalog Metadata Support
- [ ] Identify the frontend source for recognition COLOR content metadata.
- [ ] Support accessible color fields when provided: `conceptualIdentity`, `labelKey`, `shapeIcon`, `symbol`, and palettes by `colorVisionMode`.
- [ ] Resolve the active child `colorVisionMode` from child profile/session state.
- [ ] Select the matching accessible palette for the active `colorVisionMode`.
- [ ] Fall back safely to `NONE` palette if a specific mode palette is missing.
- [ ] Do not fabricate catalog items when backend content is unavailable.

### COLOR Rendering
- [ ] Render conceptual color identity separately from visual color value.
- [ ] Apply `accessibleColorValue` for visual display when available.
- [ ] Render a non-chromatic differentiator such as `shapeIcon`, `symbol`, or accessible label.
- [ ] Ensure COLOR options are not distinguishable by color alone.
- [ ] Keep rendering generic enough to work with additional colors beyond current seed values.

### Error And Empty States
- [ ] If accessible metadata is missing, render a safe fallback that still includes a text/shape differentiator.
- [ ] Avoid child-facing technical error copy.
- [ ] Log or surface adult/dev diagnostics only through existing project patterns.
- [ ] Do not block non-COLOR recognition categories.

### Testing And Verification
- [ ] Add or update tests for palette resolution by `colorVisionMode` if a harness exists.
- [ ] Verify `NONE`, `DEUTERANOMALY`, `TRITANOPIA`, and `ACHROMATOPSIA` render visibly different or non-color-dependent outputs.
- [ ] Verify missing palette falls back to `NONE` without crashing.
- [ ] Verify non-COLOR recognition rendering is unchanged.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Endpoint availability**: frontend may not yet have a catalog endpoint exposing accessible color metadata.
  Mitigation: stop and document the blocker rather than inventing content APIs.
- **Color-only regression**: rendering may still rely on swatch color.
  Mitigation: always include non-chromatic differentiators.
- **Session/profile mismatch**: active child mode may not be available in the recognition route.
  Mitigation: use existing child profile/session state and fall back to `NONE` safely.

## Dependencies

- Sprint 023 - child profile color vision contract.
- Sprint 024 - visual accessibility selector persistence.
- Sprint 025 - preview fallback patterns.
- `framework/backend/sprints/history/063-recognition-content-accessible-catalog-2026-07-09.md` - backend catalog support.
- Existing frontend recognition/content rendering flow.

## Agent Instruction

- Implement only frontend rendering support for accessible COLOR content metadata.
- Do not implement backend changes.
- Do not change backend contracts.
- Do not create fake catalog endpoints.
- Do not hardcode seed data as the source of truth for game content.
- Keep child-facing rendering safe and non-technical.
- Keep code, comments, and documentation in English.

## Notes

- If no frontend-accessible catalog metadata exists, document the blocker in sprint review and keep code changes minimal.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
- Sprint 027 - complete FEAT-012 accessibility, responsive, and acceptance pass.
