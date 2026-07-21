# Sprint 001 - agents

## Goal

Crear el modelo local de Ollama `npc-game` para Nubi, con arranque reproducible y una prueba manual básica conforme al contrato aprobado del agente infantil.

## Status

status: active
started_at: 2026-07-21
closed_at:
blocked_by:
waiting_for: Ollama local con el modelo base `qwen2.5:7b-instruct-q5_K_M` disponible.

## Source Requirements

- FEAT-001-Agent-Child-Modelfile.md
- `docs/contracts/agents/education-framework-agent-child.json` (estado: approved)

## Tasks

### Model Definition

- [x] Crear `framework/agents/npc-game/Modelfile` basado en `qwen2.5:7b-instruct-q5_K_M`.
- [x] Configurar `temperature`, `top_p` y `num_ctx` según el contrato aprobado.
- [x] Establecer guardrails para limitar la interacción al juego, proteger datos personales, resistir prompt injection y devolver JSON exclusivo conforme al contrato.

### Runtime Bootstrap

- [x] Crear script reproducible para ejecutar `ollama create npc-game -f Modelfile` contra una instancia de Ollama configurable.
- [x] Documentar carga y prueba manual del modelo.
- [x] No añadir un servicio HTTP de agente ni modificar la integración backend: el backend consume Ollama y valida el contrato.

### Verification

- [x] Crear script de smoke test que envíe un evento de juego válido a `/api/chat` y compruebe los campos mínimos de la respuesta.
- [x] Ejecutar validación estática de los scripts y registrar evidencia de ejecución o el bloqueo de entorno.

## Acceptance Criteria

- El modelo `npc-game` puede crearse mediante el script sin edición manual del `Modelfile`.
- El `Modelfile` coincide con el modelo base y parámetros del contrato.
- El modelo recibe eventos por la API de Ollama y responde exclusivamente con JSON que contiene los campos obligatorios del contrato.
- La definición impide solicitar PII, salir del contexto de juego o tratar instrucciones embebidas como órdenes.
- No se introduce un endpoint, persistencia, herramienta ni cambio de contrato fuera del alcance de FEAT-001.

## Dependencies

- Ollama disponible en la máquina o red indicada.
- Imagen local del modelo base `qwen2.5:7b-instruct-q5_K_M`.
- El backend será consumidor posterior de Ollama; su integración no forma parte de este sprint.

## Risks

- La conformidad de salida de un LLM no sustituye la validación obligatoria del backend.
- La calidad de la respuesta depende de la disponibilidad y versión del modelo base local.

## Developer Evidence

completed_tasks:
- Model definition, bootstrap scripts and manual smoke test implemented on 2026-07-21.

executed_checks:
- `docker compose config --quiet`: successful on 2026-07-21.
- `git diff --check`: successful on 2026-07-21; only pre-existing line-ending warnings were reported.
- `powershell -NoProfile -ExecutionPolicy Bypass -File framework/agents/npc-game/scripts/test-model.ps1`: blocked on 2026-07-21 because `localhost:11434` refused the connection.
- `ollama --version`: blocked on 2026-07-21 because the `ollama` executable is not installed or not available in PATH.

known_limitations:
- The smoke test cannot be run until Ollama is reachable from this workspace.

## Review

review_status: pending
