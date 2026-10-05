# SPRINT-118 Evidence: WS Payload & Lock Optimization

## Technical Summary

### Changes Implemented

1. **Typed State in Memory (GameState.java)**
   - Added transient fields `typedRecognitionState` and `typedMemoryState` to `GameState`
   - Added transient fields `pendingAudioId` and `pendingAudioText` for audio separation
   - Engines now set typed state after processing to avoid repeated JSON parsing

2. **Lock Optimization (GameOrchestratorService.java)**
   - `processAction`: Audio generation moved outside the lock
   - Event publishing moved outside the lock
   - Lock now only covers state mutation (engine.processAction, state updates, persistence)

3. **Typed State Accessors**
   - Added `getOrDeserializeRecognitionState()` and `getOrDeserializeMemoryState()` helpers
   - These methods return cached typed state if available, otherwise deserialize and cache
   - Used in `bufferAttempt`, `bufferedAttemptsOf`, `bufferedMemoryAttempts`, `resolveMemoryTopicId`

4. **Audio Separation**
   - `RoundAudioResult` still carries byte[] for transmission
   - `GameState` now stores only `pendingAudioId` and `pendingAudioText`
   - `clearAudioDataFromState()` in WebSocket handler clears byte[] after sending
   - Reduces memory retention per game

5. **Engine Updates**
   - `RecognitionEngine.initGame()` and `processAction()` set `typedRecognitionState`
   - `MemoryEngine.initGame()` and `processAction()` set `typedMemoryState`

6. **WebSocket Handler Optimization**
   - `gameStateToPayload()` uses typed state when available
   - `memoryStatePayload()` uses typed state when available
   - Added `getOrDeserializeRecognitionState()` and `getOrDeserializeMemoryState()` helpers

### Files Modified

| File | Changes |
|------|---------|
| `GameState.java` | Added 4 transient fields + getters/setters |
| `GameOrchestratorService.java` | Lock optimization, typed state accessors, audio handling |
| `GameWebSocketHandler.java` | Typed state usage, audio clearing after send |
| `RecognitionEngine.java` | Set typed state in initGame/processAction |
| `MemoryEngine.java` | Set typed state in initGame/processAction |

### Files Created

| File | Purpose |
|------|---------|
| `GameOrchestratorServiceSprint118Test.java` | Sprint tests (7 tests) |

## Test Results

### Automated Tests

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
- GameOrchestratorServiceSprint118Test

All game-related tests: 289 passed, 0 failed
```

### Test Coverage

1. **Typed State Tests**
   - `processAction_recognitionState_typedStateIsSet`: Verifies typed state is set after Recognition action
   - `processAction_memoryState_typedStateIsSet`: Verifies typed state is set after Memory action

2. **Concurrency Tests**
   - `processAction_concurrentActions_serializedWithoutStateLoss`: 5 concurrent actions on same game remain serialized

3. **Completion Tests**
   - `processAction_gameCompleted_allAttemptsRegistered`: All attempts registered on game completion

4. **Audio Tests**
   - `processAction_audioData_clearedAfterSending`: Audio ID/text stored, byte[] cleared after send
   - `gameState_audioFields_nullWhenNoAudio`: Audio fields null initially
   - `gameState_typedState_nullInitially`: Typed state null initially

## Decisions Taken

### 1. Audio Data Handling
**Decision**: Keep `RoundAudioResult` with byte[] for transmission, but clear from GameState after sending.
**Rationale**: 
- Audio is generated once per round and sent immediately
- Storing byte[] in GameState retains memory unnecessarily between actions
- Future sprint could add audio cache for retransmission scenarios

### 2. Lock Scope
**Decision**: Audio generation and event publishing moved outside lock.
**Rationale**:
- These operations don't mutate game state
- Moving them outside reduces lock hold time
- Thread safety maintained: audio generation uses immutable inputs (childProfileId, targetElementId)

### 3. Typed State Caching
**Decision**: Cache typed state in GameState transient fields.
**Rationale**:
- Avoids repeated JSON parsing in action path
- Transient fields don't affect persistence
- Cleared naturally when GameState is garbage collected

### 4. Batch Flush
**Decision**: Kept sequential flush with per-attempt error handling.
**Rationale**:
- Current implementation already handles partial failures gracefully
- True batch transaction would require schema changes
- Atomicity of achievements/summary preserved by keeping flush inside lock

## Memory Payload Evaluation (Task 7)

### Current State
Memory payload includes:
- `cards`: Array of card objects (faceUp, matched, elementId when visible)
- `elements`: Array of all element objects on board (id, code, displayValue, resourceRefs)

### Proposal for Future Sprint
**Problem**: `elements` is resent on every flip, even though it doesn't change during a game.

**Proposal**: 
1. Send `elements` only once at game start (GAME_READY)
2. Subsequent GAME_ACTION_RESULT messages omit `elements`
3. Frontend caches `elements` from GAME_READY

**Estimated Savings**:
- 4x3 board: 12 elements * ~200 bytes = ~2.4 KB per flip
- 10 flips per game: ~24 KB saved per game
- For 5-6 concurrent users: ~144 KB saved during peak

**Decision**: Documented for future sprint with frontend analyst. Requires:
- Frontend changes to cache elements
- Contract clarification (no contract change needed, just payload optimization)
- Metrics to verify actual savings

## Manual Test Guide

### Test 1: Memory 4x3 - Quick Flips
1. Start Memory game with 4x3 difficulty
2. Flip cards rapidly (2-3 per second)
3. Verify:
   - Perceived response time < 100ms
   - Cards flip back correctly after delay
   - Matched pairs stay face up
   - Game completes without state errors

### Test 2: Recognition Game Completion
1. Start Recognition game
2. Complete all rounds
3. Verify:
   - Summary shows correct stars
   - Achievements unlocked correctly
   - Parental diary shows same data as before sprint

### Test 3: Metrics Observation
1. Enable metrics endpoint
2. Play 20 consecutive games (mix of Recognition and Memory)
3. Observe:
   - `ws.message.duration{type=game_action}` p95 < 100ms
   - Heap memory stable (no growth per game)
   - No increase in GC pauses

## Risks and Mitigations

### Risk 1: Typed State Desynchronization
**Risk**: Typed state could become out of sync with enginePayload.
**Mitigation**: 
- Typed state is set immediately after enginePayload is updated
- Both are updated in same code path
- Tests verify typed state is set correctly

### Risk 2: Audio Data Loss
**Risk**: Clearing audio byte[] could cause issues if send fails.
**Mitigation**:
- Audio is sent immediately after generation
- If send fails, next round generates new audio
- Audio is not critical for gameplay (game is playable without sound)

### Risk 3: Lock Optimization Race Conditions
**Risk**: Moving work outside lock could introduce races.
**Mitigation**:
- Audio generation uses immutable inputs
- Event publishing is fire-and-forget
- Concurrency tests verify serialization

## Debt and Future Work

1. **Audio Cache**: Implement proper audio cache for retransmission scenarios
2. **Memory Elements Optimization**: Implement frontend caching of elements (requires coordination)
3. **Batch Attempt Registration**: True batch transaction for attempt registration (requires schema changes)
4. **Metrics Dashboard**: Add dashboard for ws.message.duration percentiles

## Commands Executed

```bash
# Compile
mvn compile -q

# Run sprint tests
mvn test -Dtest=GameOrchestratorServiceSprint118Test

# Run all game-related tests
mvn test -Dtest="GameOrchestratorService*Test,GameWebSocketHandler*Test,RecognitionEngine*,MemoryEngine*"
```

## Conclusion

SPRINT-118 successfully implements payload and lock optimization:
- No repeated JSON parsing in action path (typed state cached)
- Lock only covers state mutation (audio/events moved outside)
- Audio byte[] cleared from GameState after sending
- All 289 game-related tests pass
- 7 new sprint-specific tests pass
