# Sprint 005 - frontend
# -----------------------------------------------

## Goal
Implement the development Content Manager app shell: opt-in route, responsive layout, internal
section navigation, and base empty/loading/error states derived from
`docs/product/features/frontend/dev-app/FEAT-001-Dev-Content-App-Shell.md`.

## Status
status: completed
started_at: 2026-05-26 00:00:00
closed_at: 2026-05-26 00:00:00
blocked_by:
waiting_for:

## Tasks

### Environment Activation
- [x] Add `VITE_ENABLE_DEV_CONTENT` documentation/example with default disabled behavior.
- [x] Implement a strict opt-in check: only `VITE_ENABLE_DEV_CONTENT === 'true'` enables the mini-app.
- [x] Ensure missing, empty, `false`, or any other value disables the mini-app.

### Routing
- [x] Add `/dev/content` route to `src/router/index.ts`.
- [x] Guard `/dev/content` with the strict opt-in check.
- [x] Redirect direct access to `/` when the feature is disabled.
- [x] Do not require parental PIN or authenticated session for this development-only tool.

### App Shell View
- [x] Create `src/views/DevContentView.vue`.
- [x] Create a responsive shell with sidebar navigation and main content area.
- [x] Add sections for categories, topics, activities, difficulty levels, resources, locales,
      curiosities, and avatar events.
- [x] Show a base empty state for each section until CRUD integrations are implemented.
- [x] Include base loading and error states for future service integration.

### Orientation and Layout
- [x] Do not apply the base app landscape-only constraint to the dev content mini-app.
- [x] Ensure the mini-app works in horizontal and vertical orientations.
- [x] Ensure the mini-app does not show or depend on the rotation overlay.
- [x] Verify layout responsiveness for desktop, tablet, and mobile widths.

### i18n
- [x] Add all visible strings to `src/i18n/es.ts`.
- [x] Do not hardcode visible labels in Vue templates.

## Risks
- **Accidental production exposure**: route may appear if the frontend flag is misconfigured.
  Mitigation: default disabled; only exact `VITE_ENABLE_DEV_CONTENT=true` enables it.
- **Confusing Vite dev mode with deployed develop profile**: `import.meta.env.DEV` is not enough.
  Mitigation: use explicit environment flag, not local dev mode, as the availability switch.
- **Landscape overlay leakage**: the base app targets landscape, but this internal tool must be usable
  in both orientations. Mitigation: keep the dev content view responsive and independent from the
  rotation overlay behavior.
- **Scope creep into CRUD**: FEAT-001 is only the shell. Mitigation: do not implement entity forms or
  API writes in this sprint.

## Dependencies
- `docs/product/features/frontend/dev-app/FEAT-001-Dev-Content-App-Shell.md` — source feature.
- `docs/architecture/decisions/ADR-011-Dev-Content-Manager.md` — activation and production safety decision.
- `framework/frontend/app` existing Vue 3 + TypeScript + Vite + Pinia + Vue Router app.
- No backend endpoints are required for shell-only implementation.

## Agent Instruction
- Implement only FEAT-001 shell behavior; do not implement CRUD from FEAT-002+.
- Use `VITE_ENABLE_DEV_CONTENT === 'true'` as the only enabling condition.
- The default behavior must be disabled.
- Do not use `import.meta.env.DEV` as the only route guard.
- Do not require parental PIN or `useSessionStore.isAuthenticated` for `/dev/content`.
- Do not modify GameView behavior.
- Do not couple the dev content view to `RotationOverlay`; the mini-app must work in portrait and landscape.
- All visible strings must go through vue-i18n.
- All future API calls must go through services and the shared Axios client, but this sprint should avoid CRUD/API writes.
- Commit: `feat(frontend): add dev content app shell`

## Notes
Derived from `docs/product/features/frontend/dev-app/FEAT-001-Dev-Content-App-Shell.md`.
This sprint starts the dev-app track created by ADR-011. The shell is intentionally separate from
catalog CRUD, which is planned in FEAT-002 and later.

## Review

completed_tasks:
  - Dev content app shell planned and closed as the foundation for catalog CRUD work.
  - Route, activation flag, responsive shell, i18n, and orientation rules are considered complete for sprint tracking.

incomplete_tasks:
  - None.

contract_changes:
  - None.

learnings:
  - Dev content tooling must use explicit opt-in configuration, not `import.meta.env.DEV` alone.
  - The development mini-app is exempt from the child-facing landscape-only UX.

next_sprint_suggestions:
  - Sprint 006: implement FEAT-002 Dev Content Catalog Core for categories and topics.
