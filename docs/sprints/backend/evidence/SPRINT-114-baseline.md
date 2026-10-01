# SPRINT-114: Línea base de rendimiento WebSocket

## Resumen

Se ha instrumentado `GameWebSocketHandler` y sus colaboradores con métricas Micrometer para establecer la línea base de rendimiento antes de optimizar (SPRINT-115 a SPRINT-120).

## Métricas implementadas

### Timers de mensaje (`ws.message.duration`)
Tags: `type` (auth, heartbeat, game_start, game_ready, game_action, game_abandon, world_heartbeat, world_discovery_interacted, world_travel), `outcome` (ok/error).

### Timers por fase (`ws.message.phase.duration`)
Tags: `type` (auth, game_ready, game_action), `phase` (db, send, audio), `outcome` (ok/error).

### Contador de caché de audio (`ws.audio.cache`)
Tag: `result` (hit|miss).

### Timer de síntesis TTS (`ws.tts.synthesis.duration`)

### Gauges
- `ws.sessions.open{kind=ws}`: sesiones WS abiertas
- `ws.games.active{kind=game}`: partidas activas
- `ws.games.locks{kind=lock}`: locks activos en GameOrchestratorService
- `ws.world.states{kind=world}`: entradas en WorldStateRegistry

### Contador SQL (`ws.sql.statements`)
Tag: `type` (tipo de mensaje). Solo activo en perfiles dev/test (Hibernate `generate_statistics=true`). Nunca en prod.

## Objetivos de latencia (aprobados)

| Mensaje | p95 objetivo |
|---|---|
| `auth` (hasta AUTH_ACK) | < 300 ms |
| `game_action` (hasta GAME_ACTION_RESULT) | < 100 ms |
| `heartbeat` / `world_heartbeat` | < 20 ms |

## Escenario de carga automatizado

El test `WebSocketLoadTest` está preparado pero deshabilitado por defecto (requiere Docker y datos de contenido seedados). Para ejecutarlo:

```bash
mvn test -Dtest=WebSocketLoadTest -Dspring.profiles.active=test
```

Flujo por sesión: auth → world_heartbeat (1/s) → discovery → game_start → game_ready → 10× game_action → game_abandon

- **Carga normal**: 2 sesiones simultáneas (carga real esperada, aplicación monofamiliar)
- **Estrés**: 6 sesiones simultáneas (margen de seguridad)
- **Variantes**: Memory y Recognition con elemento de color accesible

## Guía de pruebas manuales de rendimiento

### 1. Wifi normal
1. Abrir sesión en tableta real con backend local/pre
2. Jugar una partida Recognition completa y una Memory
3. Anotar tiempo percibido tras auth y tras cada toque
4. Consultar `ws.message.duration` en `/actuator/prometheus`:
   ```
   curl http://localhost:8080/actuator/prometheus | grep ws_message_duration
   ```

### 2. Wifi degradado
1. Activar throttling de red (Network Link Conditioner en iOS, `tc` en Linux, o throttling del navegador)
2. Repetir el flujo anterior
3. Comprobar que la sesión no se queda colgada
4. Verificar que `ws.sessions.open` refleja correctamente las conexiones

### 3. Múltiples dispositivos
1. 2 dispositivos jugando a la vez durante 5 min (caso real)
2. Como margen: 6 pestañas del navegador
3. Vigilar p95 en `/actuator/prometheus`
4. Vigilar `hikaricp_connections_pending` para detectar saturación de pool

### 4. Reconexión tras corte
1. Cortar la conexión a mitad de partida
2. Reconectar
3. Verificar que no quedan estados huérfanos:
   - `ws.sessions.open` debe reflejar solo sesiones activas
   - `ws.games.active` no debe tener partidas fantasma
   - `ws.games.locks` debe estar vacío tras abandonar

### 5. Primera petición TTS no cacheada
1. Solicitar un texto TTS no cacheado previamente
2. Anotar si se envía sin audio (el audio llega asíncronamente)
3. Verificar `ws.audio.cache{result=miss}` incrementa
4. Segunda petición del mismo texto: verificar `ws.audio.cache{result=hit}`

## Consulta de métricas

Todas las métricas están disponibles en `/actuator/prometheus`:

```bash
# Timers de mensaje
curl -s http://localhost:8080/actuator/prometheus | grep "ws_message_duration"

# Timers por fase
curl -s http://localhost:8080/actuator/prometheus | grep "ws_message_phase_duration"

# Caché de audio
curl -s http://localhost:8080/actuator/prometheus | grep "ws_audio_cache"

# TTS
curl -s http://localhost:8080/actuator/prometheus | grep "ws_tts_synthesis"

# Gauges
curl -s http://localhost:8080/actuator/prometheus | grep "ws_sessions_open\|ws_games_active\|ws_games_locks\|ws_world_states"

# SQL por mensaje (solo dev/test)
curl -s http://localhost:8080/actuator/prometheus | grep "ws_sql_statements"
```

## Sobrecarga de instrumentación

La instrumentación usa `System.nanoTime()` y contadores Micrometer (ConcurrentHashMap para timers cacheados). La sobrecarga estimada es < 0.1 ms por mensaje, muy por debajo del objetivo de < 1 ms.

## Hallazgos de la auditoría confirmados/corregidos

Pendiente de completar con los datos reales del escenario de carga automatizado. Las consultas SQL por mensaje se registrarán con `ws.sql.statements` cuando se ejecute el test con perfil dev/test.

## Archivos modificados

### Nuevos
- `framework/backend/src/main/java/.../session/infrastructure/websocket/WebSocketMetrics.java`
- `framework/backend/src/main/java/.../shared/infrastructure/SqlStatementCounter.java`
- `framework/backend/src/main/java/.../shared/infrastructure/HibernateSqlStatementCounter.java`
- `framework/backend/src/main/java/.../shared/infrastructure/HibernateStatisticsConfig.java`
- `framework/backend/src/test/java/.../session/infrastructure/websocket/WebSocketLoadTest.java`

### Modificados
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

## Contratos afectados

Ninguno. No se ha modificado ningún mensaje WS ni contrato en `docs/contracts`.

## Riesgos y deuda

1. **Tests pre-existentes rotos**: Varias clases de test (GameOrchestratorService*Test, DistractorSelector*Test, etc.) tienen errores de compilación pre-existentes no relacionados con este sprint. Impiden ejecutar la suite completa.
2. **Load test deshabilitado**: Requiere Docker + datos seedados. La guía manual permite obtener la línea base sin automatización completa.
3. **SQL statistics aproximado**: El contador SQL usa estadísticas globales de Hibernate; en concurrencia alta puede mezclar consultas de diferentes sesiones. Aceptable para carga monofamiliar (2-3 conexiones).
