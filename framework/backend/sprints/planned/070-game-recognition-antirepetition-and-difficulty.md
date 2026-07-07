# Sprint 070 - backend
# -----------------------------------------------

## Goal
Add game-shell support for recognition session anti-repetition and deferred adaptive difficulty.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Session Anti-Repetition
- [ ] Create a runtime registry for recent element ids by `childSessionId + topicId` or the existing equivalent identifiers.
- [ ] Filter recent elements before initializing `RecognitionEngine`.
- [ ] Register the `targetElementId` after a recognition round is completed correctly.
- [ ] Fall back safely when all or most candidates are recent.
- [ ] Clear registry entries when the child session closes if the current code exposes a close event/hook.

### Deferred Difficulty
- [ ] Store tracking difficulty recommendations in `pendingDifficultyLevel` during an active round.
- [ ] Keep `currentDifficultyLevel` unchanged while the child retries the same round.
- [ ] Promote `pendingDifficultyLevel` only after a correct answer closes the round or when the game ends.
- [ ] Apply promoted difficulty before generating the next round.

### Tests
- [ ] Unit test recent elements are excluded before engine initialization when alternatives exist.
- [ ] Unit test fallback still returns candidates when all are recent.
- [ ] Unit test completed round target is registered as recent.
- [ ] Unit test difficulty recommendation during retry does not change current round.
- [ ] Unit test pending difficulty is promoted after correct answer.

## Manual Tests
- Start two recognition games in the same child session with enough content and confirm the second game avoids recently completed targets when possible.
- If tracking recommendations can be forced in dev mode, trigger one during a retry and confirm the visible round does not change.

## Risks
- Missing session close events may require explicit cleanup later.
- Filtering too aggressively can leave the engine without enough options.
- Existing tracking integration may already apply difficulty immediately and need a small orchestrator refactor.

## Dependencies
- Sprint 068 completed.
- Sprint 069 completed.
- Existing tracking adaptive difficulty support.

## Agent Instruction
- Do not persist anti-repetition across days or sessions.
- Do not put session anti-repetition inside `RecognitionEngine`.
- Do not recompute options during an active retry.
- Keep code, comments, and names in English.

## Notes
This sprint handles game-shell responsibilities that are deliberately outside the motor.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
