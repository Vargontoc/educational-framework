# FEAT-008 - Frontend: Parent Control Children Section

## Status

state: accepted
user_history: Manage children from the parental control panel
depends_on: FEAT-001-Base-Styles, FEAT-005-Parent-Control-View, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: frontend implementation of the `/panel` Children section + API integration for child profile management and child session status. ParentChannel may be used only for already contracted session events. No backend implementation is included in this feature.
test: parent can view children, edit child profile, toggle child activation, delete child, see active session time updated every 5 seconds, and close an active child session.

## Description

This feature implements the real Children section inside the Parent Control Shell.

The section lets the parent manage registered children from `/panel`: view child cards, inspect active session state, edit profile data, enable or disable child-level TTS and agent flags, block/unblock GameView access through the child `active` flag, delete a child profile, and close an active child session.

The section is adult-facing. It must keep the Parent Control visual language and must not use GameView child feedback semantics.

## Product Rules

- The Children section is available only inside protected `/panel` after parental PIN login.
- Raw parent token remains in memory only through the existing session strategy.
- The section uses adult UI semantics and adult validation colors.
- Adult errors use red `#E53935`; child GameView retry orange must not be used here.
- Child profile `active=false` means the child is blocked from entering GameView.
- Blocking a child uses the backend activation toggle endpoint; frontend must not duplicate backend session-closing logic.
- Deleting a child is destructive and uses a confirmation modal.
- Editing, activation toggle, and deletion rely on backend behavior to close or affect active sessions when applicable.
- Session status shown in cards is refreshed every 5 seconds while the Children section is active.
- All visible copy and aria labels must use Vue i18n.
- Sustained uppercase visible labels must be avoided.

## UX Flow

### Enter Children Section

- Parent logs into `/panel` through the existing PIN flow.
- Parent selects the Children item in the panel sidebar.
- The section loads family data, child profiles, and active child sessions.
- Children are shown as cards in the main panel content region.
- If loading fails, show adult-facing retryable feedback.

### Child Card

Each child card shows:

- Child avatar.
- Child name.
- Active/blocked status badge based on `ChildProfileResponse.active`.
- TTS enabled/disabled status.
- Agent enabled/disabled status.
- Active session badge when a matching `ChildSessionResponse` exists with `status: ACTIVE`.
- Live session duration calculated from `ChildSessionResponse.startedAt` and refreshed every 5 seconds.
- Edit action.
- Block or unblock action based on `active`.
- Close session action only when an active session exists.

The active/blocked and session states must not rely on color alone.

### Edit Child Profile

- Pressing Edit opens an adult-facing modal.
- The modal allows editing:
  - name.
  - birthday.
  - avatar if the existing avatar selector pattern can be reused without scope creep.
  - `ttsEnabled`.
  - `agentEnabled`.
- Name and birthday are required because `UpdateChildProfileRequest` requires both.
- TTS and agent toggles are available only when the family-level setting is enabled.
- If family-level TTS is disabled, the child TTS toggle is disabled and a short translated message explains that audio is disabled at family level.
- If family-level agent is disabled, the child agent toggle is disabled and a short translated message explains that the avatar/agent is disabled at family level.
- Submitting calls `PATCH /api/v1/family/children/{id}`.
- On success, close the modal, refresh children and sessions, and update the card.
- On `400`, show inline adult validation.
- On `404`, close stale modal state and refresh the list.
- On network or `5xx`, show retryable adult-facing feedback.

### Block Or Unblock Child

- The card action label depends on `ChildProfileResponse.active`.
- If `active=true`, show Block.
- If `active=false`, show Unblock.
- Pressing the action opens a confirmation modal.
- Confirming calls `PUT /api/v1/family/children/activation/{id}`.
- Backend toggles `active` and closes active sessions when a child becomes inactive.
- Frontend must not manually call session close/expel after this endpoint.
- After success, refresh children and sessions immediately.
- If the child was blocked, the card must show blocked state and no close-session action after refresh.

### Delete Child

- Delete is available from the edit modal or a clearly separated destructive card/menu action.
- Pressing Delete opens a destructive confirmation modal.
- Confirming calls `DELETE /api/v1/family/children/{id}`.
- Backend deletes the profile and closes active sessions when applicable.
- Frontend must not manually call session close/expel after deletion.
- After success, close any child modal, refresh children and sessions, and remove the card.
- If deletion fails with `404`, close stale modal state and refresh the list.

### Close Active Session

- The Close session action appears only when a matching active child session exists.
- Pressing Close session opens a confirmation modal.
- Confirming should use `DELETE /api/v1/sessions/children/{sessionId}/expel` so the active GameView can receive `CHILD_EXPELLED` through the contracted session event.
- After success, refresh active sessions immediately.
- If the session is already gone, clear stale session state and refresh.

## API Integration

Use the shared Axios client and request/response shapes derived from `docs/contracts/api/openapi.json`.

### Required Endpoints

- `GET /api/v1/family` to read family-level `ttsEnabled` and `agentEnabled` ceiling values when not already available in state.
- `GET /api/v1/family/children` to list child profiles.
- `GET /api/v1/family/children/{id}` if the implementation needs a profile refresh before editing.
- `PATCH /api/v1/family/children/{id}` with `UpdateChildProfileRequest`.
- `PUT /api/v1/family/children/activation/{id}` to block/unblock GameView access by toggling `active`.
- `DELETE /api/v1/family/children/{id}` to delete a child profile.
- `GET /api/v1/sessions/children?familyId={familyId}` to list active child sessions.
- `DELETE /api/v1/sessions/children/{id}/expel` to close an active child session from the panel with a child-facing session event.

### Contract Note

Backend currently exposes `PUT /api/v1/family/children/activation/{id}` in `ChildProfileController`.

Before implementing typed integration, `docs/contracts/api/openapi.json` must include this endpoint. If OpenAPI is not yet updated, frontend implementation should either wait for the contract update or isolate the call behind a small service method with a clear TODO referencing the contract drift.

Do not change backend contracts in this frontend feature unless explicitly requested.

### Update Child Request

`PATCH /api/v1/family/children/{id}` sends:

- `name`: edited child name.
- `birthday`: edited ISO date string.
- `avatar`: current or edited avatar identifier.
- `ttsEnabled`: requested child TTS value, constrained by family-level settings in UI and backend.
- `agentEnabled`: requested child agent value, constrained by family-level settings in UI and backend.

Backend applies family-level ceilings. Frontend should also communicate the ceiling to the user by disabling unavailable toggles.

## Session Polling

The Children section uses a 5-second polling interval as the baseline synchronization mechanism for active child sessions.

Polling behavior:

- Start polling when the Children section becomes active.
- Stop polling when the parent leaves the Children section.
- Stop polling when `/panel` unmounts or the parent logs out.
- Poll `GET /api/v1/sessions/children?familyId={familyId}` every 5 seconds.
- Skip a poll tick if a previous session request is still in flight.
- Do not start duplicate intervals when the parent reselects the Children section.
- On network error, show non-blocking adult feedback and continue with the next scheduled tick unless repeated errors require a retry state.
- Refresh sessions immediately after edit, activation toggle, deletion, or close-session actions.

Live time display:

- Compute session duration from `ChildSessionResponse.startedAt`.
- Update the visible duration at least every 5 seconds.
- If `startedAt` is missing or invalid, show a translated fallback instead of technical text.
- When a session disappears from the active list, remove the active session badge and hide the Close session action.

ParentChannel may later reduce the need for polling, but this feature treats 5-second polling as the required baseline.

## ParentChannel Usage

ParentChannel is optional in this feature.

If implemented, it must be limited to already contracted session events from `docs/contracts/api/websocket.json`:

- `SESSION_EXPIRED`.
- `SESSION_INVALIDATED`.
- `CHILD_EXPELLED`.
- `PARENT_BLOCK`.
- `HEARTBEAT_ACK` if surfaced technically, not visibly.

ParentChannel must not invent child profile update events. Profile list refresh still happens through REST.

## State Management

- Use existing session store for parent auth token and family session state.
- Keep raw token in memory only.
- Use a child/profile store or local section state according to existing frontend patterns.
- Stores call services; services call Axios.
- Keep edit modal draft state local to the modal.
- Do not persist child profile drafts, session polling state, or active session display state.
- Clear polling timers and in-flight flags on unmount/logout.

## Visual Requirements

- Use Nunito and global design tokens from `FEAT-001-Base-Styles`.
- Use adult panel background `#F4F6F9`.
- Use white card surfaces `#FFFFFF`.
- Use cobalt blue `#2B5BE0` for primary actions and active states.
- Use adult error red `#E53935` for destructive/error states.
- Use success green `#43A047` for active/available states where appropriate.
- Use neutral text `#1A2340` and secondary text `#6B7A99`.
- Child avatar color accents may be used on cards, but state must not rely on color alone.
- Keep layout aligned with the Parent Control Shell, not GameView.
- Avoid sustained uppercase visible labels.

Responsive behavior:

- Tablet landscape can use a multi-column card grid.
- Mobile landscape can use fewer columns or a compact list/card layout.
- The existing collapsed sidebar remains accessible.
- Portrait orientation continues to show the existing rotation overlay.

## Accessibility

- All icon buttons must have translated `aria-label` values.
- Card actions must be keyboard operable.
- Active/blocked/session states must include text or icon/shape, not color alone.
- Confirmation modals must use accessible dialog semantics.
- Focus moves into modals when opened and returns to the trigger after close.
- Destructive actions must be clearly labelled and confirmed.
- Adult touch targets must be at least `44px`.
- Text contrast must meet WCAG AA for adult UI.

## Out Of Scope

- Backend implementation changes.
- Backend contract changes unless explicitly requested.
- Child dashboard/progress details.
- Learning domain priority configuration.
- Detailed achievements or analytics.
- Chatbot/AdultAgent integration.
- GameView implementation.
- Opening GameView from the panel.
- Real-time child profile update events beyond contracted session events.
- Direct TTS, agent, or Coqui calls.
- Persisting parent token, child profile drafts, or session polling state.

## Acceptance Criteria

- The Children sidebar item renders the real Children section instead of a placeholder.
- `/panel` still requires an in-memory parent token.
- The section loads family settings, child profiles, and active child sessions.
- Child cards show avatar, name, active/blocked state, TTS state, agent state, and active session state.
- Active session duration is visible and updates every 5 seconds while the Children section is active.
- Polling stops when leaving the Children section or unmounting `/panel`.
- Polling skips overlapping requests.
- Edit opens a modal with name, birthday, optional avatar, TTS, and agent controls.
- Family-level disabled TTS/agent settings disable the corresponding child toggle and show explanatory text.
- Saving edit calls `PATCH /api/v1/family/children/{id}` and refreshes children and sessions on success.
- Blocking/unblocking calls `PUT /api/v1/family/children/activation/{id}` and refreshes children and sessions on success.
- Frontend does not manually close sessions after activation toggle because backend owns that side effect.
- Deleting calls `DELETE /api/v1/family/children/{id}` after destructive confirmation and refreshes children and sessions on success.
- Frontend does not manually close sessions after deleting because backend owns that side effect.
- Close session appears only for active sessions and calls `DELETE /api/v1/sessions/children/{sessionId}/expel`.
- `400`, `404`, network, and `5xx` responses show adult-facing translated feedback.
- All visible strings and aria labels use Vue i18n.
- Adult red is used for adult errors/destructive feedback; child retry orange is not used.
- Tablet landscape, mobile landscape, and portrait overlay behavior are verified.

## Testing Notes

Required tests:

- Children section replaces the placeholder when selected in the panel.
- Missing parent token still redirects `/panel` to Home.
- Initial load calls family, children, and active sessions endpoints.
- Cards render child profile state and active session state.
- Session duration display updates on the 5-second polling cadence.
- Polling starts only when Children section is active.
- Polling stops on section change and component unmount.
- Polling skips overlapping active-session requests.
- Edit modal validates required name and birthday.
- Edit submit payload matches `UpdateChildProfileRequest`.
- Family-level disabled TTS/agent disables corresponding child toggles.
- Activation toggle calls `PUT /api/v1/family/children/activation/{id}` and refreshes data.
- Delete confirmation calls `DELETE /api/v1/family/children/{id}` and refreshes data.
- Close session calls `DELETE /api/v1/sessions/children/{sessionId}/expel`.
- `400`, `404`, network, and `5xx` handling for edit/delete/toggle/close-session flows.
- All visible labels resolve through i18n keys.

Manual checks:

- Tablet landscape card grid.
- Mobile landscape card/list layout.
- Portrait rotation overlay.
- Keyboard operation for cards, actions, toggles, and modals.
- Focus return after edit/delete/toggle/close-session modals.
- Session duration remains readable and does not cause layout shift.

## Risks And Mitigations

- Risk: OpenAPI does not yet include the activation endpoint.
  Mitigation: block typed integration until contract update or isolate the call behind a clearly documented service method; do not silently invent models.
- Risk: Delete is confused with block/unblock.
  Mitigation: use separate labels, destructive styling, and confirmation copy; block uses activation toggle, delete removes the profile.
- Risk: Frontend duplicates backend side effects by closing sessions after profile update, activation toggle, or delete.
  Mitigation: rely on backend behavior for those endpoints and only refresh children/sessions after success.
- Risk: Session cards become stale.
  Mitigation: poll active sessions every 5 seconds only while Children section is active and refresh immediately after mutations.
- Risk: Polling creates avoidable load or duplicate requests.
  Mitigation: stop polling outside the section, skip overlapping requests, and clear intervals on unmount/logout.
- Risk: Child TTS/agent toggles conflict with family-level settings.
  Mitigation: disable unavailable child toggles and explain the family-level ceiling in the modal.
- Risk: ParentChannel scope creep.
  Mitigation: use only contracted session events and keep profile refresh REST-based.
- Risk: Adult panel drifts into GameView styling.
  Mitigation: use adult panel background, white surfaces, adult red errors, and compact adult layout.

## Dependencies

- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - adult panel visual tokens and interaction baseline.
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - Parent Control Shell and Children placeholder section.
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - child avatar/card patterns and child profile field semantics.
- `docs/contracts/api/openapi.json` - family, child profile, and child session endpoints.
- `docs/contracts/api/websocket.json` - optional ParentChannel session events.
- `docs/design/frontend_design_v1.docx` - Parent Control children section behavior and session management direction.
- `docs/design/design_decisions_v1.docx` - adult visual register, accessibility, and panel decisions.

## Agent Instruction

- Implement only the frontend Children section inside `/panel`.
- Do not implement backend changes.
- Do not change backend contracts unless explicitly requested.
- Do not implement child dashboard/progress analytics.
- Do not open GameView from the panel.
- Do not add direct TTS, agent, or Coqui calls.
- Keep parent token in memory only.
- Use shared Axios services for API calls.
- Poll active child sessions every 5 seconds only while the Children section is active.
- Do not manually close sessions after child edit, activation toggle, or delete; backend owns those side effects.
- Use Vue Router, Pinia, Vue i18n, and TypeScript according to the frontend stack.
- Keep all visible strings and aria labels in i18n.
- Commit: `feat(frontend): add parent children section`
