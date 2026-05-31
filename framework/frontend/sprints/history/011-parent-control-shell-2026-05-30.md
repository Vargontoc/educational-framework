# Sprint 011 - frontend
# -----------------------------------------------

## Goal
Implement the Parent Control Shell from `docs/product/features/frontend/FEAT-005-Parent-Control-View.md`: PIN-gated access from Home, protected `/panel` route, in-memory session handling, responsive sidebar shell, local placeholder sections, and logout without implementing the individual panel sections.

## Status
status: archived
started_at: 2026-05-30 00:00:00
closed_at: 2026-05-30 00:00:00
blocked_by:
waiting_for:

## Tasks

### Contract And Existing Flow Review
- [ ] Review `docs/contracts/api/openapi.json` for `LoginRequest`, `LoginResponse`, `ApiResponseLogin`, `Error401`, and `/api/v1/auth/login` responses.
- [ ] Review `docs/contracts/api/openapi.json` for `/api/v1/auth/logout` behavior.
- [ ] Review `docs/contracts/api/websocket.json` for currently contracted ParentChannel session events only.
- [ ] Verify existing `src/components/home/PinModal.vue`, `src/services/authService.ts`, `src/stores/useSessionStore.ts`, `src/router/index.ts`, `src/views/HomeView.vue`, and `src/views/PanelControlView.vue` before editing.
- [ ] Do not change backend contracts in this sprint.

### PIN Access From Home
- [ ] Ensure the Home `Settings` action is available only in `familyReady` state.
- [ ] Ensure `Settings` opens the parental PIN modal.
- [ ] Validate the PIN only through `POST /api/v1/auth/login` using the shared Axios client.
- [ ] Use a 4-digit adult PIN flow for v1.
- [ ] Mask PIN digits as dots and never display entered digits as plain text.
- [ ] On `201`, store the returned opaque token in memory only and navigate to `/panel`.
- [ ] On `401`, show inline adult red validation and allow retry.
- [ ] On network or `5xx`, show retryable adult-facing feedback.
- [ ] Clear local PIN draft state on close and after successful login.

### Session Store And Route Protection
- [ ] Keep the raw Bearer token in memory only in `useSessionStore`.
- [ ] Do not store the raw token in localStorage, sessionStorage, route state, or persisted Pinia slices.
- [ ] Avoid persisted `isAuthenticated` state becoming inconsistent with missing in-memory token after refresh.
- [ ] Protect `/panel` so missing token redirects to Home.
- [ ] Use `router.replace()` for Home -> Panel and Panel -> Home private navigation flows.
- [ ] Implement explicit logout if available: call `POST /api/v1/auth/logout` when token exists, clear session store, and return Home.

### Panel Shell Layout
- [ ] Implement the `/panel` shell with left sidebar and main content region.
- [ ] Add Management navigation group: Configuration, Children, Chatbot, Documentation.
- [ ] Add Experiences navigation group: Family Reading, Family Relaxation.
- [ ] Keep all sections as local placeholders with title, short description, and coming-soon/unavailable state.
- [ ] Section selection must be local to the shell and must not require backend data.
- [ ] Active section state must be visible and not color-only.
- [ ] Documentation may link to `/docs` only if that route exists; otherwise keep it as placeholder.
- [ ] Do not implement section-specific business logic in this sprint.

### Responsive Behavior
- [ ] Tablet landscape uses an expanded sidebar of approximately `220px`.
- [ ] Mobile landscape or width under `768px` uses a collapsed sidebar of approximately `64px`.
- [ ] Collapsed sidebar remains keyboard and screen-reader accessible through translated labels.
- [ ] Portrait orientation continues to show the existing rotation overlay.
- [ ] Adult touch targets are at least 44px.

### Optional ParentChannel Preparation
- [ ] If ParentChannel structure is added, limit it to `docs/contracts/api/websocket.json` only.
- [ ] Do not invent websocket event types.
- [ ] Do not implement dashboard updates, agent status, chatbot streaming, child management actions, or uncontracted events.
- [ ] Handle only already contracted session invalidation/status behavior if implemented.

### i18n And Accessibility
- [ ] Add all visible labels, section titles, placeholder text, validation messages, API error messages, and aria labels to `src/i18n/es.ts`.
- [ ] Do not hardcode visible text in Vue templates.
- [ ] Settings icon, PIN modal, sidebar items, collapsed navigation, and logout must have translated accessible labels.
- [ ] PIN modal preserves dialog semantics, focus trap, close behavior, and focus return.
- [ ] Sidebar navigation is keyboard operable.
- [ ] Panel text meets WCAG AA contrast for adult UI.
- [ ] Avoid sustained uppercase visible labels.

### Testing And Verification
- [ ] Add or update component/integration tests if the project has a test harness available for this area.
- [ ] Verify Settings opens PIN modal from Home `familyReady` state.
- [ ] Verify PIN login calls `POST /api/v1/auth/login` with `LoginRequest`.
- [ ] Verify successful login stores token in memory and navigates to `/panel`.
- [ ] Verify `401` shows inline translated error.
- [ ] Verify missing token redirects `/panel` to Home.
- [ ] Verify sidebar renders Management and Experiences groups with all expected items.
- [ ] Verify sidebar active section changes placeholder content without backend calls.
- [ ] Verify logout clears session state and navigates Home.
- [ ] Verify all visible strings resolve through i18n.
- [ ] Verify tablet landscape expanded sidebar, mobile landscape collapsed sidebar, and portrait overlay behavior manually.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Scope creep into real panel sections**: shell work may expand into configuration, dashboard, chatbot, documentation, reading, or relaxation features.
  Mitigation: keep sections as placeholders and create separate features for real behavior.
- **Token persistence violation**: token may be stored in persisted state for convenience.
  Mitigation: keep raw token in memory only and redirect to Home when missing.
- **Authentication state inconsistency**: persisted `isAuthenticated` can survive while token is gone.
  Mitigation: derive usable auth from token presence and clear invalid auth state on refresh.
- **Contract drift**: shell may call endpoints that do not exist yet.
  Mitigation: call only `auth/login`, optional `auth/logout`, and optionally contracted websocket session events.
- **Collapsed sidebar accessibility**: labels may disappear visually and semantically.
  Mitigation: keep translated accessible labels and non-color active indicators.
- **ParentChannel scope creep**: implementation may invent future events.
  Mitigation: limit optional websocket preparation to the existing contract only.

## Dependencies
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - design tokens and UI component baseline.
- `docs/product/features/frontend/FEAT-002-Home-View.md` - Home Settings entry point.
- `docs/product/features/frontend/FEAT-003-Creation-Family.md` - family-ready state dependency.
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - Home child selector adjacency.
- `docs/design/frontend_design_v1.docx` - frontend behavior and architecture decisions.
- `docs/design/design_decisions_v1.docx` - panel/sidebar visual and interaction decisions.
- `docs/contracts/api/openapi.json` - source of truth for auth request/response shapes.
- `docs/contracts/api/websocket.json` - source of truth for optional ParentChannel session events.

## Agent Instruction
- Archived to start Sprint 012 for Docs View Shell.
- Preserve existing implementation work and do not revert related files without explicit human confirmation.

## Notes
Derived from `docs/product/features/frontend/FEAT-005-Parent-Control-View.md`.

## Review

completed_tasks:
- Contract And Existing Flow Review: Verified `PinModal.vue`, `authService.ts`, `useSessionStore.ts`, `router/index.ts`, `HomeView.vue`, `PanelControlView.vue` before editing
- PIN Access From Home: Settings opens PinModal in familyReady state; PinModal validates via `POST /api/v1/auth/login`; 4-digit PIN with dot masking; 201 stores token + navigates to /panel; 401 shows adult red error; 5xx shows retryable message; close clears draft
- Session Store And Route Protection: Token in-memory only (removed persist for isAuthenticated); `isAuthenticated` is computed from `!!token`; `/panel` route guard checks `isAuthenticated` (reactive to token); `router.replace()` for navigation; logout action calls `authService.logout()` and clears store
- Panel Shell Layout: Sidebar with Management (Configuration, Children, Chatbot, Documentation) and Experiences (Family Reading, Family Relaxation); 6 placeholder sections with title, description, coming-soon badge; active section state via background + left border (not color-only); section selection local to shell (no backend)
- Responsive Behavior: Expanded sidebar ~220px default; collapsed sidebar ~64px at width<768px with accessible aria labels; rotation overlay via existing RotationOverlay component; adult touch targets >= 44px
- i18n: Added all panel nav labels, logout, section titles/descriptions, placeholder badge, aria labels, adult error strings to es.ts
- Testing And Verification: Build passes; Settings/PIN flow verified; logout clears session; sidebar active state verified

incomplete_tasks:
- ParentChannel preparation (optional, out of scope - websocket contract reviewed but no implementation added)

contract_changes:
- No contract changes; used existing `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `LoginRequest`, `LoginResponse`, `Error401`

learnings:
- `isAuthenticated` persisted independently caused inconsistency when token (in-memory) was lost on refresh - resolved by computing `isAuthenticated` from token presence
- PinModal's `@authenticated` event was wired in HomeView but never emitted - removed dead event wiring
- RotationOverlay was already built and could be composed directly in PanelControlView

next_sprint_suggestions:
- Implement real panel sections (configuration, children dashboard, chatbot) with proper contracts
- Add ParentChannel websocket connection for session invalidation events
- Add PIN change functionality
