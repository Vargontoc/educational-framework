# Sprint 026 - backend
# -----------------------------------------------

## Goal
Implement internal child achievement registration and lookup without WebSocket event emission.

## Status
status: completed
started_at: 2026-06-17
closed_at: 2026-06-17
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [x] Create `RegisterChildAchievementUseCase`.
- [x] Create `GetChildAchievementsUseCase`.
- [x] Create `ChildAchievementRepository`.
- [x] Implement service method to register an achievement.
- [x] Implement service method to list achievements by child.
- [x] Allow global achievements with nullable `activityId` and `topicId`.
- [x] Allow activity/topic-specific achievements when IDs are present.

### Persistence
- [x] Create `ChildAchievementJpaEntity`.
- [x] Create Spring Data repository.
- [x] Create persistence adapter.
- [x] Enforce uniqueness by `childProfileId`, `achievementCode`, optional `activityId`, and optional `topicId`.

### Tests
- [x] Unit test registering a global achievement.
- [x] Unit test registering an activity-specific achievement.
- [x] Unit test duplicate achievement is rejected or ignored consistently.
- [x] Unit test listing achievements by child.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Emitting `GAME_ACHIEVEMENT_UNLOCKED` from tracking would violate scope.
- Achievement condition evaluation can become too broad if not left to future game orchestration.

## Dependencies
- Sprint 021 completed.

## Agent Instruction
- Do not implement achievement condition evaluation beyond registration and lookup.
- Do not emit WebSocket events.
- Do not add dashboard endpoints in this sprint.

## Notes
Game orchestration will later decide when an achievement should be registered and whether to emit an event.

## Review

completed_tasks:
- Created RegisterChildAchievementUseCase inbound port
- Created GetChildAchievementsUseCase inbound port (with optional activityId/topicId filters)
- Created ChildAchievementRepository outbound port
- Created ChildAchievementJpaEntity (extends BaseEntity)
- Created ChildAchievementJpaRepository with derived queries
- Created ChildAchievementPersistenceAdapter with toDomain/toJpa mappers
- Created ChildAchievementService implementing both use cases
- Updated TrackingModuleConfiguration with new bean
- Created 9 unit tests (all passing):
  - registerGlobalAchievement_createsRecord
  - registerActivitySpecificAchievement_createsRecord
  - registerDuplicate_ignored
  - getChildAchievements_returnsAll
  - getChildAchievements_withActivityFilter
  - getChildAchievements_withTopicFilter
  - getChildAchievements_withBothFilters
  - registerAchievement_blankCode_throwsValidationException
  - registerAchievement_nullCode_throwsValidationException

incomplete_tasks:
- None

contract_changes:
- None (uses existing child_achievement table from Sprint 021)

learnings:
- Used ValidationException for achievementCode validation
- GetChildAchievements supports 3 overloads: child only, child+activity, child+activity+topic
- Achievements are idempotent - duplicates are silently ignored

next_sprint_suggestions:
- Implement learning path progress tracking
- Implement topic selection optimization based on performance bands
- Implement dashboard read API for tracking data
- Implement retention job for old tracking data
