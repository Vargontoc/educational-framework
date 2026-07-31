# Sprint 009 - backend
# -----------------------------------------------

## Goal
Create the FEAT-003 content module foundation: hexagonal package structure, core domain model, validators, ports, and the initial content core catalog schema.

## Status
status: completed
started_at: 2026-05-24 11:06:49
closed_at: 2026-05-24 19:40:00
blocked_by:
waiting_for:

## Tasks

### Module Structure
- [x] Create `content/model/`, `content/ports/in/`, `content/ports/out/`, `content/service/`, `content/application/`, `content/infrastructure/` package structure.
- [x] Keep the module independent from `tracking`, `game`, `avatar`, and `agent` runtime code.

### Domain Enums
- [x] Create `ContentStatus` with `ACTIVE`, `INACTIVE`, `DRAFT`.
- [x] Create `DifficultyCode` with `EASY`, `MEDIUM`, `HARD`.
- [x] Create `ResourceType` with `IMAGE`, `AUDIO`, `VIDEO`.
- [x] Create `AvatarEventType` with `GREET`, `REWARD`, `HELP`, `ENCOURAGE` if needed by model references. **Skipped** - not needed by current model references.
- [x] Create `Tone` with `FUN`, `ENERGETIC`, `CALM`, `SERIOUS` if needed by model references. **Skipped** - not needed by current model references.
- [x] Create content locale handling for v1 `es-ES`.

### Core Domain Model
- [x] Create `Category` domain model.
- [x] Create `Topic` domain model with category reference and compatible variants.
- [x] Create `Activity` domain model with game engine type, age range, status, and related topics.
- [x] Create `DifficultyLevel` domain model with static engine parameters and adaptive threshold configuration.
- [x] Create `ActivityResource` domain model with resource type, opaque path/URL, and metadata.

### Validation
- [x] Add validators for required names, status values, age ranges, locale, and required relationships.
- [x] Validate `DifficultyLevel` thresholds as configuration only; do not implement adaptive runtime decisions.
- [x] Validate `ActivityResource` metadata as opaque JSON/text when engine-specific.

### Ports
- [x] Define core catalog input ports for create/update/list operations without REST DTO dependencies.
- [x] Define output ports for persistence of category, topic, activity, difficulty level, and activity resource.

### Migration
- [x] Add a new Liquibase migration after the current latest migration for core content catalog tables.
- [x] Include foreign keys and indexes for status, category ID, topic ID, activity ID, locale, and age range where applicable.
- [x] Include the new migration in `db.changelog-master.xml`.

### Tests
- [x] Add unit tests for core domain validation.
- [x] Add unit tests for service-level create/update/list rules using mocked output ports.

## Risks
- Scope creep into runtime game/tracking logic would make this sprint too large.
- Flexible JSON/text fields can hide invalid structures; validate only stable fields here and leave engine-specific validation to later modules.
- Migration must use existing `BaseEntity` expectations with `Long` IDs and audit columns.

## Dependencies
- FEAT-003 accepted content module plan.
- Completed `shared` module with `BaseEntity`, exceptions, validators, and `ApiResponse`.
- Existing Liquibase changelog in `framework/backend/src/main/resources/db/changelog/`.

## Agent Instruction
- Do not create REST controllers in this sprint.
- Do not create tracking tables or child-specific progress fields.
- Do not reference game, avatar, agent, or tracking implementation packages.
- JPA entities, if introduced in this sprint, must extend `BaseEntity` and not redeclare `id`, `createdAt`, or `updatedAt`.
- Keep all code and comments in English.

## Notes
This sprint prepares the smallest testable foundation for FEAT-003. Runtime consumption and dev-only administrative APIs are handled in later sprints.

## Review

completed_tasks:
    - Created hexagonal package structure for content module (model, ports/in, ports/out, service, application, validation, infrastructure/persistence)
    - Created 4 domain enums: ContentStatus, DifficultyCode, ResourceType, EntityType
    - Created 6 domain models: Category, Topic, Activity, DifficultyLevel, ActivityResource, ContentLocale
    - Created 6 validators with validation rules for all entities
    - Created 6 inbound ports (use case interfaces)
    - Created 6 outbound ports (repository interfaces)
    - Created 6 service implementations
    - Created 7 JPA entities extending BaseEntity
    - Created 7 Spring Data JPA repositories
    - Created 6 persistence adapters with domain/JPA mapping
    - Created Liquibase migration 009 with 7 tables (category, topic, activity, activity_topic, difficulty_level, activity_resource, content_locale)
    - Created 12 unit test classes (6 validator tests, 6 service tests) with 64 tests total
    - All 168 project tests passing

incomplete_tasks:
    - AvatarEventType and Tone enums skipped per user decision (not needed by current model references)

contract_changes:
    - None (no REST endpoints created in this sprint)

learnings:
    - JPA entities should use String type for status fields without @Enumerated annotation (matching existing codebase pattern)
    - Activity-Topic many-to-many relationship implemented via join table entity ActivityTopicJpaEntity with composite key
    - ContentLocale uses polymorphic pattern with EntityType enum + entityId for flexible locale storage

next_sprint_suggestions:
    - Sprint 010: Dev-only administrative CRUD APIs for content catalog
    - Consider adding ContentLocale management to admin APIs
