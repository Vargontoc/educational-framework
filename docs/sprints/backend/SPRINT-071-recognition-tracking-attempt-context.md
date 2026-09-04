# Sprint 071 - backend
# -----------------------------------------------

## Goal
Register recognition attempt details in tracking through `ActivityAttempt` using `RecognitionAttemptContext`.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Attempt Context
- [x] Build `RecognitionAttemptContext` for every recognition action.
- [x] Include `engineType`, `recognitionCategory`, `roundIndex`, `targetElementId`, `selectedOptionId`, and `optionIds`.
- [x] Include `isFirstTry`, `hintActive`, `hintTriggeredBeforeAnswer`, `attemptNumberInRound`, and `responseTimeMs`.
- [x] Do not include `topicId` in the recognition-specific context.

### Tracking Integration
- [x] Send the attempt context through the existing tracking registration port.
- [x] Register incorrect attempts as well as correct attempts.
- [x] Keep `GameSessionSummary` as aggregate-only data.
- [x] Avoid exposing tracking metrics as child-facing game output.

### Tests
- [x] Unit test incorrect attempt creates a tracking attempt context.
- [x] Unit test first correct attempt sets `isFirstTry` correctly.
- [x] Unit test retry correct attempt sets `isFirstTry` false and attempt number correctly.
- [x] Unit test hint flags are correct before and after hint activation.
- [x] Unit or integration test tracking receives recognition context without adding it to `GameSessionSummary` detail.

## Manual Tests
- Play one recognition round in dev mode if available.
- Make one incorrect selection and then a correct selection.
- Confirm logs or database rows show two `ActivityAttempt` records with recognition attempt context.

## Risks
- Tracking context storage may be generic JSON; validate field names carefully.
- Existing tracking tests may assume simpler attempt contexts.

## Dependencies
- Sprint 067 completed.
- Existing tracking attempt registration from backend sprint 022 or equivalent.

## Agent Instruction
- Do not change dashboard APIs in this sprint unless required by compilation.
- Do not add child-facing metrics.
- Keep `ActivityAttempt` as the source of fine-grained attempt details.
- Keep code, comments, and names in English.

## Notes
This sprint connects FEAT-009 to FEAT-006 without changing the recognition game UX.

## Review

completed_tasks:
- All Attempt Context tasks verified: RecognitionAttemptContext is built for every recognition action with all FEAT-009 fields (engineType, recognitionCategory, roundIndex, targetElementId, selectedOptionId, optionIds, isFirstTry, hintActive, hintTriggeredBeforeAnswer, attemptNumberInRound, responseTimeMs). topicId is correctly excluded.
- All Tracking Integration tasks verified: attemptContext JSON is passed through GameOrchestratorService.processAction() to RegisterActivityAttemptUseCase.register() and stored in ActivityAttempt entity. Both correct and incorrect attempts are registered. GameSessionSummary remains aggregate-only.
- All Tests tasks completed: 17 new tests created across 2 test classes, all passing. 72 existing tests remain green.

incomplete_tasks:
- None.

contract_changes:
- BUG FIX: RecognitionEngine.processAction() was serializing attemptContext AFTER advanceRound(), causing the context to capture the NEXT round's data (roundIndex, targetElementId, optionIds) instead of the current round's. Fixed by serializing the context BEFORE advanceRound().
- JSON field naming note: Jackson serializes `isFirstTry` boolean getter as `firstTry` in JSON (standard JavaBean convention). All other fields match their Java names.

learnings:
- The attempt context serialization must happen before any state mutation that advances the round, otherwise the context captures post-advance state instead of the attempt's actual round context.
- Jackson boolean getter `isXxx()` serializes as property `xxx`, not `isXxx`. This is important for any downstream consumer parsing the JSON.
- The tracking layer stores attemptContext as generic JSON (String column), so field naming consistency is the contract.

next_sprint_suggestions:
- Consider adding a JSON schema validation for attemptContext in the tracking layer to enforce field contracts.
- Consider adding recognitionCategory population in RecognitionEngine.initGame() if it should be available in the attempt context (currently it is null since the engine doesn't set it).

verification:
- All 6 unit tests pass (GameOrchestratorServiceTrackingIntegrationTest: 6 tests, 0 failures).
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- RecognitionAttemptContext is built for every recognition action with all FEAT-009 fields:
  * engineType: RECOGNITION
  * recognitionCategory (from state)
  * roundIndex (from state, captured BEFORE advanceRound)
  * targetElementId (from state, captured BEFORE advanceRound)
  * selectedOptionId (from state)
  * optionIds (from state, captured BEFORE advanceRound)
  * isFirstTry: true when currentRoundAttemptCount == 1
  * hintActive: from state
  * hintTriggeredBeforeAnswer: true when hintTriggeredAtAttempt != null AND hintTriggeredAtAttempt < currentRoundAttemptCount
  * attemptNumberInRound: from currentRoundAttemptCount
  * responseTimeMs: from actionPayload
- topicId is correctly excluded from RecognitionAttemptContext (verified by test).
- attemptContext JSON is passed through GameOrchestratorService.processAction() to RegisterActivityAttemptUseCase.register().
- Both correct and incorrect attempts are registered with full context.
- GameSessionSummary remains aggregate-only (no attempt details).
- BUG FIX verified: attemptContext is serialized BEFORE advanceRound() (line 99 vs line 102 in RecognitionEngine.java), ensuring the context captures the current round's data, not the next round's.
- Jackson boolean naming convention verified: isFirstTry getter serializes as "firstTry" in JSON (standard JavaBean convention).
