# Sprint 069 - backend
# -----------------------------------------------

## Goal
Extend game start orchestration with optional `launchContext` and candidate filtering before `RecognitionEngine` initialization.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Launch Context
- [ ] Add a small internal `LaunchContext` model or DTO with optional `worldHostId`, `habitatTag`, `discoveryElementId`, and `narrativeContextId`.
- [ ] Do not add `biomeCode` to `LaunchContext`.
- [ ] Keep the context optional for existing game start callers.
- [ ] Preserve current start-game behavior when no context is provided.

### Candidate Filtering
- [ ] Resolve candidates before initializing `RecognitionEngine`.
- [ ] Filter by activity/category using content module data.
- [ ] Apply `launchContext.habitatTag` only when category is `ANIMAL`.
- [ ] Respect NUMBER unlock state before selecting a recognition category.
- [ ] Pass only resolved candidates to `RecognitionEngine`; do not pass `LaunchContext` directly into the engine.

### Tests
- [ ] Unit test start game without launch context still works.
- [ ] Unit test start game with habitat tag filters animal candidates.
- [ ] Unit test non-animal categories do not require habitat tag.
- [ ] Unit test NUMBER is excluded when unlock state says locked.
- [ ] Unit test `RecognitionEngine` does not receive or depend on `LaunchContext`.

## Manual Tests
- Start a game through the existing dev/fake path without launch context and confirm it still starts.
- If a dev hook exists, start an animal recognition game with `habitatTag` and confirm no error is thrown.

## Risks
- Public REST/WebSocket contracts may need updates if `startGame` is externally exposed.
- Candidate filtering crosses game, content, and tracking boundaries; keep each module responsibility clear.

## Dependencies
- Sprint 063 completed.
- Sprint 064 completed for NUMBER unlock filtering.
- Existing FEAT-007 game shell implementation.

## Agent Instruction
- Keep this sprint focused on orchestration and filtering only.
- Do not implement session anti-repetition, scoring, or world habitat derivation here.
- Update `docs/contracts/api/openapi.json` or `websocket.json` only if an external endpoint/message contract changes.
- Keep code, comments, and names in English.

## Notes
This sprint turns external context into resolved candidates while keeping `RecognitionEngine` decoupled.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
