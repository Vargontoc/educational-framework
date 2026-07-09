# Sprint 062 - backend
# -----------------------------------------------

## Goal
Add `colorVisionMode` to the child profile so recognition content can be rendered accessibly without changing `RecognitionEngine`.

## Status
status: completed
started_at: 2026-07-07
closed_at: 2026-07-07
blocked_by:
waiting_for:

## Tasks

### Family Model
- [x] Add a `colorVisionMode` enum or value object to the family/child profile domain.
- [x] Support at least `NONE`, `PROTANOPIA`, `DEUTERANOMALY`, `DEUTERANOPIA`, `TRITANOPIA`, and `ACHROMATOPSIA`, adapting names to existing enum conventions.
- [x] Set `NONE` as the default for existing/new child profiles.
- [x] Expose `colorVisionMode` through the existing child profile read model or DTO.
- [x] Allow parent configuration updates through the existing child profile update flow if that flow exists.

### Persistence And Contract
- [x] Add a new Liquibase migration if child profile data is persisted in the database.
- [x] Update REST contract docs only if an external child profile endpoint changes.
- [x] Do not add recognition-specific logic to the family module.

### Tests
- [x] Unit test default `colorVisionMode` is `NONE`.
- [x] Unit test child profile can store and return a non-default color vision mode.
- [x] Integration or controller test for profile update/read if an external endpoint is changed.

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
- Created ColorVisionMode enum with NONE, PROTANOPIA, DEUTERANOMALY, DEUTERANOPIA, TRITANOPIA, ACHROMATOPSIA
- Created migration 022 adding color_vision_mode column with default NONE
- Updated ChildProfile domain model with colorVisionMode field
- Updated ChildProfileJpaEntity with @Enumerated(EnumType.STRING) column
- Updated CreateChildProfileRequest (optional colorVisionMode, defaults to NONE server-side)
- Updated UpdateChildProfileRequest (nullable colorVisionMode, preserves existing if null)
- Updated ChildProfileResponse to expose colorVisionMode
- Updated ChildProfileUseCase interface with new parameter
- Updated ChildProfileService createChild (defaults to NONE) and updateChild (preserves existing)
- Updated ChildProfilePersistenceAdapter mapper methods
- Updated ChildProfileController endpoints and toResponse
- Added 4 new unit tests for colorVisionMode behavior
- Added 2 new integration tests for colorVisionMode create/update
- Updated db.changelog-master.xml with new migration

incomplete_tasks:
- GameWebSocketHandlerTest has pre-existing compilation issue (unrelated to this sprint)

contract_changes:
- ChildProfile create endpoint: colorVisionMode added as optional field
- ChildProfile update endpoint: colorVisionMode added as optional field
- ChildProfile response: colorVisionMode added as required field

learnings:
- Design decision: colorVisionMode optional in Create (defaults to NONE) vs nullable in Update (preserves existing)
- This approach maintains backward compatibility for create while allowing selective updates

next_sprint_suggestions:
- FEAT-009 recognition engine: implement color palette adaptation based on child colorVisionMode
- Consider adding colorVisionMode validation if specific modes are not supported by content
