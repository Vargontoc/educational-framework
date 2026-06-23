# Sprint 055 - backend
# -----------------------------------------------

## Goal
Implement activity proposal tracking in `world`, including `STARTED`, `IGNORED`, and automatic cleanup of pending proposals.

## Status
status: planned
started_at:
closed_at:
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
- [ ] Register an activity proposal through tracking when a discovery element with activity is presented.
- [ ] Store the pending proposal id in `WorldState`.
- [ ] Resolve proposal as `STARTED` when the child chooses the discovery element and game start is attempted.
- [ ] Resolve proposal as `IGNORED` when the interaction window ends without child interaction.
- [ ] Resolve existing pending proposal as `IGNORED` before creating a new proposal.
- [ ] Resolve pending proposal as `IGNORED` when the world state closes.
- [ ] Return `WorldActivityProposalResult` and `WorldProposalResolutionResult` with the properties listed in `Model Properties`.
- [ ] Apply the child-facing rule listed in `Model Properties`.

### Tests
- [ ] Unit test proposal is registered when destination contains activity.
- [ ] Unit test proposal resolves as `STARTED`.
- [ ] Unit test proposal resolves as `IGNORED`.
- [ ] Unit test creating a new proposal closes previous pending proposal as `IGNORED`.
- [ ] Unit test closing world state resolves pending proposal as `IGNORED`.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
