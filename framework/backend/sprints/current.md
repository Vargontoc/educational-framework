# Sprint 028 - backend
# -----------------------------------------------

## Goal
Implement `TopicSelectionService` using `TopicSummary` so future cognitive engines can request pedagogically useful topics.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Service
- [ ] Create `ClassifyTopicsByPerformanceUseCase`.
- [ ] Create `SelectTopicsForDifficultyUseCase` if needed by the existing style.
- [ ] Implement `TopicSelectionService` using `TopicSummary` records.
- [ ] Return topics grouped by `WEAK`, `MEDIUM`, and `STRONG`.
- [ ] Implement selection distribution for `EASY`: 50% weak, 30% medium, 20% strong.
- [ ] Implement selection distribution for `MEDIUM`: 60% weak, 30% medium, 10% strong.
- [ ] Implement selection distribution for `HARD`: 70% weak, 20% medium, 10% strong.
- [ ] Add deterministic fallback when one band has too few topics.

### Tests
- [ ] Unit test topic classification from summary data.
- [ ] Unit test `EASY` distribution.
- [ ] Unit test `MEDIUM` distribution.
- [ ] Unit test `HARD` distribution.
- [ ] Unit test fallback when a band has no topics.
- [ ] Unit test service does not require game state.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Random selection without deterministic tests can make the suite flaky.
- Pulling data from game state would break module boundaries.

## Dependencies
- Sprint 023 completed.

## Agent Instruction
- Keep selection deterministic or inject randomness behind a testable boundary.
- Do not implement game engines.
- Do not add REST endpoints.
- Use `TopicSummary`, not raw attempt recomputation.

## Notes
This service prepares Recognition, Memory, and Sequence engines without implementing those engines.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
