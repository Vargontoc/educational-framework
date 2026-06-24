# Sprint 019 - frontend
# -----------------------------------------------

## Goal

Add a lightweight `useAnimationStore` for avatar/NPC presentation state preparation and connect it minimally to GameView loading, world-ready, transition, and generic error states without introducing domain logic, final animations, Lottie, or backend dependencies.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `Frontend Animation Preparation`.
- [ ] Review existing Pinia stores in `framework/frontend/app/src/stores`.
- [ ] Review current GameView state transitions from preparing to ready.
- [ ] Review existing FEAT-010 avatar audio event handling in GameView.
- [ ] Do not change backend code or backend contracts in this sprint.

### Create Animation Store
- [ ] Create `framework/frontend/app/src/stores/useAnimationStore.ts`.
- [ ] Define a small visual state union: `idle`, `waiting`, `speaking`, `curious`, `celebrating`, `transitioning`, `error`.
- [ ] Store only presentation state, not domain state.
- [ ] Add simple actions to set state, mark speaking, stop speaking, interrupt, and reset.
- [ ] Keep the store non-persisted.
- [ ] Do not use localStorage or sessionStorage.

### Integrate With GameView
- [ ] Import `useAnimationStore` in GameView.
- [ ] Set `waiting` while GameView is preparing if it does not conflict with existing flow.
- [ ] Set `idle` when GameView reaches a valid backend world-ready state.
- [ ] Set `error` when GameView enters the generic child-safe error screen.
- [ ] Set `speaking` while an avatar event audio is being handled when the current code path allows it.
- [ ] Return to `idle` after avatar event playback, timeout, or failure only when world state remains active.
- [ ] Ensure backend-provided error audio can be associated with the error visual state without replaying on repeated renders.
- [ ] Reset or interrupt animation state on GameView unmount and terminal session events.
- [ ] Do not change the contracted WebSocket event names or payload parsing.

### UI Usage
- [ ] Apply a small CSS class or data attribute based on the animation state if useful for the placeholder.
- [ ] Keep visual behavior simple and CSS-based.
- [ ] Do not add Lottie or new runtime dependencies.
- [ ] Do not require final animation assets.

### Verification
- [ ] Verify GameView still enters the map after greeting behavior.
- [ ] Verify terminal session cleanup still works.
- [ ] Verify animation state does not persist after leaving GameView.
- [ ] Verify the error visual state can be entered once without looping error audio.
- [ ] Verify no auth, child session, or audio data is persisted.
- [ ] Run `npm run build` from `framework/frontend/app`.

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

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
