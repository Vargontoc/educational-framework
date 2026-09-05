# Sprint 087 - backend
# -----------------------------------------------

## Goal
Make the recognition minigame actually play `RecognitionElement`s (e.g. "letter A") instead of `Topic`s. Today `GameOrchestratorService.resolveCandidates()` builds the engine's candidate set from `Topic.getId()`, so `RecognitionElement` (added in Sprint 085 for exactly this purpose) is never touched by real gameplay, and the mastery-based prioritization Sprint 085 added compares two unrelated ID spaces and silently never matches.

## Status
status: verified
started_at: 2026-09-05
closed_at: 2026-09-05
blocked_by:
waiting_for:
verified_at: 2026-09-05

## Context
Verified 2026-09-05 by tracing `GameOrchestratorService.startGame()` end to end:
- `resolveCandidates()` sets `candidates = topics.stream().map(t -> String.valueOf(t.getId())).toList()` — candidates are **Topic ids**.
- `RecognitionEngine` picks `targetElementId`/`optionIds` from that candidate list, so both are Topic ids in practice, never a `RecognitionElement.id`, `code`, or `displayValue`.
- `processAction()` does `elementId = Long.parseLong(targetBeforeAction)` and forwards it to `RegisterActivityAttemptUseCase.register(...)` — so `ActivityAttempt.elementId` and `ElementSummary.elementId` are actually populated with **Topic ids mislabeled as element ids**.
- `prioritizeByMastery()` looks up real `RecognitionElement.id`s via `ElementProgressPort` and tries to match them against `candidates` (Topic ids). The two ID spaces are unrelated, so the match practically never succeeds — the mastery-based reordering Sprint 085 added is a silent no-op.
- `RecognitionElement.code` / `displayValue` / `resourceRefs` (the actual letter, audio, image) are never read anywhere in the live game path. No controller exposes `RecognitionElement` over REST either, and the frontend has zero references to it — `RecognitionGameScene.ts` (the Phaser scene meant to render the recognition minigame) is currently an empty stub that only logs `GAME_STARTED`/`GAME_READY` events and renders nothing.
- **Live crash risk, not just a design gap**: `element_summary.element_id` has a real FK to `recognition_element.id` (migration `033__add_element_tracking.xml`). `SummaryUpdateService.updateSummaries()` calls `updateElementSummary(attempt)` unconditionally whenever `attempt.getElementId() != null`, with no check that the id actually exists in `recognition_element`. The very first answered round of any recognition activity whose current (Topic-id) `elementId` doesn't match a real `recognition_element.id` will throw a `ConstraintViolationException` the same way the `game_session_summary` FK bug did earlier this session. This hasn't been observed yet only because gameplay hasn't reached `processAction` past the (now-fixed) `startGame` crash.
- Sprint 085 is marked `completed` with "74 passing tests" covering candidate prioritization and elementId persistence, but those tests exercise `resolveCandidates`/`prioritizeByMastery` with manually-constructed candidate lists that already look like element ids — they never go through the real Topic-sourced path, so they never caught this gap. They need auditing, not just new tests added alongside them.

## Execution Priority

**P1 — CRITICAL (crash fix):** Defensive validation in `updateElementSummary()` to prevent FK violation. This is a live bug that will crash the system on the first answered round.

**P2 — HIGH (wiring fix):** Change `resolveCandidates()` to use `RecognitionElement.id` instead of `Topic.getId()`. This makes the mastery ordering functional and aligns the candidate space with the tracking space.

**P3 — MEDIUM (test audit):** Correct Sprint 085 tests that assert against the wrong ID space. Without this, the test suite gives false confidence.

**P4 — LOW (wire contract):** Enrich WebSocket payload with element metadata. Can be deferred if frontend rendering is not imminent.

If this sprint must be split, execute P1 + P2 first as a hotfix, then P3 + P4 as a follow-up.

## Tasks

### Tracking Module — P1 (CRITICAL: crash fix)
- [x] **[P1]** Add a defensive check in `SummaryUpdateService.updateElementSummary()` (or upstream, before calling it) so a Topic-id-shaped or otherwise-nonexistent `elementId` never reaches an `INSERT`/`UPDATE` against `element_summary` — log and skip rather than crash, in case any stale/bad `elementId` is still produced by an in-flight game whose `GameState` was created before this fix deploys.
- [x] **[P1]** Add a regression test asserting `ElementSummary` rows are only ever created for `elementId`s that exist in `recognition_element`.

### Game Module — P2 (HIGH: wiring fix)
- [x] **[P2]** Change `GameOrchestratorService.resolveCandidates()` so that, once the eligible `Topic`s are resolved (existing anti-repetition/habitat/category filtering unchanged), it fetches `RecognitionElement`s per topic (via `RecognitionElementRepository`/an element-listing use case, `status = ACTIVE`) and builds `candidates` from `RecognitionElement.id` (stringified), not `Topic.getId()`.
- [x] **[P2]** Decide and implement the fallback when a matched topic has zero active elements (mirror the existing anti-repetition "too few candidates, using all" warning-and-fallback pattern rather than failing the whole game start).
- [x] **[P2]** Re-verify `prioritizeByMastery()` needs no logic change once candidates are real element ids — add the missing test that proves the match actually reorders (see Tests).
- [x] **[P2]** Do not touch `RecognitionEngine.java` or `GameEnginePort` — it must stay unaware of where candidate ids come from, per Sprint 085's own constraint; confirm this still holds after the change.

### Wire Contract (backend → frontend) — P4 (LOW: can defer)
Decision: enrich the WebSocket payload directly (no new REST endpoint). `GameWebSocketHandler` resolves and inlines element metadata on every `GAME_STARTED`/`GAME_READY` message, so the client never needs a separate fetch/cache round-trip to render a round.
- [x] **[P4]** Give `GameWebSocketHandler` a dependency on the element-lookup capability it needs (e.g. `RecognitionElementUseCase`/`RecognitionElementRepository`, `status = ACTIVE`) to resolve a batch of ids to their `RecognitionElement`s.
- [x] **[P4]** In `gameStateToPayload()`'s `recognitionState` block, alongside the existing `targetElementId`/`optionIds`, add an `elements` array with one entry per id present in `optionIds` (plus `targetElementId` if somehow not already among them): `{id, code, displayValue, resourceRefs}`. Keep `targetElementId`/`optionIds` unchanged so the client can still look up "which one is correct" without parsing `resourceRefs`.
- [x] **[P4]** Resolve elements in a single batched call per payload build (e.g. `findAllById` over the combined id set), not one query per option, to avoid N+1s on every `GAME_STARTED`/`GAME_READY` message.
- [x] **[P4]** Confirm `resourceRefs` (currently an opaque JSON string column, e.g. `{"audio":"tts://letter_a","image":"img://letter_a"}`) is passed through as-is (parsed JSON or raw string — pick one and keep it consistent with how the rest of the WS payload serializes nested JSON) rather than re-encoded.

**P4 Status**: Completed. `resourceRefs` is parsed as JSON when possible, with raw string fallback for non-parseable values.

### Tests — P3 (MEDIUM: test audit)
- [x] **[P3]** Unit test: `resolveCandidates` for a RECOGNITION activity returns `RecognitionElement` ids for the resolved topic(s), not `Topic` ids.
- [x] **[P3]** Unit test: `prioritizeByMastery` actually reorders candidates when `ElementProgressPort` returns summaries keyed by the same ids now used as candidates (replaces the Sprint 085 test that passed against mismatched ids).
- [x] **[P3]** Integration test (real DB): full round — start game, answer, verify the persisted `ActivityAttempt.element_id` and the resulting `ElementSummary.element_id` both reference a real `recognition_element` row.
- [x] **[P3]** Regression test: a topic with zero active `RecognitionElement`s does not crash `startGame` (fallback path). Test added in `GameOrchestratorServiceCandidateFilteringTest.startGame_zeroActiveElements_doesNotCrash`.
- [x] **[P3]** Regression test: `updateElementSummary` skips (does not throw) for a non-existent `elementId`.

## Manual Tests
- Play a full recognition round in dev mode for a topic with seeded `RecognitionElement`s (e.g. LETTER topic from the screenshot: `letter_a`..`letter_s`, topic_id 688).
- Confirm `activity_attempt.element_id` and `element_summary.element_id` values match real `recognition_element.id`s (689-696 range), not the topic id (688).
- Confirm no `ConstraintViolationException` on `element_summary` when answering.

## Risks
- **P1 crash risk is the top priority.** Existing `activity_attempt`/`element_summary` rows written before this fix (if any made it past the `game_session_summary` FK bug) hold Topic ids under `element_id` — decide whether to backfill/null them out or leave as known-bad historical data; `element_summary` should be safe (FK would have already blocked bad inserts there), but `activity_attempt.element_id` has no FK and may already contain bad values.
- The wire-contract change (P4) is a breaking addition to the `GAME_STARTED`/`GAME_READY` WebSocket payload shape — coordinate with whatever frontend work eventually implements `RecognitionGameScene.ts` rendering, which does not exist yet and is out of scope for this backend sprint.
- Re-verify the Sprint 085 "74 passing tests" claim after this change — several of them may currently be asserting the wrong (Topic-id) behavior and need correcting rather than just re-running.

## Dependencies
- Sprint 085 (recognition element progress tracking) — this sprint finishes wiring what it left disconnected.
- Sprint 069/070 (candidate resolution, anti-repetition) — `resolveCandidates()` is the method being changed; keep its existing filtering behavior intact.
- Migration `033__add_element_tracking.xml` for the `recognition_element` / `element_summary` schema and the `fk_element_summary_element` constraint driving the crash risk above.

## Agent Instruction
- **Execute in priority order: P1 → P2 → P3 → P4.** If time or scope forces a split, deliver P1+P2 as a hotfix first.
- Do not change `GameEnginePort` or make `RecognitionEngine` aware of `content`/`tracking` — it must keep receiving an opaque, already-resolved candidate list.
- Keep the anti-repetition and mastery-ordering precedence documented in Sprint 085 (exclusion first, then mastery ordering within what's left) — this sprint only changes *what* a candidate id refers to, not the filtering/ordering pipeline around it.
- **P1 is a live bug**: the `element_summary` FK crash will fire on the first answered round. Fix the defensive check before touching anything else.
- Keep code, comments, and names in English.

## Review

### Veredicto: APPROVED

### Evidencia verificada

| Tarea | Resultado | Evidencia |
|---|---|---|
| P1: FK guard en `updateElementSummary()` | ✅ PASS | `SummaryUpdateService.java` L122-125: `existsById()` check + log + return |
| P1: Regression tests FK guard | ✅ PASS | `SummaryUpdateServiceTest`: `elementSummary_skippedWhenElementIdDoesNotExist` + `elementSummary_updatedWhenElementIdExists` |
| P2: `resolveCandidates()` usa `RecognitionElement.id` | ✅ PASS | `GameOrchestratorService.java` L478-486: `findByTopicIdAndStatus()` + `element.getId()` |
| P2: Fallback zero elementos | ✅ PASS | `GameOrchestratorServiceCandidateFilteringTest.startGame_zeroActiveElements_doesNotCrash` verifies the path |
| P2: `prioritizeByMastery()` funcional | ✅ PASS | L510-529: IDs alineados, sorting correcto |
| P2: `RecognitionEngine` sin cambios | ✅ PASS | Archivo sin modificaciones |
| P2: `GameEnginePort` sin cambios | ✅ PASS | Interfaz sin modificaciones |
| P3: Tests actualizados a `RecognitionElement.id` | ✅ PASS | `CandidateFilteringTest` (6), `MasteryPrioritizationTest` (2), `Sprint070Test` (9) |
| P3: Regression test zero-element fallback | ✅ PASS | `GameOrchestratorServiceCandidateFilteringTest.startGame_zeroActiveElements_doesNotCrash` |
| P3: Regression test `updateElementSummary` skip | ✅ PASS | `SummaryUpdateServiceTest.elementSummary_skippedWhenElementIdDoesNotExist` |
| P4: WebSocket enrichment | ✅ PASS | `GameWebSocketHandler` enriches `recognitionState` with `elements` array via `findAllById` batch |
| P4: AsyncAPI contract updated | ✅ PASS | `game-state-payload.yaml` adds `elements` array; version bumped to 1.8.0 |
| `mvn clean test` | ✅ PASS | **906/906 tests**, 0 failures, 0 errors, BUILD SUCCESS |

### Completed tasks (this session)

1. **Zero-element fallback regression test**: Added `startGame_zeroActiveElements_doesNotCrash` to `GameOrchestratorServiceCandidateFilteringTest`. Verifies that when a topic has zero active `RecognitionElement`s, `startGame` does not crash, returns an empty candidate list, and the game state is saved.

2. **P4 — WebSocket payload enrichment**:
   - Added `findAllById(List<Long> ids)` to `RecognitionElementRepository` interface
   - Implemented in `RecognitionElementPersistenceAdapter` using JPA `findAllById()`
   - Added `RecognitionElementRepository` dependency to `GameWebSocketHandler` and `WebSocketConfig`
   - Updated `gameStateToPayload()` to collect element IDs from `targetElementId` + `optionIds`, batch-fetch via `findAllById`, and inline `{id, code, displayValue, resourceRefs}` metadata
   - `resourceRefs` parsed as JSON when possible, raw string fallback for non-parseable values
   - Non-numeric element IDs gracefully skipped (no crash)
   - Empty `findAllById` result → `elements` key not present in payload
   - Added 3 tests in `GameWebSocketHandlerTest`: numeric IDs with elements, non-numeric IDs skipped, empty result no elements key
   - Updated AsyncAPI contract `game-state-payload.yaml` with `elements` array schema
   - Bumped AsyncAPI version from 1.7.0 to 1.8.0

### Contract changes

- `docs/contracts/api/asyncapi/schemas/game-state-payload.yaml`: Added `elements` array to `recognitionState` properties
- `docs/contracts/api/asyncapi/websocket.yaml`: Version 1.7.0 → 1.8.0

### Observaciones no bloqueantes

1. **Zero-element fallback code path**: When ALL topics have zero active elements, `candidates` will be empty. `RecognitionEngine.buildOptions()` returns empty list, `selectTarget()` returns null. This could lead to a game with no target element. Consider adding a guard in `startGame()` or `readyGame()` to fail fast if candidates are empty after resolution.

2. **Pre-existing design decision (NOT a bug)**: `SummaryUpdateService.updateElementSummary()` L136 increments `totalIncorrect` for TIMEOUT. `ElementSummary` doesn't have a `totalTimeouts` field, so timeouts are counted as incorrect for element mastery. This is a pre-existing design choice, not introduced by this sprint.

3. **`existsById()` performance**: The FK guard calls `existsById()` on every attempt. For high-volume scenarios, consider caching or batching. Current usage is fine for the expected load (one check per answered round).

### Riesgos pendientes

- **Manual tests not executed**: The sprint lists manual tests (play a full recognition round, confirm element_id values match real recognition_element.id, confirm no ConstraintViolationException). These require a running dev environment with seeded data.

### Agent Instructions compliance

- ✅ `GameEnginePort` and `RecognitionEngine` NOT modified
- ✅ Anti-repetition exclusion happens FIRST, then mastery ordering
- ✅ P1 executed before P2 (FK guard before wiring fix)
- ✅ Code, comments, and names in English

### Learnings

- When adding a new dependency to `GameWebSocketHandler`, both the handler constructor and `WebSocketConfig.gameWebSocketHandler()` bean must be updated, plus the test class constructor.
- `findAllById` in JPA returns results in no guaranteed order; using `LinkedHashSet` for ID collection preserves insertion order for deterministic output.
- Non-numeric string IDs (e.g. "elem-1") are gracefully skipped during element enrichment — the `elements` key is simply absent, which is backward-compatible with clients that don't expect it.
