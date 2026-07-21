# Sprint 068 - backend
# -----------------------------------------------

## Goal
Complete recognition round progression, game completion, and `STARS` scoring.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Round Progression
- [ ] Advance to the next round only after a correct answer.
- [ ] Generate the next `targetElementId` and `optionIds` after each correct answer.
- [ ] Append completed targets to `roundsShownElementIds`.
- [ ] Reset per-round counters when a new round starts.
- [ ] Keep total counters across the full game.

### Completion And Scoring
- [ ] Implement `isGameComplete` for 5 completed rounds.
- [ ] Ensure a completed recognition game is successful, not failed.
- [ ] Keep abandoned/inactivity completion outside `RecognitionEngine`.
- [ ] Implement `buildSummary` or the equivalent summary hook for `STARS`.
- [ ] Return 3 stars when at least 4 of 5 rounds are first-try and average response time is within the configured good threshold.
- [ ] Return 2 stars when the game is completed and average attempts per round is <= 2.
- [ ] Return at least 1 star for any completed recognition game.

### Tests
- [ ] Unit test correct answers advance rounds until completion.
- [ ] Unit test incorrect answers do not count as completed rounds.
- [ ] Unit test 3-star scoring path.
- [ ] Unit test 2-star scoring path.
- [ ] Unit test 1-star minimum path.
- [ ] Unit test no failed game state is produced by scoring.

## Manual Tests
- Not required unless this sprint touches runtime game endpoints. If it does, run a dev game and complete 5 rounds to confirm a star result is produced.

## Risks
- Scoring thresholds may need to live in configuration; avoid hardcoding if the codebase already has configurable engine params.
- Response time availability may depend on existing `ActionResult` behavior.

## Dependencies
- Sprint 067 completed.

## Agent Instruction
- Keep scoring simple and exactly aligned with FEAT-009.
- Do not add dashboard APIs in this sprint.
- Do not add frontend-specific labels or messages.
- Keep code, comments, and names in English.

## Notes
This closes the pure engine behavior before orchestration and external module integration.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
