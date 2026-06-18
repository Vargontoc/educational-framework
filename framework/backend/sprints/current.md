# Sprint 029 - backend
# -----------------------------------------------

## Goal
Expose read-only REST endpoints for the parental dashboard using tracking summaries and progress read models.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Application Read Services
- [ ] Create dashboard application service for general child tracking summary.
- [ ] Create read method for activity performance.
- [ ] Create read method for topic performance.
- [ ] Create read method for difficulty evolution if enough data exists.
- [ ] Create read method for response time metrics.
- [ ] Create read method for achievements.
- [ ] Create read method for learning path progress.
- [ ] Prefer summaries over full attempt recomputation.

### REST API
- [ ] Create tracking dashboard controller.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/summary`.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/activities`.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/topics`.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/difficulty` if supported by stored data.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/response-time`.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/achievements`.
- [ ] Add `GET /api/v1/tracking/children/{childProfileId}/learning-progress`.
- [ ] Apply the same parental authorization style used by existing parental/productive APIs.

### Contracts
- [ ] Update `docs/contracts/api/openapi.json` after endpoints are implemented.
- [ ] Do not update `docs/contracts/api/websocket.json`.

### Tests
- [ ] Integration test general child summary endpoint.
- [ ] Integration test activity performance endpoint.
- [ ] Integration test topic performance endpoint.
- [ ] Integration test response time endpoint.
- [ ] Integration test achievements endpoint.
- [ ] Integration test learning progress endpoint.
- [ ] Negative integration test for missing child.
- [ ] Negative integration test for unauthorized access if auth applies.

## Manual Tests
- Start backend locally.
- Use Postman, curl, or Swagger to call each dashboard endpoint with a valid parental/session context.
- Confirm totals match test or manually inserted tracking rows.
- Confirm endpoints are read-only and do not register attempts, achievements, curiosities, or completed steps.
- Confirm OpenAPI includes the new dashboard paths.

## Risks
- Dashboard endpoints can accidentally recompute too much from atomic attempts.
- Authorization mistakes could expose child progress data.
- Adding write endpoints here would violate the feature boundary.

## Dependencies
- Sprint 023 completed.
- Sprint 026 completed for achievements endpoint.
- Sprint 027 completed for learning progress endpoint.

## Agent Instruction
- Add only read-only dashboard endpoints.
- Do not add runtime write endpoints for game engines.
- Do not emit WebSocket events.
- Update OpenAPI after REST endpoints are added.

## Notes
This is the first tracking sprint with user-visible REST contract impact.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
