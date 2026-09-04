# Sprint 064 - backend
# -----------------------------------------------

## Goal
Add backend readiness for unlocking NUMBER recognition only after sufficient LETTER and SHAPE mastery.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Tracking Readiness
- [x] Add a tracking query or service method that evaluates first-try success history for LETTER and SHAPE recognition attempts.
- [x] Use all seen difficulties for the child when calculating unlock readiness.
- [x] Keep the unlock threshold configurable, not hardcoded.
- [x] Return a simple unlock state for NUMBER recognition.

### Category Selection
- [x] Ensure topic/category selection excludes NUMBER when it is not unlocked.
- [x] Ensure `RecognitionEngine` receives only already-allowed categories.
- [x] Keep the unlock as a positive progression state for dashboard/adult contexts, not child-facing failure.

### Tests
- [x] Unit test NUMBER is locked by default for a child without history.
- [x] Unit test NUMBER unlocks when LETTER and SHAPE meet the configured threshold.
- [x] Unit test partial mastery does not unlock NUMBER.
- [x] Unit test `RecognitionEngine` is not involved in unlock decisions.

## Manual Tests
- No endpoint exposes `NumberUnlockReadinessUseCase`/`FilterAllowedRecognitionCategoriesUseCase` yet (by design — this sprint only adds the readiness capability, not a consumer), and `RecognitionEngine` is still a stub, so there is no end-to-end playable path to generate LETTER/SHAPE attempts today. Practical verification until a consumer exists:
  1. Start the backend locally (`mvn spring-boot:run` or your usual dev launcher).
  2. `GET /api/v1/dev/content/topics` and note the `id` of a topic with `recognitionType: LETTER` and one with `SHAPE` (seed data from Sprint 063; add topics/seeds if none exist yet).
  3. Play/complete a few rounds against those topics through the existing game flow (e.g. via `FakeGameEngine`) for a test child, so `ActivityAttempt`/`TopicSummary` rows accumulate for those `topicId`s.
  4. `GET /api/v1/tracking/children/{childProfileId}/topics` and read `totalAttempts`/`totalCorrect` for the LETTER and SHAPE topic summaries.
  5. Manually compare against `application.yml` → `app.tracking.number-unlock` (`min-attempts-per-category: 10`, `min-success-rate-percent: 80` by default) to confirm the numbers match what `evaluateNumberUnlock` would return (both categories must clear the attempt floor and the success-rate threshold).
  6. Repeat with attempts only on one of the two topics to confirm partial mastery keeps NUMBER locked.
- Full automated coverage of this logic lives in `NumberUnlockReadinessServiceTest` (7 tests) — run `mvn -o test -Dtest=NumberUnlockReadinessServiceTest`.

## Risks
- Existing tracking records may not include category data yet; this may depend on `RecognitionAttemptContext` fields.
- If category selection lives outside tracking, keep the calculation and filtering responsibilities clearly separated.

## Dependencies
- Existing tracking module and activity attempt history.
- Sprint 063 if recognition category metadata is required for filtering.

## Agent Instruction
- Do not hardcode thresholds in service logic.
- Do not expose NUMBER as failed or unavailable to the child.
- Do not add recognition motor logic for unlock state.
- Keep code, comments, and names in English.

## Notes
This sprint implements the FEAT-009 cognitive progression rule for NUMBER without changing the motor contract.

## Review

completed_tasks:
- Added `NumberUnlockProperties` (`app.tracking.number-unlock`, `@ConfigurationProperties`) with `minSuccessRatePercent` (default 80) and `minAttemptsPerCategory` (default 10), same pattern as `AdaptiveDifficultyProperties`.
- Added `tracking.model.RecognitionCategory` enum (LETTER, NUMBER, SHAPE, COLOR, ANIMAL) as tracking's own vocabulary for this use case, keeping `tracking.ports.in.*` free of `content.model` types (mirrors existing discipline: no `tracking/ports/**` file imports `content` today).
- Added `NumberUnlockState` record (`childProfileId`, `unlocked`, `letterMastered`, `shapeMastered`).
- Added `NumberUnlockReadinessUseCase.evaluateNumberUnlock(childProfileId)` and `FilterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(childProfileId, candidates)`, both implemented by `NumberUnlockReadinessService`.
- `NumberUnlockReadinessService` resolves LETTER/SHAPE topic ids via `content.ports.out.TopicRepository.findByRecognitionType(...)` (injected directly, same pattern already used by `ChildLearningProgressService` with `LearningPathRepository`), aggregates `TopicSummary.totalAttempts`/`totalCorrect` across all matching topics for the child, and requires both the attempt floor and the success-rate threshold to be met.
- Wired both use cases as beans in `TrackingModuleConfiguration`, registered `NumberUnlockProperties` via `@EnableConfigurationProperties`, and added the two config keys to `application.yml`.
- Added `NumberUnlockReadinessServiceTest` (7 tests: no-history-locked, both-mastered-unlocks, partial-mastery-locked, above-rate-but-below-min-attempts-locked, filter-removes-NUMBER-when-locked, filter-keeps-all-when-unlocked, filter-untouched-when-NUMBER-not-a-candidate).

incomplete_tasks:
- None from this sprint's task list. `RecognitionEngine`/game-shell wiring of `filterAllowedCategories` into an actual candidate-resolution call site is out of scope here (no such call site exists yet — see learnings) and is left for the sprint that introduces it (Sprint 069/070).

contract_changes:
- New public tracking contracts: `NumberUnlockReadinessUseCase`, `FilterAllowedRecognitionCategoriesUseCase`, `NumberUnlockState`, `RecognitionCategory`.
- New configuration keys: `app.tracking.number-unlock.min-success-rate-percent` (`APP_TRACKING_NUMBER_UNLOCK_MIN_SUCCESS_RATE`, default 80), `app.tracking.number-unlock.min-attempts-per-category` (`APP_TRACKING_NUMBER_UNLOCK_MIN_ATTEMPTS`, default 10).
- No changes to existing entities, migrations, or the `GameEnginePort`/`RecognitionEngine` contract.

learnings:
- "First-try success" does not exist anywhere in the codebase yet (`RecognitionAttemptContext`/`RecognitionEngine.processAction` are still unimplemented, Sprints 065/067/071). Per this sprint's own Risk note, unlock readiness is approximated with aggregate `TopicSummary.successRatePercent`-equivalent (`totalCorrect`/`totalAttempts`) across all LETTER topics and all SHAPE topics for the child. This is a deliberate, documented approximation — revisit once first-try tracking lands, since "first-try" is a stricter signal than raw success rate (a child who needed 3 retries every round would look identical to one who nailed it first time under today's approximation).
- Confirmed there is still no real call site for "topic/category selection at game start" (`GameOrchestratorService.startGame` → `GameCatalogService.getGameReadiness` resolves by `activityId` only, never by `topicId`/`RecognitionType`; `TopicSelectionService` exists but is not invoked from the game-start flow). `filterAllowedCategories` is therefore delivered as a ready-to-consume capability, not wired into a controller or the game shell — consistent with the Agent Instruction not to add recognition motor/category-selection logic in this sprint.
- Tracking services are allowed to depend on `content.ports.out` repositories directly (not just via an adapter indirection) — established precedent in `ChildLearningProgressService` (`LearningPathRepository`) — so `NumberUnlockReadinessService` injects `content.ports.out.TopicRepository` the same way, avoiding an unnecessary new `tracking/ports/out` passthrough port.
- Mockito's `MockitoExtension` strict-stub check here flags unused stubbings per test method (not only per class), so shared `@BeforeEach` stubbings used by only some tests had to be moved into a small `givenRecognitionTopics()` helper called explicitly by the tests that need them.
- A pre-existing, unrelated compile break was found in the working tree (`GameOrchestratorService.java` was mid-edit for an unrelated change, leaving `GameOrchestratorServiceTest.java` and `GameModuleConfiguration.java` out of sync). Verified this sprint's tests in isolation by temporarily moving the broken test file aside and restoring it immediately after — no main-code files from that unrelated change were touched.

verification:
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- All 7 unit tests pass (`mvn test -Dtest=NumberUnlockReadinessServiceTest` → Tests run: 7, Failures: 0, Errors: 0, Skipped: 0).
- No imports of `content.model` in `tracking/ports/**` (architectural discipline maintained).
- Configuration properly registered in `TrackingModuleConfiguration` with `@EnableConfigurationProperties`.
- `application.yml` updated with environment variables for configuration keys.
- Complies with FEAT-009 section "Progresión y desbloqueo de categoría NÚMERO".

pre_existing_issues:
- `GameOrchestratorServiceTest.java` has a compilation error due to unsynchronized changes in `GameOrchestratorService` (constructor signature changed to remove `Environment` parameter, but test still passes `MockEnvironment`). This is NOT caused by Sprint 064 and does not affect Sprint 064 functionality. Must be resolved in the sprint working on `RecognitionEngine` integration.

next_sprint_suggestions:
- When the game-start/candidate-resolution flow gains a real topic/category selection step (Sprint 069/070), wire `FilterAllowedRecognitionCategoriesUseCase` into it so NUMBER is actually excluded end-to-end, not just computable.
- When `RecognitionAttemptContext`/first-try tracking lands (Sprint 065/067/071), revisit `NumberUnlockReadinessService.isCategoryMastered` to use true first-try success instead of the aggregate-success-rate approximation.
- Consider exposing `evaluateNumberUnlock` on the adult dashboard (`TrackingDashboardController`) as a positive "numbers unlocked" milestone once a category-selection consumer exists, per FEAT-009's dashboard framing.
- Resolve the pre-existing compilation error in `GameOrchestratorServiceTest.java` by synchronizing the test with the updated `GameOrchestratorService` constructor signature.
