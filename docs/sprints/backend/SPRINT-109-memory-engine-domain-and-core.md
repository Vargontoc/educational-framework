# SPRINT-109: MemoryEngine - Domain Model and Core Logic

## Objetivo
Implementar el motor de memoria (MemoryEngine) con la lógica básica de tablero y parejas, incluyendo integración con GameOrchestratorService. Este sprint NO incluye integración con tracking ni Nubi Audio (eso va en SPRINT-110).

## Contexto
MemoryEngine es un nuevo motor de minijuego que presenta al niño un tablero de cartas boca abajo. El niño debe encontrar parejas de cartas idénticas.

**Escalera de dificultad:**
- **EASY**: 2×2 (4 cartas, 2 parejas), 2s de visibilidad
- **MEDIUM**: 2×3 (6 cartas, 3 parejas), 1.5s de visibilidad
- **HARD**: 2×4 (8 cartas, 4 parejas), 1s de visibilidad

## Tareas

### Modelo de Dominio
- [ ] Crear clase `MemoryCard` con `cardId`, `elementId`, `faceUp`, `matched`, `row`, `column`
- [ ] Crear clase `MemoryState` con todos los campos necesarios (sin roundAttempts por ahora)

### MemoryEngine
- [ ] Crear clase `MemoryEngine` que implementa `GameEnginePort`
- [ ] Implementar `initGame()`: crear tablero según dificultad, distribuir cartas
- [ ] Implementar `processAction()`: procesar volteo de carta, verificar pareja
- [ ] Implementar `getNextElement()`: retornar estado del tablero
- [ ] Implementar `isGameComplete()`: verificar si todas las parejas están encontradas
- [ ] Implementar `buildSummary()`: calcular estrellas
- [ ] Implementar lógica de volteo automático tras tiempo de visibilidad

### Integración con GameOrchestratorService
- [ ] Registrar `MemoryEngine` en `engineInstances`
- [ ] Añadir caso `MEMORY` en `resolveEngineType()`
- [ ] Implementar lógica de resolución de candidatos para MEMORY
- [ ] Implementar lógica de construcción de `engineParams` para MEMORY

### Seed de Elementos
- [ ] Crear seed `22-memory-elements.json` con 10 objetos
- [ ] Crear topic "Memoria" con `recognitionType: "MEMORY"`
- [ ] Crear actividades para MEMORY en seed de actividades

### Tests
- [ ] Test: `initGame()` inicializa tablero correctamente según dificultad
- [ ] Test: EASY genera 2×2 con 2 parejas
- [ ] Test: MEDIUM genera 2×3 con 3 parejas
- [ ] Test: HARD genera 2×4 con 4 parejas
- [ ] Test: `processAction()` con primera carta marca `faceUp=true`
- [ ] Test: `processAction()` con segunda carta coincidente marca `matched=true`
- [ ] Test: `processAction()` con segunda carta no coincidente marca `waitingForFlipBack=true`
- [ ] Test: `isGameComplete()` retorna true cuando todas las parejas están `matched`
- [ ] Test de integración: flujo completo con `GameOrchestratorService`

## Criterios de Aceptación

1. MemoryEngine implementa correctamente `GameEnginePort`
2. EASY genera tablero 2×2 con 2 parejas y 2000ms de visibilidad
3. MEDIUM genera tablero 2×3 con 3 parejas y 1500ms de visibilidad
4. HARD genera tablero 2×4 con 4 parejas y 1000ms de visibilidad
5. Parejas coincidentes permanecen boca arriba
6. Parejas no coincidentes se voltean tras el tiempo de visibilidad
7. El juego termina cuando todas las parejas fueron encontradas
8. Los tests pasan

## Dependencias

- SPRINT-097 completado (infraestructura de similitud)
- FEAT-013 completado (interacción visual básica)
- ADR-030 aceptado

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (nuevo motor con estado de cartas, pero sin tracking)

## Siguiente Sprint

SPRINT-110 añadirá:
- Integración con tracking (MemoryAttemptContext, buffer, flush)
- Nubi Audio
- Tests adicionales de tracking
