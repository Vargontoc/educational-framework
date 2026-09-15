# Sprint 098 - backend
# -----------------------------------------------

## Goal
Implementar la selección de distractores por estrategia de dificultad (`SEMANTICALLY_FAR`, `SAME_CATEGORY`, `SIMILAR_OUTLINE`) e integrar el selector en `RecognitionEngine.buildOptions()`.

## Status
status: pending
started_at:
closed_at:
blocked_by: Sprint 097
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Domain Model Extension
- [ ] Añadir campo `similarityGroup` (String, nullable) a `RecognitionElement` en `content/model/`.
- [ ] Añadir campo `similarityGroup` a `RecognitionElementJpaEntity` y `RecognitionElementPersistenceAdapter`.
- [ ] Actualizar seed `16-recognition-elements.json` para incluir `similarityGroup` en elementos de LETTER y NUMBER (agrupar por similitud visual: vocales juntas, consonantes con contorno similar, números con forma parecida).

### Distractor Selector
- [ ] Crear clase `DistractorSelector` en `game/service/` con método `select(String target, List<String> candidates, DistractorStrategy strategy, int count, Function<String, RecognitionElement> elementResolver)`.
- [ ] Implementar estrategia `SEMANTICALLY_FAR`: selección aleatoria simple de cualquier candidato (comportamiento actual).
- [ ] Implementar estrategia `SAME_CATEGORY`: filtrar candidatos por mismo `topicId` que el target, luego seleccionar aleatoriamente.
- [ ] Implementar estrategia `SIMILAR_OUTLINE`: filtrar candidatos por mismo `similarityGroup` que el target, luego seleccionar aleatoriamente.
- [ ] Implementar fallback: si no hay suficientes candidatos de la estrategia, completar con aleatorios del resto.
- [ ] Si el fallback no alcanza el count solicitado, devolver los disponibles sin error.

### Integration with RecognitionEngine
- [ ] Modificar `RecognitionEngine.buildOptions()` para aceptar `DistractorStrategy` y `optionCount` de `RoundParameters`.
- [ ] Inyectar `DistractorSelector` en `RecognitionEngine` (vía constructor o parámetro de método).
- [ ] Mantener la lógica de anti-repetición intra-partida (`roundsShownElementIds`) intacta.

### Tests
- [ ] Test unitario: `SEMANTICALLY_FAR` devuelve distractores aleatorios sin filtrar por topic.
- [ ] Test unitario: `SAME_CATEGORY` devuelve solo distractores del mismo topicId que el target.
- [ ] Test unitario: `SIMILAR_OUTLINE` devuelve solo distractores del mismo similarityGroup.
- [ ] Test unitario: fallback a aleatorio cuando no hay suficientes candidatos de la estrategia.
- [ ] Test unitario: fallback parcial cuando hay algunos pero no todos los necesarios.
- [ ] Test unitario: `buildOptions()` con `optionCount=2` devuelve 1 target + 1 distractor.
- [ ] Test unitario: `buildOptions()` con `optionCount=3` devuelve 1 target + 2 distractores.
- [ ] Test unitario: `buildOptions()` con `optionCount=4` (HARD) devuelve 1 target + 3 distractores.

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

## Notes
- Este sprint prepara la selección de distractores para que Sprint 099 pueda integrar la ladder completa.
- Los seeds de FORMAS y COLOR se completarán en Sprint 100.
- `elementResolver` permite al selector acceder a metadatos del elemento sin acoplarse al repositorio.
