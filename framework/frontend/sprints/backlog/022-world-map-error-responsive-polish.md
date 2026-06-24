# Sprint 022 - frontend
# -----------------------------------------------

## Goal

Polish FEAT-011 backend-driven World Map behavior, generic child-safe error screen, responsive layout, accessibility, and acceptance checks so the feature is ready for future minigame rendering.

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
- [ ] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for session lifecycle and viewport rules.
- [ ] Review `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` for backend-provided avatar audio behavior.
- [ ] Review `docs/contracts/api/websocket.json` for final event/message alignment.
- [ ] Do not change backend code or backend contracts in this sprint.

### FEAT-011 Acceptance Pass
- [ ] Confirm GameView does not render a map while backend world state is loading.
- [ ] Confirm GameView does not render a local fallback map when backend world state is unavailable.
- [ ] Confirm valid `WORLD_DESTINATION_READY` renders the map.
- [ ] Confirm valid active `WORLD_STATE_SYNC` renders the map.
- [ ] Confirm invalid/missing/no world state renders the generic child-safe error screen.
- [ ] Confirm no level tiles, nodes, numbered levels, progress percentages, unlock stars, engine icons, or visible locked elements remain.
- [ ] Confirm backend discovery elements show the whitish pulse cue.
- [ ] Confirm frontend does not fabricate destination, host, biome, discovery elements, or proposal ids.

### Generic Child-Safe Error Screen Polish
- [ ] Ensure the error screen uses the existing avatar placeholder image.
- [ ] Ensure the error screen has calm child-facing layout and no technical text.
- [ ] Ensure no map is shown behind or inside the error state.
- [ ] Ensure backend-provided `GAME_AVATAR_EVENT` error audio is played at most once per error entry.
- [ ] Ensure heartbeat, world heartbeat, reconnect attempts, duplicated errors, or repeated renders do not replay the same error audio.
- [ ] Keep the error screen reusable for future vertical orientation handling.

### Responsive And Accessibility Polish
- [ ] Verify tablet landscape layout manually.
- [ ] Verify mobile landscape layout manually.
- [ ] Verify portrait rotation overlay behavior is preserved or compatible with the generic child-safe error pattern.
- [ ] Verify GameView does not introduce document scrollbars.
- [ ] Verify all touch targets for backend discovery elements are at least `64px`.
- [ ] Verify interactive text, if any, is at least `20px`.
- [ ] Verify visible states are not color-only.
- [ ] Verify sustained uppercase labels are not used.
- [ ] Verify child-facing technical errors are not shown.

### Code Quality And Cleanup
- [ ] Remove unused CSS left from earlier tile placeholders.
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
- [ ] Verify world heartbeat cleanup in all terminal/error/unmount paths.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Fallback map regression**: polishing may reintroduce local world content.
  Mitigation: use FEAT-011 acceptance pass as final checklist.
- **Error audio loop**: repeated events or renders may replay the error audio.
  Mitigation: track one playback per error-state entry.
- **Lifecycle regressions**: visual cleanup may accidentally alter WebSocket/audio flows.
  Mitigation: verify FEAT-007, FEAT-010, and terminal session behavior after cleanup.

## Dependencies

- Sprint 017 - remove level placeholder.
- Sprint 018 - backend world loading and render shell.
- Sprint 019 - animation store preparation.
- Sprint 020 - backend world elements rendering.
- Sprint 021 - world heartbeat and discovery interaction.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` - avatar audio lifecycle.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not add new features beyond FEAT-011 polish and verification.
- Do not add backend-driven destination, host, activity, or LearningPath logic.
- Do not persist engagement data.
- Do not render a local fallback World Map.
- Keep all code, comments, and documentation in English.

## Notes

- After this sprint, frontend should be ready for a future minigame-rendering feature.
- Any missing backend world data must produce the generic child-safe error screen, not local fallback content.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
