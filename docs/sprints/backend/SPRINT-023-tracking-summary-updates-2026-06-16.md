# Sprint 023 - backend
# -----------------------------------------------

## Goal
Update activity and topic summaries after each registered attempt so dashboard reads and topic selection do not need full attempt recomputation.

## Status
status: completed
started_at: 2026-06-16
closed_at: 2026-06-16
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [x] Create `ActivitySummaryRepository` under `tracking/ports/out`.
- [x] Create `TopicSummaryRepository` under `tracking/ports/out`.
- [x] Extend attempt registration flow to update summaries in the same transaction.
- [x] Create or update one `ActivitySummary` per `childProfileId` and `activityId`.
- [x] Create or update one `TopicSummary` per `childProfileId` and `topicId`.

### Summary Calculations
- [x] Increment `totalAttempts`.
- [x] Increment `totalCorrect` for `CORRECT` attempts.
- [x] Increment `totalIncorrect` for `INCORRECT` attempts.
- [x] Increment `totalTimeouts` for `TIMEOUT` attempts.
- [x] Recalculate `successRatePercent`.
- [x] Recalculate `failureRatePercent` for `TopicSummary`.
- [x] Recalculate `averageResponseTimeMs` when response time is present.
- [x] Set `TopicSummary.performanceBand` as `WEAK`, `MEDIUM`, or `STRONG`.

### Persistence
- [x] Create JPA entities for `ActivitySummary` and `TopicSummary`.
- [x] Create Spring Data repositories for both summaries.
- [x] Create persistence adapters for both summaries.

### Tests
- [x] Unit test first attempt creates activity summary.
- [x] Unit test first attempt creates topic summary.
- [x] Unit test second attempt updates existing summaries.
- [x] Unit test timeout increments timeout counters.
- [x] Unit test average response time is recalculated.
- [x] Unit test topic performance band is `WEAK` when failures are above 40%.
- [x] Unit test topic performance band is `MEDIUM` when failures are between 20% and 40%.
- [x] Unit test topic performance band is `STRONG` when failures are below 20%.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Summary updates must be transactional with attempt registration to avoid drift.
- Average response time can be wrong if null response times are counted as zero.

## Dependencies
- Sprint 022 completed.

## Agent Instruction
- Do not implement dashboard REST endpoints in this sprint.
- Do not implement adaptive difficulty in this sprint.
- Keep summary formulas simple and covered by unit tests.
- Do not recompute summaries from the full attempt table for every write.

## Notes
This sprint makes the write path maintain the aggregate read model incrementally.

## Review

completed_tasks:
- Created ActivitySummaryRepository port interface
- Created TopicSummaryRepository port interface
- Created ActivitySummaryJpaEntity (extends BaseEntity)
- Created TopicSummaryJpaEntity (extends BaseEntity)
- Created ActivitySummaryJpaRepository
- Created TopicSummaryJpaRepository
- Created ActivitySummaryPersistenceAdapter with toDomain/toJpa mappers
- Created TopicSummaryPersistenceAdapter with toDomain/toJpa mappers
- Created SummaryUpdateService with updateSummaries() method
- Modified ActivityAttemptService to call SummaryUpdateService after save
- Updated TrackingModuleConfiguration with new beans
- Created 17 unit tests (all passing):
  - ActivityAttemptServiceTest: 5 tests
  - SummaryUpdateServiceTest: 8 tests
  - ActivitySummaryPersistenceAdapterTest: 2 tests
  - TopicSummaryPersistenceAdapterTest: 2 tests

incomplete_tasks:
- None

contract_changes:
- None (internal use only)

learnings:
- Used incremental average formula: newAvg = oldAvg + (newValue - oldAvg) / count
- Performance band calculation based on failureRatePercent (not successRate)
- BigDecimal comparison in tests should use compareTo() instead of equals()

next_sprint_suggestions:
- Implement curiosity viewing registration (CuriosityViewed)
- Implement achievement detection after attempts
- Implement learning path progress tracking
- Implement adaptive difficulty based on performance bands
