# Sprint 069 - backend
# -----------------------------------------------

## Goal
Extend game start orchestration with optional `launchContext` and candidate filtering before `RecognitionEngine` initialization.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Launch Context
- [x] Add a small internal `LaunchContext` model or DTO with optional `worldHostId`, `habitatTag`, `discoveryElementId`, and `narrativeContextId`.
- [x] Do not add `biomeCode` to `LaunchContext`.
- [x] Keep the context optional for existing game start callers.
- [x] Preserve current start-game behavior when no context is provided.

### Candidate Filtering
- [x] Resolve candidates before initializing `RecognitionEngine`.
- [x] Filter by activity/category using content module data.
- [x] Apply `launchContext.habitatTag` only when category is `ANIMAL`.
- [x] Respect NUMBER unlock state before selecting a recognition category.
- [x] Pass only resolved candidates to `RecognitionEngine`; do not pass `LaunchContext` directly into the engine.

### Tests
- [x] Unit test start game without launch context still works.
- [x] Unit test start game with habitat tag filters animal candidates.
- [x] Unit test non-animal categories do not require habitat tag.
- [x] Unit test NUMBER is excluded when unlock state says locked.
- [x] Unit test `RecognitionEngine` does not receive or depend on `LaunchContext`.

## Manual Tests
- Start a game through the existing dev/fake path without launch context and confirm it still starts.
- If a dev hook exists, start an animal recognition game with `habitatTag` and confirm no error is thrown.

## Risks
- Public REST/WebSocket contracts may need updates if `startGame` is externally exposed.
- Candidate filtering crosses game, content, and tracking boundaries; keep each module responsibility clear.

## Dependencies
- Sprint 063 completed.
- Sprint 064 completed for NUMBER unlock filtering.
- Existing FEAT-007 game shell implementation.

## Agent Instruction
- Keep this sprint focused on orchestration and filtering only.
- Do not implement session anti-repetition, scoring, or world habitat derivation here.
- Update `docs/contracts/api/openapi.json` or `websocket.json` only if an external endpoint/message contract changes.
- Keep code, comments, and names in English.

## Notes
This sprint turns external context into resolved candidates while keeping `RecognitionEngine` decoupled.

## Review

completed_tasks:
- LaunchContext model created with worldHostId, habitatTag, discoveryElementId, narrativeContextId (no biomeCode)
- GameOrchestrator interface extended with overloaded startGame(Long, Long, LaunchContext)
- Backward-compatible startGame(Long, Long) delegates to new method with null context
- GameState extended with candidates field (List<String>)
- GameOrchestratorService.resolveCandidates() implements full filtering pipeline: recognition category resolution from activity topics, NUMBER unlock filtering via FilterAllowedRecognitionCategoriesUseCase, habitat-based filtering for ANIMAL category via TopicUseCase
- getEngineParams() changed from {"difficultyLevelId":X} to {"candidates":[...]} format
- GameModuleConfiguration updated with TopicUseCase and FilterAllowedRecognitionCategoriesUseCase dependencies
- resolveEngine() fixed to throw EngineNotAvailableException when engine not found (was returning null)
- All 5 required unit tests pass
- Full test suite: 854 tests pass, 0 failures

incomplete_tasks:
- None

contract_changes:
- No external API/WebSocket contract changes. GameOrchestrator interface extended with backward-compatible overload.

learnings:
- Three parallel enum hierarchies (game.RecognitionCategory, tracking.RecognitionCategory, content.RecognitionType) require explicit mapping by name when crossing module boundaries
- Activity model does not carry recognition category directly; it is derived from the first topic's recognitionType via TopicUseCase
- RecognitionEngine.initGame already expected {"candidates":[...]} format; the previous getEngineParams returning {"difficultyLevelId":X} was a latent bug

next_sprint_suggestions:
- Session anti-repetition for candidate filtering (mentioned as future sprint in constraints)
- World habitat derivation from worldHostId (currently habitatTag comes directly from LaunchContext)
- External exposure of LaunchContext via WebSocket GameStartRequest if needed by frontend

verification:
- All 5 required unit tests pass (GameOrchestratorServiceCandidateFilteringTest: 5 tests, 0 failures).
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- LaunchContext model created with worldHostId, habitatTag, discoveryElementId, narrativeContextId (no biomeCode).
- GameOrchestrator interface extended with backward-compatible overload startGame(Long, Long, LaunchContext).
- Backward-compatible startGame(Long, Long) delegates to new method with null context.
- GameState extended with candidates field (List<String>).
- GameOrchestratorService.resolveCandidates() implements full filtering pipeline:
  * Recognition category resolution from activity topics via TopicUseCase
  * NUMBER unlock filtering via FilterAllowedRecognitionCategoriesUseCase
  * Habitat-based filtering for ANIMAL category via TopicUseCase.listTopicsByRecognitionTypeAndHabitat
- getEngineParams() changed from {"difficultyLevelId":X} to {"candidates":[...]} format.
- GameModuleConfiguration updated with TopicUseCase and FilterAllowedRecognitionCategoriesUseCase dependencies.
- resolveEngine() fixed to throw EngineNotAvailableException when engine not found (was returning null).
- RecognitionEngine does not receive or depend on LaunchContext (verified by grep).
- No biomeCode field in LaunchContext (verified by grep).
- Three parallel enum hierarchies (game.RecognitionCategory, tracking.RecognitionCategory, content.RecognitionType) require explicit mapping by name when crossing module boundaries.
