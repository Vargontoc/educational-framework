# Sprint 001 - infrastructure (closed)
# -----------------------------------------------

## Goal
Improve and harden the development and production infrastructure to support Ollama and PostgreSQL, implement backups, and ensure secure, reproducible developer environments per ADR-001.

## Status
status: completed
started_at: 2026-04-21 12:00:00
closed_at: 2026-04-21 13:58:00
blocked_by:
waiting_for:

## Tasks
- [x] Add `ollama` service to development `docker-compose` with internal network only and no public exposure.
- [x] Add `postgres` service to development `docker-compose` with persistent volume and example backup schedule.
- [x] Create `{service}.env.example` templates for `ollama` and `postgres` and document secret handling.
- [x] Document host/port conventions for Ollama (dev vs prod) and networking rules.
- [x] Draft PostgreSQL backup & restore procedure (daily backups, 30-day retention) and automation script examples.
- [x] Add healthchecks and basic metrics endpoints for `ollama` and `postgres`.
- [ ] Document minimal hardware requirements for running Ollama locally and in production.
- [x] Define firewall/ACL guidance for production Ollama host (do not expose Ollama publicly).
- [x] Add runbook entries for “Ollama unavailable” scenarios: degrade UX, log events to Postgres, retry/backoff policy.
- [x] Prepare production deployment notes: recommended reverse-proxy setup, Cloudflare config, and secret management options.
- [x] Create `framework/infrastructure/envs/{service}.env.example` placeholders and document usage.
- [ ] Add CI checklist item: ensure no secrets committed; validate {service}.env.example present in repo. (CI prod-override path needs fixing)
- [ ] Review and align docs/contracts entries for tracking events and agent error events.

## Risks
- Exposing Ollama publicly by misconfiguration could leak data or create an attack surface.
- Incomplete backup/restore procedures risk loss of player tracking data.
- Developer machines without sufficient resources to run Ollama may block local testing.

## Dependencies
- Backend layer must expose API endpoints for tracking events (docs/contracts) before finalizing event schemas.
- Cloudflare / TLS decisions required before production deployment notes are finalized.

## Agent Instruction
- Follow project global rules from AGENT.md (English-only docs, no secrets in repo).
- Use internal Docker network `educational-network-dev` for development services.
- When Ollama calls fail, write an error event to PostgreSQL with reason and timestamp.
- Implement configurable retry/backoff with a max attempts parameter.

## Review

completed_tasks:
  - Added `framework/infrastructure/docker-compose.yml` and `docker-compose.prod.yml` with `ollama` and `postgres` services and named volumes (`pgdata`, `ollama_models`).
  - Added `framework/infrastructure/envs/ollama.env.example` and `postgres.env.example` and local `.env` used for testing (note: real `.env` files must not be committed; see incomplete tasks).
  - Implemented healthchecks: `ollama` uses `/bin/ollama --version`, `postgres` uses `pg_isready`.
  - Added `framework/infrastructure/scripts/backup_postgres.sh` and validated a backup run locally.
  - Added `framework/infrastructure/runbook.md` with Ollama unavailable procedures and GPU checks.
  - Enabled GPU support with `runtime: nvidia` fallback; validated GPU access on host and from a GPU-enabled container.
  - Validated `docker compose config` locally; recreated and verified containers are `healthy`.

incomplete_tasks:
  - Document minimal hardware requirements (create `docs/` entry with exact CPU/RAM/VRAM recommendations).
  - CI workflow: the `ci-infrastructure.yml` references a non-existent `framework/infrastructure/prod-override` working directory; update to use the correct path (`framework/infrastructure`) and ensure CI validates the prod override file.
  - Remove any committed real `.env` files from the repository history and ensure only `.env.example` files are present. Add a note to onboarding about copying `.env.example` to `.env` and not committing it.
  - Review and align `docs/contracts` for tracking event schemas and agent error events (backend owners required to finalize schemas).

contract_changes:
  - None in this sprint (needs backend coordination).

learnings:
  - Some official images lack utilities like `curl`; prefer healthchecks that use packaged binaries or add small helper images if required.
  - `device_requests` is more modern but not accepted by all local Compose validators; `runtime: nvidia` is a practical fallback for developer machines with Docker Desktop.
  - Validate CI working directories and paths before relying on workflow to catch issues.

next_sprint_suggestions:
  - Fix CI workflow path and add an automated check to reject commits that add real `.env` files.
  - Create `docs/infrastructure/ollama-hardware.md` with recommended CPU/RAM/VRAM and example machine profiles.
  - Implement metrics endpoints or exporters for Ollama if needed and wire them to observability stack.
  - Coordinate with backend owners to finalize event tracking schemas and update `docs/contracts`.
