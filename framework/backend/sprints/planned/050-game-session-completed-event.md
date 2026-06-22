# Sprint 050 - backend
# -----------------------------------------------

## Goal
Publish a domain event when a game session completes or is abandoned so `world` can react without `game` depending on `world`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Event
- [ ] Add `GameSessionCompletedEvent` or equivalent application event.
- [ ] Include `gameId`, `childSessionId`, `activityId`, and `finalStatus`.
- [ ] Publish the event on `COMPLETED` transition.
- [ ] Publish the event on `ABANDONED` transition, including system and client abandonment paths.
- [ ] Use Spring `ApplicationEventPublisher` or existing application event pattern.

### Boundary
- [ ] Ensure `game` does not reference `world` packages.
- [ ] Ensure listeners are optional; game works if nobody listens.

### Tests
- [ ] Unit test event is published on completed game.
- [ ] Unit test event is published on abandoned game.
- [ ] Unit test no event is published for normal in-progress actions.
- [ ] Unit test game has no dependency on `world`.

## Manual Tests
- Not required. This is internal event plumbing.

## Risks
- Missing client-requested abandon path would make world miss some returns to map.
- Coupling game to world would violate FEAT-008.

## Dependencies
- Sprint 040 completed.
- Sprint 043 completed.
- Sprint 044 completed.

## Agent Instruction
- Keep event payload minimal.
- Do not implement `world` listener in this sprint.
- Do not add external message broker infrastructure.

## Notes
FEAT-008 depends on asynchronous game completion notification because a game can last minutes.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
