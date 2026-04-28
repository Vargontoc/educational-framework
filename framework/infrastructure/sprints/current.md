# Sprint 005 - infrastructure
# -----------------------------------------------

## Goal
Preparar la infraestructura de producción para despliegue tras Cloudflare per ADR-006: configurar el servicio Cloudflare Tunnel en docker-compose.prod.yml, crear la plantilla de variables de entorno, documentar el procedimiento manual en el runbook y establecer la estrategia de exposición de puertos en producción.

## Status
status: completed
started_at: 2026-04-28 00:00:00
closed_at: 2026-04-28 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Decidir enfoque Cloudflare con el humano: **Tunnel** elegido.
- [x] Añadir servicio `cloudflared` a `docker-compose.prod.yml` usando imagen `cloudflare/cloudflared` con token via `env_file: envs/cloudflare.env` (variable `TUNNEL_TOKEN`).
- [x] Crear `framework/infrastructure/envs/cloudflare.env.example` con instrucciones para generar el token.
- [x] Añadir entrada de runbook en `framework/infrastructure/runbook.md` con el procedimiento completo de configuración manual en el dashboard de Cloudflare.
- [x] Documentar la estrategia de puertos en producción en las notas del sprint.
- [x] Validar prod compose con `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`.

## Risks
- **Tunnel vs Proxy directo**: elegir la opción equivocada bloquea el despliegue. Tunnel es la opción correcta para entornos domésticos sin IP estática.
- El token del Cloudflare Tunnel (`CF_TUNNEL_TOKEN`) es un secreto de producción — nunca debe commitearse. Solo `.env.example` va al repositorio.
- Frontend y backend no existen aún en el compose — el sprint prepara la estrategia de puertos que esos layers deberán seguir cuando se añadan sus servicios.
- El modo SSL en Cloudflare debe ser **Full (strict)** para evitar ataques de stripping. Documentarlo en el runbook es crítico.

## Dependencies
- El `CF_TUNNEL_TOKEN` se genera en el dashboard de Cloudflare (One → Networks → Tunnels) — requiere acción manual del humano antes de que el servicio arranque.
- Frontend y backend layers deben seguir la estrategia de puertos definida en este sprint cuando añadan sus servicios al compose.
- No hay dependencia bloqueante de otros layers para las tareas de infraestructura.

## Agent Instruction
- Solo modificar `docker-compose.prod.yml`, `runbook.md`, y crear `envs/cloudflare.env.example`.
- NO modificar `docker-compose.yml` (compose de desarrollo) — ADR-006 es exclusivamente para producción.
- El servicio `cloudflared` solo va en `docker-compose.prod.yml`, nunca en `docker-compose.yml`.
- Si se elige el enfoque Proxy directo, no añadir ningún servicio nuevo; solo documentar la estrategia de puertos y el runbook.
- Validar siempre con el stack completo: `docker compose -f docker-compose.yml -f docker-compose.prod.yml config`.

## Notes
Sprint triggered by ADR-006 (docs/architecture/decisions/ADR-006-Cloudflare-service.md).
Nota: el fichero ADR-006 tiene un error interno — su encabezado dice "ADR-005" pero el nombre de fichero es ADR-006. Se referencia por nombre de fichero.

### Por qué Cloudflare Tunnel es la opción recomendada para este proyecto

El proyecto es una aplicación privada monousuario desplegada en entorno doméstico (mismo patrón que Ollama y PostgreSQL en docker-compose). Un entorno doméstico típicamente no tiene IP estática ni acceso para abrir puertos en el router corporativo/ISP.

Con Cloudflare Tunnel:
- El contenedor `cloudflared` establece una conexión saliente a Cloudflare — sin puertos abiertos en el router.
- No requiere IP estática.
- Cloudflare gestiona SSL/TLS de extremo a extremo.
- El token del tunnel es el único secreto requerido.

Con Proxy directo:
- Requiere IP estática (o DDNS) y puertos 80/443 abiertos en el router.
- Más configuración de red.
- Adecuado si ya existe infraestructura de red gestionada.

### Estrategia de puertos en producción (para referencia de otros layers)
```
frontend:   ports: ["80:80"]    ← expuesto al host (Cloudflare lo alcanza)
backend:    ports: ["8080:8080"] ← expuesto al host (Cloudflare lo alcanza)
ollama:     ports: !reset []    ← interno, solo educational-network
postgres:   ports: !reset []    ← interno, solo educational-network
coqui:      ports: !reset []    ← interno, solo educational-network
cloudflared: (sin ports)        ← solo conexión saliente, sin bindings
```

## Acceptance Criteria
- `docker-compose.prod.yml` contiene el servicio `cloudflared` con imagen `cloudflare/cloudflared`, comando `tunnel run`, y token via `CF_TUNNEL_TOKEN` (si se elige Tunnel).
- `envs/cloudflare.env.example` existe con las variables documentadas.
- `runbook.md` tiene una sección "Cloudflare Production Setup" con los pasos manuales.
- `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` exits 0.
- Ningún secreto real commiteado.

## Review

completed_tasks:
    - Added cloudflared service to docker-compose.prod.yml (cloudflare/cloudflared:latest, tunnel --no-autoupdate run, env_file: cloudflare.env).
    - Created envs/cloudflare.env.example with TUNNEL_TOKEN and generation instructions.
    - Added "Cloudflare Tunnel — Production Setup" section to runbook.md with one-time setup, ingress config, SSL mode, and troubleshooting steps.
    - Validated: docker compose -f docker-compose.yml -f docker-compose.prod.yml config exits 0.

incomplete_tasks:
    - Ingress rules (Public Hostname in dashboard) cannot be configured until frontend and backend services exist. Blocked on frontend and backend layers.

contract_changes:
    none

learnings:
    - Docker Compose env_file loads variables into the container at runtime. Using ${VAR} in the environment block resolves at compose parse time from the host shell, not from env_file. For secrets that must come from env_file, use only env_file and name the variable exactly as the consuming process expects it (TUNNEL_TOKEN for cloudflared).
    - cloudflared reads TUNNEL_TOKEN from the container environment automatically when running `tunnel run`. No --token flag needed in the command; the plain command ["tunnel", "--no-autoupdate", "run"] is sufficient when TUNNEL_TOKEN is loaded via env_file.
    - The cloudflared Docker image is distroless — it has no shell (sh/bash). Never use entrypoint: ["sh", "-c"] with this image; always pass arguments directly as a JSON array command.
    - Variable name in env_file must match exactly what the process expects. cloudflared expects TUNNEL_TOKEN (not CF_TUNNEL_TOKEN or similar).

next_sprint_suggestions:
    - Frontend/backend layers: expose ports 80 and 8080 respectively in their service definitions so cloudflared can route to them.
    - Infrastructure: once frontend and backend are in docker-compose.yml, configure Public Hostnames in the Cloudflare Tunnel dashboard and validate end-to-end.
