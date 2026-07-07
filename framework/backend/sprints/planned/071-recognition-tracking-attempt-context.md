# Sprint 071 - backend
# -----------------------------------------------

## Goal
Register recognition attempt details in tracking through `ActivityAttempt` using `RecognitionAttemptContext`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Attempt Context
- [ ] Build `RecognitionAttemptContext` for every recognition action.
- [ ] Include `engineType`, `recognitionCategory`, `roundIndex`, `targetElementId`, `selectedOptionId`, and `optionIds`.
- [ ] Include `isFirstTry`, `hintActive`, `hintTriggeredBeforeAnswer`, `attemptNumberInRound`, and `responseTimeMs`.
- [ ] Do not include `topicId` in the recognition-specific context.

### Tracking Integration
- [ ] Send the attempt context through the existing tracking registration port.
- [ ] Register incorrect attempts as well as correct attempts.
- [ ] Keep `GameSessionSummary` as aggregate-only data.
- [ ] Avoid exposing tracking metrics as child-facing game output.

### Tests
- [ ] Unit test incorrect attempt creates a tracking attempt context.
- [ ] Unit test first correct attempt sets `isFirstTry` correctly.
- [ ] Unit test retry correct attempt sets `isFirstTry` false and attempt number correctly.
- [ ] Unit test hint flags are correct before and after hint activation.
- [ ] Unit or integration test tracking receives recognition context without adding it to `GameSessionSummary` detail.

## Manual Tests
- Play one recognition round in dev mode if available.
- Make one incorrect selection and then a correct selection.
- Confirm logs or database rows show two `ActivityAttempt` records with recognition attempt context.

## Risks
- Tracking context storage may be generic JSON; validate field names carefully.
- Existing tracking tests may assume simpler attempt contexts.

## Dependencies
- Sprint 067 completed.
- Existing tracking attempt registration from backend sprint 022 or equivalent.

## Agent Instruction
- Do not change dashboard APIs in this sprint unless required by compilation.
- Do not add child-facing metrics.
- Keep `ActivityAttempt` as the source of fine-grained attempt details.
- Keep code, comments, and names in English.

## Notes
This sprint connects FEAT-009 to FEAT-006 without changing the recognition game UX.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
