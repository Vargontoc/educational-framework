# Sprint 054 - backend
# -----------------------------------------------

## Goal
Implement world destination selection using tracking topic selection, content catalog data, and temporary engine priority adjustment.

## Status
status: completed
started_at: 2026-06-23
closed_at: 2026-06-23
blocked_by:
waiting_for:

## Model Properties

### WorldDestinationSelectionResult

Application result returned by destination selection.

- `childSessionId`: Long, required.
- `topicId`: Long, nullable if tracking cannot recommend a topic.
- `destination`: WorldDestination, required.
- `selectedActivity`: SelectedWorldActivity, nullable when destination is decorative/narrative only.
- `priorityAdjustmentApplied`: boolean, required.

### SelectedWorldActivity

Internal activity choice made by `world`.

- `activityId`: Long, required.
- `engineType`: String or enum, required.
- `topicId`: Long, nullable.
- `source`: String or enum, required. Suggested values: `TOPIC_RECOMMENDATION`, `FALLBACK`.

### Destination Selection Rules

- `world` calls tracking to select topic; it does not classify topics itself.
- `world` queries content for compatible active activities; it does not read content persistence directly.
- If no activity is available, return a destination with `selectedActivity = null`.
- Do not include LearningPath progress, locks, completion status, diagnostic labels, or engagement labels.

## Tasks

### Selection Flow
- [x] Add `WorldOrchestrator` or selection service skeleton.
- [x] Call tracking `TopicSelectionService` to select a recommended `topicId`.
- [x] Query content for active activities compatible with that topic.
- [x] Apply temporary engine priority adjustment from world engagement logic.
- [x] Select one activity when available.
- [x] Select host, narrative situation, and discovery element from content catalog.
- [x] Build `WorldDestinationSelectionResult` with the properties listed in `Model Properties`.
- [x] Build `WorldDestination` without exposing progress/diagnostic fields.
- [x] Apply the destination selection rules listed in `Model Properties`.

### Fallbacks
- [x] If no compatible activity exists, build a decorative/narrative destination with no playable element.
- [x] If a selected activity is later rejected by game, leave fallback handling for Sprint 056.

### Tests
- [x] Unit test topic selection is called.
- [x] Unit test compatible active activity is selected.
- [x] Unit test temporary priority adjustment changes activity choice softly.
- [x] Unit test no compatible activity returns destination without game proposal.
- [x] Unit test destination payload has no child-facing diagnostic labels.

## Manual Tests
- Optional: run a dev/test fixture that builds a destination for one child session.
- Confirm logs or returned data include host, situation, discovery element, and optional activity.

## Risks
- Reimplementing topic selection in world would violate tracking ownership.
- Returning hidden progress fields to frontend would break FEAT-011.

## Dependencies
- Sprint 046 completed.
- Sprint 028 completed (`TopicSelectionService`).
- Sprint 053 completed.

## Agent Instruction
- World chooses activity and narrative context; tracking chooses topic.
- Do not expose LearningPath progress to the child-facing payload.
- Do not call game yet in this sprint.

## Notes
This sprint makes world capable of deciding the next narrative destination without starting games.

## Review

completed_tasks:
- Created `SelectedWorldActivity` model with Source enum (TOPIC_RECOMMENDATION, FALLBACK)
- Created `WorldDestinationSelectionResult` model
- Created `WorldOrchestrator` port interface
- Created `WorldOrchestratorService` implementation that:
  - Calls tracking SelectTopicsForDifficultyUseCase with EASY difficulty to get topicId
  - Queries content WorldCatalogUseCase for compatible activities
  - Applies engagement priority adjustments from WorldEngagementEvaluator
  - Builds WorldDestination with host, narrative situation, and discovery elements
  - Returns selected activity and priority adjustment flag
- Updated `WorldModuleConfiguration` to wire new service with dependencies
- Created `WorldOrchestratorServiceTest` with 5 unit tests
- All 765 tests pass

incomplete_tasks:
- None

contract_changes:
- None (internal world orchestration only)

learnings:
- WorldOrchestratorService follows the same dependency injection pattern as other services
- Using SelectTopicsForDifficultyUseCase with EASY difficulty provides topic recommendations without requiring game-specific DifficultyLevel
- The service builds a complete WorldDestination with narrative elements even when no activity is selected

next_sprint_suggestions:
- Sprint 055: World activity proposal flow