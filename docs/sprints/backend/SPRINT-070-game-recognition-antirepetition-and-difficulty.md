# Sprint 070 - backend
# -----------------------------------------------

## Goal
Add game-shell support for recognition session anti-repetition and deferred adaptive difficulty.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Session Anti-Repetition
- [x] Create a runtime registry for recent element ids by `childSessionId + topicId` or the existing equivalent identifiers.
- [x] Filter recent elements before initializing `RecognitionEngine`.
- [x] Register the `targetElementId` after a recognition round is completed correctly.
- [x] Fall back safely when all or most candidates are recent.
- [x] Clear registry entries when the child session closes if the current code exposes a close event/hook.

### Deferred Difficulty
- [x] Store tracking difficulty recommendations in `pendingDifficultyLevel` during an active round.
- [x] Keep `currentDifficultyLevel` unchanged while the child retries the same round.
- [x] Promote `pendingDifficultyLevel` only after a correct answer closes the round or when the game ends.
- [x] Apply promoted difficulty before generating the next round.

### Tests
- [x] Unit test recent elements are excluded before engine initialization when alternatives exist.
- [x] Unit test fallback still returns candidates when all are recent.
- [x] Unit test completed round target is registered as recent.
- [x] Unit test difficulty recommendation during retry does not change current round.
- [x] Unit test pending difficulty is promoted after correct answer.

## Manual Tests
- Start two recognition games in the same child session with enough content and confirm the second game avoids recently completed targets when possible.
- If tracking recommendations can be forced in dev mode, trigger one during a retry and confirm the visible round does not change.

## Risks
- Missing session close events may require explicit cleanup later.
- Filtering too aggressively can leave the engine without enough options.
- Existing tracking integration may already apply difficulty immediately and need a small orchestrator refactor.

## Dependencies
- Sprint 068 completed.
- Sprint 069 completed.
- Existing tracking adaptive difficulty support.

## Agent Instruction
- Do not persist anti-repetition across days or sessions.
- Do not put session anti-repetition inside `RecognitionEngine`.
- Do not recompute options during an active retry.
- Keep code, comments, and names in English.

## Notes
This sprint handles game-shell responsibilities that are deliberately outside the motor.

## Review

completed_tasks:
  - SessionAntiRepetitionRegistry port created (ports/out/SessionAntiRepetitionRegistry.java)
  - InMemorySessionAntiRepetitionRegistry implementation created (infrastructure/InMemorySessionAntiRepetitionRegistry.java)
  - Anti-repetition filtering integrated in GameOrchestratorService.resolveCandidates()
  - Target registration after correct answers in GameOrchestratorService.processAction()
  - Fallback when filtered candidates < MIN_OPTIONS_PER_ROUND
  - Session cleanup via GameOrchestrator.clearSessionData() hooked into ChildSessionService.closeSession(), expelChild(), and expireInactiveSessions()
  - Deferred difficulty: pendingDifficultyLevel stored during retry instead of immediate application
  - Pending difficulty promoted to currentDifficultyLevel and GameState.difficultyLevelId after correct answer
  - 9 new unit tests covering all sprint requirements (GameOrchestratorServiceSprint070Test)
  - All 863 tests pass (0 failures, 0 errors)

incomplete_tasks:

contract_changes:
  - GameOrchestrator port: added clearSessionData(Long childSessionId) method
  - GameOrchestratorService constructor: added SessionAntiRepetitionRegistry parameter

learnings:
  - Anti-repetition filtering operates at the candidate level (topic IDs used as element IDs) before engine initialization
  - Deferred difficulty uses RecognitionState.pendingDifficultyLevel to decouple tracking recommendations from round progression
  - Target capture before engine.processAction() is necessary because advanceRound() replaces targetElementId

next_sprint_suggestions:
  - Consider adding a maximum recent-elements window (e.g., last N elements) to prevent unbounded growth within a session
  - Evaluate whether anti-repetition should also apply to distractor options, not just targets
  - Integration test with real tracking module to validate deferred difficulty end-to-end

verification:
  - All 9 unit tests pass (GameOrchestratorServiceSprint070Test: 9 tests, 0 failures).
  - Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
  - SessionAntiRepetitionRegistry port created (ports/out/SessionAntiRepetitionRegistry.java).
  - InMemorySessionAntiRepetitionRegistry implementation created (infrastructure/InMemorySessionAntiRepetitionRegistry.java).
  - Anti-repetition filtering integrated in GameOrchestratorService.resolveCandidates() with fallback when filtered candidates < MIN_OPTIONS_PER_ROUND.
  - Target registration after correct answers in GameOrchestratorService.processAction() captures target BEFORE engine.processAction() to avoid losing it after advanceRound().
  - Session cleanup via GameOrchestrator.clearSessionData() hooked into ChildSessionService.closeSession(), expelChild(), and expireInactiveSessions().
  - Deferred difficulty: pendingDifficultyLevel stored during retry via applyDeferredDifficulty() instead of immediate application.
  - Pending difficulty promoted to currentDifficultyLevel and GameState.difficultyLevelId after correct answer via promotePendingDifficulty().
  - GameOrchestrator port extended with clearSessionData(Long childSessionId) method.
  - GameOrchestratorService constructor updated with SessionAntiRepetitionRegistry parameter.
  - GameModuleConfiguration updated to inject SessionAntiRepetitionRegistry.
  - RecognitionEngine does NOT contain anti-repetition logic (verified by grep) - responsibility stays in game shell as per Agent Instruction.
  - Both RecognitionEngine and GameOrchestratorService use consistent Jackson 3.x (tools.jackson.databind.ObjectMapper).
