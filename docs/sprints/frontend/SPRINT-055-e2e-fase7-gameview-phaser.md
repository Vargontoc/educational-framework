# SPRINT-055 — E2E Fase 7: GameView y escenas Phaser

## Estado

- **Estado:** pending
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

### Tarea 55.1: `loading-scene.cy.ts`

**Criterios de aceptación:**
- Positivo: al entrar en GameView con un perfil habilitado, LoadingScene se muestra sin texto de porcentaje y navega a BaseStateScene al terminar la carga (verificado vía el hook de estado de Phaser de SPRINT-048, no leyendo píxeles).
- Negativo: si la carga de assets falla (`cy.intercept` con error en un recurso), no se navega a BaseStateScene con assets incompletos; se refleja un estado de error o reintento.

### Tarea 55.2: `base-state-scene.cy.ts`

**Criterios de aceptación:**
- Positivo: BaseStateScene se alcanza como estado visual de transición no interactivo (sin controles que sugieran interactividad).
- Negativo: `WorldMapScene` y `RecognitionGameScene` no son alcanzables desde el flujo actual (verificar que no hay ruta ni transición que lleve a ellas).

### Tarea 55.3: `websocket-sesion.cy.ts`

**Criterios de aceptación:**
- Positivo: durante GameView se mantiene la señal de actividad de sesión por WebSocket (verificable vía el estado expuesto, no inspeccionando frames crudos).
- Negativo: un evento de expulsión de sesión simulado (`cy.intercept`/mock del canal) saca al niño de GameView de forma controlada, no con un error crudo en pantalla.

### Tarea 55.4: `recuperacion-despedida.cy.ts`

**Criterios de aceptación:**
- Positivo: una pérdida de conexión WebSocket simulada que se recupera a tiempo continúa la experiencia sin mostrar la escena de despedida.
- Negativo: una pérdida de conexión que no se recupera muestra la escena de despedida amable; según preferencias vigentes (NPC y TTS activados), reproduce o no la voz de despedida (usando el stub de audio de SPRINT-048).

### Tarea 55.5: `preferencias-dinamicas.cy.ts`

**Criterios de aceptación:**
- Positivo: un evento de activación/desactivación de NPC o TTS recibido durante la sesión (simulado vía canal) se refleja en el estado de Phaser (hook de SPRINT-048) y afecta a la experiencia posterior (p. ej. FarewellScene).
- Negativo: un `GAME_ERROR` recuperable no interrumpe la experiencia (la escena activa no cambia); un error crítico sí aplica el flujo de pérdida de conexión de la Tarea 55.4.

### Tarea 55.6: Cierre de la deuda técnica de SPRINT-044

**Criterios de aceptación:**
- Los 4 flujos E2E listados como pendientes en la tarea 44.5 de `SPRINT-044` (perfil habilitado, perfil bloqueado, recarga de página en GameView, sin audio/NPC) quedan cubiertos por los specs de esta fase más el spec `perfil-bloqueado.cy.ts` de SPRINT-050.
- `SPRINT-044` se actualiza referenciando este sprint como resolución de su tarea 44.5 (sin reabrir ni modificar sus tareas 44.3/44.4, que son dependencias externas de contenido/producto ajenas a este roadmap).

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El hook de inspección de Phaser (SPRINT-048) no cubre todo el estado necesario para estas aserciones | MEDIA | Ampliar el hook en este sprint si hace falta, manteniendo la restricción de "no producción" ya decidida en la Fase 0. |
| R2 | Simular pérdida/recuperación de WebSocket de forma fiable en Cypress es propenso a flakiness | ALTA | Controlar la conexión vía `cy.intercept`/cierre programático del socket de test, no desconectando la red real del entorno Docker. |
| R3 | Esta fase es la que más tiempo de mantenimiento exige a futuro por ser la zona más cambiante (histórico de 045-047) | MEDIA | Mantener las aserciones sobre estado/eventos (registry, hook de Phaser), no sobre disposición visual exacta, para resistir mejor a rediseños. |

## Dependencias bloqueantes

- [ ] SPRINT-048 y SPRINT-050 completados.
- [ ] Hook de inspección de Phaser de la tarea 48.3 cubre escena activa y flags `npcEnabled`/`ttsEnabled` como mínimo.

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
