# Sprint 013 - frontend
# -----------------------------------------------

## Goal
Implement the frontend-only Game View Shell from `docs/product/features/frontend/FEAT-007-Game-View-Shell.md`: child avatar selection opens a child session, stores it in memory only, navigates to protected-by-memory `/game/:childId`, renders the child-facing loader and empty World Map shell, starts the real frontend Game WebSocket lifecycle against `/ws/game`, sends heartbeat while connected, handles contracted session events, and cleans up on exit. Backend implementation changes are out of scope for this sprint.

## Status
status: active
started_at: 2026-06-01 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` before editing.
- [ ] Review `docs/contracts/api/openapi.json` for `OpenChildSessionRequest`, `ChildSessionResponse`, `ApiResponseChildSession`, and child session endpoints.
- [ ] Review `docs/contracts/api/websocket.json` for `/ws/game` and `SessionEvent` payloads.
- [ ] Review existing Home child selector flow before adding GameView navigation.
- [ ] Review `src/router/index.ts` and existing route guards before adding `/game/:childId`.
- [ ] Review existing session store before adding in-memory child session state.
- [ ] Review existing WebSocket utilities or stores before adding Game WebSocket lifecycle.
- [ ] Do not change backend code or backend contracts in this sprint.

### Child Session Opening From Home
- [ ] When a child avatar is selected from Home, call `POST /api/v1/sessions/children` through the shared Axios client.
- [ ] Send `childProfileId` from the selected child.
- [ ] Omit `heartbeatIntervalSeconds` or use the documented default only if the existing service pattern supports it.
- [ ] Derive request and response types from OpenAPI-generated/shared frontend types where available.
- [ ] On `201`, store `ChildSessionResponse` in memory only.
- [ ] On successful session creation, navigate with router replace to `/game/:childId`.
- [ ] Do not require parental PIN for this child GameView entry flow.
- [ ] Do not persist child session state in `localStorage`, `sessionStorage`, route state, or persisted Pinia slices.
- [ ] Do not put raw tokens or child session data in URL query parameters.

### In-Memory Child Session Guard
- [ ] Add route `/game/:childId` with name `game`.
- [ ] Protect `/game/:childId` with an in-memory child session guard, not the parental auth guard.
- [ ] Allow route only when an active in-memory child session exists for the route `childId`.
- [ ] Redirect direct URL access to Home when no matching child session exists.
- [ ] Redirect refresh-on-game to Home because in-memory child session state is gone.
- [ ] Redirect route child id mismatch to Home and clear invalid transient child session state if needed.
- [ ] Preserve existing unknown-route redirect policy.

### GameView Shell
- [ ] Create `GameView.vue` or equivalent route component.
- [ ] Render a full-viewport fixed GameView shell with no document scrollbars.
- [ ] Use child visual register: sky `#D6ECFF`, grass `#C8E6A0`, cobalt `#2B5BE0`, child retry orange `#FFB347`.
- [ ] Do not use adult panel background `#F4F6F9` as the GameView background.
- [ ] Do not use adult error red `#E53935` in GameView.
- [ ] Include child-facing loader using `assets/animations/base-idle.png` as the temporary avatar idle placeholder.
- [ ] Add a soft circular loading affordance around or near the avatar.
- [ ] Mock only the local welcome state; do not call TTS or Coqui.
- [ ] Render an empty World Map placeholder after loader/preparation state.
- [ ] Keep the map as a walk/promenade shell, not numbered levels or visible locked path.
- [ ] Do not add a child-facing exit button.

### Game WebSocket Frontend Lifecycle
- [ ] Open a real browser WebSocket targeting `/ws/game` after child session validation.
- [ ] Implement open, message, error, and close handlers.
- [ ] Send `{ "type": "heartbeat" }` while the WebSocket is open.
- [ ] Use the child session heartbeat interval when available, or a conservative local interval aligned with backend defaults.
- [ ] Parse incoming `SessionEvent` messages.
- [ ] Handle `HEARTBEAT_ACK` by updating technical connection state only.
- [ ] Handle `GAME_STATE_UPDATE` as placeholder state without rendering game engines.
- [ ] Handle `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, and `PARENT_BLOCK` by closing WebSocket, clearing child session state, and navigating to Home.
- [ ] Hide WebSocket handshake errors and technical connection failures from the child-facing UI.
- [ ] Do not put tokens in the WebSocket URL.
- [ ] Do not implement backend handshake changes in frontend beyond the current browser WebSocket lifecycle.

### Cleanup And Session Exit
- [ ] Close the WebSocket when GameView unmounts.
- [ ] Clear heartbeat timers on unmount and terminal events.
- [ ] Remove event listeners on unmount.
- [ ] Clear transient GameView local state on unmount.
- [ ] Clear child session state after terminal session events.
- [ ] Decide from existing app flow whether intentional navigation away should call `DELETE /api/v1/sessions/children/{id}` or only rely on heartbeat expiry; document the chosen behavior in review.
- [ ] Do not leave background WebSocket connections open after navigating away.

### i18n And Accessibility
- [ ] Add all visible GameView strings and aria labels to `src/i18n/es.ts` or the existing i18n module.
- [ ] Keep visible GameView copy sparse and child-friendly.
- [ ] Avoid sustained uppercase visible labels.
- [ ] Ensure any audio-related future state has equivalent visual state.
- [ ] Do not rely on color alone for visible state.
- [ ] Keep GameView useful without audio.
- [ ] Respect the existing portrait rotation overlay behavior.
- [ ] Keep touch targets in GameView at least `64px` where interactive controls exist.
- [ ] Keep interactive text at least `20px` where interactive text exists.

### Backend Handshake Mitigation Awareness
- [ ] Treat the current backend `/ws/game` handshake limitation as a known external backend issue, not a frontend blocker.
- [ ] Frontend must still implement the WebSocket lifecycle and cleanup.
- [ ] If backend rejects the socket due missing `Authorization` header or missing `childSessionId` binding, keep the empty World Map shell usable and hide technical details from the child.
- [ ] Do not implement backend code.
- [ ] Do not change `docs/contracts/api/websocket.json` in this sprint.
- [ ] Do not add token query parameter workarounds.

### Testing And Verification
- [ ] Add or update routing/component/service tests if the project has a test harness available for this area.
- [ ] Verify child avatar selection calls `POST /api/v1/sessions/children` with the selected child profile id.
- [ ] Verify successful session creation stores only in-memory child session data.
- [ ] Verify successful session creation navigates to `/game/:childId` using router replace.
- [ ] Verify `/game/:childId` without matching child session redirects to Home.
- [ ] Verify `/game/:childId` with matching active child session renders GameView.
- [ ] Verify refresh on `/game/:childId` redirects to Home.
- [ ] Verify GameView creates a WebSocket for `/ws/game` after validation.
- [ ] Verify GameView sends heartbeat while connected.
- [ ] Verify terminal session events close socket, clear child session state, and navigate Home.
- [ ] Verify GameView closes WebSocket and clears timers on unmount.
- [ ] Verify no Coqui/TTS endpoint is called.
- [ ] Verify no raw token appears in route params, query string, local storage, or session storage.
- [ ] Verify GameView has no child-facing exit button.
- [ ] Verify desktop/tablet landscape, mobile landscape, and portrait overlay behavior manually.
- [ ] Verify GameView does not create document scrollbars.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Direct URL bypass**: `/game/:childId` may become reachable without selecting a child avatar.
  Mitigation: guard route with in-memory active child session state and never restore from URL alone.
- **Backend WebSocket handshake mismatch**: current backend may reject browser WebSocket connections because custom `Authorization` headers are not available and `childSessionId` is not bound.
  Mitigation: implement frontend lifecycle and safe rejection handling; backend team has already been notified and backend changes remain out of scope.
- **Token workaround leakage**: adding tokens to WebSocket URLs would expose sensitive data.
  Mitigation: never place tokens in URL/query params and do not invent child tokens.
- **TTS scope creep**: welcome audio may trigger direct frontend Coqui calls.
  Mitigation: mock local welcome state only; do not call TTS or `openapi_tts.json`.
- **Game domain creep**: shell may start implementing map/game logic.
  Mitigation: keep World Map empty and handle only lifecycle/session events.
- **Adult UI drift**: GameView may use adult panel styles or red errors.
  Mitigation: use child register colors and hide technical failures from the child.
- **Resource leaks**: WebSocket and heartbeat timers may continue after navigation.
  Mitigation: centralize cleanup on unmount and terminal events.

## Dependencies
- `docs/product/features/frontend/FEAT-007-Game-View-Shell.md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - child visual tokens and UI baseline.
- `docs/product/features/frontend/FEAT-002-Home-View.md` - Home child selection and navigation flow.
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - child selector/avatar card behavior.
- `docs/contracts/api/openapi.json` - `OpenChildSessionRequest`, `ChildSessionResponse`, and child session endpoints.
- `docs/contracts/api/websocket.json` - `/ws/game` and `SessionEvent` contract.
- `docs/design/frontend_design_v1.docx` - GameView route, lifecycle, WebSocket, and viewport direction.
- `docs/design/design_decisions_v1.docx` - child visual register, loader, audio, animation, and interaction rules.

## Agent Instruction
- Implement only frontend work for `FEAT-007-Game-View-Shell`.
- Do not implement backend changes.
- Do not change backend contracts.
- Do not implement real minigames, procedural map content, TTS, binary audio streaming, or advanced reconnection phases.
- Do not add direct Coqui or TTS API calls.
- Do not persist raw tokens or child session state.
- Do not put tokens in WebSocket URLs.
- Use the shared Axios client for `POST /api/v1/sessions/children`.
- Use Vue Router, Pinia, Vue i18n, and TypeScript according to the frontend stack.
- Keep GameView full viewport, child-facing, and free of adult technical error UI.
- Commit: `feat(frontend): add game view shell`

## Notes
Derived from `docs/product/features/frontend/FEAT-007-Game-View-Shell.md`.

Design output:
- View/feature: child-facing GameView Shell.
- Data flow: Home child avatar selection opens `ChildSessionResponse`; GameView validates in-memory child session; WebSocket lifecycle starts after route validation.
- Component tree: Home child selector -> child session service -> router `/game/:childId` -> `GameView` -> loader + empty World Map placeholder + Game WebSocket lifecycle.
- Contract dependency: OpenAPI child session endpoints and WebSocket `SessionEvent` only.
- Backend note: backend team has been warned about `/ws/game` browser handshake and `childSessionId` binding; frontend sprint should not implement backend fixes.
- Risks: direct URL bypass, backend handshake rejection, token leakage, TTS scope creep, GameView resource leaks.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
