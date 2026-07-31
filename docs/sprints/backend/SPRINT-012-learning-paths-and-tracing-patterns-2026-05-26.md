# Sprint 012 - backend
# -----------------------------------------------

## Goal
Implement static learning path and tracing pattern catalogs without child-specific progress or game runtime state.

## Status
status: active
started_at: 2026-05-25
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [x] Create `LearningPath` domain model.
- [x] Create `LearningPathStep` domain model referencing `LearningPath` and `Activity`.
- [x] Create `TracingPattern` domain model referencing `Topic`.
- [x] Model learning path step order, position, declarative unlock condition, and optional visual metadata.
- [x] Model tracing pattern ordered normalized points as static catalog data.

### Validation
- [x] Validate learning path age ranges and status.
- [x] Validate learning path step order uniqueness inside one path.
- [x] Validate learning path step activity references.
- [x] Validate tracing pattern topic references.
- [x] Validate tracing pattern point coordinates are normalized and ordered.

### Migration
- [x] Add a new Liquibase migration for learning paths, learning path steps, and tracing patterns.
- [x] Add foreign keys to activity and topic tables.
- [x] Add indexes for status, learning path ID, activity ID, topic ID, and age range.
- [x] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [x] Create JPA entities, Spring Data repositories, and persistence adapters.
- [x] Implement use cases for learning path create, update, get, and list.
- [x] Implement use cases for learning path step create, update, get, and list by path.
- [x] Implement use cases for tracing pattern create, update, get, and list by topic.

### Dev-Only APIs
- [x] Create dev-only CRUD endpoints under `/api/v1/dev/content/learning-paths`.
- [x] Create dev-only CRUD endpoints for learning path steps under the learning path route.
- [x] Create dev-only CRUD endpoints under `/api/v1/dev/content/tracing-patterns`.
- [x] Register controllers only with Spring profile `dev`.

### Tests
- [x] Add unit tests for learning path validation.
- [x] Add unit tests for tracing pattern validation.
- [x] Add integration tests for dev-only endpoints with profile `dev`.
- [x] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
- [x] Add persistence tests for step ordering and tracing point serialization.

## Risks
- Learning path state can easily drift into tracking responsibilities.
- Tracing point format must remain simple enough for future game engine consumption.
- Parent override and child progress are intentionally deferred and must not leak into schema.

## Dependencies
- Sprint 010 completed.
- Activity and topic catalogs are available.

## Agent Instruction
- Do not add `LOCKED`, `AVAILABLE`, or `COMPLETED` per-child status to content tables.
- Do not implement parent override logic in this sprint.
- Do not implement `DotConnectionEngine`; only provide static tracing catalog data.
- Ensure all dev CRUD endpoints use `/api/v1/dev/content/**` and profile `dev` only.

## Notes
This sprint prepares static map/path and tracing data for future game and tracking features.

## Review

completed_tasks:
- Created LearningPath, LearningPathStep, and TracingPattern domain models
- Created LearningPathValidator, LearningPathStepValidator, and TracingPatternValidator
- Created Liquibase migration 011 for learning_path, learning_path_step, and tracing_pattern tables
- Created LearningPathJpaEntity, LearningPathStepJpaEntity, and TracingPatternJpaEntity
- Created LearningPathJpaRepository, LearningPathStepJpaRepository, and TracingPatternJpaRepository
- Created LearningPathPersistenceAdapter, LearningPathStepPersistenceAdapter, and TracingPatternPersistenceAdapter
- Created LearningPathUseCase, LearningPathStepUseCase, and TracingPatternUseCase ports-in
- Created LearningPathRepository, LearningPathStepRepository, and TracingPatternRepository ports-out
- Created LearningPathService, LearningPathStepService, and TracingPatternService
- Created DTOs (Create/Update/Response) for all three entities
- Created LearningPathController, LearningPathStepController, and TracingPatternController with @Profile("dev")
- Updated ContentModuleConfiguration with new service beans
- Added 67 unit tests (validators, services, persistence adapters)
- Added integration tests for dev-only endpoints

incomplete_tasks: none

contract_changes:
- Added OpenAPI paths for /api/v1/dev/content/learning-paths, /api/v1/dev/content/learning-paths/{id}/steps, and /api/v1/dev/content/tracing-patterns (CRUD)

learnings:
- TracingPattern points stored as semicolon-separated coordinate pairs (e.g., "0.0,0.0;0.5,0.5;1.0,1.0")
- LearningPathStep has unique constraint on (learning_path_id, step_order) to prevent duplicate ordering
- LearningPathStep uses CASCADE delete on both FK references (learning_path and activity)
- TracingPattern points are normalized between 0.0 and 1.0 for game engine consumption

next_sprint_suggestions:
- Sprint 013: stories catalog with parental access
- Consider adding seed data for learning paths and tracing patterns before Sprint 014 (seeds sprint)
