# Sprint 006 - infrastructure
# -----------------------------------------------

## Goal
Add the `api-educational` Spring Boot service to docker-compose for development and production, wire its env file, suppress the host port in prod so Cloudflare Tunnel routes to it internally, and update the runbook with the ingress rule.

## Status
status: active
started_at: 2026-04-30 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Add `api-educational` service to `docker-compose.yml` (build from `../backend`, port 8080, depends on postgres healthy, healthcheck on `/actuator/health`)
- [ ] Create `envs/backend.env.example` in the infrastructure layer with all runtime env vars the container needs
- [ ] Add `api-educational` override to `docker-compose.prod.yml` (`ports: !reset []`, `SPRING_PROFILES_ACTIVE=prod`)
- [ ] Update runbook: add Cloudflare ingress rule `api.<domain>` → `http://api-educational:8080`
- [ ] Validate: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0

## Risks
- Build context path must be relative to `docker-compose.yml` location — `../backend` from `framework/infrastructure/`
- `depends_on: condition: service_healthy` requires postgres healthcheck to pass; if postgres is slow the backend may restart once on first boot
- `SPRING_DATASOURCE_URL` must use the docker service name `postgres` (not `localhost`)
- Cloudflare ingress rule for `api.<domain>` can only be activated in the dashboard once this sprint is merged and deployed — manual step outside this sprint

## Dependencies
- `framework/backend/Dockerfile` already exists and produces a working JAR (backend Sprint 001)
- `postgres` service already has a healthcheck in `docker-compose.yml`
- No contract changes expected in this sprint

## Agent Instruction
- Only modify infrastructure files — never touch `framework/backend/` source code
- Build context: `context: ../backend` (relative to `framework/infrastructure/`)
- Service name: `api-educational`, container name: `api-educational`
- Dev port binding: `"8080:8080"`
- Prod override: `ports: !reset []` + `environment: - SPRING_PROFILES_ACTIVE=prod`
- Healthcheck: use `wget -qO- http://localhost:8080/actuator/health` — alpine JRE image has `wget`
- Create `envs/backend.env.example` in `framework/infrastructure/envs/` (not in `framework/backend/envs/`)
- Never commit `envs/backend.env` — only `backend.env.example` goes to the repo
- Validate always with the full stack: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`

## Notes

### Why ports: !reset [] in prod for api-educational

With Cloudflare Tunnel, the `cloudflared` container runs inside `educational-network-dev`. It resolves `api-educational:8080` via internal Docker DNS — no host port binding needed. Exposing 8080 to the host in production would be a security surface with no benefit.

Dev keeps `"8080:8080"` so developers can hit the API directly from the browser or Postman without going through Cloudflare.

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
    postgres:
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
api-educational:
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
- `docker-compose.yml` contains `api-educational` with build context, port 8080, depends on postgres healthy, and healthcheck
- `envs/backend.env.example` exists in `framework/infrastructure/envs/` with all required vars
- `docker-compose.prod.yml` overrides `api-educational` with `ports: !reset []` and `SPRING_PROFILES_ACTIVE=prod`
- `runbook.md` documents the Cloudflare ingress rule for `api-educational`
- `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0
- No `backend.env` committed (only `.env.example`)

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
