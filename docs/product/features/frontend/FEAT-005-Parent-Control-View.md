# FEAT-005 - Frontend: Parent Control Shell

## Status

state: proposal
user_history: Parent control panel shell after PIN access from Home
depends_on: FEAT-001-Base-Styles, FEAT-002-Home-View, FEAT-003-Creation-Family, FEAT-004-Modal-Creation-Child, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: frontend view + API integration using `docs/contracts/api/openapi.json`. ParentChannel preparation may use `docs/contracts/api/websocket.json` only for already contracted session events. No backend implementation is included in this feature.
test: component + integration + manual responsive checks

## Description

This feature defines the initial Parent Control shell. It includes the PIN-gated access from Home, route protection for `/panel`, and the responsive panel layout with sidebar navigation groups.

The individual panel sections are intentionally placeholders in this feature. Configuration, children dashboards, chatbot, documentation rendering, family reading, and relaxation experiences must be implemented later in their own features once their contracts and product details are defined.

## Product Rules

- The panel is an adult-facing space: clarity and control take priority over immersive child-world visuals.
- Access starts from the Home `Settings` icon when a family exists.
- The PIN is validated only by the backend through `POST /api/v1/auth/login`.
- The returned opaque Bearer token is stored in memory only through `useSessionStore`.
- The token must not be stored in `localStorage`, `sessionStorage`, route state, or persisted Pinia slices.
- Refreshing the page loses the token by design and requires PIN login again.
- The shell must not implement business logic for child management, chatbot, documentation, reading, or relaxation.
- All visible copy must come from Vue i18n.

## UX Flow

### Entry Point - Home Settings

- `HomeView` is in `familyReady` state.
- The `Settings` icon is visible.
- Pressing `Settings` opens the parental PIN modal.
- PIN length is 4 digits for v1.
- PIN digits must be masked as dots and never displayed as plain text.
- The modal uses adult validation semantics.

### PIN Login

- Submit the PIN to `POST /api/v1/auth/login` through the shared Axios client.
- On `201`, store the returned token in `useSessionStore` in memory and navigate to `/panel`.
- On `401`, show an inline red adult-facing error and allow retry.
- On network or `5xx`, show a retryable adult-facing error.
- Closing the modal clears local PIN draft state.

### Panel Route Protection

- `/panel` requires an authenticated in-memory session token.
- If the token is missing, redirect to Home.
- Use `router.replace()` for panel navigation flows to avoid meaningful browser history inside the private shell.
- On explicit logout, call `POST /api/v1/auth/logout` when a token exists, clear `useSessionStore`, and return Home.

## Panel Shell Structure

The shell contains a left sidebar and a main content region.

Navigation groups:
- Management: Configuration, Children, Chatbot, Documentation.
- Experiences: Family Reading, Family Relaxation.

Initial section behavior:
- Each section renders a placeholder title, short description, and unavailable/coming-soon state.
- The active section has a visible state in the sidebar.
- Navigation between sections is local to the shell and must not require backend data.
- Documentation may link to `/docs` if that route exists; otherwise it remains a placeholder.

Responsive behavior:
- Tablet landscape: expanded sidebar, approximately `220px`, icon/label capable.
- Mobile landscape or width under `768px`: collapsed sidebar, approximately `64px`, icon-oriented labels with accessible names.
- Portrait orientation continues to show the existing rotation overlay.

## ParentChannel Preparation

The shell may prepare the structure for ParentChannel connection, but must not invent event types.

Allowed contract reference:
- `docs/contracts/api/websocket.json`
- `/ws/parent`
- `/topic/family/{familyId}/sessions`
- contracted `SessionEvent` values only.

If ParentChannel wiring is implemented in this feature, it must be limited to connection status and session invalidation handling that is already present in the contract. Dashboard updates, agent status, chatbot streaming, and child management actions are out of scope unless contracted later.

## API Integration

Required endpoints:
- `POST /api/v1/auth/login` with `LoginRequest`.
- `POST /api/v1/auth/logout` for explicit logout when available.

Request and response shapes must be derived from `docs/contracts/api/openapi.json`:
- `LoginRequest`
- `LoginResponse`
- `ApiResponseLogin`
- `Error401`

The shell must not call endpoints for configuration, dashboards, chatbot, documentation, reading, relaxation, or child control until those are defined in later feature contracts.

## State Management

- Use `useSessionStore` for authenticated session state.
- Keep the raw token in memory only.
- Do not persist `isAuthenticated` independently if it can become inconsistent with a missing token.
- Keep local sidebar active-section state in the panel view unless it needs to be shared.
- Stores must call services only; services call Axios.

## Visual Requirements

- Use Nunito and the global design tokens from `FEAT-001-Base-Styles`.
- Panel background uses adult neutral panel background `#F4F6F9`.
- Surface cards use white `#FFFFFF`.
- Primary action and active navigation use cobalt blue `#2B5BE0`.
- Experience group uses a warm/gold accent `#F5A623`.
- Adult validation error uses red `#E53935`.
- Sidebar separates Management from Experiences visually.
- Avoid sustained uppercase visible labels.

## Accessibility

- `Settings` icon and all sidebar items must have translated accessible labels.
- PIN modal must preserve dialog semantics, focus trap, close behavior, and focus return.
- Sidebar navigation must be keyboard operable.
- Collapsed sidebar must remain accessible through `aria-label` or equivalent visible/assistive text.
- Active section state must not be color-only.
- Adult touch targets must be at least 44px.
- Panel text must meet WCAG AA contrast for adult UI.

## Out Of Scope

- Configuration section implementation.
- Children dashboard implementation.
- Chatbot or AdultAgent implementation.
- Documentation rendering implementation.
- Family Reading implementation.
- Family Relaxation implementation.
- Blocking/unblocking children.
- Closing or expelling child sessions from the panel.
- Editing family or child data.
- Any endpoint not currently defined in `docs/contracts/api/openapi.json`.
- Any websocket event not currently defined in `docs/contracts/api/websocket.json`.
- Backend implementation or contract changes.

## Acceptance Criteria

- The `Settings` action on Home opens the parental PIN modal when a family exists.
- The PIN modal validates through `POST /api/v1/auth/login` only.
- On successful login, the token is stored only in memory and the app navigates to `/panel`.
- On failed PIN, an inline adult red error is shown and the user can retry.
- `/panel` redirects to Home when the in-memory token is missing.
- The panel renders a responsive sidebar with Management and Experiences groups.
- Sidebar items include Configuration, Children, Chatbot, Documentation, Family Reading, and Family Relaxation.
- Selecting a sidebar item updates the active placeholder section without backend calls.
- Active item state is visible and not color-only.
- Mobile landscape/collapsed sidebar remains keyboard and screen-reader accessible.
- Explicit logout clears session state and returns Home.
- All visible strings are provided by Vue i18n.
- Portrait orientation continues to show the existing rotation overlay.

## Testing Notes

Required tests:
- Settings opens PIN modal from Home `familyReady` state.
- PIN login calls `POST /api/v1/auth/login` with `LoginRequest`.
- Successful login stores token in memory and navigates to `/panel`.
- `401` shows inline translated error.
- Missing token redirects `/panel` to Home.
- Sidebar renders all expected groups and items.
- Sidebar active section changes placeholder content without backend calls.
- Logout clears session state and navigates Home.
- All visible labels resolve through i18n keys.

Manual checks:
- Tablet landscape expanded sidebar.
- Mobile landscape collapsed sidebar.
- Portrait rotation overlay still covers the flow.
- Keyboard operation for PIN modal and sidebar.

## Risks And Mitigations

- Risk: The shell grows into full section implementation.
  Mitigation: keep each section as a placeholder and create separate features for real behavior.
- Risk: Token persistence violates the authentication strategy.
  Mitigation: keep raw token in memory only and redirect to Home when missing.
- Risk: Contract drift for panel sections.
  Mitigation: do not call endpoints that are not present in `openapi.json` or `websocket.json`.
- Risk: Sidebar collapsed mode becomes inaccessible.
  Mitigation: provide accessible labels and non-color active indicators.
- Risk: ParentChannel implementation invents events.
  Mitigation: limit optional wiring to the current websocket contract only.
