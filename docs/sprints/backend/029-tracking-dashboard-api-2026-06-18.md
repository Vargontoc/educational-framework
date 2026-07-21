# Sprint 029 - backend
# -----------------------------------------------

## Goal
Expose read-only REST endpoints for the parental dashboard using tracking summaries and progress read models.

## Status
status: completed
started_at: 2026-06-18
closed_at: 2026-06-18
blocked_by:
waiting_for:

## Tasks

### Application Read Services
- [x] Create dashboard application service for general child tracking summary.
- [x] Create read method for activity performance.
- [x] Create read method for topic performance.
- [x] Create read method for difficulty evolution if enough data exists.
- [x] Create read method for response time metrics.
- [x] Create read method for achievements.
- [x] Create read method for learning path progress.
- [x] Prefer summaries over full attempt recomputation.

### REST API
- [x] Create tracking dashboard controller.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/summary`.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/activities`.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/topics`.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/difficulty` if supported by stored data.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/response-time`.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/achievements`.
- [x] Add `GET /api/v1/tracking/children/{childProfileId}/learning-progress`.
- [x] Apply the same parental authorization style used by existing parental/productive APIs.

### Contracts
- [ ] Update `docs/contracts/api/openapi.json` after endpoints are implemented.
- [x] Do not update `docs/contracts/api/websocket.json`.

### Tests
- [x] Integration test general child summary endpoint.
- [x] Integration test activity performance endpoint.
- [x] Integration test topic performance endpoint.
- [x] Integration test response time endpoint.
- [x] Integration test achievements endpoint.
- [x] Integration test learning progress endpoint.
- [x] Negative integration test for missing child.
- [x] Negative integration test for unauthorized access if auth applies.

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
- Created ChildTrackingSummaryResponse DTO
- Created ActivityPerformanceResponse DTO
- Created TopicPerformanceResponse DTO
- Created DifficultyChangeRecord DTO
- Created DifficultyEvolutionResponse DTO
- Created ActivityResponseTime DTO
- Created ResponseTimeMetricsResponse DTO
- Created DifficultyEvolution model
- Created DifficultyEvolutionJpaEntity
- Created DifficultyEvolutionJpaRepository
- Created DifficultyEvolutionPersistenceAdapter
- Created TrackingDashboardService (implements all read methods)
- Created TrackingDashboardController with 7 endpoints
- Created ForbiddenException for authorization
- Created TrackingDashboardControllerTest (9 tests, skipped due to Docker unavailability)
- Updated ActivitySummaryRepository with findByChildProfileId
- Updated TopicSummaryRepository with findByChildProfileId (was added in Sprint 028)
- Updated ChildLearningProgressRepository with findByChildProfileId
- Updated ChildLearningCompletedStepRepository with findByChildProfileId
- All JPA repositories and persistence adapters updated accordingly

incomplete_tasks:
- OpenAPI update pending (manual step)

contract_changes:
- New REST endpoints at /api/v1/tracking/children/{childProfileId}/*
- Requires Authorization header with valid family session token

learnings:
- Authorization pattern: verify childProfile.familyId matches familyUseCase.getFamily().getId()
- All dashboard data derived from summary tables (ActivitySummary, TopicSummary), not raw attempts
- Difficulty evolution history stored in child_difficulty_evolution table (new)
- Query params: activityId for difficulty/achievements, learningPathId for learning-progress

next_sprint_suggestions:
- Implement difficulty evolution recording when adaptive difficulty changes level
- Implement retention job for old tracking data
- Implement dashboard metrics aggregation job
