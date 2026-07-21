# Sprint 042 - backend
# -----------------------------------------------

## Goal
Add WebSocket transport for full game lifecycle: start, ready, and abandon games. Complete the game action loop by enabling the client to control the entire game flow over WebSocket.

## Status
status: closed
started_at: 2026-06-22
closed_at: 2026-06-22
blocked_by:
waiting_for:

## Constraints
- Reuse existing `SessionEvent` infrastructure
- All game lifecycle events use child session WebSocket only (no parental STOMP)
- Errors use `GameErrorCode` enum from Sprint 041
- In-memory game state (no persistence)
- One active game per child at a time (no gameId needed in requests)

## Tasks

### Message Contracts

#### Start Game
- [x] Define `GAME_START` message contract (client → server)
- [x] Define `GAME_STARTED` event (server → client)
- [x] Handle `startGame(Long childProfileId, Long activityId)` via WebSocket
- [x] Return initial `GameState` in response
- [x] Implicit abandon of existing game before starting new one

#### Ready Game
- [x] Define `GAME_READY` message contract (client → server)
- [x] Define `GAME_READY` event (server → client)
- [x] Handle `readyGame(Long gameId)` via WebSocket
- [x] Return updated `GameState` in response

#### Abandon Game
- [x] Define `GAME_ABANDON` message contract (client → server)
- [x] Define `GAME_ABANDONED` event (server → client)
- [x] Handle `abandonGame(Long gameId)` via WebSocket
- [x] Clean up game state from registry
- [x] Return final `GameState` in response

### Error Handling
- [x] `INVALID_STATE_TRANSITION` when trying to start/ready/abandon in wrong state
- [x] `GAME_NOT_FOUND` when gameId doesn't exist
- [x] `NO_ACTIVE_GAME` when trying to abandon with no active game

### WebSocket Handler Changes
- [x] Add `handleGameStart()` method
- [x] Add `handleGameReady()` method
- [x] Add `handleGameAbandon()` method
- [x] Add new message types to switch case in `handleTextMessage()`

### Configuration
- [x] Verify `GameOrchestrator` is properly injected (already done in Sprint 041)

### Tests
- [x] Unit test start game returns `GAME_STARTED` with initial state
- [x] Unit test start game without activityId returns `GAME_ERROR`
- [x] Unit test start game with existing game abandons previous
- [x] Unit test ready game with no active game returns `GAME_ERROR`
- [x] Unit test ready game returns `GAME_READY` with in-progress state
- [x] Unit test abandon game with no active game returns `GAME_ERROR`
- [x] Unit test abandon game returns `GAME_ABANDONED` and cleans registry

## Message Contracts

### Start Game

**Incoming (`game_start`):**
```json
{
  "type": "game_start",
  "activityId": 12345
}
```

**Outgoing (`GAME_STARTED`):**
```json
{
  "event": "GAME_STARTED",
  "sessionId": 1,
  "payload": {
    "gameId": 1,
    "status": "WAITING",
    "activityId": 12345,
    "difficultyLevelId": 5
  }
}
```

### Ready Game

**Incoming (`game_ready`):**
```json
{
  "type": "game_ready"
}
```

**Outgoing (`GAME_READY`):**
```json
{
  "event": "GAME_READY",
  "sessionId": 1,
  "payload": {
    "gameId": 1,
    "status": "IN_PROGRESS"
  }
}
```

### Abandon Game

**Incoming (`game_abandon`):**
```json
{
  "type": "game_abandon"
}
```

**Outgoing (`GAME_ABANDONED`):**
```json
{
  "event": "GAME_ABANDONED",
  "sessionId": 1,
  "payload": {
    "gameId": 1,
    "status": "ABANDONED"
  }
}
```

### Error

```json
{
  "event": "GAME_ERROR",
  "sessionId": 1,
  "payload": {
    "code": "NO_ACTIVE_GAME",
    "gameId": 1
  }
}
```

## Dependencies
- Sprint 041 completed (Game WebSocket Contract)
- Sprint 039 completed (FakeGameEngine)
- Sprint 032 completed (Game State Registry)

## Agent Instruction
- Reuse `SessionEvent` and `SessionEventType` from Sprint 041
- Use `GameErrorCode` enum already defined
- Keep all games in-memory
- Follow same error handling pattern as `handleGameAction()`
- Do not add parental notifications

## Review

completed_tasks:
- All 4 start game tasks completed
- All 4 ready game tasks completed
- All 5 abandon game tasks completed
- All 3 error handling tasks completed
- All 4 WebSocket handler changes completed
- All 7 test tasks completed

incomplete_tasks:
- None

bugs_found_and_fixed:
- None. BUILD SUCCESS — 642 tests, 0 failures, 95 skipped.

observations:
- `GameStartRequest.activityIdOrThrow()` is dead code — `handleGameStart()` parses `activityId` inline
  and handles null explicitly (returning `PARSING_ERROR`). Using the DTO method would produce `ENGINE_ERROR`
  instead, because `IllegalArgumentException` would fall through to `catch (Exception e)`. The inline
  approach is correct; the DTO is unused. No change needed for correctness.
- `GameOrchestratorService.abandonGame()` (Sprint 039) does not call `registerGameSessionSummary`. Per
  FEAT-007, this call is required on both COMPLETED and ABANDONED transitions. This is a pre-existing
  gap in Sprint 039, not introduced here. Flagged for a future hardening sprint.

build_result: BUILD SUCCESS — 642 tests, 0 failures, 95 skipped

contract_changes:
- Added `game_start`, `game_ready`, `game_abandon` message types to `websocket.json`
- Added `GAME_STARTED`, `GAME_READY`, `GAME_ABANDONED` events to `websocket.json`
- Added `INVALID_STATE_TRANSITION` and `NO_ACTIVE_GAME` error codes
- Bumped contract version to 1.3.0

new_files:
- `game/infrastructure/websocket/dto/GameStartRequest.java`

learnings:
- Simplified by not requiring gameId - infer from childSessionId
- Implicit abandon when starting new game with existing active game
- ChildSessionUseCase.getSession() provides childProfileId needed for startGame

next_sprint_suggestions:
- Add game state sync on reconnect
- Add game pause/resume functionality
- Add session expiry cleanup for abandoned games
