# Sprint 031 - backend
# -----------------------------------------------

## Goal
Harden tracking contracts, test coverage, and module boundaries after the tracking implementation sprints are complete.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Contract Review
- [ ] Verify `docs/contracts/api/openapi.json` includes all dashboard REST endpoints.
- [ ] Verify `docs/contracts/api/websocket.json` is unchanged unless a separate WebSocket sprint explicitly changed it.
- [ ] Verify no tracking runtime write operations are exposed as public REST endpoints.
- [ ] Verify response DTO names and fields are stable for frontend consumption.

### Boundary Review
- [ ] Verify tracking does not depend on TTS implementation packages.
- [ ] Verify tracking does not depend on avatar implementation packages.
- [ ] Verify tracking does not depend on game implementation packages.
- [ ] Verify tracking consumes content through ports/application services instead of direct content persistence access.
- [ ] Verify tracking does not emit WebSocket events.

### Test Review
- [ ] Run all tracking unit tests.
- [ ] Run dashboard integration tests.
- [ ] Run retention tests.
- [ ] Run the full backend test suite if practical.
- [ ] Add missing negative tests found during review.

### Documentation
- [ ] Update sprint review notes with final contract changes.
- [ ] Document any intentionally deferred behavior.
- [ ] Document manual verification results.

## Manual Tests
- Start backend locally.
- Open Swagger or inspect generated OpenAPI if available.
- Confirm dashboard endpoints appear with expected request and response shapes.
- Call the main dashboard summary endpoint with valid data.
- Confirm there is no public endpoint for registering attempts or emitting game events.

## Risks
- Contract drift can block frontend work.
- Hidden dependency on game/avatar/TTS packages would make tracking harder to maintain.
- Missing negative tests can hide authorization or validation gaps.

## Dependencies
- Sprint 029 completed.
- Sprint 030 completed.

## Agent Instruction
- This is a hardening sprint, not a feature expansion sprint.
- Do not add new behavior unless it fixes a test, contract, or boundary gap.
- Do not implement game, avatar, agent, or notification behavior.

## Notes
This sprint closes the tracking module implementation loop before game integration work begins.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
