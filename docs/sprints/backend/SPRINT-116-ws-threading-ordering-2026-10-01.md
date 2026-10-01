# SPRINT-116: Rendimiento WebSocket de juego - Hilos y orden por sesión

## Objetivo
Sacar del hilo de lectura del WebSocket el trabajo pesado (BD, JSON, audio) manteniendo el orden de los mensajes de cada niño.

## Contexto
Con la carga real (máximo 2 conexiones de juego a la vez) el objetivo de este sprint no es el paralelismo masivo sino que un mensaje lento de un niño no bloquee sus propios latidos ni a su hermano. Por eso el modelo es deliberadamente sencillo.

Hoy `handleTextMessage` hace todo en el hilo del contenedor: un `game_ready` espera a la generación del audio, y un `auth` lento retrasa los latidos de la misma sesión. No existe `TaskExecutor` propio ni hilos virtuales (Java 21 disponible).

## Tareas
- [ ] Decidir y documentar el modelo: ejecutor de hilos virtuales (`Executors.newVirtualThreadPerTaskExecutor`) con una cola serial por `childSessionId` (los mensajes de una sesión se procesan en orden; sesiones distintas en paralelo)
- [ ] `heartbeat` y `world_heartbeat` siguen en línea mientras sean baratos tras SPRINT-119; si no, van por la misma cola
- [ ] `auth`, `game_start`, `game_ready`, `game_action`, `game_abandon`, `world_discovery_interacted` y `world_travel` pasan por la cola serial
- [ ] Un mensaje que falla no detiene la cola de la sesión
- [ ] Cerrar la cola de una sesión en `afterConnectionClosed` y descartar sus mensajes pendientes
- [ ] Límite de mensajes pendientes por sesión (protección frente a clientes defectuosos): al superarlo, cerrar con POLICY_VIOLATION
- [ ] **Audio no bloqueante (decisión del usuario):** en el camino del mensaje (`game_ready`, `game_action` correcta, `auth`, `world_travel`) se consulta solo la caché de audio. Si hay audio cacheado, se envía en ese mismo flujo; si no, el mensaje **no espera** al TTS: se responde ya y la síntesis de Chatterbox se lanza en paralelo (ya es `@Async` en `AudioAdapter`, verificar que ninguna ruta la espera, incluida la de `RoundAudioService` y `AvatarService`)
- [ ] **Decisión cerrada (usuario, 2026-10-01): el audio generado tarde nunca se envía.** Se guarda solo en caché para la siguiente vez. Motivo: un audio fuera de contexto confunde al niño (p. ej. oír "¡felicidades!" cuando ya está en el mapa del mundo). El pre-calentamiento de SPRINT-120 reduce los fallos de caché
- [ ] Test: una síntesis que termina después de que el mensaje haya respondido no produce ningún envío por el WebSocket
- [ ] Ningún camino de mensaje invoca síntesis TTS síncrona ni espera a un `Future` del TTS
- [ ] Envío del audio fuera de cualquier lock de partida (verificar en `GameOrchestratorService.processAction`)
- [ ] Eliminar la copia intermedia al construir el `BinaryMessage` de audio (escribir cabecera + audio en un único buffer dimensionado)
- [ ] Ejecutor propio con nombre de hilos y métricas de cola (SPRINT-114); no se usa el executor `@Async` por defecto
- [ ] Valorar `spring.threads.virtual.enabled` solo si el análisis demuestra que no afecta a Hikari (pool de 10): documentar la conclusión

### Tests
- [ ] Orden: 100 `game_action` enviadas rápido se procesan en orden
- [ ] Aislamiento: un mensaje lento de un niño no retrasa los de otro
- [ ] Un fallo en un mensaje no bloquea los siguientes
- [ ] Cierre de sesión descarta pendientes y libera la cola
- [ ] Con TTS caído o lento (simulado), `game_ready`/`game_action` responden en el tiempo normal; con audio en caché se envía en el mismo flujo
- [ ] Los mensajes emitidos y su orden respecto al audio (JSON primero, binario después) no cambian

### Pruebas manuales
- [ ] Tocar rápido varias respuestas seguidas: el resultado respeta el orden y no hay errores
- [ ] Con TTS lento (parar el servicio de audio): el juego sigue respondiendo en `game_action` y el latido no se retrasa
- [ ] Texto sin cachear: el mensaje responde de inmediato, no hay audio esa vez, no llega ningún audio después aunque la síntesis termine, y la segunda vez sí suena desde caché
- [ ] 2 sesiones simultáneas (caso real) y 6 como margen: comparar p95 con la línea base de SPRINT-114

## Criterios de Aceptación
1. Un mensaje lento de una sesión no retrasa a otras ni a los latidos de la propia sesión
2. El orden por sesión se conserva
3. Sin cambios en el contrato de mensajes
4. p95 de `game_ready` y `auth` no peor que la línea base, y de los latidos mejor o igual
5. Pool de Hikari sin saturación en el escenario de 6 sesiones

## Contratos y dependencias
- Sin cambios en `docs/contracts`.
- Atención: Hikari de 10 conexiones; más paralelismo no debe agotar el pool (medir `hikaricp_connections_pending`).

## Riesgos
- Reordenación de mensajes si la cola no es estricta.
- Contexto de seguridad/transacción perdido al cambiar de hilo: los servicios abren sus propias transacciones, pero hay que verificarlo.

## Dependencias
- SPRINT-114 y SPRINT-115 completados.

## Estimación
- **Tamaño:** L | **Riesgo:** Medio-Alto
