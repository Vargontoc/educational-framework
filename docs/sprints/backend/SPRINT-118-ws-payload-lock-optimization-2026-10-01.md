# SPRINT-118: Rendimiento WebSocket de juego - Payload de estado y sección crítica

## Objetivo
Reducir el coste de construir el estado que se envía al niño en cada acción y acortar el tiempo que se mantiene el lock de la partida.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- `gameStateToPayload` deserializa `RecognitionState`/`MemoryState` desde JSON en cada llamada.
- `processAction` mantiene el lock de partida durante todo el trabajo y hace varias idas y vueltas JSON (`deserializeRecognitionState`, `bufferAttempt`).
- Al completar, `flushBufferedAttempts` hace N `register` secuenciales en transacciones separadas, con el lock tomado.
- `RoundAudioResult.audioData` (byte[]) vive dentro de `GameState` en el registro.
- Memory reenvía `elements` completos y se re-parsea el tablero en cada volteo.

## Tareas
- [x] Mantener el estado de engine tipado en memoria y serializar a `enginePayload` solo al persistir/consolidar (valorar cuándo se necesita realmente el JSON)
- [x] `gameStateToPayload`: construir a partir del estado ya deserializado; ningún `readValue` repetido por mensaje
- [x] Separar la parte estática de la dinámica del payload (elementos/recursos vs. estado de la ronda) usando las cachés de SPRINT-117
- [x] `processAction`: mover fuera del lock todo lo que no muta el estado (p. ej. generar el payload de respuesta, enviar)
- [x] `flushBufferedAttempts`: registrar los intentos en una única transacción por lotes, fuera del lock cuando sea seguro, preservando atomicidad y orden de logros/resumen
- [x] Mover `byte[] audioData` fuera de `GameState`: guardar `audioId`/texto y resolver el audio desde la caché de audio al enviarlo
- [x] Evaluar (sin cambiar aún el contrato) el tamaño del payload de Memory y documentar la propuesta de no reenviar `elements` en volteos; se decide con los datos medidos y con el analista de frontend

### Tests
- [x] Equivalencia de payloads antes/después (snapshot) en Recognition y Memory
- [x] Concurrencia: acciones simultáneas sobre la misma partida siguen serializadas y sin pérdida de estado
- [x] Finalización: los intentos se registran todos, en orden, y los logros/resumen son idénticos
- [x] Fallo en medio de la consolidación: no se pierde ni duplica ningún intento (comportamiento actual conservado o mejorado y documentado)
- [x] `GameState` ya no retiene audio tras enviarlo

### Pruebas manuales
- [ ] Memory 4x3: voltear cartas rápido y comprobar tiempo percibido y estado correcto
- [ ] Completar una partida Recognition: el resumen, los logros y el diario parental muestran lo mismo que antes
- [ ] Observar `ws.message.duration{type=game_action}` y memoria de heap durante 20 partidas seguidas

## Criterios de Aceptación
1. p95 de `game_action` < 100 ms en Recognition y Memory
2. No quedan parseos JSON repetidos del estado en el camino de una acción
3. El lock de partida solo cubre la mutación del estado
4. Los payloads y los datos de tracking son idénticos a los de antes
5. La memoria retenida por partida no incluye audio

## Contratos y dependencias
- Sin cambios en `docs/contracts` en este sprint. La reducción de `elements` en Memory, si compensa, se propone como sprint aparte con frontend.
- Tracking: la consolidación sigue la regla de SPRINT-095 (diferida) y SPRINT-096.

## Riesgos
- Cambiar la serialización del estado puede afectar a la recuperación tras reinicio (el registro es en memoria, por lo que el riesgo es bajo, pero se verifica).
- Mover trabajo fuera del lock puede introducir condiciones de carrera: tests de concurrencia obligatorios.

## Dependencias
- SPRINT-117 completado.

## Estimación
- **Tamaño:** L | **Riesgo:** Medio-Alto
