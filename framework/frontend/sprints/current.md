# Sprint 001 - frontend
# -----------------------------------------------

## Goal
Initialize the Vue 3 + TypeScript + Vite base project with minimum dev and production
configuration: stores, routing, HTTP client, i18n, PWA manifest, and environment
separation — no business logic, consumed by all future feature sprints.

## Status
status: active
started_at: 2026-05-06 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Project Scaffold
- [ ] Initialize Vite + Vue 3 + TypeScript project under `framework/frontend/`
- [ ] Install runtime dependencies: `vue-router@4`, `pinia`, `pinia-plugin-persistedstate`, `axios`, `vue-i18n@9`
- [ ] Install dev/build dependencies: `vite-plugin-pwa`, `@vitejs/plugin-vue`, `typescript`, `@types/node`
- [ ] Configure `tsconfig.json` — strict mode, path alias `@` → `src/`
- [ ] Configure `vite.config.ts` — `@vitejs/plugin-vue`, `vite-plugin-pwa`, path alias `@`

### Environment Configuration
- [ ] Create `.env.development` — `VITE_API_BASE_URL=http://localhost:8080`, `VITE_WS_BASE_URL=ws://localhost:8080`
- [ ] Create `.env.production` — `VITE_API_BASE_URL=` and `VITE_WS_BASE_URL=` (empty — injected at deploy time)
- [ ] Create `.env.example` with both variables documented (no values)
- [ ] Verify `.env*.local` is present in `.gitignore`

### Folder Structure
- [ ] Create the following `src/` layout:
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
- [ ] Create `src/shared/api/axios.ts` — shared Axios instance:
    - `baseURL: import.meta.env.VITE_API_BASE_URL`
    - Request interceptor: reads token from `useSessionStore` and adds `Authorization: Bearer <token>` header when present
    - Response interceptor: on 401 clears `useSessionStore` and redirects to `/` via `router.replace('/')`

### Routing
- [ ] Create `src/router/index.ts` — Vue Router 4 in `history` mode with 4 routes:
    - `/` → `HomeView` (public)
    - `/panel` → `PanelControlView` (guard stub — always passes this sprint)
    - `/game/:childId` → `GameView` (guard stub — always passes this sprint)
    - `/:pathMatch(.*)*` → redirect to `/`
- [ ] Global `beforeEach` guard — stubs that log route name in dev; structured for real auth logic in a future sprint
- [ ] All programmatic navigation in views uses `router.replace()` — never `router.push()`

### Pinia Stores
- [ ] Create `src/stores/useSessionStore.ts`:
    - State: `familyId: number | null`, `selectedChildId: number | null`, `isAuthenticated: boolean`
    - Persist to `sessionStorage` via `pinia-plugin-persistedstate` (only the three fields above — no tokens)
- [ ] Create `src/stores/useWSStore.ts`:
    - State: `gameChannelStatus: 'disconnected' | 'connecting' | 'connected'`, `parentChannelStatus: 'disconnected' | 'connecting' | 'connected'`
    - No persistence
- [ ] Create `src/stores/useUIStore.ts`:
    - State: `isLoading: boolean`, `errorMessage: string | null`, `modalOpen: boolean`
    - No persistence
- [ ] Register `pinia-plugin-persistedstate` in `main.ts`

### Internationalisation
- [ ] Create `src/i18n/es.ts` — Spanish locale with placeholder keys:
    `common.loading`, `common.error`, `home.title`, `panel.title`, `game.title`, `rotation.message`
- [ ] Configure `vue-i18n` in `main.ts` — `locale: 'es'`, `fallbackLocale: 'es'`

### PWA Manifest
- [ ] Configure `vite-plugin-pwa` in `vite.config.ts`:
    - `registerType: 'autoUpdate'`
    - Manifest: `display: 'standalone'`, `orientation: 'landscape'`, `name`, `short_name`, icon placeholders
- [ ] Verify generated `manifest.webmanifest` in build output

### Base Views and Components
- [ ] Create `src/views/HomeView.vue` — minimal scaffold: `<script setup lang="ts">`, renders `$t('home.title')`
- [ ] Create `src/views/PanelControlView.vue` — minimal scaffold, renders `$t('panel.title')`
- [ ] Create `src/views/GameView.vue` — minimal scaffold; reads `route.params.childId`, renders `$t('game.title')`
- [ ] Create `src/components/RotationOverlay.vue`:
    - Detects portrait orientation via `window.matchMedia('(orientation: portrait)')`
    - Overlays the entire viewport when in portrait with `$t('rotation.message')`
    - No store dependency — pure component

### App Entry
- [ ] Update `src/App.vue` — `<router-view>` + conditional `<RotationOverlay>`; no business logic
- [ ] Update `src/main.ts` — wire `createPinia()` (with persistence plugin), `createRouter()`, `createI18n()` into `createApp(App)`

### Dockerfile
- [ ] Create `framework/frontend/Dockerfile` — multi-stage build:
    - Stage 1 (`node:20-alpine`): `npm ci`, `npm run build`
    - Stage 2 (`nginx:alpine`): copy `dist/` to `/usr/share/nginx/html/`; `EXPOSE 80`

## Risks
- **ADR-010 written in Spanish:** global rules require English for all documentation.
  The ADR should be translated before the next review cycle.
- Auth guards are stubs in this sprint — real auth logic (read token from store, redirect on missing) is implemented in the auth feature sprint.
- `pinia-plugin-persistedstate` persists `useSessionStore` to `sessionStorage` for reload
  resilience. Must never persist tokens or credentials — enforce via explicit `persist.paths` config.
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
- `useSessionStore` persist config must use explicit `paths` or `pick` to exclude any future
  credential-like field — fail-safe by allowlist, not blocklist.
- `RotationOverlay` must function without any store import — `matchMedia` listener only.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
Sprint triggered by ADR-010 (`docs/architecture/decisions/ADR-010-Frontend-layer.md`).

### Dependency versions (baseline)
| Package | Version |
|---|---|
| vue | ^3.4 |
| vue-router | ^4.3 |
| pinia | ^2.2 |
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
| Console logs in guards | `console.log` stubs | removed (no-console lint) |

### Package layout after this sprint
```
framework/frontend/
  public/
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
    App.vue
    main.ts
  .env.development
  .env.production
  .env.example
  vite.config.ts
  tsconfig.json
  package.json
  Dockerfile
  sprints/
    current.md
    history/
```

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
