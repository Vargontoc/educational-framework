# Sprint 035 - backend
# -----------------------------------------------

## Goal
Ensure the game shell can load the minimum content catalog data needed to start a game without depending on content persistence internals.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Content Read Ports
- [ ] Identify existing content read services or ports for `Activity` and `DifficultyLevel`.
- [ ] Add a minimal internal query for active activity by id if missing.
- [ ] Add a minimal internal query for active difficulty level by id or current difficulty code if missing.
- [ ] Include only fields needed by the game shell: activity id, engine type, age range, topic references, difficulty id/code, difficulty parameters.

### Validation
- [ ] Ensure inactive or draft activities cannot start a game.
- [ ] Ensure inactive difficulty levels cannot start a game.
- [ ] Ensure content age-range metadata remains available for future 3-4 filtering.

### Tests
- [ ] Unit test active activity can be loaded for game.
- [ ] Unit test inactive activity is rejected.
- [ ] Unit test active difficulty can be loaded for game.
- [ ] Unit test missing activity or difficulty returns a clear domain error.

## Manual Tests
- Start backend locally with seed content.
- Use existing content APIs or test fixtures to verify one active activity and difficulty can be resolved.
- Confirm inactive/draft content is not returned as playable.

## Risks
- Expanding content APIs too much could turn this into a frontend catalog sprint.
- Direct game access to content repositories would break module boundaries.

## Dependencies
- Content module completed through Sprint 015.
- FEAT-003 Content Module.
- FEAT-007 requires content lookup before game start.

## Agent Instruction
- Keep this sprint content-focused and minimal.
- Do not implement game orchestration.
- Do not add public read APIs unless they already belong to FEAT-003 and are required for manual verification.

## Notes
This sprint makes game start possible without deciding any concrete minigame behavior.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
