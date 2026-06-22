# Sprint 052 - backend
# -----------------------------------------------

## Goal
Implement the in-memory `WorldStateRegistry` used to store one active World Map runtime state per child session.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Registry
- [ ] Add `WorldStateRegistry` port/interface.
- [ ] Add in-memory implementation backed by a map.
- [ ] Store and retrieve by `childSessionId`.
- [ ] Enforce at most one active world state per child session.
- [ ] Remove state when world session closes or child session ends.

### Tests
- [ ] Unit test save and find by child session id.
- [ ] Unit test replacing or rejecting duplicate active state follows documented behavior.
- [ ] Unit test remove closed state.
- [ ] Unit test missing state returns empty result.

## Manual Tests
- Not required. This is an in-memory infrastructure sprint.

## Risks
- Persisting `WorldState` would contradict FEAT-008 v1.
- Multiple active states per child session would make proposals ambiguous.

## Dependencies
- Sprint 051 completed.

## Agent Instruction
- Do not add Redis or database persistence.
- Keep implementation simple for single-family usage.
- Make behavior deterministic and unit-testable.

## Notes
World state is runtime-only, like game state, and is lost on backend process restart.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
