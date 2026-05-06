# ADR-010 — Frontend Layer Architecture

## Status

status:        accepted
date:          2026-05-06
superseded_by: —

---

## 1. Context

Web application designed for children aged 3 to 8. The frontend acts as a presentation and
reactive event management layer, delegating all business logic to the backend. Communication
is handled via REST API and WebSocket. Target devices are Android and PC; iOS is out of scope
for this phase.

> **Guiding principle:** The frontend contains no domain logic. It is a reactive renderer of
> events produced by the backend.

---

## 2. Decision Summary

| Decision | Resolution |
|---|---|
| **Technology stack** | Vue 3 + TypeScript. 100% custom components — no generic UI libraries. |
| **State management** | Pinia with 3 minimal stores: `useSessionStore`, `useWSStore`, `useUIStore`. Light persistence via `sessionStorage` for family session state. |
| **REST communication** | Centralised service layer with environment variables for the base URL. Separate configuration per environment. |
| **WebSocket** | Two distinct channels: `GameChannel` and `ParentChannel`. Centralised service independent of Vue components. |
| **WS reconnection** | Automatic exponential backoff. No offline event queue (events are discarded). Avatar handles the waiting state visually. |
| **Routing** | Vue Router in `history` mode with `replace` navigation (no history stack). Global guards protect `/panel` and `/game`. Reload always redirects to Home. |
| **Route protection** | `/panel` requires a PIN validated against the backend. `/game` requires an active child session in the store. No view is directly accessible by URL except Home. |
| **Orientation** | PWA Manifest with `orientation: landscape` as the primary strategy. User-friendly rotation screen as fallback. |
| **PWA** | Web App Manifest with `display: standalone` and `orientation: landscape`. No forced installation. Access via URL in the browser. |
| **Target devices** | Android and PC. iOS out of scope for this phase. |
| **Internationalisation** | `vue-i18n` configured from project inception. Active language: Spanish. Architecture ready for new languages without refactoring. |
| **Environments** | Environment variables for REST and WebSocket base URLs. Separation via `.env` files. |

---

## 3. Decision Detail

### 3.1 State Management — Pinia

The backend is the single source of truth. The frontend maintains only the session and UI state
required to operate between events. Three stores are defined:

- `useSessionStore` — Active family, selected child, authentication state with the backend.
- `useWSStore` — Connection state for both WebSocket channels, active channel in use.
- `useUIStore` — Modal state, loading screens, error messages, and UX states.

`useGameStore` is eliminated. Game logic resides exclusively in the backend; the frontend receives
events and renders without modelling its own game state.

> **Persistence:** `sessionStorage` for family session state, so that an accidental reload does not
> destroy the active parent session.

---

### 3.2 WebSocket — Channels and Reconnection

#### Channels

Two independent channels, each managed by its own service:

- `GameChannel` — Real-time game events during GameView.
- `ParentChannel` — Parental control panel events.

#### Reconnection strategy

The WS service layer implements automatic reconnection with **exponential backoff**: retry attempts
are progressively spaced to avoid saturating the backend. Connection state is reactive and exposed
to the store so that components respond visually.

On reconnect, the backend is notified to resume the session state. Events generated during the
disconnection period are discarded.

> **Reconnection experience for the child (option B):** The avatar plays a specific animation and
> delivers a pre-loaded message while the system works in the background. If reconnection exceeds
> the defined timeout, the avatar requests adult intervention. Detailed behaviour will be discussed
> in the GameView context.

---

### 3.3 Routing — Vue Router

#### Route structure

| Route | View | Protection |
|---|---|---|
| `/` | Home | None — public access |
| `/panel` | PanelControl | PIN validated against backend |
| `/game/:childId` | GameView | Active child session in store |
| `/docs` | Documentation | None — public access |

#### Guards and navigation

Global navigation guards intercept any direct access to protected routes and redirect to Home.
All internal navigation uses `router.replace()` instead of `router.push()`, eliminating the
navigation history stack. The browser back button has no functional effect within the application.

> **Page reload:** There is no route recovery mechanism. The user always returns to Home and must
> authenticate again. A deliberate decision to simplify state management.

---

### 3.4 Orientation and PWA

#### Forced orientation strategy (by priority)

1. **Web App Manifest** — `orientation: landscape`. Primary and most robust solution on Android.
2. **Screen Orientation API** — Invoked programmatically when loading in a fullscreen context. Complements the manifest.
3. **Rotation screen** — Child-friendly visual component as a fallback for edge cases.

#### PWA configuration

The PWA is configured with a complete Manifest but without forced installation. Parents access
via URL in the browser.

- `display: standalone` — Hides the browser bar in installed mode.
- `orientation: landscape` — Forces landscape at the operating system level.
- Icons and splash screen configured for a consistent loading experience.

---

### 3.5 Internationalisation — vue-i18n

`vue-i18n` is configured from project inception. All visible text, whether targeted at children
or adults, is managed through the translation system.

Active language in this phase: **Spanish**. The architecture supports adding new languages by
adding only the corresponding translation files, with no changes to components or logic.

> **Rationale:** Adding i18n retroactively to an app with two distinct audiences (children and
> adults) carries a disproportionate refactoring cost compared to the minimal upfront investment.

---

## 4. Consequences and Implications

### For the frontend team

- The base project must configure Pinia, Vue Router, `vue-i18n`, Vite with `.env` files, and the
  Web App Manifest from the start.
- The WS service layer must be implemented as an independent module, not coupled to Vue components.
- All visible text must go through `vue-i18n` from the first commit. Hardcoded literals in
  templates are not permitted.
- Navigation between views always uses `router.replace()`, never `router.push()`.

### Open discussion points

- Detailed avatar behaviour during WS reconnection — to be addressed in the GameView discussion.
- Component structure for Home, PanelControl, and GameView — pending per-view discussions.
- Avatar animation system and visual states — GameView discussion.

---

## 5. Alternatives Considered and Discarded

| Alternative | Reason for discarding |
|---|---|
| **Vuex instead of Pinia** | Pinia is the official Vue 3 standard, better TypeScript integration and simpler API for lightweight stores. |
| **Game state in frontend** | The backend is the single source of truth. Duplicating logic in the frontend would create inconsistencies. |
| **Single WS channel** | Two distinct channels are maintained for domain clarity and to allow independent connect/disconnect per active view. |
| **Offline event queue** | Game events are time-dependent; queuing them would create inconsistencies with backend state on reconnect. |
| **CSS transform rotate for orientation** | Causes issues with touch event coordinates. Discarded as primary solution; retained only as last-resort fallback. |
| **iOS as a target platform** | Safari restrictions on the Screen Orientation API and divergent PWA behaviours. Out of scope for this phase. |
| **Deferred i18n** | The cost of adding it retroactively to an app with two audiences is disproportionate compared to the minimal initial investment. |

---

## 6. References

- [Vue 3](https://vuejs.org)
- [Pinia](https://pinia.vuejs.org)
- [Vue Router 4](https://router.vuejs.org)
- [vue-i18n v9](https://vue-i18n.intlify.dev)
- [Web App Manifest — MDN](https://developer.mozilla.org/en-US/docs/Web/Manifest)
- [Screen Orientation API — MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen_Orientation_API)
- [Exponential Backoff and Jitter — AWS](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)

---

## 7. Risks and Mitigations (per layer)

Risks identified by cross-referencing frontend decisions with backend ADRs (`ADR-007`, `ADR-008`,
`ADR-009`) and practical mitigations per layer.

### 7.1 Frontend

- Risk: total dependency on the backend as the single source of truth causes experience loss during
  outages or prolonged reconnections (loss of visual progress, game timeout).
  - Mitigation: display clear connection states; implement optimal reconnection with backoff and
    limits; on reconnect, request an explicit state re-sync from the backend and present a recovery
    UI (message and "restore" button).

- Risk: excessive reconnection attempts or simultaneous reconnections from multiple clients
  (children) can overload the backend/WS.
  - Mitigation: apply exponential backoff with jitter (already specified), attempt limit with visual
    exposure; on the client, use staggered reconnection when reopening the app.

- Risk: forced orientation and PWA may behave differently across devices (Android ok, iOS
  problematic although iOS is currently excluded).
  - Mitigation: document per-platform behaviours, detect runtime platform and show fallback UX;
    E2E tests on target devices (Android/PC).

### 7.2 Backend

- Risk: opaque token strategy with DB validation (`ADR-009`) increases reads on every authenticated
  request, impacting latency under load.
  - Mitigation: index `token_hash`, use prepared statements; add in-memory LRU cache with TTL for
    recent tokens if consistency allows; measure and alert on latency.

- Risk: transactional operations for `ChildSession` (single-active enforcement) may produce
  contention or failures under concurrency.
  - Mitigation: implement in a single transaction as specified in `ADR-009`, use appropriate
    indices, apply row-level locking or optimistic verification with controlled retries.

- Risk: WS reconnection and state re-sync may fail to restore consistency if the backend does not
  expose restore endpoints.
  - Mitigation: define an explicit "restore state" contract/endpoint (see
    `docs/contracts/websocket.json`) and ensure the backend can emit `GAME_STATE_UPDATE` with
    resumable state.

- Risk: scheduled jobs (expiration/archival) may fail or stall, leaving sessions unexpired
  (`ADR-009` Consequences).
  - Mitigation: instrument jobs with metrics, logs, and alerts; if distributed, use coordination
    (ShedLock/Quartz) and healthchecks; expose metrics for Prometheus.

### 7.3 Shared / Architecture

- Risk: breaking changes in the `shared` module (`ADR-008`) (e.g. `ApiResponse`, exceptions)
  impact multiple modules.
  - Mitigation: keep contract stable, use integration tests against the `shared` API, internal
    semver policy and mandatory PR reviews for changes to `shared`.

- Risk: Spring AI adoption (`ADR-007`) and additional dependencies may increase startup time and
  introduce implicit coupling.
  - Mitigation: isolate AI integrations via adapters, add health endpoints and configurable
    timeouts; load and startup profiling in CI.

### 7.4 Agents / Modelfiles

- Risk: local models (Ollama/Modelfile) unavailable or failing to load — degradation of the child
  agent functionality.
  - Mitigation: verify Modelfile load on startup; expose health-check for the agent service;
    provide fallback responses or friendly messages if the agent does not respond; version
    Modelfiles with controlled deployment.

- Risk: implicit dependency between Modelfiles and backend (format or response-expectation
  coupling).
  - Mitigation: document the backend-to-agents contract in `docs/contracts/agents` and validate
    with contract tests; never import Modelfiles in backend code (reference by contract only).

---

These mitigations are practical and prioritisable: first instrumentation/monitoring and clear
contracts (`docs/contracts/*`), then performance tuning (indices, caches), and finally operational
hardening (ShedLock, healthchecks, alerts). Maintaining discipline in `shared` and at inter-layer
interfaces reduces the cost of future changes.
