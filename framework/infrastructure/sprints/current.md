# Sprint 001 - infrastructure
# -----------------------------------------------

## Goal
Improve and harden the development and production infrastructure to support Ollama and PostgreSQL, implement backups, and ensure secure, reproducible developer environments per ADR-001.

## Status
status: in-progress
started_at: 2026-04-21 12:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Add `ollama` service to development `docker-compose` with internal network only and no public exposure.
- [ ] Add `postgres` service to development `docker-compose` with persistent volume and example backup schedule.
- [ ] Create `{service}.env.example` templates for `ollama` and `postgres` and document secret handling.
- [ ] Document host/port conventions for Ollama (dev vs prod) and networking rules.
- [ ] Draft PostgreSQL backup & restore procedure (daily backups, 30-day retention) and automation script examples.
- [ ] Add healthchecks and basic metrics endpoints for `ollama` and `postgres`.
- [ ] Document minimal hardware requirements for running Ollama locally and in production.
- [ ] Define firewall/ACL guidance for production Ollama host (do not expose Ollama publicly).
- [ ] Add runbook entries for “Ollama unavailable” scenarios: degrade UX, log events to Postgres, retry/backoff policy.
- [ ] Prepare production deployment notes: recommended reverse-proxy setup, Cloudflare config, and secret management options.
- [ ] Create `framework/infrastructure/envs/{service}.env.example` placeholders and document usage.
- [ ] Add CI checklist item: ensure no secrets committed; validate {service}.env.example present in repo.
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

## Notes
This sprint is derived directly from ADR-001 — it focuses on Option A (Docker + local Ollama in dev, managed Postgres in prod) and implements the ADR checklist items flagged as required for development readiness.

## Acceptance Criteria
- Ollama service defined in `framework/infrastructure/docker-compose.yml` and not published to host ports in production override.
- PostgreSQL defined with named volume `pgdata` and reachable from app; backups can be created with `framework/infrastructure/scripts/backup_postgres.sh`.
- `framework/infrastructure/envs/{service}.env.example` files present for `ollama` and `postgres`; no real `.env` files committed.
- Healthchecks present for `ollama` and `postgres` and report `healthy` within a reasonable time after startup.
- GPU support validated on host and from a GPU-enabled container; `OLLAMA_USE_GPU` configurable and `ollama` can detect GPUs.
- Runbook entries added to `framework/infrastructure/runbook.md` covering Ollama failures and GPU checks.
- CI workflow validates `docker compose config` for local and prod override (see `.github/workflows/ci-infrastructure.yml`).

## Owners
- `infrastructure`: infra-team (owner: infrastructure)
- `ollama` service and GPU validation: infra-devops
- `postgres` backups and retention policy: infra-database
- CI / pipeline checks: ci-owner
- Documentation and runbook: docs-owner
## Review

completed_tasks:
    {}
    
incomplete_tasks:
    {}

contract_changes:
    {}

learnings:
    {}

next_sprint_suggestions:
    {}
