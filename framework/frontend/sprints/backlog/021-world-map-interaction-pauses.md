# Sprint 021 - frontend
# -----------------------------------------------

## Goal

Implement frontend-only local interaction pause behavior for World Map elements so the walk temporarily responds to child exploration without recording engagement or changing backend progression.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `Walk Pace Control` and `Interaction Pause`.
- [ ] Review Sprint 020 world element implementation.
- [ ] Review existing GameView cleanup patterns before adding timers.
- [ ] Do not change backend code or backend contracts in this sprint.

### Local Walk Movement
- [ ] Add a simple local walk movement or scrolling effect if not already present.
- [ ] Keep movement slow and non-urgent.
- [ ] Ensure movement does not imply a progress path.
- [ ] Ensure movement can be paused and resumed.

### Interaction Pause Behavior
- [ ] Pause local walk movement when the child taps a simple interactive element.
- [ ] Pause local walk movement when the child taps a discovery element.
- [ ] Play a brief local visual reaction for the tapped element.
- [ ] Start a short exploration timer after each interaction.
- [ ] Restart the timer if the child interacts again during the exploration window.
- [ ] Resume local walk movement automatically when the timer expires.
- [ ] Clear timers on GameView unmount and terminal session events.

### Constraints
- [ ] Do not send interaction pause events to backend.
- [ ] Do not persist interaction pause data.
- [ ] Do not infer child interest from repeated taps.
- [ ] Do not infer lack of interest from ignored elements.
- [ ] Do not launch minigames.
- [ ] Do not block system events or navigation while a local pause is active.

### Visual Feedback
- [ ] Keep reactions immediate and brief.
- [ ] Keep reactions non-punitive and non-diagnostic.
- [ ] Avoid red error states in GameView.
- [ ] Ensure reactions remain usable without audio.
- [ ] Keep touch areas at least `64px`.

### Verification
- [ ] Verify tapping an element pauses movement.
- [ ] Verify repeated taps restart the exploration timer.
- [ ] Verify movement resumes automatically after no new interaction.
- [ ] Verify timers are cleared when leaving GameView.
- [ ] Verify terminal session events are not delayed by local pause behavior.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Timer leaks**: interaction timers may survive navigation.
  Mitigation: centralize cleanup on unmount and terminal events.
- **Backend responsibility drift**: local pauses may become engagement tracking.
  Mitigation: do not send, store, or interpret interaction pause data.
- **Blocked navigation**: pause behavior may delay terminal session events.
  Mitigation: terminal events must interrupt local pause immediately.

## Dependencies

- Sprint 020 - world element layers.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- Existing GameView lifecycle and cleanup behavior.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not record or persist engagement data.
- Do not infer learning, ability, interest, fatigue, or difficulty from interactions.
- Do not launch minigames.
- Keep all code, comments, and documentation in English.

## Notes

- This sprint implements visual responsiveness only.
- Backend remains responsible for LearningPath, destination, content selection, and tracking.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
