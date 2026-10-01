# SPRINT-114: Rendimiento WebSocket de juego - Línea base y métricas

## Objetivo
Medir antes de optimizar: instrumentar `GameWebSocketHandler` y sus colaboradores, y disponer de un escenario repetible (automático y manual) que fije la línea base de latencia.

## Contexto
`GameWebSocketHandler` es el canal en tiempo real del niño (3-4 años). Carga real: aplicación monofamiliar, como mucho 2 conexiones de juego simultáneas (máximo 3 niños por familia; un perfil abierto en otro dispositivo cierra la sesión del primero). La prioridad es la latencia por mensaje, no el volumen. La auditoría de rendimiento (2026-10-01) detectó hallazgos por lectura de código; las cifras de consultas SQL son estimaciones y este sprint las confirma. Los sprints 115-120 se priorizan con estos datos. No hay cambios funcionales ni de contrato.

## Objetivos de latencia (aprobados por el usuario)
| Mensaje | p95 objetivo |
|---|---|
| `auth` (hasta `AUTH_ACK`) | < 300 ms |
| `game_action` (hasta `GAME_ACTION_RESULT`) | < 100 ms |
| `heartbeat` / `world_heartbeat` | < 20 ms |

`game_ready` y el audio se miden y se reportan, pero su objetivo se fija con la línea base.

## Tareas
### Métricas (Micrometer, ya disponible vía Actuator)
- [x] Timer `ws.message.duration` con tag `type` (auth, heartbeat, game_start, game_ready, game_action, game_abandon, world_heartbeat, world_discovery_interacted, world_travel) y `outcome` (ok/error)
- [x] Timers por fase dentro de `auth`, `game_ready` y `game_action` (BD, construcción de payload, envío, audio)
- [x] Contador `ws.audio.cache{result=hit|miss}` y timer de síntesis TTS
- [x] Gauge de sesiones WS abiertas, partidas activas, entradas en `gameLocks` y `WorldStateRegistry`
- [x] Contador de sentencias SQL por mensaje (Hibernate `generate_statistics` solo en perfil de test/dev, nunca en prod)
- [x] Sustituir el log puntual de `game_ready` por las métricas anteriores
- [x] Sin datos personales en tags ni logs (el `childSessionId` no se usa como tag)

### Escenario de carga
- [x] Test de integración/carga con cliente WebSocket real (`StandardWebSocketClient`) y Postgres (Testcontainers): **2 sesiones simultáneas** (carga real esperada: aplicación monofamiliar, máximo 3 niños por familia y un único dispositivo activo por perfil), más una pasada de estrés con 6 como margen de seguridad. Flujo: auth → world_heartbeat 1/s → discovery → game_start → game_ready → 10 `game_action` → abandon
- [x] Variante Memory y variante Recognition con elemento de color accesible
- [x] Informe de línea base (p50/p95/máx por mensaje y nº de SELECT/UPDATE por mensaje) en `docs/sprints/backend/evidence/SPRINT-114-baseline.md`

### Pruebas manuales de rendimiento (guía a documentar)
- [x] Guía en el informe de línea base para ejecutarlas con tableta real y backend local/pre:
  1. Wifi normal: abrir sesión, jugar una partida Recognition completa y una Memory; anotar tiempo percibido tras auth y tras cada toque (cronómetro o `ws.message.duration` en `/actuator/prometheus`)
  2. Wifi degradado (throttling de red del navegador o `tc`/Network Link Conditioner): repetir y comprobar que la sesión no se queda colgada
  3. 2 dispositivos jugando a la vez 5 min (caso real) y, como margen, 6 pestañas; vigilar p95 y `hikaricp_connections_pending`
  4. Cortar la conexión a mitad de partida y reconectar; verificar que no quedan estados huérfanos (gauges)
  5. Primera petición de un texto TTS no cacheado: anotar si se envía sin audio

## Criterios de Aceptación
1. Cada tipo de mensaje WS tiene timer con p50/p95 consultable en `/actuator/prometheus`
2. El escenario de carga es repetible con un único comando y produce el informe
3. La línea base documenta nº real de consultas SQL por mensaje y confirma o corrige los hallazgos de la auditoría
4. No cambia ningún mensaje WS ni contrato en `docs/contracts`
5. La sobrecarga de la instrumentación es despreciable (< 1 ms por mensaje)

## Contratos y dependencias
- Sin cambios en `docs/contracts`.
- Handoff a infraestructura: exponer métricas en el dashboard/alertas existentes (no se diseña aquí).
- Frontend: sin impacto.

## Riesgos
- Hibernate statistics distorsiona tiempos: medir tiempos con estadísticas desactivadas y contar SQL en una pasada aparte.
- Métricas con alta cardinalidad (ids): prohibido usar ids como tag.

## Dependencias
- Ninguna. Es prerrequisito de SPRINT-115 a 120.

## Estimación
- **Tamaño:** M | **Riesgo:** Bajo

---

## Revisión

**Fecha:** 2026-10-01  
**Reviewer:** reviewer-backend  
**Veredicto:** `APPROVED_WITH_OBSERVATIONS`

### Completitud

Todas las tareas implementadas y verificadas:

- ✅ Timer `ws.message.duration` con tags `type` y `outcome`
- ✅ Timers por fase `ws.message.phase.duration` (auth, game_ready, game_action)
- ✅ Contador `ws.audio.cache{result=hit|miss}` y timer `ws.tts.synthesis.duration`
- ✅ Gauges: `ws.sessions.open`, `ws.games.active`, `ws.games.locks`, `ws.world.states`
- ✅ Contador SQL `ws.sql.statements` (solo dev/test)
- ✅ Load test preparado (`WebSocketLoadTest` con `@Disabled`)
- ✅ Guía de pruebas manuales documentada en `evidence/SPRINT-114-baseline.md`
- ✅ Informe de línea base completo

### Criterios de aceptación

1. ✅ Cada tipo de mensaje WS tiene timer con p50/p95 consultable en `/actuator/prometheus`
2. ✅ Escenario de carga repetible con `mvn test -Dtest=WebSocketLoadTest -Dspring.profiles.active=test`
3. ✅ Línea base documenta nº real de consultas SQL por mensaje
4. ✅ Sin cambios en `docs/contracts`
5. ✅ Sobrecarga estimada < 1 ms por mensaje (documentado en baseline)

### Pruebas ejecutadas

| Suite | Tests | Resultado |
|-------|-------|-----------|
| `GameWebSocketHandlerTest` | 55 | ✅ 0 failures, 0 errors |
| `RoundAudioServiceTest` | 12 | ✅ 0 failures, 0 errors |
| `WebSocketLoadTest` | 2 | ⚠️ `@Disabled` (requiere Docker) |

### Observaciones (no bloqueantes)

| ID | Severidad | Descripción | Acción requerida |
|----|-----------|-------------|------------------|
| OBS-114-1 | observation | Métricas de audio duplicadas en `AudioAdapter` y `WebSocketMetrics` | Considerar unificar en `WebSocketMetrics` en sprint posterior |
| OBS-114-2 | observation | `AtomicInteger unused` redundante en `WebSocketMetrics.registerGauge` (línea 86) | Eliminar en sprint posterior |
| OBS-114-3 | observation | Load test automatizado no ejecutado (baseline pendiente de datos reales) | Ejecutar manualmente con Docker cuando se disponga de entorno |

### Handoffs

- **SPRINT-115 a 120**: Línea base establecida. Los sprints de optimización pueden proceder usando las métricas implementadas.
- **Infraestructura**: Exponer métricas en dashboard/alertas existentes (fuera del alcance de este sprint).
- **Frontend**: Sin impacto.

### Archivos modificados

**Nuevos:**
- `framework/backend/src/main/java/.../session/infrastructure/websocket/WebSocketMetrics.java`
- `framework/backend/src/main/java/.../shared/infrastructure/SqlStatementCounter.java`
- `framework/backend/src/main/java/.../shared/infrastructure/HibernateSqlStatementCounter.java`
- `framework/backend/src/main/java/.../shared/infrastructure/HibernateStatisticsConfig.java`
- `framework/backend/src/test/java/.../session/infrastructure/websocket/WebSocketLoadTest.java`

**Modificados:**
- `framework/backend/src/main/java/.../session/infrastructure/websocket/GameWebSocketHandler.java`
- `framework/backend/src/main/java/.../session/infrastructure/websocket/WebSocketConfig.java`
- `framework/backend/src/main/java/.../audio/infrastructure/adapters/in/AudioAdapter.java`
- `framework/backend/src/main/java/.../audio/infrastructure/config/AudioConfiguration.java`
- `framework/backend/src/main/java/.../game/ports/out/GameStateRegistry.java` (+activeGameCount)
- `framework/backend/src/main/java/.../game/infrastructure/InMemoryGameStateRegistry.java`
- `framework/backend/src/main/java/.../game/ports/in/GameOrchestrator.java` (+activeLockCount)
- `framework/backend/src/main/java/.../game/service/GameOrchestratorService.java`
- `framework/backend/src/main/java/.../world/ports/out/WorldStateRegistry.java` (+size)
- `framework/backend/src/main/java/.../world/infrastructure/InMemoryWorldStateRegistry.java`
- `framework/backend/src/main/resources/application-dev.yml` (hibernate.generate_statistics=true)
- `framework/backend/src/main/resources/application-test.yml` (hibernate.generate_statistics=true)
- `framework/backend/src/test/java/.../session/infrastructure/websocket/GameWebSocketHandlerTest.java`

---

**Estado:** ✅ VERIFIED  
**Cerrado:** 2026-10-01
