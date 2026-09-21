# SPRINT-110: MemoryEngine - Tracking Integration and Nubi Audio

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
- [ ] Crear clase `MemoryAttemptContext` con campos:
  - `engineType: MEMORY`
  - `memoryCategory: String`
  - `cardId1: String` (primera carta)
  - `cardId2: String` (segunda carta)
  - `elementId: String` (elemento de la pareja)
  - `isMatch: boolean`
  - `attemptNumber: int`
  - `responseTimeMs: long`
  - `isFirstTry: boolean`
- [ ] Crear clase `MemoryRoundAttemptRecord` (similar a `RoundAttemptRecord`)
- [ ] Añadir campo `roundAttempts: List<MemoryRoundAttemptRecord>` a `MemoryState`

### Integración con Tracking
- [ ] Modificar `MemoryEngine.processAction()` para construir `MemoryAttemptContext` en cada intento
- [ ] Registrar intentos en buffer (`MemoryState.roundAttempts`)
- [ ] Implementar flush de buffer al completar partida
- [ ] Integrar con `RegisterActivityAttemptUseCase` para persistir intentos
- [ ] Integrar con `GameSessionSummary` para registrar resumen de partida
- [ ] Calcular métricas: `totalAttempts`, `totalCorrectFirstTry`, `totalResponseTimeMs`
- [ ] Asegurar que el buffer se limpia al cerrar sesión
- [ ] Implementar consolidación solo al completar (regla transversal)

### Nubi Audio
- [ ] Reutilizar `RoundAudioService` de SPRINT-104
- [ ] Extraer texto de `resourceRefs["nubi-audio"]` del primer elemento
- [ ] Generar audio y enviar vía `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`

### Tests
- [ ] Test: `MemoryAttemptContext` se construye correctamente para cada intento
- [ ] Test: buffer de intentos se acumula correctamente
- [ ] Test: flush de buffer persiste intentos vía `RegisterActivityAttemptUseCase`
- [ ] Test: `GameSessionSummary` se registra correctamente al completar
- [ ] Test de integración: tracking registra intentos correctamente al completar partida

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
