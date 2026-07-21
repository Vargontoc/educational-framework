# Sprint 048 - backend
# -----------------------------------------------

## Goal
Add the parental dashboard read model `ActivityEngagementSummary` using `ActivityProposalLog` and `GameSessionSummary`.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Model Properties

### ActivityEngagementSummary

Read-only parental dashboard projection. It is descriptive, not diagnostic.

- `gameEngineType`: String, required.
- `startedCount`: long, required, default `0`.
- `ignoredCount`: long, required, default `0`.
- `completedCount`: long, required, default `0`.
- `abandonedCount`: long, required, default `0`.

### Response Shape

- `childProfileId`: Long, required.
- `items`: List<ActivityEngagementSummary>, required, can be empty.

### Aggregation Rules

- `startedCount` comes from `ActivityProposalLog.outcome = STARTED`.
- `ignoredCount` comes from `ActivityProposalLog.outcome = IGNORED`.
- `completedCount` comes from `GameSessionSummary.finalStatus = COMPLETED`.
- `abandonedCount` comes from `GameSessionSummary.finalStatus = ABANDONED`.
- Group by content `Activity.gameEngineType`.
- Do not produce recommendations, scores, warnings, or diagnostic labels.

## Tasks

### Dashboard Query
- [x] Add read use case for activity engagement summary by child profile.
- [x] Aggregate `STARTED` and `IGNORED` counts from `ActivityProposalLog`.
- [x] Aggregate `COMPLETED` and `ABANDONED` counts from `GameSessionSummary`.
- [x] Group results by activity `gameEngineType`.
- [x] Return `ActivityEngagementSummary` with the properties listed in `Model Properties`.
- [x] Apply the aggregation rules listed in `Model Properties`.

### REST Contract
- [ ] Add read-only dashboard endpoint if consistent with existing tracking dashboard pattern.
- [ ] Update `docs/contracts/api/openapi.json` if an endpoint is added.
- [ ] Ensure no write endpoint is exposed.

### Tests
- [x] Unit test aggregation with started/ignored proposals.
- [x] Unit test aggregation with completed/abandoned sessions.
- [x] Unit test empty data returns zero/empty summary consistently.
- [ ] Integration test endpoint authorization if REST endpoint is added.

## Manual Tests
- Start backend locally.
- Seed or create proposal/session summary rows.
- Call the dashboard endpoint if implemented.
- Verify counts by `gameEngineType` match the test data.

## Risks
- Joining content for `gameEngineType` can introduce direct persistence coupling if not done through accepted patterns.
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
- Created ActivityEngagementSummary record
- Created ActivityEngagementSummaryResult record
- Created ActivityInformationPort cross-module port in tracking
- Implemented ActivityInformationPortImpl in content module
- Added findByChildProfileId to ActivityProposalLogRepository and adapter
- Added findByChildProfileId to GameSessionSummaryRepository and adapter
- Created GetActivityEngagementSummaryUseCase interface
- Created ActivityEngagementSummaryService with aggregation logic
- Updated TrackingModuleConfiguration with use case bean
- Updated ContentModuleConfiguration with ActivityInformationPort bean
- Created ActivityEngagementSummaryServiceTest with 6 unit tests
- All 722 tests pass

incomplete_tasks:
- REST endpoint not implemented (no controller added)
- Integration test not implemented

contract_changes:
- None (internal ports only)

learnings:
- Cross-module ports maintain hexagonal architecture boundaries
- Using Map<Long, String> for batch activity engine type lookup
- Activities without engineType (deleted) are omitted from results

next_sprint_suggestions:
- Sprint 049: World learning progress port