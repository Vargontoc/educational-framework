# Sprint 028 - backend
# -----------------------------------------------

## Goal
Implement `TopicSelectionService` using `TopicSummary` so future cognitive engines can request pedagogically useful topics.

## Status
status: completed
started_at: 2026-06-18
closed_at: 2026-06-18
blocked_by:
waiting_for:

## Tasks

### Ports And Service
- [x] Create `ClassifyTopicsByPerformanceUseCase`.
- [x] Create `SelectTopicsForDifficultyUseCase` if needed by the existing style.
- [x] Implement `TopicSelectionService` using `TopicSummary` records.
- [x] Return topics grouped by `WEAK`, `MEDIUM`, and `STRONG`.
- [x] Implement selection distribution for `EASY`: 50% weak, 30% medium, 20% strong.
- [x] Implement selection distribution for `MEDIUM`: 60% weak, 30% medium, 10% strong.
- [x] Implement selection distribution for `HARD`: 70% weak, 20% medium, 10% strong.
- [x] Add deterministic fallback when one band has too few topics.

### Tests
- [x] Unit test topic classification from summary data.
- [x] Unit test `EASY` distribution.
- [x] Unit test `MEDIUM` distribution.
- [x] Unit test `HARD` distribution.
- [x] Unit test fallback when a band has no topics.
- [x] Unit test service does not require game state.

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
- Created DifficultyLevel enum (EASY, MEDIUM, HARD)
- Created TopicGroupedByPerformance DTO (Map<performanceBand, List<Long>>)
- Created TopicSelectionResult DTO (List<Long> selectedTopicIds)
- Created ClassifyTopicsByPerformanceUseCase inbound port
- Created SelectTopicsForDifficultyUseCase inbound port (with optional count parameter)
- Created TopicSelectionService implementing both use cases
- Added findByChildProfileId to TopicSummaryRepository, TopicSummaryJpaRepository, TopicSummaryPersistenceAdapter
- Updated TrackingModuleConfiguration with new service bean
- Created 13 unit tests (all passing):
  - classifyTopics_returnsEmptyMap_whenNoTopics
  - classifyTopics_groupsTopicsByPerformanceBand
  - selectTopicsForDifficulty_EASY_returnsCorrectDistribution
  - selectTopicsForDifficulty_MEDIUM_returnsCorrectDistribution
  - selectTopicsForDifficulty_HARD_returnsCorrectDistribution
  - selectTopicsForDifficulty_fallbackWhenBandEmpty
  - selectTopicsForDifficulty_returnsAllWhenCountExceedsAvailable
  - selectTopicsForDifficulty_defaultCount_returnsAllMatching
  - selectTopicsForDifficulty_nullCount_returnsAllMatching
  - selectTopicsForDifficulty_emptyTopicList
  - selectTopicsForDifficulty_respectsExactCount
  - selectTopicsForDifficulty_topicsFromWeakBandFirstForEasy
  - selectTopicsForDifficulty_deterministicOrdering

incomplete_tasks:
- None

contract_changes:
- TopicSummaryRepository: added findByChildProfileId(Long childProfileId) method
- TopicSummaryJpaRepository: added derived query
- TopicSummaryPersistenceAdapter: implemented findByChildProfileId

learnings:
- TopicSelectionService is stateless and uses TopicSummaryRepository
- Distribution percentages are applied proportionally: (count * percentage) / 100
- Deterministic ordering using natural sort of topic IDs
- Fallback mechanism redistributes unused slots to other bands when a band is exhausted
- count=null or count<=0 returns all available topics

next_sprint_suggestions:
- Implement dashboard read API for topic selection results
- Implement topic recommendation based on learning path progress
- Implement retention job for old tracking data
