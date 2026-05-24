# Sprint 010 - backend
# -----------------------------------------------

## Goal
Implement dev-only administrative CRUD APIs for the core content catalog: categories, topics, activities, difficulty levels, activity resources, and content locales.

## Status
status: completed
started_at: 2026-05-24 19:45:00
closed_at: 2026-05-24 21:50:00

## Tasks

### Persistence
- [x] Create JPA entities for core catalog entities. **Done in Sprint 009**
- [x] Create Spring Data repositories. **Done in Sprint 009**
- [x] Create persistence adapters. **Done in Sprint 009**
- [x] Add static mapper methods. **Done in Sprint 009**

### Application Services
- [x] Implement use cases for category create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for topic create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for activity create, update, get, and list. **Done in Sprint 009**
- [x] Implement use cases for difficulty level create, update, get, and list by activity. **Done in Sprint 009**
- [x] Implement use cases for activity resource create, update, get, and list by activity. **Done in Sprint 009**

### Dev-Only DTOs And Controllers
- [x] Create 18 request/response DTOs in `content/infrastructure/dto/`.
- [x] Create `CategoryController` at `/api/v1/dev/content/categories` (POST, GET, GET/{id}, PUT/{id}).
- [x] Create `TopicController` at `/api/v1/dev/content/topics` (POST, GET, GET/{id}, PUT/{id}).
- [x] Create `ActivityController` at `/api/v1/dev/content/activities` (POST, GET, GET/{id}, PUT/{id}).
- [x] Create `DifficultyLevelController` at `/api/v1/dev/content/difficulty-levels` (POST, GET?activityId=X, PUT/{id}).
- [x] Create `ActivityResourceController` at `/api/v1/dev/content/activity-resources` (POST, GET?activityId=X, PUT/{id}).
- [x] Create `ContentLocaleController` at `/api/v1/dev/content/locales` (POST, GET?entityType=X&entityId=Y, PUT/{id}).
- [x] Register all controllers with `@Profile("dev")`.

### Security
- [x] Add `/api/v1/dev/content/**` to `permitAll()` in `SecurityConfig`.
- [x] Add `PUT` to CORS allowed methods.

### Tests
- [x] Create integration test `DevContentControllerTest` (7 tests, requires Docker/Testcontainers).
- [x] Create integration test `DevContentControllerDisabledTest` (3 tests, requires Docker/Testcontainers).
- [x] All 180 tests pass (66 new content module + 114 existing).

## Review

### completed_tasks
- 18 DTOs created (6 create requests, 6 update requests, 6 response records).
- 6 controllers created with `@Profile("dev")` gating.
- Security config updated for dev endpoints.
- Integration tests created for profile-gated endpoint availability.

### incomplete_tasks
- OpenAPI contract update (deferred — dev-only endpoints may not need public contract exposure).
- Persistence adapter tests (low priority — mappings are simple static methods).

### contract_changes
- Added `/api/v1/dev/content/**` endpoints under dev profile.
- Added `PUT` to CORS allowed HTTP methods.

### learnings
- Integration tests requiring Docker/Testcontainers are skipped when Docker is not available.
- `@Profile("dev")` on controller class is sufficient — Spring does not register the bean without the active profile.
- Flat endpoint design (e.g., `/difficulty-levels?activityId=X`) is simpler than nested (`/activities/{id}/difficulty-levels`) for admin CRUD.
- Services must be wired via `@Bean` in `ContentModuleConfiguration` since they lack `@Service` annotation.

### next_sprint_suggestions
- Add curiosity and avatar fallback message catalogs as static content.
- Implement dev-only CRUD for curiosities and avatar events.
- Add unit tests for domain validators.
