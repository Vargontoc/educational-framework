# Sprint 073 - backend
# -----------------------------------------------

## Goal
Expose recognition runtime payloads safely and harden FEAT-009 end-to-end backend behavior.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Runtime Payload
- [ ] Map `RecognitionState` into the existing game state/event payload shape.
- [ ] Include `recognitionCategory`, `roundIndex`, `totalRounds`, `targetElementId`, `optionIds`, and `hintActive`.
- [ ] Include completion result with `STARS` only as child-facing score.
- [ ] Do not expose raw response times, percentages, precision, or detailed attempts as child-facing data.
- [ ] Do not add frontend asset paths as backend domain fields.

### Contract And Regression
- [ ] Update `docs/contracts/api/websocket.json` if the game WebSocket payload contract changes.
- [ ] Update `docs/contracts/api/openapi.json` only if REST endpoints changed in earlier sprints.
- [ ] Run the full backend test suite.
- [ ] Add or adjust integration tests for start, retry, hint, completion, and stars payloads if an existing pattern exists.

### Edge Cases
- [ ] Verify too few candidates falls back safely.
- [ ] Verify missing `habitatTag` does not block game start.
- [ ] Verify repeated incorrect answers beyond hint activation keep the same round active.
- [ ] Verify exactly 5 completed rounds produce a completed game with stars.

## Manual Tests
- Start backend locally.
- Start a recognition game from the normal game entry point.
- Select one wrong option twice and confirm the same round remains active and hint activates.
- Select the correct option and confirm the game advances.
- Complete 5 rounds and confirm a stars result is emitted.
- If world integration is available, start an animal recognition game from world and confirm it starts safely.

## Risks
- WebSocket contract changes can affect frontend; keep payload additive when possible.
- Full integration can reveal missing content seeds or frontend contract assumptions.
- Earlier sprints may leave small mismatches between documented FEAT-009 fields and implemented DTO names.

## Dependencies
- Sprints 062 through 072 completed or explicitly skipped with documented reason.
- Existing game WebSocket lifecycle implementation.

## Agent Instruction
- Do not introduce new feature scope in this sprint.
- Backend provides state; frontend decides visual layout, skins, animations, and touch rendering.
- Regenerate/update API contract files only when actual external contracts change.
- Keep code, comments, and names in English.

## Notes
This sprint is a stabilization and backend/frontend contract handoff pass, not a place to add new recognition variants.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
