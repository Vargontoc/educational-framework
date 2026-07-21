# Sprint 057 - backend
# -----------------------------------------------

## Goal
Listen to game completion events and advance LearningPath only after world narrative arrival has completed.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Tasks

### Event Listener
- [x] Add listener for `GameSessionCompletedEvent` or equivalent.
- [x] Match events to active `WorldState` by `childSessionId` and activity/proposal context.
- [x] If `finalStatus == COMPLETED`, mark destination ready for arrival narrative completion.
- [x] If `finalStatus == ABANDONED`, continue the walk without advancing LearningPath.

### LearningPath Advancement
- [x] Add world use case to mark narrative arrival sequence complete.
- [x] Call tracking `registerLearningPathStepProgress(...)` only after narrative completion.
- [x] Do not advance LearningPath directly on raw `GAME_COMPLETED` event.

### Tests
- [x] Unit test completed game does not immediately advance progress.
- [x] Unit test narrative completion advances LearningPath.
- [x] Unit test abandoned game does not advance LearningPath.
- [x] Unit test event for unrelated session is ignored.
- [x] Unit test duplicate completion does not advance twice.

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
This sprint preserves FEAT-011'"'"'s rule that the child experiences arrival and celebration, not a visible level completion.

## Review

completed_tasks:
- Created `WorldNarrativeCompletionStatus` enum with values: NO_PENDING, AWAITING_NARRATIVE, NARRATIVE_COMPLETE
- Created `WorldNarrativeCompletionResult` model with childSessionId, learningPathId, learningPathStepId, progressAdvanced, status
- Updated `WorldState` with `narrativeCompletionStatus` field (default NO_PENDING)
- Created `WorldGameCompletionListener` using Spring @EventListener for GameSessionCompletedEvent
  - COMPLETED event sets status to AWAITING_NARRATIVE
  - ABANDONED event sets status to NO_PENDING
- Created `WorldNarrativeCompletionUseCase` port interface
- Created `WorldNarrativeCompletionService` implementing the use case
  - Only advances LearningPath when status is AWAITING_NARRATIVE
  - Uses childProfileId from WorldState
  - Resets status to NO_PENDING after advancement
- Updated `WorldModuleConfiguration` to wire new service
- Created 8 unit tests (3 for listener, 5 for service)
- All 786 tests pass

incomplete_tasks:
- None

contract_changes:
- None (internal world event handling only)

learnings:
- Scalable status enum design allows future extension (timer, external event, frontend confirmation)
- Event listener is optional from game perspective - game does not know if anyone listens
- WorldState narrativeCompletionStatus ensures LearningPath only advances after narrative completion

next_sprint_suggestions:
- Sprint 058: World heartbeat inactivity