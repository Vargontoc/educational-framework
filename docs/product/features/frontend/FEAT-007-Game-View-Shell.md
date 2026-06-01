# FEAT-007 - Frontend: Game View Shell

## Status

state: proposal
user_history: Game view for children
depends_on: FEAT-001-Base-Styles, FEAT-002-Home-View, FEAT-004-Modal-Creation-Child
owned_by: frontend
scope: frontend route + child session opening + in-memory access guard + GameView shell + real Game WebSocket lifecycle. No backend implementation is included in this feature.
test: child avatar selection opens a child session, navigates to GameView, starts the Game WebSocket lifecycle, sends heartbeat, handles session events, and closes the connection on exit.

## Description

This feature defines the first GameView shell for the child experience.

The GameView is entered only by selecting a child avatar from `HomeView`. It is not protected by the parental PIN and does not require a parental token, but it must not be reachable by typing `/game/:childId` directly in the browser. Direct URL access is blocked by requiring an active `ChildSessionResponse` in memory for the selected child.

When a child avatar is selected, the frontend opens a child session through `POST /api/v1/sessions/children`, stores the returned child session in memory only, and navigates to `/game/:childId`. GameView then renders a child-facing loader with the avatar idle placeholder, opens the real Game WebSocket lifecycle, sends heartbeat messages, and renders an empty World Map placeholder for this first shell.

This feature intentionally does not implement real game engines, procedural map content, TTS synthesis, binary audio streaming, game actions beyond lifecycle placeholders, or backend changes.

## Product Rules

- GameView is child-facing and uses the child visual register.
- Entry to GameView is only valid from child avatar selection in Home.
- Direct URL access to `/game/:childId` without an in-memory active child session redirects to Home.
- No parental PIN is required to enter GameView after a child avatar is selected.
- No raw token is persisted in `localStorage`, `sessionStorage`, route state, URL query parameters, or persisted Pinia slices.
- The child session is stored in memory only.
- GameView must not show a child-facing exit button.
- GameView must not show technical errors to the child.
- GameView must not use adult red `#E53935` for child feedback; use child retry orange `#FFB347` where a retry state is needed.
- All visible copy and aria labels must use Vue i18n.
- Sustained uppercase labels must be avoided.

## UX Flow

### Valid Entry From Home

- Home loads the family and child list according to existing HomeView flow.
- The user opens the child selector.
- The user selects a child avatar.
- Frontend calls `POST /api/v1/sessions/children` with the selected child profile id.
- On `201`, frontend stores the returned `ChildSessionResponse` in memory only.
- Frontend navigates with router replace to `/game/:childId`.
- GameView validates that the in-memory child session exists, is `ACTIVE`, and belongs to the route `childId`.
- GameView shows the child loader while preparing the shell and WebSocket lifecycle.
- GameView renders the empty World Map placeholder.

### Direct URL Access

- User opens `/game/:childId` directly or refreshes while on GameView.
- In-memory child session state is absent.
- Router guard or view-level guard redirects to Home.
- No child session is opened implicitly from a direct URL.
- No backend call is made to infer or restore a child session.

### Existing Active Child Session

- If selecting a child opens a new session while an existing session for that child is active, backend currently closes the previous session and emits `SESSION_EXPIRED` with reason `new_session_opened`.
- Frontend must handle the event by closing the old GameView connection and returning that old client to Home if it is still open.
- The new valid selection flow proceeds with the new child session response.

## REST Integration

Use the shared Axios client and types derived from `docs/contracts/api/openapi.json`.

### Open Child Session

- Endpoint: `POST /api/v1/sessions/children`.
- Request schema: `OpenChildSessionRequest`.
- Response schema: `ApiResponseChildSession` with `ChildSessionResponse` in `data`.
- Required payload:

```json
{
  "childProfileId": 123,
  "heartbeatIntervalSeconds": 30
}
```

- `heartbeatIntervalSeconds` may be omitted if the frontend wants backend default behavior.
- Store `ChildSessionResponse.id`, `childProfileId`, `familyId`, `status`, `startedAt`, and `lastActivityAt` in memory only.
- The child session response does not include a child token. Frontend must not invent one.

### Close Child Session

- Endpoint: `DELETE /api/v1/sessions/children/{id}`.
- Use on intentional GameView teardown if the current flow requires the session to close when leaving GameView.
- Do not call this endpoint when handling backend terminal events if the session is already expired, expelled, blocked, or invalidated.
- If this behavior is not yet desired by product, clean local state and close WebSocket only; leave backend session expiry to heartbeat timeout.

### Heartbeat REST Fallback

- Endpoint: `POST /api/v1/sessions/children/{id}/heartbeat` exists in backend and OpenAPI.
- This feature should prefer WebSocket heartbeat for GameView.
- REST heartbeat can be reserved as future fallback if WebSocket connection is unavailable and product accepts degraded play.

## Routing

- Add route `/game/:childId` with name `game`.
- The route is not protected by parental PIN.
- The route is protected by an in-memory child session guard.
- If no active in-memory child session exists for `route.params.childId`, redirect to Home.
- Unknown routes continue to redirect according to the existing router policy.
- Use `router.replace` for Home to GameView navigation to avoid child back-navigation history.

## State Management

- Use the existing session store if available, or extend it minimally for child session state.
- Store child session state in memory only.
- Required state:
  - selected child profile id.
  - active child session id.
  - child session family id.
  - child session status.
  - heartbeat interval seconds when known.
- Do not persist child session state.
- Do not store child session state in route params beyond the visible `childId` required by the route.
- Do not add a game domain store for this shell; game domain state remains backend-owned.

## Game WebSocket Lifecycle

Use the backend Game WebSocket endpoint currently registered at `/ws/game`.

### Connect

- GameView opens the WebSocket after validating the active in-memory child session.
- The connection is part of the real frontend lifecycle in this feature.
- Connection state should be local or in the existing WebSocket/session store if one exists.
- The UI should remain child-friendly if the connection closes or is rejected.

### Outgoing Messages

Send heartbeat messages while the WebSocket is open:

```json
{
  "type": "heartbeat"
}
```

Prepare but do not fully implement game action messages:

```json
{
  "type": "game_action",
  "payload": {}
}
```

No real minigame action is required in this feature.

### Incoming Events

Handle contracted `SessionEvent` payloads from `docs/contracts/api/websocket.json` and backend implementation:

- `HEARTBEAT_ACK`: update technical connection/heartbeat state only; no visible child message is required.
- `GAME_STATE_UPDATE`: accept and store minimal placeholder state if needed, but do not render engines or real map content yet.
- `SESSION_EXPIRED`: close WebSocket, clear child session state, and navigate to Home with child-friendly non-technical feedback if any feedback is shown.
- `SESSION_INVALIDATED`: close WebSocket, clear child session state, and navigate to Home.
- `CHILD_EXPELLED`: close WebSocket, clear child session state, and navigate to Home after a friendly avatar transition if available.
- `PARENT_BLOCK`: close WebSocket, clear child session state, and navigate to Home using the same child-safe behavior as expelled/invalidated.

### Cleanup

- On GameView unmount, close the WebSocket.
- Clear heartbeat timers.
- Remove event listeners.
- Clear transient GameView local state.
- Clear child session state only when leaving the game flow intentionally or after terminal session events.
- Do not leave background WebSocket connections open after navigating away.

## Backend Handshake Mitigation

Frontend will implement the real Game WebSocket lifecycle in this feature, but the current backend handshake has a known incompatibility with browser clients and child-session binding.

Current backend behavior reviewed:

- `WebSocketConfig` registers native WebSocket endpoint `/ws/game`.
- `/ws/game` uses `WebSocketAuthInterceptor`.
- `WebSocketAuthInterceptor` requires an `Authorization: Bearer <token>` HTTP header.
- Browser `new WebSocket(url)` cannot send custom `Authorization` headers.
- `GameWebSocketHandler` requires a WebSocket session attribute named `childSessionId`.
- The current interceptor sets `familyId` and `familySessionId`, but does not set `childSessionId`.
- If `childSessionId` is absent, `GameWebSocketHandler.afterConnectionEstablished` closes the connection with `POLICY_VIOLATION`.

Backend team action required:

- Provide a browser-compatible handshake that associates the already-opened child session id with the WebSocket session.
- Do not require frontend to place raw tokens in URL query parameters.
- Recommended approach: accept an initial WebSocket auth/bind message before normal heartbeat/game messages:

```json
{
  "type": "auth",
  "childSessionId": 123
}
```

- Backend should validate that the child session exists and is `ACTIVE`, then bind `childSessionId` to the WebSocket session attributes.
- After binding, backend should accept `heartbeat` and future `game_action` messages.
- If backend chooses another handshake, update `docs/contracts/api/websocket.json` before frontend implementation starts.

Frontend mitigation until backend update:

- Implement connection creation, open/error/close handlers, heartbeat scheduling, event parsing, and cleanup.
- Treat handshake rejection as a recoverable technical state hidden from the child.
- Do not block rendering the empty World Map placeholder solely because the WebSocket is rejected during this shell feature, unless product later requires hard blocking.
- Log or expose developer diagnostics only in development tooling, not in child-facing UI.

## Loader

- GameView loader uses the child register, not the adult spinner.
- Use `assets/animations/base-idle.png` as the temporary avatar idle placeholder until Lottie assets are available.
- Show a soft circular loading affordance around or near the avatar.
- The loader may show a short welcoming visual state.
- Welcome audio is mocked locally for this feature; do not call Coqui or `openapi_tts.json` directly from frontend.
- The intended future welcome phrase is: `Hola {childName}. Vamos a jugar.`
- The visual loader must remain usable without audio.
- Do not add artificial delays beyond real preparation needs.

## World Map Placeholder

- Render an empty World Map shell after loader/preparation.
- Use v1 scenario baseline: soft day sky and simplified grass ground.
- The map is a walk/promenade, not a visible level track.
- Do not render numbered tiles, visible locks, scores, timers, or game-over states.
- Do not implement procedural path, available activity object, engines, or map scrolling in this feature.
- Keep the structure ready for future world layers:
  - background sky layer.
  - distant world-life layer.
  - ground layer.
  - avatar/guide layer.
  - interactive object layer.
  - feedback overlay layer.

## Viewport And Layout

- GameView owns the full device viewport.
- Use dynamic viewport units where appropriate, such as `100dvh`, with safe fallbacks.
- Avoid browser header issues on mobile by using fixed viewport layout and CSS safe-area handling.
- No document-level scrollbars should appear while GameView is mounted.
- All GameView content is rendered inside the fixed viewport.
- Portrait orientation follows the existing app rotation overlay behavior.
- Mobile landscape and tablet landscape are first-class targets.
- Touch targets in GameView are at least `64px`; primary game actions in future features should be `80-96px`.
- Interactive text in GameView must not be smaller than `20px`.

## Visual Requirements

- Use Nunito and global design tokens from `FEAT-001-Base-Styles`.
- Use child register visuals:
  - sky background `#D6ECFF`.
  - grass/ground `#C8E6A0`.
  - cobalt primary `#2B5BE0`.
  - celebration accent `#F5A623`.
  - child retry/error feedback `#FFB347`.
- Do not use adult panel background `#F4F6F9` as the main GameView background.
- Do not use adult error red `#E53935` in GameView.
- Keep text sparse and supportive; GameView is not a dense reading interface.
- World-life animations must be slow, peripheral, and non-blocking when added later.
- All animations must be interruptible.

## Accessibility

- The experience must be usable without audio.
- Any audio information must have an equivalent visual state.
- Avoid technical child-facing messages.
- Do not rely on color alone for state.
- Keep focus behavior safe when navigating into and out of GameView.
- Avoid sustained uppercase labels.
- Respect reduced motion where global app support exists.
- Screen reader support for the child experience is not a full v1 target, but route landmarks and translated labels should remain coherent for adults supervising the session.

## Out Of Scope

- Backend implementation changes.
- Updating `docs/contracts/api/openapi.json` or `docs/contracts/api/websocket.json`.
- Direct frontend calls to Coqui TTS or `openapi_tts.json`.
- Real TTS synthesis.
- Binary audio streaming and audioId correlation.
- Audio worker implementation.
- Lottie integration if no Lottie avatar assets are available yet.
- Procedural World Map content.
- Horizontal map scroll.
- Game engines.
- Scoring, visible progress pressure, timers, game over, or numbered levels.
- Parent controls inside GameView.
- Child exit button.
- Advanced reconnection phases.
- Full game action processing.
- Persisting child session state.

## Acceptance Criteria

- Selecting a child avatar from Home calls `POST /api/v1/sessions/children` with `childProfileId`.
- On successful child session creation, the frontend stores `ChildSessionResponse` in memory only.
- On successful child session creation, the frontend navigates to `/game/:childId` using router replace.
- `/game/:childId` renders only when an active in-memory child session exists for that child id.
- Direct URL access to `/game/:childId` without in-memory child session redirects to Home.
- Refreshing while on `/game/:childId` redirects to Home because in-memory child session state is gone.
- GameView renders the child loader with `assets/animations/base-idle.png` placeholder.
- GameView renders an empty World Map placeholder after the loader/preparation state.
- GameView opens a WebSocket lifecycle targeting `/ws/game` after child session validation.
- GameView sends WebSocket heartbeat messages while the socket is open.
- GameView parses `SessionEvent` messages from the WebSocket.
- `HEARTBEAT_ACK` is handled without visible child-facing technical copy.
- Terminal session events close the WebSocket, clear child session state, and navigate to Home.
- WebSocket close/error handlers do not expose technical messages to the child.
- GameView closes WebSocket and clears timers/listeners on unmount.
- No direct frontend request is made to Coqui TTS.
- No raw token is persisted or placed in URL query parameters.
- GameView has no child-facing exit button.
- GameView viewport is fixed to the visible device viewport and does not create scrollbars.
- Portrait orientation behavior remains consistent with the existing rotation overlay.
- All visible copy and aria labels use Vue i18n.

## Testing Notes

Required tests:

- Child avatar selection calls `POST /api/v1/sessions/children` with the selected child profile id.
- Successful session creation stores only in-memory child session data.
- Successful session creation navigates to `/game/:childId`.
- `/game/:childId` without child session redirects to Home.
- `/game/:childId` with matching active child session renders GameView.
- Route child id mismatch redirects to Home or clears invalid state.
- GameView creates a WebSocket for `/ws/game` after validation.
- GameView sends `{ "type": "heartbeat" }` while connected.
- `HEARTBEAT_ACK` updates technical state without visible technical copy.
- `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, and `PARENT_BLOCK` close the socket and navigate to Home.
- GameView closes WebSocket on unmount.
- No Coqui/TTS endpoint is called.
- No raw token appears in route params, query string, local storage, or session storage.

Manual checks:

- Tablet landscape GameView shell.
- Mobile landscape GameView shell.
- Portrait rotation overlay behavior.
- Browser refresh on `/game/:childId` returns to Home.
- Browser back button does not create a child-friendly escape path into an invalid GameView.
- No scrollbars appear in GameView.

## Risks And Mitigations

- Risk: Direct URL access bypasses Home selection.
  Mitigation: guard `/game/:childId` with in-memory active child session state and never restore from URL alone.
- Risk: Frontend invents a child token because the WebSocket currently asks for Bearer auth.
  Mitigation: do not invent tokens; document backend handshake change and keep child session id in memory only.
- Risk: Backend WebSocket rejects browser connections because `Authorization` header cannot be sent by `new WebSocket()` and `childSessionId` is not bound.
  Mitigation: frontend implements lifecycle and handles rejection safely; backend must add browser-compatible child-session binding.
- Risk: TTS scope creep through direct Coqui calls.
  Mitigation: mock welcome locally and never call `openapi_tts.json` from frontend.
- Risk: GameView accidentally uses adult UI feedback semantics.
  Mitigation: use child register colors and never use adult red `#E53935` in GameView.
- Risk: Viewport scrollbars appear on mobile browser chrome changes.
  Mitigation: use fixed viewport layout, dynamic viewport units, safe-area handling, and manual checks on mobile landscape.
- Risk: WebSocket or heartbeat timers leak after leaving GameView.
  Mitigation: centralize cleanup on unmount and terminal events.
- Risk: Implementing game domain logic in frontend.
  Mitigation: keep shell limited to lifecycle, placeholder rendering, and contracted event handling; backend remains source of truth.

## Dependencies

- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - visual tokens and child UI baseline.
- `docs/product/features/frontend/FEAT-002-Home-View.md` - Home child selection and navigation flow.
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - child selector/avatar card behavior.
- `docs/contracts/api/openapi.json` - `OpenChildSessionRequest`, `ChildSessionResponse`, and child session endpoints.
- `docs/contracts/api/websocket.json` - Game WebSocket endpoint and `SessionEvent` contract.
- `docs/design/design_decisions_v1.docx` - child visual register, audio, animation, typography, interaction, and loader rules.
- `docs/design/frontend_design_v1.docx` - route model, GameView architecture, WebSocket lifecycle, viewport, and testing direction.

## Agent Instruction

- Implement only the Game View Shell described here.
- Do not implement backend changes in this frontend feature.
- Do not implement real minigames or game engines.
- Do not add direct Coqui or TTS API calls.
- Do not persist raw tokens or child session state.
- Do not put tokens in WebSocket URLs.
- Use the existing shared Axios client for `POST /api/v1/sessions/children`.
- Use Vue Router, Pinia, Vue i18n, and TypeScript according to the frontend stack.
- Keep all user-visible strings in i18n.
- Keep GameView full viewport, child-facing, and free of adult technical error UI.
- Commit: `feat(frontend): add game view shell`
