# Sprint 067 - backend
# -----------------------------------------------

## Goal
Implement recognition action processing with retry-until-correct behavior and hint activation.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Action Processing
- [x] Implement `processAction` for selected option actions.
- [x] Return `CORRECT` when `selectedOptionId` matches `targetElementId`.
- [x] Return `INCORRECT` when the selected option is not the target.
- [x] Keep the same `targetElementId` and `optionIds` after incorrect answers.
- [x] Increment round attempt counters and total incorrect counters correctly.
- [x] Update `lastActionAt` after each action.

### Hint Rules
- [x] Activate `hintActive` after 2 consecutive failures in the same round.
- [x] Set `hintTriggeredAtAttempt` when the hint is first activated.
- [x] Keep hint rendering outside backend domain logic.
- [x] Do not return `TIMEOUT` from `RecognitionEngine`.

### Tests
- [x] Unit test incorrect answer keeps the same round open.
- [x] Unit test first incorrect answer does not activate hint.
- [x] Unit test second consecutive incorrect answer activates hint.
- [x] Unit test correct answer after failures returns `CORRECT` and preserves attempt data.
- [x] Unit test `RecognitionEngine` never returns `TIMEOUT` for recognition actions.

## Manual Tests
- Not required. Unit tests cover domain behavior.

## Risks
- The shared `ActionResultType` may include `TIMEOUT`; recognition must not emit it for child actions.
- Advancing after an incorrect answer would break the no-failure rule for ages 3-4.

## Dependencies
- Sprint 066 completed.

## Agent Instruction
- Do not implement scoring or full game completion in this sprint.
- Do not call tracking directly unless the current game shell already requires attempt context from `processAction`.
- Keep child-facing penalty logic out of the backend domain.
- Keep code, comments, and names in English.

## Notes
This sprint implements the core 3-4 year old safety rule: retries are supportive, not punitive.

## Review

completed_tasks:
  - processAction implemented with full retry-until-correct behavior
  - CORRECT/INCORRECT result determination based on selectedOptionId vs targetElementId
  - Round state preserved after incorrect answers (same target, same options)
  - All counters correctly maintained (currentRoundAttemptCount, currentRoundConsecutiveFailures, totalIncorrectAttempts, totalCorrectFirstTry)
  - lastActionAt and selectedOptionId updated on every action
  - Hint activation after 2 consecutive failures with hintTriggeredAtAttempt tracking
  - RecognitionAttemptContext serialized into ActionResult.attemptContext
  - responseTimeMs propagated from actionPayload to ActionResult
  - TIMEOUT never returned by RecognitionEngine
  - 10 new unit tests added (23 total, all passing)

incomplete_tasks:
  - None within sprint scope

contract_changes:
  - RecognitionDefaults.HINT_ACTIVATION_THRESHOLD = 2 (new constant)

learnings:
  - Hint flag remains active once triggered within a round (not reset on subsequent failures)
  - hintTriggeredAtAttempt captures the attempt number when hint was first activated
  - Null/blank actionPayload treated as incorrect (selectedOptionId = null)
  - Pre-existing GameOrchestratorServiceTest failures (9 tests) are unrelated to this sprint

next_sprint_suggestions:
  - Implement getNextElement for round advancement after correct answer
  - Implement isGameComplete to check totalRounds completion
  - Implement buildSummary for end-of-game statistics
  - Add scoring logic for correct/incorrect attempts

verification:
  - All 23 unit tests pass (RecognitionEngineTest: 23 tests, 0 failures).
  - Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
  - No framework dependencies (Spring, JPA, Jakarta) in RecognitionEngine.
  - No prohibited dependencies (world, tracking, content, session) in RecognitionEngine.
  - No prohibited fields (topicId, promptType, biomeCode, totalTimeouts) in RecognitionEngine or RecognitionState.
  - processAction implements full retry-until-correct behavior per FEAT-009.
  - CORRECT/INCORRECT result determination based on selectedOptionId vs targetElementId.
  - Round state preserved after incorrect answers (same target, same options).
  - All counters correctly maintained (currentRoundAttemptCount, currentRoundConsecutiveFailures, totalIncorrectAttempts, totalCorrectFirstTry).
  - lastActionAt and selectedOptionId updated on every action.
  - Hint activation after 2 consecutive failures with hintTriggeredAtAttempt tracking.
  - RecognitionAttemptContext serialized into ActionResult.attemptContext.
  - responseTimeMs propagated from actionPayload to ActionResult.
  - TIMEOUT never returned by RecognitionEngine (verified by test).
  - HINT_ACTIVATION_THRESHOLD = 2 added to RecognitionDefaults.
  - Hint flag remains active once triggered within a round (not reset on subsequent failures).
  - Null/blank actionPayload treated as incorrect (selectedOptionId = null).
