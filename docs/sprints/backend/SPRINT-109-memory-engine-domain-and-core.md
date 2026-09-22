# SPRINT-109: MemoryEngine - Domain Model and Core Logic

**Estado:** implemented

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
- [x] Crear clase `MemoryCard` con `cardId`, `elementId`, `faceUp`, `matched`, `row`, `column`
- [x] Crear clase `MemoryState` con todos los campos necesarios (sin roundAttempts por ahora)

### MemoryEngine
- [x] Crear clase `MemoryEngine` que implementa `GameEnginePort`
- [x] Implementar `initGame()`: crear tablero según dificultad, distribuir cartas
- [x] Implementar `processAction()`: procesar volteo de carta, verificar pareja
- [x] Implementar `getNextElement()`: retornar estado del tablero
- [x] Implementar `isGameComplete()`: verificar si todas las parejas están encontradas
- [x] Implementar `buildSummary()`: calcular estrellas
- [x] Implementar lógica de volteo automático tras tiempo de visibilidad

### Integración con GameOrchestratorService
- [x] Registrar `MemoryEngine` en `engineInstances`
- [x] Añadir caso `MEMORY` en `resolveEngineType()`
- [x] Implementar lógica de resolución de candidatos para MEMORY
- [x] Implementar lógica de construcción de `engineParams` para MEMORY

### Seed de Elementos
- [x] Crear seed `22-memory-elements.json` con 10 objetos
- [x] Crear topic "Memoria" con `recognitionType: "MEMORY"`
- [x] Crear actividades para MEMORY en seed de actividades

### Tests
- [x] Test: `initGame()` inicializa tablero correctamente según dificultad
- [x] Test: EASY genera 2×2 con 2 parejas
- [x] Test: MEDIUM genera 2×3 con 3 parejas
- [x] Test: HARD genera 2×4 con 4 parejas
- [x] Test: `processAction()` con primera carta marca `faceUp=true`
- [x] Test: `processAction()` con segunda carta coincidente marca `matched=true`
- [x] Test: `processAction()` con segunda carta no coincidente marca `waitingForFlipBack=true`
- [x] Test: `isGameComplete()` retorna true cuando todas las parejas están `matched`
- [x] Test de integración: flujo completo con `GameOrchestratorService`

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

## Implementación completada (2026-09-21)

### Resumen técnico
- **Modelo** (`game.model.memory`): `MemoryCard` (`cardId`, `elementId`, `faceUp`, `matched`, `row`, `column`), `MemoryState` (tablero, `matchedPairs`, `waitingForFlipBack`, `flipBackCardIds`, `flipBackAt`, contadores; sin `roundAttempts`), `MemoryBoardConfig` + `MemoryDifficultyLadder` (ADR-030: EASY 2×2/2000 ms, MEDIUM 2×3/1500 ms, HARD 2×4/1000 ms), `MemoryContentMode` (HIGH_CONTRAST / STANDARD / SAME_CATEGORY) y `MemoryDefaults` (umbrales de estrellas).
- **`MemoryEngine`** (`GameEnginePort`, sin estado: todo vive en `GameState.enginePayload`, una instancia compartida):
  - `initGame`: reparte el tablero según `memoryParams` (`rows`, `columns`, `flipDelayMs`, `contentMode`) y baraja las cartas (ids `card-N`, con fila/columna). Contenido: EASY mezcla elementos de grupos distintos (alto contraste), MEDIUM al azar, HARD las 4 parejas de un mismo `similarityGroup`. Si el nivel pide más parejas de las que hay (o ningún grupo llega), degrada a selección aleatoria / tablero más pequeño en vez de fallar.
  - `processAction` (`{"cardId", "responseTimeMs"}`): primera carta → boca arriba; segunda coincidente → ambas `matched` y fijas boca arriba; no coincidente → `waitingForFlipBack=true`, ambas boca arriba hasta `flipBackAt`. Tocar una carta ya visible, ya emparejada o inexistente no cambia nada.
  - **Volteo automático sin timers en el servidor:** al llegar la siguiente acción, si la pareja fallida ya venció se voltea antes de leer el toque; si no ha vencido y se toca otra carta también se voltea (no se pierde ningún toque). `getNextElement` (puro) presenta la pareja ya volteada cuando vence.
  - `getNextElement`: `rows`, `columns`, `totalPairs`, `matchedPairs`, `flipDelayMs`, `waitingForFlipBack`, `elementIds` (distintos, para precarga) y `cards`; el `elementId` de una carta boca abajo se omite para no revelar el tablero. Devuelve `null` al completar.
  - `isGameComplete`: `matchedPairs >= totalPairs`. `buildSummary`: estrellas por intentos de pareja / nº de parejas (≤1,5 → 3; ≤2,5 → 2; si no 1).
  - Eventos en el `attemptContext` (`FIRST_FLIP`, `MATCH`, `MISMATCH`, `IGNORED`), porque `ActionResultType` solo tiene tres valores: primera carta, coincidencia e ignorado → `CORRECT`; no coincidencia → `INCORRECT`.
- **Orquestador:** `MemoryEngine` registrado en `engineInstances`; `startGame` resuelve los candidatos MEMORY (elementos activos de los topics de la actividad); `getEngineParams` construye `candidates`, `candidateMetadata` (con `similarityGroup`) y `memoryParams` a partir del `DifficultyCode`. `flushBufferedAttempts` no hace nada para motores que no son RECOGNITION (el payload no es un `RecognitionState`).
- **Seed:** `22-memory-elements.json` (10 objetos: 5 frutas y 5 vehículos, `image` = code, `similarityGroup`), topic "Memoria" (`MEMORY`, Matemáticas, 3-4 años), actividad "Memoria de Parejas" (`MEMORY`) con niveles EASY/MEDIUM/HARD. `RecognitionType.MEMORY` añadido (la columna es `VARCHAR(20)`: sin migración). `SeedService.loadRecognitionMemory`.

### Decisiones de detalle y desvíos
1. **Los `engineParams` del `DifficultyLevel` no se usan:** la escalera es la fija del ADR-030 (`MemoryDifficultyLadder`); los niveles de la actividad heredada "Memoria de Animales" (`MEMORY_GAME`, `pairs`/`timeLimit`) no encajan con ella.
2. **Actividad heredada "Memoria de Animales"** (`gameEngineType: "MEMORY_GAME"`): sigue sin ser jugable (motor inexistente, `EngineNotAvailableException` en `startGame`). No se ha tocado; se creó una actividad nueva "Memoria de Parejas".
3. **Candidatos:** por los topics de la propia actividad (no por tipo de reconocimiento), sin filtro de categorías de tracking, anti-repetición ni prioridad por dominio (no aplican a un tablero de parejas).
4. **Un `INCORRECT` en no coincidencia es solo un valor técnico** para el ciclo del orquestador; ADR-030 exige que el cliente no lo presente como fallo (sin sonido, sin vaivén, sin contador de fallos).
5. Test existente `readyGame_notDevProfile_throwsEngineNotAvailable` (usaba MEMORY como motor "no disponible") sustituido por `startGame_unknownEngineType_throwsEngineNotAvailable` (`MEMORY_GAME`), que comprueba el mismo contrato.

### Archivos
- **Nuevos:** `MemoryEngine`, `game/model/memory/*` (6 clases), `seeds/22-memory-elements.json`; tests `MemoryEngineTest` (31), `GameOrchestratorServiceMemoryTest` (9), `MemoryRecognitionSeedTest` (3).
- **Modificados:** `GameOrchestratorService`, `RecognitionType`, `SeedData`, `SeedService`, seeds `02-topics`, `04-activities`, `05-difficulty-levels`; `GameOrchestratorServiceTest` (1 test sustituido).

### Contratos afectados
Ninguno en este sprint. `GameWebSocketHandler.gameStateToPayload` solo serializa el estado de reconocimiento (`engine == RECOGNITION`): el payload del tablero de memoria (`getNextElement`) y el mensaje de acción `cardId` se definen al integrar el frontend / SPRINT-110.

### Migraciones
Ninguna. Los datos nuevos se cargan con el seeding; en una BD ya sembrada solo se añaden los que faltan.

### Pruebas
- `mvn -o test`: 1184 tests; los 103 que no pasan son los mismos preexistentes ajenos al sprint (14 clases: contexto de Spring —falta la tabla pgvector `content_generated`—, `ChildProfileServiceTest`, `WorldOrchestratorServiceTest.selectDestination_hostWithoutWorldWidth_defaultsTo2560`). Ningún fallo en memoria, orquestador ni seeds.
- Cubiertos: tablero por dificultad (celdas, parejas, visibilidad), cada elemento dos veces y todo boca abajo, contraste/categoría por nivel (30 semillas) y degradación, primera carta, coincidencia (fija boca arriba), no coincidencia (`waitingForFlipBack`), volteo lazy (vencido / no vencido / toque durante la espera), toques ignorados, contadores, `getNextElement` sin revelar cartas, fin de partida, estrellas y flujo completo por el orquestador con el motor real (sin registrar intentos de reconocimiento; sí resumen de sesión).

### Riesgos, deuda y handoffs
- **Tracking y Nubi Audio (SPRINT-110):** hoy no se registran intentos de memoria ni se genera audio de ronda; al completar sí se registra el resumen de sesión y se evalúan logros genéricos (`topicId` lo envía el cliente).
- **Frontend:** falta el payload del tablero en `GAME_READY`/`GAME_ACTION_RESULT`, el mensaje `{cardId}` y las imágenes de los 10 elementos de memoria.
- El cliente debe temporizar el volteo con `flipDelayMs` y no mostrar `INCORRECT` como fallo; si no espera y toca otra carta, el servidor voltea la pareja al instante.
- Las estrellas son solo recompensa por completar; no deben mostrarse como puntuación de errores.
