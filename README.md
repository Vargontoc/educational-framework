# AI Education Platform

Plataforma educativa basada en agentes de IA diseñada como complemento lúdico para niños de 3 a 8 años. La aplicación ofrece juegos educativos, actividades interactivas y elementos de seguimiento que permiten a las familias acompañar el progreso infantil.

## Qué se ha hecho (resumen)

- Arquitectura y decisiones importantes: el repositorio incluye registros de decisiones arquitectónicas (ADRs) que recogen las decisiones tomadas durante el diseño e implementación. Entre los ADRs presentes están:
  - [ADR-001-Infrastructure-setup.md](docs/architecture/decisions/ADR-001-Infrastructure-setup.md)
  - [ADR-002-education-framework-agent-child.md](docs/architecture/decisions/ADR-002-education-framework-agent-child.md)
  - [ADR-003-education-framework-agent-adult.md](docs/architecture/decisions/ADR-003-education-framework-agent-adult.md)
  - [ADR-004-TTS-Service.md](docs/architecture/decisions/ADR-004-TTS-Service.md)
  - [ADR-005-Voice-Reference.md](docs/architecture/decisions/ADR-005-Voice-Reference.md)
  - [ADR-006-Cloudflare-service.md](docs/architecture/decisions/ADR-006-Cloudflare-service.md)
  - [ADR-007-backend-layer.md](docs/architecture/decisions/ADR-007-backend-layer.md)
  - [ADR-008-Shared-Module.md](docs/architecture/decisions/ADR-008-Shared-Module.md)
  - [ADR-009-Session-Module.md](docs/architecture/decisions/ADR-009-Session-Module.md)

- Features y contenidos ya definidos en la carpeta de producto:
  - Agentes:
    - [FEAT-001-Agent-Child-Modelfile.md](docs/product/features/agents/FEAT-001-Agent-Child-Modelfile.md)
    - [FEAT-002-Agent-Name-bot.md](docs/product/features/agents/FEAT-002-Agent-Name-bot.md)
    - [FEAT-003-Agent-Tone.md](docs/product/features/agents/FEAT-003-Agent-Tone.md)
    - [FEAT-004-Agent-Tell-Curiosities.md](docs/product/features/agents/FEAT-004-Agent-Tell-Curiosities.md)
    - [FEAT-005-Agent-Character.md](docs/product/features/agents/FEAT-005-Agent-Character.md)
    - [FEAT-006-Agent-Motivation-Scope.md](docs/product/features/agents/FEAT-006-Agent-Motivation-Scope.md)
  - Backend:
    - [FEAT-001-Family-Module.md](docs/product/features/backend/FEAT-001-Family-Module.md)
    - [FEAT-002-Session-Module.md](docs/product/features/backend/FEAT-002-Session-Module.md)
  - TTS:
    - [FEAT-001-XTTS-Model.md](docs/product/features/tts/FEAT-001-XTTS-Model.md)

Estos documentos describen las decisiones, el comportamiento de los agentes y las funcionalidades que ya se han acordado y preparado en el repositorio.

## Stack

Java 21 + Spring Boot 3 + SpringAI · Vue 3 + TypeScript · PostgreSQL · Docker

## Quick start — Development

Requisitos: `Docker Desktop`, `docker compose` y `git`.

1. Clona el repositorio y entra en la carpeta:

```bash
git clone <repo-url> educational-framework
cd educational-framework
```

2. Crea los archivos de entorno a partir de los ejemplos y edita secretos según corresponda:

```bash
cp framework/infrastructure/envs/ollama.env.example framework/infrastructure/envs/ollama.env
cp framework/infrastructure/envs/postgres.env.example framework/infrastructure/envs/postgres.env
```

3. Inicia los servicios principales (Ollama + PostgreSQL):

```bash
docker compose -f framework/infrastructure/docker-compose.yml up -d
docker compose -f framework/infrastructure/docker-compose.yml ps
```

4. Carga los agentes en Ollama (cuando el contenedor esté listo):

```powershell
docker cp "framework/agents/education-framework-agent-child/Modelfile" \
    ollama-educational:/root/Modelfile
docker exec ollama-educational ollama create education-framework-agent-child \
    -f /root/Modelfile
docker exec ollama-educational ollama list
```

Los datos persisten en los volúmenes `pgdata` y `ollama_models`.

## Estructura principal

- `framework/backend/`: servicio backend (Java + Spring Boot)
- `framework/frontend/`: aplicación cliente (Vue 3 + TypeScript)
- `framework/agents/`: Modelfiles y contexto de agentes de dominio
- `docs/architecture/decisions/`: ADRs (decisiones arquitectónicas)
- `docs/product/features/`: especificaciones de features ya acordadas

## Trabajar con los agentes

Antes de trabajar con cualquier tarea relacionada con los agentes, consulte los archivos `agent.md` y las guías en `framework/agents/*` o en `analysis/skills/*` según corresponda.

## Rama principal y flujo de trabajo

La rama `develop` actúa como base protegida; los cambios se incorporan mediante PRs desde ramas de trabajo específicas.

---

Enlaces directos a los ADRs y a las features incluidos arriba para facilitar la navegación.
