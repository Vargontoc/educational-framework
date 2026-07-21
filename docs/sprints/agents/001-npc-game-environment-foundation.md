# Sprint 001 - agents

## Goal

Dejar disponible un modelo `npc-game` en el Ollama externo de infraestructura local, con carga reproducible mediante su API y comprobacion manual basica, sin integrarlo todavia con el flujo de juego.

## Status

status: active
started_at: 2026-07-21
closed_at:
blocked_by:
waiting_for:

## Tasks

- [x] Crear la definicion `Modelfile` para el modelo local `npc-game` a partir de un modelo base de Ollama adecuado.
- [x] Limitar la personalidad inicial a un acompanamiento tranquilo y jugueton, breve, seguro y exclusivo del contexto de juego infantil.
- [x] Usar `qwen2.5:7b-instruct-q5_K_M` como modelo base para `npc-game` y ajustar el Modelfile a sus parametros de generacion conservadores.
- [x] Adaptar el script reproducible para crear o actualizar `npc-game` mediante la API HTTP de una instancia Ollama configurable, sin requerir el ejecutable local `ollama`.
- [x] Crear un smoke test manual que compruebe que Ollama puede responder a una solicitud basica del modelo.
- [x] Ejecutar la carga y el smoke test contra el Ollama externo de infraestructura local expuesto en `http://127.0.0.1:11434`.
- [x] Documentar los requisitos de entorno, los pasos de carga y la prueba manual.
- [x] Registrar las tareas realizadas, comprobaciones ejecutadas y bloqueos de entorno en este sprint.

## Risks

- El Ollama externo puede estar temporalmente no disponible o con estado de salud degradado aunque su API responda.
- El modelo base recomendado puede requerir descarga antes de crear `npc-game`.
- La respuesta del modelo puede no ajustarse siempre a las restricciones de contenido; esta base no sustituye las validaciones futuras del backend.
- Un formato provisional de solicitud o respuesta podria confundirse con el contrato definitivo del juego.

## Dependencies

- Ollama de infraestructura externa, contenedor `ollama`, accesible mediante `http://127.0.0.1:11434`.
- Modelo base `qwen2.5:7b-instruct-q5_K_M` disponible en dicha instancia; actualmente debe descargarse.
- La comunicacion real queda pendiente de los eventos y esquemas de minijuegos definidos por backend.

## Agent Instruction

- Implementar exclusivamente el arranque de `npc-game` en el Ollama externo de infraestructura local desde `framework/agents/npc-game/`.
- No modificar backend, frontend, TTS, Docker, contratos compartidos ni la feature funcional.
- No crear endpoint, persistencia, herramientas MCP, datos de menores ni integracion con WebSocket.
- No establecer un contrato definitivo de entrada o salida: cualquier solicitud del smoke test es solo de disponibilidad local.
- Mantener los mensajes de prueba breves, apropiados para 3-4 anos y sin datos personales, evaluaciones, diagnosticos, presion o conversacion abierta.
- Usar la API HTTP de Ollama para verificar, descargar si es necesario, crear y probar el modelo. No depender de que `ollama` este instalado en el PATH del host.
- Usar por defecto `http://127.0.0.1:11434`, manteniendo un parametro o variable de entorno para otro host compatible.
- Antes de crear el modelo, asegurar que el modelo base `qwen2.5:7b-instruct-q5_K_M` esta disponible en Ollama. La descarga debe ser visible y fallar de forma clara si no termina correctamente.
- Ejecutar la carga y el smoke test reales contra la API externa. Si esta no responde, completar la validacion estatica posible y registrar el bloqueo y el comando ejecutado en Developer Evidence.
- Actualizar este sprint solo como implementado; no marcarlo como verificado.

## Notes

- Fuente funcional: `docs/product/features/agents/FEAT-002-NPC-Game-Domain-Agent.md`.
- La futura integracion definira los eventos de entrada, mundo, minijuegos, pistas y despedida cuando backend disponga de sus esquemas.
- El nombre y la personalidad configurable de Nubi no forman parte de este sprint.
- Ollama no pertenece al `docker-compose.yml` de este aplicativo: es infraestructura local externa y no debe anadirse ni modificarse en este sprint.
- La instancia externa esta expuesta en `http://127.0.0.1:11434`; al preparar este cambio respondia a `/api/tags`, aunque el contenedor mostraba estado `unhealthy`.

## Developer Evidence

completed_tasks:
- Modelfile actualizado en `framework/agents/npc-game/Modelfile` con base `qwen2.5:7b-instruct-q5_K_M`, limites de acompanamiento infantil en juego y parametros conservadores.
- Script reproducible `framework/agents/npc-game/load-ollama.ps1` actualizado para consultar, descargar si falta y crear o actualizar `npc-game` mediante `/api/tags`, `/api/pull` y `/api/create` contra una instancia configurable de Ollama, sin usar el ejecutable local `ollama`.
- Smoke test manual `framework/agents/npc-game/smoke-test.ps1` actualizado para usar `/api/generate` con una situacion local de juego sin datos personales.
- Documentacion operativa actualizada en `framework/agents/npc-game/README.md` con los requisitos HTTP externos y el flujo de carga por API.

executed_checks:
- `Parser.ParseFile` de PowerShell ejecutado sobre `load-ollama.ps1` y `smoke-test.ps1`; comprobacion estatica superada (`PowerShell syntax valid`).
- `git diff --check -- docs/sprints/agents/001-npc-game-environment-foundation.md framework/agents/npc-game` ejecutado sin incidencias.
- `GET http://127.0.0.1:11434/api/tags` ejecutado correctamente; la API externa estaba disponible y expuso tres modelos antes de la carga.
- `./framework/agents/npc-game/load-ollama.ps1 -OllamaHost http://127.0.0.1:11434` ejecutado correctamente. Descargo `qwen2.5:7b-instruct-q5_K_M` mediante `/api/pull` y creo o actualizo `npc-game` mediante `/api/create`.
- `./framework/agents/npc-game/smoke-test.ps1 -OllamaHost http://127.0.0.1:11434` ejecutado correctamente. Respondio: `Que bonita nube! Podemos hacerla volar por el cielo.` La consola de PowerShell mostro los caracteres de apertura con una codificacion no representable, sin afectar a la respuesta ni a la comprobacion de ausencia de preguntas.

known_limitations:
- El Modelfile y el smoke test no definen un contrato definitivo de entrada o salida ni una integracion con eventos de juego.
- Las restricciones del prompt no sustituyen las validaciones futuras de la capa de integracion.
- No se requiere el ejecutable `ollama` en `PATH`; los scripts se comunican exclusivamente por HTTP con la infraestructura externa.

## Review

review_status: pending
