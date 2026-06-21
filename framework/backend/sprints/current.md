# Sprint 033 - backend
# -----------------------------------------------

## Goal
Add internal tracking use cases that evaluate achievements for game attempts and game completion without emitting WebSocket events.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Attempt-Based Achievements
- [ ] Extend the attempt registration result to include `unlockedAchievements[]`.
- [ ] Evaluate attempt-based achievement conditions after a registered attempt.
- [ ] Include `DIFFICULTY_INCREASED` achievements when adaptive difficulty changes.
- [ ] Avoid duplicate `ChildAchievement` rows for already-earned achievements.

### Completion-Based Achievements
- [ ] Add internal use case `evaluateGameCompletionAchievements(childProfileId, activityId)`.
- [ ] Evaluate `ACTIVITY_COMPLETED` conditions only when called by game.
- [ ] Return a lightweight list of newly unlocked achievements.

### Boundaries
- [ ] Ensure tracking does not emit WebSocket events.
- [ ] Ensure tracking does not call avatar or TTS.
- [ ] Read achievement catalog data through content-owned ports or existing allowed content access patterns.

### Tests
- [ ] Unit test attempt-based achievement unlock.
- [ ] Unit test difficulty-increased achievement unlock.
- [ ] Unit test completion-based achievement unlock.
- [ ] Unit test already-earned achievement is not duplicated.
- [ ] Unit test no WebSocket/avatar dependency exists in tracking services.

## Manual Tests
- Register attempts that satisfy a simple achievement condition through an internal test fixture.
- Confirm one `ChildAchievement` is stored.
- Repeat the same action and confirm no duplicate achievement is created.

## Risks
- Achievement rules can become too complex for a junior sprint if too many condition types are included.
- Reading content catalog data directly from persistence could break module boundaries.
- Emitting events from tracking would conflict with FEAT-007.

## Dependencies
- Sprint 026 tracking achievements completed.
- Sprint 024 tracking adaptive difficulty completed.
- Sprint 032 recommended if completion achievements need session summary context.

## Agent Instruction
- Keep the first implementation minimal: only conditions needed by FEAT-007 (`DIFFICULTY_INCREASED`, `ACTIVITY_COMPLETED`, and one simple streak/attempt condition if already modeled).
- Return data to callers; do not publish events.
- Do not implement game or WebSocket code in this sprint.

## Notes
FEAT-007 expects the game orchestrator to trigger evaluation and then emit `GAME_ACHIEVEMENT_UNLOCKED` itself.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
