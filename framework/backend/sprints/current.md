# Sprint 045 - backend
# -----------------------------------------------

## Goal
Add the minimum static World Map catalog in content so `world` can build narrative destinations without owning catalog data.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Model Properties

### WorldHost

Static content-owned catalog entry for a host character shown as a World Map destination.

- `id`: Long, inherited from `BaseEntity`.
- `code`: String, required, unique, stable identifier. Example: `MEADOW_DOG`.
- `displayName`: String, required. Example: `Dog`.
- `biome`: String or enum, required. v1 value: `MEADOW`.
- `description`: String, optional, adult/admin-facing only.
- `minAge`: Integer, required. v1 seed: `3`.
- `maxAge`: Integer, required. v1 seed: `4`, future can support up to `8`.
- `status`: ContentStatus, required: `ACTIVE`, `INACTIVE`, `DRAFT`.
- `sortOrder`: Integer, optional, deterministic seed ordering.
- `visualAssetKey`: String, optional placeholder asset key.
- `createdAt`: inherited from `BaseEntity`.
- `updatedAt`: inherited from `BaseEntity`.

### WorldNarrativeSituation

Static content-owned catalog entry describing the narrative reason for visiting a host.

- `id`: Long, inherited from `BaseEntity`.
- `code`: String, required, unique, stable identifier. Example: `HOST_FOUND_SOMETHING`.
- `displayText`: String, required, short narrative/admin text.
- `situationType`: String or enum, required. Examples: `FOUND_OBJECT`, `WANTS_TO_SHOW`, `LOOKING_FOR`, `SAW_SOMETHING`.
- `tone`: String or enum, optional. Examples: `CALM`, `JOYFUL`, `CURIOUS`.
- `minAge`: Integer, required. v1 seed: `3`.
- `maxAge`: Integer, required. v1 seed: `4`, future can support up to `8`.
- `status`: ContentStatus, required: `ACTIVE`, `INACTIVE`, `DRAFT`.
- `sortOrder`: Integer, optional, deterministic seed ordering.
- `createdAt`: inherited from `BaseEntity`.
- `updatedAt`: inherited from `BaseEntity`.

### WorldDiscoveryElement

Static content-owned catalog entry for an element that can be shown during the walk.

- `id`: Long, inherited from `BaseEntity`.
- `code`: String, required, unique, stable identifier. Example: `MEADOW_SHINY_FLOWER`.
- `displayName`: String, required, admin-facing.
- `elementType`: String or enum, required: `DECORATIVE`, `SIMPLE_INTERACTIVE`, `DISCOVERY`.
- `biome`: String or enum, required. v1 value: `MEADOW`.
- `minAge`: Integer, required. v1 seed: `3`.
- `maxAge`: Integer, required. v1 seed: `4`, future can support up to `8`.
- `status`: ContentStatus, required: `ACTIVE`, `INACTIVE`, `DRAFT`.
- `activityId`: Long, nullable. Only allowed when `elementType = DISCOVERY`.
- `topicId`: Long, nullable. Optional hint for future filtering.
- `visualAssetKey`: String, optional placeholder asset key.
- `interactionCueType`: String or enum, optional. Example: `BREATHING_GLOW`.
- `sortOrder`: Integer, optional, deterministic seed ordering.
- `createdAt`: inherited from `BaseEntity`.
- `updatedAt`: inherited from `BaseEntity`.

### Validation Rules

- `code` must be unique per table.
- `minAge <= maxAge`.
- `status` must never be null.
- v1 seed data must target ages `3-4`.
- `WorldDiscoveryElement.activityId` is only allowed for `elementType = DISCOVERY`.
- `DECORATIVE` and `SIMPLE_INTERACTIVE` elements must not reference `activityId`.
- Content must not store child-specific state, ignored counts, started counts, progress, or engagement data.
- Frontend child-facing labels like `ignored`, `abandoned`, `low engagement`, or diagnostic text must not exist in these models.

## Tasks

### Content Schema
- [ ] Add `WorldHost` domain model with the properties listed in `Model Properties`.
- [ ] Add `WorldNarrativeSituation` domain model with the properties listed in `Model Properties`.
- [ ] Add `WorldDiscoveryElement` domain model with the properties listed in `Model Properties`.
- [ ] Add validation rules listed in `Model Properties`.
- [ ] Include status fields so inactive/draft world catalog records are not used at runtime.
- [ ] Include age range fields to support v1 ages 3-4 and future growth up to age 8.
- [ ] Add Liquibase migration for the new content tables.
- [ ] Add seed data for one v1 meadow host, one narrative situation, and one discovery element.

### Tests
- [ ] Unit test world host validation.
- [ ] Unit test narrative situation validation.
- [ ] Unit test discovery element validation.
- [ ] Persistence/integration test saves and reads world catalog records if Testcontainers is available.

## Manual Tests
- Start backend locally with seed loading enabled.
- Verify at least one active host, one active narrative situation, and one active discovery element exist.
- Verify inactive/draft records are not considered playable/usable by runtime queries once Sprint 046 is implemented.

## Risks
- Over-designing the catalog could block a junior developer.
- Mixing runtime progression into content would violate FEAT-008.
- Missing age range fields would make future 5-8 support harder.

## Dependencies
- FEAT-003 Content Module.
- FEAT-008 World Module.

## Agent Instruction
- Keep this sprint content-only.
- Do not implement world runtime logic.
- Do not store child-specific progress or interaction data in content.
- Keep seed data minimal and idempotent.

## Notes
This is the first preparation sprint for FEAT-008 because `world` must consume static catalog data owned by content.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
