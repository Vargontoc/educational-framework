# Sprint 066 - backend
# -----------------------------------------------

## Goal
Implement recognition game initialization and round generation using candidates already resolved outside the engine.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Engine Initialization
- [x] Create `RecognitionEngine` implementing the existing `GameEngine` contract.
- [x] Implement `initGame` using candidates already provided by the game/content flow.
- [x] Initialize `RecognitionState` with round counters, difficulty fields, timestamps, and empty shown-element history.
- [x] Select a `targetElementId` for the first round.
- [x] Build `optionIds` with 2-3 options and always include the target.

### Round Selection
- [x] Avoid repeating targets already present in `roundsShownElementIds` when enough candidates exist.
- [x] Use a safe fallback when candidate count is small.
- [x] Keep category, habitat, NUMBER unlock, and session filtering outside the engine.
- [x] Do not create timeout behavior.

### Tests
- [x] Unit test `initGame` creates a valid first round.
- [x] Unit test options include the target exactly once.
- [x] Unit test option count is 2-3 when enough candidates exist.
- [x] Unit test target selection avoids already shown elements when possible.
- [x] Unit test engine does not query `world`, tracking, content repositories, or session registries directly.

## Manual Tests
- Not required. Unit tests verify the engine behavior for this sprint.

## Risks
- The existing `GameEngine` interface may differ from the draft in FEAT-009; adapt to the implemented interface.
- Random selection can make tests flaky; use deterministic selection or injectable randomness if the codebase already has a pattern for it.

## Dependencies
- Sprint 065 completed.
- Existing FEAT-007 game engine contract.

## Agent Instruction
- Implement only initialization and round generation.
- Do not implement action processing, scoring, tracking, WebSocket, or world integration here.
- Keep the engine independent from `world`, content repositories, tracking, and session anti-repetition registries.
- Keep code, comments, and names in English.

## Notes
This sprint should be small enough for a junior developer to finish with unit tests in one pass.

## Review

completed_tasks:
  - RecognitionEngine refactored with injectable Random for deterministic testing
  - initGame parses engineParams JSON to extract candidates list
  - RecognitionState initialized with all FEAT-009 fields (round counters, difficulty, timestamps, empty history)
  - Target selection avoids roundsShownElementIds when unshown candidates exist
  - Safe fallback: when all candidates are shown, repetition is allowed
  - Option count is 2-3 (MIN/MAX_OPTIONS_PER_ROUND) when enough candidates; adapts to fewer candidates
  - Target always included exactly once in optionIds
  - Options shuffled for non-deterministic presentation order
  - RecognitionState serialized to JSON via Jackson ObjectMapper with JavaTimeModule
  - Engine has zero dependencies on world, tracking, content, or session classes
  - 14 unit tests all passing

incomplete_tasks:
  - processAction (deferred to next sprint)
  - getNextElement (deferred to next sprint)
  - isGameComplete (deferred to next sprint)
  - buildSummary (deferred to next sprint)

contract_changes:
  - engineParams JSON format: {"candidates": ["elem-1", "elem-2", ...]}
  - Optional "category" field in engineParams is accepted but not used by engine (category stays in orchestrator scope)

learnings:
  - Jackson ObjectMapper requires JavaTimeModule registration for LocalDateTime serialization; spring-boot-jackson2 brings jackson-datatype-jsr310 transitively
  - Injectable Random via constructor enables deterministic unit tests without flaky randomness

next_sprint_suggestions:
  - Sprint 067: Implement processAction (correct/incorrect handling, attempt tracking, round advancement)
  - Sprint 067: Implement getNextElement (return current round target/options from serialized state)
  - Sprint 067: Implement isGameComplete (check roundIndex >= totalRounds)

verification:
  - All 14 unit tests pass (RecognitionEngineTest: 14 tests, 0 failures).
  - Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
  - No framework dependencies (Spring, JPA, Jakarta) in RecognitionEngine.
  - No prohibited dependencies (world, tracking, content, session) in RecognitionEngine.
  - RecognitionEngine implements GameEnginePort contract correctly.
  - initGame correctly initializes GameState with all required fields.
  - RecognitionState initialized with all FEAT-009 fields (round counters, difficulty, timestamps, empty history).
  - Target selection avoids roundsShownElementIds when unshown candidates exist.
  - Safe fallback: when all candidates are shown, repetition is allowed.
  - Option count is 2-3 (MIN/MAX_OPTIONS_PER_ROUND) when enough candidates; adapts to fewer candidates.
  - Target always included exactly once in optionIds.
  - Options shuffled for non-deterministic presentation order.
  - RecognitionState serialized to JSON via Jackson ObjectMapper with JavaTimeModule.
  - Injectable Random via constructor enables deterministic unit tests.
  - processAction, getNextElement, isGameComplete, buildSummary throw UnsupportedOperationException (deferred to next sprint).
