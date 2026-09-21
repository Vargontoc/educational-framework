# SPRINT-108: Extender RecognitionEngine para Modo Comparación

**Estado:** implemented

## Objetivo
Extender RecognitionEngine para soportar modo comparación (grande/pequeño), reutilizando toda la infraestructura existente de tracking, buffer, flush, anti-repetición, hint, etc.

## Contexto
En lugar de crear un nuevo motor ComparisonEngine, extendemos RecognitionEngine para soportar dos modos:
- **Modo Reconocimiento** (existente): el niño identifica el elemento correcto entre opciones diferentes
- **Modo Comparación** (nuevo): el niño identifica el elemento más grande entre opciones del mismo elemento con diferentes escalas

**Escalera de dificultad para modo comparación:**
- **EASY**: 2 opciones, proporciones 100% y 40%
- **MEDIUM**: 2 opciones, proporciones 100% y 65%
- **HARD**: 3 opciones, proporciones 100%, 75% y 50%

## Requisitos

### Extensión del Modelo de Dominio

**Añadir a RecognitionState:**
```java
public class RecognitionState {
    // ... campos existentes
    
    // Nuevos campos para modo comparación
    private boolean comparisonMode; // true si es modo comparación
    private List<ComparisonOption> comparisonOptions; // opciones con escalas (solo si comparisonMode=true)
}

public class ComparisonOption {
    private String elementId; // mismo elementId para todas las opciones
    private double scalePercent; // 100, 75, 65, 50, 40
}
```

**Nota:** No se crea un nuevo motor, solo se extiende RecognitionState con campos opcionales.

### Lógica de Modo Comparación

**En RecognitionEngine.buildOptions():**
```java
private List<String> buildOptions(RecognitionState state, String target) {
    if (state.isComparisonMode()) {
        return buildOptionsForComparison(state, target);
    }
    return buildOptionsForRecognition(state, target); // lógica existente
}

private List<String> buildOptionsForComparison(RecognitionState state, String target) {
    List<ComparisonOption> options = new ArrayList<>();
    
    if (state.getDifficultyLevel() == DifficultyLevel.EASY) {
        options.add(new ComparisonOption(target, 100));
        options.add(new ComparisonOption(target, 40));
    } else if (state.getDifficultyLevel() == DifficultyLevel.MEDIUM) {
        options.add(new ComparisonOption(target, 100));
        options.add(new ComparisonOption(target, 65));
    } else { // HARD
        options.add(new ComparisonOption(target, 100));
        options.add(new ComparisonOption(target, 75));
        options.add(new ComparisonOption(target, 50));
    }
    
    state.setComparisonOptions(options);
    return options.stream().map(o -> o.getElementId()).toList();
}
```

**En RecognitionEngine.getNextElement():**
```java
public String getNextElement(GameState gameState) {
    // ... lógica existente
    if (state.isComparisonMode()) {
        map.put("comparisonOptions", state.getComparisonOptions());
    }
    return OBJECT_MAPPER.writeValueAsString(map);
}
```

### Seed de Elementos para Comparación

Crear seed `21-comparison-elements.json` con 10 objetos reutilizando assets existentes:
```json
[
  {
    "code": "apple",
    "displayValue": "Manzana",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es la manzana más grande?\", \"image\": \"apple\"}",
    "similarityGroup": "fruit"
  },
  {
    "code": "car",
    "displayValue": "Coche",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es el coche más grande?\", \"image\": \"car\"}",
    "similarityGroup": "vehicle"
  },
  {
    "code": "house",
    "displayValue": "Casa",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es la casa más grande?\", \"image\": \"house\"}",
    "similarityGroup": "building"
  },
  {
    "code": "tree",
    "displayValue": "Árbol",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es el árbol más grande?\", \"image\": \"tree\"}",
    "similarityGroup": "plant"
  },
  {
    "code": "flower",
    "displayValue": "Flor",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es la flor más grande?\", \"image\": \"flower\"}",
    "similarityGroup": "plant"
  },
  {
    "code": "ball",
    "displayValue": "Pelota",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es la pelota más grande?\", \"image\": \"ball\"}",
    "similarityGroup": "toy"
  },
  {
    "code": "book",
    "displayValue": "Libro",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es el libro más grande?\", \"image\": \"book\"}",
    "similarityGroup": "object"
  },
  {
    "code": "cup",
    "displayValue": "Taza",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es la taza más grande?\", \"image\": \"cup\"}",
    "similarityGroup": "object"
  },
  {
    "code": "hat",
    "displayValue": "Sombrero",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es el sombrero más grande?\", \"image\": \"hat\"}",
    "similarityGroup": "clothing"
  },
  {
    "code": "shoe",
    "displayValue": "Zapato",
    "resourceRefs": "{\"nubi-audio\": \"¿Cuál es el zapato más grande?\", \"image\": \"shoe\"}",
    "similarityGroup": "clothing"
  }
]
```

**Nota:** Los assets (apple, car, house, etc.) ya existen en `recognition-animals` o `recognition-colors`. Se reutilizan.

### Configuración de Actividades

Crear actividades para modo comparación en seed de actividades:
```json
{
  "name": "Comparación de Tamaño - EASY",
  "engineType": "RECOGNITION",
  "recognitionCategory": "COMPARISON",
  "difficultyLevel": "EASY",
  "comparisonMode": true
}
```

### Nubi Audio

Reutilizar `RoundAudioService` de SPRINT-104:
- Consigna: "¿Cuál es el más grande?"
- Generar audio y enviar vía `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`

## Tareas

### Extensión del Modelo
- [x] Añadir campo `comparisonMode` a `RecognitionState`
- [x] Crear clase `ComparisonOption` con `elementId` y `scalePercent`
- [x] Añadir campo `comparisonOptions` a `RecognitionState`
- [x] Añadir getters/setters correspondientes

### Extensión de RecognitionEngine
- [x] Modificar `buildOptions()` para detectar `comparisonMode`
- [x] Implementar `buildOptionsForComparison()` con lógica de escalas por dificultad
- [x] Modificar `getNextElement()` para incluir `comparisonOptions` si `comparisonMode=true`
- [x] Asegurar que `processAction()` funcione correctamente en modo comparación (la lógica es la misma: verificar si el elemento seleccionado es el target)

### Seed de Elementos
- [x] Crear seed `21-comparison-elements.json` con 10 objetos
- [x] Crear topic "Comparación" con `recognitionType: "COMPARISON"`
- [x] Crear actividades para COMPARISON en seed de actividades con `comparisonMode: true`

### Integración con GameOrchestratorService
- [x] Modificar `resolveCandidates()` para soportar categoría COMPARISON
- [x] Modificar `getEngineParams()` para incluir `comparisonMode: true` si la actividad es de comparación
- [x] Asegurar que el flujo de buffer/flush funciona correctamente (ya funciona, no requiere cambios)

### Nubi Audio
- [x] Reutilizar `RoundAudioService` de SPRINT-104
- [x] Extraer texto de `resourceRefs["nubi-audio"]` del target element
- [x] Generar audio y enviar vía `GAME_AVATAR_EVENT`

### Tests
- [x] Test unitario: `buildOptionsForComparison()` genera opciones correctas para EASY (100%, 40%)
- [x] Test unitario: `buildOptionsForComparison()` genera opciones correctas para MEDIUM (100%, 65%)
- [x] Test unitario: `buildOptionsForComparison()` genera opciones correctas para HARD (100%, 75%, 50%)
- [x] Test unitario: `getNextElement()` incluye `comparisonOptions` si `comparisonMode=true`
- [x] Test unitario: `processAction()` funciona correctamente en modo comparación
- [x] Test de integración: flujo completo con `GameOrchestratorService` en modo comparación

## Criterios de Aceptación

1. RecognitionState soporta `comparisonMode` y `comparisonOptions`
2. EASY genera 2 opciones con proporciones 100% y 40%
3. MEDIUM genera 2 opciones con proporciones 100% y 65%
4. HARD genera 3 opciones con proporciones 100%, 75% y 50%
5. Todas las opciones en modo comparación usan el mismo elementId
6. `getNextElement()` incluye `comparisonOptions` en el payload
7. `processAction()` funciona correctamente en modo comparación
8. Nubi Audio se genera y envía correctamente
9. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Reutilización máxima**: no se crea un nuevo motor, solo se extiende RecognitionEngine
- **Proporciones**: son reglas de resultado visual, el frontend aplica las escalas con `setScale()`
- **Mismo sprite**: todas las opciones usan el mismo asset, solo cambia la escala
- **Lógica condicional**: se añade `if (comparisonMode)` en `buildOptions()` y `getNextElement()`
- **Audio**: se reutiliza la infraestructura de SPRINT-104 sin cambios

## Dependencias

- SPRINT-104 completado (Nubi Audio)
- SPRINT-097 completado (infraestructura de similitud)
- FEAT-013 completado (interacción visual básica)
- ADR-028 y ADR-029 aceptados

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (extensión de motor existente, no creación de nuevo motor)
- **Riesgo:** Bajo (lógica similar a reconocimiento, solo cambia la construcción de opciones)

## Frontend

Se requiere sprint específico de frontend para:
- Extender RecognitionGameScene para soportar modo comparación
- Renderizado de objetos con diferentes escalas
- Consigna visual "¿Cuál es el más grande?"

## Implementación completada (2026-09-21)

### Resumen técnico
- **Categoría COMPARISON** en los tres enums (`game.RecognitionCategory`, `content.RecognitionType`, `tracking.RecognitionCategory`). La columna `recognition_type` es `VARCHAR(20)`: no hace falta migración.
- **Modelo:** `ComparisonOption(elementId, scalePercent)` (record), y `RecognitionState` gana `comparisonMode`, `comparisonOptions` y `comparisonScales` (la escalera de la partida, interna: no se envía al cliente).
- **Escalera** (`ComparisonScaleLadder`, ADR-029): EASY [100, 40], MEDIUM [100, 65], HARD [100, 75, 50]. `RecognitionDifficultyService` la resuelve por dificultad para COMPARISON y fija `optionCount` = nº de tamaños; `RoundParameters` gana `comparisonScales` (nullable; constructores antiguos conservados).
- **`RecognitionEngine`:** `buildOptionsForState` deriva a `buildOptionsForComparison` en modo comparación: una opción por tamaño, todas con el `elementId` del target y en orden aleatorio. `getNextElement` incluye `comparisonOptions`. Sin escalera explícita usa la de EASY.
- **Orquestador:** `getEngineParams` añade `comparisonMode: true` para COMPARISON y `roundParameters.comparisonScales`. `resolveCandidates`, tracking, buffer/flush, anti-repetición, hint y audio de Nubi funcionan sin cambios (la categoría se deriva del `recognitionType` del topic de la actividad).
- **Payload al cliente** (`GameWebSocketHandler.gameStateToPayload`): `comparisonMode` siempre; `comparisonOptions` solo si el modo está activo.
- **Seed:** `21-comparison-elements.json` (10 objetos, `nubi-audio` "¿Cuál es X más grande?", `image` = code), topic "Comparación" (`COMPARISON`, categoría Matemáticas, 3-4 años), actividad "Comparación de Tamaño" (`RECOGNITION`) con sus 3 niveles EASY/MEDIUM/HARD. `SeedService.loadRecognitionComparison` la carga y calienta la caché de audio con el mismo texto que se pide en ejecución.

### Decisiones de detalle y desvíos
1. **Cómo se reconoce la respuesta (decisión clave, requiere handoff a frontend).** El sprint dice que `processAction` "funciona igual" (comparar `selectedOptionId` con el target), pero como todas las opciones comparten `elementId`, cualquier toque sería acierto. En modo comparación la respuesta es correcta **solo si el cliente envía `selectedScalePercent` igual al mayor tamaño de la ronda** (y el `selectedOptionId`, si viene, es el target). Sin `selectedScalePercent` cuenta como no acertada. Documentado en `game-client-message.yaml`. El pseudocódigo de SPRINT-083 (`ev.setAction(element.id, diff)`) debe ampliarse para enviar la escala de la opción pulsada.
2. **Dificultad → escalera:** el sprint usa `state.getDifficultyLevel() == DifficultyLevel.EASY`, que no existe en el estado. La dificultad llega por `RoundParameters` (resuelta por el orquestador desde el `DifficultyLevel` de la partida), así que la escalera viaja como `comparisonScales`.
3. **Actividades:** el modelo actual tiene una actividad con tres `DifficultyLevel`, no tres actividades. Se crea una actividad "Comparación de Tamaño" con sus tres niveles, y no los campos `recognitionCategory`/`comparisonMode` del JSON del sprint (no existen en `ActivitySeed`): el modo se deduce del topic de tipo COMPARISON.
4. **Assets:** el sprint afirma que apple, car, house, etc. ya existen en `recognition-animals`/`recognition-colors`. **No es así** (solo hay animales y colores); el backend solo guarda el `code`/`image`, pero el frontend necesita las imágenes (bloque `recognition-comparison` de SPRINT-083).
5. `optionIds` de una ronda de comparación contiene el mismo id repetido (una vez por opción), como pide el criterio de aceptación 5; el orden de `comparisonOptions` se baraja cada ronda.

### Archivos
- **Nuevos:** `ComparisonOption`, `ComparisonScaleLadder`, `seeds/21-comparison-elements.json`; tests `RecognitionEngineComparisonTest` (17), `GameOrchestratorServiceComparisonTest` (5), `ComparisonRecognitionSeedTest` (3).
- **Modificados:** `RecognitionState`, `RoundParameters`, `RecognitionDifficultyService`, `RecognitionEngine`, `GameOrchestratorService`, `GameWebSocketHandler`, `SeedData`, `SeedService`, los tres enums, seeds `02-topics`, `04-activities`, `05-difficulty-levels`; tests `RecognitionDifficultyServiceTest` (+3), `GameWebSocketHandlerTest` (+2), `RecognitionCategoryTest`.

### Contratos afectados (`docs/contracts`)
- `api/asyncapi/schemas/game-state-payload.yaml`: `COMPARISON` en `recognitionCategory`; `comparisonMode` y `comparisonOptions`.
- `schemas/round-ready-event.v1.yaml`: `comparisonMode` y `comparisonOptions`.
- `schemas/round-parameters.v1.yaml`: `comparisonScales` (opcional).
- `api/asyncapi/messages/game-client-message.yaml`: `selectedScalePercent` en la acción de comparación.
Cambios aditivos: los payloads sin los campos nuevos se interpretan como rondas de reconocimiento.

### Migraciones
Ninguna de esquema. Los datos nuevos (topic, actividad, niveles, 10 elementos) se cargan con el seeding normal; en una BD ya sembrada solo se añaden los que faltan (las claves ya cargadas se saltan).

### Pruebas
- `mvn -o test` (sin `EducationalFrameworkApplicationTests`): 1140 tests, 0 fallos en el ámbito del sprint. Ajusté `RecognitionCategoryTest` (5 -> 6 valores).
- Nuevos: escalera EASY/MEDIUM/HARD (2/2/3 opciones y tamaños), mismo `elementId` en todas las opciones, `getNextElement` con `comparisonOptions`, `processAction` (el mayor acierta y avanza; uno menor falla y mantiene la ronda; sin escala o con otro elemento no acierta; pista tras 2 fallos), partida completa por dificultad, flujo del orquestador con el motor real, payload del handler y seeds.
- El "test de integración con `GameOrchestratorService`" es `GameOrchestratorServiceComparisonTest` (motor y orquestador reales, repositorios mockeados; sin `@SpringBootTest`).
- **Fallos preexistentes ajenos al sprint** (los mismos de SPRINT-106/107): `ChildProfileServiceTest` (11), `WorldOrchestratorServiceTest` (1) y 101 tests que cargan el contexto de Spring (falta la tabla pgvector `content_generated`). No se ha podido arrancar la app.

### Riesgos, deuda y handoffs
- **Frontend (SPRINT-083):** enviar `selectedScalePercent` al pulsar una opción (sin él, ninguna respuesta de comparación cuenta como acierto); leer `comparisonMode`/`comparisonOptions`; crear los assets y el bloque `recognition-comparison`.
- Las opciones por defecto de la escalera no son configurables (constantes en `ComparisonScaleLadder`), a diferencia de la escalera de reconocimiento (`RecognitionProperties`).
- El `RecognitionAttemptContext` que se envía a tracking no registra la escala elegida (solo los ids, que aquí son idénticos); no se ha ampliado para no añadir señales de tracking no acordadas.
- La categoría COMPARISON no pasa por ningún filtro de desbloqueo (solo NUMBER lo tiene).
