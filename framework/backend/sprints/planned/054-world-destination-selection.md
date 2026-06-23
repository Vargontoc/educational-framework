# Sprint 054 - backend
# -----------------------------------------------

## Goal
Implement world destination selection using tracking topic selection, content catalog data, and temporary engine priority adjustment.

## Status
status: planned
started_at:
closed_at:
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
- [ ] Add `WorldOrchestrator` or selection service skeleton.
- [ ] Call tracking `TopicSelectionService` to select a recommended `topicId`.
- [ ] Query content for active activities compatible with that topic.
- [ ] Apply temporary engine priority adjustment from world engagement logic.
- [ ] Select one activity when available.
- [ ] Select host, narrative situation, and discovery element from content catalog.
- [ ] Build `WorldDestinationSelectionResult` with the properties listed in `Model Properties`.
- [ ] Build `WorldDestination` without exposing progress/diagnostic fields.
- [ ] Apply the destination selection rules listed in `Model Properties`.

### Fallbacks
- [ ] If no compatible activity exists, build a decorative/narrative destination with no playable element.
- [ ] If a selected activity is later rejected by game, leave fallback handling for Sprint 056.

### Tests
- [ ] Unit test topic selection is called.
- [ ] Unit test compatible active activity is selected.
- [ ] Unit test temporary priority adjustment changes activity choice softly.
- [ ] Unit test no compatible activity returns destination without game proposal.
- [ ] Unit test destination payload has no child-facing diagnostic labels.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
