# SPRINT-053 — E2E Fase 5: canal STOMP y chatbot

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** MEDIA
- **Dependencias:** SPRINT-048 (arnés), SPRINT-050 (sesión de panel parental disponible). Cubre los flujos de SPRINT-034, SPRINT-035, SPRINT-036.
- **Impacto estimado:** Es el bloque técnicamente más exigente del roadmap (WebSocket STOMP real + streaming de un modelo Ollama real vía RAG). Requiere el stack E2E completo (`api` con Ollama local del host, ver `dev-agents/skills/e2e-cypress/SKILL.md`).

## Objetivo

Cubrir con E2E la conexión STOMP del panel parental, la carga y estado de la conversación del chatbot, y la interacción de chat (envío, streaming, mención `@`).

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 034 | Conexión STOMP al entrar/salir de `/panel` | `cypress/e2e/fase5-chatbot/conexion-stomp.cy.ts` |
| 035 | Carga de última conversación y estado inicial | `cypress/e2e/fase5-chatbot/estado-conversacion.cy.ts` |
| 036 | Envío de mensaje, streaming de respuesta, mención `@` | `cypress/e2e/fase5-chatbot/interaccion-chat.cy.ts` |

## Tareas del sprint

### Tarea 53.1: `conexion-stomp.cy.ts`

**Criterios de aceptación:**
- Positivo: al entrar en cualquier sección de `/panel` se establece la conexión STOMP (verificable vía el estado expuesto en `useWSStore`, no inspeccionando frames STOMP crudos).
- Negativo: al salir del árbol `/panel` la conexión se cierra; verificar que no queda una conexión STOMP abierta tras la navegación (posible fuga de recursos).

### Tarea 53.2: `estado-conversacion.cy.ts`

**Criterios de aceptación:**
- Positivo: con una conversación previa existente, entrar al chatbot carga y muestra su último estado.
- Negativo: sin conversación previa (familia recién creada), el chatbot muestra un estado inicial vacío comprensible, no un error por respuesta 404/vacía de `GET /api/v1/agents/conversations`.

### Tarea 53.3: `interaccion-chat.cy.ts`

**Criterios de aceptación:**
- Positivo: enviar un mensaje muestra el streaming de la respuesta en curso y el mensaje final una vez completado; el atajo `@` sugiere y permite mencionar un perfil infantil.
- Negativo: enviar un mensaje que excede el `maxLength` (4000) lo impide o lo trunca según el comportamiento definido, sin romper el estado del chat; una respuesta de streaming interrumpida a mitad (error de red simulado con `cy.intercept`) deja el chat en un estado recuperable, no en un buffer de streaming colgado indefinidamente.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El streaming real de Ollama es lento o no determinista en contenido, haciendo frágiles las aserciones sobre el texto exacto de respuesta | ALTA | Aserir sobre la mecánica (aparece streaming, termina, mensaje queda en el historial) y no sobre el contenido textual generado por el modelo. |
| R2 | El stack E2E depende de que Ollama local del host tenga el modelo de chat ya descargado, además del de embeddings | MEDIA | Documentar explícitamente el modelo de chat requerido (además de `mxbai-embed-large`) como prerrequisito de esta fase en `e2e-cypress` antes de ejecutarla. |
| R3 | Timeouts por defecto de Cypress son insuficientes para una respuesta completa de streaming | MEDIA | Aumentar el timeout específico de estas aserciones (`{ timeout: ... }`), no el global de la suite. |

## Dependencias bloqueantes

- [ ] SPRINT-048 y SPRINT-050 completados.
- [ ] Confirmar en `dev-agents/skills/e2e-cypress/SKILL.md` el modelo de chat exacto que debe estar descargado en el Ollama local para este sprint (además del de embeddings).

## Criterios de aceptación del sprint

1. Los 3 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh` con el Ollama local del host disponible.
2. Cada spec cubre al menos un caso positivo y un caso negativo.
3. Ninguna aserción depende del contenido textual exacto generado por el modelo.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/sprints/frontend/SPRINT-034-cliente-stomp-sesion-panel.md`
- ADR-010 (canal parental)
