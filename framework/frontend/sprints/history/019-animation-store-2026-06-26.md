# Sprint 019 - frontend
# -----------------------------------------------

## Goal

Add a lightweight `useAnimationStore` for avatar/NPC presentation state preparation and connect it minimally to GameView loading, world-ready, transition, and generic error states without introducing domain logic, final animations, Lottie, or backend dependencies.

## Status

status: completed
started_at: 2026-06-26
closed_at: 2026-06-26
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `Frontend Animation Preparation`.
- [x] Review existing Pinia stores in `framework/frontend/app/src/stores`.
- [x] Review current GameView state transitions from preparing to ready.
- [x] Review existing FEAT-010 avatar audio event handling in GameView.
- [x] Do not change backend code or backend contracts in this sprint.

### Create Animation Store
- [x] Create `framework/frontend/app/src/stores/useAnimationStore.ts`.
- [x] Define a small visual state union: `idle`, `waiting`, `speaking`, `error`.
- [x] Store only presentation state, not domain state.
- [x] Add simple actions to set state, mark speaking, stop speaking, interrupt, and reset.
- [x] Keep the store non-persisted.
- [x] Do not use localStorage or sessionStorage.

### Integrate With GameView
- [x] Import `useAnimationStore` in GameView.
- [x] Set `waiting` while GameView is preparing if it does not conflict with existing flow.
- [x] Set `idle` when GameView reaches a valid backend world-ready state.
- [x] Set `error` when GameView enters the generic child-safe error screen.
- [x] Set `speaking` while an avatar event audio is being handled when the current code path allows it.
- [x] Return to `idle` after avatar event playback, timeout, or failure only when world state remains active.
- [x] Ensure backend-provided error audio can be associated with the error visual state without replaying on repeated renders.
- [x] Reset or interrupt animation state on GameView unmount and terminal session events.
- [x] Do not change the contracted WebSocket event names or payload parsing.

### UI Usage
- [x] Apply a small CSS class or data attribute based on the animation state if useful for the placeholder.
- [x] Keep visual behavior simple and CSS-based.
- [x] Do not add Lottie or new runtime dependencies.
- [x] Do not require final animation assets.

### Verification
- [x] Verify GameView still enters the map after greeting behavior.
- [x] Verify terminal session cleanup still works.
- [x] Verify animation state does not persist after leaving GameView.
- [x] Verify the error visual state can be entered once without looping error audio.
- [x] Verify no auth, child session, or audio data is persisted.
- [x] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Domain logic leakage**: the animation store may start deciding narrative or progression.
  Mitigation: store only visual state and expose only presentation actions.
- **FEAT-010 regression**: changing audio handling could break greeting/farewell flow.
  Mitigation: integrate around existing flow and keep event parsing untouched.
- **Premature animation dependency**: adding Lottie before assets exist creates unnecessary complexity.
  Mitigation: do not add animation libraries in this sprint.

## Dependencies

- Sprint 018 - static Discovery Walk layout.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md` - avatar audio lifecycle.
- Existing Pinia setup.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not add Lottie or new animation dependencies.
- Do not make `useAnimationStore` persisted.
- Do not move LearningPath, destination, host, activity, or engagement decisions into frontend state.
- Keep all code, comments, and documentation in English.

## Notes

- `useAnimationStore` is preparation for future animations, not a final animation system.
- The store does not decide whether world payloads are valid.
- Static placeholders remain acceptable after this sprint.

## Review

completed_tasks:
- Review FEAT-011-World-Map..md
- Review existing Pinia stores
- Review GameView state transitions
- Review FEAT-010 avatar audio handling
- Create useAnimationStore.ts with states: idle, waiting, speaking, error
- Add actions: setWaiting, setSpeaking, setIdle, setError, reset
- Integrate animationStore into GameView
- Set waiting on AUTH_ACK handler
- Set speaking before audioStore.playAudio, idle after
- Set idle on handleWorldDestinationReady success
- Set idle on handleWorldStateSync success
- Set waiting on handleWorldActivityStarted
- Set error on enterErrorState
- Reset on revokeSession and onUnmounted
- Add :data-animation-state attribute to template
- Verify npm run build passes

incomplete_tasks:

contract_changes: none

learnings:
- Minimal store with only states that have clear triggers: idle, waiting, speaking, error
- Speaking state set before audio plays, returns to idle only if world is still active
- Animation state reset on all cleanup paths (unmount, revokeSession)
- Data attribute on avatar elements allows future CSS-based state styling without new CSS in this sprint

next_sprint_suggestions:
- FEAT-011 continues: add CSS for animation states when assets are ready
- FEAT-011 continues: add curious/celebrating states when triggers exist
- FEAT-011 continues: connect discovery element interactions to animation state

## Implementation Details

### Files Created

#### src/stores/useAnimationStore.ts
- New Pinia store for avatar/NPC presentation state
- States: 'idle' | 'waiting' | 'speaking' | 'error'
- Actions: setWaiting, setSpeaking, setIdle, setError, reset
- Non-persisted (no localStorage)

### Files Modified

#### src/views/GameView.vue
- Added import: useAnimationStore
- Added instance: animationStore = useAnimationStore()
- Added setWaiting() on AUTH_ACK handler
- Added setWaiting() on handleWorldActivityStarted
- Added setSpeaking() before audioStore.playAudio
- Added setIdle() after audio resolves/rejects (if world still active)
- Added setIdle() on handleWorldDestinationReady success
- Added setIdle() on handleWorldStateSync success
- Added setError() on enterErrorState
- Added reset() on revokeSession
- Added reset() on onUnmounted
- Added :data-animation-state to loader avatar, host img, error avatar

### State Transitions

| From | To | Trigger |
|------|-----|---------|
| idle | waiting | onMounted, AUTH_ACK, WORLD_ACTIVITY_STARTED |
| waiting | speaking | before audioStore.playAudio |
| speaking | idle | after audio completes (if world_active) |
| idle | error | enterErrorState() |
| any | idle | revokeSession, onUnmounted |

### Build Verification
- npm run build: passed
- GameView JS: 7.48 kB (up from 6.64 kB)
- GameView CSS: 3.82 kB (unchanged)
