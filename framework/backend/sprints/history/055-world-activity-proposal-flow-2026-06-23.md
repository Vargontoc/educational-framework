# Sprint 055 - backend
# -----------------------------------------------

## Goal
Implement activity proposal tracking in `world`, including `STARTED`, `IGNORED`, and automatic cleanup of pending proposals.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Model Properties

### WorldActivityProposalResult

Application result when `world` registers or updates a proposal.

- `childSessionId`: Long, required.
- `proposalRuntimeId`: String or Long, required.
- `trackingProposalId`: Long, required.
- `activityId`: Long, required.
- `topicId`: Long, nullable.
- `status`: String or enum, required. Suggested values: `PENDING`, `STARTED`, `IGNORED`.

### WorldProposalResolutionResult

Application result when `world` resolves a pending proposal.

- `trackingProposalId`: Long, required.
- `outcome`: ActivityProposalOutcome, required: `STARTED`, `IGNORED`.
- `resolvedAt`: timestamp/string following existing backend timestamp pattern, required.

### Child-Facing Rule

- `status = IGNORED` and `outcome = IGNORED` are internal/tracking data only.
- Do not send `IGNORED`, `ignored`, `abandoned`, `low engagement`, or diagnostic labels in child-facing payloads.

## Tasks

### Proposal Lifecycle
- [x] Register an activity proposal through tracking when a discovery element with activity is presented.
- [x] Store the pending proposal id in `WorldState`.
- [x] Resolve proposal as `STARTED` when the child chooses the discovery element and game start is attempted.
- [x] Resolve proposal as `IGNORED` when the interaction window ends without child interaction.
- [x] Resolve existing pending proposal as `IGNORED` before creating a new proposal.
- [x] Resolve pending proposal as `IGNORED` when the world state closes.
- [x] Return `WorldActivityProposalResult` and `WorldProposalResolutionResult` with the properties listed in `Model Properties`.
- [x] Apply the child-facing rule listed in `Model Properties`.

### Tests
- [x] Unit test proposal is registered when destination contains activity.
- [x] Unit test proposal resolves as `STARTED`.
- [x] Unit test proposal resolves as `IGNORED`.
- [x] Unit test creating a new proposal closes previous pending proposal as `IGNORED`.
- [x] Unit test closing world state resolves pending proposal as `IGNORED`.

## Manual Tests
- Simulate a destination with a discovery element and no interaction.
- Confirm tracking receives `IGNORED`.
- Simulate a child interaction.
- Confirm tracking receives `STARTED`.

## Risks
- Pending proposals can remain unresolved if session/system events are missed.
- Labeling ignored proposals in child-facing payload would violate FEAT-008.

## Dependencies
- Sprint 047 completed.
- Sprint 052 completed.
- Sprint 054 completed.

## Agent Instruction
- Do not expose `IGNORED` to the child-facing frontend payload.
- Keep v1 cleanup simple and document any hardening warning.
- Do not start games yet except as a mocked boundary in tests.

## Notes
This sprint captures a common child behavior: seeing something optional and not interacting with it.

## Review

completed_tasks:
- Created `WorldActivityProposalResult` model with Status enum (PENDING, STARTED, IGNORED)
- Created `WorldProposalResolutionResult` model
- Created `WorldActivityProposalUseCase` port interface
- Created `WorldProposalResolutionUseCase` port interface
- Created `WorldProposalService` implementing both ports
- Added `getPendingProposalId` method to `WorldStateRegistry`
- Updated `InMemoryWorldStateRegistry` with new method
- Updated `WorldModuleConfiguration` to wire new service
- Created `WorldProposalServiceTest` with 7 unit tests
- All 772 tests pass

incomplete_tasks:
- None

contract_changes:
- None (internal world proposal tracking only)

learnings:
- WorldProposalService uses existing tracking proposal ports (RegisterActivityProposalUseCase, ResolveActivityProposalUseCase)
- `resolveAndCloseWorld` method handles automatic IGNORED resolution on world state close
- New proposal automatically resolves previous pending proposal as IGNORED before registering new one

next_sprint_suggestions:
- Sprint 056: World game start integration