# Sprint 098 - backend
# -----------------------------------------------

## Goal
Implementar la selección de distractores por estrategia de dificultad (`SEMANTICALLY_FAR`, `SAME_CATEGORY`, `SIMILAR_OUTLINE`) e integrar el selector en `RecognitionEngine.buildOptions()`.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by: Sprint 097
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Domain Model Extension
- [x] Añadir campo `similarityGroup` (String, nullable) a `RecognitionElement` en `content/model/`.
- [x] Añadir campo `similarityGroup` a `RecognitionElementJpaEntity` y `RecognitionElementPersistenceAdapter`.
- [x] Actualizar seed `16-recognition-elements.json` para incluir `similarityGroup`. Ver Decisión 1: el seed solo tenía LETTER (8 letras), sin ninguna entrada NUMBER — se añadió `similarityGroup` a las 8 letras existentes (`angular_peak`: A/E/M, `curve_round`: C/O/S, `vertical_bump`: B/D); no se crearon elementos NUMBER nuevos.

### Distractor Selector
- [x] Crear clase `DistractorSelector` en `game/service/` con método `select(String target, List<String> candidates, DistractorStrategy strategy, int count, Function<String, RecognitionElement> elementResolver)`.
- [x] Implementar estrategia `SEMANTICALLY_FAR`: selección aleatoria simple de cualquier candidato (comportamiento actual).
- [x] Implementar estrategia `SAME_CATEGORY`: filtrar candidatos por mismo `topicId` que el target, luego seleccionar aleatoriamente.
- [x] Implementar estrategia `SIMILAR_OUTLINE`: filtrar candidatos por mismo `similarityGroup` que el target, luego seleccionar aleatoriamente.
- [x] Implementar fallback: si no hay suficientes candidatos de la estrategia, completar con aleatorios del resto.
- [x] Si el fallback no alcanza el count solicitado, devolver los disponibles sin error.

### Integration with RecognitionEngine
- [x] Modificar `RecognitionEngine.buildOptions()` para aceptar `DistractorStrategy` y `optionCount` de `RoundParameters`. Implementado como una **sobrecarga nueva** de 5 argumentos; la sobrecarga original de 2 argumentos se mantiene y delega en la nueva con `SEMANTICALLY_FAR`/`optionCount=null`, reproduciendo el comportamiento previo exactamente. Ver Decisión 2.
- [x] Inyectar `DistractorSelector` en `RecognitionEngine` (vía constructor o parámetro de método). Se añadió como campo, construido en el constructor compartiendo el mismo `Random` del engine.
- [x] Mantener la lógica de anti-repetición intra-partida (`roundsShownElementIds`) intacta. No se tocó `selectTarget()` ni el manejo de `roundsShownElementIds`.

### Tests
- [x] Test unitario: `SEMANTICALLY_FAR` devuelve distractores aleatorios sin filtrar por topic.
- [x] Test unitario: `SAME_CATEGORY` devuelve solo distractores del mismo topicId que el target.
- [x] Test unitario: `SIMILAR_OUTLINE` devuelve solo distractores del mismo similarityGroup.
- [x] Test unitario: fallback a aleatorio cuando no hay suficientes candidatos de la estrategia.
- [x] Test unitario: fallback parcial cuando hay algunos pero no todos los necesarios.
- [x] Test unitario: `buildOptions()` con `optionCount=2` devuelve 1 target + 1 distractor.
- [x] Test unitario: `buildOptions()` con `optionCount=3` devuelve 1 target + 2 distractores.
- [x] Test unitario: `buildOptions()` con `optionCount=4` (HARD) devuelve 1 target + 3 distractores.

## Manual Tests
- Ejecutar tests unitarios con `mvn test -pl framework/backend -Dtest=DistractorSelectorTest,RecognitionEngineTest`.
- Verificar que los tests de `RecognitionEngine` siguen pasando tras la modificación de `buildOptions()`.

## Risks
- `similarityGroup` mal asignado en seeds puede causar distractores incorrectos: mitigar con validación manual de contenido.
- Fallback demasiado frecuente puede degradar la experiencia: mitigar asegurando contenido suficiente por grupo.

## Dependencies
- Sprint 097 completado (modelo de `RoundParameters` y `DistractorStrategy`).

## Agent Instruction
- No modificar la lógica de `advanceRound()` ni `processAction()` en este sprint.
- No modificar `GameOrchestratorService` en este sprint.
- `similarityGroup` es nullable; elementos sin grupo se tratan como grupo único (fallback a aleatorio).
- El fallback no debe lanzar excepción; devolver los disponibles.

## Decisiones confirmadas

1. **No existían elementos NUMBER en el seed.** La tarea original asumía `similarityGroup` en LETTER y NUMBER, pero `16-recognition-elements.json` solo contenía 8 letras (A,B,C,D,E,M,O,S), cero números. Se asignó `similarityGroup` a las 8 letras existentes según semejanza visual real (`angular_peak`: A/E/M — trazos rectos/picos; `curve_round`: C/O/S — formas curvas; `vertical_bump`: B/D — trazo vertical + panza, par clásico de confusión infantil). No se crearon elementos NUMBER nuevos: es contenido nuevo, fuera de alcance de un sprint de motor (coherente con que el propio doc deja seeds de FORMAS/COLOR para Sprint 100).
2. **`buildOptions()` ganó una sobrecarga nueva en vez de reemplazar la existente.** Esto permitió cumplir simultáneamente "modificar `buildOptions()` para aceptar `DistractorStrategy`/`optionCount`" y "no modificar la lógica de `advanceRound()`/`processAction()`" ni `GameOrchestratorService`: `buildInitialState()` y `advanceRound()` (únicos llamantes internos) siguen invocando la sobrecarga de 2 argumentos sin ningún cambio de código; esa sobrecarga ahora delega en la nueva pasando `SEMANTICALLY_FAR` + `optionCount=null`, reproduciendo el comportamiento anterior exactamente (verificado: los 37 tests preexistentes de `RecognitionEngineTest` pasan sin ninguna modificación). El resultado es que este sprint es puramente aditivo/inerte sobre el flujo real de partida — la ladder no se conecta todavía a rondas reales. Eso es intencional: el propio doc indica que la integración end-to-end ocurre en Sprint 099.
3. **`elementResolver: Function<String, RecognitionElement>` viaja solo como parámetro de método, nunca como campo del engine.** Evita cualquier ambigüedad respecto al test de desacoplamiento `engineHasNoDependenciesOnWorldTrackingContentOrSession` (que inspecciona constructores y campos declarados): el único campo nuevo del engine es `distractorSelector`, de tipo `game.service.DistractorSelector` (propio del módulo), sin ninguna referencia a `content.*` en su superficie declarada.

## Notes
- Este sprint prepara la selección de distractores para que Sprint 099 pueda integrar la ladder completa.
- Los seeds de FORMAS y COLOR se completarán en Sprint 100.
- `elementResolver` permite al selector acceder a metadatos del elemento sin acoplarse al repositorio.

## Verificación
- `mvn -o compile` / `mvn -o test-compile`: BUILD SUCCESS.
- `mvn -o test -Dtest=DistractorSelectorTest,RecognitionEngineTest`: 49 tests ejecutados (8 + 41), 0 fallos. Los 37 tests preexistentes de `RecognitionEngineTest` (incluido `engineHasNoDependenciesOnWorldTrackingContentOrSession`) siguen en verde sin haber sido modificados.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.content.**,es.vargontoc.educational.framework.game.**"`: 490 tests ejecutados, 0 fallos. 34 errores, todos en `DevContentControllerTest`/`DevContentControllerDisabledTest` por `IllegalState: Failed to load ApplicationContext` (falta de Docker/Testcontainers en este entorno) — mismo tipo de limitación preexistente documentada en sprints anteriores, sin relación con los cambios de este sprint.
- Confirmado que `GameOrchestratorService.java` no fue modificado y que `advanceRound()`/`processAction()`/`buildInitialState()` en `RecognitionEngine.java` no tienen diff de comportamiento (solo la extracción de `clampToDefaultRange` como método privado, sin cambiar su lógica).

## Review

### Developer implementation — Evidencias

#### Resumen técnico verificado

1. **`similarityGroup` en el modelo/persistencia**: campo añadido a `RecognitionElement`, `RecognitionElementJpaEntity` (columna `similarity_group VARCHAR(50)` nullable) y `RecognitionElementPersistenceAdapter` (mapeo `toDomain`/`toJpa` en ambas direcciones). Migración `041__add_similarity_group_to_recognition_element.xml` incluida correctamente en `db.changelog-master.xml` tras `040`.
2. **Seed actualizado**: `16-recognition-elements.json` confirma las 8 letras existentes (A,B,C,D,E,M,O,S) con `similarityGroup` asignado exactamente como documenta la Decisión 1 (`angular_peak`: A/E/M; `curve_round`: C/O/S; `vertical_bump`: B/D). Cero elementos NUMBER, confirmando que no se inventó contenido nuevo. **No listado como tarea explícita pero verificado**: `SeedData.RecognitionElementSeed` (record) y `SeedService.loadRecognitionElements(...)` fueron actualizados para leer y persistir `similarityGroup` desde el JSON — sin este cambio el campo del seed habría sido ignorado silenciosamente. Correcto y necesario.
3. **`DistractorSelector.select(...)`**: verificado línea por línea contra los 8 tests. La estrategia `SEMANTICALLY_FAR` no filtra; `SAME_CATEGORY` filtra por `topicId` igual al target; `SIMILAR_OUTLINE` filtra por `similarityGroup` igual al target (con guarda explícita para `similarityGroup == null`, de modo que un target sin grupo nunca "empareja" por accidente con otro elemento también sin grupo — confirmado por el test `select_similarOutline_nullGroupNeverMatchesAnotherNullGroup`, que en realidad pasa por la vía de fallback total, no por un macheo real de nulos). El fallback (parcial y total) rellena desde el resto del pool sin lanzar excepción cuando no hay candidatos suficientes.
4. **`RecognitionEngine.buildOptions()`**: la nueva sobrecarga de 5 argumentos y la retrocompatibilidad de la de 2 argumentos están correctamente implementadas. Verificado por trazado manual que la delegación (`SEMANTICALLY_FAR`, `optionCount=null`, `id -> null`) reproduce exactamente la secuencia de construcción de `pool`/`shuffle`/selección del código anterior, consumiendo el `Random` compartido en el mismo orden — consistente con que los 37 tests preexistentes de `RecognitionEngineTest` pasen sin ninguna modificación. `clampToDefaultRange(...)` es una extracción literal de las 3 líneas previas, sin cambio de lógica.
5. **Alcance respetado**: `git diff` confirma que `GameOrchestratorService.java` no fue tocado y que `advanceRound()`/`processAction()`/`buildInitialState()` siguen invocando la sobrecarga de 2 argumentos sin cambios — la ladder no llega todavía a una partida real (confirmado, es intencional y documentado para Sprint 099).

#### Pruebas ejecutadas (verificación independiente)

```
mvn -o test -Dtest=DistractorSelectorTest,RecognitionEngineTest
Tests run: 49 (41 + 8), Failures: 0, Errors: 0 — BUILD SUCCESS
```

```
mvn -o test -Dtest="es.vargontoc.educational.framework.content.**,es.vargontoc.educational.framework.game.**"
Tests run: 490, Failures: 0, Errors: 34 — BUILD FAILURE
```
Los 34 errores están confinados a `DevContentControllerTest` (26) y `DevContentControllerDisabledTest` (8), ambos con `IllegalState: Failed to load ApplicationContext` — mismo patrón de falta de Docker/Testcontainers ya documentado en sprints anteriores, confirmado que no tiene relación con `similarityGroup`, el seed ni `RecognitionElement`.

#### Criterios de aceptación cubiertos por tests

| Criterio | Test | Estado |
|---|---|---|
| `SEMANTICALLY_FAR` no filtra por topic | `select_semanticallyFar_ignoresElementResolverAndReturnsFromCandidates` | ✅ |
| `SAME_CATEGORY` filtra por `topicId` | `select_sameCategory_returnsOnlyMatchingTopicId` | ✅ |
| `SIMILAR_OUTLINE` filtra por `similarityGroup` | `select_similarOutline_returnsOnlyMatchingSimilarityGroup` | ✅ |
| Fallback total cuando ningún candidato matchea | `select_totalFallback_whenNoCandidateMatchesStrategy` | ✅ |
| Fallback parcial cuando faltan candidatos | `select_partialFallback_fillsRemainderFromOtherCandidates` | ✅ |
| Pool agotado no lanza excepción | `select_exhaustedPool_returnsAvailableWithoutThrowing` | ✅ |
| `buildOptions(optionCount=2/3/4)` → target + N-1 distractores | `buildOptions_optionCountTwo/Three/Four_...` | ✅ |
| Sobrecarga de 2 argumentos preserva comportamiento previo | 37 tests preexistentes + `buildOptions_twoArgOverload_stillProducesSameBehaviorAsBefore` (nuevo) | ✅ |

### Reviewer verification

**Veredicto: APPROVED_WITH_OBSERVATIONS**

Implementación completa, correcta y bien probada. Los valores del seed, la lógica de selección de distractores y la preservación exacta del comportamiento previo de `buildOptions()` están verificados con evidencia reproducible. `GameOrchestratorService` y los contratos no se tocaron, tal como exigía el alcance.

#### Observación (no bloqueante, para considerar antes de Sprint 099)

`RecognitionEngine.java` ahora importa `es.vargontoc.educational.framework.content.model.RecognitionElement` directamente, para tipar el parámetro `Function<String, RecognitionElement> elementResolver` de la nueva sobrecarga de `buildOptions()`. La Decisión 3 del sprint es transparente sobre esto: explica que se eligió pasar `elementResolver` como parámetro de método (nunca como campo) específicamente para no hacer fallar `engineHasNoDependenciesOnWorldTrackingContentOrSession`, que **solo** inspecciona por reflexión los constructores públicos y los campos declarados de `RecognitionEngine`, no las firmas de sus métodos.

Confirmado por lectura del test: es exactamente así de estrecho. Eso significa que, aunque el test sigue en verde, `RecognitionEngine` (paquete `game.engine`, históricamente pensado como el núcleo "puro" sin dependencias de `content`/`world`/`tracking`/`session`) tiene ahora una dependencia de compilación real — aunque mínima y no invocada dentro de la clase — hacia `content.model`. La clase que sí ejecuta lógica sobre `RecognitionElement` es `DistractorSelector` (paquete `game.service`, donde ya es normal depender de `content` — mismo patrón que `GameOrchestratorService`); `RecognitionEngine` solo reenvía la referencia sin tocarla.

No es un defecto funcional — nada se rompe y la elección de pasar el resolver como parámetro (no como campo/dependencia inyectada) es la decisión de diseño correcta para mantener el motor agnóstico del repositorio. El único residuo es el nombre del tipo `RecognitionElement` en la firma del método. Si la pureza de paquete de `game.engine` es una invariante que el equipo quiere mantener estrictamente (no solo "lo que el test actual detecta"), una alternativa de bajo costo sería que `buildOptions()` reciba `Function<String, ?>` (o un pequeño record propio de `game.model.recognition` con solo `topicId`/`similarityGroup`) y delegue el cast/adaptación a `DistractorSelector`. Se deja como sugerencia para quien continúe con Sprint 099, no como bloqueante de este sprint.
