# Sprint 066 - backend
# -----------------------------------------------

## Goal
Implement recognition game initialization and round generation using candidates already resolved outside the engine.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Engine Initialization
- [ ] Create `RecognitionEngine` implementing the existing `GameEngine` contract.
- [ ] Implement `initGame` using candidates already provided by the game/content flow.
- [ ] Initialize `RecognitionState` with round counters, difficulty fields, timestamps, and empty shown-element history.
- [ ] Select a `targetElementId` for the first round.
- [ ] Build `optionIds` with 2-3 options and always include the target.

### Round Selection
- [ ] Avoid repeating targets already present in `roundsShownElementIds` when enough candidates exist.
- [ ] Use a safe fallback when candidate count is small.
- [ ] Keep category, habitat, NUMBER unlock, and session filtering outside the engine.
- [ ] Do not create timeout behavior.

### Tests
- [ ] Unit test `initGame` creates a valid first round.
- [ ] Unit test options include the target exactly once.
- [ ] Unit test option count is 2-3 when enough candidates exist.
- [ ] Unit test target selection avoids already shown elements when possible.
- [ ] Unit test engine does not query `world`, tracking, content repositories, or session registries directly.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
