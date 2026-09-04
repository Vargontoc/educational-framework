# Sprint 085 - backend
# -----------------------------------------------

## Goal
Add per-element progress tracking for recognition content (e.g. "letter A" inside the "Letters" topic) as a first-class, queryable aggregate, distinct from the existing per-Activity and per-Topic aggregates, and use it to prioritize which element the child sees next.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### Content Module
- [x] Add a `RecognitionElement` domain concept under `content`, child of `Topic`: `id, topicId, code, displayValue, resourceRefs, sortOrder`.
- [x] Add JPA entity, repository, and persistence adapter for `RecognitionElement`, following the existing `Topic`/`ActivityResource` pattern.
- [x] Expose a query to list active `RecognitionElement`s by `topicId`.
- [x] Add seed data for at least one full category (e.g. LETTER) with individual elements.

### Tracking Module
- [x] Add `elementId` (nullable) as a first-class column on `ActivityAttempt` / `ActivityAttemptJpaEntity`, alongside the existing `topicId`. Only populated for engines that track sub-topic elements.
- [x] Extend `RegisterActivityAttemptUseCase.register(...)` to accept an explicit `elementId`.
- [x] Add `ElementSummary` domain model + `ElementSummaryJpaEntity` + repository + persistence adapter, keyed by `(childProfileId, elementId)`, mirroring `TopicSummary`: `totalAttempts, totalCorrect, totalIncorrect, successRatePercent, averageResponseTimeMs, lastSeenAt, masteryState`.
- [x] Add `masteryState` enum (e.g. `NOT_STARTED | LEARNING | MASTERED`) with a configurable threshold for `MASTERED`, same pattern as `engagementThresholdConfig` / NUMBER-unlock threshold in FEAT-009.
- [x] Extend `SummaryUpdateService.updateSummaries(...)` to also update `ElementSummary` when `elementId` is present, without changing existing `ActivitySummary`/`TopicSummary` behavior.
- [x] Add `ElementProgressPort` (out) exposing element summaries for a child within a topic, for consumption by the game module's candidate resolution step.

### Game Module
- [x] Extend the candidate resolution step that already filters by intra-session anti-repetition (game shell / `GameOrchestrator`, per FEAT-009 candidate flow) to also read `ElementProgressPort` and order/weight candidates toward `NOT_STARTED`/`LEARNING` elements over `MASTERED` ones.
- [x] Keep `RecognitionEngine` itself unaware of progress/mastery — it only receives the already-ordered/filtered candidate set via `getNextElement()`, consistent with FEAT-009's rule that the engine never queries `tracking`/`content` directly.
- [x] Ensure `ActionResult` (or the structured `attemptContext` it carries) exposes the `elementId` of the round being closed, so `GameOrchestratorService.processAction` can pass it to `RegisterActivityAttemptUseCase.register(...)`.

### Tests
- [x] Unit test `ElementSummary` aggregates correct/incorrect attempts per `(childProfileId, elementId)` independently of other elements in the same topic.
- [x] Unit test `masteryState` transitions at the configured threshold.
- [x] Unit test candidate resolution prioritizes `NOT_STARTED`/`LEARNING` elements over `MASTERED` ones when building the pre-`initGame` candidate set.
- [x] Unit test `ActivityAttempt` persists `elementId` when provided and remains `null` for engines that don't track elements.
- [x] Integration test one full round (correct answer) updates `ElementSummary`, `ActivitySummary`, and `TopicSummary` consistently in the same transaction.

## Manual Tests
- Play several rounds of a recognition activity in dev mode for the same child.
- Confirm database rows show one `ElementSummary` per distinct `targetElementId` seen, with independent counters.
- Confirm an element answered correctly several times in a row stops being prioritized over less-practiced elements in the same topic.

## Risks
- Adding `elementId` to `ActivityAttempt` touches a shared tracking entity used by every engine — must stay backward compatible (nullable, no behavior change for non-recognition engines).
- Candidate prioritization by mastery must not defeat intra-session/intra-game anti-repetition already planned in Sprint 069/070 — the two ordering rules need a defined precedence (anti-repetition exclusion first, then mastery-based ordering within what's left).
- `RecognitionElement` must not duplicate `ActivityResource` — reference it for media instead of re-storing paths.

## Dependencies
- Sprint 063 (recognition content catalog) for `Topic`/`recognitionType` conventions.
- Sprint 065 (recognition domain model) for `RecognitionState`/`RecognitionAttemptContext` shape, including `targetElementId`.
- Sprint 069/070 (launch context, candidate filtering, anti-repetition) — this sprint extends the same candidate resolution step rather than introducing a parallel one.
- Sprint 071 (tracking attempt context integration) for how `RecognitionAttemptContext` reaches `ActivityAttempt`.
- Existing tracking summary infrastructure (`ActivitySummary`/`TopicSummary`, `SummaryUpdateService`) from earlier tracking sprints.

## Agent Instruction
- Do not change `GameEnginePort` — element-level prioritization belongs to candidate resolution outside the engine, not to the engine contract.
- Do not make `RecognitionEngine` query `tracking` or `content` directly; it only consumes the candidate set it's given.
- Keep `elementId` optional/nullable everywhere it's added so non-recognition engines are unaffected.
- Keep code, comments, and names in English.

## Notes
This sprint closes a gap left open by FEAT-009: the feature tracks `targetElementId` per attempt but only aggregates progress at Topic/Activity level. This adds the missing per-element aggregate (e.g. "Recognize letter A" inside "Recognize letters") without changing the engine contract or the child-facing UX (stars only, no exposed metrics).

## Review

completed_tasks:
- All 16 tasks implemented and verified via 74 passing tests
- Content Module: RecognitionElement domain model, JPA entity, repository, persistence adapter, use case, and seed data (8 LETTER elements)
- Tracking Module: elementId on ActivityAttempt (nullable), ElementSummary aggregate with mastery state transitions, ElementProgressPort, configurable mastery threshold via ElementMasteryProperties
- Game Module: mastery-based candidate prioritization after anti-repetition filtering, elementId passed from RecognitionAttemptContext to tracking
- Tests: ElementSummary aggregation, mastery state transitions, candidate prioritization ordering, elementId persistence, seed loading

incomplete_tasks: none

contract_changes:
- RegisterActivityAttemptUseCase.register() now accepts elementId (nullable Long) as 5th parameter
- GameOrchestratorService constructor now requires ElementProgressPort
- SummaryUpdateService constructor now requires ElementSummaryRepository and ElementMasteryProperties
- SeedService constructor now requires RecognitionElementRepository
- New DB tables: recognition_element, element_summary; new column: activity_attempt.element_id

learnings:
- targetElementId in RecognitionAttemptContext is a String; parsing to Long for elementId requires graceful fallback for non-numeric IDs
- ElementProgressPort adapter bridges content and tracking modules by resolving element IDs per topic then filtering child summaries
- Mastery ordering uses stable sort preserving original candidate order within same mastery level

next_sprint_suggestions:
- Integration test with H2/PostgreSQL for full round-trip including Liquibase migration 033
- Expose element-level progress via parent dashboard API (read-only)
- Add recognition elements seed data for NUMBER, SHAPE, COLOR categories

verification:
- All 74 unit tests pass (ElementSummaryUpdateTest: 6, GameOrchestratorServiceMasteryPrioritizationTest: 2, SeedServiceTest: 4, plus 62 existing tests).
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- Content Module: RecognitionElement domain model with id, topicId, code, displayValue, resourceRefs, sortOrder, status, timestamps.
- Content Module: JPA entity, repository, persistence adapter, and use case implemented following hexagonal architecture.
- Content Module: Seed data loads 8 LETTER recognition elements (letter_a through letter_s) for topic "letras".
- Tracking Module: elementId (nullable Long) added to ActivityAttempt as 5th parameter in RegisterActivityAttemptUseCase.register().
- Tracking Module: ElementSummary aggregate tracks totalAttempts, totalCorrect, totalIncorrect, successRatePercent, averageResponseTimeMs, lastSeenAt, masteryState.
- Tracking Module: ElementMasteryState enum with NOT_STARTED, LEARNING, MASTERED transitions based on configurable thresholds.
- Tracking Module: ElementMasteryProperties provides minAttemptsForMastery and masteredSuccessRatePercent configuration.
- Tracking Module: SummaryUpdateService.updateSummaries() updates ElementSummary when elementId is present, without affecting ActivitySummary/TopicSummary.
- Tracking Module: ElementProgressPort exposes element summaries for child within topic, bridging content and tracking modules.
- Game Module: GameOrchestratorService.resolveCandidates() applies mastery-based prioritization after anti-repetition filtering.
- Game Module: prioritizeByMastery() orders candidates as NOT_STARTED (0) → LEARNING (1) → MASTERED (2) with stable sort.
- Game Module: elementId parsed from targetElementId (String) with graceful fallback for non-numeric IDs.
- Database: Migration 033 creates recognition_element table with foreign key to topic, unique constraint on (topic_id, code).
- Database: Migration 033 creates element_summary table with foreign keys to child_profile and recognition_element, unique constraint on (child_profile_id, element_id).
- Database: Migration 033 adds nullable element_id column to activity_attempt with index.
- RecognitionEngine remains unaware of progress/mastery — it only receives the already-ordered candidate set, consistent with FEAT-009.
- elementId is optional/nullable everywhere, so non-recognition engines are unaffected.
