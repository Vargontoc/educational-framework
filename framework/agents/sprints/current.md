# Sprint 2 - framework/agents
# -----------------------------------------------

## Goal
Create a Dockerfile at the agents layer root that builds and serves the ai-educational-child agent via Ollama, ready for local development and future deployment.

## Status
status:     completed
started_at: 2026-04-17 00:00:00
closed_at:  2026-04-17 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Write framework/agents/Dockerfile
- [x] Write framework/agents/.dockerignore
- [x] Write framework/agents/entrypoint.sh — starts ollama serve and creates the model
- [x] Verify Modelfile path resolution inside the container
- [x] Update docs/contracts/agents/ai-educational-child.json — add docker section (image name, exposed port)

## Risks
- Ollama model pull inside Docker requires internet access at build time or a pre-pulled model layer — use COPY approach with local model files if offline
- qwen2.5:7b-instruct-q5_K_M is ~5GB — build time will be long on first run
- entrypoint must wait for ollama serve to be ready before running ollama create

## Dependencies
- framework/agents/ai-educational-child/Modelfile must exist — confirmed (Sprint 1 done)
- No backend dependency for this sprint

## Agent Instruction
- Dockerfile goes at framework/agents/Dockerfile (layer root, not inside the agent subdirectory)
- Base image: ollama/ollama
- The container must expose port 11434 (Ollama default)
- entrypoint.sh must: (1) start ollama serve in background, (2) wait for it to be ready, (3) run ollama create ai-educational-child, (4) keep container alive
- .dockerignore must exclude sprints/, skills/, *.md (keep only runtime files: Modelfile + context/)
- Contract update is non-breaking (adding docker metadata only)

## Notes
- This Dockerfile is for the agents layer only — not a full-stack compose
- Backend layer will reference this image when its sprint begins
- Model name inside container: ai-educational-child

## Review

completed_tasks:
  - framework/agents/Dockerfile: usa ollama/ollama:latest, copia solo archivos runtime del agente
  - framework/agents/.dockerignore: excluye sprints/, skills/, *.md excepto Modelfile
  - framework/agents/entrypoint.sh: arranca ollama serve en background, espera readiness con curl, crea el modelo y mantiene el proceso vivo
  - docs/contracts/agents/ai-educational-child.json: bumped a v1.1.0, sección docker añadida (non-breaking)

incomplete_tasks:
  none

contract_changes:
  - docs/contracts/agents/ai-educational-child.json bumped 1.0.0 → 1.1.0
  - Cambio non-breaking: solo añade metadata docker, no altera input/output/events

learnings:
  - .dockerignore necesita excluir *.md globalmente pero whitelistear Modelfile explícitamente — orden importa
  - El entrypoint debe hacer wait $OLLAMA_PID para que el container no muera tras crear el modelo

next_sprint_suggestions:
  - Sprint 3: personalidades explorer, wise, silly
  - Sprint 4: validación local Ollama — probar cada event type con ambas personalidades y medir word count
  - Sprint 5 (backend dep): backend conecta llamada Ollama usando contrato v1.1.0
