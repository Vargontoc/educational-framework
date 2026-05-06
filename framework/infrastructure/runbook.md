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

## Cloudflare Tunnel — Production Setup (ADR-006)

Cloudflare Tunnel routes public traffic to frontend (port 80) and backend (port 8080)
without opening inbound ports on the host router. All other services remain internal.

### One-time setup (manual — Cloudflare Dashboard)

1. **Create the tunnel**
   - Cloudflare Dashboard → Zero Trust → Networks → Tunnels → Create a tunnel
   - Name: `educational-framework-prod`
   - Connector: Docker
   - Copy the token from the install command shown on screen

2. **Set the token on the production host**
   ```bash
   cp framework/infrastructure/envs/cloudflare.env.example framework/infrastructure/envs/cloudflare.env
   # Edit cloudflare.env and set TUNNEL_TOKEN=<token>
   ```

3. **Configure public hostnames (ingress rules)**
   - In the tunnel's "Public Hostname" tab, add entries as services become available:
     | Subdomain           | Service                       | Available from   |
     |---------------------|-------------------------------|------------------|
     | api.yourdomain.com  | http://api-educational:8080   | Sprint 006       |
     | app.yourdomain.com  | http://app-educational:80     | Sprint 002 (frontend) |
   - These hostnames must match your Cloudflare-managed DNS zone.

4. **Set SSL mode to Full (strict)**
   - Cloudflare Dashboard → your domain → SSL/TLS → Overview
   - Select **Full (strict)** — prevents SSL stripping attacks.

5. **Start the tunnel**
   ```bash
   cd framework/infrastructure
   docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d cloudflared
   docker logs cloudflared-educational
   # Expected: "Connection ... registered with protocol: quic"
   ```

6. **Verify in dashboard**
   - Cloudflare Dashboard → Zero Trust → Networks → Tunnels
   - Tunnel status must show **Healthy** (green).

### Cloudflared container unavailable

Symptoms: public hostnames return 502 or "Tunnel not found".

1. Check container: `docker ps --filter name=cloudflared-educational`
2. Check logs: `docker logs cloudflared-educational --tail 50`
3. Verify token: confirm `CF_TUNNEL_TOKEN` in `cloudflare.env` matches the token in the dashboard.
4. Restart: `docker compose -f docker-compose.yml -f docker-compose.prod.yml restart cloudflared`
5. If issue persists, regenerate the tunnel token in the dashboard and update `cloudflare.env`.
