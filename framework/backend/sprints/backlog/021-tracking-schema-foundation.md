# Sprint 021 - backend
# -----------------------------------------------

## Goal
Create the tracking module schema foundation without business logic, REST endpoints, WebSocket events, game engines, avatar integration, or agent integration.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Module Skeleton
- [ ] Create the `tracking` package following the backend hexagonal structure.
- [ ] Create pure domain models for `ActivityAttempt`, `ActivitySummary`, `TopicSummary`, `CuriosityViewed`, `ChildAchievement`, `ChildLearningProgress`, and `ChildLearningCompletedStep`.
- [ ] Create enums for `AttemptResult` and `TopicPerformanceBand`.
- [ ] Keep all domain models free of Spring and JPA annotations.

### Migration
- [ ] Add a new Liquibase migration after the current latest backend migration.
- [ ] Create `activity_attempt` table.
- [ ] Create `activity_summary` table.
- [ ] Create `topic_summary` table.
- [ ] Create `curiosity_viewed` table.
- [ ] Create `child_achievement` table.
- [ ] Create `child_learning_progress` table.
- [ ] Create `child_learning_completed_step` table.
- [ ] Use the existing `BaseEntity` identifier strategy with `Long` IDs.
- [ ] Add required foreign-key-like ID columns as `BIGINT` values.
- [ ] Add basic uniqueness constraints described in `FEAT-006`.
- [ ] Add indexes for common lookups and retention deletion.
- [ ] Include the new migration in `db.changelog-master.xml`.

### Tests
- [ ] Add a schema/migration integration test if the project has an existing pattern for it.
- [ ] Add a context load test if needed to prove the new migration does not break startup.

## Manual Tests
- Start the backend locally and confirm Liquibase applies the new migration without errors.
- Inspect PostgreSQL and confirm all tracking tables exist.
- Confirm `activity_attempt` has an index usable for retention by `created_at`.

## Risks
- Schema design mistakes will affect all later tracking sprints.
- Adding JPA annotations to domain models would violate the hexagonal structure.
- Accidentally modeling completed learning steps as JSON would reintroduce the avoided design issue.

## Dependencies
- `FEAT-006 - Backend: Tracking Module` approved.
- Content, family, and session schemas already available.

## Agent Instruction
- Keep this sprint schema-only and model-only.
- Do not implement business services.
- Do not add REST controllers.
- Do not update OpenAPI in this sprint unless an endpoint is accidentally introduced, which should not happen.
- Do not implement WebSocket, game, avatar, TTS, or agent behavior.

## Notes
This sprint creates the stable database and domain foundation for all later tracking work.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
