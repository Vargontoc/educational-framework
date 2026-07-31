# Sprint 035 - backend
# -----------------------------------------------

## Goal
Ensure the game shell can load minimum content catalog data (Activity, DifficultyLevel) without depending on content persistence internals.

## Status
status: closed
started_at: 2026-06-21
closed_at: 2026-06-21
blocked_by:
waiting_for:

## Tasks

### Exception
- [x] Create `ContentNotReadyException` exception.

### Activity Extension
- [x] Extend `ActivityRepository` with `findByIdAndStatus(Long, ContentStatus)`.
- [x] Extend `ActivityJpaRepository` with `findByIdAndStatus(Long, String)`.
- [x] Extend `ActivityPersistenceAdapter` with `findByIdAndStatus` implementation.
- [x] Extend `ActivityUseCase` with `getGameReadyActivity(Long)`.
- [x] Implement `ActivityService.getGameReadyActivity(Long)`.

### DifficultyLevel Extension
- [x] Extend `DifficultyLevelRepository` with `findByIdAndEngineParamsIsNotNull(Long)`.
- [x] Extend `DifficultyLevelJpaRepository` with `findByIdAndEngineParamsIsNotNull(Long)`.
- [x] Extend `DifficultyLevelPersistenceAdapter` with `findByIdAndEngineParamsIsNotNull` implementation.
- [x] Extend `DifficultyLevelRepository` with `findFirstByActivityIdOrderByDifficultyCodeAsc(Long)`.
- [x] Extend `DifficultyLevelJpaRepository` with `findFirstByActivityIdOrderByDifficultyCodeAsc(Long)`.
- [x] Extend `DifficultyLevelPersistenceAdapter` with `findFirstByActivityIdOrderByDifficultyCodeAsc` implementation.
- [x] Extend `DifficultyLevelUseCase` with `getGameReadyDifficultyLevel(Long)`.
- [x] Extend `DifficultyLevelUseCase` with `getEasiestDifficultyLevel(Long)`.
- [x] Implement `DifficultyLevelService.getGameReadyDifficultyLevel(Long)`.
- [x] Implement `DifficultyLevelService.getEasiestDifficultyLevel(Long)`.

### GameCatalog Coordination
- [x] Create `GameCatalogUseCase` port interface.
- [x] Create `GameCatalogReadiness` DTO record with `activity`, `difficultyLevel`, `isNewToActivity`.
- [x] Create `GameCatalogService` implementing the use case.
- [x] Implement `getGameReadiness(Long childProfileId, Long activityId)`:
  - Validate Activity is ACTIVE.
  - Check `ActivitySummaryRepository` for existing tracking history.
  - If returning child (has history with difficultyLevelId): use saved difficulty level.
  - If new child: use EASY difficulty (getEasiestDifficultyLevel).
  - Return `GameCatalogReadiness` with `isNewToActivity` flag.

### Tests
- [x] Unit test `ActivityService.getGameReadyActivity` - happy path.
- [x] Unit test `ActivityService.getGameReadyActivity` - not active throws `ContentNotReadyException`.
- [x] Unit test `DifficultyLevelService.getGameReadyDifficultyLevel` - happy path.
- [x] Unit test `DifficultyLevelService.getGameReadyDifficultyLevel` - no engine params throws `ContentNotReadyException`.
- [x] Unit test `DifficultyLevelService.getEasiestDifficultyLevel` - happy path.
- [x] Unit test `DifficultyLevelService.getEasiestDifficultyLevel` - no levels throws `ContentNotReadyException`.
- [x] Unit test `GameCatalogService.getGameReadiness` - returning child gets saved difficulty.
- [x] Unit test `GameCatalogService.getGameReadiness` - new child gets EASY difficulty.
- [x] Unit test `GameCatalogService.getGameReadiness` - existing summary with null difficultyId gets EASY difficulty.

## Manual Tests
- Not required. Unit tests verify behavior.

## Constraints
- DifficultyLevel: Validate via `engineParams` presence, not status field.
- Only internal ports (no REST endpoints).
- Activity with EASY difficulty if no tracking history exists.
- Returning children get their current level from tracking history.

## Risks
- `ActivityJpaRepository.findByIdAndStatus` returning non-Optional caused compilation error.

## Dependencies
- Sprint 034 session heartbeat bridge.
- FEAT-007 game catalog readiness.

## Agent Instruction
- Keep content validation logic in content module.
- Do not add REST endpoints for game catalog.
- Use `ActivitySummaryRepository` for tracking lookup only.

## Review

completed_tasks:
- All 10 tasks completed
- All 10 unit tests added and passing
- 573 total tests pass with 0 failures

incomplete_tasks:
- None

contract_changes:
- `ActivityRepository` port extended with `findByIdAndStatus(Long, ContentStatus)`
- `ActivityJpaRepository` extended with `findByIdAndStatus(Long, String)` returning `Optional`
- `DifficultyLevelRepository` port extended with `findByIdAndEngineParamsIsNotNull(Long)` and `findFirstByActivityIdOrderByDifficultyCodeAsc(Long)`
- `ActivityUseCase` port extended with `getGameReadyActivity(Long)`
- `DifficultyLevelUseCase` port extended with `getGameReadyDifficultyLevel(Long)` and `getEasiestDifficultyLevel(Long)`
- New port `GameCatalogUseCase` with `getGameReadiness(Long, Long)` method
- New DTO `GameCatalogReadiness(activity, difficultyLevel, isNewToActivity)`
- New exception `ContentNotReadyException`

learnings:
- `findByIdAndStatus` JPA method must return `Optional<ActivityJpaEntity>`, not `ActivityJpaEntity` directly.

next_sprint_suggestions:
- Sprint 036: Game Domain Foundation - create pure game domain model for FEAT-007 shell.
