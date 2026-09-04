# Sprint 065 - backend
# -----------------------------------------------

## Goal
Create the pure recognition domain model required by FEAT-009 without orchestration, persistence, WebSocket, or tracking integration.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Domain Model
- [x] Create the recognition package following the backend hexagonal structure.
- [x] Add `RecognitionCategory` enum or equivalent domain value for `LETTER`, `NUMBER`, `SHAPE`, `COLOR`, and `ANIMAL`.
- [x] Add `RecognitionState` payload model with FEAT-009 fields.
- [x] Add `RecognitionAttemptContext` model with FEAT-009 fields.
- [x] Add defaults or configuration hooks for 5 rounds and 2-3 options.
- [x] Ensure recognition models do not contain `topicId`, `promptType`, `biomeCode`, `totalTimeouts`, or motor timeout fields.

### Tests
- [x] Unit test `RecognitionState` can represent an initial round.
- [x] Unit test retry counters and hint state can be represented.
- [x] Unit test `RecognitionAttemptContext` stores target, selected option, options, first-try flag, hint flags, attempt number, and response time.
- [x] Unit test recognition models do not require framework or persistence dependencies.

## Manual Tests
- Not required. This is a domain-only sprint.

## Risks
- Over-modeling persistence details here would make later engine work harder for junior developers.
- Reintroducing prompt, biome, topic, or timeout fields would contradict FEAT-009.

## Dependencies
- FEAT-007 game domain model.
- Sprint 063 if category names depend on content conventions.

## Agent Instruction
- Keep all classes framework-free where possible.
- Do not add JPA entities, migrations, REST endpoints, or WebSocket payloads.
- Do not call content, tracking, world, avatar, or frontend-specific code.
- Keep code, comments, and names in English.

## Notes
This sprint establishes the vocabulary used by the concrete `RecognitionEngine`.

## Review

completed_tasks:
  - Created `game.model.recognition` package with `RecognitionState`, `RecognitionAttemptContext`, and `RecognitionDefaults`.
  - Created `RecognitionCategory` enum in `game.model.enums` with LETTER, NUMBER, SHAPE, COLOR, ANIMAL.
  - `RecognitionState` includes all FEAT-009 fields: recognitionCategory, roundIndex, totalRounds (default 5), targetElementId, optionIds, selectedOptionId, roundsShownElementIds, currentRoundAttemptCount, currentRoundConsecutiveFailures, totalIncorrectAttempts, totalCorrectFirstTry, hintActive, hintTriggeredAtAttempt, roundStartedAt, lastActionAt, currentDifficultyLevel, pendingDifficultyLevel.
  - `RecognitionAttemptContext` includes all FEAT-009 fields: engineType (defaults to RECOGNITION), recognitionCategory, roundIndex, targetElementId, selectedOptionId, optionIds, isFirstTry, hintActive, hintTriggeredBeforeAnswer, attemptNumberInRound, responseTimeMs.
  - `RecognitionDefaults` provides constants: DEFAULT_TOTAL_ROUNDS=5, MIN_OPTIONS_PER_ROUND=2, MAX_OPTIONS_PER_ROUND=3, DEFAULT_DIFFICULTY_LEVEL=1.
  - No `topicId`, `promptType`, `biomeCode`, `totalTimeouts`, or timeout fields present in any recognition model.
  - All classes are plain POJOs with no Spring, JPA, or framework dependencies.
  - 12 unit tests pass: RecognitionStateTest (7), RecognitionAttemptContextTest (4), RecognitionCategoryTest (1).

incomplete_tasks:
  - None.

contract_changes:
  - No contract changes. This sprint is domain-only with no endpoint or schema modifications.

learnings:
  - The content module already has a `RecognitionType` enum with identical values. A separate `RecognitionCategory` was created in the game domain to maintain hexagonal boundary independence — the game domain should not depend on the content module's vocabulary.
  - The `RecognitionState` constructor applies defaults (totalRounds=5, difficultyLevel=1, empty lists) so that initial state creation is safe without requiring callers to know the defaults.

next_sprint_suggestions:
  - Implement the concrete `RecognitionEngine` logic using `RecognitionState` as the engine payload, replacing the current stub.
  - Add serialization/deserialization between `RecognitionState` and the `GameState.enginePayload` JSON string.
  - Define the recognition action types (e.g., SELECT_OPTION, REQUEST_HINT) as a domain enum for the engine's `processAction`.

verification:
  - All 12 unit tests pass (RecognitionStateTest: 7, RecognitionAttemptContextTest: 4, RecognitionCategoryTest: 1).
  - Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
  - No framework dependencies (Spring, JPA, Jakarta) in recognition domain models.
  - No prohibited fields present (topicId, promptType, biomeCode, totalTimeouts, timeout fields).
  - All FEAT-009 fields present in `RecognitionState`: recognitionCategory, roundIndex, totalRounds, targetElementId, optionIds, selectedOptionId, roundsShownElementIds, currentRoundAttemptCount, currentRoundConsecutiveFailures, totalIncorrectAttempts, totalCorrectFirstTry, hintActive, hintTriggeredAtAttempt, roundStartedAt, lastActionAt, currentDifficultyLevel, pendingDifficultyLevel.
  - All FEAT-009 fields present in `RecognitionAttemptContext`: engineType (defaults to RECOGNITION), recognitionCategory, roundIndex, targetElementId, selectedOptionId, optionIds, isFirstTry, hintActive, hintTriggeredBeforeAnswer, attemptNumberInRound, responseTimeMs.
  - `RecognitionDefaults` provides constants: DEFAULT_TOTAL_ROUNDS=5, MIN_OPTIONS_PER_ROUND=2, MAX_OPTIONS_PER_ROUND=3, DEFAULT_DIFFICULTY_LEVEL=1.
  - `RecognitionCategory` enum has all 5 values: LETTER, NUMBER, SHAPE, COLOR, ANIMAL.
  - Domain models are plain POJOs with no framework dependencies, maintaining hexagonal architecture boundaries.
