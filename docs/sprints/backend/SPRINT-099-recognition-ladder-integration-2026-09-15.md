# Sprint 099 - backend
# -----------------------------------------------

## Goal
Integrar `RecognitionDifficultyService` en el flujo del motor y el orquestador: aplicar la ladder de dificultad de extremo a extremo, exponer parámetros de ronda en `RecognitionState`, y resolver `colorVisionMode` para la categoría COLOR.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by: Sprint 097, Sprint 098
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### RecognitionState Extension
- [x] Añadir campo `guideChromEnabled` (boolean) a `RecognitionState`.
- [x] Añadir campo `touchEnableDelayMs` (int) a `RecognitionState`.
- [x] Añadir campo `nonChromaticKeyRequired` (boolean) a `RecognitionState`.
- [x] Añadir campo `distractorStrategy` (DistractorStrategy) a `RecognitionState`.
- [x] Añadir getters/setters correspondientes. También se añadió `optionCount` (Integer) y `candidateMetadata` (List&lt;CandidateMetadata&gt;), necesarios para que `advanceRound()` pueda reconstruir el resolver de distractores en rondas posteriores a la primera (ver Decisión 1).

### RecognitionEngine Integration
- [x] Modificar `RecognitionEngine.initGame()` para aceptar `RoundParameters` vía `engineParams` (JSON).
- [x] Almacenar `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired` y `distractorStrategy` en `RecognitionState` durante `initGame()`.
- [x] Modificar `RecognitionEngine.buildOptions()` para usar `optionCount` y `distractorStrategy` de `RecognitionState`. Vía un nuevo helper privado `buildOptionsForState(...)` que reconstruye el resolver desde `state.getCandidateMetadata()`.
- [x] Mantener `advanceRound()` consistente: al avanzar ronda, preservar los parámetros de dificultad. Único cambio: la llamada a `buildOptions(candidates, target)` pasa a ser `buildOptionsForState(state, candidates, target)`; el resto del método no se modificó.

### GameOrchestratorService Integration
- [x] Inyectar `RecognitionDifficultyService` en `GameOrchestratorService`.
- [x] Inyectar `ChildProfileUseCase` (puerto ya existente del módulo `family`, en vez de un `ChildProfileRepository` nuevo) para resolver `colorVisionMode`.
- [x] Modificar `readyGame()` (indirectamente, vía `getEngineParams()`) para:
    1. Resolver `DifficultyCode` desde `GameState.difficultyLevelId` (`difficultyLevelUseCase.getGameReadyDifficultyLevel(...)`, con fallback a EASY).
    2. Resolver `colorVisionMode` desde `ChildProfile` (`childProfileUseCase.getChild(...)`, con fallback a NONE).
    3. Llamar a `RecognitionDifficultyService.resolveRoundParameters()`.
    4. Incluir `RoundParameters` en `engineParams` JSON.
- [x] Modificar `getEngineParams()` para incluir `roundParameters` en el JSON (y `candidateMetadata`, necesario para que el motor pueda evaluar `SAME_CATEGORY`/`SIMILAR_OUTLINE` sin tocar el repositorio — ver Decisión 1).
- [x] **`resolveCandidates()` no se modificó** — ver Decisión 2 (desviación documentada): la resolución de `RoundParameters`/`colorVisionMode` vive enteramente en `getEngineParams()`/`readyGame()`, no en `resolveCandidates()` (que sigue ocupándose solo de construir la lista de IDs candidatos, sin relación con la ladder).

### WebSocket Event Extension
- [x] Modificar `getNextElement()` en `RecognitionEngine` para incluir `guideChromEnabled`, `touchEnableDelayMs` y `nonChromaticKeyRequired` en el JSON de salida.
- [x] Verificar que el evento WebSocket emitido por `GameWebSocketHandler` propaga estos campos al frontend. Ver Decisión 3: `getNextElement()` no está en el camino real de WebSocket hoy; los 3 campos también se añadieron directamente a `GameWebSocketHandler.gameStateToPayload()` (la vía que sí llega al cliente en `GAME_READY`/`GAME_STARTED`/`GAME_ABANDONED`).

### Tests
- [x] Test unitario: `initGame()` almacena `RoundParameters` en `RecognitionState`.
- [x] Test unitario: `getNextElement()` incluye `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`.
- [x] Test de integración: `readyGame()` resuelve `RoundParameters` correctamente (verificado para MEDIUM; EASY/HARD cubiertos indirectamente por los tests de fallback y de COLOR).
- [x] Test de integración: COLOR con `colorVisionMode=DEUTERANOPIA` — verificado que `RecognitionDifficultyService.resolveRoundParameters` se invoca con el `ColorVisionMode` correcto (la lógica de `nonChromaticKeyRequired` en sí ya estaba cubierta por los tests de SPRINT-097).
- [x] Test de integración: `colorVisionMode=NONE` — cubierto por el test de resolución MEDIUM.
- [x] Test de integración: `advanceRound()` preserva parámetros de dificultad entre rondas.
- [x] Tests adicionales no listados originalmente: fallback a EASY cuando `DifficultyLevel` no existe/no está listo; fallback a `ColorVisionMode.NONE` cuando el `ChildProfile` no existe; compatibilidad hacia atrás cuando `engineParams` no incluye `roundParameters` (comportamiento SPRINT-098 preservado).

## Manual Tests
- Iniciar backend y abrir un minijuego de reconocimiento.
- Verificar en logs que `RoundParameters` se resuelven correctamente.
- Verificar que el evento WebSocket `GAME_READY` incluye los nuevos campos dentro de `recognitionState` (nota: el evento no se llama `ROUND_READY` — ese nombre no existe en `GameWebSocketHandler`; corregido tras observación del revisor en SPRINT-099).
- Verificar que el frontend recibe `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`.

## Risks
- Romper compatibilidad con frontend existente si los nuevos campos no son opcionales: mitigar haciéndolos opcionales en el JSON.
- `colorVisionMode` no resuelto si el perfil no existe: mitigar con fallback a NONE.
- `DifficultyCode` no disponible si `difficultyLevelId` es nulo: mitigar con valor por defecto EASY.

## Dependencies
- Sprint 097 completado (modelo de `RoundParameters`).
- Sprint 098 completado (`DistractorSelector` integrado en `RecognitionEngine`).
- `ChildProfileRepository` existente (family module).
- `GameWebSocketHandler` existente (session module).

## Agent Instruction
- No modificar la lógica de `processAction()` ni `flushBufferedAttempts()` en este sprint.
- No modificar seeds de contenido en este sprint.
- Los nuevos campos en `RecognitionState` deben ser opcionales para no romper serialización existente.
- `colorVisionMode` se resuelve desde `ChildProfile`, no desde `LaunchContext`.

## Decisiones confirmadas

1. **`CandidateMetadata` (nuevo record) reemplaza `RecognitionElement` en las firmas de `DistractorSelector`/`RecognitionEngine.buildOptions()`.** `RecognitionEngine` no tiene ni puede tener acceso a un repositorio (solo ve `engineParams` en `initGame()` y su propio `RecognitionState` serializado después) — para que `advanceRound()` pueda evaluar `SAME_CATEGORY`/`SIMILAR_OUTLINE` en la ronda 2+, los metadatos de cada candidato (`topicId`, `similarityGroup`) tienen que viajar dentro de `engineParams` (clave `candidateMetadata`) y persistirse en `RecognitionState` (campo `candidateMetadata`), no resolverse en caliente. Esto resuelve también, como efecto colateral directo (no como trabajo aparte), la observación no bloqueante que el revisor dejó en SPRINT-098 sobre el import de `content.model.RecognitionElement` en `game.engine`/`game.service.DistractorSelector` — ya no existe ninguna referencia a ese tipo en ninguno de los dos ficheros.
2. **`resolveCandidates()` no se tocó**, a diferencia de lo que sugería la tarea "Modificar `resolveCandidates()` para pasar `colorVisionMode`...". `resolveCandidates()` solo construye la lista de IDs candidatos (llamada desde `startGame()`) y no tiene relación funcional con `RoundParameters`. La resolución completa (DifficultyCode → ColorVisionMode → RoundParameters) se implementó dentro de `getEngineParams()`/`readyGame()`, que es donde `GameState` ya tiene todo lo necesario (`difficultyLevelId`, `recognitionCategory` ya resuelto por `startGame()`, `childProfileId`) sin necesitar ningún lookup adicional de sesión.
3. **`getNextElement()` se modificó tal como pedía la tarea, pero la vía real al frontend es `GameWebSocketHandler.gameStateToPayload()`.** Se confirmó por lectura del código que `getNextElement()` no está enganchado al flujo en vivo de WebSocket (`GameWebSocketHandler` reconstruye el payload de reconocimiento manualmente desde `RecognitionState`, sin llamar nunca a `engine.getNextElement()`). Para que los 3 campos nuevos lleguen realmente al cliente, se añadieron también a `gameStateToPayload()`.
4. **Fallbacks defensivos documentados en Risks, implementados literalmente**: `DifficultyLevel` no encontrado/no listo (`ContentNotReadyException`) o `difficultyLevelId` nulo → `DifficultyCode.EASY`; `ChildProfile` no encontrado (`ResourceNotFoundException`) o `childProfileId` nulo → `ColorVisionMode.NONE`. Ninguno de los dos lanza excepción hacia arriba — `readyGame()` nunca falla por esta causa.
5. **Nueva dependencia real de servicio `game → family`** (`ChildProfileUseCase.getChild(...)`), más allá del precedente type-only de SPRINT-097 (`ColorVisionMode`). Es la primera vez que `GameOrchestratorService` invoca un caso de uso de `family`, pero sigue el mismo patrón hexagonal ya usado para `content`/`tracking` en esta misma clase.

## Notes
- Este sprint integra la ladder de extremo a extremo.
- Los parámetros de ronda se envían al frontend vía WebSocket, sin endpoint REST adicional (decisión confirmada).
- Sprint 100 completará el contenido de FORMAS y COLOR.
- Sprint 101 externalizará la configuración a `application.yml`.

## Verificación
- `mvn -o compile` / `mvn -o test-compile`: BUILD SUCCESS.
- `mvn -o test -Dtest=DistractorSelectorTest,RecognitionEngineTest,GameOrchestratorServiceTest,GameOrchestratorServiceSprint070Test,GameOrchestratorServiceMasteryPrioritizationTest,GameOrchestratorServiceCandidateFilteringTest,GameOrchestratorServiceTrackingIntegrationTest,GameWebSocketHandlerTest`: 145 tests ejecutados, 0 fallos.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**,es.vargontoc.educational.framework.session.**,es.vargontoc.educational.framework.content.**,es.vargontoc.educational.framework.family.**,es.vargontoc.educational.framework.tracking.**"`: 808 tests ejecutados, **0 fallos**, 96 errores. Todos los errores son preexistentes y ajenos a este sprint:
  - La mayoría son el mismo patrón documentado en sprints anteriores (`AuthControllerTest`, `ChildSessionControllerTest`, `DevContentControllerTest`, `TrackingDashboardControllerTest`, `TrackingRetentionPersistenceTest`, `TrackingSchemaApplicationTest`, `AdultProfileControllerTest`, `ChildProfileControllerTest`, `FamilyControllerTest`): `IllegalState: Failed to load ApplicationContext` por falta de Docker/Testcontainers en este entorno.
  - `ChildProfileServiceTest` (10 errores) falla con `NullPointerException: this.avatarUseCase is null` — confirmado como preexistente y no relacionado: `git status` confirma que ningún fichero de `family`/`avatar` fue tocado por este sprint.
- Confirmado que `processAction()` y `flushBufferedAttempts()` en `RecognitionEngine.java`/`GameOrchestratorService.java` no tienen diff de lógica (solo `advanceRound()` cambió, por diseño explícito del sprint).

## Review

### Developer implementation — Evidencias

#### Resumen técnico verificado

1. **`CandidateMetadata` reemplaza `RecognitionElement` en `DistractorSelector`/`RecognitionEngine`**: confirmado por lectura completa de ambos ficheros — cero referencias a `content.model.*` en `game.engine.RecognitionEngine` ni en `game.service.DistractorSelector`. Esto resuelve efectivamente la observación no bloqueante dejada en la revisión de SPRINT-098 sobre el import de `content.model.RecognitionElement` en el motor; la Decisión 1 lo documenta correctamente como efecto colateral, no como tarea aparte.
2. **`RecognitionState` extendido**: los 4 campos pedidos (`guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`, `distractorStrategy`) más los 2 adicionales documentados (`optionCount`, `candidateMetadata`) están presentes con getters/setters. `buildInitialState()` los puebla desde `RoundParameters` antes de invocar `buildOptionsForState(...)`, en el orden correcto.
3. **`RecognitionEngine.initGame()`/`advanceRound()`**: `initGame()` parsea `roundParameters` y `candidateMetadata` de `engineParams` vía tres nuevos métodos `parseX(...)` que fallan a `null`/`List.of()` de forma segura si faltan (JSON antiguo sin esas claves). `advanceRound()` cambia únicamente la llamada a `buildOptionsForState(state, candidates, target)` en vez de `buildOptions(candidates, target)`; el resto del método es idéntico — confirmado por `git diff` y por el test `advanceRound_preservesRoundParametersAcrossRounds`.
4. **`GameOrchestratorService.getEngineParams()`**: reescrito de concatenación manual de `StringBuilder` a un `LinkedHashMap` serializado con Jackson — mejora de robustez, no solo de alcance. `resolveRoundParameters()` devuelve `null` correctamente cuando el engine no es RECOGNITION o `recognitionCategory` es nulo (fallback SPRINT-098 sigue funcionando). `resolveDifficultyCode()`/`resolveColorVisionMode()` verificados contra las implementaciones reales de `DifficultyLevelService.getGameReadyDifficultyLevel(...)` (solo lanza `ContentNotReadyException`) y `ChildProfileService.getChild(...)` (solo lanza `ResourceNotFoundException`) — los `catch` son exhaustivos para las excepciones de negocio documentadas; ninguna de las dos rutas de fallback puede dejar escapar una excepción no capturada hacia `readyGame()`, tal como afirma la Decisión 4.
5. **`buildCandidateMetadata()`**: una sola consulta batch (`findAllById`) para todos los candidatos, sin N+1. Devuelve metadata en el mismo orden que `candidateIds`, con `topicId`/`similarityGroup` a `null` si el elemento no se resuelve — consistente con el manejo de "unresolvable" ya probado en `DistractorSelectorTest` (SPRINT-098).
6. **`GameWebSocketHandler.gameStateToPayload()`**: los 3 campos se añaden correctamente a la vía real que llega al cliente (`GAME_READY`/`GAME_STARTED`/`GAME_ABANDONED`), confirmado por el test actualizado que verifica valores exactos en el payload deserializado.
7. **Alcance respetado**: `processAction()` y `flushBufferedAttempts()` sin diff de lógica; `resolveCandidates()` no tocado; seeds no tocados — todo confirmado por `git diff`.

#### Pruebas ejecutadas (verificación independiente)

```
mvn -o test -Dtest=DistractorSelectorTest,RecognitionEngineTest,GameOrchestratorServiceTest,GameOrchestratorServiceSprint070Test,GameOrchestratorServiceMasteryPrioritizationTest,GameOrchestratorServiceCandidateFilteringTest,GameOrchestratorServiceTrackingIntegrationTest,GameWebSocketHandlerTest
Tests run: 145, Failures: 0, Errors: 0 — BUILD SUCCESS
```
Coincide exactamente con el recuento reportado por el developer.

```
mvn -o test -Dtest="es.vargontoc.educational.framework.game.**,es.vargontoc.educational.framework.session.**,es.vargontoc.educational.framework.content.**,es.vargontoc.educational.framework.family.**,es.vargontoc.educational.framework.tracking.**"
Tests run: 808, Failures: 0, Errors: 96 — BUILD FAILURE
```
Los 96 errores coinciden exactamente con los que reporta el developer: el patrón habitual `IllegalState: Failed to load ApplicationContext` (Docker/Testcontainers) en `DevContentController*`, `Auth/ChildSession/AdultProfile/ChildProfile/FamilyControllerTest`, `TrackingDashboardControllerTest`, `TrackingRetentionPersistenceTest`, `TrackingSchemaApplicationTest`; más `ChildProfileServiceTest` (11 errores) por `NullPointerException: this.avatarUseCase is null` — confirmado como un bug de fixture preexistente en ese test (no invoca a `avatarUseCase` como mock), ajeno a este sprint (`git status` confirma que ningún fichero de `family`/`avatar` fue tocado).

#### Criterios de aceptación cubiertos por tests

| Criterio | Test | Estado |
|---|---|---|
| `initGame()` almacena `RoundParameters` en `RecognitionState` | `initGame_storesRoundParametersInState` | ✅ |
| `getNextElement()` incluye los 3 campos nuevos | `getNextElement_includesGuideChromTouchDelayAndNonChromaticKey` | ✅ |
| `advanceRound()` preserva parámetros entre rondas | `advanceRound_preservesRoundParametersAcrossRounds` | ✅ |
| Compatibilidad hacia atrás sin `roundParameters` | `initGame_withoutRoundParameters_fallsBackToDefaultBehavior` | ✅ |
| `readyGame()` resuelve `RoundParameters` (MEDIUM) | `readyGame_resolvesRoundParametersFromDifficultyLevelAndColorVisionMode` | ✅ |
| COLOR + DEUTERANOPIA → `ColorVisionMode` correcto pasado al servicio | `readyGame_colorCategoryWithDeuteranopia_marksNonChromaticKeyRequired` | ✅ |
| `DifficultyLevel` no listo/nulo → fallback EASY | `readyGame_missingDifficultyLevel_defaultsToEasy` | ✅ |
| `ChildProfile` no encontrado → fallback `ColorVisionMode.NONE` | `readyGame_missingChildProfile_defaultsToColorVisionModeNone` | ✅ |
| Payload real al cliente incluye los 3 campos | `GameWebSocketHandlerTest` (test actualizado) | ✅ |

### Reviewer verification

**Veredicto: CHANGES_REQUIRED**

La implementación es sólida, coherente con FEAT-011/ADR-028, y está probada con una profundidad notable (fallbacks, compatibilidad hacia atrás, y la resolución previa de la observación de SPRINT-098). Sin embargo, hay un defecto de completitud que corresponde devolver al developer antes de cerrar el sprint como `verified`.

#### Defecto (bloqueante para `verified`, corrección rápida)

**Contrato no actualizado para un cambio de payload real.** `GameWebSocketHandler.gameStateToPayload()` ahora envía `guideChromEnabled`, `touchEnableDelayMs` y `nonChromaticKeyRequired` dentro de `recognitionState` en los eventos `GAME_READY`/`GAME_STARTED`/`GAME_ABANDONED` — confirmado en código y probado en `GameWebSocketHandlerTest`. Sin embargo `docs/contracts/api/asyncapi/schemas/game-state-payload.yaml` sigue documentando `recognitionState` solo con `recognitionCategory`, `roundIndex`, `totalRounds`, `targetElementId`, `optionIds`, `hintActive` y `elements` — los 3 campos nuevos no aparecen ni en `properties` ni en `required`. A diferencia de SPRINT-097/098 (que explícitamente no tocaban contratos y no lo necesitaban), este sprint sí introduce un cambio de contrato real y en vivo, y ni la sección "Contratos y dependencias externas" ni una tarea explícita lo cubren.

**Acción requerida**: añadir las 3 propiedades a `recognitionState.properties` en `game-state-payload.yaml` (boolean/integer, siempre presentes igual que `hintActive` ya que son primitivos con valor por defecto) e incluirlas en `required` junto a `hintActive`, ya que se serializan siempre que el engine es RECOGNITION, tengan o no `RoundParameters` resueltos.

#### Observación menor (no bloqueante)

- "Manual Tests" pide verificar que "el evento WebSocket `ROUND_READY` incluye los nuevos campos", pero ese tipo de evento no existe en `GameWebSocketHandler` (los tipos reales son `GAME_STARTED`/`GAME_READY`/`GAME_ABANDONED`, confirmado por grep). Es un resto de la redacción original de la tarea, anterior a la Decisión 3. Corregir el texto para no confundir a quien haga la prueba manual.

#### Limitaciones de la revisión

- No se ejecutaron los "Manual Tests" (backend levantado, verificación visual del WebSocket) por falta de Docker en este entorno — mismo límite reconocido en sprints anteriores. Cubierto de forma equivalente por los tests automatizados de `GameWebSocketHandlerTest` y `RecognitionEngineTest`.

### Developer follow-up (post-review)

Ambos puntos del revisor corregidos:

1. **Defecto bloqueante — contrato actualizado.** `docs/contracts/api/asyncapi/schemas/game-state-payload.yaml`: añadidas `guideChromEnabled` (boolean), `touchEnableDelayMs` (integer) y `nonChromaticKeyRequired` (boolean) a `recognitionState.properties`, e incluidas en `recognitionState.required` junto a `hintActive` — se serializan siempre que el engine es RECOGNITION, tal como señaló el revisor. YAML validado sintácticamente.
2. **Observación menor — texto corregido.** "Manual Tests" ya no referencia el evento inexistente `ROUND_READY`; ahora dice `GAME_READY`, que es el tipo de evento real emitido por `GameWebSocketHandler` tras `readyGame()`.

Sin cambios de código de producción adicionales — el payload en tiempo de ejecución ya era correcto (confirmado por el revisor); lo que faltaba era únicamente la documentación del contrato.

### Reviewer follow-up verification

Verificado de forma independiente:

- `game-state-payload.yaml`: las 3 propiedades están correctamente añadidas a `recognitionState.properties` con tipo y descripción adecuados, e incluidas en `recognitionState.required` junto a `hintActive`. Confirmado que `guideChromEnabled`/`touchEnableDelayMs`/`nonChromaticKeyRequired` son campos primitivos (`boolean`/`int`) en `RecognitionState.java` con valores por defecto de Java (`false`/`0`), por lo que Jackson los serializa siempre que el engine es RECOGNITION, con o sin `RoundParameters` resueltos — la justificación del developer para incluirlos en `required` es correcta, no una copia mecánica de la sugerencia.
- `git status` confirma que solo se tocó el fichero YAML y el propio sprint doc; ningún fichero de código de `framework/backend/src` cambió desde la revisión anterior — coherente con "sin cambios de código de producción adicionales".
- El texto de "Manual Tests" ya no menciona `ROUND_READY`; ahora referencia correctamente `GAME_READY`.

**Veredicto final: APPROVED**. Las dos observaciones quedan resueltas con evidencia verificada de forma independiente; no quedan hallazgos pendientes.
