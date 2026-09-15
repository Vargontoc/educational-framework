# Sprint 102 - backend
# -----------------------------------------------

## Goal
Actualizar FEAT-009 para reflejar el modelo de consolidación diferida (decisión 4 de FEAT-011) y crear los contratos WebSocket de `round-parameters` y `round-ready-event` en `docs/contracts`.

## Status
status: pending
started_at:
closed_at:
blocked_by: Sprint 099, Sprint 101
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### FEAT-009 Documentation Update
- [ ] Actualizar FEAT-009 §"Persistencia: ActivityAttempt vs GameSessionSummary" para reflejar el modelo de consolidación diferida.
- [ ] Añadir párrafo explicativo sobre buffer en memoria (`RecognitionState.roundAttempts`), flush al completar, y limpieza al cerrar sesión.
- [ ] Añadir referencia cruzada a FEAT-011 para la decisión de consolidación diferida.
- [ ] Verificar que FEAT-009 §"RecognitionAttemptContext" sigue siendo coherente con el nuevo modelo.

### WebSocket Contracts
- [ ] Crear `docs/contracts/schemas/round-parameters.v1.yaml` con el esquema de `RoundParameters`:
    - `optionCount` (integer)
    - `distractorStrategy` (enum: SEMANTICALLY_FAR, SAME_CATEGORY, SIMILAR_OUTLINE)
    - `guideChromEnabled` (boolean)
    - `touchEnableDelayMs` (integer)
    - `nonChromaticKeyRequired` (boolean)
- [ ] Crear `docs/contracts/schemas/round-ready-event.v1.yaml` con el esquema del evento WebSocket `ROUND_READY`:
    - `type` (const: "ROUND_READY")
    - `payload` (object con `targetElementId`, `optionIds`, `roundIndex`, `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`)
- [ ] Verificar que los contratos YAML son válidos y coherentes con la implementación.

### Handoff Documentation
- [ ] Documentar handoff a frontend en FEAT-011: contratos WebSocket que frontend debe consumir (`ROUND_READY` con parámetros de ronda).
- [ ] Documentar handoff a agents: Nubi no condiciona la resolución de ronda (sin cambios de contrato).
- [ ] Documentar handoff a contenido: catálogo de FORMAS y COLOR adaptado a la ladder (Sprint 100).

### Validation
- [ ] Verificar que no hay contradicciones entre FEAT-009 actualizado y FEAT-011.
- [ ] Verificar que los contratos de `docs/contracts` son coherentes con la implementación de Sprint 099.
- [ ] Verificar que `RecognitionState` serializado coincide con el esquema de `round-ready-event.v1.yaml`.

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
- La actualización de FEAT-009 es necesaria para mantener la coherencia documental tras la decisión 4 de FEAT-011.
