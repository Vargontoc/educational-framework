# Sprint 027 - backend
# -----------------------------------------------

## Goal
Implement child learning path progress and completed step history using relational tables, not JSON lists.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Ports And Services
- [ ] Create `UpdateChildLearningProgressUseCase`.
- [ ] Create `RegisterChildLearningCompletedStepUseCase`.
- [ ] Create `GetChildLearningProgressUseCase`.
- [ ] Create `ChildLearningProgressRepository`.
- [ ] Create `ChildLearningCompletedStepRepository`.
- [ ] Implement current step upsert by child and learning path.
- [ ] Implement completed step registration by child, learning path, and step.
- [ ] Implement progress read including current step and completed steps.

### Persistence
- [ ] Create `ChildLearningProgressJpaEntity`.
- [ ] Create `ChildLearningCompletedStepJpaEntity`.
- [ ] Create Spring Data repositories.
- [ ] Create persistence adapters.
- [ ] Enforce one progress row per child and learning path.
- [ ] Enforce one completed step row per child, learning path, and step.

### Tests
- [ ] Unit test creating initial learning progress.
- [ ] Unit test updating current step.
- [ ] Unit test registering a completed step.
- [ ] Unit test duplicate completed step is rejected or ignored consistently.
- [ ] Unit test reading progress with completed steps.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
