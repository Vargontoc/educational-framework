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

## Model Properties

### WorldEngagementThresholdConfig

Runtime configuration for detecting temporary engagement patterns.

- `childProfileId`: Long, nullable in v1 because defaults are global.
- `windowSize`: Integer, required, v1 default `3`.
- `abandonmentThreshold`: Integer, required, v1 default `2`.

### WorldEngineEngagementPattern

Result of evaluating recent signals for one engine type.

- `engineType`: String or enum, required.
- `windowSize`: Integer, required.
- `abandonmentCount`: Integer, required.
- `patternDetected`: boolean, required.

### WorldEnginePriorityAdjustment

Soft runtime adjustment applied by world destination selection.

- `engineType`: String or enum, required.
- `priorityMultiplier`: decimal, required, greater than `0`, less than or equal to `1`.
- `reason`: String, optional, internal only. Do not expose to child-facing payloads.

### Rules

- v1 defaults are `windowSize = 3` and `abandonmentThreshold = 2`.
- One abandonment never triggers a pattern.
- `IGNORED` proposals do not affect priority in v1.
- Adjustment is temporary and in-memory for the current `ChildSession` only.
- Never exclude an engine completely; only reduce probability/priority softly.

## Tasks

### Configuration
- [ ] Add `WorldEngagementThresholdConfig` model with the properties listed in `Model Properties`.
- [ ] Add internal port/use case `engagementThresholdConfig(childProfileId)`.
- [ ] Return v1 defaults `N=3`, `M=2`.
- [ ] Keep implementation ready for future per-child override without adding persistence now.

### Pattern Evaluation
- [ ] Add small service that evaluates abandoned games by `engineType` inside a `ChildSession` window.
- [ ] Return `WorldEngineEngagementPattern` and `WorldEnginePriorityAdjustment` using the properties listed in `Model Properties`.
- [ ] Ensure one abandonment never triggers a pattern.
- [ ] Ensure `IGNORED` proposals do not affect priority in v1.
- [ ] Return a soft priority adjustment, never total exclusion.
- [ ] Apply the rules listed in `Model Properties`.

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
