# Sprint 027 - backend
# -----------------------------------------------

## Goal
Implement child learning path progress and completed step history using relational tables, not JSON lists.

## Status
status: completed
started_at: 2026-06-17
closed_at: 2026-06-17
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [x] Create `UpdateChildLearningProgressUseCase`.
- [x] Create `RegisterChildLearningCompletedStepUseCase`.
- [x] Create `GetChildLearningProgressUseCase`.
- [x] Create `ChildLearningProgressRepository`.
- [x] Create `ChildLearningCompletedStepRepository`.
- [x] Implement current step upsert by child and learning path.
- [x] Implement completed step registration by child, learning path, and step.
- [x] Implement progress read including current step and completed steps.

### Persistence
- [x] Create `ChildLearningProgressJpaEntity`.
- [x] Create `ChildLearningCompletedStepJpaEntity`.
- [x] Create Spring Data repositories.
- [x] Create persistence adapters.
- [x] Enforce one progress row per child and learning path.
- [x] Enforce one completed step row per child, learning path, and step.

### Tests
- [x] Unit test creating initial learning progress.
- [x] Unit test updating current step.
- [x] Unit test registering a completed step.
- [x] Unit test duplicate completed step is rejected or ignored consistently.
- [x] Unit test reading progress with completed steps.

## Manual Tests
- Not applicable. This sprint exposes no REST endpoint and should be verified through automated tests.

## Risks
- Reintroducing completed steps as JSON would make dashboard and statistics harder.
- Learning path catalog data must remain in content; tracking owns only child progress.

## Dependencies
- Sprint 021 completed.
- Content learning path and learning path step catalog is available.

## Agent Instruction
- Do not add child progress fields to content tables.
- Do not implement map UI behavior.
- Do not expose dashboard APIs yet.

## Notes
This sprint provides the runtime progress model for the future learning path game loop and dashboard.

## Review

completed_tasks:
- Created UpdateChildLearningProgressUseCase inbound port
- Created RegisterChildLearningCompletedStepUseCase inbound port (idempotent)
- Created GetChildLearningProgressUseCase inbound port (returns ChildLearningProgressResponse DTO)
- Created ChildLearningProgressRepository outbound port
- Created ChildLearningCompletedStepRepository outbound port
- Created ChildLearningProgressResponse DTO with completedSteps list
- Created CompletedStepInfo DTO (stepId + completedAt)
- Created ChildLearningProgressJpaEntity (extends BaseEntity, ManyToOne to LearningPathStepJpaEntity)
- Created ChildLearningCompletedStepJpaEntity (extends BaseEntity, ManyToOne to LearningPathStepJpaEntity)
- Created ChildLearningProgressJpaRepository with derived query
- Created ChildLearningCompletedStepJpaRepository with derived queries
- Created ChildLearningProgressPersistenceAdapter with toDomain/toJpa mappers
- Created ChildLearningCompletedStepPersistenceAdapter with toDomain/toJpa mappers
- Created ChildLearningProgressService implementing all 3 use cases
- Updated TrackingModuleConfiguration with new service bean
- Created 9 unit tests (all passing):
  - updateCurrentStep_createsNewProgressWhenNotExists
  - updateCurrentStep_updatesExistingProgress
  - updateCurrentStep_throwsWhenLearningPathNotFound
  - updateCurrentStep_throwsWhenLearningPathIdIsNull
  - registerCompletedStep_createsNewRecord
  - registerCompletedStep_ignoresDuplicate
  - getChildLearningProgress_returnsProgressWithCompletedSteps
  - getChildLearningProgress_returnsEmptyWhenNoProgress
  - getChildLearningProgress_returnsMultipleCompletedSteps

incomplete_tasks:
- None

contract_changes:
- None (uses existing tables from Sprint 021)

learnings:
- Used ValidationException for null learningPathId validation in UpdateChildLearningProgressUseCase
- Used LearningPathRepository.findById to validate learningPathId exists (content module coupling)
- GetChildLearningProgress returns ChildLearningProgressResponse DTO with nested completedSteps list
- Completed steps are idempotent - duplicates are silently ignored
- Used ManyToOne relationships to LearningPathStepJpaEntity for FK references

next_sprint_suggestions:
- Implement learning path step unlock logic based on unlockCondition
- Implement dashboard read API for learning progress
- Implement learning path recommendation engine based on progress
- Implement retention job for old tracking data
