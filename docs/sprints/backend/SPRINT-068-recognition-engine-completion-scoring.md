# Sprint 068 - backend
# -----------------------------------------------

## Goal
Complete recognition round progression, game completion, and `STARS` scoring.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Round Progression
- [x] Advance to the next round only after a correct answer.
- [x] Generate the next `targetElementId` and `optionIds` after each correct answer.
- [x] Append completed targets to `roundsShownElementIds`.
- [x] Reset per-round counters when a new round starts.
- [x] Keep total counters across the full game.

### Completion And Scoring
- [x] Implement `isGameComplete` for 5 completed rounds.
- [x] Ensure a completed recognition game is successful, not failed.
- [x] Keep abandoned/inactivity completion outside `RecognitionEngine`.
- [x] Implement `buildSummary` or the equivalent summary hook for `STARS`.
- [x] Return 3 stars when at least 4 of 5 rounds are first-try and average response time is within the configured good threshold.
- [x] Return 2 stars when the game is completed and average attempts per round is <= 2.
- [x] Return at least 1 star for any completed recognition game.

### Tests
- [x] Unit test correct answers advance rounds until completion.
- [x] Unit test incorrect answers do not count as completed rounds.
- [x] Unit test 3-star scoring path.
- [x] Unit test 2-star scoring path.
- [x] Unit test 1-star minimum path.
- [x] Unit test no failed game state is produced by scoring.

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
  - Round progression: correct answer triggers advanceRound (increment roundIndex, select new target avoiding shown, build options, append to roundsShownElementIds, reset per-round counters).
  - Completion: isGameComplete returns true when roundIndex >= totalRounds.
  - Scoring: buildSummary calculates STARS per FEAT-009 (3/2/1), sets GameStatus.COMPLETED, never produces failed/abandoned state.
  - getNextElement returns JSON with targetElementId, optionIds, roundIndex; null when game complete.
  - totalResponseTimeMs accumulated on every action in processAction.
  - candidateElementIds persisted in RecognitionState for round advancement.
  - GOOD_RESPONSE_TIME_THRESHOLD_MS (5000ms) added to RecognitionDefaults.
  - processAction returns completed=true on last correct answer and guards against post-completion actions.
  - All 37 tests pass (25 existing + 12 new).

incomplete_tasks:

contract_changes:
  - RecognitionState: added totalResponseTimeMs (long) and candidateElementIds (List<String>) fields.
  - RecognitionDefaults: added GOOD_RESPONSE_TIME_THRESHOLD_MS (long, 5000ms).
  - No changes to GameEnginePort, ActionResult, GameState, or endpoint contracts.

learnings:
  - Round advancement needed candidate list persistence in RecognitionState (candidateElementIds) since engine is stateless between calls.
  - Average response time calculated as totalResponseTimeMs / totalActions (not per-round average of per-round averages).
  - Average attempts per round = (totalIncorrectAttempts + totalRounds) / totalRounds, since each round produces exactly one correct answer.

next_sprint_suggestions:
  - Orchestration layer integration: wire RecognitionEngine into the game shell for full lifecycle (init → loop → complete).
  - Difficulty adjustment logic based on performance (pendingDifficultyLevel field exists but is unused).
  - Dashboard API for game results and star history.

verification:
  - All 37 unit tests pass (RecognitionEngineTest: 37 tests, 0 failures).
  - Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
  - No framework dependencies (Spring, JPA, Jakarta) in RecognitionEngine.
  - No prohibited dependencies (world, tracking, content, session) in RecognitionEngine.
  - No prohibited fields (topicId, promptType, biomeCode, totalTimeouts) in RecognitionEngine or RecognitionState.
  - Round progression: correct answer triggers advanceRound (increment roundIndex, select new target avoiding shown, build options, append to roundsShownElementIds, reset per-round counters).
  - Completion: isGameComplete returns true when roundIndex >= totalRounds.
  - Scoring: buildSummary calculates STARS per FEAT-009 (3/2/1), sets GameStatus.COMPLETED, never produces failed/abandoned state.
  - getNextElement returns JSON with targetElementId, optionIds, roundIndex; null when game complete.
  - totalResponseTimeMs accumulated on every action in processAction.
  - candidateElementIds persisted in RecognitionState for round advancement.
  - GOOD_RESPONSE_TIME_THRESHOLD_MS (5000ms) added to RecognitionDefaults.
  - processAction returns completed=true on last correct answer and guards against post-completion actions.
  - 3-star scoring: at least 4 of 5 rounds first-try AND avg response time <= 5000ms.
  - 2-star scoring: game completed AND avg attempts per round <= 2.
  - 1-star minimum: any completed recognition game receives at least 1 star.
  - No failed game state produced by scoring (verified by test).
