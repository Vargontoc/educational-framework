# Sprint 007 - frontend
# -----------------------------------------------

## Goal
Implement Dev Content Activity Configuration: activities, difficulty levels, and activity resources
list/create/edit flows, topic/activity filtering, inline API errors, and OpenAPI-derived types from
`docs/product/features/frontend/dev-app/FEAT-003-Dev-Content-Activity-Configuration.md`.

## Status
status: completed
started_at: 2026-05-26 00:00:00
closed_at: 2026-05-27 00:00:00
blocked_by:
waiting_for:

## Tasks

### Contract Types
- [x] Review `docs/contracts/api/openapi.json` for dev-content activity, difficulty level, and activity resource schemas.
- [x] Add OpenAPI-derived types for activities, difficulty levels, and activity resources.
- [x] Include request and response types for create/update/list operations.
- [x] Represent flexible metadata or parameters exactly as the contract defines them; do not invent local shapes.

### Services
- [x] Extend `src/services/devContentService.ts` for activity configuration endpoints.
- [x] Add `listActivities(topicId?)` using `GET /api/v1/dev/content/activities` with optional `topicId`.
- [x] Add `getActivityById(id)` using `GET /api/v1/dev/content/activities/{id}`.
- [x] Add `createActivity(payload)` using `POST /api/v1/dev/content/activities`.
- [x] Add `updateActivity(id, payload)` using `PUT /api/v1/dev/content/activities/{id}`.
- [x] Add `listDifficultyLevels(activityId)` using `GET /api/v1/dev/content/difficulty-levels`.
- [x] Add `createDifficultyLevel(payload)` using `POST /api/v1/dev/content/difficulty-levels`.
- [x] Add `updateDifficultyLevel(id, payload)` using `PUT /api/v1/dev/content/difficulty-levels/{id}`.
- [x] Add `listActivityResources(activityId)` using `GET /api/v1/dev/content/activity-resources`.
- [x] Add `createActivityResource(payload)` using `POST /api/v1/dev/content/activity-resources`.
- [x] Add `updateActivityResource(id, payload)` using `PUT /api/v1/dev/content/activity-resources/{id}`.
- [x] Ensure all calls use `src/shared/api/axios.ts`.

### State Management
- [x] Extend the dev content Pinia store for activities, difficulty levels, and resources.
- [x] Track topics, activities, selected topic, selected activity, loading state, and error state.
- [x] Stores must call services only; stores must not call Axios directly.

### Activities UI
- [x] Implement activity listing in the dev content app shell.
- [x] Add topic filter for activities using real topic data from FEAT-002.
- [x] Add create activity form with fields required by contract.
- [x] Add edit activity flow: fetch fresh data via `getActivityById(id)` before opening the form.
- [x] Show API validation errors inline for `400`, `404`, and `409` cases where applicable.
- [x] Show loading, empty, and error states.

### Difficulty Levels UI
- [x] Require selecting an activity before listing or creating difficulty levels.
- [x] Implement difficulty level listing by selected `activityId`.
- [x] Add create difficulty level form with fields required by contract.
- [x] Add edit difficulty level flow using the selected/listed difficulty level data.
- [x] If the contract uses flexible parameters, render a simple text/JSON editor and validate only JSON syntax before submit.
- [x] Show API validation errors inline for `400` and `404` cases where applicable.
- [x] Show loading, empty, and error states.

### Activity Resources UI
- [x] Require selecting an activity before listing or creating resources.
- [x] Implement activity resource listing by selected `activityId`.
- [x] Add create activity resource form with fields required by contract.
- [x] Add edit activity resource flow using the selected/listed resource data.
- [x] If the contract uses flexible metadata, render a simple text/JSON editor and validate only JSON syntax before submit.
- [x] Clearly indicate that V1 manages resource references, not physical file uploads.
- [x] Show API validation errors inline for `400` and `404` cases where applicable.
- [x] Show loading, empty, and error states.

### i18n and UX
- [x] Add all visible labels and messages to `src/i18n/es.ts`.
- [x] Do not hardcode visible labels in Vue templates.
- [x] Keep the UI responsive in portrait and landscape.
- [x] Do not add delete operations.
- [x] Do not add game engine preview or runtime execution.

## Risks
- **Frontend becomes a game-rule source**: forms may start validating engine semantics.
  Mitigation: frontend only edits configuration; backend owns domain validity.
- **Flexible JSON fields are difficult to edit**: parameters/metadata may be malformed.
  Mitigation: use a simple JSON/text editor in V1 and validate only JSON syntax before submit.
- **Resource references point to missing assets**: V1 may create references without file management.
  Mitigation: clearly label resources as references only unless the contract adds uploads.
- **Scope creep into locales/curiosities/avatar events**: FEAT-003 covers only activities, difficulty levels, and resources.
  Mitigation: leave locales, curiosities, and avatar events for FEAT-004.

## Dependencies
- `docs/product/features/frontend/dev-app/FEAT-003-Dev-Content-Activity-Configuration.md` — source feature.
- `docs/product/features/frontend/dev-app/FEAT-002-Dev-Content-Catalog-Core.md` — categories/topics dependency.
- `docs/architecture/decisions/ADR-011-Dev-Content-Manager.md` — dev app activation decision.
- `docs/contracts/api/openapi.json` — source of truth for request/response shapes.
- Backend content endpoints under `/api/v1/dev/content/activities`, `/difficulty-levels`, and `/activity-resources` must exist in dev profile.

## Agent Instruction
- Implement only FEAT-003 activity configuration behavior: activities, difficulty levels, and activity resources.
- Do not implement locales, curiosities, avatar events, story catalog, uploads, delete operations, or game previews.
- Derive all TypeScript request/response types from `docs/contracts/api/openapi.json`.
- All Axios calls must go through `src/shared/api/axios.ts`.
- Stores call services; services call Axios.
- Keep `VITE_ENABLE_DEV_CONTENT === 'true'` as the route activation rule.
- Do not require parental PIN or authenticated session for `/dev/content`.
- Do not add frontend domain validation beyond basic required-field UX and JSON syntax checks for flexible fields.
- All visible strings must go through vue-i18n.
- Commit: `feat(frontend): add dev content activity configuration`

## Notes
Derived from `docs/product/features/frontend/dev-app/FEAT-003-Dev-Content-Activity-Configuration.md`.
This sprint assumes the FEAT-002 category/topic core exists and can provide topic selectors for
activity filtering and forms.

## Review

completed_tasks:
- Contract types for activities, difficulty levels, and activity resources
- Service methods for all CRUD operations
- Pinia store extended with new state and actions
- ActivityList component with topic filter
- ActivityForm component with topic multi-select
- DifficultyLevelList component (requires activity selection)
- DifficultyLevelForm component with JSON validation
- ActivityResourceList component (requires activity selection)
- ActivityResourceForm component with JSON metadata validation
- i18n keys for all new entities
- DevContentView updated with new components

incomplete_tasks:
- None

contract_changes:
- None

learnings:
- JSON editor fields require client-side validation before submit
- Parent-child relationships (activity → difficulty levels, activity → resources) need explicit state tracking
- V1 resource references should be clearly labeled to avoid confusion with file uploads

next_sprint_suggestions:
- FEAT-004: Locales, curiosities, and avatar events management
- Consider adding bulk import/export functionality for content
- Add search/filter capabilities for large datasets
