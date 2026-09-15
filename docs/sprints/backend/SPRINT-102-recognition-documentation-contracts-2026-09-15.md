# Sprint 102 - backend
# -----------------------------------------------

## Goal
Actualizar FEAT-009 para reflejar el modelo de consolidación diferida (decisión 4 de FEAT-011) y crear los contratos WebSocket de `round-parameters` y `round-ready-event` en `docs/contracts`.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by: Sprint 099, Sprint 101
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### FEAT-009 Documentation Update
- [x] Actualizar FEAT-009 §"Persistencia: ActivityAttempt vs GameSessionSummary" para reflejar el modelo de consolidación diferida.
- [x] Añadir párrafo explicativo sobre buffer en memoria (`RecognitionState.roundAttempts`), flush al completar, y limpieza al cerrar sesión.
- [x] Añadir referencia cruzada a FEAT-011 para la decisión de consolidación diferida — apunta a FEAT-011 §3.8/§4.8 y ADR-028 §4 regla 7 en vez de una "decisión 4" que no existe como tal en FEAT-011 (ver Decisiones).
- [x] Verificar que FEAT-009 §"RecognitionAttemptContext" sigue siendo coherente con el nuevo modelo — confirmado por lectura cruzada contra `RecognitionAttemptContext.java`: mismos 11 campos, sin cambios necesarios.

### WebSocket Contracts
- [x] Crear `docs/contracts/schemas/round-parameters.v1.yaml` con el esquema de `RoundParameters`:
    - `optionCount` (integer)
    - `distractorStrategy` (enum: SEMANTICALLY_FAR, SAME_CATEGORY, SIMILAR_OUTLINE)
    - `guideChromEnabled` (boolean)
    - `touchEnableDelayMs` (integer)
    - `nonChromaticKeyRequired` (boolean)
- [x] Crear `docs/contracts/schemas/round-ready-event.v1.yaml` con el esquema del evento WebSocket — **corregido a `GAME_READY`** (el evento `ROUND_READY` no existe en la implementación, ver Decisiones):
    - `event` (const: "GAME_READY") — el campo del sobre real es `event`, no `type`.
    - `payload.recognitionState` (object con `targetElementId`, `optionIds`, `roundIndex`, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`) — anidado bajo `recognitionState`, no plano.
- [x] Verificar que los contratos YAML son válidos y coherentes con la implementación — sintaxis validada con `yaml.safe_load`, contenido verificado por lectura cruzada contra `RoundParameters.java`/`DistractorStrategy.java`/`SessionEvent.java`/`SessionEventType.java`/`GameWebSocketHandler.gameStateToPayload()`.

### Handoff Documentation
- [x] Documentar handoff a frontend en FEAT-011: contratos WebSocket que frontend debe consumir (`GAME_READY` con `recognitionState`, corregido de la referencia obsoleta a `ROUND_READY`).
- [x] Documentar handoff a agents: Nubi no condiciona la resolución de ronda (sin cambios de contrato).
- [x] Documentar handoff a contenido: catálogo de FORMAS y COLOR adaptado a la ladder (Sprint 100).

### Validation
- [x] Verificar que no hay contradicciones entre FEAT-009 actualizado y FEAT-011 — coherente (misma cita de consolidación diferida en ambos).
- [x] Verificar que los contratos de `docs/contracts` son coherentes con la implementación de Sprint 099 — coherentes tras la corrección de nombre/forma del evento.
- [x] Verificar que `RecognitionState` serializado coincide con el esquema de `round-ready-event.v1.yaml` — coincide exactamente con el subconjunto de `recognitionState` que `GameWebSocketHandler.gameStateToPayload()` serializa (líneas 627-637), una vez corregido el esquema para reflejar el anidamiento real bajo `payload.recognitionState`.

## Manual Tests
- Revisar FEAT-009 actualizado y verificar que el párrafo de consolidación diferida es claro y coherente.
- Revisar contratos YAML y verificar que son válidos (sintaxis correcta).
- Comparar contratos YAML con la implementación de `RecognitionEngine.getNextElement()` y verificar coherencia.

## Risks
- Documentación desactualizada puede causar confusión en futuros sprints: mitigar revisando coherencia.
- Contratos YAML incorrectos pueden causar errores de integración frontend: mitigar validando sintaxis.

## Dependencies
- Sprint 099 completado (integración de ladder, eventos WebSocket extendidos).
- Sprint 101 completado (configuración dinámica externalizada).

## Agent Instruction
- No modificar código de producción en este sprint.
- Solo actualizar documentación (FEAT-009) y crear contratos YAML en `docs/contracts`.
- Los contratos YAML deben ser autocontenidos y no referenciar implementación interna.
- FEAT-009 debe mantener su estructura original; solo añadir/actualizar las secciones afectadas.

## Notes
- Este sprint cierra la serie de sprints de FEAT-011.
- Los contratos YAML son la fuente de verdad para frontend; ninguna capa los duplica.
- La actualización de FEAT-009 es necesaria para mantener la coherencia documental tras la decisión de consolidación diferida (ver Decisiones sobre la referencia real, no una "decisión 4" literal).

## Decisiones confirmadas

1. **El evento documentado es `GAME_READY`, no `ROUND_READY`.** El Goal y las tareas de este mismo sprint citan un evento `ROUND_READY` que no existe en la implementación — verificado en `SessionEventType.java` (no hay tal constante) y en `GameWebSocketHandler.java:520`, donde el evento que entrega el estado de ronda lista es `GAME_READY`. Es la misma confusión terminológica que el revisor de Sprint 099 ya señaló una vez en la sección Manual Tests de ese sprint (corregida entonces a `GAME_READY`). La tarea de Validation de este sprint ("verificar que `RecognitionState` serializado coincide con el esquema") obliga a ejecutar esa verificación honestamente, así que `round-ready-event.v1.yaml` documenta el evento real. También se corrigió el nombre del campo del sobre (`event`, no `type`, per `SessionEvent.java`) y la forma del payload (anidado bajo `payload.recognitionState`, no plano), ambos verificados contra `SessionEvent.java` y `GameWebSocketHandler.gameStateToPayload()`.
2. **"Decisión 4 de FEAT-011" no existe como tal.** FEAT-011 no tiene una lista numerada de "decisiones" (a diferencia de FEAT-009, que sí tiene un "Historial de decisiones"). El contenido real de consolidación diferida vive en FEAT-011 §3 "Requisitos funcionales" punto 8, §4 "Criterios de aceptación" punto 8, y ADR-028 §4 "Reglas transversales" regla 7. FEAT-009 se actualizó referenciando esas ubicaciones reales en vez de repetir la cita imprecisa del Goal.
3. **`round-ready-event.v1.yaml` no duplica `game-state-payload.yaml` completo.** Documenta solo el subconjunto de `recognitionState` pedido explícitamente por el sprint (6 campos de "ronda lista": `targetElementId`, `optionIds`, `roundIndex`, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`), con una referencia en la descripción a `docs/contracts/api/asyncapi/schemas/game-state-payload.yaml` para el payload completo (que incluye además `recognitionCategory`, `totalRounds`, `hintActive`, `elements`). Evita dos fuentes de verdad divergentes para el mismo payload real.

## Verificación
- Sintaxis YAML validada (`yaml.safe_load`) en `round-parameters.v1.yaml` y `round-ready-event.v1.yaml`: ambos parsean correctamente.
- Coherencia campo a campo verificada por lectura cruzada: `round-parameters.v1.yaml` ↔ `RoundParameters.java`/`DistractorStrategy.java`; `round-ready-event.v1.yaml` ↔ `SessionEvent.java`/`SessionEventType.java`/`GameWebSocketHandler.gameStateToPayload()` (líneas 598-674).
- `git status`/`git diff --stat`: solo 4 archivos tocados, todos bajo `docs/` (`FEAT-009...md`, `FEAT-011...md`, 2 YAML nuevos) — ningún archivo `.java` modificado, cumpliendo el Agent Instruction de no tocar código de producción.

## Review

### Developer implementation — Evidencias
1. **FEAT-009**: nueva sección "Modelo de consolidación diferida" describe el buffer `RecognitionState.roundAttempts` (lista de `RoundAttemptRecord`), su flush al completar la partida y su descarte al abandonar. Verifiqué `RoundAttemptRecord.java` campo a campo: `topicId`, `elementId`, `difficultyLevelId`, `result` (`AttemptResult`), `responseTimeMs`, `attemptContext` — coincide exactamente con "topicId, elementId, difficultyLevelId, resultado, tiempo de respuesta y contexto de intento" citado en el texto. El comportamiento de descarte en abandono ya estaba verificado en SPRINT-096 (`discardGameForSession` no vuelca el buffer).
2. **Corrección de la cita "decisión 4 de FEAT-011"**: confirmé que FEAT-011 no tiene una lista de "decisiones" numeradas (a diferencia de FEAT-009). Localicé y leí íntegramente FEAT-011 §3 punto 8 y §4 punto 8, y ADR-028 §4 "Reglas transversales" regla 7 — los tres textos citados en el diff coinciden palabra por palabra con el contenido real de esos documentos. El desarrollador identificó y corrigió una imprecisión que estaba en el propio enunciado del sprint, en vez de inventar una "decisión 4" inexistente para cuadrar la tarea.
3. **Corrección `ROUND_READY` → `GAME_READY`**: confirmé en `SessionEventType.java` que no existe ninguna constante `ROUND_READY`, y que `GAME_READY` sí existe ("Game transitioned to IN_PROGRESS and ready for actions."). Confirmé en `GameWebSocketHandler.java:520` que el evento que efectivamente transporta `recognitionState` de una ronda lista es `GAME_READY`, construido con `SessionEvent.of(SessionEventType.GAME_READY, childSessionId, gameStateToPayload(updatedState))`. Coincide con el mismo hallazgo que el revisor de Sprint 099 ya había señalado en la sección Manual Tests de ese sprint.
4. **`round-parameters.v1.yaml`**: los 5 campos (`optionCount`, `distractorStrategy` con enum de 3 valores, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`) coinciden exactamente en nombre, tipo y orden con el record `RoundParameters.java`. YAML parsea correctamente (`yaml.safe_load`, reejecutado independientemente).
5. **`round-ready-event.v1.yaml`**: el sobre (`event` const `GAME_READY`, `sessionId`, `payload`) coincide exactamente con los `@JsonProperty` de `SessionEvent.java` (`event`, `sessionId`, `payload`) — no con `type`, como habría sido un error común. El subconjunto de `recognitionState` documentado (`targetElementId`, `optionIds`, `roundIndex`, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`) es exactamente un subconjunto de los 9 campos reales que `GameWebSocketHandler.gameStateToPayload()` escribe en `recognitionPayload` (líneas 628-637) — omite deliberadamente `recognitionCategory`, `totalRounds`, `hintActive` y `elements`, documentados aparte en `game-state-payload.yaml` según la Decisión 3, evitando duplicar una fuente de verdad.
6. **Handoff en FEAT-011 (§8 nueva)**: resume correctamente el contrato para frontend (`GAME_READY`/`recognitionState`), confirma que Nubi no condiciona la resolución de ronda (sin contradicción con ADR-028 §4 regla 5) y referencia el catálogo de FORMAS/COLOR de Sprint 100.

### Verificación independiente
- `git diff` de `FEAT-009-Recognition-Engine.md` y `FEAT-011-...md`: solo las secciones nuevas descritas arriba, estructura original intacta (cumple Agent Instruction).
- `git status`: confirmado que únicamente 4 archivos de `docs/` están tocados; ningún `.java` modificado.
- Releí `RoundParameters.java`, `SessionEvent.java`, `SessionEventType.java` y `GameWebSocketHandler.java` (líneas 590-674) de forma independiente y confirmé cada afirmación de las Decisiones 1-3, sin encontrar ninguna discrepancia.
- Reejecuté `yaml.safe_load` sobre ambos YAML nuevos: parsean sin error.

### Reviewer verification

**Veredicto: APPROVED**

Sprint puramente documental que cumple el Agent Instruction al pie de la letra (cero cambios en `.java`). Lo más destacable es que el desarrollador encontró y corrigió honestamente dos imprecisiones que venían en el propio enunciado del sprint (el evento `ROUND_READY` inexistente y la "decisión 4 de FEAT-011" inexistente) en vez de documentar literalmente lo que el Goal/Tasks pedían — el mismo patrón de rigor ya visto en sprints anteriores (098, 099, 100). Todas las citas cruzadas a FEAT-011/ADR-028 y los dos contratos YAML nuevos fueron verificados palabra por palabra y campo por campo contra el código real, sin encontrar ninguna discrepancia. Sin observaciones.
