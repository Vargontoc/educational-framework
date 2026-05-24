# Sprint 010 - backend
# -----------------------------------------------

## Goal
Implement persistence and dev-only administrative CRUD APIs for the core content catalog: categories, topics, activities, difficulty levels, and activity resources.

## Status
status: active
started_at: 2026-05-24 19:45:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Persistence
- [x] Create JPA entities for `Category`, `Topic`, `Activity`, `DifficultyLevel`, and `ActivityResource` if not already created. **Done in Sprint 009**
- [x] Create Spring Data repositories for the core catalog entities. **Done in Sprint 009**
- [x] Create persistence adapters implementing the content output ports. **Done in Sprint 009**
- [ ] Add static mapper methods following the existing backend adapter style.

### Application Services
- [x] Implement use cases for category create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for topic create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for activity create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for difficulty level create, update, get, and list by activity. **Done in Sprint 009**
- [x] Implement use cases for activity resource create, update, get, and list by activity. **Done in Sprint 009**

### Dev-Only DTOs And Controllers
- [ ] Create request/response DTOs for core catalog admin operations.
- [ ] Create dev-only controller for `/api/v1/dev/content/categories`.
- [ ] Create dev-only controller for `/api/v1/dev/content/topics`.
- [ ] Create dev-only controller for `/api/v1/dev/content/activities`.
- [ ] Create dev-only controller for `/api/v1/dev/content/activities/{id}/difficulty-levels` if needed for admin management.
- [ ] Create dev-only controller for `/api/v1/dev/content/activities/{id}/resources` if needed for admin management.
- [ ] Register all development administrative controllers only with Spring profile `dev`.

### Contract Updates
- [ ] Update `docs/contracts/api/openapi.json` with dev-only core catalog endpoints if the project documents dev endpoints in the shared contract.
- [ ] Mark dev-only endpoints clearly in operation descriptions.

### Tests
- [ ] Add integration tests with active profile `dev` proving `/api/v1/dev/content/**` endpoints are available.
- [ ] Add integration tests without profile `dev` proving `/api/v1/dev/content/**` endpoints are unavailable.
- [ ] Add positive and negative tests for required validation rules.
- [ ] Add persistence adapter tests where mappings are non-trivial.

## Risks
- Dev-only endpoints could accidentally become available in production if controllers are not profile-gated.
- Admin APIs must not be treated as product APIs by frontend or runtime game flows.
- OpenAPI may become confusing if dev endpoints are mixed with production endpoints without clear descriptions.

## Dependencies
- Sprint 009 completed.
- Existing Spring profile/test profile setup.
- Existing security configuration reviewed for `/api/v1/dev/content/**` behavior.

## Agent Instruction
- Use `/api/v1/dev/content/**`, not `/api/v1/admin/content/**`.
- Annotate or configure development administrative controllers so they exist only under Spring profile `dev`.
- Do not implement production read APIs in this sprint except where strictly necessary for tests.
- Do not add child progress, viewed flags, scores, attempts, or runtime state to content entities.
- All responses should use `ApiResponse<T>` where applicable.

## Notes
This sprint validates the administrative workflow for the core catalog while keeping production surface clean.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
