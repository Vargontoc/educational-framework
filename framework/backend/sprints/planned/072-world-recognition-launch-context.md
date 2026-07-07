# Sprint 072 - backend
# -----------------------------------------------

## Goal
Make `world` provide recognition launch context for animal activities without coupling `RecognitionEngine` to `world`.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### World Context
- [ ] Detect when a world-started activity is a recognition animal activity using existing activity metadata.
- [ ] Derive `habitatTag` from the active destination or world host data.
- [ ] Include `worldHostId` when available.
- [ ] Include `discoveryElementId` and `narrativeContextId` only when the current world model already exposes them.
- [ ] Do not include or duplicate `biomeCode` in launch context.

### Game Start Integration
- [ ] Pass launch context to the game start use case or orchestrator.
- [ ] Preserve existing behavior for non-animal and non-recognition activities.
- [ ] Keep all world-specific decisions inside the world module.
- [ ] Ensure game and recognition do not call world directly.

### Tests
- [ ] Unit test animal recognition launch includes habitat tag.
- [ ] Unit test non-animal launch does not force habitat filtering.
- [ ] Unit test missing habitat falls back safely without blocking game start.
- [ ] Unit test there is no direct dependency from `RecognitionEngine` to world classes.

## Manual Tests
- Start a world destination with an animal discovery element in dev mode.
- Confirm the game starts normally.
- Confirm logs or debug output show launch context with `habitatTag` when available.

## Risks
- Existing world activity proposal code may not expose enough metadata; avoid large refactors.
- Wrong habitat derivation can reduce content variety but must not block the child flow.

## Dependencies
- Sprint 069 completed.
- Existing world game start integration from backend sprint 056 or equivalent.
- FEAT-008 world module behavior.

## Agent Instruction
- Keep world responsible for narrative context only.
- Do not move world state lookup into game or recognition.
- Do not add habitat sub-topics.
- Keep code, comments, and names in English.

## Notes
This sprint implements the FEAT-009 rule: world decides context, game receives opaque data, recognition receives only resolved candidates.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
