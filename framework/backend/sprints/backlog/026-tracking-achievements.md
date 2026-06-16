# Sprint 026 - backend
# -----------------------------------------------

## Goal
Implement internal child achievement registration and lookup without WebSocket event emission.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [ ] Create `RegisterChildAchievementUseCase`.
- [ ] Create `GetChildAchievementsUseCase`.
- [ ] Create `ChildAchievementRepository`.
- [ ] Implement service method to register an achievement.
- [ ] Implement service method to list achievements by child.
- [ ] Allow global achievements with nullable `activityId` and `topicId`.
- [ ] Allow activity/topic-specific achievements when IDs are present.

### Persistence
- [ ] Create `ChildAchievementJpaEntity`.
- [ ] Create Spring Data repository.
- [ ] Create persistence adapter.
- [ ] Enforce uniqueness by `childProfileId`, `achievementCode`, optional `activityId`, and optional `topicId`.

### Tests
- [ ] Unit test registering a global achievement.
- [ ] Unit test registering an activity-specific achievement.
- [ ] Unit test duplicate achievement is rejected or ignored consistently.
- [ ] Unit test listing achievements by child.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
