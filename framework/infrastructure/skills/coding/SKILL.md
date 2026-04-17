# SKILL — infrastructure/coding
# ─────────────────────────────────────────────
# Use this skill for: adding new services, configuring existing ones,
# adding volumes, networks, env vars or new docker-compose overrides.

## Before writing anything

1. Read framework/infrastructure/AGENT.md for layer context
2. Check frameworkinfrastructure/sprints/current.md for active tasks
3. Validate current state: docker compose config --quiet
4. Never modify framewokork/backend/  framework/frontend/ or framework/agents source files from this layer

## Adding a new service

Follow this sequence:
1. Define the service in docker-compose.yml under services:
2. Assign it to educational-network explicitly
3. Add a named volume if the service needs persistence
4. Create framework/infrastructure/envs/{service}.env.example with all required vars
5. Add {service}.env to .gitignore if not already present
6. If the service needs a production override, add it to docker-compose.prod.yml
7. Add a healthcheck if other services depend on this one

## Service conventions
- Every service must be on educational-network — no exceptions
- Every service must read its config from env_file, never hardcoded values
- Persistent data always uses named volumes, never anonymous volumes
- container_name must follow: educational-{service} (e.g. educationa-db, educational-ollama)
- restart policy: unless-stopped for local, always for production

## env file conventions
- One .env.example per service in framework/infrastructure/envs/
- .env.example is always committed — .env is always gitignored
- Variable names follow: {SERVICE}_{SETTING} in UPPER_SNAKE_CASE
- Always include a comment above each variable explaining its purpose
- Never include default values that are secrets — use placeholders:
    JWT_SECRET=your_jwt_secret_min_256_bits

## Production overrides
- docker-compose.prod.yml only contains what differs from local
- Database ports must be closed in production: ports: []
- All services use restart: always in production
- Never duplicate entire service definitions — only override what changes

## After coding
- Validate both files: docker compose config --quiet
- Validate prod override: docker compose -f docker-compose.yml
  -f docker-compose.prod.yml config --quiet
- Mark completed tasks in infrastructure/sprints/current.md
- If a new service was added, notify backend or frontend via their
  sprints/current.md Dependencies section if they depend on it