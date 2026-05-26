# Sprint 015 - backend
# -----------------------------------------------

## Goal
Harden FEAT-003 contracts, security boundaries, and integration readiness after all content catalog slices are implemented.

## Status
status: active
started_at: 2026-05-26
closed_at:
blocked_by:
waiting_for:

## Tasks

### Schema Gaps From Sprint 012
- [ ] Decide whether to add `status` (ACTIVE/INACTIVE/DRAFT) to `LearningPathStep` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [ ] Decide whether to add `patternType` to `TracingPattern` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [ ] Decide whether to add `minAge`/`maxAge` to `TracingPattern` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [ ] Update `docs/product/features/backend/FEAT-003-Content-Module.md` to reflect any fields that are deliberately omitted.

### Contract Hardening
- [ ] Review `docs/contracts/api/openapi.json` for all productive content endpoints.
- [ ] Ensure productive story endpoints document parental authorization requirements.
- [ ] Ensure dev-only endpoints are either omitted from production-facing contracts or clearly marked as development-only.
- [ ] Verify response schemas use `ApiResponse<T>` consistently where applicable.
- [ ] Verify enum values match backend implementation.

### Security Review
- [ ] Verify `/api/v1/dev/content/**` endpoints are registered only under Spring profile `dev`.
- [ ] Verify `/api/v1/dev/content/**` endpoints are unavailable outside profile `dev`.
- [ ] Verify productive story endpoints require parental PIN/session authorization.
- [ ] Verify no productive content endpoint exposes draft/inactive administrative content.

### Boundary Review
- [ ] Verify content tables contain no child-specific progress fields.
- [ ] Verify no tracking entities were created in content.
- [ ] Verify no game engine runtime state was created in content.
- [ ] Verify no agent or TTS calls were added to content services.
- [ ] Verify content exposes stable identifiers for future tracking references.

### Regression Tests
- [ ] Run unit tests for content services and validators.
- [ ] Run integration tests for dev-only endpoints with profile `dev`.
- [ ] Run integration tests for non-dev profile endpoint absence.
- [ ] Run integration tests for productive story authorization.
- [ ] Run seed idempotency tests.
- [ ] Run full backend test suite where environment allows.

### Documentation
- [ ] Update `docs/product/features/backend/FEAT-003-Content-Module.md` review/status if implementation is complete.
- [ ] Document any intentionally deferred productive read APIs.
- [ ] Document follow-up needs for game, avatar, agent, frontend, and tracking layers.

## Risks
- Contract drift between dev endpoints, productive endpoints, and frontend expectations.
- Accidental production exposure of development administrative APIs.
- Future modules may assume tracking or game state exists in content unless boundaries are documented clearly.

## Dependencies
- Sprints 009 through 014 completed.
- Productive story authorization behavior confirmed against current session/auth implementation.
- OpenAPI generation/update workflow available.

## Agent Instruction
- Do not add new functional scope unless it is required to fix contract or security gaps found in this sprint.
- Prefer removing accidental production exposure over adding compatibility behavior.
- Keep FEAT-003 boundaries explicit: content is static/catalog data, tracking is future runtime child history.
- If Docker/Testcontainers is unavailable, report which integration tests could not run and why.

## Notes
This sprint closes FEAT-003 from the backend perspective and prepares downstream implementation by frontend, game, avatar, agent, and tracking layers.

## Review

completed_tasks:
- Schema Gaps: Added patternType, minAge, maxAge to TracingPattern (migration 014 existed, wired through model/JPA/DTOs/service/controller/OpenAPI/seed)
- Schema Gaps: Updated FEAT-003 to document TracingPattern field decisions
- Contract Hardening: Added productive story endpoints to OpenAPI (GET /api/v1/content/stories, GET /api/v1/content/stories/{id})
- Contract Hardening: Added Story/StoryPage/StoryDetail schemas to OpenAPI
- Contract Hardening: Updated TracingPattern schemas with patternType, minAge, maxAge
- Contract Hardening: Added content tag to OpenAPI
- Security Review: Removed permitAll for /api/v1/dev/content/** from SecurityConfig
- Security Review: Verified productive story endpoints require FamilySession auth (Bearer token)
- Security Review: Updated DevContentControllerTest to include auth tokens
- Security Review: Updated DevContentControllerDisabledTest to expect 401 (was 404)
- Security Review: Created ProductiveStoryControllerTest with auth verification
- Boundary Review: Verified content has no tracking/game/agent/TTS leaks
- Boundary Review: Verified all entities have stable IDs
- Regression Tests: All 277 unit tests pass (integration tests require Docker/Testcontainers)

incomplete_tasks:
- Schema Gaps: LearningPathStep status field already existed (no action needed)
- Security Review: PIN re-verification for story detail endpoint (not implemented - FamilySession auth is sufficient per current architecture)
- Regression Tests: Integration tests could not run (Docker/Testcontainers unavailable in current environment)
- Documentation: Document intentionally deferred productive read APIs (categories, topics, activities, curiosities, learning-paths)

contract_changes:
- Added /api/v1/content/stories (GET) - list active stories
- Added /api/v1/content/stories/{id} (GET) - story detail with pages
- Added StoryResponse, StoryPageResponse, StoryDetailResponse schemas
- Added ApiResponseStoryList, ApiResponseStoryDetail schemas
- Added content tag
- Updated CreateTracingPatternRequest with patternType, minAge, maxAge
- Updated UpdateTracingPatternRequest with patternType, minAge, maxAge
- Updated TracingPatternResponse with patternType, minAge, maxAge

learnings:
- Migration 014 already added the TracingPattern columns but code was not updated
- SecurityConfig permitAll for dev content was safe (controllers don't register outside dev) but removing it improves defense-in-depth
- DevContentControllerDisabledTest expected 404 but now gets 401 after SecurityConfig change
- Productive story endpoints already required auth via SecurityConfig anyRequest().authenticated()

next_sprint_suggestions:
- Implement remaining productive read-only APIs (categories, topics, activities, curiosities, learning-paths)
- Add PIN re-verification step-up auth for sensitive story content if needed
- Run full integration test suite with Docker/Testcontainers available
- Frontend integration for productive story endpoints
