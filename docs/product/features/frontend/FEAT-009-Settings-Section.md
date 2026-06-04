# FEAT-009 - Frontend: Parent Control Settings Section

## Status

state: proposal
user_history: Manage family configuration from the parental control panel
depends_on: FEAT-001-Base-Styles, FEAT-005-Parent-Control-View, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: frontend implementation of the `/panel` Settings section for family-level configuration, global TTS and agent controls, PIN update flow, and integration with real REST/WebSocket contracts. No backend implementation, mock state, or fake events are included in this feature.
test: parent can load real family settings, disable/enable global TTS and agent settings through real API integration, verify disabled global settings persist their effect on children, update the family PIN with confirmation, close only family sessions after PIN update, and react only to contracted realtime events.

## Description

This feature implements the real Settings section inside the Parent Control Shell.

The section lets the parent manage family-level configuration from `/panel`: global TTS availability, global agent/avatar availability, and family PIN update. The frontend must use real API and WebSocket contracts only. If the required backend behavior or contract entries are missing, the affected part of the feature is blocked until backend updates the contracts.

The section is adult-facing. It must keep the Parent Control visual language and must not use GameView child feedback semantics.

## Product Rules

- The Settings section is available only inside protected `/panel` after parental PIN login.
- Raw parent token remains in memory only through the existing session strategy.
- The section uses adult UI semantics and adult validation colors.
- Adult errors use red `#E53935`; child GameView retry orange must not be used here.
- Global TTS and agent settings act as family-level ceilings for every child.
- Disabling global TTS must persist the effective disabled state on all children that currently have TTS enabled.
- Disabling global agent must persist the effective disabled state on all children that currently have agent enabled.
- Re-enabling global TTS or agent only removes the family-level ceiling; it must not reactivate children that were disabled individually.
- The frontend must not simulate propagation locally, invent response data, or use mocks for production feature behavior.
- All visible copy and aria labels must use Vue i18n.
- Sustained uppercase visible labels must be avoided.

## UX Flow

### Enter Settings Section

- Parent logs into `/panel` through the existing PIN flow.
- Parent selects the Settings item in the panel sidebar.
- The section loads current family settings from the backend.
- If loading fails, show adult-facing retryable feedback.

### Global TTS Toggle

- The toggle reflects `FamilyResponse.ttsEnabled` from the backend.
- Turning TTS off opens a confirmation modal explaining that the setting will affect children and active GameView sessions.
- Confirming calls a real backend integration that persists the family setting and the child-level disabled effect.
- After success, refresh family settings and child data if needed by the UI.
- Turning TTS on only enables the family-level ceiling and must not reactivate children manually disabled before.
- If the backend contract does not document the required child propagation behavior, this flow is blocked.

### Global Agent Toggle

- The toggle reflects `FamilyResponse.agentEnabled` from the backend.
- Turning agent off opens a confirmation modal explaining that the setting will affect children and active GameView sessions.
- Confirming calls a real backend integration that persists the family setting and the child-level disabled effect.
- After success, refresh family settings and child data if needed by the UI.
- Turning agent on only enables the family-level ceiling and must not reactivate children manually disabled before.
- If the backend contract does not document the required child propagation behavior, this flow is blocked.

### PIN Update

- The parent does not need to verify the current PIN because access to `/panel` already required a valid family session.
- Pressing Change PIN opens a custom numeric keypad flow.
- The parent enters a new 4-digit PIN.
- The parent confirms the new 4-digit PIN.
- Digits are shown as masked indicators, never as plain text.
- If confirmation does not match, show adult red error feedback, shake the indicators, clear the confirmation entry, and keep the first PIN entry.
- Confirming a matching PIN calls a real backend integration.
- After successful PIN update, the frontend clears the current family session from memory and returns to Home.
- The backend must invalidate only FamilySessions after PIN update.
- Active ChildSessions in GameView must not be closed only because the PIN changed.
- Other panel instances must react through a real contracted family-session invalidation event.

## API Integration

Use the shared Axios client and request/response shapes derived from `docs/contracts/api/openapi.json`.

### Required Endpoints

- `GET /api/v1/family` to read current family settings.
- A real contract-backed endpoint to update global TTS and agent settings with persisted child propagation.
- A real contract-backed endpoint to update the family PIN and invalidate FamilySessions.

### Current Contract Risk

`PATCH /api/v1/family` currently accepts `UpdateFamilyRequest` with `name`, `pin`, `ttsEnabled`, and `agentEnabled`, but the contract must explicitly document whether this endpoint persists child-level propagation and invalidates FamilySessions after PIN update.

If `PATCH /api/v1/family` is used:

- The frontend must send the current `name` because `UpdateFamilyRequest.name` is required.
- The backend contract must document that disabling `ttsEnabled` persists child TTS disabled effects.
- The backend contract must document that disabling `agentEnabled` persists child agent disabled effects.
- The backend contract must document that PIN update invalidates FamilySessions only, not ChildSessions.

If these behaviors are not documented in `docs/contracts/api/openapi.json`, frontend implementation must stop and report the backend contract blocker. Do not implement local-only behavior to hide the missing integration.

## WebSocket Integration

Use only events documented in `docs/contracts/api/websocket.json`.

### Required Realtime Behavior

- Active GameView sessions must receive real contracted events when global TTS or agent availability changes.
- TTS availability changes use `CHILD_TTS_ACTIVATED` and `CHILD_TTS_DEACTIVATED` from `docs/contracts/api/websocket.json`.
- Agent availability changes use `CHILD_AGENT_ACTIVATED` and `CHILD_AGENT_DEACTIVATED` from `docs/contracts/api/websocket.json`.
- Other active panel sessions must receive a real contracted event when FamilySessions are invalidated after a PIN update.

### Contract Note

`docs/contracts/api/websocket.json` defines the required TTS and agent event names in `SessionEvent.payload.event`.

The frontend must not invent alternate events such as local `TTS_DISABLED`, `AGENT_DISABLED`, or similar names. If GameView needs additional event payload fields beyond `event` and `sessionId`, those fields must be documented in the WebSocket contract before implementation uses them.

GameView must not show adult toasts. Any GameView reaction to contracted TTS or agent events must follow GameView feedback rules: visual/audio/avatar state updates, not adult notifications.

## State Management

- Use existing session store for parent auth token and family session state.
- Keep raw token in memory only.
- Use a settings store or local section state according to existing frontend patterns.
- Stores call services; services call Axios.
- Keep PIN draft state local to the PIN modal/flow.
- Do not persist PIN values, settings drafts, or fake propagation state.
- Clear PIN state after success, close, mismatch reset, or unrecoverable error.

## Visual Requirements

- Use Nunito and global design tokens from `FEAT-001-Base-Styles`.
- Use adult panel background `#F4F6F9`.
- Use white card surfaces `#FFFFFF`.
- Use cobalt blue `#2B5BE0` for primary actions and active states.
- Use adult error red `#E53935` for destructive/error states.
- Use success green `#43A047` for successful adult confirmations where appropriate.
- Use neutral text `#1A2340` and secondary text `#6B7A99`.
- Keep layout aligned with the Parent Control Shell, not GameView.
- Avoid sustained uppercase visible labels.

Responsive behavior:

- Tablet landscape can use a two-column settings layout.
- Mobile landscape can use a compact single-column layout.
- The existing collapsed sidebar remains accessible.
- Portrait orientation continues to show the existing rotation overlay.

## Accessibility

- All icon buttons must have translated `aria-label` values.
- Toggles and keypad actions must be keyboard operable.
- Settings states must include text or icon/shape, not color alone.
- Confirmation modals must use accessible dialog semantics.
- Focus moves into modals when opened and returns to the trigger after close.
- Adult touch targets must be at least `44px`.
- Text contrast must meet WCAG AA for adult UI.

## Out Of Scope

- Backend implementation changes.
- Backend contract changes unless explicitly requested.
- Mock endpoints, mock events, fake settings state, or local-only propagation.
- Children section implementation covered by `FEAT-008-Child-Section.md`.
- Child dashboard/progress details.
- Learning domain priority configuration.
- Minigame weight, priority, or seed configuration per child.
- Chatbot/AdultAgent integration.
- Direct TTS, agent, or Coqui calls.
- Closing ChildSessions after PIN update.

## Acceptance Criteria

- The Settings sidebar item renders the real Settings section instead of a placeholder.
- `/panel` still requires an in-memory parent token.
- The section loads current family settings from the backend.
- Global TTS toggle reflects real backend state.
- Global agent toggle reflects real backend state.
- Disabling global TTS calls real API integration that persists the disabled effect on children.
- Disabling global agent calls real API integration that persists the disabled effect on children.
- Re-enabling global TTS or agent does not reactivate children manually disabled before.
- If the required backend behavior or OpenAPI contract is missing, the implementation reports the blocker instead of adding local-only behavior.
- PIN update uses a custom 4-digit keypad with new PIN and confirmation.
- PIN update does not ask for the current PIN.
- PIN digits are masked and never persisted or logged.
- Mismatched PIN confirmation shows adult-facing error feedback and clears only the confirmation entry.
- Successful PIN update clears the current family session and returns to Home.
- PIN update invalidates FamilySessions only; active ChildSessions are not closed by this flow.
- Realtime behavior uses only events documented in `docs/contracts/api/websocket.json`.
- TTS realtime behavior uses `CHILD_TTS_ACTIVATED` and `CHILD_TTS_DEACTIVATED`.
- Agent realtime behavior uses `CHILD_AGENT_ACTIVATED` and `CHILD_AGENT_DEACTIVATED`.
- GameView does not show adult toasts for TTS/agent setting changes.
- `400`, `404`, network, and `5xx` responses show adult-facing translated feedback.
- All visible strings and aria labels use Vue i18n.
- Adult red is used for adult errors/destructive feedback; child retry orange is not used.
- Tablet landscape, mobile landscape, and portrait overlay behavior are verified.

## Testing Notes

Required tests:

- Settings section replaces the placeholder when selected in the panel.
- Missing parent token still redirects `/panel` to Home.
- Initial load calls the real family settings endpoint.
- TTS toggle renders the backend value and submits the real API request on confirmation.
- Agent toggle renders the backend value and submits the real API request on confirmation.
- Disabling global TTS refreshes settings and affected child state if the UI displays it.
- Disabling global agent refreshes settings and affected child state if the UI displays it.
- Enabling global TTS/agent does not locally mutate child settings to enabled.
- PIN flow validates new PIN confirmation.
- PIN flow clears PIN state after success, close, or mismatch reset.
- Successful PIN update clears the session store and navigates to Home.
- ChildSessions are not closed by the PIN update frontend flow.
- `400`, `404`, network, and `5xx` handling for settings and PIN flows.
- All visible labels resolve through i18n keys.

Manual checks:

- Tablet landscape settings layout.
- Mobile landscape settings layout.
- Portrait rotation overlay.
- Keyboard operation for toggles, keypad, actions, and modals.
- Focus return after confirmation and PIN modals.

## Risks And Mitigations

- Risk: Existing OpenAPI does not document child propagation side effects for global settings.
  Mitigation: stop implementation and request backend contract update; do not mock propagation.
- Risk: Existing WebSocket contract defines TTS/agent event names but not detailed payload semantics.
  Mitigation: use only `event` and `sessionId` unless backend documents additional payload fields.
- Risk: PIN update accidentally closes ChildSessions.
  Mitigation: specify FamilySession-only invalidation and verify frontend does not call child session endpoints.
- Risk: Frontend hides missing backend behavior with local-only state.
  Mitigation: use only real API responses and contracted events.
- Risk: Adult panel drifts into GameView styling.
  Mitigation: use adult panel background, white surfaces, adult red errors, and compact adult layout.

## Dependencies

- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - adult panel visual tokens and interaction baseline.
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - Parent Control Shell and Settings placeholder section.
- `docs/contracts/api/openapi.json` - family settings and PIN update endpoints.
- `docs/contracts/api/websocket.json` - realtime session/settings events.
- `docs/design/frontend_design_v1.docx` - Parent Control settings behavior and PIN flow direction.
- `docs/design/design_decisions_v1.docx` - adult visual register, accessibility, and panel decisions.

## Agent Instruction

- Implement only the frontend Settings section inside `/panel`.
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
