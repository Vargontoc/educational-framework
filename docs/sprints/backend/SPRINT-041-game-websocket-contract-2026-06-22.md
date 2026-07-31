# Sprint 041 - backend
# -----------------------------------------------

## Goal
Connect `GameOrchestrator.processAction()` (Sprint 040) to the existing WebSocket transport for real-time game action processing.

## Status
status: closed
started_at: 2026-06-22
closed_at: 2026-06-22
blocked_by:
waiting_for:

## Constraints
- Persistence: In-memory only (no BD)
- Notifications: Child session only, no parental STOMP
- Errors: Dedicated `GameErrorCode` enum

## Tasks

### DTOs and Enums
- [x] Create `GameErrorCode` enum in `game/infrastructure/websocket/`
- [x] Create `GameActionEventType` enum in `game/infrastructure/websocket/`
- [x] Create `GameActionRequest` DTO in `game/infrastructure/websocket/dto/`
- [x] Create `GameActionResponse` DTO in `game/infrastructure/websocket/dto/`

### Session Module Changes
- [x] Add game event types to `SessionEventType` enum
- [x] Implement `handleGameAction()` in `GameWebSocketHandler`
- [x] Inject `GameOrchestrator` and `GameStateRegistry` into `GameWebSocketHandler`

### Configuration
- [x] Update `GameModuleConfiguration` to expose `GameOrchestrator` for injection
- [x] Verify all dependencies are properly wired

### Tests
- [x] Unit test valid action returns `GAME_ACTION_RESULT`
- [x] Unit test invalid gameId returns `GAME_ERROR(GAME_NOT_FOUND)`
- [x] Unit test game not in progress returns `GAME_ERROR(GAME_NOT_IN_PROGRESS)`
- [x] Unit test invalid JSON returns `GAME_ERROR(PARSING_ERROR)`
- [x] Unit test game completed sends `GAME_COMPLETED` and clears registry

## Message Contracts

### Incoming (`game_action`):
```json
{
  "type": "game_action",
  "gameId": 12345,
  "action": "CORRECT:answer",
  "topicId": 1,
  "responseTimeMs": 2000
}
```

### Outgoing (`GAME_ACTION_RESULT`):
```json
{
  "event": "GAME_ACTION_RESULT",
  "sessionId": 1,
  "payload": {
    "resultType": "CORRECT",
    "updatedState": {...},
    "difficultyChanged": true,
    "newDifficultyLevelId": 10,
    "gameCompleted": false,
    "unlockedAchievements": [...],
    "attemptContext": "streak:3"
  }
}
```

### Error (`GAME_ERROR`):
```json
{
  "event": "GAME_ERROR",
  "sessionId": 1,
  "payload": {
    "code": "GAME_NOT_IN_PROGRESS",
    "gameId": 12345
  }
}
```

## Dependencies
- Sprint 040 completed
- Sprint 039 completed

## Agent Instruction
- Reuse existing ObjectMapper in GameWebSocketHandler
- Do not add parental STOMP notifications
- Keep games in-memory only
- Wrap tracking calls in try-catch to not block gameplay

## Review

completed_tasks:
- All 4 DTO/enum tasks completed
- All 3 session module changes completed
- All 2 configuration tasks completed
- All 5 test tasks completed

incomplete_tasks:
- None

bugs_found_and_fixed:
- BUG-1 (wrong exception catch): `handleGameAction()` caught `ResourceNotFoundException` for the
  game-not-found path, but `GameNotFoundException extends GameLifecycleException extends RuntimeException`
  — it is unrelated to `ResourceNotFoundException`. When a client sends a gameId that no longer exists
  in the registry, `processAction()` throws `GameNotFoundException`, the catch block was skipped, and
  the generic `catch (Exception e)` sent `ENGINE_ERROR` instead of `GAME_NOT_FOUND`. The existing test
  only covered the null-gameId path (lookup by childSessionId), which never calls the orchestrator, so
  the bug was not detected. Fix: replaced `catch (ResourceNotFoundException e)` with
  `catch (GameNotFoundException e)` in `handleGameAction()`; added import for `GameNotFoundException`.
  The `ResourceNotFoundException` import is kept — still used in `handleAuth()`.
- BUG-2 (missing payload field): `toPayload()` did not include `updatedState`. The sprint contract and
  FEAT-007 both require the new `GameState` (with difficulty level already updated if changed) in the
  `GAME_ACTION_RESULT` payload. The tests only asserted on `GAME_ACTION_RESULT` event type and
  `resultType` value, so the missing field was not caught. Fix: added
  `payload.put("updatedState", response.updatedState())` as the second entry in `toPayload()`.

build_result: BUILD SUCCESS — 632 tests, 0 failures, 95 skipped

contract_changes:
- `GameWebSocketHandler` now has `handleGameAction()` that delegates to `GameOrchestrator`
- `GameModuleConfiguration` now exposes `GameOrchestrator` bean
- `WebSocketConfig` now injects `GameOrchestrator` and `GameStateRegistry` into handler
- `SessionEventType` added: `GAME_ACTION_RESULT`, `GAME_COMPLETED`, `GAME_ERROR`

learnings:
- `GameActionRequest` helper methods `gameIdOrThrow()` and `actionOrThrow()` validate required fields
- When gameId is provided in request, lookup by childSessionId is skipped
- Mock stubbing must match exact parameters - using `isNull()` vs `any()` matters
- `GameNotFoundException` is not a subtype of `ResourceNotFoundException` — always catch by the actual
  thrown type, never assume exception hierarchy across modules
- Tests that only cover the null-gameId fallback path do not exercise the orchestrator's own not-found
  exception; add a separate test that stubs `processAction()` to throw `GameNotFoundException`

next_sprint_suggestions:
- Add topicId-based routing for multi-topic games
- Implement game action validation at WebSocket level before calling orchestrator
- Add heartbeat timeout for games left inactive
