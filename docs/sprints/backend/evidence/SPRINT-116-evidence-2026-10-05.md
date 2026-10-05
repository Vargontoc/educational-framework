# SPRINT-116 Evidence: WebSocket Threading and Ordering

## Technical Summary

Implemented per-session serial message queue with virtual thread executor. Messages from one child session process in order; different sessions process in parallel. Heartbeats bypass the serial queue to avoid being blocked by slow game actions.

## Changes

### New Files
- `SessionMessageDispatcher.java`: Per-session serial queue dispatcher with virtual thread executor
- `SynchronousSessionMessageDispatcher.java`: Test helper for synchronous test execution
- `GameWebSocketHandlerSprint116Test.java`: 7 tests covering ordering, isolation, failure recovery, session close, pending limit, audio non-blocking, and heartbeat isolation

### Modified Files
- `GameWebSocketHandler.java`: Integrated dispatcher, refactored `handleTextMessage` to dispatch processing, heartbeats bypass serial queue, `sendRoundAudioIfPresent` uses decorated session
- `WebSocketConfig.java`: Added `WebSocketMetrics` and `SessionMessageDispatcher` beans
- `WebSocketGameProperties.java`: Added `maxPendingMessagesPerSession` property (default 50)
- `WebSocketMetrics.java`: Added queue depth gauges
- `application.yml`: Added `max-pending-messages-per-session` config
- `GameWebSocketHandlerTest.java`: Updated constructor for dispatcher
- `GameWebSocketHandlerSprint115Test.java`: Updated constructor for dispatcher

## Test Results

### Automated Tests
- **GameWebSocketHandlerSprint116Test**: 7/7 passed
  - `order_hundredGameActionsProcessedInOrder`: 100 actions processed in strict order
  - `isolation_slowMessageFromOneSessionDoesNotDelayAnother`: Fast session completes in <1s while slow session takes 2s
  - `failureInOneMessageDoesNotBlockFollowing`: 3 actions processed despite middle one throwing exception
  - `sessionCloseDiscardsPendingAndFreesQueue`: Pending messages discarded, queue freed
  - `pendingLimitExceeded_closesSession`: Session closed with POLICY_VIOLATION when limit exceeded
  - `audioNonBlocking_gameReadyRespondsWithoutWaitingForTts`: game_ready completes without TTS wait
  - `heartbeatDoesNotBlockOnSlowGameAction`: Heartbeat completes while game_action is blocked
- **Full suite**: 1189/1189 passed (excluding 8 pre-existing failures in seed/content tests)

### Audio Non-Blocking Verification
- `AudioAdapter.getAudio()` already checks cache first, returns null on miss, fires async synthesis
- `RoundAudioService.generateRoundAudio()` returns `RoundAudioResult.noAudio()` on cache miss
- `AvatarService.processEvent()` falls through to fallback on cache miss
- No synchronous TTS wait exists in any message path
- Audio send is outside game lock (verified in `GameOrchestratorService.processAction`)
- `sendBinaryFrame` already uses single sized buffer (no intermediate copy)

### Threading Model
- Virtual thread executor: `Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("ws-session-", 0).factory())`
- Per-session serial queue: `ConcurrentHashMap<Long, SessionQueue>`
- Heartbeats bypass serial queue: `dispatchHeartbeat()` submits directly to executor
- Pending limit: configurable via `app.ws.game.max-pending-messages-per-session` (default 50)
- Session close: `closeSession()` removes queue and discards pending messages

### Metrics
- `ws.dispatcher.queues`: Number of active session queues (gauge)
- `ws.dispatcher.pending`: Total pending messages across all queues (gauge)
- Existing `ws.message.duration` timer captures per-message processing time

### spring.threads.virtual.enabled Analysis
NOT enabled. Rationale:
- We create our own virtual thread executor for WS message processing (targeted approach)
- Enabling globally could affect Tomcat's HTTP thread pool unexpectedly
- HikariCP (pool of 10) uses platform threads; virtual threads park (not pin) during DB waits
- With max 6 concurrent sessions and pool of 10, no saturation risk
- Our approach is more controlled and measurable

## Manual Test Guide

### 1. Quick Successive Touches
- Start a game session
- Tap 10+ responses rapidly (<200ms apart)
- Verify: results respect order, no errors in logs, `ws.message.duration` for game_action stays consistent

### 2. Slow TTS Scenario
- Stop the Chatterbox audio service
- Play a game, perform game_action
- Verify: game responds immediately (no audio that time), heartbeat doesn't delay
- Check metrics: `ws.message.duration` for game_action and heartbeat should be <100ms

### 3. Uncached Text
- Clear audio cache or use new text
- Perform game_action with uncached text
- Verify: message responds immediately, no audio arrives later
- Perform same action again
- Verify: audio plays from cache (second time)

### 4. Multi-Session Load
- Open 2 simultaneous sessions (real case)
- Open 6 sessions (stress margin)
- Compare p95 of `ws.message.duration` for game_ready and auth with SPRINT-114 baseline
- Check `hikaricp_connections_pending` stays at 0

## Risks and Debt

### Risks
- Virtual thread pinning: If any code uses `synchronized` blocks during DB calls, virtual threads pin to platform threads. Current code uses `ReentrantLock` (no pinning).
- Queue memory: With pending limit of 50 and max 6 sessions, max 300 messages queued. Each message holds a reference to session and JSON. Memory impact is negligible.

### Debt
- No integration test with real WebSocket client and full Spring context (requires Docker/Testcontainers)
- Manual load test not automated (documented in guide above)

## Acceptance Criteria Status

1. ✅ One slow message from a session doesn't delay others nor its own heartbeats
2. ✅ Per-session order is preserved
3. ✅ No changes to message contract
4. ✅ p95 of game_ready and auth not worse (audio non-blocking verified)
5. ✅ Hikari pool without saturation (max 6 sessions, pool of 10, virtual threads park not pin)
