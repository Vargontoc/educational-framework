# Sprint 047 - backend
# -----------------------------------------------

## Goal
Add `ActivityProposalLog` in tracking to record activity proposals shown by `world` before a game exists.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Model Properties

### ActivityProposalLog

Tracking-owned persistent record for an activity proposal shown by `world` before a game exists.

- `id`: Long, inherited from `BaseEntity`.
- `childProfileId`: Long, required.
- `childSessionId`: Long, required.
- `activityId`: Long, required.
- `topicId`: Long, nullable because not every activity is topic-bound.
- `outcome`: ActivityProposalOutcome, nullable while pending; set to `STARTED` or `IGNORED` when resolved.
- `proposedAt`: timestamp/string following existing tracking timestamp pattern, required.
- `resolvedAt`: timestamp/string following existing tracking timestamp pattern, nullable until resolved.
- `createdAt`: inherited from `BaseEntity`.
- `updatedAt`: inherited from `BaseEntity`.

### ActivityProposalOutcome

- `STARTED`: The child interacted with the discovery element and world attempted/accepted game start.
- `IGNORED`: The proposal window ended, another proposal replaced it, or the session/system flow closed it without game start.

### Validation Rules

- `childProfileId`, `childSessionId`, `activityId`, and `proposedAt` are required.
- `resolvedAt` must be null while `outcome` is null.
- `resolvedAt` is required when `outcome` is set.
- `resolvedAt` must not be before `proposedAt`.
- A resolved proposal must not be resolved again.
- `IGNORED` is descriptive dashboard data, not a child-facing failure or diagnosis.

## Tasks

### Tracking Schema
- [x] Add `ActivityProposalLog` domain model with the properties listed in `Model Properties`.
- [x] Add `ActivityProposalOutcome` enum with the values listed in `Model Properties`.
- [x] Add JPA entity and repository.
- [x] Add Liquibase migration for `activity_proposal_log`.
- [x] Store all fields listed in `Model Properties`.
- [x] Add indexes for `childProfileId`, `childSessionId`, `activityId`, `topicId`, and `proposedAt`.

### Tracking Ports
- [x] Add internal use case `registerActivityProposal(childProfileId, childSessionId, activityId, topicId)`.
- [x] Add internal use case `resolveActivityProposal(proposalId, outcome)`.
- [x] Prevent resolving the same proposal twice.
- [x] Implement the validation rules listed in `Model Properties`.

### Tests
- [x] Unit test registering a proposal creates a pending log.
- [x] Unit test resolving as `STARTED`.
- [x] Unit test resolving as `IGNORED`.
- [x] Unit test double resolution is rejected.
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
- Created ActivityProposalOutcome enum with STARTED and IGNORED values
- Created ActivityProposalLog domain model
- Created ActivityProposalLogResult record
- Created ActivityProposalLogJpaEntity
- Created ActivityProposalLogJpaRepository
- Created ActivityProposalLogRepository port interface
- Created ActivityProposalLogPersistenceAdapter
- Created ActivityProposalLogValidator with registration and resolution validation
- Created RegisterActivityProposalUseCase interface
- Created ResolveActivityProposalUseCase interface
- Created ActivityProposalLogService implementing both use cases
- Updated TrackingModuleConfiguration with beans for both use cases
- Added Liquibase migration 021__create_activity_proposal_log.xml
- Updated db.changelog-master.xml to include migration 021
- Created ActivityProposalLogServiceTest with 13 unit tests

incomplete_tasks:
- Persistence/integration test if Testcontainers is available (skipped - no Testcontainers setup)

contract_changes:
- None (internal ports only)

learnings:
- Static method references in lambda expressions require class name prefix (e.g., `ActivityProposalLogPersistenceAdapter::toDomain`)
- topicId can be null for non-topic-bound activities

next_sprint_suggestions:
- Sprint 048: Activity engagement summary tracking