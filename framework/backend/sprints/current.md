# Sprint 063 - backend
# -----------------------------------------------

## Goal
Prepare recognition content candidates, habitat metadata, and accessible color data needed by FEAT-009.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Recognition Catalog
- [ ] Confirm or add recognition category support for `LETTER`, `NUMBER`, `SHAPE`, `COLOR`, and `ANIMAL`.
- [ ] Ensure animal content can store a `habitatTag` without creating habitat sub-topics.
- [ ] Add or verify initial data coverage for Granja, Selva, and Mar animal habitats.
- [ ] Add or extend an internal query for active/game-ready candidates by category.
- [ ] Add optional candidate filtering by `habitatTag` for animal recognition.

### Accessible Color Data
- [ ] Model colors by conceptual identity instead of visual color value only.
- [ ] Add accessible palette metadata keyed by `colorVisionMode` or an equivalent content representation.
- [ ] Add a non-chromatic differentiator for COLOR content, such as icon, shape, label key, or equivalent metadata.
- [ ] Keep visual rendering decisions out of backend domain logic.

### Tests
- [ ] Unit test candidates can be filtered by category.
- [ ] Unit test animal candidates can be filtered by habitat tag.
- [ ] Unit test non-animal categories are not split into habitat sub-topics.
- [ ] Unit test COLOR content exposes conceptual identity and non-chromatic differentiator metadata.

## Manual Tests
- If seed data changes, start the backend locally and verify there is at least one game-ready item per recognition category.
- If a dev content API exists, query COLOR content and confirm accessible metadata is present.

## Risks
- Existing content model may already encode category/topic differently; adapt instead of duplicating fields.
- Color accessibility data can become frontend-specific if stored as concrete UI styling instead of content metadata.

## Dependencies
- Sprint 062 if content needs to reference the exact `colorVisionMode` enum names.
- Existing content module from backend sprints 009, 010, 014, and 035.

## Agent Instruction
- Keep the content module responsible for content metadata only.
- Do not implement `RecognitionEngine` in this sprint.
- Do not add frontend asset paths as backend domain fields.
- Keep code, comments, and names in English.

## Notes
This sprint supports the `Content Module` priority changes from FEAT-009.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
