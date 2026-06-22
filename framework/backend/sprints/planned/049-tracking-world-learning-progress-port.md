# Sprint 049 - backend
# -----------------------------------------------

## Goal
Confirm or complete the tracking port that lets `world` advance a `LearningPathStep` after the narrative arrival sequence completes.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Port Review
- [ ] Review existing learning progress use cases from Sprint 027.
- [ ] Confirm whether `registerLearningPathStepProgress(childProfileId, learningPathId, learningPathStepId)` already exists.
- [ ] If missing, add the use case as an internal tracking port.
- [ ] Ensure it updates `ChildLearningProgress`.
- [ ] Ensure it records `ChildLearningCompletedStep` without duplicates.
- [ ] Return the updated learning progress state.

### Tests
- [ ] Unit test advancing to a new step.
- [ ] Unit test duplicate completed step is not inserted.
- [ ] Unit test invalid child/path/step is rejected.
- [ ] Unit test operation is idempotent where appropriate.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
