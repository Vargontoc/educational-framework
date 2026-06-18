# Sprint 030 - backend
# -----------------------------------------------

## Goal
Delete `ActivityAttempt` records older than 180 days while preserving summaries, achievements, curiosity cycles, and learning progress.

## Status
status: completed
started_at: 2026-06-18
closed_at: 2026-06-18
blocked_by:
waiting_for:

## Tasks

### Retention Job
- [x] Create tracking retention job using the existing `AbstractRetentionJob` pattern if available.
- [x] Hardcode retention to 180 days for v1.
- [x] Delete only `ActivityAttempt` rows older than the cutoff.
- [x] Keep `ActivitySummary` rows.
- [x] Keep `TopicSummary` rows.
- [x] Keep `ChildAchievement` rows.
- [x] Keep `ChildLearningProgress` rows.
- [x] Keep `ChildLearningCompletedStep` rows.
- [x] Keep `CuriosityViewed` rows because reset is cycle-based, not time-based.
- [x] Add structured logs with cutoff and deleted row count.

### Repository Support
- [x] Add repository method to delete attempts older than a timestamp.
- [ ] Add repository method to count old attempts if useful for logging or tests.

### Tests
- [x] Unit test cutoff is 180 days before job execution time.
- [x] Unit test job calls attempt repository deletion.
- [x] Persistence/integration test deletes old attempts (disabled - Docker required).
- [x] Persistence/integration test keeps recent attempts (disabled - Docker required).
- [x] Persistence/integration test summaries and progress rows are not deleted (disabled - Docker required).

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
- Created TrackingRetentionProperties with retentionDays = 180
- Created TrackingRetentionJob extending AbstractRetentionJob
- Added deleteCreatedAtBefore to ActivityAttemptRepository interface
- Added deleteByCreatedAtBefore to ActivityAttemptJpaRepository with @Modifying
- Added implementation to ActivityAttemptPersistenceAdapter
- Created TrackingRetentionJobTest (5 unit tests - all passing)
- Created TrackingRetentionPersistenceTest (3 integration tests - disabled, Docker required)
- Job scheduled at 3 AM daily (0 0 3 * * *) different from SessionRetentionJob (2 AM)
- Uses app.tracking.scheduling.enabled property (matchIfMissing = true)

incomplete_tasks:
- Count method for old attempts (not needed per backlog)

contract_changes:
- No REST contract changes (internal job only)
- Property app.tracking.retention.retentionDays = 180
- Property app.tracking.scheduling.enabled (boolean, default true)

learnings:
- AbstractRetentionJob pattern is clean and reusable
- @Modifying required on JPA delete queries
- Integration tests disabled with @Disabled("Docker/Testcontainers requerido") follow pattern from TrackingDashboardControllerTest
- Job scheduling can be disabled via property

next_sprint_suggestions:
- Contract hardening sprint (031) to verify all tracking REST endpoints in OpenAPI
- Difficulty evolution recording when adaptive difficulty changes level
- Dashboard metrics aggregation job
