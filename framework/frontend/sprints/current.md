# Sprint 006 - frontend
# -----------------------------------------------

## Goal
Implement the Dev Content Catalog Core: categories and topics list/create/edit flows, category-based
topic filtering, inline API errors, and OpenAPI-derived types from
`docs/product/features/frontend/dev-app/FEAT-002-Dev-Content-Catalog-Core.md`.

## Status
status: active
started_at: 2026-05-26 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Contract Types
- [ ] Review `docs/contracts/api/openapi.json` for dev-content category and topic schemas.
- [ ] Add OpenAPI-derived types for categories and topics in the existing frontend API types module.
- [ ] Include request and response types for create/update/list operations.

### Services
- [ ] Create or extend `src/services/devContentService.ts` for `/api/v1/dev/content/**`.
- [ ] Add `listCategories()` using `GET /api/v1/dev/content/categories`.
- [ ] Add `getCategoryById(id)` using `GET /api/v1/dev/content/categories/{id}`.
- [ ] Add `createCategory(payload)` using `POST /api/v1/dev/content/categories`.
- [ ] Add `updateCategory(id, payload)` using `PUT /api/v1/dev/content/categories/{id}`.
- [ ] Add `listTopics(categoryId?)` using `GET /api/v1/dev/content/topics` with optional `categoryId`.
- [ ] Add `getTopicById(id)` using `GET /api/v1/dev/content/topics/{id}`.
- [ ] Add `createTopic(payload)` using `POST /api/v1/dev/content/topics`.
- [ ] Add `updateTopic(id, payload)` using `PUT /api/v1/dev/content/topics/{id}`.
- [ ] Ensure all calls use `src/shared/api/axios.ts`.

### State Management
- [ ] Create or extend a dev content Pinia store if shared state is needed.
- [ ] Track categories, topics, selected category, loading state, and error state.
- [ ] Stores must call services only; stores must not call Axios directly.

### Categories UI
- [ ] Implement category listing in the dev content app shell.
- [ ] Add create category form with fields required by contract.
- [ ] Add edit category flow: fetch fresh data via `getCategoryById(id)` before opening the form.
- [ ] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [ ] Show loading, empty, and error states.

### Topics UI
- [ ] Implement topic listing in the dev content app shell.
- [ ] Add category filter for topics using real category data.
- [ ] Add create topic form with category selector populated from loaded categories.
- [ ] Add edit topic flow: fetch fresh data via `getTopicById(id)` before opening the form.
- [ ] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [ ] Show loading, empty, and error states.

### i18n and UX
- [ ] Add all visible labels and messages to `src/i18n/es.ts`.
- [ ] Do not hardcode visible labels in Vue templates.
- [ ] Keep the UI responsive in portrait and landscape.
- [ ] Do not add delete operations.

## Risks
- **Contract drift**: frontend models may diverge from backend OpenAPI schemas.
  Mitigation: derive types from `docs/contracts/api/openapi.json` and do not invent local shapes.
- **Invalid topic/category relationships**: topic forms may reference missing categories.
  Mitigation: populate category selectors only from real categories loaded through the API.
- **Duplicated backend validation**: frontend may over-encode domain rules.
  Mitigation: validate only basic required fields for UX; backend remains source of truth.
- **Scope creep into activities/resources/locales**: FEAT-002 covers only categories and topics.
  Mitigation: leave activities, difficulty levels, resources, locales, curiosities, and avatar events as shell sections.

## Dependencies
- `docs/product/features/frontend/dev-app/FEAT-002-Dev-Content-Catalog-Core.md` — source feature.
- `docs/product/features/frontend/dev-app/FEAT-001-Dev-Content-App-Shell.md` — previous shell feature.
- `docs/architecture/decisions/ADR-011-Dev-Content-Manager.md` — dev app activation decision.
- `docs/contracts/api/openapi.json` — source of truth for request/response shapes.
- Backend content endpoints under `/api/v1/dev/content/categories` and `/api/v1/dev/content/topics` must exist in dev profile.

## Agent Instruction
- Implement only FEAT-002 catalog core behavior: categories and topics.
- Do not implement activities, difficulty levels, resources, locales, curiosities, avatar events, or delete operations.
- Derive all TypeScript request/response types from `docs/contracts/api/openapi.json`.
- All Axios calls must go through `src/shared/api/axios.ts`.
- Stores call services; services call Axios.
- Keep `VITE_ENABLE_DEV_CONTENT === 'true'` as the route activation rule from Sprint 005.
- Do not require parental PIN or authenticated session for `/dev/content`.
- Do not add frontend domain validation beyond basic required-field UX.
- All visible strings must go through vue-i18n.
- Commit: `feat(frontend): add dev content catalog core`

## Notes
Derived from `docs/product/features/frontend/dev-app/FEAT-002-Dev-Content-Catalog-Core.md`.
This sprint assumes the FEAT-001 shell exists and focuses on the first real dev-content API
integration: categories and topics.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
