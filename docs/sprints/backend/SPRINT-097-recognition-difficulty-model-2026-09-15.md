# Sprint 097 - backend
# -----------------------------------------------

## Goal
Crear el modelo de dificultad de `RecognitionEngine` con parámetros configurables por nivel (EASY/MEDIUM/HARD) y categoría (LETRA/NÚMERO/FORMA/COLOR/ANIMAL), conforme a la ladder de ADR-028 y FEAT-011.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Domain Model
- [ ] Crear enum `DistractorStrategy` con valores `SEMANTICALLY_FAR`, `SAME_CATEGORY`, `SIMILAR_OUTLINE` en `game/model/recognition/`.
- [ ] Crear record `RoundParameters` en `game/model/recognition/` con campos: `optionCount` (int), `distractorStrategy` (DistractorStrategy), `guideChromEnabled` (boolean), `touchEnableDelayMs` (int), `nonChromaticKeyRequired` (boolean).
- [ ] Crear clase `RecognitionDifficultyConfig` en `game/model/recognition/` con valores por defecto de la ladder (no hardcodeados en la lógica, sino como configuración inyectable).

### Application Service
- [ ] Crear `RecognitionDifficultyService` en `game/service/` con método `resolveRoundParameters(DifficultyCode, RecognitionCategory, ColorVisionMode)` que devuelve `RoundParameters`.
- [ ] Implementar lógica de resolución de `optionCount`: EASY=2, MEDIUM=3, HARD=3-4 (configurable).
- [ ] Implementar lógica de resolución de `distractorStrategy`: EASY=SEMANTICALLY_FAR, MEDIUM=SAME_CATEGORY, HARD=SIMILAR_OUTLINE.
- [ ] Implementar lógica de `guideChromEnabled`: true solo en EASY, false en MEDIUM y HARD.
- [ ] Implementar lógica de `touchEnableDelayMs`: EASY=500, MEDIUM=800, HARD=0 (configurable).
- [ ] Implementar lógica de `nonChromaticKeyRequired`: true si categoría es COLOR y `colorVisionMode` no es NONE, false en resto de casos.

### Spring Configuration
- [ ] Registrar `RecognitionDifficultyService` como bean de Spring en `GameModuleConfiguration`.

### Tests
- [ ] Test unitario: EASY + LETTER → 2 opciones, SEMANTICALLY_FAR, guideChrom=true, delay=500, nonChromatic=false.
- [ ] Test unitario: MEDIUM + ANIMAL → 3 opciones, SAME_CATEGORY, guideChrom=false, delay=800, nonChromatic=false.
- [ ] Test unitario: HARD + NUMBER → 3-4 opciones, SIMILAR_OUTLINE, guideChrom=false, delay=0, nonChromatic=false.
- [ ] Test unitario: EASY + COLOR + NONE → nonChromatic=false.
- [ ] Test unitario: MEDIUM + COLOR + DEUTERANOPIA → nonChromatic=true.
- [ ] Test unitario: HARD + COLOR + ACHROMATOPSIA → nonChromatic=true.
- [ ] Test unitario: SHAPE se comporta igual que LETTER/NUMBER/ANIMAL para parámetros base.

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

## Notes
- Este sprint sienta la base para los sprints 098-102.
- La ladder de ADR-028 es la fuente de verdad para los valores por defecto.
- `colorVisionMode` ya existe en `ChildProfile` (family module). No se modifica en este sprint.
