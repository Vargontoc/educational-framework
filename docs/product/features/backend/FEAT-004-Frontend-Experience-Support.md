# FEAT-004 - Backend: Frontend Experience Support

## Status

state: proposed
user_history: Backend support for the v1 family frontend experience
depends_on: family, session, content
owned_by: backend
test: unit + integration + contract
sprints:
- framework/backend/sprints/planned

## Description

This feature captures backend responsibilities required by the v1 frontend experience defined in `docs/design/frontend_design_v1.docx` and `docs/design/design_decisions_v1.docx`.

The frontend remains a reactive rendering layer. The backend owns family state, session arbitration, catalog-driven activities, avatar fallback copy, runtime game state, and configuration values that affect the child and parent experience.

## Scope

In scope:
- Expose initial family bootstrap data for Home without requiring an authenticated family session.
- Provide parent PIN validation and issue the FamilySession token used by protected parent routes.
- Create and arbitrate ChildSession lifecycle for GameView entry.
- Emit runtime game state and system events required by the frontend map and minigame containers.
- Provide configurable values for inactivity timeout, WebSocket reconnection timeout, generated-copy timeout, and minigame pacing.
- Serve catalog-backed avatar fallback messages for errors, transitions, playful breaks, and system events.
- Broadcast parent-channel events to all active family sessions when child or family session state changes.
- Expose parent dashboard data needed to validate adult-agent responses against real progress data.

Out of scope:
- Frontend rendering, layout, CSS tokens, animation implementation, or Lottie assets.
- Client-side game loops for future MotionEngine or AimEngine.
- External analytics.
- Legal consent flows for a commercial launch.

## Requirements

### Home Bootstrap

- Provide a public endpoint that returns whether a family exists, the family display name when available, and `pinLength`.
- The response must not expose the PIN, child personal data beyond what is needed for the Home entry state, or FamilySession tokens.
- Server errors should be distinguishable from unregistered-family state.

### Parent Access

- Validate the parent PIN through the backend.
- Return an opaque FamilySession token in the response body.
- Invalidate previous FamilySession instances when PIN change flows require it.
- Emit an event to active parent channels when a FamilySession is invalidated by another device or security-sensitive action.

### Child Session Lifecycle

- Create a ChildSession when a child avatar is selected from Home.
- Return enough session metadata for the frontend to enter `/game/:childId` only after the session is active.
- Resolve conflicts when the same child is already active on another device.
- Emit `SYSTEM_EXPELLED` for visible child-session displacement and `SYSTEM_BLOCKED` for blocked access, with child-safe semantics handled by the frontend.

### Runtime Game State

- Backend is the source of truth for the map, available activity, engine type, difficulty, assets, and evaluation.
- `GAME_STATE_UPDATE` must include all rendering parameters required by the active engine.
- MemoryEngine state includes `flipDelayMs`.
- PuzzleEngine state includes `referencePopupIntervalMs` and `referenceInitialDisplayMs` when relevant.
- DotConnectionEngine state includes tracing pattern metadata and visual guide settings.
- Rhythm engine state includes visual and audio stimulus metadata, plus a visual-only fallback path.

### Avatar And Audio Fallbacks

- Avatar events use a two-message protocol when audio is available: JSON metadata first, then correlated MP3 binary by `audioId`.
- JSON metadata includes `eventType`, `audioAvailable`, `audioId`, and fallback `text`.
- AvatarEventCatalog must include fallback messages for Home technical errors, game transitions, reconnection timeout, expelled or blocked session exits, playful breaks, and generated-copy timeout fallback.
- Generated transition phrases must have a backend-configurable timeout before falling back to catalog copy.

### Parent Dashboard Data

- Provide dashboard endpoints for recent child sessions, learning areas, achievements, and a single celebratory highlight.
- Provide historical progress data grouped by session or date so the frontend can render learning curves.
- Avoid exposing unnecessary personally identifiable information in dashboard payloads.

### Parent Channel

- Broadcast relevant session, child-status, agent-status, and action-confirmation events to all active FamilySession instances.
- Multiple panel instances must be supported without requiring a single active device lock.

### Configuration

- Store global family audio and avatar settings.
- Store per-child inactivity timeout, audio override, avatar override, avatar tone, avatar name, and learning-domain priority order.
- Store playful-break cadence as a parent-configurable value.

## Contract Impact

Expected contract areas:
- `docs/contracts/api/openapi.json` for REST bootstrap, session, configuration, dashboard, and content endpoints.
- `docs/contracts/api/websocket.json` for GameChannel and ParentChannel events.
- `docs/contracts/api/agents/` only if AdultAgent context payloads change.

No frontend source file is a contract source of truth.

## Acceptance Criteria

- Home can determine registered or unregistered family state from backend data.
- Parent PIN validation returns a FamilySession token without persisting auth in the frontend.
- Child selection creates or resolves a ChildSession before GameView entry.
- GameView receives backend-authored state for map and engine rendering.
- System events cover expelled, blocked, agent down, agent recovered, reconnection timeout, and app version refresh flows.
- Parent dashboard data is sufficient for the panel and AdultAgent verification use cases.
- OpenAPI and WebSocket contracts document every request, response, and event shape used by the frontend.

## Risks

- Frontend/backend responsibility drift: keep game decisions and adaptive logic in backend contracts.
- Child data privacy: dashboard endpoints must avoid unnecessary PII and external analytics.
- Audio coupling: avatar animation must remain usable when TTS audio is missing or disabled.
- Multi-device conflicts: backend must arbitrate session state and broadcast changes consistently.
