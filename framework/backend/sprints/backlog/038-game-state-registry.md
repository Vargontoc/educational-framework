# Sprint 038 - backend
# -----------------------------------------------

## Goal
Implement the in-memory `GameStateRegistry` used by the game shell to store active game states during a backend process lifetime.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Registry
- [ ] Add `GameStateRegistry` port/interface.
- [ ] Add in-memory implementation backed by a map.
- [ ] Store and retrieve by `gameId`.
- [ ] Retrieve active game by `childSessionId`.
- [ ] Remove state when a game completes or is abandoned.
- [ ] Reject duplicate active games for the same child session if required by the shell flow.

### Tests
- [ ] Unit test save and find by game id.
- [ ] Unit test find active game by child session id.
- [ ] Unit test remove completed game.
- [ ] Unit test duplicate child session handling.

## Manual Tests
- Not required. This is an in-memory infrastructure sprint.

## Risks
- Persisting `GameState` would contradict FEAT-007 v1.
- Missing child-session lookup would break reconnect.

## Dependencies
- Sprint 036 completed.

## Agent Instruction
- Do not add Redis or database persistence.
- Keep the implementation simple for single-family usage.
- Make behavior deterministic and easy to unit test.

## Notes
The registry only restores games while the backend process remains alive.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
