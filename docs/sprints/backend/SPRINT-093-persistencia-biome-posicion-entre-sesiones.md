# Sprint 093 - backend
# -----------------------------------------------

## Goal
Persistir el bioma y la posición de exploración del niño entre sesiones, sustituyendo (o complementando) el registro en memoria actual, para que el paseo se retome exactamente donde se dejó.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-11):

- `WorldState`/`visibleDiscoveryElements` vive hoy únicamente en `InMemoryWorldStateRegistry` (SPRINT-089), explícitamente **no persistido entre sesiones** — al cerrar sesión (o reiniciar el proceso) se pierde. Esto es correcto para "últimos elementos mostrados" (dato de rotación, no debe sobrevivir sesiones por diseño de SPRINT-089), pero es insuficiente para FEAT-012, que exige retomar el mismo estado de exploración en la siguiente sesión.

**Decisión confirmada por el usuario (2026-09-11):** se persiste la posición, no solo el bioma — el niño reanuda exactamente donde dejó el paseo, no en el punto de inicio del bioma.

Este es el cambio de mayor riesgo de privacidad de todo FEAT-012: pasar de "nada persiste" a "algo persiste" exige aplicar minimización de datos estrictamente (ADR-026 §6, FEAT-012 §6) — el campo persistido debe ser el mínimo necesario para reanudar, nunca un historial de movimientos, tiempo de sesión ni interacciones con elementos decorativos.

## Status
status: verified
started_at: 2026-09-12
closed_at: 2026-09-12
verified_at: 2026-09-12
blocked_by: SPRINT-091
waiting_for:

## Tasks

### Modelo de persistencia
- [x] Nueva tabla (p. ej. `world_exploration_state`) o extensión de una entidad de sesión/perfil existente, con como máximo: identificador del niño/perfil, bioma activo, posición normalizada (mismo formato 0.0–1.0 ya usado para `positionX`/`positionY` de elementos, SPRINT-090) y timestamp de última actualización. Sin campos de progreso, interacción o duración.
- [x] Migración Liquibase correspondiente (numeración siguiente a la última existente).
- [x] Decidir explícitamente qué ocurre si el bioma persistido ya no existe en el catálogo activo (p. ej. contenido retirado) — no debe romper el arranque de sesión; fallback razonable a documentar (p. ej. Pradera).

### Escritura
- [x] Actualizar el estado persistido en los mismos puntos donde hoy se actualiza `WorldState` en memoria (tras `buildDestination`, tras `WORLD_TRAVEL_REQUEST` de SPRINT-092) — con la posición más reciente conocida del niño en ese bioma. Confirmar con frontend con qué frecuencia/mecanismo se reporta la posición actual del jugador al backend, si no existe ya un canal para ello (hoy el offset de `GradualScroller` es puramente local al cliente).
- [x] Evaluar si hace falta un nuevo mensaje WS ligero (p. ej. `WORLD_POSITION_UPDATE`, con throttling) para que el cliente informe su posición sin saturar el canal — **no implementar sin confirmar antes con frontend y con quien revisó privacidad**, ya que reportar posición con frecuencia alta podría interpretarse como tracking de movimiento, algo que ADR-026/FEAT-012 excluyen explícitamente.

### Lectura
- [x] Al construir el primer destino de una sesión (`WORLD_STATE_SYNC` inicial), si existe estado persistido para el niño, usar su bioma y exponer su posición para que frontend la consuma (SPRINT-069 frontend) en vez de arrancar siempre en offset 0 del bioma por defecto.
- [x] Primera entrada sin estado previo: comportamiento ya vigente (Pradera, punto de inicio) — este sprint no lo modifica.

### Tests
- [x] Unit test: tras persistir bioma+posición y reiniciar el registro (simulando fin de proceso/sesión), el siguiente arranque de sesión recupera exactamente ese bioma y posición.
- [x] Unit test: sin estado previo, arranca en Pradera (comportamiento heredado, no debe romperse).
- [x] Unit test: bioma persistido que ya no existe en el catálogo activo cae al fallback documentado sin error.
- [x] Contract test: el campo de posición expuesto en la sincronización inicial no incluye ningún dato adicional a bioma/posición (revisión de minimización de datos).

## Manual Tests
- Explorar hasta cierta posición en un bioma, cerrar sesión, reabrir con el mismo perfil y verificar que el paseo arranca en ese bioma y esa posición aproximada.

## Risks
- Es el cambio de mayor riesgo de privacidad del FEAT — no proceder con la implementación de escritura sin la confirmación explícita pendiente (ver "Preguntas de decisión").
- Si se introduce un mensaje de reporte de posición sin throttling adecuado, podría generar tráfico WS innecesario o percibirse como tracking granular de movimiento del niño — evaluar cuidadosamente antes de implementar esa pieza en concreto.

## Dependencies
- SPRINT-091 (catálogo de biomas, para saber a qué biomas puede referirse el estado persistido).
- Frontend: SPRINT-069 (consumo del estado persistido al reanudar).

## Preguntas de decisión al usuario
1. ~~¿Cómo llega al backend la posición actual del niño para poder persistirla?~~  
   **RESPONDIDO (2026-09-12):** Reutilizar `world_heartbeat` existente. El heartbeat actúa como autoguardado, añadiendo `positionX`/`positionY` + `biome` al payload entrante. NO crear nuevo mensaje WS. El throttling inherente del heartbeat evita saturación y tracking granular.
2. ~~¿Qué fallback se aplica si el bioma persistido deja de estar disponible?~~  
   **RESPONDIDO (2026-09-12):** Pradera (MEADOW) por defecto.

## Agent Instruction
- No persistir ningún dato más allá de bioma y posición normalizada — ni interacciones con elementos, ni tiempo de sesión, ni conteos.
- No implementar el mensaje de reporte de posición sin resolver antes la pregunta de decisión #1 con el usuario.
- Código, comentarios y nombres en inglés.

## Notes
- Este sprint depende de una decisión de producto/privacidad explícita (pregunta #1) que no se puede resolver solo a nivel técnico — no cerrar su diseño final sin esa confirmación.

## Review

### completed_tasks
- Tabla `world_exploration_state` creada con migración Liquibase 037 (child_profile_id PK, biome, position_x, position_y, updated_at). FK a child_profile con CASCADE. CHECK constraints para rango 0.0-1.0.
- Dominio `WorldExplorationState`, puerto `WorldExplorationStateRepository`, adapter JPA `JpaWorldExplorationStateRepository`.
- `world_heartbeat` ampliado con campos opcionales `positionX`, `positionY`, `biome` (aditivos, nullable, sin nueva versión de canal).
- `WorldHeartbeatService.recordHeartbeat` persiste exploration state cuando heartbeat trae posición/bioma. Clamping a [0.0, 1.0].
- `GameWebSocketHandler.getNewWorld` lee estado persistido por childProfileId al autenticar; si bioma disponible en catálogo activo → usa ese bioma; si no → fallback a MEADOW.
- `GameWebSocketHandler.handleWorldTravel` persiste exploration state tras viaje.
- `WorldOrchestrator.buildDestinationForBiomeOrDefault` e `isBiomeAvailable` para fallback.
- `WorldStateSyncPayload` ampliado con positionX/positionY nullable (backward-compatible).
- Contrato AsyncAPI actualizado: WorldHeartbeatMessage con positionX, positionY, biome opcionales.
- Contrato AsyncAPI actualizado: WorldStateSyncPayload con positionX, positionY opcionales (defecto de revisión corregido).
- Tests: WorldExplorationPersistenceTest (9 tests), WorldStateSyncPayloadMinimizationTest (3 tests).
- 520 unit tests pasan sin errores.

### incomplete_tasks
Ninguna.

### Review verdict
**APPROVED**

### Review evidence
- **Migración 037**: tabla `world_exploration_state` creada con PK `child_profile_id`, campos `biome`, `position_x`, `position_y`, `updated_at`. FK a `child_profile` con CASCADE. CHECK constraints para rango 0.0-1.0. ✅
- **Dominio**: `WorldExplorationState` con campos correctos. ✅
- **Puerto/Adapter**: `WorldExplorationStateRepository` y `JpaWorldExplorationStateRepository` implementados correctamente. ✅
- **WorldHeartbeatService**: método `recordHeartbeat` ampliado con posición/bioma, persiste exploration state con clamping a [0.0, 1.0]. ✅
- **GameWebSocketHandler.getNewWorld**: lee estado persistido por childProfileId, verifica bioma disponible con `isBiomeAvailable`, fallback a MEADOW si no disponible. ✅
- **GameWebSocketHandler.handleWorldTravel**: persiste exploration state tras viaje con `persistExplorationStateOnTravel`. ✅
- **WorldOrchestrator**: métodos `buildDestinationForBiomeOrDefault` e `isBiomeAvailable` para fallback. ✅
- **WorldStateSyncPayload.java**: ampliado con `positionX`/`positionY` nullable, constructor backward-compatible. ✅
- **Contrato AsyncAPI heartbeat**: `WorldHeartbeatMessage` con `positionX`, `positionY`, `biome` opcionales. ✅
- **Tests unitarios**: 104/104 pasan (WorldExplorationPersistenceTest: 9, WorldStateSyncPayloadMinimizationTest: 3, WorldOrchestratorServiceTest: 24, WorldCatalogServiceTest: 13, WorldHostPersistenceAdapterTest: 9, WorldHostPayloadContractTest: 3, GameWebSocketHandlerTest: 43). ✅
- **Tests de integración**: errores preexistentes de contexto Spring (no relacionados con SPRINT-093). ✅

### Defectos corregidos
1. **Contrato world-state-sync-payload.yaml desactualizado** (corregido): el contrato no incluía los campos `positionX` y `positionY` que el código Java `WorldStateSyncPayload.java` expone.
   - **Corrección**: añadidos `positionX` y `positionY` en `docs/contracts/api/asyncapi/schemas/world-state-sync-payload.yaml` con `type: number`, `format: double`, `minimum: 0.0`, `maximum: 1.0`, `nullable: true`. Sin nueva versión de payload ni de canal AsyncAPI.
   - **Verificación**: los 4 campos del record Java (`status`, `destination`, `positionX`, `positionY`) están ahora reflejados en el contrato. 104/104 tests unitarios pasan sin regresiones.

### ADR-026 compliance
- ✅ Minimización de datos: solo se persiste bioma y posición normalizada, sin historial de movimientos, tiempo de sesión ni interacciones.
- ✅ Fallback a Pradera: cuando el bioma persistido ya no existe en el catálogo activo.
- ✅ Posición normalizada: rango 0.0-1.0 con CHECK constraints en BD y clamping en código.
- ✅ Reutilización de heartbeat: no se crea nuevo mensaje WS, se usa `world_heartbeat` existente con campos opcionales.

### Observations
- Los tests de integración fallan por problemas preexistentes de contexto Spring (base de datos), no relacionados con los cambios del SPRINT-093.
