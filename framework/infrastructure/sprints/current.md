# Sprint 006 - infrastructure
# -----------------------------------------------

## Goal
Add the `api-educational` Spring Boot service to docker-compose, rename `postgres` to `postgres-educational` for naming consistency, introduce a separate `educational-network` prod network to allow dev and prod stacks to run simultaneously without conflicts, and wire Cloudflare Tunnel routing to the backend.

## Status
status: completed
started_at: 2026-04-30 00:00:00
closed_at: 2026-04-30 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Rename `postgres` service and container to `postgres-educational` in `docker-compose.yml`; update `SPRING_DATASOURCE_URL` in `backend.env.example` accordingly
- [x] Add prod network `educational-network` to `docker-compose.prod.yml`: override the top-level `educational-network-dev` network declaration with `!override` so the actual Docker network is named `educational-network` in prod; dev compose keeps `educational-network-dev`
- [x] Add `api-educational` service to `docker-compose.yml` (build from `../backend`, port 8080, depends on `postgres-educational` healthy, healthcheck on `/actuator/health`)
- [x] Create `envs/backend.env.example` in the infrastructure layer with all runtime env vars the container needs
- [x] Add `api-educational` override to `docker-compose.prod.yml` (`ports: !reset []`, `SPRING_PROFILES_ACTIVE=prod`)
- [x] Update runbook: add Cloudflare ingress rule `api.<domain>` → `http://api-educational:8080`
- [x] Validate: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0

## Risks
- Renaming `postgres` → `postgres-educational` changes the Docker service DNS name; any hardcoded `postgres` hostname in env files or backend config will break — update `SPRING_DATASOURCE_URL` in all env examples
- Build context path must be relative to `docker-compose.yml` location — `../backend` from `framework/infrastructure/`
- `depends_on: condition: service_healthy` requires `postgres-educational` healthcheck to pass; if postgres is slow the backend may restart once on first boot
- Network name override in prod: Docker Compose merges `networks:` maps, so the prod file must explicitly list `educational-network` for each service and declare it at the top level; `educational-network-dev` is not declared in `docker-compose.prod.yml` and must not appear there
- Cloudflare ingress rule for `api.<domain>` can only be activated in the dashboard once this sprint is merged and deployed — manual step outside this sprint

## Dependencies
- `framework/backend/Dockerfile` already exists and produces a working JAR (backend Sprint 001)
- `postgres-educational` service must have a healthcheck before `api-educational` can declare `depends_on`
- No contract changes expected in this sprint

## Agent Instruction
- Only modify infrastructure files — never touch `framework/backend/` source code
- Rename `postgres` → `postgres-educational` in `docker-compose.yml`: service key, `container_name`, healthcheck `-U` user ref if hardcoded; do NOT change the postgres image or volume names
- Prod network: declare `educational-network` (driver: bridge) in `docker-compose.prod.yml` top-level `networks:` block; override each service's `networks:` to `[educational-network]`; the dev compose keeps `educational-network-dev` unchanged
- `cloudflared` in `docker-compose.prod.yml` must also be moved to `educational-network`
- Build context for api-educational: `context: ../backend` (relative to `framework/infrastructure/`)
- Service name: `api-educational`, container name: `api-educational`
- Dev port binding: `"8080:8080"`
- Prod override for api-educational: `ports: !reset []` + `environment: - SPRING_PROFILES_ACTIVE=prod`
- Healthcheck: `wget -qO- http://localhost:8080/actuator/health` — alpine JRE image has `wget`
- Create `envs/backend.env.example` in `framework/infrastructure/envs/` (not in `framework/backend/envs/`)
- `SPRING_DATASOURCE_URL` in the new `backend.env.example` must use `postgres-educational` as hostname
- Never commit `envs/backend.env` — only `backend.env.example` goes to the repo
- Validate always with the full stack: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`

## Notes

### Why a separate prod network

Dev uses `educational-network-dev`; prod uses `educational-network`. With different network names both stacks can be up simultaneously on the same host (e.g. developer runs dev locally while prod is running). Docker will create two isolated bridge networks without name collision.

### How the prod network override works

Docker Compose v5's `!reset` on service-level `networks:` sequences is broken — it falls back to the Docker default network instead of replacing with the specified value. The working approach is `!override` on the TOP-LEVEL network declaration:

```yaml
# docker-compose.prod.yml
networks:
  educational-network-dev: !override
    name: educational-network
    driver: bridge
```

This keeps the internal compose key as `educational-network-dev` (so service definitions need no changes) but instructs Docker to create the actual bridge network as `educational-network`. Result: dev containers live on `educational-network-dev`, prod containers live on `educational-network` — fully isolated.

### Service definition (dev)

```yaml
api-educational:
  build:
    context: ../backend
    dockerfile: Dockerfile
  image: api-educational:latest
  container_name: api-educational
  env_file:
    - envs/backend.env
  networks:
    - educational-network-dev
  ports:
    - "8080:8080"
  depends_on:
    postgres-educational:
      condition: service_healthy
  restart: unless-stopped
  healthcheck:
    test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 60s
```

### Prod override for api-educational

```yaml
api-educational:
  environment:
    - SPRING_PROFILES_ACTIVE=prod
  ports: !reset []
```

### Cloudflare ingress rule to add (manual — dashboard)

Once this sprint is merged and deployed, add in the tunnel's "Public Hostname" tab:

| Subdomain            | Service                      |
|----------------------|------------------------------|
| api.yourdomain.com   | http://api-educational:8080  |

## Acceptance Criteria
- `docker-compose.yml` uses `postgres-educational` as service name and container name everywhere
- `docker-compose.yml` contains `api-educational` with build context, port 8080, depends on `postgres-educational` healthy, and healthcheck
- `envs/backend.env.example` exists in `framework/infrastructure/envs/` with `SPRING_DATASOURCE_URL` pointing to `postgres-educational`
- `docker-compose.prod.yml` overrides the `educational-network-dev` top-level network declaration to produce `educational-network` as the real Docker bridge name
- `docker-compose.prod.yml` overrides `api-educational` with `ports: !reset []` and `SPRING_PROFILES_ACTIVE=prod`
- `runbook.md` documents the Cloudflare ingress rule for `api-educational`
- `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0 and shows `name: educational-network` in the top-level networks block
- No `backend.env` committed (only `.env.example`)

## Review

completed_tasks:
    - Renamed postgres → postgres-educational (service key, container_name) in docker-compose.yml.
    - Added api-educational service to docker-compose.yml: build from ../backend, port 8080, depends_on postgres-educational (service_healthy), healthcheck on /actuator/health via wget.
    - Created envs/backend.env.example in the infrastructure layer with all runtime vars including SPRING_DATASOURCE_URL pointing to postgres-educational.
    - Overrode docker-compose.prod.yml top-level network with `!override` to rename educational-network-dev → educational-network at the Docker level; dev and prod stacks are now fully isolated when running simultaneously.
    - Added api-educational prod override: ports: !reset [], SPRING_PROFILES_ACTIVE=prod.
    - Updated runbook with Cloudflare ingress rule for api.yourdomain.com → http://api-educational:8080.
    - Validated: docker compose -f docker-compose.yml -f docker-compose.prod.yml config exits 0.

incomplete_tasks:
    - Cloudflare Public Hostname for api.yourdomain.com cannot be configured until this sprint is deployed to production — manual dashboard step.
    - Backend Sprint 001 manual verification steps (./mvnw spring-boot:run, docker build) are backend layer responsibility, not infrastructure.

contract_changes:
    none

learnings:
    - Docker Compose v5 `!reset` on service-level `networks:` sequences is broken — it resets to the Docker default network instead of replacing with the specified list items. Use `!override` on the TOP-LEVEL networks declaration to rename the actual Docker network while keeping internal compose key references unchanged across all service definitions.
    - The `!override` approach for network renaming is more elegant than per-service overrides: one declaration in the networks block handles all services without touching individual service definitions.

next_sprint_suggestions:
    - Backend layer: complete Sprint 001 manual verification (mvnw spring-boot:run, docker build) and ensure the service starts correctly inside the compose stack.
    - Infrastructure: add frontend-educational service once frontend layer scaffold is ready; expose port 80 and add Cloudflare ingress rule for app.yourdomain.com.
