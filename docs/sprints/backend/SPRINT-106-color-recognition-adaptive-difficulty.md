# SPRINT-106: Color Recognition - Adaptive Difficulty & Similarity Validation

**Estado:** implemented

## Objetivo
Implementar la lógica de dificultad adaptativa para el minijuego de reconocimiento de colores, incluyendo validación de similitud de colores según el perfil de visión del niño y manejo especial para perfiles ACHROMATOMALY/ACHROMATOPSIA.

## Contexto
El minijuego de colores tiene requisitos específicos:
- **EASY/MEDIUM**: 2-3 opciones con icono de referencia relacionado con el color
- **HARD**: 3 opciones sin icono de referencia
- Los distractores deben validarse usando `AdaptativeColorService.isTooSimilar()` para evitar colores muy similares según el `colorVisionMode` del perfil
- Perfiles con **ACHROMATOMALY** o **ACHROMATOPSIA**: no se renderiza el minijuego, pero sí el DiscoveryElement
- Cada `RecognitionElement` tiene:
  - `resourceRefs["nubi-audio"]`: texto para audio de Nubi
  - `resourceRefs["color"]`: código hexadecimal del color para validación de similitud

## Requisitos

### Dificultad Adaptativa

**EASY (2 opciones):**
- 1 target + 1 distractor
- Mostrar icono de referencia relacionado con el color (ej: manzana roja para "red")
- Validar que target y distractor no sean muy similares según `colorVisionMode`

**MEDIUM (3 opciones):**
- 1 target + 2 distractores
- Mostrar icono de referencia relacionado con el color
- Validar que todos los colores no sean muy similares entre sí según `colorVisionMode`

**HARD (3 opciones):**
- 1 target + 2 distractores
- **NO** mostrar icono de referencia
- Validar que todos los colores no sean muy similares entre sí según `colorVisionMode`

### Validación de Similitud

Usar `AdaptativeColorService.isTooSimilar(colorVisionMode, color1, color2)`:
- Threshold: Delta E <= 20.0 en espacio Lab
- Si dos colores son muy similares, buscar alternativas en el pool de candidatos
- Si no hay suficientes alternativas, relajar la restricción (fallback)

### Manejo de Perfiles ACHROMATOMALY/ACHROMATOPSIA

- Si el perfil tiene `colorVisionMode = ACHROMATOMALY` o `ACHROMATOPSIA`:
  - **NO** renderizar el minijuego de colores
  - **SÍ** renderizar el DiscoveryElement (el elemento interactivo en WorldMap)
  - El minijuego se considera "no disponible" para estos perfiles
  - Implementar en `GameOrchestratorService.startGame()` o `readyGame()`

### Nubi Audio

- Extraer texto de `resourceRefs["nubi-audio"]` del target element
- Generar audio usando `AudioAdapter.getAudio()`
- Enviar vía `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"` (reutiliza SPRINT-104)

### Estructura de Datos

**RecognitionElement para colores:**
```json
{
  "code": "color_red",
  "displayValue": "Red",
  "resourceRefs": "{\"nubi-audio\": \"¿Dónde está el color rojo?\", \"color\": \"#FF0000\"}",
  "accessibleColorId": 1,
  "similarityGroup": "warm"
}
```

- `code`: Referencia al bloque de assets (ej: "color_red")
- `resourceRefs["color"]`: Código hexadecimal para validación de similitud
- `resourceRefs["nubi-audio"]`: Texto para audio de Nubi
- `accessibleColorId`: ID para paleta de colores accesibles
- `similarityGroup`: Grupo de similitud (ej: "warm", "cool") - opcional

**Nota**: Los iconos son propios del frontend y se gestionan mediante la estructura de assets existente en `assets-manifest.json`.

## Tareas

### Modelo de Datos
- [x] Actualizar seed `20-recognition-elements-colors.json` para incluir:
  - `resourceRefs["color"]` con código hexadecimal
  - `resourceRefs["icon"]` con nombre del icono de referencia
  - `resourceRefs["nubi-audio"]` con texto para Nubi
- [x] Crear método utilitario para parsear `resourceRefs` y extraer campos específicos

### Servicio de Validación de Colores
- [x] Crear `ColorSimilarityValidator` en `game/service/`:
  - Método `validateDistractors(target, distractors, colorVisionMode)` → boolean
  - Método `filterValidDistractors(target, candidates, colorVisionMode, count)` → List<String>
  - Usar `AdaptativeColorService.isTooSimilar()` internamente
  - Extraer código de color de `resourceRefs["color"]`
- [x] Registrar como bean de Spring

### Integración con DistractorSelector
- [x] Modificar `DistractorSelector` para soportar categoría COLOR:
  - Inyectar `ColorSimilarityValidator`
  - Para COLOR: usar `filterValidDistractors()` en lugar de selección aleatoria simple
  - Validar que todos los distractores sean suficientemente diferentes del target y entre sí
- [x] Mantener fallback si no hay suficientes distractores válidos

### Manejo de Perfiles Achromatic
- [x] Modificar `GameOrchestratorService.readyGame()`:
  - Verificar si `colorVisionMode` es ACHROMATOMALY o ACHROMATOPSIA
  - Si es categoría COLOR y perfil es achromatic:
    - Marcar juego como "no disponible"
    - Enviar evento especial al frontend (ej: `GAME_UNAVAILABLE`)
    - No inicializar `RecognitionEngine`
- [x] Crear evento `GameUnavailableEvent` para notificar al frontend

### Integración con RecognitionEngine
- [x] Modificar `RecognitionEngine.initGame()` para aceptar parámetro de `showIcon`:
  - EASY/MEDIUM: `showIcon = true`
  - HARD: `showIcon = false`
- [x] Incluir `showIcon` en `RecognitionState`
- [x] Incluir `showIcon` en evento `GAME_READY` hacia frontend

### Nubi Audio para Colores
- [x] Reutilizar `RoundAudioService` de SPRINT-104:
  - Extraer texto de `resourceRefs["nubi-audio"]`
  - Generar audio y enviar vía `GAME_AVATAR_EVENT`
- [x] No requiere cambios adicionales (ya implementado)

### Tests
- [x] Test unitario: `validateDistractors()` con colores muy similares → false
- [x] Test unitario: `validateDistractors()` con colores suficientemente diferentes → true
- [x] Test unitario: `filterValidDistractors()` filtra colores similares según `colorVisionMode`
- [x] Test unitario: Perfil ACHROMATOPSIA + categoría COLOR → juego no disponible
- [x] Test unitario: Perfil NONE + categoría COLOR → juego disponible
- [x] Test unitario: EASY/MEDIUM incluyen `showIcon = true`
- [x] Test unitario: HARD incluye `showIcon = false`
- [x] Test de integración: flujo completo con validación de similitud

## Criterios de Aceptación

1. EASY/MEDIUM muestran icono de referencia, HARD no lo muestra
2. Los distractores son validados usando `AdaptativeColorService.isTooSimilar()`
3. Si dos colores son muy similares (Delta E <= 20), se buscan alternativas
4. Perfiles ACHROMATOMALY/ACHROMATOPSIA no pueden jugar minijuego de colores
5. El DiscoveryElement se renderiza correctamente incluso para perfiles achromatic
6. Nubi Audio se genera y envía correctamente para colores
7. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Delta E**: Umbral de 20.0 en espacio Lab es perceptualmente significativo
- **Fallback**: Si no hay suficientes distractores válidos, relajar restricción gradualmente
- **Performance**: Cachear resultados de `isTooSimilar()` para evitar cálculos repetidos
- **Accesibilidad**: Perfiles achromatic no juegan colores, pero sí ven el DiscoveryElement
- **Iconos**: Los iconos son opcionales y solo se muestran en EASY/MEDIUM

## Dependencias

- SPRINT-104 completado (Nubi Audio)
- `AdaptativeColorService` existente (family module)
- `ColorVisionMode` existente (family module)

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media-Alta (validación de similitud, manejo de perfiles especiales)
- **Riesgo:** Medio (lógica de validación puede ser compleja)

## Implementación completada (2026-09-20)

### Punto de partida
Al empezar, el árbol de trabajo ya contenía una implementación parcial **sin commitear** de este sprint (seed con `resourceRefs`, `ColorSimilarityValidator`, integración en `DistractorSelector`/`RecognitionEngine`, `showIcon` en `RoundParameters`/`RecognitionState`/handler). No compilaba (`RecognitionEngine` sin el import del validador), el validador no estaba registrado como bean y los tests existentes usaban el constructor antiguo del orquestador. Se ha completado y corregido sobre esa base en lugar de rehacerla.

### Resumen técnico
- **Seed** (`19-recognition-elements-colors.json`, no `20-` como dice el sprint): `resourceRefs` con `nubi-audio` (texto normalizado), `color` (hex) e `icon`. `SeedData`/`SeedService` lo cargan.
- **`RecognitionResourceRefs`** (nuevo, `game/service`): utilidad única para leer campos de `resourceRefs` (`nubiAudio`, `colorHex`, `icon`, `get`). El orquestador y `RoundAudioService` la usan; se elimina el parser duplicado.
- **`ColorSimilarityValidator`** (bean en `GameModuleConfiguration`): `validateDistractors` y `filterValidDistractors` sobre `AdaptativeColorService.isTooSimilar` (ΔE ≤ 20 en Lab). Añadido: caché de pares independiente del orden y por `colorVisionMode` (nota de rendimiento del sprint), y tolerancia a hex mal formados (antes lanzaban `NumberFormatException` y rompían la ronda). Fallback: si no hay suficientes candidatos válidos se relaja la restricción.
- **`DistractorSelector`**: para COLOR usa `filterValidDistractors` en todas las dificultades; el orquestador crea un `RecognitionEngine` por partida con el `colorVisionMode` del niño.
- **Perfiles acromáticos**: `readyGame()` lanza `GameUnavailableException` (razón `COLOR_VISION_ACHROMATIC`) si la categoría es COLOR y el perfil es ACHROMATOMALY/ACHROMATOPSIA (`ColorVisionMode.isAchromatic()`), antes de cualquier transición: no se inicializa el motor, no se genera audio y el estado se descarta **sin** registrar resumen de sesión ni "abandono" (el niño no llegó a jugar). El handler responde `GAME_UNAVAILABLE` (nuevo `SessionEventType`, payload `GameUnavailableEvent`: gameId, activityId, reason) en lugar de `GAME_READY`/`GAME_ERROR`. El DiscoveryElement no se toca: sigue visible.
- **`showIcon`**: `true` en EASY/MEDIUM, `false` en HARD (`RecognitionDifficultyService`), guardado en `RecognitionState`, propagado en `advanceRound` y enviado en el `recognitionState` de `GAME_READY`/`GAME_STARTED`/`GAME_ACTION_RESULT`.
- **Nubi audio**: sin cambios funcionales (reutiliza SPRINT-104).

### Bugs encontrados y corregidos (ya existían en el código heredado)
1. **La primera ronda ignoraba la categoría.** `RecognitionEngine.initGame` construía las opciones de la ronda 0 antes de asignar `recognitionCategory` al estado, así que el selector recibía `category = null` y se saltaba la validación de color… y también la de similitud de LETTER/NUMBER de sprints anteriores, solo en la primera ronda. Ahora la categoría se pasa a `buildInitialState`. Lo detectó el test de flujo completo (ofrecía rojo y casi-rojo a la vez).
2. **`RoundParameters` con 6 componentes rompía la compatibilidad.** Con Jackson 3 el `showIcon` ausente (primitivo) hacía fallar la deserialización y `parseRoundParameters` devolvía `null`, descartando toda la escalera de dificultad para cualquier `engineParams` sin ese campo (3 tests de `RecognitionEngineTest` en rojo). Ahora se asume `true` si falta.

### Archivos
- **Nuevos:** `game/service/RecognitionResourceRefs.java`, `game/exception/GameUnavailableException.java`, `game/model/enums/GameUnavailableReason.java`, `game/model/event/GameUnavailableEvent.java`, `docs/contracts/schemas/game-unavailable-event.v1.yaml`; tests: `ColorSimilarityValidatorTest`, `RecognitionResourceRefsTest`, `DistractorSelectorColorTest`, `RecognitionEngineColorTest`, `GameOrchestratorServiceColorTest`, `ColorRecognitionSeedTest`.
- **Modificados:** `RecognitionEngine`, `DistractorSelector`, `ColorSimilarityValidator`, `GameOrchestratorService`, `GameModuleConfiguration`, `RoundAudioService`, `RecognitionDifficultyService`, `RoundParameters`, `RecognitionState`, `CandidateMetadata`, `ColorVisionMode` (`isAchromatic()`), `SessionEventType`, `GameWebSocketHandler`, `SeedData`, `SeedService`, seed de colores; tests existentes (constructor del orquestador; `RecognitionDifficultyServiceTest`, `GameWebSocketHandlerTest` ampliados).

### Contratos afectados (`docs/contracts`)
- `api/asyncapi/messages/session-event.yaml` y `websocket.yaml`: evento `GAME_UNAVAILABLE`.
- `schemas/game-unavailable-event.v1.yaml`: nuevo.
- `schemas/round-parameters.v1.yaml`, `schemas/round-ready-event.v1.yaml`, `api/asyncapi/schemas/game-state-payload.yaml`: campo `showIcon` (requerido; los payloads anteriores se interpretan como `true`); `resourceRefs` documenta `color` e `icon`.

### Migraciones
Ninguna de esquema. La semilla de colores cambia (nuevo `resourceRefs`); se aplica con el seeding normal.

### Pruebas
- `mvn -o test` (suite completa, excluyendo `EducationalFrameworkApplicationTests`): 1073 tests, **0 fallos en el ámbito del sprint**. Suite de juego/sesión/seed: verde (`RecognitionEngineTest` 45, `GameWebSocketHandlerTest` 47, `SeedServiceTest` 4; 49 tests nuevos en total).
- Tests nuevos: validador (16: similares → false, distintos → true, filtrado por `colorVisionMode` con el caso rojo vs gris de igual luminancia solo similar en ACHROMATOPSIA, fallback, caché, hex inválido), selector COLOR (3), motor con el servicio de color real (6: `showIcon`, opciones nunca similares en 60 semillas, respeta el modo del niño), orquestador (10: ACHROMATOPSIA/ACHROMATOMALY → no disponible sin resumen ni eventos, NONE y PROTANOPIA → disponible, EASY/MEDIUM con icono, HARD sin icono, 25 partidas validadas para PROTANOPIA), handler (`GAME_UNAVAILABLE` sin `GAME_ERROR`, `showIcon` en el payload), semilla (3), utilidad de `resourceRefs` (6).
- El "test de integración: flujo completo con validación de similitud" se cubre con `RecognitionEngineColorTest` y `GameOrchestratorServiceColorTest` (motor + orquestador + `AdaptativeColorService` reales, repositorios y registro mockeados); no hay `@SpringBootTest` porque el contexto completo no arranca en este entorno (ver abajo).
- **Fallos preexistentes ajenos al sprint** (verificados en un checkout limpio de HEAD): `ChildProfileServiceTest` (11 errores, `avatarUseCase` nulo) y `WorldOrchestratorServiceTest.selectDestination_hostWithoutWorldWidth_defaultsTo2560` (1 fallo). Además 101 tests que cargan el contexto de Spring fallan porque falta la tabla pgvector `public.content_generated` (módulo de agentes / BD de test). Por eso **no he podido comprobar el arranque de la app** con el bean nuevo; se verificó por inspección: una única implementación de `ColorAdaptativeUseCase`, sin ciclos.

### Decisiones de detalle
1. La comprobación acromática va en `readyGame()` como pide la tarea, no en `startGame()`. Consecuencia: el cliente recibe antes `GAME_STARTED`; el juego se descarta en `game_ready`.
2. `showIcon` se calcula para todas las categorías (no solo COLOR); el contrato indica que solo aplica a COLOR.
3. La caché de similitud se acota a 4096 pares y se vacía al llenarse.

### Riesgos, deuda y handoffs
- **Frontend (handoff necesario):** el cliente no maneja aún `GAME_UNAVAILABLE` (debería volver al WorldMap) ni usa `showIcon`/`resourceRefs.icon` para pintar el icono de referencia. Los assets de iconos (`apple`, `leaf`, `drop`, `sun`, `carrot`, `flower`, `grape`, `moon`, `cloud`) tampoco están en `assets-manifest.json`.
- **Discrepancia sprint/ADR-028:** el sprint dice HARD = 3 opciones, pero la escalera configurada (`RecognitionProperties`) da 4 en HARD y `round-parameters.v1.yaml` documenta 3–4. No se ha tocado la escalera compartida.
- Sigue enviándose al cliente el texto `nubi-audio` dentro de `resourceRefs` (heredado de SPRINT-104; el frontend no lo muestra).
- Recomendable evitar el paso por `GAME_STARTED` bloqueando en `startGame()` si se quiere una experiencia más limpia para perfiles acromáticos.
