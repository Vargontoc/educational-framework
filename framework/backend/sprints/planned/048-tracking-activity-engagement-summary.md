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

## Model Properties

### ActivityEngagementSummary

Read-only parental dashboard projection. It is descriptive, not diagnostic.

- `engineType`: String or enum, required.
- `startedCount`: long/integer, required, default `0`.
- `ignoredCount`: long/integer, required, default `0`.
- `completedCount`: long/integer, required, default `0`.
- `abandonedCount`: long/integer, required, default `0`.

### Response Shape

If exposed by REST, follow the existing tracking dashboard response wrapper pattern.

- `childProfileId`: Long, required in path or parent response context.
- `items`: List<ActivityEngagementSummary>, required, can be empty.

### Aggregation Rules

- `startedCount` comes from `ActivityProposalLog.outcome = STARTED`.
- `ignoredCount` comes from `ActivityProposalLog.outcome = IGNORED`.
- `completedCount` comes from `GameSessionSummary.finalStatus = COMPLETED`.
- `abandonedCount` comes from `GameSessionSummary.finalStatus = ABANDONED`.
- Group by content `Activity.engineType`.
- Do not produce recommendations, scores, warnings, or diagnostic labels.

## Tasks

### Dashboard Query
- [ ] Add read use case for activity engagement summary by child profile.
- [ ] Aggregate `STARTED` and `IGNORED` counts from `ActivityProposalLog`.
- [ ] Aggregate `COMPLETED` and `ABANDONED` counts from `GameSessionSummary`.
- [ ] Group results by activity `engineType`.
- [ ] Return `ActivityEngagementSummary` with the properties listed in `Model Properties`.
- [ ] Apply the aggregation rules listed in `Model Properties`.

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
