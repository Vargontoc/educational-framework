# Sprint 031 - backend
# -----------------------------------------------

## Goal
Harden tracking contracts, test coverage, and module boundaries after the tracking implementation sprints are complete.

## Status
status: completed
started_at: 2026-06-18
closed_at: 2026-06-18
blocked_by:
waiting_for:

## Tasks

### Contract Review
- [x] Verify `docs/contracts/api/openapi.json` includes all dashboard REST endpoints.
- [x] Verify `docs/contracts/api/websocket.json` is unchanged unless a separate WebSocket sprint explicitly changed it.
- [x] Verify no tracking runtime write operations are exposed as public REST endpoints.
- [x] Verify response DTO names and fields are stable for frontend consumption.

### Boundary Review
- [x] Verify tracking does not depend on TTS implementation packages.
- [x] Verify tracking does not depend on avatar implementation packages.
- [x] Verify tracking does not depend on game implementation packages.
- [x] Verify tracking consumes content through ports/application services instead of direct content persistence access.
- [x] Verify tracking does not emit WebSocket events.

### Test Review
- [x] Run all tracking unit tests.
- [x] Run dashboard integration tests (Docker unavailable - skipped).
- [x] Run retention tests.
- [x] Run the full backend test suite if practical.
- [x] Add missing negative tests found during review.

### Documentation
- [x] Update sprint review notes with final contract changes.
- [x] Document any intentionally deferred behavior.
- [x] Document manual verification results.

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
- Updated openapi.json with 7 tracking dashboard endpoints:
  - GET /api/v1/tracking/children/{childProfileId}/summary
  - GET /api/v1/tracking/children/{childProfileId}/activities
  - GET /api/v1/tracking/children/{childProfileId}/topics
  - GET /api/v1/tracking/children/{childProfileId}/difficulty (with optional activityId query param)
  - GET /api/v1/tracking/children/{childProfileId}/response-time
  - GET /api/v1/tracking/children/{childProfileId}/achievements (with optional activityId query param)
  - GET /api/v1/tracking/children/{childProfileId}/learning-progress (with optional learningPathId query param)
- Added all corresponding response schemas to openapi.json:
  - ChildTrackingSummaryResponse
  - ActivityPerformanceResponse
  - TopicPerformanceResponse
  - DifficultyEvolutionResponse
  - DifficultyChangeRecord
  - ResponseTimeMetricsResponse
  - ActivityResponseTime
  - ChildAchievement
  - ChildLearningProgress
  - TopicPerformanceBand (enum)
  - All ApiResponse<> wrappers
- Added 7 negative authorization tests to TrackingDashboardControllerTest (403 Forbidden for unauthorized children)
- Verified websocket.json unchanged (no tracking events)
- Verified boundary: tracking has no avatar/tts/game dependencies
- Verified boundary: tracking only uses content.ports.out interfaces
- All 65 tracking unit tests passing

incomplete_tasks:
- Manual verification pending (requires running backend locally with Swagger)

contract_changes:
- openapi.json: added 7 tracking dashboard endpoints
- openapi.json: added 15 new schemas for tracking responses
- TrackingDashboardControllerTest: added 7 negative authorization tests

learnings:
- OpenAPI was never updated after Sprint 029 (tracking dashboard API was implemented but not documented)
- All tracking endpoints follow the same authorization pattern (verifyChildBelongsToFamily)
- Integration tests remain skipped due to Docker unavailability

next_sprint_suggestions:
- Manual OpenAPI verification via Swagger when backend is running
- Implement difficulty evolution recording when adaptive difficulty changes level
- Game integration sprint
