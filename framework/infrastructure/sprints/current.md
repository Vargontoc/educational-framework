# Sprint 002 - infrastructure
# -----------------------------------------------

## Goal
Add Coqui TTS service to the Docker Compose stack per ADR-004, and enforce explicit dev/prod port discipline across all services.

## Status
status: completed
started_at: 2026-04-26 00:00:00
closed_at: 2026-04-26 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Add `coqui-educational` service to `docker-compose.yml` with GPU support, internal network, persistent volume, and dev port exposure (5002:5002).
- [x] Add postgres dev port exposure (5432:5432) to `docker-compose.yml`.
- [x] Add `coqui-educational` production override to `docker-compose.prod.yml` with `ports: !reset []`.
- [x] Add `ports: !reset []` override for `ollama-educational` in `docker-compose.prod.yml` to suppress the dev port in production.
- [x] Add `ports: !reset []` override for `postgres` in `docker-compose.prod.yml`.
- [x] Create `framework/infrastructure/envs/coqui.env.example` with documented env variables.
- [x] Validate both compose files with `docker compose config`.

## Risks
- `ghcr.io/coqui-ai/tts` requires `--use_cuda true` only when an NVIDIA GPU is available; CPU-only setups must override the command manually.
- First startup downloads the model (~300 MB); initial TTS response latency will be high until the model is cached in the volume.
- If port 5002 is not suppressed in production, the TTS API becomes reachable from the host.

## Dependencies
- No blocking dependency on other layers for infrastructure changes.
- Backend layer will consume `http://coqui-educational:5002/api/tts` — contract endpoint documented in ADR-004.

## Agent Instruction
- Follow ADR-004 exactly for service name (`coqui-educational`), image, command, volume name (`coqui_models`), and network.
- Use `runtime: nvidia` consistent with `ollama-educational` for GPU access.
- Dev compose exposes all service ports to the host; prod override suppresses them with `ports: []`.
- Never commit real `.env` files; only `.env.example` files are committed.
- After all changes, validate: `docker compose config` and `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`.

## Notes
Sprint triggered by ADR-004 (docs/architecture/decisions/ADR-004-TTS-Service.md).
Coqui TTS internal endpoint for backend: `http://coqui-educational:5002/api/tts` — never exposed to the UI directly.
Model `tts_models/es/css10/vits` is downloaded to volume `coqui_models` on first start and persists across restarts, following the same pattern as `ollama_models`.
The ADR container snippet uses `educational-coqui` as the hostname reference — this is a typo; the actual Docker Compose service name (and hostname) is `coqui-educational`.

## Acceptance Criteria
- `coqui-educational` service present in `docker-compose.yml` with volume `coqui_models`, network `educational-network-dev`, and port `5002:5002`.
- `postgres` service exposes `5432:5432` in `docker-compose.yml`.
- Running `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` shows no published ports for `coqui-educational`, `ollama-educational`, or `postgres`.
- `envs/coqui.env.example` present; no real `coqui.env` committed.
- `docker compose config` exits 0 for both dev and prod stacks.

## Review

completed_tasks:
    - Added coqui-educational service to docker-compose.yml (ghcr.io/coqui-ai/tts, port 5002:5002, nvidia runtime, coqui_models volume).
    - Added postgres port 5432:5432 to docker-compose.yml for dev convenience.
    - Added !reset [] for ports on all three services in docker-compose.prod.yml; validated no ports published in prod stack.
    - Created envs/coqui.env.example with COQUI_MODEL_NAME and COQUI_USE_CUDA.
    - Both docker compose config and docker compose -f ... -f ... config exit 0.

incomplete_tasks:
    none

contract_changes:
    none — backend contract endpoint http://coqui-educational:5002/api/tts is documented in ADR-004 only; no docs/contracts/ change required at infrastructure level.

learnings:
    - Docker Compose list fields (ports) are merged across override files, not replaced. Setting ports: [] does not clear the base list.
    - Docker Compose v5 supports the YAML !reset tag. ports: !reset [] correctly clears the base port list in a prod override.
    - The ADR-004 snippet references hostname educational-coqui which is incorrect; the Docker Compose service name (and hostname) is coqui-educational.

next_sprint_suggestions:
    - Backend layer: implement HTTP client to call http://coqui-educational:5002/api/tts from Spring Boot, including prosody parameters per ADR-004 (FEAT-003).
    - Infrastructure: add audio cache volume and consider pre-synthesis of catalogue phrases at container startup.
    - Infrastructure: add docker-compose.ci.yml entry for coqui-educational if CI smoke tests require TTS.
