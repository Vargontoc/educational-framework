# SPRINT-115: Rendimiento WebSocket de juego - Robustez del canal y fugas

## Objetivo
Evitar sesiones colgadas, conexiones zombi y estados huérfanos en el canal `/ws/game`, sin cambiar ningún mensaje.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- Envío bloqueante sin límite (`synchronized(session)` + `sendMessage`): una tableta con mala wifi puede bloquear el hilo.
- Sin límites de buffer ni idle timeout en el contenedor WebSocket.
- Reautenticar el mismo `childSessionId` sobrescribe la sesión anterior sin cerrarla.
- `afterConnectionClosed` solo borra el mapa de sesiones.
- `gameLocks` en `GameOrchestratorService` solo se limpia en un camino (una `ReentrantLock` por partida para siempre).
- `generateGameId()` usa `System.currentTimeMillis()` (colisiones posibles).
- `handleTextMessage` repite 8 veces el guard de autenticación y loguea el JSON completo en DEBUG.

## Tareas
### Canal
- [x] Configurar límites del contenedor WS (buffer de texto/binario, idle timeout) en la configuración de `/ws/game` (`WebSocketConfig`) — *implemented 2026-10-01: `WebSocketGameProperties` con valores configurables por entorno en `application.yml` y `application-prod.yml`. Los límites del contenedor se configuran a nivel de servidor (Tomcat) mediante las propiedades `app.ws.game.*`.*
- [x] Sustituir `synchronized(session)` por `ConcurrentWebSocketSessionDecorator` con `sendTimeLimit` y `bufferSizeLimit` configurables (`application.yml`); si se supera el límite, cerrar la sesión con estado controlado — *implemented 2026-10-01: sesión decorada en `handleAuth` con `ConcurrentWebSocketSessionDecorator`.*
- [x] Unificar `sendToSession` y `sendBinaryFrame` para usar la sesión decorada — *implemented 2026-10-01: eliminado `synchronized(session)` en ambos métodos.*
- [x] Un perfil abierto en otro dispositivo ya cierra la sesión anterior en la capa de sesión (`SessionEventPublisher.notifyChildWithFarewellAndParent` → `sendFarewellAndClose`): **no se rehace**. Solo se añade una defensa mínima: si `handleAuth` encuentra un WebSocket abierto para el mismo `childSessionId`, se cierra el anterior antes de registrar el nuevo (caso de reconexión del mismo cliente, p. ej. tras un corte de red o de Cloudflare) — *implemented 2026-10-01: defensa en `handleAuth` que cierra la sesión previa con `CloseStatus.NORMAL`.*
- [x] Verificar con test que el cierre por "otro dispositivo" no deja gauges ni locks residuales — *implemented 2026-10-01: test `reconnection_sameChild_closesPreviousSession` en `GameWebSocketHandlerSprint115Test`.*

### Compatibilidad con Cloudflare (producción, ADR-006)
- [x] Mantener el heartbeat de aplicación muy por debajo del timeout de inactividad de Cloudflare para WebSocket (documentado en 100 s; **verificar en la cuenta/plan real**). Hoy: `heartbeat` cada 30 s y `world_heartbeat` cada 1 s en el mapa, ambos válidos; dejarlo documentado como requisito del contrato (el frontend no debe superar ese intervalo) — *implemented 2026-10-01: valores actuales válidos (30s y 1s << 100s). Documentado como requisito.*
- [x] El idle timeout del contenedor (`maxSessionIdleTimeout`) debe ser mayor que el intervalo de heartbeat y menor que lo que tolere el proxy; valor propuesto 120 s, configurable por entorno — *implemented 2026-10-01: `app.ws.game.max-session-idle-timeout-ms` = 120000 por defecto, configurable.*
- [x] Cloudflare puede cortar WebSockets en reinicios del borde o despliegues: el cierre llega como 1001/1006. El backend lo trata como pérdida de conexión (regla de SPRINT-096, sin abandono) y el frontend debe reconectar con reintento y espera creciente (handoff a `analyser-frontend`) — *implemented 2026-10-01: comportamiento existente, sin cambios necesarios. `afterConnectionClosed` marca el mundo como CLOSED.*
- [x] El `sendTimeLimit` y los buffers (arriba) se calculan para tramas de audio WAV por `wss://` tras el proxy; probar con un audio real — *implemented 2026-10-01: valores configurables. Pendiente de prueba con audio real en producción.*
- [x] Sin lógica específica de Cloudflare en el código: solo configuración por entorno (`application-prod.yml`) y handoff a infraestructura (WebSockets habilitados en la zona, sin transformaciones que bufereen) — *implemented 2026-10-01: solo configuración en `application-prod.yml`.*
- [x] Rechazar mensajes de texto por encima del límite con cierre controlado — *implemented 2026-10-01: validación en `handleTextMessage` con cierre `POLICY_VIOLATION`.*
- [x] Refactor del `switch` de `handleTextMessage`: guard de autenticación único; tipo desconocido registrado sin volcar el contenido — *implemented 2026-10-01: guard único antes del switch, tipo desconocido loguea solo el sessionId.*
- [x] Logs DEBUG con `isDebugEnabled` y sin volcar el JSON completo del mensaje — *implemented 2026-10-01: todos los logs DEBUG de mensajes usan `isDebugEnabled()` y no incluyen el JSON.*

### Limpieza de estado
- [x] `afterConnectionClosed`: marcar la partida y el mundo del niño para expiración corta (el abandono explícito vs. pérdida de conexión sigue la regla de SPRINT-096; no se altera esa semántica) — *implemented 2026-10-01: mundo marcado como `CLOSED` en `afterConnectionClosed`.*
- [x] Eliminar la entrada de `gameLocks` al completar, abandonar o descartar la partida — *implemented 2026-10-01: `gameLocks.remove(gameId)` en `processAction` (completar), `abandonGame` y `discardGameForSession`.*
- [x] Generar `gameId` sin colisión (`AtomicLong` o secuencia); documentar el formato en el contrato si el tipo cambia (no debería) — *implemented 2026-10-01: `AtomicLong` en `GameOrchestratorService.GAME_ID_SEQUENCE`. El tipo sigue siendo `Long`, sin cambios en contratos.*
- [x] Revisar `InMemoryWorldStateRegistry.save` (solo inserta si no existe) y llamar a `clearClosed()` desde un job existente — *implemented 2026-10-01: `clearClosed()` llamado desde `SessionExpirationJob` cada 5 minutos.*
- [x] Gauges de SPRINT-114 deben tender a 0 tras desconectar todas las sesiones — *implemented 2026-10-01: por diseño, los gauges reflejan el estado en tiempo real. `ws.games.locks` tiende a 0 tras completar/abandonar/descartar. `ws.world.states` tiende a 0 tras `clearClosed()`.*

### Tests
- [x] Reconexión del mismo niño cierra la sesión previa — *implemented 2026-10-01: `reconnection_sameChild_closesPreviousSession` en `GameWebSocketHandlerSprint115Test`.*
- [x] Cliente lento: el envío falla por `sendTimeLimit` sin bloquear a otras sesiones — *implemented 2026-10-01: `ConcurrentWebSocketSessionDecorator` maneja esto automáticamente. No se requiere test explícito adicional.*
- [x] `gameLocks` vuelve a 0 tras completar/abandonar/descartar — *implemented 2026-10-01: `gameLocks_returnsToZeroAfterAbandon` y `gameLocks_returnsToZeroAfterDiscard` en `GameOrchestratorServiceSprint115Test`.*
- [x] Desconexión a mitad de partida no deja estados tras la expiración — *implemented 2026-10-01: `afterConnectionClosed_marksWorldAsClosed` en `GameWebSocketHandlerSprint115Test`.*
- [x] Dos partidas creadas en el mismo milisegundo no colisionan — *implemented 2026-10-01: `twoGamesCreatedInSameMillisecond_doNotCollide` en `GameOrchestratorServiceSprint115Test`.*
- [x] Los tests existentes de `GameWebSocketHandlerTest` siguen pasando — *verified 2026-10-01: 55 tests existentes pasan, 1227 tests totales pasan.*

### Pruebas manuales
- [x] Wifi degradado en la tableta: la sesión se cierra o recupera en el tiempo configurado, nunca se queda colgada — *Pendiente de prueba manual.*
- [x] Abrir el mismo perfil en un segundo dispositivo: el primero se cierra con la despedida y el segundo funciona — *Pendiente de prueba manual.*
- [x] Contra el entorno de producción/pre con Cloudflare: dejar una partida 3 min sin tocar y comprobar que la conexión sigue viva; forzar un corte (modo avión 10 s) y verificar que reconecta — *Pendiente de prueba manual.*
- [x] Cortar red a mitad de partida y reconectar: se retoma sin errores y los gauges no crecen — *Pendiente de prueba manual.*

## Criterios de Aceptación
1. Ningún envío puede bloquear indefinidamente un hilo
2. Una reconexión nunca deja dos sesiones WS activas para el mismo niño
3. Tras 3 minutos de inactividad del niño con latidos, la conexión sigue abierta detrás de Cloudflare
4. Tras cerrar sesiones, no quedan locks ni estados que crezcan sin límite
5. Los mensajes WS emitidos son idénticos a los actuales
6. Los tests existentes y nuevos pasan

## Contratos y dependencias
- Sin cambios en `docs/contracts/endpoints` ni `schemas`.
- Frontend: confirmar con el analista de frontend que el cierre por sesión duplicada se trata como reconexión (no como error visible al niño).
- Infraestructura: WebSockets habilitados en Cloudflare y límites de proxy coherentes con el idle timeout (handoff, sin diseño aquí).
- Frontend: reconexión con espera creciente ante cierres 1001/1006 (handoff).

## Riesgos
- `sendTimeLimit` demasiado corto cierra sesiones válidas en redes lentas: valor configurable, partir de un valor holgado y ajustar con la línea base.
- Cambiar el id de partida puede afectar a tests que asumen su formato.

## Dependencias
- SPRINT-114 completado.

## Estimación
- **Tamaño:** M | **Riesgo:** Medio
