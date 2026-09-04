# Sprint 085 - backend
# -----------------------------------------------

## Goal
Add per-element progress tracking for recognition content (e.g. "letter A" inside the "Letters" topic) as a first-class, queryable aggregate, distinct from the existing per-Activity and per-Topic aggregates, and use it to prioritize which element the child sees next.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Content Module
- [ ] Add a `RecognitionElement` domain concept under `content`, child of `Topic`: `id, topicId, code, displayValue, resourceRefs, sortOrder`.
- [ ] Add JPA entity, repository, and persistence adapter for `RecognitionElement`, following the existing `Topic`/`ActivityResource` pattern.
- [ ] Expose a query to list active `RecognitionElement`s by `topicId`.
- [ ] Add seed data for at least one full category (e.g. LETTER) with individual elements.

### Tracking Module
- [ ] Add `elementId` (nullable) as a first-class column on `ActivityAttempt` / `ActivityAttemptJpaEntity`, alongside the existing `topicId`. Only populated for engines that track sub-topic elements.
- [ ] Extend `RegisterActivityAttemptUseCase.register(...)` to accept an explicit `elementId`.
- [ ] Add `ElementSummary` domain model + `ElementSummaryJpaEntity` + repository + persistence adapter, keyed by `(childProfileId, elementId)`, mirroring `TopicSummary`: `totalAttempts, totalCorrect, totalIncorrect, successRatePercent, averageResponseTimeMs, lastSeenAt, masteryState`.
- [ ] Add `masteryState` enum (e.g. `NOT_STARTED | LEARNING | MASTERED`) with a configurable threshold for `MASTERED`, same pattern as `engagementThresholdConfig` / NUMBER-unlock threshold in FEAT-009.
- [ ] Extend `SummaryUpdateService.updateSummaries(...)` to also update `ElementSummary` when `elementId` is present, without changing existing `ActivitySummary`/`TopicSummary` behavior.
- [ ] Add `ElementProgressPort` (out) exposing element summaries for a child within a topic, for consumption by the game module's candidate resolution step.

### Game Module
- [ ] Extend the candidate resolution step that already filters by intra-session anti-repetition (game shell / `GameOrchestrator`, per FEAT-009 candidate flow) to also read `ElementProgressPort` and order/weight candidates toward `NOT_STARTED`/`LEARNING` elements over `MASTERED` ones.
- [ ] Keep `RecognitionEngine` itself unaware of progress/mastery — it only receives the already-ordered/filtered candidate set via `getNextElement()`, consistent with FEAT-009's rule that the engine never queries `tracking`/`content` directly.
- [ ] Ensure `ActionResult` (or the structured `attemptContext` it carries) exposes the `elementId` of the round being closed, so `GameOrchestratorService.processAction` can pass it to `RegisterActivityAttemptUseCase.register(...)`.

### Tests
- [ ] Unit test `ElementSummary` aggregates correct/incorrect attempts per `(childProfileId, elementId)` independently of other elements in the same topic.
- [ ] Unit test `masteryState` transitions at the configured threshold.
- [ ] Unit test candidate resolution prioritizes `NOT_STARTED`/`LEARNING` elements over `MASTERED` ones when building the pre-`initGame` candidate set.
- [ ] Unit test `ActivityAttempt` persists `elementId` when provided and remains `null` for engines that don't track elements.
- [ ] Integration test one full round (correct answer) updates `ElementSummary`, `ActivitySummary`, and `TopicSummary` consistently in the same transaction.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
