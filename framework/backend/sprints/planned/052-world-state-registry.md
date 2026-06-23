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

## Model Properties

### WorldStateRegistry

Port/interface for runtime-only world state storage.

- `save(WorldState state)`: stores or replaces a world state for `state.childSessionId`.
- `findByChildSessionId(Long childSessionId)`: returns optional active state.
- `existsByChildSessionId(Long childSessionId)`: returns true when active state exists.
- `removeByChildSessionId(Long childSessionId)`: removes state and returns removed state if useful.
- `clearClosed()`: optional helper only if useful for cleanup tests.

### InMemoryWorldStateRegistry

- Backing storage: `Map<Long, WorldState>` keyed by `childSessionId`.
- Persistence: none.
- Max expected size: small single-family usage.

### Behavior Rules

- At most one active `WorldState` per `childSessionId`.
- Saving a new active state for the same `childSessionId` may replace the previous state only if tests document the behavior.
- Missing state lookups return empty/optional, not exceptions.
- Removing a missing state is a no-op.
- Registry must not serialize or persist state to database.

## Tasks

### Registry
- [ ] Add `WorldStateRegistry` port/interface with the methods listed in `Model Properties`.
- [ ] Add `InMemoryWorldStateRegistry` backed by the map described in `Model Properties`.
- [ ] Store and retrieve by `childSessionId`.
- [ ] Enforce at most one active world state per child session.
- [ ] Remove state when world session closes or child session ends.
- [ ] Apply the behavior rules listed in `Model Properties`.

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
