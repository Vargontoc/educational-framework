# Sprint 011 - backend
# -----------------------------------------------

## Goal
Add curiosity and avatar fallback message catalogs as static content, with dev-only CRUD and no TTS or agent execution.

## Status
status: closed
started_at: 2026-05-24 21:55:00
closed_at: 2026-05-25
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [x] Create `Curiosity` domain model referencing `Topic`.
- [x] Create `AvatarEventCatalog` domain model with event type, tone, locale, message text, and status.
- [x] Validate curiosity text for TTS-friendly length and simple structure.
- [x] Validate avatar fallback message text with the existing agent/TTS `content_text` maximum length of 300 characters.

### Migration
- [x] Add a new Liquibase migration for curiosity and avatar event catalog tables.
- [x] Add foreign keys to topics where applicable.
- [x] Add indexes for topic ID, event type, tone, locale, status, and age range.
- [x] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [x] Create JPA entities, Spring Data repositories, and persistence adapters.
- [x] Implement use cases for curiosity create, update, get, and list.
- [x] Implement use cases for avatar event create, update, get, and list.
- [x] Implement service query for active curiosities by topic, age, locale, and status.
- [x] Implement service query for active avatar fallback messages by event type, tone, and locale.

### Dev-Only APIs
- [x] Create dev-only CRUD endpoints under `/api/v1/dev/content/curiosities`.
- [x] Create dev-only CRUD endpoints under `/api/v1/dev/content/avatar-events`.
- [x] Register controllers only with Spring profile `dev`.

### Tests
- [x] Add unit tests for curiosity validation.
- [x] Add unit tests for avatar fallback validation.
- [x] Add integration tests for dev-only endpoints with profile `dev`.
- [x] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
- [x] Add tests for filtering active curiosity and avatar fallback records.

## Risks
- This catalog may be confused with agent-generated text; it is only fallback/static content.
- Long or complex text can increase TTS latency and reduce child comprehension.
- Adding `CuriosityViewed` here would violate the tracking boundary.

## Dependencies
- Sprint 010 completed.
- FEAT-001 agent/TTS content length constraint remains valid.
- Topic catalog is available for curiosity references.

## Agent Instruction
- Do not call agents or TTS from this sprint.
- Do not create `CuriosityViewed` or any child-specific runtime state.
- Keep avatar fallback messages deterministic and catalog-owned.
- Ensure all dev CRUD endpoints use `/api/v1/dev/content/**` and profile `dev` only.

## Notes
This sprint prepares fallback content for future avatar/agent resilience without integrating those runtime layers.

## Review

completed_tasks:
- Created AvatarEventType and AvatarTone enums
- Created Curiosity and AvatarEventCatalog domain models
- Created Liquibase migration 010 for curiosity and avatar_event_catalog tables
- Created CuriosityValidator and AvatarEventCatalogValidator
- Created CuriosityUseCase and AvatarEventCatalogUseCase ports-in
- Created CuriosityRepository and AvatarEventCatalogRepository ports-out
- Created CuriosityService and AvatarEventCatalogService
- Created JPA entities, repositories, and persistence adapters for both entities
- Created DTOs (Create/Update/Response) for both entities
- Created CuriosityController and AvatarEventCatalogController with @Profile("dev")
- Updated ContentModuleConfiguration with new service beans
- Added 43 unit tests (validators, services, persistence adapters)
- Added integration tests for dev-only endpoints

incomplete_tasks: none

contract_changes:
- Added OpenAPI paths for /api/v1/dev/content/curiosities and /api/v1/dev/content/avatar-events (CRUD)
- Added 10 new OpenAPI schema components: CreateCuriosityRequest, UpdateCuriosityRequest, CuriosityResponse, CreateAvatarEventCatalogRequest, UpdateAvatarEventCatalogRequest, AvatarEventCatalogResponse, and ApiResponse wrappers

post_review_fixes:
- Fixed dead-branch logic in CuriosityController.listCuriosities(): topicId-only filter was unreachable due to incorrect condition order
- Fixed NPE risk in AvatarEventCatalogPersistenceAdapter.findActiveByFilters(): added null guard before calling .name() on enum arguments
- Updated FEAT-003-Content-Module.md enum values to match agent contract and implementation (AvatarEventType, AvatarTone)

learnings:
- Followed existing hexagonal architecture patterns consistently
- Tags stored as CSV in TEXT column (same pattern as compatibleVariants in Topic)
- Curiosity supports optional topicId (nullable FK) for general curiosities
- AvatarEventCatalog uses enum fields stored as Strings in DB
- Dev-only endpoints still require OpenAPI documentation despite not being part of production surface

next_sprint_suggestions:
- Sprint 012: learning paths and tracing patterns (see planned/012-learning-paths-and-tracing-patterns.md)
- Consider adding seed data for curiosities and avatar events before Sprint 014 (seeds sprint)
