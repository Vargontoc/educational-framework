# Sprint 037 - backend
# -----------------------------------------------

## Goal
Define the pure `GameEngine` contract and add a simple fake engine for testing the shell without implementing a real minigame.

## Status
status: closed
started_at: 2026-06-21
closed_at: 2026-06-21
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
- All 4 engine contract tasks completed
- All 3 fake engine tasks completed
- All 4 test tasks completed
- 601 total tests pass with 0 failures (15 new tests added)

incomplete_tasks:
- None

contract_changes:
- New `GameEnginePort` interface with `initGame`, `processAction`, `getNextElement`, `isGameComplete`, `buildSummary` methods
- New `FakeGameEngine` test implementation in src/test
- Contract validates no adaptive difficulty methods exist

learnings:
- Pure domain contracts enable easy testing without real infrastructure
- Keeping fake engine in test sources prevents accidental use in production

next_sprint_suggestions:
- Sprint 038: Game State Registry - in-memory registry for active game states
- Sprint 039: Game Lifecycle Orchestrator - coordinate game state transitions
