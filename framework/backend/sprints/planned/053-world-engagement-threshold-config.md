# Sprint 053 - backend
# -----------------------------------------------

## Goal
Implement `engagementThresholdConfig(childProfileId)` for world engagement pattern detection using v1 global defaults.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Configuration
- [ ] Add `WorldEngagementThresholdConfig` model with window size `N` and threshold `M`.
- [ ] Add internal port/use case `engagementThresholdConfig(childProfileId)`.
- [ ] Return v1 defaults `N=3`, `M=2`.
- [ ] Keep implementation ready for future per-child override without adding persistence now.

### Pattern Evaluation
- [ ] Add small service that evaluates abandoned games by `engineType` inside a `ChildSession` window.
- [ ] Ensure one abandonment never triggers a pattern.
- [ ] Ensure `IGNORED` proposals do not affect priority in v1.
- [ ] Return a soft priority adjustment, never total exclusion.

### Tests
- [ ] Unit test default config returns N=3 and M=2.
- [ ] Unit test one abandonment does not trigger adjustment.
- [ ] Unit test two of last three abandonments for same engine triggers adjustment.
- [ ] Unit test ignored proposals do not trigger adjustment.
- [ ] Unit test adjustment is temporary/in-memory only.

## Manual Tests
- Not required. This is pure domain/application logic.

## Risks
- Persisting engagement labels would violate FEAT-008.
- Treating ignored elements as priority signal in v1 could over-interpret normal child behavior.

## Dependencies
- Sprint 051 completed.

## Agent Instruction
- Do not add database tables for engagement config.
- Do not add parent override UI/API.
- Do not generate diagnostic labels.

## Notes
This sprint prepares scalable behavior while keeping v1 simple and safe for ages 3-4.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
