# Sprint 008 - infrastructure
# -----------------------------------------------

## Goal
Add the `app-educational` (frontend) service to the local Docker Compose environment.

## Status
status: completed
started_at: 2026-05-06 12:36:10
closed_at: 2026-05-06 12:37:30
blocked_by: 
waiting_for: 

## Tasks
- [x] Create `framework/infrastructure/envs/frontend.env.example` with default values for `FRONTEND_BIND_IP` and `FRONTEND_HOST_PORT`.
- [x] Add `app-educational` service to `framework/infrastructure/docker-compose.yml` using `../frontend/app` as build context.
- [x] Configure `app-educational` to depend on `api-educational` (service_healthy).
- [x] Configure `app-educational` healthcheck (e.g., using `wget` to port 80).
- [x] Validate configuration using `docker compose config`.

## Risks
- The frontend container might fail to start if the Nginx configuration is not properly handling the SPA routing (though the current `Dockerfile` seems standard).
- Network reachability issues if the frontend container cannot reach the backend API via the browser (CORS or incorrect API URL environment variables at build time).

## Dependencies
- Requires the frontend `Dockerfile` to be stable (`framework/frontend/app/Dockerfile`).

## Agent Instruction
- When adding the service, use `FRONTEND_BIND_IP:-127.0.0.1` and `FRONTEND_HOST_PORT:-80` mapped to container port 80.
- Do not commit real `.env` files, only the `.env.example`.
- Ensure the service is attached to the `educational-network-dev` network.

## Review
completed_tasks:
    - Created `frontend.env.example` and `frontend.env` files for the `app-educational` service.
    - Successfully added the `app-educational` block to the `docker-compose.yml` mapping port 80 locally.
    - Verified `docker-compose.yml` configuration parsing with no errors.

incomplete_tasks:
    - None.

contract_changes:
    - No changes to API/schema contracts.

learnings:
    - The configuration for the `app-educational` frontend was integrated correctly into the main `docker-compose.yml` without introducing networking conflicts or syntax errors.

next_sprint_suggestions:
    - Validate that the frontend successfully reaches the backend API in a full run (`docker compose up -d`).
