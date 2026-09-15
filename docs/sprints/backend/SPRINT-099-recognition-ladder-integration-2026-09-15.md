# Sprint 099 - backend
# -----------------------------------------------

## Goal
Integrar `RecognitionDifficultyService` en el flujo del motor y el orquestador: aplicar la ladder de dificultad de extremo a extremo, exponer parámetros de ronda en `RecognitionState`, y resolver `colorVisionMode` para la categoría COLOR.

## Status
status: pending
started_at:
closed_at:
blocked_by: Sprint 097, Sprint 098
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### RecognitionState Extension
- [ ] Añadir campo `guideChromEnabled` (boolean) a `RecognitionState`.
- [ ] Añadir campo `touchEnableDelayMs` (int) a `RecognitionState`.
- [ ] Añadir campo `nonChromaticKeyRequired` (boolean) a `RecognitionState`.
- [ ] Añadir campo `distractorStrategy` (DistractorStrategy) a `RecognitionState`.
- [ ] Añadir getters/setters correspondientes.

### RecognitionEngine Integration
- [ ] Modificar `RecognitionEngine.initGame()` para aceptar `RoundParameters` vía `engineParams` (JSON).
- [ ] Almacenar `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired` y `distractorStrategy` en `RecognitionState` durante `initGame()`.
- [ ] Modificar `RecognitionEngine.buildOptions()` para usar `optionCount` y `distractorStrategy` de `RecognitionState`.
- [ ] Mantener `advanceRound()` consistente: al avanzar ronda, preservar los parámetros de dificultad.

### GameOrchestratorService Integration
- [ ] Inyectar `RecognitionDifficultyService` en `GameOrchestratorService`.
- [ ] Inyectar `ChildProfileRepository` (o puerto equivalente) para resolver `colorVisionMode`.
- [ ] Modificar `readyGame()` para:
    1. Resolver `DifficultyCode` desde `GameState.difficultyLevelId`.
    2. Resolver `colorVisionMode` desde `ChildProfile`.
    3. Llamar a `RecognitionDifficultyService.resolveRoundParameters()`.
    4. Incluir `RoundParameters` en `engineParams` JSON.
- [ ] Modificar `getEngineParams()` para incluir `roundParameters` en el JSON.
- [ ] Modificar `resolveCandidates()` para pasar `colorVisionMode` al servicio de dificultad cuando la categoría sea COLOR.

### WebSocket Event Extension
- [ ] Modificar `getNextElement()` en `RecognitionEngine` para incluir `guideChromEnabled`, `touchEnableDelayMs` y `nonChromaticKeyRequired` en el JSON de salida.
- [ ] Verificar que el evento WebSocket emitido por `GameWebSocketHandler` propaga estos campos al frontend.

### Tests
- [ ] Test unitario: `initGame()` almacena `RoundParameters` en `RecognitionState`.
- [ ] Test unitario: `getNextElement()` incluye `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`.
- [ ] Test de integración: `readyGame()` resuelve `RoundParameters` correctamente para EASY/MEDIUM/HARD.
- [ ] Test de integración: `resolveCandidates()` para COLOR con `colorVisionMode=DEUTERANOPIA` marca `nonChromaticKeyRequired=true`.
- [ ] Test de integración: `resolveCandidates()` para COLOR con `colorVisionMode=NONE` marca `nonChromaticKeyRequired=false`.
- [ ] Test de integración: `advanceRound()` preserva parámetros de dificultad entre rondas.

## Manual Tests
- Iniciar backend y abrir un minijuego de reconocimiento.
- Verificar en logs que `RoundParameters` se resuelven correctamente.
- Verificar que el evento WebSocket `ROUND_READY` incluye los nuevos campos.
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

## Notes
- Este sprint integra la ladder de extremo a extremo.
- Los parámetros de ronda se envían al frontend vía WebSocket, sin endpoint REST adicional (decisión confirmada).
- Sprint 100 completará el contenido de FORMAS y COLOR.
- Sprint 101 externalizará la configuración a `application.yml`.
