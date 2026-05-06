# Sprint 001 - frontend
# -----------------------------------------------

## Goal
Initialize the Vue 3 + TypeScript + Vite base project with minimum dev and production
configuration: stores, routing, HTTP client, i18n, PWA manifest, and environment
separation — no business logic, consumed by all future feature sprints.

## Status
status: completed
started_at: 2026-05-06 00:00:00
closed_at: 2026-05-06 00:00:00
blocked_by:
waiting_for:

## Tasks

### Project Scaffold
- [x] Initialize Vite + Vue 3 + TypeScript project under `framework/frontend/`
- [x] Install runtime dependencies: `vue-router@4`, `pinia`, `pinia-plugin-persistedstate`, `axios`, `vue-i18n@9`
- [x] Install dev/build dependencies: `vite-plugin-pwa`, `@vitejs/plugin-vue`, `typescript`, `@types/node`
- [x] Configure `tsconfig.json` — strict mode, path alias `@` → `src/`
- [x] Configure `vite.config.ts` — `@vitejs/plugin-vue`, `vite-plugin-pwa`, path alias `@`
- [x] Create a Dockerfile

### Environment Configuration
- [x] Create `.env.development` — `VITE_API_BASE_URL=http://localhost:8080`, `VITE_WS_BASE_URL=ws://localhost:8080`
- [x] Create `.env.production` — `VITE_API_BASE_URL=` and `VITE_WS_BASE_URL=` (empty — injected at deploy time)
- [x] Create `.env.example` with both variables documented (no values)
- [x] Verify `.env*.local` is present in `.gitignore`

### Folder Structure
- [x] Create the following `src/` layout:
  ```
  src/
    assets/
    components/
    i18n/
      es.ts
    router/
      index.ts
    services/
    shared/
      api/
        axios.ts
      types/
    stores/
      useSessionStore.ts
      useWSStore.ts
      useUIStore.ts
    views/
      HomeView.vue
      PanelControlView.vue
      GameView.vue
    App.vue
    main.ts
  ```

### HTTP Client
- [x] Create `src/shared/api/axios.ts` — shared Axios instance:
    - `baseURL: import.meta.env.VITE_API_BASE_URL`
    - Request interceptor: reads token from `useSessionStore` and adds `Authorization: Bearer <token>` header when present
    - Response interceptor: on 401 clears `useSessionStore` and redirects to `/` via `router.replace('/')`

### Routing
- [x] Create `src/router/index.ts` — Vue Router 4 in `history` mode with 4 routes:
    - `/` → `HomeView` (public)
    - `/panel` → `PanelControlView` (guard stub — always passes this sprint)
    - `/game/:childId` → `GameView` (guard stub — always passes this sprint)
    - `/:pathMatch(.*)*` → redirect to `/`
- [x] Global `beforeEach` guard — stubs that log route name in dev; structured for real auth logic in a future sprint
- [x] All programmatic navigation in views uses `router.replace()` — never `router.push()`

### Pinia Stores
- [x] Create `src/stores/useSessionStore.ts`:
    - State: `familyId: number | null`, `selectedChildId: number | null`, `isAuthenticated: boolean`
    - `token: string | null` — in-memory only, NOT in `pick` → cleared on page refresh
    - Persist to `sessionStorage` via `pinia-plugin-persistedstate` (only the three fields above — no tokens)
- [x] Create `src/stores/useWSStore.ts`:
    - State: `gameChannelStatus: 'disconnected' | 'connecting' | 'connected'`, `parentChannelStatus: 'disconnected' | 'connecting' | 'connected'`
    - No persistence
- [x] Create `src/stores/useUIStore.ts`:
    - State: `isLoading: boolean`, `errorMessage: string | null`, `modalOpen: boolean`
    - No persistence
- [x] Register `pinia-plugin-persistedstate` in `main.ts`

### Internationalisation
- [x] Create `src/i18n/es.ts` — Spanish locale with placeholder keys:
    `common.loading`, `common.error`, `home.title`, `panel.title`, `game.title`, `rotation.message`
- [x] Configure `vue-i18n` in `main.ts` — `locale: 'es'`, `fallbackLocale: 'es'`

### PWA Manifest
- [x] Configure `vite-plugin-pwa` in `vite.config.ts`:
    - `registerType: 'autoUpdate'`
    - Manifest: `display: 'standalone'`, `orientation: 'landscape'`, `name`, `short_name`, icon placeholders
- [x] Verify generated `manifest.webmanifest` in build output — confirmed in `dist/manifest.webmanifest`

### Base Views and Components
- [x] Create `src/views/HomeView.vue` — minimal scaffold: `<script setup lang="ts">`, renders `t('home.title')`
- [x] Create `src/views/PanelControlView.vue` — minimal scaffold, renders `t('panel.title')`
- [x] Create `src/views/GameView.vue` — minimal scaffold; reads `route.params.childId`, renders `t('game.title')`
- [x] Create `src/components/RotationOverlay.vue`:
    - Detects portrait orientation via `window.matchMedia('(orientation: portrait)')`
    - Overlays the entire viewport when in portrait with `t('rotation.message')`
    - No store dependency — pure component

### App Entry
- [x] Update `src/App.vue` — `<router-view>` + `<RotationOverlay>`; no business logic
- [x] Update `src/main.ts` — wire `createPinia()` (with persistence plugin), `createRouter()`, `createI18n()` into `createApp(App)`

### Dockerfile
- [x] Create `framework/frontend/Dockerfile` — multi-stage build:
    - Stage 1 (`node:20-alpine`): `npm ci`, `npm run build`
    - Stage 2 (`nginx:alpine`): copy `dist/` to `/usr/share/nginx/html/`; `EXPOSE 80`

## Risks
- **ADR-010 written in Spanish:** resolved — translated to English in this sprint.
- Auth guards are stubs in this sprint — real auth logic (read token from store, redirect on missing) is implemented in the auth feature sprint.
- `pinia-plugin-persistedstate` persists `useSessionStore` to `sessionStorage` for reload
  resilience. Must never persist tokens or credentials — enforce via explicit `persist` pick config.
- `vite-plugin-pwa` orientation lock at manifest level is the primary strategy; Screen Orientation
  API programmatic call is deferred to the GameView sprint (requires fullscreen context).
- `/docs` route present in ADR-010 routing table but absent in `frontend/agent.md` — omitted from
  this sprint; add explicitly if needed in a future sprint.

## Dependencies
- `docs/contracts/api/openapi.json` — exists (backend Sprint 008). ✓
- `docs/contracts/api/websocket.json` — exists (backend Sprint 008). ✓
- No API calls are made in this sprint — contract dependency is for awareness only.

## Agent Instruction
- Do NOT implement real auth logic in guards this sprint — stubs only.
- Do NOT call any backend endpoint in this sprint — Axios instance is configured but unused.
- All visible text must go through `vue-i18n` — zero hardcoded strings in templates.
- Axios request interceptor adds `Authorization: Bearer <token>` only when the token is present in `useSessionStore` — never hardcode or duplicate the header.
- Stores use Composition API style (`defineStore` with setup function) — not Options style.
- Views and components use `<script setup lang="ts">` — never Options API.
- All programmatic navigation must call `router.replace()`, never `router.push()`.
- `useSessionStore` persist config must use explicit `pick` to exclude any future
  credential-like field — fail-safe by allowlist, not blocklist.
- `RotationOverlay` must function without any store import — `matchMedia` listener only.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
Sprint triggered by ADR-010 (`docs/architecture/decisions/ADR-010-Frontend-layer.md`).

### Directory layout after this sprint
```
framework/frontend/
  app/                        ← Vue application root (npm project)
    envs/
      .env.development
      .env.production
      .env.example
    src/
      assets/
      components/
        RotationOverlay.vue
      i18n/
        es.ts
      router/
        index.ts
      services/
      shared/
        api/
          axios.ts
        types/
      stores/
        useSessionStore.ts
        useWSStore.ts
        useUIStore.ts
      views/
        HomeView.vue
        PanelControlView.vue
        GameView.vue
      vite-env.d.ts
      App.vue
      main.ts
    .gitignore
    Dockerfile
    index.html
    package.json
    package-lock.json
    tsconfig.json
    tsconfig.node.json
    vite.config.ts           ← envDir: './envs'
  agent.md
  skills/
  sprints/
    current.md
    history/
```

### Dependency versions (resolved)
| Package | Version |
|---|---|
| vue | ^3.4 |
| vue-router | ^4.3 |
| pinia | ^3.0 (upgraded from ^2.2 — required by pinia-plugin-persistedstate@4) |
| pinia-plugin-persistedstate | ^4.1 |
| axios | ^1.7 |
| vue-i18n | ^9.13 |
| vite | ^5.4 |
| @vitejs/plugin-vue | ^5.1 |
| vite-plugin-pwa | ^0.20 |
| typescript | ^5.5 |
| node (runtime/build) | 20 LTS |

### Dev vs Production configuration delta
| Setting | Development | Production |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Injected at deploy |
| `VITE_WS_BASE_URL` | `ws://localhost:8080` | Injected at deploy |
| Source maps | `true` | `false` |
| Build minify | esbuild (default) | terser |
| vue-devtools | enabled (browser ext) | disabled (default) |
| Console logs in guards | `console.log` stubs | silent |

## Review

completed_tasks:
    - Scaffolded Vue 3 + TypeScript + Vite project (manual files — no interactive cli scaffold to preserve existing agent.md/skills/).
    - tsconfig.json: strict mode, bundler moduleResolution, path alias @→src/.
    - vite.config.ts: @vitejs/plugin-vue, vite-plugin-pwa (orientation: landscape, display: standalone), @ alias.
    - Environment files: .env.development (localhost), .env.production (empty for inject), .env.example (documented).
    - .gitignore: node_modules/, dist/, *.local, .env*.local, .vite/.
    - src/shared/api/axios.ts: shared Axios instance, Bearer token request interceptor, 401 response interceptor.
    - src/router/index.ts: Vue Router 4 history mode, 4 routes, beforeEach stub with dev logging.
    - useSessionStore: familyId/selectedChildId/isAuthenticated persisted to sessionStorage; token in-memory only (excluded from pick).
    - useWSStore: gameChannelStatus, parentChannelStatus — no persistence.
    - useUIStore: isLoading, errorMessage, modalOpen — no persistence.
    - src/i18n/es.ts: Spanish locale with placeholder keys for all views and rotation overlay.
    - main.ts: createApp wired with pinia (+persistedstate plugin), router, i18n.
    - App.vue: router-view + RotationOverlay.
    - View stubs: HomeView, PanelControlView, GameView — all use useI18n(), no hardcoded strings.
    - RotationOverlay: matchMedia portrait detection, fixed overlay z-index 9999, no store dependency.
    - Dockerfile: node:20-alpine build + nginx:alpine serve, EXPOSE 80.
    - Production build verified: vue-tsc type check passed, 63 modules bundled, manifest.webmanifest generated by vite-plugin-pwa.
    - Refactored directory layout: app code moved to app/, .env* files moved to app/envs/, vite.config.ts updated with envDir: './envs'. Build re-verified after move.

incomplete_tasks:
    - PWA icon files (public/icon-192.png, public/icon-512.png) — placeholder paths in manifest; actual icon assets must be added before production deployment.
    - vue-i18n v9 is deprecated (v11 is current) — migration deferred; no breaking changes for this sprint's usage.

contract_changes:
    none — no REST endpoints consumed; openapi.json not affected.

learnings:
    - pinia-plugin-persistedstate@4.x requires pinia@>=3.0.0; pinia@2.x is incompatible. Updated to pinia@^3.0.0.
    - vue-i18n@9 is deprecated as of 2026; v11 is the supported version. Migration is low-risk (Composition API is stable) but deferred.
    - pinia-plugin-persistedstate v4 uses `pick: string[]` (allowlist) instead of v3's `paths: string[]` — safer default for credential hygiene.
    - token field excluded from `pick` achieves in-memory-only storage without additional tooling; cleared automatically on page refresh per ADR-010 §3.3.

next_sprint_suggestions:
    - Sprint 002 (frontend): Implement Home view — family PIN entry, POST /api/v1/auth/login, populate useSessionStore on success, navigate to /panel.
    - Sprint 003 (frontend): Implement PanelControl view — child list, open child session.
    - Dependency: real route guards require useSessionStore.token check — implement after Sprint 002.
