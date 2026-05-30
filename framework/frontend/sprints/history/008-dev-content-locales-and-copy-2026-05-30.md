# Sprint 008 - frontend
# -----------------------------------------------

## Goal
Implement Dev Content Locales and Copy: content locales, curiosities, and avatar event catalog
list/create/edit flows, entity/topic/event filters, inline API errors, and OpenAPI-derived types from
`docs/product/features/frontend/dev-app/FEAT-004-Dev-Content-Locales-And-Copy.md`.

## Status
status: archived
started_at: 2026-05-27 00:00:00
closed_at: 2026-05-30 00:00:00
blocked_by:
waiting_for:

## Tasks

### Contract Types
- [ ] Review `docs/contracts/api/openapi.json` for dev-content locale, curiosity, and avatar event catalog schemas.
- [ ] Add OpenAPI-derived types for content locales, curiosities, and avatar event catalog entries.
- [ ] Include request and response types for create/update/list operations.
- [ ] Use contract enums for locale entity type, avatar event type, and avatar tone; do not invent local enum values.

### Services
- [ ] Extend `src/services/devContentService.ts` for locale and copy endpoints.
- [ ] Add `listContentLocales(entityType, entityId)` using `GET /api/v1/dev/content/locales`.
- [ ] Add `createContentLocale(payload)` using `POST /api/v1/dev/content/locales`.
- [ ] Add `updateContentLocale(id, payload)` using `PUT /api/v1/dev/content/locales/{id}`.
- [ ] Add `listCuriosities(filters?)` using `GET /api/v1/dev/content/curiosities` with optional `topicId`, `age`, and `locale`.
- [ ] Add `getCuriosityById(id)` using `GET /api/v1/dev/content/curiosities/{id}`.
- [ ] Add `createCuriosity(payload)` using `POST /api/v1/dev/content/curiosities`.
- [ ] Add `updateCuriosity(id, payload)` using `PUT /api/v1/dev/content/curiosities/{id}`.
- [ ] Add `listAvatarEvents(filters?)` using `GET /api/v1/dev/content/avatar-events` with optional `eventType`, `tone`, and `locale`.
- [ ] Add `getAvatarEventById(id)` using `GET /api/v1/dev/content/avatar-events/{id}`.
- [ ] Add `createAvatarEvent(payload)` using `POST /api/v1/dev/content/avatar-events`.
- [ ] Add `updateAvatarEvent(id, payload)` using `PUT /api/v1/dev/content/avatar-events/{id}`.
- [ ] Ensure all calls use `src/shared/api/axios.ts`.

### State Management
- [ ] Extend the dev content Pinia store for content locales, curiosities, and avatar events.
- [ ] Track selected locale entity type, selected entity id, selected topic, age filter, locale filter, event type filter, and tone filter.
- [ ] Track loading and error states independently enough to avoid one section blocking another.
- [ ] Stores must call services only; stores must not call Axios directly.

### Locales UI
- [ ] Implement content locale listing by selected `entityType` and `entityId`.
- [ ] Require selecting `entityType` and `entityId` before listing or creating locales.
- [ ] Add create content locale form with fields required by contract.
- [ ] Add edit content locale flow using selected/listed locale data.
- [ ] Clearly distinguish content locales from Vue i18n UI translations.
- [ ] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [ ] Show loading, empty, and error states.

### Curiosities UI
- [ ] Implement curiosity listing.
- [ ] Add filters for topic, age, and locale when supported by the contract.
- [ ] Add create curiosity form with topic selector populated from real topic data.
- [ ] Add edit curiosity flow: fetch fresh data via `getCuriosityById(id)` before opening the form.
- [ ] Use textarea-style controls for long copy fields.
- [ ] Show TTS-oriented helper text without adding blocking domain validation unless required by contract.
- [ ] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [ ] Show loading, empty, and error states.

### Avatar Events UI
- [ ] Implement avatar event catalog listing.
- [ ] Add filters for event type, tone, and locale when supported by the contract.
- [ ] Add create avatar event catalog form with fields required by contract.
- [ ] Add edit avatar event flow: fetch fresh data via `getAvatarEventById(id)` before opening the form.
- [ ] Use textarea-style controls for message/copy fields.
- [ ] Show TTS/fallback-message helper text without calling TTS or agent services.
- [ ] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [ ] Show loading, empty, and error states.

### i18n and UX
- [ ] Add all visible labels and messages to `src/i18n/es.ts`.
- [ ] Do not hardcode visible labels in Vue templates.
- [ ] Keep the UI responsive in portrait and landscape.
- [ ] Do not add delete operations.
- [ ] Do not call agent services, generate text automatically, invoke TTS, or preview audio.

## Risks
- **Confusing content locales with UI i18n**: developers may mix catalog data with Vue translations.
  Mitigation: label the section clearly and document that content locales are backend catalog data.
- **Text generation scope creep**: the interface may become a prompt/generation tool.
  Mitigation: FEAT-004 only edits persisted content; no agent calls or generated text.
- **TTS preview scope creep**: avatar/copy fields may tempt audio preview integration.
  Mitigation: show helper text only; do not invoke TTS or audio preview.
- **Over-validating copy**: frontend may block valid backend data based on local wording rules.
  Mitigation: provide guidance and basic required-field UX only; backend owns final validation.
- **Scope creep into stories or delete**: FEAT-004 does not include story catalog or deletes.
  Mitigation: leave stories and delete operations out unless a future feature explicitly adds them.

## Dependencies
- `docs/product/features/frontend/dev-app/FEAT-004-Dev-Content-Locales-And-Copy.md` - source feature.
- `docs/product/features/frontend/dev-app/FEAT-002-Dev-Content-Catalog-Core.md` - category/topic data for selectors.
- `docs/product/features/frontend/dev-app/FEAT-003-Dev-Content-Activity-Configuration.md` - activity/resource data for locale entity selection where needed.
- `docs/architecture/decisions/ADR-011-Dev-Content-Manager.md` - dev app activation decision.
- `docs/contracts/api/openapi.json` - source of truth for request/response shapes.
- Backend content endpoints under `/api/v1/dev/content/locales`, `/curiosities`, and `/avatar-events` must exist in dev profile.

## Agent Instruction
- Archived to start Sprint 009 for family creation.
- Preserve existing implementation work and do not revert related files without explicit human confirmation.

## Notes
Derived from `docs/product/features/frontend/dev-app/FEAT-004-Dev-Content-Locales-And-Copy.md`.

## Review

completed_tasks:

incomplete_tasks:
- Sprint archived before checklist completion to prioritize `FEAT-003-Creation-Family`.

contract_changes:

learnings:

next_sprint_suggestions:
- Re-open dev content locales and copy as a later sprint if still required.
