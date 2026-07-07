# Sprint 067 - backend
# -----------------------------------------------

## Goal
Implement recognition action processing with retry-until-correct behavior and hint activation.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Action Processing
- [ ] Implement `processAction` for selected option actions.
- [ ] Return `CORRECT` when `selectedOptionId` matches `targetElementId`.
- [ ] Return `INCORRECT` when the selected option is not the target.
- [ ] Keep the same `targetElementId` and `optionIds` after incorrect answers.
- [ ] Increment round attempt counters and total incorrect counters correctly.
- [ ] Update `lastActionAt` after each action.

### Hint Rules
- [ ] Activate `hintActive` after 2 consecutive failures in the same round.
- [ ] Set `hintTriggeredAtAttempt` when the hint is first activated.
- [ ] Keep hint rendering outside backend domain logic.
- [ ] Do not return `TIMEOUT` from `RecognitionEngine`.

### Tests
- [ ] Unit test incorrect answer keeps the same round open.
- [ ] Unit test first incorrect answer does not activate hint.
- [ ] Unit test second consecutive incorrect answer activates hint.
- [ ] Unit test correct answer after failures returns `CORRECT` and preserves attempt data.
- [ ] Unit test `RecognitionEngine` never returns `TIMEOUT` for recognition actions.

## Manual Tests
- Not required. Unit tests cover domain behavior.

## Risks
- The shared `ActionResultType` may include `TIMEOUT`; recognition must not emit it for child actions.
- Advancing after an incorrect answer would break the no-failure rule for ages 3-4.

## Dependencies
- Sprint 066 completed.

## Agent Instruction
- Do not implement scoring or full game completion in this sprint.
- Do not call tracking directly unless the current game shell already requires attempt context from `processAction`.
- Keep child-facing penalty logic out of the backend domain.
- Keep code, comments, and names in English.

## Notes
This sprint implements the core 3-4 year old safety rule: retries are supportive, not punitive.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
