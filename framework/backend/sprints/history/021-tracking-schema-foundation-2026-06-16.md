# Sprint 021 - backend
# -----------------------------------------------

## Goal
Create the tracking module schema foundation without business logic, REST endpoints, WebSocket events, game engines, avatar integration, or agent integration.

## Status
status: completed
started_at: 2026-06-16
closed_at: 2026-06-16
blocked_by:
waiting_for:

## Tasks

### Module Skeleton
- [x] Create the `tracking` package following the backend hexagonal structure.
- [x] Create pure domain models for `ActivityAttempt`, `ActivitySummary`, `TopicSummary`, `CuriosityViewed`, `ChildAchievement`, `ChildLearningProgress`, and `ChildLearningCompletedStep`.
- [x] Create enums for `AttemptResult` and `TopicPerformanceBand`.
- [x] Keep all domain models free of Spring and JPA annotations.

### Migration
- [x] Add a new Liquibase migration after the current latest backend migration.
- [x] Create `activity_attempt` table.
- [x] Create `activity_summary` table.
- [x] Create `topic_summary` table.
- [x] Create `curiosity_viewed` table.
- [x] Create `child_achievement` table.
- [x] Create `child_learning_progress` table.
- [x] Create `child_learning_completed_step` table.
- [x] Use the existing `BaseEntity` identifier strategy with `Long` IDs.
- [x] Add required foreign-key-like ID columns as `BIGINT` values.
- [x] Add basic uniqueness constraints described in `FEAT-006`.
- [x] Add indexes for common lookups and retention deletion.
- [x] Include the new migration in `db.changelog-master.xml`.

### Tests
- [x] Add a schema/migration integration test if the project has an existing pattern for it.
- [x] Add a context load test if needed to prove the new migration does not break startup.

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
- Created tracking module structure under `es.vargontoc.educational.framework.tracking`
- Created 7 pure domain models (no JPA annotations): ActivityAttempt, ActivitySummary, TopicSummary, CuriosityViewed, ChildAchievement, ChildLearningProgress, ChildLearningCompletedStep
- Created 2 enums: AttemptResult (CORRECT, INCORRECT, TIMEOUT) and TopicPerformanceBand (WEAK, MEDIUM, STRONG)
- Created migration 015__create_tracking_schema.xml with all 7 tables
- Added foreign key constraints as real FKs (not just BIGINT columns)
- Added uniqueness constraints per FEAT-006 specifications
- Added indexes for common lookups and retention deletion (created_at index on activity_attempt)
- Included migration in db.changelog-master.xml
- Created TrackingSchemaApplicationTest with model instantiation tests

incomplete_tasks:
- None

contract_changes:
- No contract changes (schema-only sprint)

learnings:
- Foreign keys confirmed as real constraints (user clarification)
- attempt_context field added as TEXT for engine-specific metadata
- All 7 domain models follow the hexagonal architecture pattern with pure Java getters/setters

next_sprint_suggestions:
- Implement ports/in use case interfaces for tracking operations
- Implement ports/out repository interfaces
- Implement persistence adapters and JPA entities
- Implement business services for tracking operations
