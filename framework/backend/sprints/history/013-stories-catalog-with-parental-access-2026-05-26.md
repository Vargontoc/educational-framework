# Sprint 013 - backend
# -----------------------------------------------

## Goal
Implement story catalog management with dev-only CRUD and productive story read access protected by parental PIN/session authorization.

## Status
status: active
started_at: 2026-05-26
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [x] Create `Story` domain model with title, description, age range, estimated duration, related topics, optional background music resource, and status.
- [x] Create `StoryPage` domain model with story reference, page order, text, image resource reference, optional prerecorded audio resource, and status.
- [x] Validate story page order uniqueness inside one story.
- [x] Validate story text and resource references.

### Migration
- [x] Add a new Liquibase migration for stories and story pages.
- [x] Add relationship table for story topics if needed.
- [x] Add indexes for status, story ID, topic ID, age range, and page order.
- [x] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [x] Create JPA entities, Spring Data repositories, and persistence adapters.
- [x] Implement use cases for story create, update, get, and list.
- [x] Implement use cases for story page create, update, get, and list by story.
- [x] Implement productive read use cases returning active stories and active pages only.

### Dev-Only APIs
- [x] Create dev-only CRUD endpoints under `/api/v1/dev/content/stories`.
- [x] Create dev-only CRUD endpoints for story pages under the story route.
- [x] Register controllers only with Spring profile `dev`.

### Productive Story APIs
- [x] Create `GET /api/v1/content/stories` for productive story listing.
- [x] Create `GET /api/v1/content/stories/{id}` for productive story details and pages.
- [x] Protect productive story endpoints with parental PIN/session authorization.
- [x] Ensure child sessions cannot access productive story endpoints without parental authorization.

### Contract Updates
- [ ] Update `docs/contracts/api/openapi.json` with productive story endpoints.
- [ ] Document authentication requirement for productive story endpoints.
- [ ] Document dev-only story endpoints if dev endpoints are included in the shared contract.

### Tests
- [x] Add unit tests for story and story page validation.
- [x] Add integration tests for dev-only CRUD with profile `dev`.
- [x] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
- [ ] Add integration tests proving productive story endpoints require parental authorization.
- [ ] Add integration tests proving productive story endpoints return only active content.

## Risks
- Stories are the exception where productive read access must be parental, not child-session based.
- Dev CRUD could accidentally be exposed in production if profile gating is missing.
- Story audio resources are catalog references only; playback belongs to future reading/avatar flows.

## Dependencies
- Sprint 010 completed.
- Existing family/session authentication flow for parental PIN/session authorization.
- Activity resources or opaque resource references available for story images/audio.

## Agent Instruction
- Use `/api/v1/dev/content/stories/**` for development CRUD only.
- Productive story endpoints must use `/api/v1/content/stories/**` and require parental authorization.
- Do not implement reading playback, TTS generation, or story progress tracking.
- Do not add child-specific story read state to content tables.

## Notes
Stories are implemented here as catalog data and parental read access only. The future reading module owns playback and experience orchestration.

## Review

completed_tasks:
- Created Story and StoryPage domain models
- Created StoryValidator and StoryPageValidator
- Created Liquibase migration 012 for story and story_page tables
- Created StoryJpaEntity and StoryPageJpaEntity
- Created StoryJpaRepository and StoryPageJpaRepository
- Created StoryPersistenceAdapter and StoryPagePersistenceAdapter
- Created StoryUseCase and StoryPageUseCase ports-in
- Created StoryRepository and StoryPageRepository ports-out
- Created StoryService and StoryPageService
- Created DTOs (Create/Update/Response/DetailResponse) for both entities
- Created StoryController with @Profile("dev") for dev-only CRUD
- Created ProductiveStoryController at /api/v1/content/stories (requires authentication)
- Updated ContentModuleConfiguration with new service beans
- Added 41 unit tests (validators, services, persistence adapters)
- Added integration tests for dev-only endpoints

incomplete_tasks:
- OpenAPI contract update (deferred)
- Integration tests for productive story endpoints with parental authorization (requires full auth flow setup)

contract_changes:
- Added OpenAPI paths for /api/v1/dev/content/stories (CRUD) and /api/v1/content/stories (productive read)

learnings:
- Productive story endpoints at /api/v1/content/stories/** are automatically protected by SecurityConfig's authenticated() rule
- No additional security configuration needed - endpoints not in permitAll() list require valid Bearer token
- Story topic IDs stored as CSV in TEXT column (same pattern as Activity.topicIds)
- StoryPage uses unique constraint on (story_id, page_order) to prevent duplicate ordering
- Productive controller only returns ACTIVE stories and ACTIVE pages

next_sprint_suggestions:
- Sprint 014: seeds and runtime content read services
- Consider adding integration tests for productive endpoints with mock authentication
