# Sprint 006 - infrastructure
# -----------------------------------------------

## Goal
Add the `api-educational` Spring Boot service to docker-compose, rename `postgres` to `postgres-educational` for naming consistency, introduce a separate `educational-network` prod network to allow dev and prod stacks to run simultaneously without conflicts, and wire Cloudflare Tunnel routing to the backend.

## Status
status: active
started_at: 2026-04-30 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Rename `postgres` service and container to `postgres-educational` in `docker-compose.yml` (service name, container_name, healthcheck user ref); update `SPRING_DATASOURCE_URL` in `backend.env.example` accordingly
- [ ] Add prod network `educational-network` to `docker-compose.prod.yml`: declare it and override all service `networks:` entries to use `educational-network` instead of `educational-network-dev`; dev compose keeps `educational-network-dev`
- [ ] Add `api-educational` service to `docker-compose.yml` (build from `../backend`, port 8080, depends on `postgres-educational` healthy, healthcheck on `/actuator/health`)
- [ ] Create `envs/backend.env.example` in the infrastructure layer with all runtime env vars the container needs
- [ ] Add `api-educational` override to `docker-compose.prod.yml` (`ports: !reset []`, `SPRING_PROFILES_ACTIVE=prod`)
- [ ] Update runbook: add Cloudflare ingress rule `api.<domain>` → `http://api-educational:8080`
- [ ] Validate: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0

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

### Why ports: !reset [] in prod for api-educational

With Cloudflare Tunnel, `cloudflared` runs inside `educational-network` and resolves `api-educational:8080` via internal Docker DNS — no host port binding needed. Dev keeps `"8080:8080"` for direct Postman/browser access.

### Service definition sketch (dev)

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

### Prod override sketch

```yaml
networks:
  educational-network:
    name: educational-network
    driver: bridge

services:
  ollama-educational:
    networks: [educational-network]
  postgres-educational:
    networks: [educational-network]
  coqui-educational:
    networks: [educational-network]
  cloudflared:
    networks: [educational-network]
  api-educational:
    networks: [educational-network]
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    ports: !reset []
```

### backend.env.example variables

Variables sourced from `framework/backend/envs/backend.env.example` (backend layer owns the canonical list):

```
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/edu_db
SPRING_DATASOURCE_USERNAME=edu
SPRING_DATASOURCE_PASSWORD=change_me
SERVER_PORT=8080
SPRING_AI_OLLAMA_BASE_URL=http://ollama-educational:11434
SPRING_AI_OLLAMA_CHAT_MODEL=llama3.2
LOGGING_LEVEL_ES_VARGONTOC=INFO
JWT_SECRET=change_me_use_openssl_rand_hex_32
JWT_EXPIRATION_MS=86400000
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
- `docker-compose.prod.yml` declares `educational-network` and overrides all service `networks:` to use it
- `docker-compose.prod.yml` overrides `api-educational` with `ports: !reset []` and `SPRING_PROFILES_ACTIVE=prod`
- `runbook.md` documents the Cloudflare ingress rule for `api-educational`
- `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0 and shows no `educational-network-dev` in the prod merged output
- No `backend.env` committed (only `.env.example`)

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
