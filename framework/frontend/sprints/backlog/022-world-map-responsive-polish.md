# Sprint 022 - frontend
# -----------------------------------------------

## Goal

Polish FEAT-011 frontend behavior, responsive layout, accessibility, and acceptance checks so the World Map Discovery Walk is ready for backend-driven map data in a later feature.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md` end to end.
- [ ] Review Sprint 017 through Sprint 021 outputs.
- [ ] Review `docs/design/design_decisions_v1.docx` ADR-001, ADR-002, ADR-003, ADR-004, and ADR-005 if design context is needed.
- [ ] Review existing GameView route, WebSocket, audio, and cleanup behavior.
- [ ] Do not change backend code or backend contracts in this sprint.

### FEAT-011 Acceptance Pass
- [ ] Confirm GameView reads as a horizontal discovery walk.
- [ ] Confirm no level tiles, nodes, numbered levels, progress percentages, unlock stars, or visible locked elements remain.
- [ ] Confirm no engine icons or activity-type visual labels are shown on the map.
- [ ] Confirm discovery elements use visual-first organic signaling.
- [ ] Confirm the experience remains understandable when audio is unavailable or muted.
- [ ] Confirm avatar/NPC placeholder states are presentation-only.
- [ ] Confirm frontend does not infer child ability, interest, fatigue, or learning difficulty.

### Responsive And Accessibility Polish
- [ ] Verify tablet landscape layout manually.
- [ ] Verify mobile landscape layout manually.
- [ ] Verify portrait rotation overlay behavior is preserved.
- [ ] Verify GameView does not introduce document scrollbars.
- [ ] Verify all touch targets for interactive world elements are at least `64px`.
- [ ] Verify interactive text, if any, is at least `20px`.
- [ ] Verify visible states are not color-only.
- [ ] Verify sustained uppercase labels are not used.
- [ ] Verify child-facing technical errors are not shown.

### Code Quality And Cleanup
- [ ] Remove unused CSS left from earlier placeholders.
- [ ] Remove unused imports, refs, timers, and helper functions.
- [ ] Keep GameView logic readable for a junior developer.
- [ ] Keep functions small and local unless reuse is clear.
- [ ] Add short comments only where behavior would otherwise be unclear.
- [ ] Do not introduce global listeners unless already required by existing GameView behavior.

### Testing And Verification
- [ ] Add or update minimal tests if a test harness already exists for the touched area.
- [ ] Verify direct `/game/:childId` access still redirects without in-memory child session.
- [ ] Verify GameView still opens WebSocket after valid child session.
- [ ] Verify greeting/farewell avatar event behavior still follows FEAT-010.
- [ ] Verify terminal session events still clean up and navigate Home.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Late visual regressions**: polishing may reintroduce level-map semantics.
  Mitigation: use FEAT-011 acceptance pass as the final checklist.
- **Accessibility gaps**: world elements may be too small or color-dependent.
  Mitigation: verify touch sizes and non-color cues manually.
- **Lifecycle regressions**: visual cleanup may accidentally alter WebSocket/audio flows.
  Mitigation: verify FEAT-010 and terminal session behavior after cleanup.

## Dependencies

- Sprint 017 - remove level placeholder.
- Sprint 018 - static Discovery Walk layout.
- Sprint 019 - animation store preparation.
- Sprint 020 - world element layers.
- Sprint 021 - interaction pause behavior.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` - avatar audio lifecycle.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not add new features beyond FEAT-011 polish and verification.
- Do not add backend-driven destination, host, activity, or LearningPath logic.
- Do not persist engagement data.
- Keep all code, comments, and documentation in English.

## Notes

- After this sprint, frontend should be ready for a future contracted backend map-data feature.
- Any missing backend map data must be reported as a future contract requirement, not solved locally.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
