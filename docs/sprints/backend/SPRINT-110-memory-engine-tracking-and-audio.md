# SPRINT-110: MemoryEngine - Tracking Integration and Nubi Audio

**Estado:** implemented

## Objetivo
Añadir integración con tracking (registro de intentos, buffer, flush) y Nubi Audio al MemoryEngine implementado en SPRINT-109.

## Contexto
SPRINT-109 implementó el motor básico de memoria. Este sprint añade:
- Registro de intentos en buffer durante la partida
- Flush de buffer al completar partida
- Integración con `RegisterActivityAttemptUseCase`
- Nubi Audio para consigna

## Tareas

### Modelo de Tracking
- [x] Crear clase `MemoryAttemptContext` con campos:
  - `engineType: MEMORY`
  - `memoryCategory: String`
  - `cardId1: String` (primera carta)
  - `cardId2: String` (segunda carta)
  - `elementId: String` (elemento de la pareja)
  - `isMatch: boolean`
  - `attemptNumber: int`
  - `responseTimeMs: long`
  - `isFirstTry: boolean`
- [x] Crear clase `MemoryRoundAttemptRecord` (similar a `RoundAttemptRecord`)
- [x] Añadir campo `roundAttempts: List<MemoryRoundAttemptRecord>` a `MemoryState`

### Integración con Tracking
- [x] Modificar `MemoryEngine.processAction()` para construir `MemoryAttemptContext` en cada intento
- [x] Registrar intentos en buffer (`MemoryState.roundAttempts`)
- [x] Implementar flush de buffer al completar partida
- [x] Integrar con `RegisterActivityAttemptUseCase` para persistir intentos
- [x] Integrar con `GameSessionSummary` para registrar resumen de partida
- [x] Calcular métricas: `totalAttempts`, `totalCorrectFirstTry`, `totalResponseTimeMs`
- [x] Asegurar que el buffer se limpia al cerrar sesión
- [x] Implementar consolidación solo al completar (regla transversal)

### Nubi Audio
- [x] Reutilizar `RoundAudioService` de SPRINT-104
- [x] Extraer texto de `resourceRefs["nubi-audio"]` del primer elemento
- [x] Generar audio y enviar vía `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`

### Tests
- [x] Test: `MemoryAttemptContext` se construye correctamente para cada intento
- [x] Test: buffer de intentos se acumula correctamente
- [x] Test: flush de buffer persiste intentos vía `RegisterActivityAttemptUseCase`
- [x] Test: `GameSessionSummary` se registra correctamente al completar
- [x] Test de integración: tracking registra intentos correctamente al completar partida

## Criterios de Aceptación

1. Cada intento de pareja se registra en `MemoryAttemptContext` con métricas correctas
2. Buffer de intentos se acumula durante la partida
3. Flush de buffer persiste intentos vía `RegisterActivityAttemptUseCase` solo al completar
4. `GameSessionSummary` se registra correctamente al completar partida
5. Nubi Audio se genera y envía correctamente
6. Los tests pasan

## Dependencias

- SPRINT-109 completado (MemoryEngine básico)
- SPRINT-104 completado (Nubi Audio)

## Estimación

- **Tamaño:** S (Small)
- **Complejidad:** Media (integración con tracking existente)

## Implementación completada (2026-09-21)

### Resumen técnico
- **`MemoryAttemptContext`** (`game.model.memory`): `engineType` (MEMORY), `memoryCategory`, `event`, `cardId1`, `cardId2`, `elementId`, `match`, `attemptNumber`, `responseTimeMs`, `firstTry`. Se construye en **cada** acción y viaja como `attemptContext` (JSON, nombres de bean como `RecognitionAttemptContext`: `match`, `firstTry`). Solo las acciones que resuelven una pareja (MATCH / MISMATCH) son intentos que se guardan en el buffer.
- **`MemoryRoundAttemptRecord(elementId, result, responseTimeMs, attemptContext)`** y `MemoryState.roundAttempts` (buffer). Además `MemoryState` gana `totalCorrectFirstTry`, `mismatchedElementIds` y `memoryCategory` (el grupo temático común del tablero, `MIXED` si hay varios, `null` si no hay metadatos). Las métricas `totalAttempts` (`pairAttempts`), `totalCorrectFirstTry` y `totalResponseTimeMs` viven en el estado; `GameState.attempts/correctAttempts/incorrectAttempts` se mantienen para el resumen de sesión.
- **Motor:** `MemoryEngine.processAction` construye el contexto y añade al buffer cada pareja resuelta (CORRECT si coincide, INCORRECT si no). *Primer intento*: la coincidencia de un elemento que no había participado en ninguna pareja fallida anterior. En una no coincidencia no se atribuye ningún `elementId` (no hay un único elemento al que imputarla).
- **Flush (`GameOrchestratorService`):** al completar (y solo si no es repetición), `bufferedAttemptsOf` convierte el buffer de memoria a `RoundAttemptRecord`, añadiendo el `difficultyLevelId` de la partida y el `topicId` del elemento (o el del tablero para una pareja fallida), y los registra vía `RegisterActivityAttemptUseCase` con el mismo bucle que reconocimiento (fallos por intento tolerados). Luego logros de finalización y `GameSessionSummary` (ya existente). Si el cliente no envía `topicId`, para los logros se usa el topic del tablero.
- **Buffer y cierre:** nada se registra antes de completar. Abandono, descarte por evento de sistema y repetición no registran intentos; el estado (con su buffer) sale del registro al cerrar la partida. Las únicas llamadas a tracking del orquestador son el resumen de sesión al completar, el resumen de sesión ABANDONED (ya existente; en memoria va con contadores a 0, porque el motor mantiene contadores parciales en el estado y no deben consolidarse) y el flush de intentos, que solo ocurre al completar.
- **Nubi Audio:** `generateAndAttachRoundAudio` para MEMORY toma el `nubi-audio` del elemento de la primera carta vía `RoundAudioService` (mismo filtro de NPC/voz que reconocimiento) y lo deja en `roundAudioResult`; el handler existente (`attachRoundAudio` en `GAME_READY` → `sendRoundAudioIfPresent`) ya lo envía como `GAME_AVATAR_EVENT` `ROUND_PROMPT` + binario, sin cambios. El audio es una sola consigna por partida: en `processAction` de MEMORY se limpia `roundAudioResult` para que el handler no la reenvíe con cada acción. Es best-effort: sin audio o con fallo del TTS la partida se juega igual.
- **Seed:** los 10 elementos de `22-memory-elements.json` llevan `nubi-audio` = "Encuentra las parejas" y `SeedService.loadRecognitionMemory` calienta la caché de audio con ese texto.

### Decisiones de detalle y desvíos
1. **El motor no conoce topic ni dificultad**, así que el buffer del motor no usa `RoundAttemptRecord`: el orquestador completa esos dos campos al hacer el flush.
2. **Pareja fallida sin `elementId`:** se registra `INCORRECT` con `elementId = null` (el sprint define `elementId` como "elemento de la pareja"); así no penaliza el dominio de un elemento concreto.
3. **Registrar `INCORRECT`** en tracking es técnico (mismo ciclo que reconocimiento); no debe presentarse al niño como fallo (ADR-030) y puede intervenir en la adaptación de dificultad.
4. **Consigna:** una sola por partida, tomada del primer elemento (como pide el sprint); todos los elementos del seed tienen el mismo texto. En BDs ya sembradas antes de este sprint los elementos no tienen `nubi-audio`: no habrá audio hasta volver a sembrar.
5. `responseTimeMs` de cada intento se lee del JSON de la acción (`{"cardId", "responseTimeMs"}`), no del parámetro del mensaje WebSocket, como hace el motor de reconocimiento para sus métricas internas.
6. El sprint habla de un "`GAME_AVATAR_EVENT` con `eventType = ROUND_PROMPT`": ya lo genera `GameAvatarEvent.roundPrompt` desde SPRINT-104; no hizo falta tocar handler ni contratos.

### Archivos
- **Nuevos:** `MemoryAttemptContext`, `MemoryRoundAttemptRecord`.
- **Modificados:** `MemoryState`, `MemoryEngine`, `GameOrchestratorService`, `SeedService`, `seeds/22-memory-elements.json`; tests `MemoryEngineTest` (31 → 38), `GameOrchestratorServiceMemoryTest` (9 → 20), `MemoryRecognitionSeedTest`.

### Contratos afectados (`docs/contracts/api/asyncapi`)
- `messages/game-client-message.yaml`: acción MEMORY `{"cardId", "responseTimeMs"}` y qué toques se ignoran.
- `messages/game-action-response.yaml`: `attemptContext` documentado para RECOGNITION y MEMORY (eventos, campos, `resultType` técnico y registro solo al completar).
- `messages/game-avatar-event.yaml`: `ROUND_PROMPT` añadido al enum `eventType` (ya se enviaba desde SPRINT-104 y faltaba en el contrato) y cuándo se emite en cada motor.
- `schemas/game-state-payload.yaml`: aclarado que para `MEMORY` solo van los campos comunes (el tablero aún no forma parte del contrato) y que `attempts` no es un contador para el niño.
El `attemptContext` de memoria no incluye el `elementId` de cartas boca abajo ni de parejas fallidas.

### Migraciones
Ninguna.

### Pruebas
- `mvn -o test`: 1201 tests; los 103 que no pasan son los mismos preexistentes ajenos al sprint (14 clases: contexto de Spring, `ChildProfileServiceTest`, `WorldOrchestratorServiceTest`). Sin fallos en memoria, orquestador ni seeds.
- Cubiertos: contexto de cada evento (coincidencia, no coincidencia, primer volteo), buffer solo con parejas resueltas y en orden, `firstTry` y métricas, `memoryCategory`; en el orquestador: nada se registra antes de completar, flush de 3 intentos (1 fallo + 2 aciertos) con topic/dificultad/contexto correctos, resumen de sesión una vez, logros con el topic del tablero, logros desbloqueados devueltos, repetición sin registro, abandono (resumen sin progreso parcial) y descarte sin registro, audio de la consigna (con y sin `withRoundAudio`, no reenviado por acción, fallo del TTS tolerado).

### Riesgos, deuda y handoffs
- **Frontend:** reproducir el `ROUND_PROMPT` al recibir `GAME_READY`; enviar `responseTimeMs` dentro del JSON de la acción; no mostrar `INCORRECT` ni `match:false` como fallo.
- Los intentos INCORRECT de memoria pueden influir en la adaptación de dificultad del tracking existente; conviene validarlo con producto (ADR-030 pide lenguaje no evaluativo hacia el niño, no hacia el registro).
- Sigue pendiente el payload del tablero en `GAME_READY` / `GAME_ACTION_RESULT` y el mensaje `{cardId}` (frontend).
