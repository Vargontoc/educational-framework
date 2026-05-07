# Sprint 002 - frontend
# -----------------------------------------------

## Goal
Make the frontend SPA reachable via Cloudflare Tunnel in production (ADR-006).
Only the frontend service is exposed externally this sprint — backend remains internal.
Covers: nginx SPA routing fix, production compose verification, and Cloudflare Public Hostname setup.

## Status
status: completed
started_at: 2026-05-06 00:00:00
closed_at: 2026-05-06 00:00:00
blocked_by:
waiting_for:

## Tasks

### nginx SPA Routing
- [x] Create `framework/frontend/app/nginx.conf` — `try_files $uri $uri/ /index.html` for Vue Router history mode; gzip enabled; long-lived cache headers for static assets.
- [x] Update `framework/frontend/app/Dockerfile` — Stage 2 copies `nginx.conf` to `/etc/nginx/conf.d/default.conf`.

### Production Compose
- [x] Verify `app-educational` is present in `docker-compose.prod.yml` with `ports: !reset []` (Cloudflare reaches service via internal Docker network `educational-network`). ✓ Already correct from infrastructure sprint 008.

### Runbook
- [x] Fix incorrect service name in `framework/infrastructure/runbook.md` (was `frontend-educational`, corrected to `app-educational`).

### Cloudflare Dashboard (manual — requires human)
- [ ] In Cloudflare Dashboard → Zero Trust → Networks → Tunnels → `educational-framework-prod` → Public Hostnames tab:
  - Add entry: `app.yourdomain.com` → Service `http://app-educational:80`
  - Subdomain must be within your Cloudflare-managed DNS zone.
- [ ] Confirm SSL mode is set to **Full (strict)**: Dashboard → domain → SSL/TLS → Overview.

### Validation
- [ ] Run production build locally and verify nginx serves the app correctly (see Manual Test Plan).
- [ ] Validate production compose config: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`.

## Risks
- **Vite env vars baked at build time**: `VITE_API_BASE_URL` is empty in production this sprint (backend not exposed). Any future API calls will fail with an empty base URL until the backend is added to Cloudflare in a later sprint. Mitigation: document required build step when backend becomes available.
- **Base image CVEs**: `node:20-alpine` (11 high) and `nginx:alpine` (2 high) report vulnerabilities from the IDE scanner. These are upstream image issues, not application code. Acceptable for now; pin to specific patch versions before a public production rollout.
- **Cloudflare tunnel token**: `TUNNEL_TOKEN` must be set in `framework/infrastructure/envs/cloudflare.env` on the production host before starting the cloudflared container. Never committed to the repo.

## Dependencies
- `framework/infrastructure/docker-compose.prod.yml` — `cloudflared` service already configured (infrastructure Sprint 005). ✓
- `framework/infrastructure/docker-compose.yml` — `app-educational` service already defined (infrastructure Sprint 008). ✓
- `TUNNEL_TOKEN` in `framework/infrastructure/envs/cloudflare.env` — production host manual step.

## Agent Instruction
- Do NOT add backend-facing logic to the frontend in this sprint — `VITE_API_BASE_URL` stays empty in production.
- Do NOT modify `docker-compose.yml` (dev stack) — only verify `docker-compose.prod.yml`.
- All nginx changes live in `framework/frontend/app/nginx.conf` — do not edit the Dockerfile build stage.
- The `nginx.conf` `try_files` fallback must point to `/index.html`, not `@fallback` or any other named location.

## Manual Test Plan

### TC-01: SPA direct-URL routing (critical)
```
docker compose -f docker-compose.yml -f docker-compose.prod.yml build app-educational
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d app-educational
```
- Open `http://localhost:80/` → HomeView renders (i18n key `home.title` appears)
- Open `http://localhost:80/panel` in a new tab (direct URL) → HomeView or PanelControlView renders; **no nginx 404**
- Open `http://localhost:80/game/42` in a new tab (direct URL) → GameView renders; **no nginx 404**
- Open `http://localhost:80/totally-wrong` → redirects to HomeView (Vue Router catch-all)
- Browser refresh on `/panel` → stays on PanelControlView; **no nginx 404**

### TC-02: Static asset caching
```
curl -I http://localhost:80/assets/<bundle>.js
```
- Response header: `Cache-Control: public, immutable`
- Response header: `Content-Encoding: gzip` (if Accept-Encoding: gzip sent)

### TC-03: PWA manifest
- Open `http://localhost:80/manifest.webmanifest` → returns valid JSON with `display: standalone`, `orientation: landscape`

### TC-04: Rotation overlay (browser DevTools)
- Open `http://localhost:80/` in Chrome DevTools → Device Toolbar → select a portrait device
- `RotationOverlay` must appear with the Spanish rotation message

### TC-05: Cloudflare end-to-end (requires tunnel token + dashboard config)
- Start full prod stack: `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d`
- Verify cloudflared shows **Healthy** in Cloudflare Dashboard → Zero Trust → Networks → Tunnels
- Open `https://app.yourdomain.com/` in browser → HomeView loads over HTTPS
- Open `https://app.yourdomain.com/panel` directly → no 404 (TC-01 via Cloudflare)
- Confirm browser shows padlock (SSL Full strict)
- Confirm `http://app.yourdomain.com` redirects to `https://` (Cloudflare HTTPS redirect)

### TC-06: Container stop/restart resilience
- `docker compose -f docker-compose.yml -f docker-compose.prod.yml restart app-educational`
- After restart, repeat TC-01 — app must recover without manual intervention

## Review

completed_tasks:
    - nginx.conf created with try_files SPA routing, gzip, and immutable cache headers for assets.
    - Dockerfile Stage 2 updated to copy nginx.conf into the nginx image.
    - docker-compose.prod.yml verified: app-educational present with ports reset for Cloudflare tunnel routing.
    - runbook.md corrected: service name fixed from frontend-educational to app-educational.

incomplete_tasks:
    - Cloudflare Dashboard Public Hostname entry not created — requires human access to Zero Trust dashboard.
    - SSL Full (strict) mode not confirmed — requires human action in Cloudflare SSL/TLS settings.
    - Production build local validation (TC-01 to TC-06) not executed — requires running Docker environment.

contract_changes:
    None. This sprint did not touch docs/contracts/.

learnings:
    - Infrastructure sprint 008 had already placed app-educational in docker-compose.prod.yml with the correct ports reset — no change needed.
    - Cloudflare dashboard steps are inherently manual and cannot be automated by the agent; they should be tracked as human tasks in future sprints rather than agent tasks.

next_sprint_suggestions:
    - Sprint 003: FEAT-001 Base Styles — CSS variables, Button/Card/Badge components, dev-only design system demo route.
    - Deferred: Cloudflare validation (TC-05) should be revisited once the human completes the dashboard setup.
