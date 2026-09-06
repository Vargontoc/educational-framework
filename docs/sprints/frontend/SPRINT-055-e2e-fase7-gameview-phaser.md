# SPRINT-055 — E2E Fase 7: GameView y escenas Phaser

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-048 (arnés, incluye el hook de inspección de Phaser de la tarea 48.3), SPRINT-050 (perfil habilitado/bloqueado disponible). Cubre los flujos de SPRINT-041 (parcial, ya cubierto en Fase 2), SPRINT-042, SPRINT-043, SPRINT-044, SPRINT-045, SPRINT-046, SPRINT-047.
- **Impacto estimado:** Es la zona con más historial reciente de correcciones (045, 046 y 047 son sprints de arreglo de websocket/reconexión/sesión/despedida sobre lo construido en 042/043), lo que la convierte en la de mayor beneficio de tener un test de regresión, pese a ser la más compleja técnicamente (canvas Phaser + simulación de red).

## Objetivo

Cubrir con E2E la entrada a GameView, el placeholder de carga, el estado base no interactivo, la señal de actividad de sesión por WebSocket, la recuperación ante pérdida de conexión y la escena de despedida, y los cambios dinámicos de preferencias durante la sesión.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 042 | LoadingScene: placeholder visual, sin porcentaje | `cypress/e2e/fase7-gameview/loading-scene.cy.ts` |
| 043 | BaseStateScene: estado no interactivo, exclusión de WorldMap/RecognitionGame | `cypress/e2e/fase7-gameview/base-state-scene.cy.ts` |
| 045 | Señal de actividad de sesión por WebSocket, manejo de expulsión | `cypress/e2e/fase7-gameview/websocket-sesion.cy.ts` |
| 046 | Recuperación de conexión y escena de despedida | `cypress/e2e/fase7-gameview/recuperacion-despedida.cy.ts` |
| 047 | Cambios dinámicos de preferencias (audio/NPC) y clasificación de errores | `cypress/e2e/fase7-gameview/preferencias-dinamicas.cy.ts` |
| 044 | Flujos de validación integral (regresión de accesibilidad/responsividad ya cubierta en Fase 2; aquí solo los E2E completos pendientes de esa validación) | Repartido entre los specs anteriores, ver Tarea 55.6 |

## Tareas del sprint

### Tarea 55.1: `loading-scene.cy.ts` — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Positivo: al entrar en GameView con un perfil habilitado, LoadingScene se muestra sin texto de porcentaje y navega a BaseStateScene al terminar la carga (verificado vía `__NUBI_GAME_STATE__.activeScene`, sin leer píxeles).
- ✅ Negativo: si la apertura de sesión falla (`cy.intercept` con respuesta `success: false`), no se navega a BaseStateScene; se redirige a Home.

**Evidencia:** `cypress/e2e/fase7-gameview/loading-scene.cy.ts` — 2 tests (1 positivo, 1 negativo).

### Tarea 55.2: `base-state-scene.cy.ts` — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Positivo: BaseStateScene se alcanza como estado visual de transición no interactivo (sin botones ni controles interactivos).
- ✅ Negativo: `WorldMapScene` y `RecognitionGameScene` no están registradas en el flujo actual (verificado vía `__NUBI_GAME_STATE__.sceneKeys`).

**Evidencia:** `cypress/e2e/fase7-gameview/base-state-scene.cy.ts` — 2 tests (1 positivo, 1 negativo).

### Tarea 55.3: `websocket-sesion.cy.ts` — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Positivo: durante GameView se mantiene la señal de actividad de sesión por WebSocket (verificable vía `__NUBI_GAME_STATE__.wsReadyState === WebSocket.OPEN`).
- ✅ Negativo: un evento CHILD_EXPELLED inyectado vía `injectWsEvent` saca al niño de GameView de forma controlada (redirección a Home, sin error crudo).

**Evidencia:** `cypress/e2e/fase7-gameview/websocket-sesion.cy.ts` — 2 tests (1 positivo, 1 negativo).

### Tarea 55.4: `recuperacion-despedida.cy.ts` — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Positivo: una pérdida de conexión WebSocket simulada (vía `closeWs`) que se recupera a tiempo continúa la experiencia sin mostrar la escena de despedida (activeScene sigue siendo `base-state`).
- ✅ Negativo: una pérdida de conexión que no se recupera (SESSION_EXPIRED inyectado) muestra la escena de despedida (`farewell`) y vuelve a Home.
- ✅ Recarga de página en GameView recupera la sesión y continúa en BaseStateScene.

**Evidencia:** `cypress/e2e/fase7-gameview/recuperacion-despedida.cy.ts` — 3 tests (2 positivo, 1 recarga).

### Tarea 55.5: `preferencias-dinamicas.cy.ts` — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Positivo: eventos CHILD_TTS_ACTIVATED y CHILD_AGENT_DEACTIVATED inyectados durante la sesión se reflejan en el estado de Phaser (`ttsEnabled`, `npcEnabled`).
- ✅ Negativo: GAME_ERROR recuperable (TEMPORARY_FAILURE) no interrumpe la experiencia; GAME_ERROR crítico (SESSION_NOT_FOUND) aplica flujo de pérdida de conexión → farewell → Home.
- ✅ Flujo sin audio/NPC: GameView funciona sin requerir audio ni NPC.

**Evidencia:** `cypress/e2e/fase7-gameview/preferencias-dinamicas.cy.ts` — 5 tests (2 positivo, 2 negativo, 1 sin audio/NPC).

### Tarea 55.6: Cierre de la deuda técnica de SPRINT-044 — ✅ IMPLEMENTADA

**Criterios de aceptación:**
- ✅ Los 4 flujos E2E de tarea 44.5 quedan cubiertos:
  - Perfil habilitado → `loading-scene.cy.ts` (positivo) + `perfil-bloqueado.cy.ts` (Fase 2)
  - Perfil bloqueado → `perfil-bloqueado.cy.ts` (Fase 2, SPRINT-050)
  - Recarga de página en GameView → `recuperacion-despedida.cy.ts` (test de recarga)
  - Sin audio/NPC → `preferencias-dinamicas.cy.ts` (test "flujo sin audio/NPC")
- ✅ SPRINT-044 actualizado referenciando este sprint como resolución de tarea 44.5.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El hook de inspección de Phaser (SPRINT-048) no cubre todo el estado necesario para estas aserciones | MEDIA | Ampliar el hook en este sprint si hace falta, manteniendo la restricción de "no producción" ya decidida en la Fase 0. |
| R2 | Simular pérdida/recuperación de WebSocket de forma fiable en Cypress es propenso a flakiness | ALTA | Controlar la conexión vía `cy.intercept`/cierre programático del socket de test, no desconectando la red real del entorno Docker. |
| R3 | Esta fase es la que más tiempo de mantenimiento exige a futuro por ser la zona más cambiante (histórico de 045-047) | MEDIA | Mantener las aserciones sobre estado/eventos (registry, hook de Phaser), no sobre disposición visual exacta, para resistir mejor a rediseños. |

## Dependencias bloqueantes

- [x] SPRINT-048 y SPRINT-050 completados.
- [x] Hook de inspección de Phaser de la tarea 48.3 cubre escena activa y flags `npcEnabled`/`ttsEnabled` como mínimo.

## Criterios de aceptación del sprint

1. Los 5 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo.
3. Ninguna aserción depende de leer contenido del canvas a nivel de píxel.
4. La tarea 44.5 de SPRINT-044 queda referenciada como resuelta por este sprint.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/product/features/frontend/FEAT-010-Entrada-a-GameView-y-carga-inicial.md`
- `docs/sprints/frontend/SPRINT-044-integracion-accesibilidad-validacion.md` (tarea 44.5, deuda técnica que este sprint cierra)
- `docs/sprints/frontend/SPRINT-045-restauracion-websocket-gestion-sesion.md`
- `docs/sprints/frontend/SPRINT-046-recuperacion-conexion-despedida.md`
- `docs/sprints/frontend/SPRINT-047-cambios-dinamicos-preferencias-errores.md`

---

## Evidencia de implementación

- **Fecha:** 2026-09-06
- **Archivos creados:**
  - `cypress/e2e/fase7-gameview/loading-scene.cy.ts` — 2 tests
  - `cypress/e2e/fase7-gameview/base-state-scene.cy.ts` — 2 tests
  - `cypress/e2e/fase7-gameview/websocket-sesion.cy.ts` — 2 tests
  - `cypress/e2e/fase7-gameview/recuperacion-despedida.cy.ts` — 3 tests
  - `cypress/e2e/fase7-gameview/preferencias-dinamicas.cy.ts` — 5 tests
- **Archivos modificados:**
  - `src/views/GameView.vue` — Ampliado hook `__NUBI_GAME_STATE__` con `sceneKeys`, `wsReadyState`, `injectWsEvent`, `closeWs` (solo bajo Cypress). Corregido `ttsEnabled` para leer del registry key correcto (`ttsEnabled` en lugar de `voiceEnabled`).
- **Total tests Fase 7:** 5 specs, 14 tests (8 positivos, 6 negativos).
- **Typecheck:** `vue-tsc --noEmit` — 0 errores nuevos en archivos del sprint.
- **Contratos afectados:** Ninguno (tests E2E no modifican contratos).
- **Cobertura de tarea 44.5 (SPRINT-044):**
  - Perfil habilitado → `loading-scene.cy.ts` + `perfil-bloqueado.cy.ts`
  - Perfil bloqueado → `perfil-bloqueado.cy.ts` (Fase 2)
  - Recarga de página → `recuperacion-despedida.cy.ts`
  - Sin audio/NPC → `preferencias-dinamicas.cy.ts`
- **Decisiones de diseño:**
  - Hook ampliado con `injectWsEvent` para simular eventos WebSocket sin depender de la red real. Solo activo bajo Cypress.
  - `closeWs` para simular pérdida de conexión sin desconectar la red del entorno Docker.
  - Aserciones sobre estado/eventos (hook de Phaser), no sobre disposición visual del canvas.
  - `ttsEnabled` en el hook ahora lee del registry key `ttsEnabled` (coherente con LoadingScene que establece `ttsEnabled` en el registry).
