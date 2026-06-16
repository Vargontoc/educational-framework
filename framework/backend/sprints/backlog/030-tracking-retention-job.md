# Sprint 030 - backend
# -----------------------------------------------

## Goal
Delete `ActivityAttempt` records older than 180 days while preserving summaries, achievements, curiosity cycles, and learning progress.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Retention Job
- [ ] Create tracking retention job using the existing `AbstractRetentionJob` pattern if available.
- [ ] Hardcode retention to 180 days for v1.
- [ ] Delete only `ActivityAttempt` rows older than the cutoff.
- [ ] Keep `ActivitySummary` rows.
- [ ] Keep `TopicSummary` rows.
- [ ] Keep `ChildAchievement` rows.
- [ ] Keep `ChildLearningProgress` rows.
- [ ] Keep `ChildLearningCompletedStep` rows.
- [ ] Keep `CuriosityViewed` rows because reset is cycle-based, not time-based.
- [ ] Add structured logs with cutoff and deleted row count.

### Repository Support
- [ ] Add repository method to delete attempts older than a timestamp.
- [ ] Add repository method to count old attempts if useful for logging or tests.

### Tests
- [ ] Unit test cutoff is 180 days before job execution time.
- [ ] Unit test job calls attempt repository deletion.
- [ ] Persistence/integration test deletes old attempts.
- [ ] Persistence/integration test keeps recent attempts.
- [ ] Persistence/integration test summaries and progress rows are not deleted.

## Manual Tests
- Insert one old `ActivityAttempt` older than 180 days and one recent attempt.
- Trigger the job manually if the project has a simple way to run scheduled jobs in development.
- Confirm only the old attempt is deleted.
- Confirm summaries, achievements, and learning progress remain unchanged.

## Risks
- Deleting summaries would lose long-term progress.
- Making retention configurable now would add complexity that `FEAT-006` intentionally avoids.

## Dependencies
- Sprint 022 completed.
- Sprint 023 completed.

## Agent Instruction
- Do not archive attempts; delete them.
- Do not make retention configurable in this sprint.
- Do not delete any tracking aggregate or progress table.

## Notes
The app is single-family, so a fixed generous 180-day retention is acceptable for v1.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
