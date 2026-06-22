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

## Tasks

### Selection Flow
- [ ] Add `WorldOrchestrator` or selection service skeleton.
- [ ] Call tracking `TopicSelectionService` to select a recommended `topicId`.
- [ ] Query content for active activities compatible with that topic.
- [ ] Apply temporary engine priority adjustment from world engagement logic.
- [ ] Select one activity when available.
- [ ] Select host, narrative situation, and discovery element from content catalog.
- [ ] Build `WorldDestination` without exposing progress/diagnostic fields.

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
