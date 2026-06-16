# Sprint 023 - backend
# -----------------------------------------------

## Goal
Update activity and topic summaries after each registered attempt so dashboard reads and topic selection do not need full attempt recomputation.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [ ] Create `ActivitySummaryRepository` under `tracking/ports/out`.
- [ ] Create `TopicSummaryRepository` under `tracking/ports/out`.
- [ ] Extend attempt registration flow to update summaries in the same transaction.
- [ ] Create or update one `ActivitySummary` per `childProfileId` and `activityId`.
- [ ] Create or update one `TopicSummary` per `childProfileId` and `topicId`.

### Summary Calculations
- [ ] Increment `totalAttempts`.
- [ ] Increment `totalCorrect` for `CORRECT` attempts.
- [ ] Increment `totalIncorrect` for `INCORRECT` attempts.
- [ ] Increment `totalTimeouts` for `TIMEOUT` attempts.
- [ ] Recalculate `successRatePercent`.
- [ ] Recalculate `failureRatePercent` for `TopicSummary`.
- [ ] Recalculate `averageResponseTimeMs` when response time is present.
- [ ] Set `TopicSummary.performanceBand` as `WEAK`, `MEDIUM`, or `STRONG`.

### Persistence
- [ ] Create JPA entities for `ActivitySummary` and `TopicSummary`.
- [ ] Create Spring Data repositories for both summaries.
- [ ] Create persistence adapters for both summaries.

### Tests
- [ ] Unit test first attempt creates activity summary.
- [ ] Unit test first attempt creates topic summary.
- [ ] Unit test second attempt updates existing summaries.
- [ ] Unit test timeout increments timeout counters.
- [ ] Unit test average response time is recalculated.
- [ ] Unit test topic performance band is `WEAK` when failures are above 40%.
- [ ] Unit test topic performance band is `MEDIUM` when failures are between 20% and 40%.
- [ ] Unit test topic performance band is `STRONG` when failures are below 20%.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
