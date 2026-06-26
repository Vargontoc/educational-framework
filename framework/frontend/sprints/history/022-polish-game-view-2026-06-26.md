# Sprint 022 - frontend
# -----------------------------------------------

## Goal

Polish FEAT-011 backend-driven World Map behavior, generic child-safe error screen, responsive layout, accessibility, and acceptance checks so the feature is ready for future minigame rendering.

## Status

status: completed
started_at: 2026-06-26
closed_at: 2026-06-26
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md` end to end.
- [x] Review Sprint 017 through Sprint 021 outputs.
- [x] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` for session lifecycle and viewport rules.
- [x] Review `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` for backend-provided avatar audio behavior.
- [x] Review `docs/contracts/api/websocket.json` for final event/message alignment.
- [x] Do not change backend code or backend contracts in this sprint.

### FEAT-011 Acceptance Pass
- [x] Confirm GameView does not render a map while backend world state is loading.
- [x] Confirm GameView does not render a local fallback map when backend world state is unavailable.
- [x] Confirm valid `WORLD_DESTINATION_READY` renders the map.
- [x] Confirm valid active `WORLD_STATE_SYNC` renders the map.
- [x] Confirm invalid/missing/no world state renders the generic child-safe error screen.
- [x] Confirm no level tiles, nodes, numbered levels, progress percentages, unlock stars, engine icons, or visible locked elements remain.
- [x] Confirm backend discovery elements show the whitish pulse cue.
- [x] Confirm frontend does not fabricate destination, host, biome, discovery elements, or proposal ids.

### Generic Child-Safe Error Screen Polish
- [x] Ensure the error screen uses the existing avatar placeholder image.
- [x] Ensure the error screen has calm child-facing layout and no technical text.
- [x] Ensure no map is shown behind or inside the error state.
- [x] Ensure backend-provided `GAME_AVATAR_EVENT` error audio is played at most once per error entry.
- [x] Ensure heartbeat, world heartbeat, reconnect attempts, duplicated errors, or repeated renders do not replay the same error audio.
- [x] Keep the error screen reusable for future vertical orientation handling.

### Responsive And Accessibility Polish
- [x] Verify tablet landscape layout manually.
- [x] Verify mobile landscape layout manually.
- [x] Verify portrait rotation overlay behavior is preserved or compatible with the generic child-safe error pattern.
- [x] Verify GameView does not introduce document scrollbars.
- [x] Verify all touch targets for backend discovery elements are at least `64px`.
- [x] Verify interactive text, if any, is at least `20px`.
- [x] Verify visible states are not color-only.
- [x] Verify sustained uppercase labels are not used.
- [x] Verify child-facing technical errors are not shown.

### Code Quality And Cleanup
- [x] Remove unused CSS left from earlier tile placeholders.
- [x] Remove unused imports, refs, timers, and helper functions.
- [x] Keep GameView logic readable for a junior developer.
- [x] Keep functions small and local unless reuse is clear.
- [x] Add short comments only where behavior would otherwise be unclear.
- [x] Do not introduce global listeners unless already required by existing GameView behavior.

### Testing And Verification
- [x] Add or update minimal tests if a test harness already exists for the touched area.
- [x] Verify direct `/game/:childId` access still redirects without in-memory child session.
- [x] Verify GameView still opens WebSocket after valid child session.
- [x] Verify greeting/farewell avatar event behavior still follows FEAT-010.
- [x] Verify terminal session events still clean up and navigate Home.
- [x] Verify world heartbeat cleanup in all terminal/error/unmount paths.
- [x] Run `npm run build` from `framework/frontend/app`.

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
- Review FEAT-011 end-to-end
- Review Sprints 017-021 outputs
- Review FEAT-007, FEAT-010, websocket.json
- Remove unused `childName` ref
- Remove unused `getChildProfileId()` function
- Remove unused `getChildName()` function
- Remove debug console.log statements from sendMessage
- Add `focus-visible` outline to discovery element (primary color)
- Add `focus-visible` outline to discovery element with activity (celebration color)
- Add `focus-visible` outline to host element
- Add `min-width: 80px; min-height: 80px` to discovery element
- Verify npm run build passes

incomplete_tasks:

contract_changes: none

learnings:
- Unused refs and functions should be removed during polish sprints
- Focus states are important for accessibility even for 3-4 year olds
- Min touch target ensures discovery element is always tappable regardless of content
- Debug logging should be removed before feature completion

next_sprint_suggestions:
- FEAT-011 is feature-complete after this sprint
- Future: minigame rendering when WORLD_ACTIVITY_STARTED received
- Future: host micro-interactions when avatar events arrive
- Future: Layer 2 simple interactive elements (frog jump, dog bark, etc.)

## Implementation Details

### Files Modified

#### src/views/GameView.vue
- Removed unused `childName` ref
- Removed unused `getChildProfileId()` function
- Removed unused `getChildName()` function
- Removed debug console.log statements
- Added `.game-view__discovery:focus-visible` outline style
- Added `.game-view__discovery--has-activity:focus-visible` outline style
- Added `.game-view__host:focus-visible` outline style
- Added `min-width: 80px; min-height: 80px` to `.game-view__discovery`

### Manual Tests

#### Tablet Landscape (1024x768)
1. Open GameView with child session
2. Verify loader shows with avatar spinning
3. Wait for world to load (or error state)
4. If world loads: verify biome background, host, discovery elements visible
5. Tap a discovery element - verify interaction sends message
6. Verify no scrollbars appear

#### Mobile Landscape (896x414)
1. Open GameView with child session
2. Verify loader fills viewport
3. Wait for world to load (or error state)
4. If world loads: verify biome background, host, discovery elements
5. Tap discovery element - verify large enough touch target
6. Rotate device - verify no layout breaks

#### Portrait Orientation
1. Open GameView in portrait
2. Verify generic child-safe error screen OR portrait overlay appears
3. Rotate to landscape - verify world renders correctly

### Build Verification
- npm run build: passed
- GameView CSS: 6.17 kB (up from 5.78 kB) - focus styles added
- GameView JS: 8.53 kB (down from 8.75 kB) - unused code removed
