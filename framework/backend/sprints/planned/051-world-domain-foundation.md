# Sprint 051 - backend
# -----------------------------------------------

## Goal
Create the pure `world` domain model for runtime World Map state without persistence, WebSocket, or external side effects.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create `world` package following backend hexagonal structure.
- [ ] Add `WorldState` with `childSessionId`, current destination, current host, visible discovery elements, `lastWorldActivityAt`, and pending proposal reference.
- [ ] Add `WorldDestination` model.
- [ ] Add `WorldDiscoveryProposal` model.
- [ ] Add `WorldRuntimeStatus` enum for active/closed states.
- [ ] Add `WorldEngagementWindow` model for per-session abandoned-engine observations.
- [ ] Ensure no child-facing diagnostic labels exist in domain outputs.

### Tests
- [ ] Unit test `WorldState` can track `lastWorldActivityAt`.
- [ ] Unit test pending proposal can be attached and cleared.
- [ ] Unit test domain model does not include persistent ids for child progress owned by tracking.
- [ ] Unit test engagement window can record abandonment by `engineType`.

## Manual Tests
- Not required. This is a pure domain sprint.

## Risks
- Adding persistence too early would violate FEAT-008.
- Over-modeling narrative data can duplicate content ownership.

## Dependencies
- FEAT-008 World Module.

## Agent Instruction
- Keep classes framework-free where practical.
- Do not add JPA entities for world runtime state.
- Do not implement WebSocket handling.
- Do not call content, tracking, game, session, avatar, or TTS.

## Notes
This sprint defines the vocabulary needed by later world orchestration sprints.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
