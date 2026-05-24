# Sprint 011 - backend
# -----------------------------------------------

## Goal
Add curiosity and avatar fallback message catalogs as static content, with dev-only CRUD and no TTS or agent execution.

## Status
status: active
started_at: 2026-05-24 21:55:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create `Curiosity` domain model referencing `Topic`.
- [ ] Create `AvatarEventCatalog` domain model with event type, tone, locale, message text, and status.
- [ ] Validate curiosity text for TTS-friendly length and simple structure.
- [ ] Validate avatar fallback message text with the existing agent/TTS `content_text` maximum length of 300 characters.

### Migration
- [ ] Add a new Liquibase migration for curiosity and avatar event catalog tables.
- [ ] Add foreign keys to topics where applicable.
- [ ] Add indexes for topic ID, event type, tone, locale, status, and age range.
- [ ] Include the migration in `db.changelog-master.xml`.

### Persistence And Services
- [ ] Create JPA entities, Spring Data repositories, and persistence adapters.
- [ ] Implement use cases for curiosity create, update, get, and list.
- [ ] Implement use cases for avatar event create, update, get, and list.
- [ ] Implement service query for active curiosities by topic, age, locale, and status.
- [ ] Implement service query for active avatar fallback messages by event type, tone, and locale.

### Dev-Only APIs
- [ ] Create dev-only CRUD endpoints under `/api/v1/dev/content/curiosities`.
- [ ] Create dev-only CRUD endpoints under `/api/v1/dev/content/avatar-events`.
- [ ] Register controllers only with Spring profile `dev`.

### Tests
- [ ] Add unit tests for curiosity validation.
- [ ] Add unit tests for avatar fallback validation.
- [ ] Add integration tests for dev-only endpoints with profile `dev`.
- [ ] Add integration tests proving dev-only endpoints are unavailable outside profile `dev`.
- [ ] Add tests for filtering active curiosity and avatar fallback records.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
