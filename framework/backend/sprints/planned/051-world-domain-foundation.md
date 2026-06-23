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

## Model Properties

### WorldState

Runtime-only state for the child's active World Map walk. It is not persisted.

- `childSessionId`: Long, required.
- `childProfileId`: Long, required if available from session context.
- `status`: WorldRuntimeStatus, required.
- `currentDestination`: WorldDestination, nullable until first destination is built.
- `visibleDiscoveryElements`: List<WorldDiscoveryProposal>, required, can be empty.
- `pendingProposalId`: Long, nullable, tracking-owned `ActivityProposalLog` id.
- `lastWorldActivityAt`: timestamp/string following existing backend timestamp pattern, required.
- `createdAt`: timestamp/string, required.
- `updatedAt`: timestamp/string, required.

### WorldDestination

Runtime narrative destination built from content catalog and tracking topic selection.

- `destinationId`: String or Long, required, runtime correlation id.
- `hostId`: Long, required, content-owned host id.
- `hostCode`: String, required.
- `hostDisplayName`: String, required.
- `narrativeSituationId`: Long, required.
- `narrativeSituationCode`: String, required.
- `displayText`: String, nullable, short narrative/admin text.
- `biome`: String or enum, required.
- `discoveryProposals`: List<WorldDiscoveryProposal>, required, can be empty.

### WorldDiscoveryProposal

Runtime proposal shown to the child. It must not expose tracking outcome labels.

- `proposalRuntimeId`: String or Long, required.
- `discoveryElementId`: Long, required.
- `discoveryElementCode`: String, required.
- `displayName`: String, required, admin/frontend metadata.
- `elementType`: String or enum, required.
- `activityId`: Long, nullable.
- `topicId`: Long, nullable.
- `visualAssetKey`: String, nullable.
- `interactionCueType`: String or enum, nullable.
- `trackingProposalId`: Long, nullable, internal only; do not send to child-facing payload unless required for correlation and safe.

### WorldRuntimeStatus

- `ACTIVE`: The child is currently in the world walk.
- `CLOSED`: The runtime state was closed due to inactivity, session close, or transition cleanup.

### WorldEngagementWindow

Runtime-only per-session observation window for abandoned games by engine type.

- `childSessionId`: Long, required.
- `signals`: List<WorldEngineAbandonmentSignal>, required, max size controlled by config.

### WorldEngineAbandonmentSignal

- `engineType`: String or enum, required.
- `activityId`: Long, required.
- `finalStatus`: String or enum, required, only `ABANDONED` is recorded for v1 pattern adjustment.
- `occurredAt`: timestamp/string, required.

### Validation Rules

- These models must not be JPA entities.
- These models must not contain `ignored`, `abandoned`, `low engagement`, or diagnostic child-facing text.
- These models must not persist LearningPath progress; tracking owns progress persistence.

## Tasks

### Domain Model
- [ ] Create `world` package following backend hexagonal structure.
- [ ] Add `WorldState` with the properties listed in `Model Properties`.
- [ ] Add `WorldDestination` model with the properties listed in `Model Properties`.
- [ ] Add `WorldDiscoveryProposal` model with the properties listed in `Model Properties`.
- [ ] Add `WorldRuntimeStatus` enum with the values listed in `Model Properties`.
- [ ] Add `WorldEngagementWindow` and `WorldEngineAbandonmentSignal` models with the properties listed in `Model Properties`.
- [ ] Ensure no child-facing diagnostic labels exist in domain outputs.
- [ ] Apply the validation rules listed in `Model Properties`.

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
