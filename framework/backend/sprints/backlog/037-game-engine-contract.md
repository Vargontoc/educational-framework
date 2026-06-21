# Sprint 037 - backend
# -----------------------------------------------

## Goal
Define the pure `GameEngine` contract and add a simple fake engine for testing the shell without implementing a real minigame.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Engine Contract
- [ ] Add `GameEngine` interface or abstract contract.
- [ ] Include `initGame(...)`, `processAction(...)`, `getNextElement(...)`, `isGameComplete(...)`, and `buildSummary(...)`.
- [ ] Ensure the contract does not include `evaluateAdaptiveDifficulty(...)`.
- [ ] Ensure the contract has no tracking, avatar, WebSocket, or repository dependency.

### Fake Engine
- [ ] Add a test-only fake engine that completes after a small number of actions.
- [ ] Support at least one correct action, one incorrect action, and one timeout result.
- [ ] Return attempt context for at least one engine-specific value.

### Tests
- [ ] Unit test engine contract can initialize game state from input data.
- [ ] Unit test fake engine processes a correct action.
- [ ] Unit test fake engine marks completion after configured number of actions.
- [ ] Unit test no adaptive difficulty method exists on the contract.

## Manual Tests
- Not required. This is a contract and unit-test sprint.

## Risks
- A fake engine that is too realistic can turn into a hidden minigame implementation.
- Letting engines call tracking would violate FEAT-007.

## Dependencies
- Sprint 036 completed.

## Agent Instruction
- Keep the fake engine in test sources unless there is a clear dev-only package pattern.
- Do not implement any real minigame.
- Do not add WebSocket handling.

## Notes
This sprint enables junior developers to test orchestration without waiting for real engines.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
