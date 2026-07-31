# Sprint 064 - backend
# -----------------------------------------------

## Goal
Add backend readiness for unlocking NUMBER recognition only after sufficient LETTER and SHAPE mastery.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Tracking Readiness
- [ ] Add a tracking query or service method that evaluates first-try success history for LETTER and SHAPE recognition attempts.
- [ ] Use all seen difficulties for the child when calculating unlock readiness.
- [ ] Keep the unlock threshold configurable, not hardcoded.
- [ ] Return a simple unlock state for NUMBER recognition.

### Category Selection
- [ ] Ensure topic/category selection excludes NUMBER when it is not unlocked.
- [ ] Ensure `RecognitionEngine` receives only already-allowed categories.
- [ ] Keep the unlock as a positive progression state for dashboard/adult contexts, not child-facing failure.

### Tests
- [ ] Unit test NUMBER is locked by default for a child without history.
- [ ] Unit test NUMBER unlocks when LETTER and SHAPE meet the configured threshold.
- [ ] Unit test partial mastery does not unlock NUMBER.
- [ ] Unit test `RecognitionEngine` is not involved in unlock decisions.

## Manual Tests
- Not required unless a dev dashboard/API exposes unlock state. If available, seed attempts for LETTER and SHAPE and verify NUMBER becomes eligible.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
