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

## Tasks

### Content Schema
- [ ] Add content domain model for world hosts, such as `WorldHost`.
- [ ] Add content domain model for narrative situations, such as `WorldNarrativeSituation`.
- [ ] Add content domain model for discovery elements, such as `WorldDiscoveryElement`.
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
