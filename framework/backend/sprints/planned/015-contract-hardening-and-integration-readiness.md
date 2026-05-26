# Sprint 015 - backend
# -----------------------------------------------

## Goal
Harden FEAT-003 contracts, security boundaries, and integration readiness after all content catalog slices are implemented.

## Status
status: pending
started_at:
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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
