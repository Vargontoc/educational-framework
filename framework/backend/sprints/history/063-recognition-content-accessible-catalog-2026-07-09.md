# Sprint 063 - backend
# -----------------------------------------------

## Goal
Prepare recognition content candidates, habitat metadata, and accessible color data needed by FEAT-009.

## Status
status: completed
started_at: 2026-07-09
closed_at: 2026-07-09
blocked_by:
waiting_for:

## Tasks

### Recognition Catalog
- [x] Confirm or add recognition category support for `LETTER`, `NUMBER`, `SHAPE`, `COLOR`, and `ANIMAL`.
- [x] Ensure animal content can store a `habitatTag` without creating habitat sub-topics.
- [x] Add or verify initial data coverage for Granja, Selva, and Mar animal habitats.
- [x] Add or extend an internal query for active/game-ready candidates by category.
- [x] Add optional candidate filtering by `habitatTag` for animal recognition.

### Accessible Color Data
- [x] Model colors by conceptual identity instead of visual color value only.
- [x] Add accessible palette metadata keyed by `colorVisionMode` or an equivalent content representation.
- [x] Add a non-chromatic differentiator for COLOR content, such as icon, shape, label key, or equivalent metadata.
- [x] Keep visual rendering decisions out of backend domain logic.

### Tests
- [x] Unit test candidates can be filtered by category.
- [x] Unit test animal candidates can be filtered by habitat tag.
- [x] Unit test non-animal categories are not split into habitat sub-topics.
- [x] Unit test COLOR content exposes conceptual identity and non-chromatic differentiator metadata.

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
- Created RecognitionType enum with LETTER, NUMBER, SHAPE, COLOR, ANIMAL
- Expanded Biome enum with GRANJA, SELVA, MAR
- Extended Topic model with recognitionType and habitatTag fields
- Created migration 023 adding recognition_type and habitat_tag columns to topic table
- Extended TopicRepository and TopicService with findByRecognitionType and findByRecognitionTypeAndHabitatTag methods
- Created AccessibleColor model with conceptualIdentity, labelKey, shapeIcon, symbol fields
- Created AccessibleColorPalette model linking colors to ColorVisionMode with accessible color values
- Created JPA entities, repositories, and persistence adapters for accessible colors
- Created migration 024 with accessible_color and accessible_color_palette tables
- Updated seeds/02-topics.json with recognitionType metadata for existing topics
- Created seeds/15-accessible-colors.json with sample color data and palettes
- Updated SeedService and SeedData to support loading accessible color seed data
- Updated ContentModuleConfiguration with new repository dependencies
- Added unit tests for new TopicService filtering methods

incomplete_tasks:
- GameWebSocketHandlerTest has pre-existing compilation issue (unrelated to this sprint)

contract_changes:
- Topic entity now has recognitionType and habitatTag fields
- New accessible_color and accessible_color_palette tables added
- Seed data updated with recognition metadata and accessible color palettes

learnings:
- Adding seed data support requires updating multiple layers: SeedData records, SeedService constructor and methods, ContentModuleConfiguration
- Using Biome enum for habitatTag couples topic habitat to world biome - could be decoupled in future if needed
- Accessible color palettes store color values per ColorVisionMode, allowing flexible rendering by frontend

next_sprint_suggestions:
- FEAT-009 recognition engine: implement RecognitionEngine using the new recognition type metadata
- Consider adding LETTER recognition type support with dedicated content
- Consider expanding animal habitat coverage in seeds with more WorldHost entries
