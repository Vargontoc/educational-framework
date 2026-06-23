# Sprint 050 - backend
# -----------------------------------------------

## Goal
Publish a domain event when a game session completes or is abandoned so `world` can react without `game` depending on `world`.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Model Properties

### GameSessionCompletedEvent

Internal application event published by `game`. It must not depend on `world`.

- `gameId`: Long, required.
- `childSessionId`: Long, required.
- `activityId`: Long, required.
- `finalStatus`: GameSessionFinalStatus or equivalent enum, required: `COMPLETED`, `ABANDONED`.
- `occurredAt`: timestamp/string following existing backend timestamp pattern, required.

### Event Rules

- Publish once for each transition to `COMPLETED`.
- Publish once for each transition to `ABANDONED`, including system and client abandonment paths.
- Do not publish for `WAITING`, `STARTING`, or `IN_PROGRESS`.
- The event is internal only; it does not update `docs/contracts/api/websocket.json` by itself.
- `game` must not import or reference `world` packages.

## Tasks

### Domain Event
- [x] Add `GameSessionCompletedEvent` or equivalent application event with the properties listed in `Model Properties`.
- [x] Publish the event on `COMPLETED` transition.
- [x] Publish the event on `ABANDONED` transition, including system and client abandonment paths.
- [x] Use Spring `ApplicationEventPublisher` or existing application event pattern.
- [x] Apply the event rules listed in `Model Properties`.

### Boundary
- [x] Ensure `game` does not reference `world` packages.
- [x] Ensure listeners are optional; game works if nobody listens.

### Tests
- [x] Unit test event is published on completed game.
- [x] Unit test event is published on abandoned game.
- [x] Unit test event is published on abandoned game via abandonGameForSession (system abandonment).
- [x] Unit test no event is published for normal in-progress actions.
- [x] Unit test game has no dependency on `world`.

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
- Created `GameSessionCompletedEvent` record in `game/model/event/`
- Used tracking's `GameSessionFinalStatus` enum directly in the event (no new enum needed)
- Added `ApplicationEventPublisher` dependency to `GameOrchestratorService`
- Modified `processAction()` to publish event on COMPLETED
- Modified `abandonGame()` to publish event on ABANDONED (client)
- Modified `abandonGameForSession()` to publish event on ABANDONED (system)
- Created `publishGameCompletedEvent()` helper method with try-catch for fault tolerance
- Updated `GameModuleConfiguration` to wire `ApplicationEventPublisher`
- Added 3 new unit tests verifying event publication (21 tests pass, all green)

incomplete_tasks:
- None

contract_changes:
- None (internal Spring application event only)

learnings:
- Using existing tracking's `GameSessionFinalStatus` avoids creating a duplicate enum
- Spring's `ApplicationEventPublisher` provides sufficient event propagation for internal use
- Fault-tolerant event publishing (try-catch) ensures game flow continues even if listener fails

next_sprint_suggestions:
- Sprint 051: World domain foundation
