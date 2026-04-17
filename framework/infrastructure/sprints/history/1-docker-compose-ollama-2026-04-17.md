# Sprint 1 - infrastructure

## Goal
Create docker-compose files for local and production environments that start Ollama and load the agents defined in the agents layer.

## Status
status: completed
started_at: 2026-04-17 00:00:00
closed_at: 2026-04-17 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Create docker-compose.yml for local development (Ollama + agents, GPU optional, port exposed)
- [x] Create docker-compose.prod.yml override for production (GPU required, no port exposure to host, restart policy, resource limits)
- [x] Create framework/infrastructure/envs/ollama-educational.env.example with documented variables
- [x] Validate both compose files with: docker compose config
- [x] Validate production override with: docker compose -f docker-compose.yml -f docker-compose.prod.yml config

## Risks
- Ollama model pull (qwen2.5:7b-instruct-q5_K_M) is large; first-start cold time may be long on slow connections
- NVidia GPU driver availability varies between developer machines; local compose must degrade gracefully without GPU
- entrypoint.sh blocks until model is created — healthcheck must reflect actual model readiness, not just port open
- Volume permissions for /root/.ollama may differ on Windows (WSL2) vs Linux hosts
- Production network rule: ollama-educational must NOT expose 11434 to host; backend must reach it via internal network only

## Dependencies
- framework/agents/Dockerfile and entrypoint.sh must be stable (completed in agents Sprint 2)
- framework/agents/ai-educational-child/Modelfile must be present (completed in agents Sprint 1)
- No contract changes expected for this sprint; infrastructure does not own the agent API contract

## Agent Instruction
- Build context for ollama-educational must be set to ../agents (relative to this layer's compose files)
- Local compose: expose port 11434 to host for direct developer testing via curl
- Production compose: do NOT expose port 11434; service is only reachable on educational-network
- NVidia GPU deploy block belongs in docker-compose.prod.yml; local uses cpu_count/mem_limit as soft limits only
- Volume name: ollama_data (named volume, not bind mount) to persist downloaded model weights across restarts
- All env vars must be documented in envs/ollama-educational.env.example; no defaults containing secrets
- After writing files, run: docker compose config to validate syntax

## Notes
- Infrastructure layer path: framework/infrastructure/
- Agents Dockerfile path: framework/agents/Dockerfile
- Agent exposed port: 11434 (Ollama default)
- Network name defined in agent.md: educational-network (bridge)
- Production port rules from agent.md: only frontend:80 and backend:8080 may be exposed to host
- Ollama model created inside container by entrypoint.sh — no pull from registry at runtime

## Review
completed_tasks:
    - Created docker-compose.yml (local): service ollama-educational, port 11434 exposed, named volume ollama_data, healthcheck, educational-network, env_file
    - Created docker-compose.prod.yml (production override): ports !reset [], NVidia GPU deploy block (count:1, capabilities:[gpu]), memory limit 12G, restart always, json-file logging (50m × 5), IMAGE_TAG variable for CI
    - Created envs/ollama-educational.env.example with OLLAMA_HOST, OLLAMA_MAX_LOADED_MODELS, OLLAMA_GPU_MEMORY_FRACTION, OLLAMA_FLASH_ATTENTION, OLLAMA_NUM_THREAD, OLLAMA_LOG_LEVEL
    - Validated local config: docker compose config — passed
    - Validated production config: docker compose -f docker-compose.yml -f docker-compose.prod.yml config — passed, port 11434 absent from production output

incomplete_tasks:
    none

contract_changes:
    none — infrastructure layer does not own agent API contracts

learnings:
    - Docker Compose merges array fields (ports) by appending, not replacing. Used `ports: !reset []` in docker-compose.prod.yml to remove the base port binding in production.
    - healthcheck start_period set to 60s to account for Ollama model creation time inside entrypoint.sh; retries: 12 × 10s = 2 min window total.
    - env_file requires envs/ollama-educational.env to exist at runtime (gitignored); .env.example is the committed template.

next_sprint_suggestions:
    - Add backend service (port 8080) and frontend service (port 80) to docker-compose.yml once those layers define their containers
    - Add .dockerignore for infrastructure layer if build context grows
    - Add a Makefile or shell script with shortcuts: make up, make up-prod, make logs, make down
