# Sprint 046 - backend
# -----------------------------------------------

## Goal
Expose internal content read ports for World Map catalog data so `world` can query active hosts, situations, discovery elements, and compatible activities without reading content persistence directly.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Model Properties

### WorldHostProjection

Internal content projection returned to `world`. It must not be a JPA entity.

- `id`: Long, required.
- `code`: String, required.
- `displayName`: String, required.
- `biome`: String or enum, required.
- `description`: String, nullable.
- `minAge`: Integer, required.
- `maxAge`: Integer, required.
- `visualAssetKey`: String, nullable.
- `sortOrder`: Integer, nullable.

### WorldNarrativeSituationProjection

Internal content projection for narrative destination construction.

- `id`: Long, required.
- `code`: String, required.
- `displayText`: String, required.
- `situationType`: String or enum, required.
- `tone`: String or enum, nullable.
- `minAge`: Integer, required.
- `maxAge`: Integer, required.
- `sortOrder`: Integer, nullable.

### WorldDiscoveryElementProjection

Internal content projection for elements shown during the walk.

- `id`: Long, required.
- `code`: String, required.
- `displayName`: String, required.
- `elementType`: String or enum, required: `DECORATIVE`, `SIMPLE_INTERACTIVE`, `DISCOVERY`.
- `biome`: String or enum, required.
- `minAge`: Integer, required.
- `maxAge`: Integer, required.
- `activityId`: Long, nullable. Only present for playable discovery elements.
- `topicId`: Long, nullable.
- `visualAssetKey`: String, nullable.
- `interactionCueType`: String or enum, nullable.
- `sortOrder`: Integer, nullable.

### CompatibleActivityProjection

Internal content projection used by `world` to choose an activity for a selected topic.

- `activityId`: Long, required.
- `activityCode`: String, nullable if current Activity model has no code.
- `displayName`: String, required if available in content.
- `engineType`: String or enum, required.
- `topicIds`: List<Long>, required, can be empty only for non-topic activities.
- `minAge`: Integer, required.
- `maxAge`: Integer, required.
- `difficultyLevelIds`: List<Long>, required, can be empty if difficulty is resolved elsewhere.

### Query Rules

- Only `ACTIVE` catalog rows are returned.
- Records must match the requested age: `minAge <= targetAge <= maxAge`.
- Internal projections must not expose persistence entities.
- Internal projections must not include child progress, proposal outcomes, ignored counts, or engagement labels.

## Tasks

### Content Ports
- [ ] Add internal query/use case to list active world hosts for a target age.
- [ ] Add internal query/use case to list active narrative situations for a target age.
- [ ] Add internal query/use case to list active discovery elements for a target age.
- [ ] Add internal query/use case to find active activities compatible with a `topicId`.
- [ ] Ensure inactive/draft content is never returned to `world`.

### DTO/Domain Projection
- [ ] Return lightweight content projections with the properties listed in `Model Properties`.
- [ ] Keep frontend-facing REST DTOs separate from internal world projections.
- [ ] Apply the query rules listed in `Model Properties`.

### Tests
- [ ] Unit test active host lookup by age.
- [ ] Unit test inactive host is excluded.
- [ ] Unit test active activity lookup by topic.
- [ ] Unit test missing compatible activity returns an empty list, not an exception.

## Manual Tests
- Optional: use a dev fixture or test runner to query active world catalog data.
- Confirm only active records in the correct age range are returned.

## Risks
- Exposing public REST endpoints by accident would expand FEAT-008 scope.
- Returning full JPA entities could leak content persistence details to `world`.

## Dependencies
- Sprint 045 completed.
- Sprint 035 content game catalog readiness completed.

## Agent Instruction
- Keep this sprint inside content/application boundaries.
- Do not add world package code yet.
- Do not add public REST endpoints unless a separate frontend/content feature requires them.

## Notes
This sprint gives `world` safe read access to static data while preserving content ownership.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
