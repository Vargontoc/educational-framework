# Infrastructure Runbook (minimum)

## Ollama Unavailable

Symptoms:
- Agent requests fail with connection errors or 5xx responses.

Immediate actions:
1. Check container health: `docker ps` and `docker inspect ollama-educational`.
2. Check service logs: `docker logs ollama-educational` for errors.
3. If using GPU, verify NVIDIA drivers and container runtime: run `nvidia-smi` on the host and
	`docker run --rm --gpus all nvidia/cuda:11.0-base nvidia-smi` to ensure containers can access the GPU.
3. If unavailable, mark system state: write an error event to Postgres (tracking table `agent_errors`) with timestamp and reason.
4. Trigger configurable retry/backoff (default: 3 attempts, exponential backoff starting 2s).

Degraded UX guidance:
- Show user state: "Agent temporarily unavailable" and allow non-LLM flows.

Recovery steps:
1. Restart container: `docker restart ollama-educational`.
2. If restart fails, check host resources (CPU, RAM, disk, VRAM) and available GPU drivers.
3. Check for crashed model processes or OOM; consult logs.

GPU-specific checks:
- Confirm host NVIDIA drivers present: `nvidia-smi` should list GPUs and processes.
- Confirm Docker has GPU support: `docker run --rm --gpus all nvidia/cuda:11.0-base nvidia-smi` returns GPU info.
- If GPU device access fails inside the container, check Docker Engine version and NVIDIA Container Toolkit installation.

Escalation:
- If issue persists >30 minutes, notify ops and consider failover to a remote/managed model endpoint.

## Postgres Backup & Restore (quick)

Backup:
- Use `framework/infrastructure/scripts/backup_postgres.sh` with appropriate env vars.

Restore:
- Example: `gunzip -c backupfile.sql.gz | psql -U $POSTGRES_USER -d $POSTGRES_DB -h $PGHOST`

## Network / Firewall

- Ollama must not be exposed to public internet. Use Docker network or host loopback. Apply firewall rules to restrict access to Ollama host in production.
