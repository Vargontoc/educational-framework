# Sprint 097 - backend
# -----------------------------------------------

## Goal
Crear el modelo de dificultad de `RecognitionEngine` con parámetros configurables por nivel (EASY/MEDIUM/HARD) y categoría (LETRA/NÚMERO/FORMA/COLOR/ANIMAL), conforme a la ladder de ADR-028 y FEAT-011.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by:
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Domain Model
- [x] Crear enum `DistractorStrategy` con valores `SEMANTICALLY_FAR`, `SAME_CATEGORY`, `SIMILAR_OUTLINE` en `game/model/recognition/`.
- [x] Crear record `RoundParameters` en `game/model/recognition/` con campos: `optionCount` (int), `distractorStrategy` (DistractorStrategy), `guideChromEnabled` (boolean), `touchEnableDelayMs` (int), `nonChromaticKeyRequired` (boolean).
- [x] Crear clase `RecognitionDifficultyConfig` en `game/model/recognition/` con valores por defecto de la ladder (no hardcodeados en la lógica, sino como configuración inyectable). Implementada como `Map<DifficultyCode, LadderTier>` (`LadderTier` = record anidado con los 4 valores por nivel), poblado en el constructor y expuesto vía `tierFor(DifficultyCode)`. Ver Decisión 1 sobre el valor concreto usado para `HARD.optionCount`.

### Application Service
- [x] Crear `RecognitionDifficultyService` en `game/service/` con método `resolveRoundParameters(DifficultyCode, RecognitionCategory, ColorVisionMode)` que devuelve `RoundParameters`.
- [x] Implementar lógica de resolución de `optionCount`: EASY=2, MEDIUM=3, HARD=4 (dentro del rango 3-4 documentado; ver Decisión 1).
- [x] Implementar lógica de resolución de `distractorStrategy`: EASY=SEMANTICALLY_FAR, MEDIUM=SAME_CATEGORY, HARD=SIMILAR_OUTLINE.
- [x] Implementar lógica de `guideChromEnabled`: true solo en EASY, false en MEDIUM y HARD.
- [x] Implementar lógica de `touchEnableDelayMs`: EASY=500, MEDIUM=800, HARD=0 (configurable).
- [x] Implementar lógica de `nonChromaticKeyRequired`: true si categoría es COLOR y `colorVisionMode` no es NONE, false en resto de casos. Usa `family.model.ColorVisionMode` completo (9 valores), no solo los 2-3 mencionados a modo de ejemplo en el doc — cualquier valor distinto de NONE activa la clave no cromática (ver Decisión 2).

### Spring Configuration
- [x] Registrar `RecognitionDifficultyService` como bean de Spring en `GameModuleConfiguration`. También se registró `RecognitionDifficultyConfig` como bean independiente (inyectado en el servicio), para que sea sustituible/mockeable y quede listo para la externalización a `application.yml` en Sprint 101.

### Tests
- [x] Test unitario: EASY + LETTER → 2 opciones, SEMANTICALLY_FAR, guideChrom=true, delay=500, nonChromatic=false.
- [x] Test unitario: MEDIUM + ANIMAL → 3 opciones, SAME_CATEGORY, guideChrom=false, delay=800, nonChromatic=false.
- [x] Test unitario: HARD + NUMBER → 3-4 opciones (aserción de rango, no de valor exacto), SIMILAR_OUTLINE, guideChrom=false, delay=0, nonChromatic=false.
- [x] Test unitario: EASY + COLOR + NONE → nonChromatic=false.
- [x] Test unitario: MEDIUM + COLOR + DEUTERANOPIA → nonChromatic=true.
- [x] Test unitario: HARD + COLOR + ACHROMATOPSIA → nonChromatic=true.
- [x] Test unitario: SHAPE se comporta igual que LETTER/NUMBER/ANIMAL para parámetros base (aserción por igualdad de `RoundParameters` completo entre ambas categorías, misma dificultad).

## Manual Tests
- Ejecutar tests unitarios con `mvn test -pl framework/backend -Dtest=RecognitionDifficultyServiceTest`.
- Verificar que todos los tests pasan sin errores.

## Risks
- Valores hardcodeados en lugar de configurables: mitigar usando `RecognitionDifficultyConfig` inyectable.
- Confusión entre `guideChromEnabled` (cromo guía permanente de EASY) y `hintActive` (ayuda tras 2 fallos): son conceptos distintos que deben documentarse.

## Dependencies
- Ninguna. Este sprint es el primero de la serie FEAT-011.
- FEAT-009 ya implementado (base del motor).

## Agent Instruction
- No modificar `RecognitionEngine` en este sprint. Solo crear el modelo y servicio de dificultad.
- No modificar contratos de `docs/contracts`.
- Los valores por defecto deben ser configurables, no constantes finales en la lógica de negocio.
- `RecognitionDifficultyConfig` es una clase de configuración con valores por defecto; la externalización a `application.yml` se hará en Sprint 101.

## Decisiones confirmadas

1. **`HARD.optionCount` fijado en 4, no como rango.** La ladder de ADR-028 documenta "3-4" para HARD, pero `RoundParameters.optionCount()` es un `int` único (no un rango). Se fijó el default en 4 (extremo más exigente del rango documentado), configurable vía `RecognitionDifficultyConfig` igual que el resto de valores. El test correspondiente valida `optionCount >= 3 && optionCount <= 4` en vez de un literal exacto, para no sobre-especificar un valor que el propio ADR deja abierto.
2. **`nonChromaticKeyRequired` usa los 9 valores de `family.model.ColorVisionMode`, no solo NONE/DEUTERANOPIA/ACHROMATOPSIA.** El doc mencionaba esos 3 a modo de ejemplo; la regla real implementada es `category == COLOR && colorVisionMode != NONE`, que cubre also PROTANOPIA/PROTANOMALY/DEUTERANOMALY/TRITANOPIA/TRITANOMALY/ACHROMATOMALY sin necesidad de listarlos explícitamente.
3. **Nueva dependencia de módulo `game → family`** (import de `family.model.ColorVisionMode` en `RecognitionDifficultyService`). No existía previamente en `game`, pero hay precedente directo: `content` ya depende de `family.model.ColorVisionMode` (vía `AccessibleColorPalette`), y `game` ya depende de `content` extensamente. Es un import de un enum de dominio puro, sin acoplar servicios ni introducir dependencias circulares.
4. **SHAPE sin fila propia en la ladder:** se resuelve tratándola igual que LETTER/NUMBER/ANIMAL — los 4 parámetros base dependen solo de `DifficultyCode`, nunca de la categoría; únicamente `nonChromaticKeyRequired` varía por categoría (solo para COLOR). Esto resuelve el pendiente abierto en FEAT-011 sin necesidad de una decisión de producto adicional.
5. **`RecognitionEngine` y `GameOrchestratorService` no se tocaron**, tal como indica "Agent Instruction". `RecognitionDifficultyService` queda registrado como bean de Spring pero sin consumidores todavía — la integración con el motor de reconocimiento (que hoy se instancia con `new RecognitionEngine()`, fuera del contexto de Spring) es explícitamente de un sprint posterior (098+).

## Notes
- Este sprint sienta la base para los sprints 098-102.
- La ladder de ADR-028 es la fuente de verdad para los valores por defecto.
- `colorVisionMode` ya existe en `ChildProfile` (family module). No se modifica en este sprint.

## Verificación
- `mvn -o compile`: BUILD SUCCESS.
- `mvn -o test-compile`: BUILD SUCCESS (sin impacto en el resto del módulo `game`).
- `mvn -o test -Dtest=RecognitionDifficultyServiceTest`: 7 tests ejecutados, 0 fallos.
- Confirmado que `RecognitionEngine.java` y `GameOrchestratorService.java` no fueron modificados.

## Review

### Developer implementation — Evidencias

#### Resumen técnico verificado

1. **`DistractorStrategy`**: enum de 3 valores (`SEMANTICALLY_FAR`, `SAME_CATEGORY`, `SIMILAR_OUTLINE`) en `game/model/recognition/`, tal como se pidió.
2. **`RoundParameters`**: record con los 5 campos exactos del diseño (`optionCount`, `distractorStrategy`, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`).
3. **`RecognitionDifficultyConfig`**: `Map<DifficultyCode, LadderTier>` (`EnumMap`) poblado en el constructor con los 3 niveles; `LadderTier` es un record anidado con los 4 valores base. `tierFor(DifficultyCode)` expone la consulta. Valores verificados contra la ladder de ADR-028 §4: EASY=(2, SEMANTICALLY_FAR, true, 500ms), MEDIUM=(3, SAME_CATEGORY, false, 800ms), HARD=(4, SIMILAR_OUTLINE, false, 0ms) — coinciden exactamente con la tabla del ADR.
4. **`RecognitionDifficultyService.resolveRoundParameters(...)`**: delega los 4 valores base a `tierFor(difficultyCode)` y calcula `nonChromaticKeyRequired = category == COLOR && colorVisionMode != NONE` de forma independiente. Esta regla coincide literalmente con el texto de ADR-028 §4 ("cuando exista una preferencia visual de color configurada para el perfil, los distractores necesitan una clave adicional al matiz") y con el criterio de aceptación 7 de FEAT-011.
5. **`colorVisionMode` con 9 valores**: confirmado que el enum `family.model.ColorVisionMode` tiene exactamente los 9 valores citados (`NONE` + 8 tipos de daltonismo/acromatopsia). La condición `!= NONE` cubre los 8 sin necesidad de listarlos.
6. **Nueva dependencia `game → family`**: confirmado el precedente citado — `content` ya importa `family.model.ColorVisionMode` en 6 archivos (p. ej. `AccessibleColorPalette.java`), y `game` ya depende de `content` extensamente. Es un import de un enum de dominio puro; no hay ciclo ni acoplamiento de servicios.
7. **Beans de Spring**: `GameModuleConfiguration` gana `recognitionDifficultyConfig()` y `recognitionDifficultyService(RecognitionDifficultyConfig)`, ambos registrados correctamente y sin tocar el bean existente de `GameOrchestratorService`.
8. **SHAPE sin fila propia**: confirmado por diseño — los 4 parámetros base solo dependen de `DifficultyCode`; `nonChromaticKeyRequired` es el único que varía por categoría (y solo para COLOR). El test de igualdad `SHAPE` vs `ANIMAL` lo demuestra.
9. **Alcance respetado**: `git diff` confirma que `RecognitionEngine.java`, `GameOrchestratorService.java` y `docs/contracts/` no fueron tocados, tal como exige "Agent Instruction".

#### Pruebas ejecutadas (verificación independiente)

```
mvn -o test -Dtest=RecognitionDifficultyServiceTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

```
mvn -o test -Dtest="es.vargontoc.educational.framework.game.**"
Tests run: 153, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```
Sin regresiones en el resto del módulo `game` (incluye `RecognitionEngineTest`, `GameOrchestratorServiceTest` y variantes, todos verdes).

#### Criterios de aceptación cubiertos por tests

| Criterio (ADR-028 ladder / FEAT-011 §4) | Test | Estado |
|---|---|---|
| EASY: 2 opciones, SEMANTICALLY_FAR, cromo guía, espera 500ms | `resolveRoundParameters_easyLetter_returnsEasyTierWithoutNonChromaticKey` | ✅ |
| MEDIUM: 3 opciones, SAME_CATEGORY, sin cromo, espera 800ms | `resolveRoundParameters_mediumAnimal_returnsMediumTierWithoutNonChromaticKey` | ✅ |
| HARD: 3-4 opciones (rango, no valor exacto), SIMILAR_OUTLINE, sin espera | `resolveRoundParameters_hardNumber_returnsHardTierWithoutNonChromaticKey` | ✅ |
| COLOR sin preferencia configurada → sin clave no cromática | `resolveRoundParameters_easyColorWithoutVisionPreference_doesNotRequireNonChromaticKey` | ✅ |
| COLOR con preferencia (DEUTERANOPIA) → clave no cromática requerida | `resolveRoundParameters_mediumColorWithDeuteranopia_requiresNonChromaticKey` | ✅ |
| COLOR con preferencia (ACHROMATOPSIA) → clave no cromática requerida | `resolveRoundParameters_hardColorWithAchromatopsia_requiresNonChromaticKey` | ✅ |
| SHAPE se comporta igual que otras categorías no-COLOR | `resolveRoundParameters_shape_behavesLikeOtherNonColorCategories` | ✅ |

### Reviewer verification

**Veredicto: APPROVED**

Sprint pequeño, autocontenido y ejecutado con precisión. Los valores de la ladder, la lógica de `nonChromaticKeyRequired` y el alcance (sin tocar `RecognitionEngine`/`GameOrchestratorService`/contratos) coinciden exactamente con ADR-028 y FEAT-011. Las 4 decisiones documentadas están bien justificadas y no introducen ambigüedad. No se detectaron defectos, código muerto ni cambios fuera de alcance.

#### Observaciones (no bloqueantes)

- **FEAT-011 "Decisiones pendientes" #1** ("confirmar si los patrones o texturas de color se muestran siempre o solo con preferencia visual configurada") sigue formalmente abierta a nivel de producto. La implementación de este sprint (`nonChromaticKeyRequired` solo si hay preferencia configurada) es la lectura correcta del texto normativo ya aprobado en ADR-028 §4, así que no hay contradicción — pero como el servicio todavía no tiene consumidores (confirmado: `RecognitionEngine` no lo usa aún), vale la pena que quien resuelva la pendiente de producto revise si esta interpretación sigue siendo la deseada antes de que Sprint 098+ la conecte al motor real.
- El campo `colorVisionMode` puede ser técnicamente `null` en la firma de `resolveRoundParameters(...)` (no hay validación de nulidad); en producción esto no ocurre porque `child_profile.color_vision_mode` tiene `NOT NULL DEFAULT 'NONE'` a nivel de BD (migración `022`), pero si el servicio llega a usarse alguna vez con un `ChildProfile` en memoria sin ese campo poblado, `null != NONE` evaluaría a `true` (activaría la clave no cromática por defecto). Comportamiento razonable como fail-safe, no requiere cambio.
- Nit de documentación: "Manual Tests" indica `mvn test -pl framework/backend -Dtest=...`, pero `framework/backend` no es un submódulo de un reactor Maven multi-módulo (su `pom.xml` es autónomo) — el comando correcto sería ejecutarlo desde ese directorio sin `-pl`, o con `-f framework/backend/pom.xml`. No afecta al resultado ya verificado.
