# Sprint 047 - backend
# -----------------------------------------------

## Goal
Add `ActivityProposalLog` in tracking to record activity proposals shown by `world` before a game exists.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Tracking Schema
- [ ] Add `ActivityProposalLog` domain model.
- [ ] Add `ActivityProposalOutcome` enum with `STARTED` and `IGNORED`.
- [ ] Add JPA entity and repository.
- [ ] Add Liquibase migration for `activity_proposal_log`.
- [ ] Store `childProfileId`, `childSessionId`, `activityId`, nullable `topicId`, `outcome`, `proposedAt`, `resolvedAt`, `createdAt`, and `updatedAt`.
- [ ] Add indexes for `childProfileId`, `childSessionId`, `activityId`, `topicId`, and `proposedAt`.

### Tracking Ports
- [ ] Add internal use case `registerActivityProposal(childProfileId, childSessionId, activityId, topicId)`.
- [ ] Add internal use case `resolveActivityProposal(proposalId, outcome)`.
- [ ] Prevent resolving the same proposal twice.
- [ ] Validate `resolvedAt` is not before `proposedAt`.

### Tests
- [ ] Unit test registering a proposal creates a pending log.
- [ ] Unit test resolving as `STARTED`.
- [ ] Unit test resolving as `IGNORED`.
- [ ] Unit test double resolution is rejected.
- [ ] Persistence/integration test if Testcontainers is available.

## Manual Tests
- Register one activity proposal through an internal test fixture.
- Resolve it as `IGNORED`.
- Confirm the database row has `outcome = IGNORED` and `resolvedAt` set.
- Confirm no public REST endpoint was added for write operations.

## Risks
- Forcing ignored proposals into `ActivityAttempt` or `GameSessionSummary` would break their semantics.
- Public write endpoints would violate tracking boundaries.

## Dependencies
- Sprint 032 completed (`GameSessionSummary`).
- FEAT-006 Tracking Module.
- FEAT-008 World Module.

## Agent Instruction
- Keep this sprint tracking-only.
- Do not emit WebSocket events.
- Do not implement world runtime logic.
- Do not interpret ignored proposals as diagnosis or learning failure.

## Notes
This log captures the pre-game proposal phase; completed/abandoned games remain represented by `GameSessionSummary`.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
