# Sprint 002 - agents

## Goal

Dejar disponible un modelo `agent-educational-parent` en el Ollama externo de infraestructura local, con carga reproducible mediante su API y comprobacion manual basica, sin integrarlo todavia con el panel parental ni el backend.

## Status

status: active
started_at: 2026-07-21
closed_at:
blocked_by:
waiting_for:

## Tasks

- [x] Crear `framework/agents/agent-educational-parent/Modelfile` basado en `qwen3:14b`.
- [x] Configurar un comportamiento conversacional calmado para personas adultas, con limites claros sobre informacion infantil, salud, psicologia, educacion profesional, seguridad y asuntos legales.
- [x] Crear un script reproducible que use la API HTTP de Ollama para comprobar el modelo base y crear o actualizar `agent-educational-parent`.
- [x] Crear un smoke test manual por API que compruebe una respuesta general para una persona adulta sin incluir datos personales ni datos reales de menores.
- [x] Documentar requisitos, carga y prueba contra el Ollama externo de infraestructura local.
- [x] Ejecutar las validaciones disponibles, incluyendo la carga y smoke test reales contra `http://127.0.0.1:11434`.
- [x] Registrar las tareas realizadas, comprobaciones ejecutadas y bloqueos de entorno en este sprint.

## Risks

- La latencia de `qwen3:14b` es mayor que la de un modelo pequeno; es aceptable para esta experiencia no interactiva en tiempo real, pero debe observarse antes de uso familiar real.
- El modelo puede dar respuestas demasiado concluyentes sobre el menor si la futura integracion no limita correctamente el contexto y la salida.
- Los datos de tracking, herramientas y comandos futuros pueden exponer informacion fuera de su finalidad si backend no autoriza, filtra y valida cada operacion.
- Las restricciones del modelo no sustituyen los controles de acceso, sanitizacion, consentimiento y validacion de backend.

## Dependencies

- Ollama de infraestructura externa, contenedor `ollama`, accesible mediante `http://127.0.0.1:11434`.
- Modelo base `qwen3:14b` disponible en dicha instancia.
- La integracion futura depende de los comandos, esquemas y herramientas definidos por backend para el panel parental y tracking.

## Agent Instruction

- Implementar exclusivamente el arranque del agente en `framework/agents/agent-educational-parent/`.
- Usar `qwen3:14b` como modelo base y la API HTTP de Ollama. No requerir el ejecutable `ollama` en el PATH del host.
- Usar por defecto `http://127.0.0.1:11434` y permitir indicar otro host compatible mediante parametro o variable de entorno.
- No modificar backend, frontend, TTS, Docker, contratos compartidos, ADR ni features funcionales.
- No crear endpoint, persistencia, historial, comandos, herramientas reales, MCP, acceso a tracking, datos de menores ni integracion con WebSocket.
- No establecer un contrato definitivo de entrada o salida: el smoke test solo comprueba disponibilidad local.
- Mantener el modelo limitado a personas adultas. Debe distinguir hechos proporcionados de explicaciones generales, evitar diagnosticos o evaluaciones y recomendar apoyo profesional ante temas clinicos, psicologicos, educativos profesionales, legales o de seguridad.
- No usar datos personales ni familiares reales en scripts, ejemplos o pruebas.
- Ejecutar carga y smoke test reales contra el Ollama externo. Si la API no responde, completar la validacion estatica posible y registrar el bloqueo en Developer Evidence.
- Actualizar este sprint solo como implementado; no marcarlo como verificado.

## Notes

- Nombre confirmado del modelo: `agent-educational-parent`.
- La latencia es asumible para este chatbot; el frontend resolvera su presentacion para personas adultas en una feature posterior.
- Backend sera responsable de parsear comandos, autorizar acciones, sanitizar contexto y proporcionar las futuras herramientas de consulta de tracking.
- El Ollama externo no pertenece al `docker-compose.yml` de este aplicativo y no debe anadirse ni modificarse en este sprint.
- Referencia de producto actual: `docs/product/decisions/ADR-003-education-framework-agent-adult.md`.

## Developer Evidence

completed_tasks:

- Modelfile, scripts HTTP de carga y smoke test, y README operativo creados en `framework/agents/agent-educational-parent/`.
- El agente queda limitado a personas adultas, con derivacion para consultas clinicas, psicologicas, educativas profesionales, legales o de seguridad.

executed_checks:

- Revision estatica: scripts sin dependencia del ejecutable `ollama`; usan `/api/tags`, `/api/pull`, `/api/create` y `/api/generate`.
- `2026-07-21`: Ollama disponible en `http://127.0.0.1:11434`; `qwen3:14b` disponible.
- `2026-07-21`: `load-ollama.ps1` creo o actualizo `agent-educational-parent` correctamente.
- `2026-07-21`: smoke test ejecutado correctamente tras desactivar el razonamiento visible de Qwen3 para obtener texto de respuesta.

known_limitations:

- No se ha creado integracion con backend, panel parental, datos, historial, herramientas, comandos, contratos ni WebSocket.
- La validacion real depende de que Ollama externo este disponible y pueda alojar `qwen3:14b`.
- El smoke test comprueba disponibilidad y una derivacion general; no sustituye la revision funcional, de seguridad ni la futura validacion de la integracion.

## Review

review_status: pending
