# Sprint 049 - backend
# -----------------------------------------------

## Goal
Confirm or complete the tracking port that lets `world` advance a `LearningPathStep` after the narrative arrival sequence completes.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Tasks

### Port Review
- [x] Review existing learning progress use cases from Sprint 027.
- [x] Confirm whether `registerLearningPathStepProgress(childProfileId, learningPathId, learningPathStepId)` already exists.
- [x] If missing, add the use case as an internal tracking port.
- [x] Ensure it updates `ChildLearningProgress`.
- [x] Ensure it records `ChildLearningCompletedStep` without duplicates.
- [x] Return the updated learning progress state.

### Tests
- [x] Unit test advancing to a new step.
- [x] Unit test duplicate completed step is not inserted.
- [x] Unit test invalid child/path/step is rejected.
- [x] Unit test operation is idempotent where appropriate.

## Manual Tests
- Optional: use a test fixture to advance a learning path step.
- Confirm the child progress row and completed step row are updated.

## Risks
- Advancing LearningPath on `GAME_COMPLETED` directly would violate FEAT-008; world decides after narrative completion.
- Duplicating completed steps would corrupt dashboard progress.

## Dependencies
- Sprint 027 tracking learning progress completed.
- FEAT-008 World Module.

## Agent Instruction
- Keep this sprint tracking-only.
- Do not implement world event listener yet.
- Do not expose a child-facing progress endpoint.

## Notes
This sprint may be a small confirmation/hardening sprint if the required port already exists.

## Review

completed_tasks:
- Created RegisterLearningPathStepProgressUseCase interface
- Added LearningPathStepRepository dependency to ChildLearningProgressService
- Implemented registerLearningPathStepProgress method with combined logic:
  - Validates learningPathId and learningPathStepId (including step belongs to path)
  - Creates or updates ChildLearningProgress
  - Records ChildLearningCompletedStep idempotently
  - Returns ChildLearningProgressResponse with updated state
- Updated TrackingModuleConfiguration with new bean
- Created RegisterLearningPathStepProgressUseCaseTest with 8 unit tests
- All 730 tests pass

incomplete_tasks:
- None

contract_changes:
- None (internal tracking port only)

learnings:
- Single combined port is better than multiple calls from world
- Step validation must check step belongs to the specific learning path
- Idempotency handled by checking for existing completed step before saving

next_sprint_suggestions:
- Sprint 050: Game session completed event