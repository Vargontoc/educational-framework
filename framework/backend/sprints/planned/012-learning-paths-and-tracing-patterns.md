# Sprint 012 - backend
# -----------------------------------------------

## Goal
Implement static learning path and tracing pattern catalogs without child-specific progress or game runtime state.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create `LearningPath` domain model.
- [ ] Create `LearningPathStep` domain model referencing `LearningPath` and `Activity`.
- [ ] Create `TracingPattern` domain model referencing `Topic`.
- [ ] Model learning path step order, position, declarative unlock condition, and optional visual metadata.
- [ ] Model tracing pattern ordered normalized points as static catalog data.

### Validation
- [ ] Validate learning path age ranges and status.
- [ ] Validate learning path step order uniqueness inside one path.
- [ ] Validate learning path step activity references.
- [ ] Validate tracing pattern topic references.
- [ ] Validate tracing pattern point coordinates are normalized and ordered.

### Migration
- [ ] Add a new Liquibase migration for learning paths, learning path steps, and tracing patterns.
- [ ] Add foreign keys to activity and topic tables.
- [ ] Add indexes for status, learning path ID, activity ID, topic ID, and age range.
- [ ] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [ ] Create JPA entities, Spring Data repositories, and persistence adapters.
- [ ] Implement use cases for learning path create, update, get, and list.
- [ ] Implement use cases for learning path step create, update, get, and list by path.
- [ ] Implement use cases for tracing pattern create, update, get, and list by topic.

### Dev-Only APIs
- [ ] Create dev-only CRUD endpoints under `/api/v1/dev/content/learning-paths`.
- [ ] Create dev-only CRUD endpoints for learning path steps under the learning path route.
- [ ] Create dev-only CRUD endpoints under `/api/v1/dev/content/tracing-patterns`.
- [ ] Register controllers only with Spring profile `dev`.

### Tests
- [ ] Add unit tests for learning path validation.
- [ ] Add unit tests for tracing pattern validation.
- [ ] Add integration tests for dev-only endpoints with profile `dev`.
- [ ] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
- [ ] Add persistence tests for step ordering and tracing point serialization.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
