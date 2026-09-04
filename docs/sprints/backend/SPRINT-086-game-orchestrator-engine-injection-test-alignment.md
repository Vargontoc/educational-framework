# Sprint 086 - backend
# -----------------------------------------------

## Goal
Make `GameOrchestratorService` accept its game engines via dependency injection instead of constructing `RecognitionEngine` internally, and realign `GameOrchestratorServiceTest` with the current, real recognition engine contract and engine-registration policy.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Root cause (from a test-suite triage session)

Running the full backend test suite surfaced a `NullPointerException` in `GameOrchestratorService.resolveEngine` (`state.getEngine().name()` on a null `engine`), because `startGame` never set `GameState.engine` on the state it creates — a real production bug affecting every activity, not just a test artifact. That part is already fixed in this session: `startGame` now resolves `EngineType` from `Activity.gameEngineType` via a new `resolveEngineType(Activity)` helper, throwing `EngineNotAvailableException` if the activity's engine type is missing/unknown, and the shared test fixtures (`createActivity`, `createRealGameState` in `GameOrchestratorServiceTest`) were updated to set a `RECOGNITION` engine so existing state-transition tests keep passing.

With the NPE gone, `resolveEngine` now actually reaches the real `RecognitionEngine` (registered unconditionally in `GameOrchestratorService`'s constructor: `engineInstances.putIfAbsent(EngineType.RECOGNITION.name(), new RecognitionEngine())`), and 6 tests in `GameOrchestratorServiceTest` fail against it — **not because the orchestrator is broken, but because the test suite's assumptions about the engine are stale**:

1. **Payload format mismatch (5 tests)**: `processAction_correctAction_calls_tracking`, `processAction_withDifferentGameIds_runConcurrently`, `processAction_completedAction_publishesGameSessionCompletedEvent`, `processAction_trackingFails_continuesWithoutTracking`, `processAction_completedAction_calls_game_completion_and_summary` all call `orchestratorService.processAction(gameId, "CORRECT:1", null, 2000)` — a `"CORRECT:<n>"` / `"INCORRECT:<n>"` string convention that matches the old generic `FakeGameEngine`, not `RecognitionEngine.processAction`, which expects a structured payload it can parse a `selectedOptionId` out of and compares against `RecognitionState.targetElementId` (an id generated internally by the engine's own round logic, per FEAT-009). The literal string `"CORRECT:1"` never matches, so every action comes back `INCORRECT` and the game never completes in the 3-action test.
2. **Stale engine-availability premise (1 test)**: `readyGame_notDevProfile_throwsEngineNotAvailable` builds a fresh `GameOrchestratorService` and expects `RECOGNITION` to be unavailable ("not dev profile"), but the constructor registers `RECOGNITION` unconditionally today — there is no profile-gated engine registration in the current code. This test's premise reflects an older policy that no longer exists.

**Deeper architectural issue**: `GameOrchestratorService` constructs `new RecognitionEngine()` itself instead of receiving engines through its constructor. This is why `GameOrchestratorServiceTest` cannot substitute a lightweight test double for the engine and is forced to exercise the real, business-rule-heavy `RecognitionEngine` at the orchestration-logic test level — the two concerns (game lifecycle orchestration vs. engine-specific recognition rules) are conflated in this one test class. `GameEnginePort` and the old `FakeGameEngine` exist precisely to keep these decoupled; wiring should restore that separation.

## Tasks

### Engine Injection
- [ ] Change `GameOrchestratorService`'s constructor to accept its engine registry (e.g. `Map<String, GameEnginePort>` or a small `GameEngineRegistry` port) instead of instantiating `RecognitionEngine` internally.
- [ ] Update `GameModuleConfiguration` to build and inject that registry (still registering `RecognitionEngine` for `RECOGNITION` in production wiring).
- [ ] Confirm `EngineType`/engine-lookup behavior (`resolveEngine`, `EngineNotAvailableException`) is unchanged from the caller's perspective — this is a wiring change, not a behavior change.

### Test Realignment
- [ ] Update `GameOrchestratorServiceTest` to inject a lightweight fake `GameEnginePort` (reintroduce/reuse a `FakeGameEngine`-style test double) for the 5 `processAction_*` tests currently sending `"CORRECT:N"`/`"INCORRECT:N"` payloads, so orchestration behavior (tracking calls, event publishing, completion handling) is tested independently of `RecognitionEngine`'s internal round/target-element logic.
- [ ] Re-evaluate `readyGame_notDevProfile_throwsEngineNotAvailable`: either remove it if profile-gated engine availability is no longer a real policy, or reintroduce that gating in `GameOrchestratorService`/`GameModuleConfiguration` if it is still an intended behavior — confirm which with whoever owns the current recognition-engine work (sprints 064-073) before deciding.
- [ ] Keep (or add) at least one orchestrator-level integration-style test that exercises the real `RecognitionEngine` end-to-end for a couple of actions, so the real payload contract still has coverage somewhere — just not baked into every generic lifecycle test.

## Manual Tests
- Not required — this is an orchestration wiring and unit-test change with no user-facing behavior difference (assuming the fake-engine test double correctly represents `GameEnginePort`'s contract).
- After the fix, `mvn -o test -Dtest=GameOrchestratorServiceTest` should pass all tests without touching `RecognitionEngine` internals.

## Risks
- `RecognitionEngine`'s real business rules (FEAT-009: rounds, hint activation, retry-until-correct, `RecognitionState`/`RecognitionAttemptContext`) are being actively implemented across sprints 064-073 in parallel with this triage — coordinate before changing `GameOrchestratorService`'s constructor signature to avoid clobbering concurrent work on `GameModuleConfiguration`/`RecognitionEngine`.
- Removing `readyGame_notDevProfile_throwsEngineNotAvailable` outright could silently drop an intended product rule (e.g. "recognition engine only available in dev until content is ready") if that rule was meant to still exist but got dropped by accident during recent recognition-engine sprints — verify intent before deleting.

## Dependencies
- Sprints 064-068 (already implemented per `git log`: "Sprint 64 & 65 implemented", "SPRINT-66/67/68 implemented") for the current real `RecognitionEngine` contract this sprint needs to align tests against.
- `game/engine/FakeGameEngine.java` (or its reintroduction) as the test double for orchestrator-level tests.

## Agent Instruction
- Do not change `RecognitionEngine`'s business behavior (rounds, hints, scoring) as part of this sprint — this sprint is about test isolation and orchestrator wiring, not engine rules.
- Do not silently delete `readyGame_notDevProfile_throwsEngineNotAvailable` without confirming whether profile-gated engine availability is still an intended product rule.
- Keep `GameEnginePort` as the seam between orchestration and engine-specific logic; do not have `GameOrchestratorService` reach into `RecognitionEngine`-specific internals.
- Keep code, comments, and names in English.

## Notes
Found while triaging the full backend test suite (fixing Testcontainers/Postgres config and a missing `TelegramPort` bean in the same session). The `startGame` NPE fix and test-fixture engine assignment are already applied directly (pure bug fix, not a design decision). This sprint covers the remaining, genuinely business/architecture-shaped correction: restoring orchestrator/engine test isolation and confirming the still-open engine-availability policy question.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
