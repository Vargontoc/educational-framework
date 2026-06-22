# Sprint 057 - backend
# -----------------------------------------------

## Goal
Listen to game completion events and advance LearningPath only after world narrative arrival has completed.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Event Listener
- [ ] Add listener for `GameSessionCompletedEvent` or equivalent.
- [ ] Match events to active `WorldState` by `childSessionId` and activity/proposal context.
- [ ] If `finalStatus == COMPLETED`, mark destination ready for arrival narrative completion.
- [ ] If `finalStatus == ABANDONED`, continue the walk without advancing LearningPath.

### LearningPath Advancement
- [ ] Add world use case to mark narrative arrival sequence complete.
- [ ] Call tracking `registerLearningPathStepProgress(...)` only after narrative completion.
- [ ] Do not advance LearningPath directly on raw `GAME_COMPLETED` event.

### Tests
- [ ] Unit test completed game does not immediately advance progress.
- [ ] Unit test narrative completion advances LearningPath.
- [ ] Unit test abandoned game does not advance LearningPath.
- [ ] Unit test event for unrelated session is ignored.
- [ ] Unit test duplicate completion does not advance twice.

## Manual Tests
- Complete a fake game launched from world.
- Trigger or simulate narrative arrival completion.
- Confirm LearningPath advances only after the narrative step.

## Risks
- Advancing on raw game completion can make the backend progression diverge from the child narrative experience.
- Duplicate events could advance multiple steps if not guarded.

## Dependencies
- Sprint 049 completed.
- Sprint 050 completed.
- Sprint 056 completed.

## Agent Instruction
- Keep world as the owner of the decision moment, tracking as the owner of persistence.
- Do not expose LearningPath progress to child-facing payloads.
- Ensure listener remains optional from game perspective.

## Notes
This sprint preserves FEAT-011's rule that the child experiences arrival and celebration, not a visible level completion.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
