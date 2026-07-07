# Sprint 062 - backend
# -----------------------------------------------

## Goal
Add `colorVisionMode` to the child profile so recognition content can be rendered accessibly without changing `RecognitionEngine`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Family Model
- [ ] Add a `colorVisionMode` enum or value object to the family/child profile domain.
- [ ] Support at least `NONE`, `PROTANOPIA`, `DEUTERANOMALY`, `DEUTERANOPIA`, `TRITANOPIA`, and `ACHROMATOPSIA`, adapting names to existing enum conventions.
- [ ] Set `NONE` as the default for existing/new child profiles.
- [ ] Expose `colorVisionMode` through the existing child profile read model or DTO.
- [ ] Allow parent configuration updates through the existing child profile update flow if that flow exists.

### Persistence And Contract
- [ ] Add a new Liquibase migration if child profile data is persisted in the database.
- [ ] Update REST contract docs only if an external child profile endpoint changes.
- [ ] Do not add recognition-specific logic to the family module.

### Tests
- [ ] Unit test default `colorVisionMode` is `NONE`.
- [ ] Unit test child profile can store and return a non-default color vision mode.
- [ ] Integration or controller test for profile update/read if an external endpoint is changed.

## Manual Tests
- If the profile API is changed, update a child profile with a non-default `colorVisionMode` and read it back through the existing dev/API flow.

## Risks
- Existing child profile update DTOs may require contract updates for frontend.
- Enum naming must be stable before frontend consumes it.

## Dependencies
- FEAT-009 section `Accesibilidad — modo de visión de color`.
- Existing family/child profile module.

## Agent Instruction
- Keep this sprint limited to profile preference storage and exposure.
- Do not implement content palettes, recognition engine behavior, or frontend rendering here.
- Never hardcode values that belong in existing environment configuration.
- Keep code, comments, and names in English.

## Notes
`colorVisionMode` is a cross-module prerequisite. The recognition motor must remain unaware of color accessibility rendering.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
