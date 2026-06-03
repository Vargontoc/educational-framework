# Sprint 015 - frontend
# -----------------------------------------------

## Goal
Implement the Parent Control Settings Section from `docs/product/features/frontend/FEAT-009-Settings-Section`: replace the `/panel` Settings placeholder with real adult-facing family configuration, global TTS and agent controls, PIN update with confirmation, real REST/WebSocket integration, i18n, accessibility, and responsive panel layout. Backend implementation is out of scope.

## Status
status: active
started_at: 2026-06-03 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-009-Settings-Section` before editing.
- [ ] Review `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` for panel shell constraints and Settings placeholder behavior.
- [ ] Review `docs/product/features/frontend/FEAT-001-Base-Styles.md` for adult visual and accessibility tokens.
- [ ] Review `docs/contracts/api/openapi.json` for family settings and PIN update contract behavior.
- [ ] Review `docs/contracts/api/websocket.json` for `CHILD_TTS_ACTIVATED`, `CHILD_TTS_DEACTIVATED`, `CHILD_AGENT_ACTIVATED`, `CHILD_AGENT_DEACTIVATED`, and `SESSION_INVALIDATED`.
- [ ] Review existing panel component structure before replacing the Settings placeholder.
- [ ] Review existing service/store patterns before adding settings services.
- [ ] Do not implement backend code in this sprint.

### Contract And Endpoint Alignment
- [ ] Use OpenAPI-derived types where the endpoint exists in `docs/contracts/api/openapi.json`.
- [ ] Confirm `GET /api/v1/family` is available in frontend API typing or service layer.
- [ ] Confirm the real endpoint for updating global TTS and agent settings with persisted child propagation.
- [ ] Confirm the real endpoint for updating the family PIN and invalidating FamilySessions only.
- [ ] If `PATCH /api/v1/family` is used, send the current `name` because `UpdateFamilyRequest.name` is required.
- [ ] If OpenAPI does not document child propagation or FamilySession-only invalidation, stop and report the backend contract blocker.
- [ ] Do not invent local request/response models that diverge from existing schemas.
- [ ] Do not add mock endpoints, fake events, or local-only propagation state.

### Settings Section Integration
- [ ] Replace the Settings placeholder in `/panel` with the real Settings section.
- [ ] Keep `/panel` protected by the existing in-memory parent token guard.
- [ ] Load current family settings from `GET /api/v1/family`.
- [ ] Show adult-facing loading, empty, and retryable error states.
- [ ] Keep raw parent token in memory only.
- [ ] Do not open GameView from the panel.

### Global TTS Control
- [ ] Render a global TTS toggle from real `FamilyResponse.ttsEnabled` state.
- [ ] Opening TTS disable flow shows a confirmation modal explaining child and active-session effects.
- [ ] Confirming TTS disable calls the real API integration that persists child-level disabled effect.
- [ ] Refresh family settings and affected child state if displayed by the UI.
- [ ] Re-enabling global TTS must not reactivate children manually disabled before.
- [ ] Do not simulate child propagation locally.

### Global Agent Control
- [ ] Render a global agent toggle from real `FamilyResponse.agentEnabled` state.
- [ ] Opening agent disable flow shows a confirmation modal explaining child and active-session effects.
- [ ] Confirming agent disable calls the real API integration that persists child-level disabled effect.
- [ ] Refresh family settings and affected child state if displayed by the UI.
- [ ] Re-enabling global agent must not reactivate children manually disabled before.
- [ ] Do not simulate child propagation locally.

### PIN Update Flow
- [ ] Add Change PIN action inside Settings.
- [ ] Do not ask for the current PIN because the parent is already authenticated in `/panel`.
- [ ] Use the custom numeric keypad pattern with 4 digits.
- [ ] Ask for new PIN and confirmation.
- [ ] Mask digits as indicators; never show PIN digits as plain text.
- [ ] On mismatch, show adult red feedback, shake indicators, clear confirmation entry, and keep the first PIN entry.
- [ ] On matching confirmation, call the real backend integration.
- [ ] On success, clear the current family session from memory and navigate Home.
- [ ] Do not close ChildSessions from the frontend after PIN update.
- [ ] Clear PIN state after success, close, mismatch reset, or unrecoverable error.

### WebSocket Event Handling
- [ ] Use only events documented in `docs/contracts/api/websocket.json`.
- [ ] Handle `CHILD_TTS_ACTIVATED` and `CHILD_TTS_DEACTIVATED` according to GameView feedback rules if this sprint touches GameView event handling.
- [ ] Handle `CHILD_AGENT_ACTIVATED` and `CHILD_AGENT_DEACTIVATED` according to GameView feedback rules if this sprint touches GameView event handling.
- [ ] Use `SESSION_INVALIDATED` for other active panel sessions after PIN update where the existing ParentChannel flow supports it.
- [ ] Do not invent alternate event names or payload fields.
- [ ] Do not show adult toasts in GameView.

### Visual, Responsive, And Accessibility
- [ ] Use adult panel background `#F4F6F9` and white card surfaces.
- [ ] Use cobalt `#2B5BE0` for primary actions and active states.
- [ ] Use adult error red `#E53935` for destructive/error states.
- [ ] Do not use child GameView retry orange in adult panel flows.
- [ ] Keep layout aligned with Parent Control Shell, not GameView.
- [ ] Add all visible strings and aria labels to `src/i18n/es.ts` or existing i18n module.
- [ ] Avoid sustained uppercase visible labels.
- [ ] Ensure all icon buttons have translated accessible labels.
- [ ] Ensure toggles and keypad actions are keyboard operable.
- [ ] Ensure settings states are not color-only.
- [ ] Ensure modals manage focus and return focus to triggers.
- [ ] Keep adult touch targets at least 44px.
- [ ] Verify tablet landscape, mobile landscape, and portrait rotation overlay behavior.

### Testing And Verification
- [ ] Add or update service/component/routing tests if the project has a test harness available for this area.
- [ ] Verify Settings section replaces the placeholder when selected in the panel.
- [ ] Verify missing parent token still redirects `/panel` to Home.
- [ ] Verify initial load calls the real family settings endpoint.
- [ ] Verify TTS toggle renders backend value and submits the real API request on confirmation.
- [ ] Verify agent toggle renders backend value and submits the real API request on confirmation.
- [ ] Verify enabling global TTS/agent does not locally mutate child settings to enabled.
- [ ] Verify PIN flow validates new PIN confirmation.
- [ ] Verify PIN flow clears PIN state after success, close, or mismatch reset.
- [ ] Verify successful PIN update clears session state and navigates Home.
- [ ] Verify ChildSessions are not closed by the PIN update frontend flow.
- [ ] Verify `400`, `404`, network, and `5xx` handling for settings and PIN flows.
- [ ] Verify all visible labels resolve through i18n keys.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **OpenAPI side-effect drift**: OpenAPI may not document child propagation for global settings or FamilySession-only invalidation after PIN update.
  Mitigation: stop implementation and request backend contract update; do not mock propagation.
- **Local-only propagation temptation**: frontend may appear correct while backend state is not persisted.
  Mitigation: use only real API responses and refresh from backend after mutations.
- **Wrong session invalidation**: PIN update may accidentally close ChildSessions.
  Mitigation: do not call child session endpoints from PIN flow; require FamilySession-only contract behavior.
- **WebSocket payload ambiguity**: TTS/agent event names exist but payload semantics may be minimal.
  Mitigation: use only `event` and `sessionId` unless backend documents additional payload fields.
- **Adult UI drift**: Settings may use GameView colors/feedback.
  Mitigation: keep adult panel visual language and adult error semantics.

## Dependencies
- `docs/product/features/frontend/FEAT-009-Settings-Section` - source feature.
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - Parent Control Shell and Settings placeholder.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - visual and accessibility tokens.
- `docs/contracts/api/openapi.json` - family settings and PIN update endpoints.
- `docs/contracts/api/websocket.json` - realtime session/settings events.
- `docs/design/frontend_design_v1.docx` - panel/settings behavior and PIN flow.
- `docs/design/design_decisions_v1.docx` - adult UI and accessibility decisions.

## Agent Instruction
- Implement only `FEAT-009-Settings-Section` frontend work.
- Do not implement backend changes.
- Do not change backend contracts unless explicitly requested.
- Do not add mocks, fake endpoints, fake events, or local-only propagation state.
- Do not implement Children section work already covered by `FEAT-008-Child-Section.md`.
- Do not implement learning domain priority, minigame weight, or seed configuration.
- Do not close ChildSessions after PIN update.
- Keep parent token in memory only.
- Use shared Axios services for API calls.
- Use only WebSocket events documented in `docs/contracts/api/websocket.json`.
- Use Vue Router, Pinia, Vue i18n, and TypeScript according to the frontend stack.
- Keep all visible strings and aria labels in i18n.
- Commit: `feat(frontend): add parent settings section`

## Notes
Derived from `docs/product/features/frontend/FEAT-009-Settings-Section`.

Design output:
- View/feature: adult-facing Settings section inside Parent Control Shell.
- Data flow: family settings load, settings mutation, PIN update, family session cleanup, contracted realtime events.
- Component tree: `/panel` -> `PanelControlView` Settings section -> settings cards/toggles + confirm modals + PIN keypad flow.
- Contract dependency: OpenAPI family settings/PIN behavior and WebSocket session/settings events.
- Risks: OpenAPI side-effect drift, local-only propagation, wrong session invalidation, WebSocket payload ambiguity.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
