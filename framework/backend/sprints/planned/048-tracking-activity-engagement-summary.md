# Sprint 048 - backend
# -----------------------------------------------

## Goal
Add the parental dashboard read model `ActivityEngagementSummary` using `ActivityProposalLog` and `GameSessionSummary`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Dashboard Query
- [ ] Add read use case for activity engagement summary by child profile.
- [ ] Aggregate `STARTED` and `IGNORED` counts from `ActivityProposalLog`.
- [ ] Aggregate `COMPLETED` and `ABANDONED` counts from `GameSessionSummary`.
- [ ] Group results by activity `engineType`.
- [ ] Return `engineType`, `startedCount`, `ignoredCount`, `completedCount`, and `abandonedCount`.

### REST Contract
- [ ] Add read-only dashboard endpoint if consistent with existing tracking dashboard pattern.
- [ ] Update `docs/contracts/api/openapi.json` if an endpoint is added.
- [ ] Ensure no write endpoint is exposed.

### Tests
- [ ] Unit test aggregation with started/ignored proposals.
- [ ] Unit test aggregation with completed/abandoned sessions.
- [ ] Unit test empty data returns zero/empty summary consistently.
- [ ] Integration test endpoint authorization if REST endpoint is added.

## Manual Tests
- Start backend locally.
- Seed or create proposal/session summary rows.
- Call the dashboard endpoint if implemented.
- Verify counts by `engineType` match the test data.

## Risks
- Joining content for `engineType` can introduce direct persistence coupling if not done through accepted patterns.
- Dashboard wording must remain descriptive, not diagnostic.

## Dependencies
- Sprint 047 completed.
- Sprint 032 completed.
- Sprint 029 tracking dashboard API completed.

## Agent Instruction
- Keep this as a read-only dashboard sprint.
- Do not add recommendations, scores, labels, or diagnoses.
- Do not expose child-facing data.

## Notes
This query helps parents observe started/ignored/completed/abandoned activity patterns without interpreting them automatically.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
