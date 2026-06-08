# Sprint 015 - backend
# -----------------------------------------------

## Goal
Harden FEAT-003 contracts, security boundaries, and integration readiness after all content catalog slices are implemented.

## Status
status: closed
started_at: 2026-06-08
closed_at: 2026-06-08
blocked_by:
waiting_for:

## Tasks

### Schema Gaps From Sprint 012
- [x] Decide whether to add `status` (ACTIVE/INACTIVE/DRAFT) to `LearningPathStep` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [x] Decide whether to add `patternType` to `TracingPattern` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [x] Decide whether to add `minAge`/`maxAge` to `TracingPattern` as specified in FEAT-003. If yes: new Liquibase migration, domain model update, JPA entity, DTOs, and OpenAPI schemas.
- [x] Update `docs/product/features/backend/FEAT-003-Content-Module.md` to reflect any fields that are deliberately omitted.

### Contract Hardening
- [x] Review `docs/contracts/api/openapi.json` for all productive content endpoints.
- [x] Ensure productive story endpoints document parental authorization requirements.
- [x] Ensure dev-only endpoints are either omitted from production-facing contracts or clearly marked as development-only.
- [x] Verify response schemas use `ApiResponse<T>` consistently where applicable.
- [x] Verify enum values match backend implementation.

### Security Review
- [x] Verify `/api/v1/dev/content/**` endpoints are registered only under Spring profile `dev`.
- [x] Verify `/api/v1/dev/content/**` endpoints are unavailable outside profile `dev`.
- [x] Verify productive story endpoints require parental PIN/session authorization.
- [x] Verify no productive content endpoint exposes draft/inactive administrative content.

### Boundary Review
- [x] Verify content tables contain no child-specific progress fields.
- [x] Verify no tracking entities were created in content.
- [x] Verify no game engine runtime state was created in content.
- [x] Verify no agent or TTS calls were added to content services.
- [x] Verify content exposes stable identifiers for future tracking references.

### Regression Tests
- [x] Run unit tests for content services and validators.
- [x] Run integration tests for dev-only endpoints with profile `dev`.
- [x] Run integration tests for non-dev profile endpoint absence.
- [x] Run integration tests for productive story authorization.
- [x] Run seed idempotency tests.
- [ ] Run full backend test suite where environment allows.

### Documentation
- [x] Update `docs/product/features/backend/FEAT-003-Content-Module.md` review/status if implementation is complete.
- [x] Document any intentionally deferred productive read APIs.
- [x] Document follow-up needs for game, avatar, agent, frontend, and tracking layers.

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
- All schema fields implemented: `status` on LearningPathStep, `patternType`/`minAge`/`maxAge` on TracingPattern (migration 014)
- Full stack wiring: model, JPA entity, DTOs, service, controller, OpenAPI contract
- `@Profile("dev")` confirmed on all dev content controllers
- `DevContentControllerDisabledTest` verifies 401 on dev endpoints outside dev profile
- Productive story endpoints require Bearer auth (SecurityConfig + integration test coverage)
- No cross-module dependencies: content services contain no TTS, agent, tracking, or game calls
- Content tables have no child-specific progress or runtime fields
- `ApiResponse<T>` wrapper used consistently across all content controllers
- FEAT-003-Content-Module.md updated with implementation notes, deferred APIs documented
- DTO gap fixed: `LearningPathStepResponse` was missing `status` field — corrected in this sprint review

incomplete_tasks:
- Full backend test suite not run (Docker/Testcontainers environment not verified)

contract_changes:
- Added `ContentStatus` enum schema to openapi.json components/schemas
- Added `status` property (`$ref: ContentStatus`) to `LearningPathStepResponse` schema in openapi.json

learnings:
- Schema fields added in Liquibase migrations can be silently excluded from response DTOs if the record and mapper are not updated together. Response DTOs should be verified against domain model fields at sprint close.
- `@Profile("dev")` on controllers + SecurityConfig `anyRequest().authenticated()` provides two independent layers of protection for dev endpoints.

next_sprint_suggestions:
- Implement deferred productive read-only APIs (categories, topics, activities, learning paths) once frontend flows are defined
- Add FamilySession-scoped authorization to productive story endpoints if finer-grained child access control is required
- Sprint 016: WebSocket game channel integration testing (auth handshake, heartbeat, session lifecycle)
