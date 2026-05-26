# Sprint 005 - frontend
# -----------------------------------------------

## Goal
Implement the development Content Manager app shell: opt-in route, responsive layout, internal
section navigation, and base empty/loading/error states derived from
`docs/product/features/frontend/dev-app/FEAT-001-Dev-Content-App-Shell.md`.

## Status
status: active
started_at: 2026-05-26 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Environment Activation
- [ ] Add `VITE_ENABLE_DEV_CONTENT` documentation/example with default disabled behavior.
- [ ] Implement a strict opt-in check: only `VITE_ENABLE_DEV_CONTENT === 'true'` enables the mini-app.
- [ ] Ensure missing, empty, `false`, or any other value disables the mini-app.

### Routing
- [ ] Add `/dev/content` route to `src/router/index.ts`.
- [ ] Guard `/dev/content` with the strict opt-in check.
- [ ] Redirect direct access to `/` when the feature is disabled.
- [ ] Do not require parental PIN or authenticated session for this development-only tool.

### App Shell View
- [ ] Create `src/views/DevContentView.vue`.
- [ ] Create a responsive shell with sidebar navigation and main content area.
- [ ] Add sections for categories, topics, activities, difficulty levels, resources, locales,
      curiosities, and avatar events.
- [ ] Show a base empty state for each section until CRUD integrations are implemented.
- [ ] Include base loading and error states for future service integration.

### Orientation and Layout
- [ ] Do not apply the base app landscape-only constraint to the dev content mini-app.
- [ ] Ensure the mini-app works in horizontal and vertical orientations.
- [ ] Ensure the mini-app does not show or depend on the rotation overlay.
- [ ] Verify layout responsiveness for desktop, tablet, and mobile widths.

### i18n
- [ ] Add all visible strings to `src/i18n/es.ts`.
- [ ] Do not hardcode visible labels in Vue templates.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
