# SPRINT-119 Evidence

## Automated tests

### New tests
- `SessionActivityTrackerTest` (8 tests): deferred activity persistence, flush on close, write reduction
- `ExplorationStateTrackerTest` (8 tests): biome change tracking, position tracking, flush behavior
- `WorldStateSyncPayloadShapeTest` (3 tests): contract shape preservation

### Updated tests
- `ChildSessionServiceTest` (19 tests): updated for SessionActivityTracker usage
- `WorldHeartbeatServiceTest` (6 tests): updated for ExplorationStateTracker
- `WorldExplorationPersistenceTest` (9 tests): updated for deferred persistence
- `GameWebSocketHandlerTest` (55 tests): updated constructor
- `GameWebSocketHandlerSprint115Test` (5 tests): updated constructor
- `GameWebSocketHandlerSprint116Test` (7 tests): updated constructor

### Commands
```
mvn test -Dtest="SessionActivityTrackerTest,ExplorationStateTrackerTest,WorldStateSyncPayloadShapeTest,ChildSessionServiceTest,WorldHeartbeatServiceTest,WorldExplorationPersistenceTest,GameWebSocketHandlerTest,GameWebSocketHandlerSprint115Test,GameWebSocketHandlerSprint116Test"
```
Result: 120 tests, 0 failures

## Manual test guide

### Test 1: Session closes at expected time without activity
1. Open a child session and connect via WebSocket
2. Do not send any heartbeat or game action
3. Wait for `heartbeatInterval * graceMultiplier + activityFlushInterval` seconds (default: 30*2 + 12 = 72s)
4. Verify session is expired and SESSION_EXPIRED event is sent
5. Margin: up to `activityFlushIntervalSeconds` (12s) additional delay is acceptable

### Test 2: SQL write reduction with heartbeats
1. Connect and authenticate a WebSocket session
2. Send 60 heartbeats over 1 minute (1 per second)
3. Check `ws.sql.statements` metric (SPRINT-114) for `heartbeat` type
4. Expected: at most `ceil(60 / activityFlushIntervalSeconds)` = 5 SQL statements (instead of 120 previously)
5. For `world_heartbeat`: at most `ceil(60 / explorationFlushIntervalSeconds)` = 4 SQL statements

### Test 3: Biome travel persists and reconnects correctly
1. Connect and travel to a biome (e.g., BEACH)
2. Verify WORLD_STATE_SYNC is sent with correct biome
3. Disconnect WebSocket
4. Reconnect with same childSessionId
5. Verify world resumes in BEACH biome (from `world_exploration_state` table)

## Acceptance criteria verification

1. **Heartbeat without SQL**: `heartbeat` handler calls `sessionActivityTracker.recordActivity()` (in-memory only). `world_heartbeat` calls `explorationStateTracker.recordPosition()`/`recordBiomeChange()` (in-memory only). No SQL in normal path.
2. **p95 < 20ms**: Heartbeat path is now pure in-memory operations (ConcurrentHashMap.put). Expected p95 < 1ms.
3. **Session expiration**: Uses `getEffectiveLastActivity()` (memory > DB). Margin: up to `activityFlushIntervalSeconds` (12s) within the 60s grace period.
4. **Biome/position persistence**: `ExplorationStateTracker.flushAndRemove()` called on disconnect. Biome changes tracked and flushed at interval.
5. **No contract changes**: WORLD_STATE_SYNC payload shape unchanged. Destination still sent in each sync.
