# Sprint 009 - backend
# -----------------------------------------------

## Goal
Create the FEAT-003 content module foundation: hexagonal package structure, core domain model, validators, ports, and the initial content core catalog schema.

## Status
status: active
started_at: 2026-05-24 11:06:49
closed_at:
blocked_by:
waiting_for:

## Tasks

### Module Structure
- [ ] Create `content/model/`, `content/ports/in/`, `content/ports/out/`, `content/service/`, `content/application/`, `content/infrastructure/` package structure.
- [ ] Keep the module independent from `tracking`, `game`, `avatar`, and `agent` runtime code.

### Domain Enums
- [ ] Create `ContentStatus` with `ACTIVE`, `INACTIVE`, `DRAFT`.
- [ ] Create `DifficultyCode` with `EASY`, `MEDIUM`, `HARD`.
- [ ] Create `ResourceType` with `IMAGE`, `AUDIO`, `VIDEO`.
- [ ] Create `AvatarEventType` with `GREET`, `REWARD`, `HELP`, `ENCOURAGE` if needed by model references.
- [ ] Create `Tone` with `FUN`, `ENERGETIC`, `CALM`, `SERIOUS` if needed by model references.
- [ ] Create content locale handling for v1 `es-ES`.

### Core Domain Model
- [ ] Create `Category` domain model.
- [ ] Create `Topic` domain model with category reference and compatible variants.
- [ ] Create `Activity` domain model with game engine type, age range, status, and related topics.
- [ ] Create `DifficultyLevel` domain model with static engine parameters and adaptive threshold configuration.
- [ ] Create `ActivityResource` domain model with resource type, opaque path/URL, and metadata.

### Validation
- [ ] Add validators for required names, status values, age ranges, locale, and required relationships.
- [ ] Validate `DifficultyLevel` thresholds as configuration only; do not implement adaptive runtime decisions.
- [ ] Validate `ActivityResource` metadata as opaque JSON/text when engine-specific.

### Ports
- [ ] Define core catalog input ports for create/update/list operations without REST DTO dependencies.
- [ ] Define output ports for persistence of category, topic, activity, difficulty level, and activity resource.

### Migration
- [ ] Add a new Liquibase migration after the current latest migration for core content catalog tables.
- [ ] Include foreign keys and indexes for status, category ID, topic ID, activity ID, locale, and age range where applicable.
- [ ] Include the new migration in `db.changelog-master.xml`.

### Tests
- [ ] Add unit tests for core domain validation.
- [ ] Add unit tests for service-level create/update/list rules using mocked output ports.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
