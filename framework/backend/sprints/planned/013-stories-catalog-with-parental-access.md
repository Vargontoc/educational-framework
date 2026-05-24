# Sprint 013 - backend
# -----------------------------------------------

## Goal
Implement story catalog management with dev-only CRUD and productive story read access protected by parental PIN/session authorization.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create `Story` domain model with title, description, age range, estimated duration, related topics, optional background music resource, and status.
- [ ] Create `StoryPage` domain model with story reference, page order, text, image resource reference, optional prerecorded audio resource, and status.
- [ ] Validate story page order uniqueness inside one story.
- [ ] Validate story text and resource references.

### Migration
- [ ] Add a new Liquibase migration for stories and story pages.
- [ ] Add relationship table for story topics if needed.
- [ ] Add indexes for status, story ID, topic ID, age range, and page order.
- [ ] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [ ] Create JPA entities, Spring Data repositories, and persistence adapters.
- [ ] Implement use cases for story create, update, get, and list.
- [ ] Implement use cases for story page create, update, get, and list by story.
- [ ] Implement productive read use cases returning active stories and active pages only.

### Dev-Only APIs
- [ ] Create dev-only CRUD endpoints under `/api/v1/dev/content/stories`.
- [ ] Create dev-only CRUD endpoints for story pages under the story route.
- [ ] Register controllers only with Spring profile `dev`.

### Productive Story APIs
- [ ] Create `GET /api/v1/content/stories` for productive story listing.
- [ ] Create `GET /api/v1/content/stories/{id}` for productive story details and pages.
- [ ] Protect productive story endpoints with parental PIN/session authorization.
- [ ] Ensure child sessions cannot access productive story endpoints without parental authorization.

### Contract Updates
- [ ] Update `docs/contracts/api/openapi.json` with productive story endpoints.
- [ ] Document authentication requirement for productive story endpoints.
- [ ] Document dev-only story endpoints if dev endpoints are included in the shared contract.

### Tests
- [ ] Add unit tests for story and story page validation.
- [ ] Add integration tests for dev-only CRUD with profile `dev`.
- [ ] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
