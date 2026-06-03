# Sprint 014 - frontend
# -----------------------------------------------

## Goal
Implement the Parent Control Children Section from `docs/product/features/frontend/FEAT-008-Child-Section.md`: replace the `/panel` Children placeholder with real adult-facing child profile management, active session display, 5-second active-session polling, edit/block/unblock/delete/close-session actions, i18n, accessibility, and responsive panel layout. Backend implementation is out of scope.

## Status
status: archived
started_at: 2026-06-01 00:00:00
closed_at: 2026-06-03 00:00:00
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-008-Child-Section.md` before editing.
- [ ] Review `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` for panel shell constraints and Children placeholder behavior.
- [ ] Review `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` for child card/avatar/edit patterns.
- [ ] Review `docs/contracts/api/openapi.json` for family, child profile, and child session schemas/endpoints.
- [ ] Review `docs/contracts/api/websocket.json` for optional ParentChannel session events only.
- [ ] Review existing panel component structure before replacing the Children placeholder.
- [ ] Review existing service/store patterns before adding child profile/session services.
- [ ] Do not implement backend code in this sprint.

### Contract And Endpoint Alignment
- [ ] Use OpenAPI-derived types where the endpoint exists in `docs/contracts/api/openapi.json`.
- [ ] Confirm `GET /api/v1/family`, `GET /api/v1/family/children`, `PATCH /api/v1/family/children/{id}`, `DELETE /api/v1/family/children/{id}`, `GET /api/v1/sessions/children?familyId={familyId}`, and `DELETE /api/v1/sessions/children/{id}/expel` are available in frontend API typing or service layer.
- [ ] Account for backend endpoint `PUT /api/v1/family/children/activation/{id}` for block/unblock.
- [ ] If OpenAPI does not yet include `PUT /api/v1/family/children/activation/{id}`, isolate the call in a clearly named service method and document the contract drift in review.
- [ ] Do not change backend contracts unless explicitly requested.
- [ ] Do not invent local request/response models that diverge from existing schemas.

### Children Section Integration
- [ ] Replace the Children placeholder in `/panel` with the real Children section.
- [ ] Keep `/panel` protected by the existing in-memory parent token guard.
- [ ] Load family settings needed for child TTS/agent ceiling values.
- [ ] Load child profiles from `GET /api/v1/family/children`.
- [ ] Load active child sessions from `GET /api/v1/sessions/children?familyId={familyId}`.
- [ ] Show adult-facing loading, empty, and retryable error states.
- [ ] Do not open GameView from the panel.

### Child Cards
- [ ] Render child cards with avatar, name, active/blocked state, TTS state, agent state, and active session state.
- [ ] Use `ChildProfileResponse.active` to show active vs blocked state.
- [ ] Match active `ChildSessionResponse` entries by `childProfileId` and `status: ACTIVE`.
- [ ] Show live session duration from `ChildSessionResponse.startedAt` when active.
- [ ] Show Edit action for each child.
- [ ] Show Block or Unblock action according to `active`.
- [ ] Show Close session action only when the child has an active session.
- [ ] Ensure active/blocked/session states do not rely on color alone.

### Edit Child Modal
- [ ] Add adult-facing edit modal for child profile updates.
- [ ] Include required name field.
- [ ] Include required birthday field using contract-compatible date format.
- [ ] Include avatar editing only if the existing avatar selector pattern can be reused without scope creep.
- [ ] Include `ttsEnabled` toggle.
- [ ] Include `agentEnabled` toggle.
- [ ] Disable child TTS toggle when family-level TTS is disabled and show translated explanatory text.
- [ ] Disable child agent toggle when family-level agent is disabled and show translated explanatory text.
- [ ] Validate required name and birthday before submit.
- [ ] Submit `PATCH /api/v1/family/children/{id}` with `UpdateChildProfileRequest` shape.
- [ ] On success, close modal and refresh children and sessions.
- [ ] On `400`, show inline adult validation feedback.
- [ ] On `404`, close stale modal state and refresh list.
- [ ] On network or `5xx`, show retryable adult-facing feedback.

### Block And Unblock
- [ ] Add confirmation modal for Block/Unblock action.
- [ ] Call `PUT /api/v1/family/children/activation/{id}` on confirmation.
- [ ] Refresh children and sessions after success.
- [ ] Do not manually call session close/expel after activation toggle; backend owns that side effect.
- [ ] After blocking, ensure the card shows blocked state and no stale Close session action remains after refresh.

### Delete Child
- [ ] Add destructive confirmation modal for Delete.
- [ ] Keep delete visually and semantically distinct from Block/Unblock.
- [ ] Call `DELETE /api/v1/family/children/{id}` on confirmation.
- [ ] Refresh children and sessions after success.
- [ ] Remove the child card after successful refresh.
- [ ] Do not manually call session close/expel after delete; backend owns that side effect.
- [ ] On `404`, close stale modal state and refresh list.

### Close Active Session
- [ ] Show Close session only for children with active sessions.
- [ ] Add confirmation modal for Close session.
- [ ] Call `DELETE /api/v1/sessions/children/{sessionId}/expel` on confirmation.
- [ ] Refresh active sessions immediately after success.
- [ ] If session is already gone, clear stale local session state and refresh.
- [ ] Do not show technical session errors to the adult without translated context.

### Session Polling
- [ ] Start active-session polling when the Children section becomes active.
- [ ] Poll `GET /api/v1/sessions/children?familyId={familyId}` every 5 seconds.
- [ ] Stop polling when parent leaves the Children section.
- [ ] Stop polling when `/panel` unmounts or parent logs out.
- [ ] Skip a poll tick if a previous active-session request is still in flight.
- [ ] Prevent duplicate intervals if the parent reselects Children.
- [ ] Refresh sessions immediately after edit, activation toggle, delete, and close-session actions.
- [ ] Compute visible session duration from `startedAt` and update display on the 5-second cadence.
- [ ] Show translated fallback if `startedAt` is missing or invalid.

### Optional ParentChannel Use
- [ ] ParentChannel integration is optional.
- [ ] If used, subscribe only to already contracted session events from `docs/contracts/api/websocket.json`.
- [ ] Do not invent child profile update events.
- [ ] Keep profile refresh REST-based.
- [ ] Do not add WebSocket dependency if polling satisfies this sprint.

### Visual, Responsive, And Accessibility
- [ ] Use adult panel background `#F4F6F9` and white card surfaces.
- [ ] Use cobalt `#2B5BE0` for primary actions and active states.
- [ ] Use adult error red `#E53935` for destructive/error states.
- [ ] Do not use child GameView retry orange in adult panel flows.
- [ ] Keep layout aligned with Parent Control Shell, not GameView.
- [ ] Add all visible strings and aria labels to `src/i18n/es.ts` or existing i18n module.
- [ ] Avoid sustained uppercase visible labels.
- [ ] Ensure all icon buttons have translated accessible labels.
- [ ] Ensure card actions are keyboard operable.
- [ ] Ensure modals manage focus and return focus to triggers.
- [ ] Ensure active/blocked/session states are not color-only.
- [ ] Keep adult touch targets at least 44px.
- [ ] Verify tablet landscape, mobile landscape, and portrait rotation overlay behavior.

### Testing And Verification
- [ ] Add or update service/component/routing tests if the project has a test harness available for this area.
- [ ] Verify Children section replaces the placeholder when selected in the panel.
- [ ] Verify missing parent token still redirects `/panel` to Home.
- [ ] Verify initial load calls family, children, and active-session endpoints.
- [ ] Verify cards render child profile state and active session state.
- [ ] Verify session duration display updates on the 5-second polling cadence.
- [ ] Verify polling starts only while Children section is active.
- [ ] Verify polling stops on section change and component unmount.
- [ ] Verify polling skips overlapping active-session requests.
- [ ] Verify edit modal validation and submit payload.
- [ ] Verify family-level disabled TTS/agent disables corresponding child toggles.
- [ ] Verify activation toggle calls `PUT /api/v1/family/children/activation/{id}` and refreshes data.
- [ ] Verify delete confirmation calls `DELETE /api/v1/family/children/{id}` and refreshes data.
- [ ] Verify close session calls `DELETE /api/v1/sessions/children/{sessionId}/expel`.
- [ ] Verify error handling for `400`, `404`, network, and `5xx` flows.
- [ ] Verify all visible labels resolve through i18n keys.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Activation endpoint contract drift**: backend exposes `PUT /api/v1/family/children/activation/{id}` but OpenAPI may not include it yet.
  Mitigation: isolate the service call and document drift in review; do not silently invent broad local models.
- **Delete vs block confusion**: delete is destructive while block/unblock is reversible.
  Mitigation: separate labels, styling, confirmation copy, and actions clearly.
- **Duplicated backend side effects**: frontend may close sessions after edit, activation toggle, or delete even though backend owns those side effects.
  Mitigation: only refresh children/sessions after those profile mutations.
- **Stale session state**: active session cards may become outdated.
  Mitigation: poll active sessions every 5 seconds while Children is active and refresh immediately after mutations.
- **Polling load or leaks**: timer may run outside the section or overlap requests.
  Mitigation: stop timer on section change/unmount/logout and skip overlapping requests.
- **Family ceiling mismatch**: child TTS/agent toggles may appear enabled when family-level settings disable them.
  Mitigation: load family settings, disable unavailable toggles, and show explanatory text.
- **Adult UI drift**: panel may use GameView colors/feedback.
  Mitigation: keep adult panel visual language and adult error semantics.

## Dependencies
- `docs/product/features/frontend/FEAT-008-Child-Section.md` - source feature.
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - Parent Control Shell and Children placeholder.
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - child card/avatar/profile patterns.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - visual and accessibility tokens.
- `docs/contracts/api/openapi.json` - family, child profile, and child session endpoints.
- `docs/contracts/api/websocket.json` - optional ParentChannel session events.
- `docs/design/frontend_design_v1.docx` - panel/children section behavior.
- `docs/design/design_decisions_v1.docx` - adult UI and accessibility decisions.

## Agent Instruction
- Implement only `FEAT-008-Child-Section` frontend work.
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

## Notes
Derived from `docs/product/features/frontend/FEAT-008-Child-Section.md`.

Design output:
- View/feature: adult-facing Children section inside Parent Control Shell.
- Data flow: family settings + child profiles + active child sessions; session polling every 5 seconds while section is active.
- Component tree: `/panel` -> `PanelControlView` Children section -> child cards + edit/confirm modals + polling/session state.
- Contract dependency: OpenAPI family/child/session endpoints; activation endpoint may require OpenAPI update.
- Risks: activation contract drift, destructive delete confusion, stale sessions, polling leaks, family TTS/agent ceiling mismatch.

## Review

completed_tasks:

incomplete_tasks:
- Not reviewed in this sprint file before archival.

contract_changes:

learnings:

next_sprint_suggestions:
- Implement `FEAT-009-Settings-Section` as the next frontend sprint.
