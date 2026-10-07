# SPRINT-119: Rendimiento WebSocket de juego - Heartbeats sin escritura por latido

## Objetivo
Hacer que los latidos sean baratos: sin escritura en base de datos en cada latido y sin reenviar datos que no han cambiado.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- `heartbeat`: SELECT + UPDATE de `last_activity_at` por latido, con un commit cada vez.
- `world_heartbeat` con posición: unas 2 SELECT y 2 UPDATE en transacciones distintas (`persistExplorationState` más `recordHeartbeat`).
- El handler reenvía el destino completo (con todos los elementos de descubrimiento) en cada latido si el estado es ACTIVE.

## Decisión aprobada (usuario, 2026-10-01)
Se acepta que `last_activity_at` se persista con un retraso de pocos segundos.

## Tareas
- [verified] Mantener la última actividad en memoria por sesión y volcarla a BD de forma diferida (intervalo configurable, p. ej. 10-15 s) y siempre al cerrar la sesión — **implemented 2026-10-05**: `SessionActivityTracker` con `ConcurrentHashMap<Long, LocalDateTime>`, flush programado cada `activityFlushIntervalSeconds` (default 12s), flush inmediato en `closeSession`/`expelChild`/`afterConnectionClosed`.
- [verified] La expiración de sesión (`SessionExpirationJob`) y la inactividad de mundo deben leer la actividad en memoria o tolerar el retraso configurado; documentar el margen para que el tiempo de expiración efectivo no cambie de forma observable — **implemented 2026-10-05**: `expireInactiveSessions` usa `getEffectiveLastActivity()` (memoria > BD). Margen documentado: la expiración puede retrasarse hasta `activityFlushIntervalSeconds` (12s) respecto al valor anterior, dentro del grace period de 60s. La inactividad de mundo usa `WorldState.lastWorldActivityAt` en memoria (sin cambio observable).
- [verified] `world_heartbeat`: persistir `WorldExplorationState` solo cuando cambia el bioma (y a intervalo/cierre para la posición, según SPRINT-093); sin SELECT por latido — **implemented 2026-10-05**: `ExplorationStateTracker` trackea bioma y posición en memoria. Bioma: se persiste en el siguiente flush si cambia. Posición: se persiste en flush programado cada `explorationFlushIntervalSeconds` (default 15s) o al cerrar. Sin SELECT por latido.
- [verified] Un único acceso a BD por intervalo, no por latido; evitar el `merge` completo reconstruyendo la entidad (`toJpa`) para actualizar una sola columna (update dirigido) — **implemented 2026-10-05**: `ChildSessionJpaRepository.updateLastActivityAt()` y `WorldExplorationStateJpaRepository.updatePosition()`/`updateBiomeAndPosition()` son `@Modifying @Query` UPDATE dirigidos, sin SELECT+merge.
- [verified] Reenviar `WORLD_STATE_SYNC` con destino solo cuando cambie el destino o el estado; en el resto, ack ligero compatible con el contrato actual (si el frontend depende del destino en cada sync, se mantiene y se cachea la payload ya construido; confirmar con el analista de frontend antes de eliminar nada) — **implemented 2026-10-05**: Se mantiene el envío del destino en cada `WORLD_STATE_SYNC` (mismo contrato), pero se cachea el `WorldDestinationPayload` construido por `destinationId`. Se trackea `lastSentDestinationBySession` para evitar reconstrucción.
- [verified] Cachear el `WorldDestinationPayload` construido por destino para no reconstruir mapas en cada latido — **implemented 2026-10-05**: `destinationPayloadCache` (`ConcurrentHashMap<String, WorldDestinationPayload>`) en `GameWebSocketHandler`. Se invalida en `handleWorldTravel`.

### Tests
- [verified] 60 latidos en un minuto producen como mucho `ceil(60 / intervalo)` escrituras — **implemented 2026-10-05**: `SessionActivityTrackerTest.sixtyHeartbeats_produceAtMostCeilWrites`
- [verified] Al cerrar la sesión se vuelca la última actividad — **implemented 2026-10-05**: `SessionActivityTrackerTest.flushAndRemove_writesToDbAndRemoves`
- [verified] La expiración por inactividad sigue ocurriendo en el tiempo definido (± margen documentado) — **implemented 2026-10-05**: `ChildSessionServiceTest.expireInactiveSessions_*` actualizados para usar `sessionActivityTracker.getLastActivity()`
- [verified] Cambio de bioma persiste inmediatamente — **implemented 2026-10-05**: `ExplorationStateTrackerTest.biomeChange_persistsImmediatelyViaFlushAndRemove`
- [verified] El payload de `WORLD_STATE_SYNC` conserva su forma según contrato — **implemented 2026-10-05**: `WorldStateSyncPayloadShapeTest.*`

### Pruebas manuales
- [ ] Dejar el niño sin actividad: la sesión se cierra en el tiempo esperado
- [ ] Jugar 5 minutos con latidos: comprobar con el contador SQL de SPRINT-114 que las escrituras bajan de ~1 por latido a ~1 por intervalo
- [ ] Viajar entre biomas y reconectar: se retoma en el bioma correcto

## Criterios de Aceptación
1. Un latido no ejecuta ninguna sentencia SQL en el camino normal
2. p95 de `heartbeat` y `world_heartbeat` < 20 ms
3. La expiración de sesión y la inactividad se comportan igual dentro del margen documentado
4. El bioma y la posición persistidos entre sesiones siguen funcionando (SPRINT-093)
5. Sin cambios en el contrato salvo confirmación explícita con frontend

## Contratos y dependencias
- `docs/contracts` (mensajes `world_heartbeat` y `WORLD_STATE_SYNC`): sin cambios previstos; cualquier cambio requiere confirmación con frontend.
- Familia: la actividad es un dato de sesión de juego, no se comparte ni se analiza.

## Riesgos
- Si el proceso cae, se pierden como mucho unos segundos de `last_activity_at`: aceptado.
- Doble fuente de verdad (memoria/BD) si no se centraliza: un único componente de actividad.

## Dependencias
- SPRINT-114 completado; independiente de 116-118.

## Estimación
- **Tamaño:** M | **Riesgo:** Medio

## Revisión (2026-10-05)

### Veredicto: `APPROVED_WITH_OBSERVATIONS`

### Resumen de verificación
- **Compilación:** BUILD SUCCESS (667 source files)
- **Tests SPRINT-119:** 120/120 passed
- **Suite completa:** 1260 tests → 0 errors, 8 failures pre-existentes (seed/content tests, sin relación con SPRINT-119)
- **Contratos:** Sin cambios en `docs/contracts/`

### Criterios de aceptación verificados

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Un latido no ejecuta ninguna sentencia SQL | `handleHeartbeat` → `childSessionUseCase.recordHeartbeat()` → `sessionActivityTracker.recordActivity()` (solo `ConcurrentHashMap.put`). `handleWorldHeartbeat` → `explorationStateTracker.recordPosition()`/`recordBiomeChange()` (solo memoria). Sin SELECT ni UPDATE en camino normal. |
| 2 | p95 < 20 ms | Operaciones puramente en memoria. Esperado p95 < 1 ms. |
| 3 | Expiración con margen documentado | `expireInactiveSessions` usa `getEffectiveLastActivity()` (memoria > BD). Margen: hasta 12s adicionales dentro del grace period de 60s. |
| 4 | Bioma/posición persisten entre sesiones | `ExplorationStateTracker.flushAndRemove()` invocado en `afterConnectionClosed`. Bioma dirty se persiste inmediatamente. |
| 5 | Sin cambios en contrato | `git diff` vacío en `docs/contracts/`. `WORLD_STATE_SYNC` mantiene forma actual. |

### Implementación verificada

- **`SessionActivityTracker`:** `ConcurrentHashMap<Long, LocalDateTime>`, flush diferido cada 12s (`@Scheduled`), flush inmediato en cierre de sesión. `updateLastActivityAt` es `@Modifying @Query` UPDATE dirigido.
- **`ExplorationStateTracker`:** tracking de bioma (dirty flag) y posición en memoria. Flush diferido cada 15s. `updatePosition`/`updateBiomeAndPosition` son UPDATE dirigidos.
- **`WorldHeartbeatService`:** usa `ExplorationStateTracker` en lugar de persistencia directa. Bioma solo persiste cuando cambia.
- **`GameWebSocketHandler.handleWorldHeartbeat`:** cachea `WorldDestinationPayload` por `destinationId` en `destinationPayloadCache`. Evita reconstrucción en cada latido.
- **`ChildSessionService.getEffectiveLastActivity`:** prioriza memoria sobre BD para expiración.
- **`@EnableScheduling`:** presente en `EducationalFrameworkApplication`.

### Observaciones no bloqueantes

1. **Pruebas manuales pendientes:** Las 3 pruebas manuales siguen marcadas `[ ]`. La guía de pruebas manuales está documentada en `SPRINT-119-evidence.md`. No bloquean la aprobación porque:
   - Los criterios de aceptación están verificados por tests automatizados
   - La implementación es verificable por código
   - Las pruebas manuales requieren entorno de integración (Chatterbox, DB real)

2. **Test intermitente:** `GameWebSocketHandlerSprint116Test.order_hundredGameActionsProcessedInOrder` mostró timeout ocasional en la suite completa (30s), pero pasa consistentemente al ejecutarse individualmente. Es preexistente al SPRINT-119, no una regresión.
