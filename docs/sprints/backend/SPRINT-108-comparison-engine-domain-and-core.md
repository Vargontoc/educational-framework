# SPRINT-108: Extender RecognitionEngine para Modo Comparación

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
- [ ] Añadir campo `comparisonMode` a `RecognitionState`
- [ ] Crear clase `ComparisonOption` con `elementId` y `scalePercent`
- [ ] Añadir campo `comparisonOptions` a `RecognitionState`
- [ ] Añadir getters/setters correspondientes

### Extensión de RecognitionEngine
- [ ] Modificar `buildOptions()` para detectar `comparisonMode`
- [ ] Implementar `buildOptionsForComparison()` con lógica de escalas por dificultad
- [ ] Modificar `getNextElement()` para incluir `comparisonOptions` si `comparisonMode=true`
- [ ] Asegurar que `processAction()` funcione correctamente en modo comparación (la lógica es la misma: verificar si el elemento seleccionado es el target)

### Seed de Elementos
- [ ] Crear seed `21-comparison-elements.json` con 10 objetos
- [ ] Crear topic "Comparación" con `recognitionType: "COMPARISON"`
- [ ] Crear actividades para COMPARISON en seed de actividades con `comparisonMode: true`

### Integración con GameOrchestratorService
- [ ] Modificar `resolveCandidates()` para soportar categoría COMPARISON
- [ ] Modificar `getEngineParams()` para incluir `comparisonMode: true` si la actividad es de comparación
- [ ] Asegurar que el flujo de buffer/flush funciona correctamente (ya funciona, no requiere cambios)

### Nubi Audio
- [ ] Reutilizar `RoundAudioService` de SPRINT-104
- [ ] Extraer texto de `resourceRefs["nubi-audio"]` del target element
- [ ] Generar audio y enviar vía `GAME_AVATAR_EVENT`

### Tests
- [ ] Test unitario: `buildOptionsForComparison()` genera opciones correctas para EASY (100%, 40%)
- [ ] Test unitario: `buildOptionsForComparison()` genera opciones correctas para MEDIUM (100%, 65%)
- [ ] Test unitario: `buildOptionsForComparison()` genera opciones correctas para HARD (100%, 75%, 50%)
- [ ] Test unitario: `getNextElement()` incluye `comparisonOptions` si `comparisonMode=true`
- [ ] Test unitario: `processAction()` funciona correctamente en modo comparación
- [ ] Test de integración: flujo completo con `GameOrchestratorService` en modo comparación

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
