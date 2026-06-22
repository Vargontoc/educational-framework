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

## Tasks

### Content Ports
- [ ] Add internal query/use case to list active world hosts for a target age.
- [ ] Add internal query/use case to list active narrative situations for a target age.
- [ ] Add internal query/use case to list active discovery elements for a target age.
- [ ] Add internal query/use case to find active activities compatible with a `topicId`.
- [ ] Ensure inactive/draft content is never returned to `world`.

### DTO/Domain Projection
- [ ] Return lightweight content projections with ids and display metadata only.
- [ ] Keep frontend-facing REST DTOs separate from internal world projections.

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
