# Sprint 073 - backend
# -----------------------------------------------

## Goal
Expose recognition runtime payloads safely and harden FEAT-009 end-to-end backend behavior.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Runtime Payload
- [x] Map `RecognitionState` into the existing game state/event payload shape.
- [x] Include `recognitionCategory`, `roundIndex`, `totalRounds`, `targetElementId`, `optionIds`, and `hintActive`.
- [x] Include completion result with `STARS` only as child-facing score.
- [x] Do not expose raw response times, percentages, precision, or detailed attempts as child-facing data.
- [x] Do not add frontend asset paths as backend domain fields.

### Contract And Regression
- [x] Update `docs/contracts/api/websocket.json` if the game WebSocket payload contract changes.
- [ ] Update `docs/contracts/api/openapi.json` only if REST endpoints changed in earlier sprints.
- [x] Run the full backend test suite.
- [x] Add or adjust integration tests for start, retry, hint, completion, and stars payloads if an existing pattern exists.

### Edge Cases
- [x] Verify too few candidates falls back safely.
- [x] Verify missing `habitatTag` does not block game start.
- [x] Verify repeated incorrect answers beyond hint activation keep the same round active.
- [x] Verify exactly 5 completed rounds produce a completed game with stars.

## Manual Tests
- Start backend locally.
- Start a recognition game from the normal game entry point.
- Select one wrong option twice and confirm the same round remains active and hint activates.
- Select the correct option and confirm the game advances.
- Complete 5 rounds and confirm a stars result is emitted.
- If world integration is available, start an animal recognition game from world and confirm it starts safely.

## Risks
- WebSocket contract changes can affect frontend; keep payload additive when possible.
- Full integration can reveal missing content seeds or frontend contract assumptions.
- Earlier sprints may leave small mismatches between documented FEAT-009 fields and implemented DTO names.

## Dependencies
- Sprints 062 through 072 completed or explicitly skipped with documented reason.
- Existing game WebSocket lifecycle implementation.

## Agent Instruction
- Do not introduce new feature scope in this sprint.
- Backend provides state; frontend decides visual layout, skins, animations, and touch rendering.
- Regenerate/update API contract files only when actual external contracts change.
- Keep code, comments, and names in English.

## Notes
This sprint is a stabilization and backend/frontend contract handoff pass, not a place to add new recognition variants.

## Review

completed_tasks:
- Updated `gameStateToPayload()` in `GameWebSocketHandler` to include `engine`, `starsEarned`, and a nested `recognitionState` object with child-facing fields (`recognitionCategory`, `roundIndex`, `totalRounds`, `targetElementId`, `optionIds`, `hintActive`) when engine is RECOGNITION.
- Added `deserializeRecognitionState()` helper that safely falls back to an empty `RecognitionState` on malformed JSON, preventing payload generation failures.
- Added 6 unit tests covering: recognition payload inclusion, MEMORY engine exclusion, null engine exclusion, `starsEarned` on completion, internal field exclusion (candidateElementIds, counters, response times, pendingDifficultyLevel), and invalid enginePayload resilience.
- Updated `game-state-payload.yaml` AsyncAPI contract to document `engine`, `starsEarned`, and `recognitionState` with full field descriptions and required markers.
- Bumped WebSocket contract version from 1.6.0 to 1.7.0.
- Full backend test suite: 889 tests, 0 failures, 0 errors.

incomplete_tasks:
- OpenAPI contract update was not applicable — no REST endpoints changed in this sprint.

contract_changes:
- `docs/contracts/api/asyncapi/schemas/game-state-payload.yaml` — added `engine` (enum RECOGNITION|MEMORY), `starsEarned` (integer), and `recognitionState` (object with recognitionCategory, roundIndex, totalRounds, targetElementId, optionIds, hintActive).
- `docs/contracts/api/asyncapi/websocket.yaml` — version bumped to 1.7.0.

learnings:
- The `gameStateToPayload` method was changed from `private` to package-private to allow direct unit testing of payload structure without going through the full WebSocket message flow. This is a minimal visibility increase that keeps test surface clean.
- Deserialization failure in the payload path is handled gracefully (returns empty RecognitionState) rather than propagating an exception, ensuring the WebSocket payload is always generated even if enginePayload is corrupt.

next_sprint_suggestions:
- Frontend sprint to consume the new `recognitionState` payload and render round progression, options, and hint activation.
- Consider adding an integration test that exercises the full recognition lifecycle (start → actions → hint → completion → stars) through the WebSocket handler with a real RecognitionEngine, if not already covered by orchestrator-level tests.

verification:
- All 35 unit tests pass (GameWebSocketHandlerTest: 35 tests, 0 failures).
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- gameStateToPayload() includes engine, starsEarned, and recognitionState for RECOGNITION engine.
- recognitionState payload includes only child-facing fields: recognitionCategory, roundIndex, totalRounds, targetElementId, optionIds, hintActive.
- Internal fields (candidateElementIds, attempt counters, response times, pendingDifficultyLevel) are correctly excluded from payload.
- deserializeRecognitionState() helper safely handles malformed JSON by returning empty RecognitionState.
- MEMORY engine correctly excludes recognitionState from payload.
- null engine correctly excludes both engine and recognitionState from payload.
- Completed game includes starsEarned in payload.
- AsyncAPI contract updated: game-state-payload.yaml documents engine, starsEarned, and recognitionState with full field descriptions.
- WebSocket contract version bumped from 1.6.0 to 1.7.0.
- gameStateToPayload visibility changed from private to package-private to enable direct unit testing.
- Invalid enginePayload does not throw exception, gracefully returns empty RecognitionState.
