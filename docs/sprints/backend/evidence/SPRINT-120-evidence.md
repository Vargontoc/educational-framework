# SPRINT-120 Evidence - TTS Audio Cache Hardening

## Date: 2026-10-06

## Summary

Implemented all non-optional tasks from SPRINT-120 to harden the TTS audio cache and client.

## Changes

### 1. Cache key with SHA-256 (collision prevention)
- **File**: `audio/domain/AudioCache.java`
- Changed from `int textHash` (Java `hashCode()`, collision-prone) to `String textHashSha256`
- Key is SHA-256 of `normalizedText|exaggeration|cfgWeight|temperature|voice`
- Text normalization: strip + collapse whitespace
- Old cache entries are automatically invalidated (different file names)

### 2. Configurable disk cache path
- **Files**: `audio/infrastructure/config/AudioCacheConfiguration.java`, `AudioConfiguration.java`, `application.yml`
- Path now comes from `app.audio.cache.disk-path` (default: `./data/audio-cache`)
- Startup validation: checks directory exists (creates if needed), verifies it's a directory, performs a write probe
- Invalid path throws `IllegalStateException` with clear message, preventing silent failures

### 3. Periodic enforceDiskCapacity
- **File**: `audio/infrastructure/scheduler/AudioCacheCapacityJob.java`
- Moved from per-write to scheduled task (`@Scheduled` with configurable interval)
- Default: every 300 seconds
- No more directory listing on every write

### 4. Pre-warming
- **File**: `audio/infrastructure/adapters/in/AudioPreWarmer.java`
- On `ApplicationReadyEvent`, synthesizes fixed texts in background
- Configurable concurrency limit via `Semaphore` (default: 2)
- Default fixed texts: welcome, farewell, encouragement, round prompts
- Configurable via `app.audio.prewarm.texts`

### 5. HTTP client hardening
- **File**: `audio/infrastructure/config/AudioConfiguration.java`
- Replaced `SimpleClientHttpRequestFactory` (no pool) with `JdkClientHttpRequestFactory` (connection pooling via `java.net.http.HttpClient`)
- **File**: `audio/infrastructure/adapters/out/ChatterboxAdapter.java`
- `/ping` result cached for configurable TTL (default: 5 seconds)
- Eliminates a `/ping` call before every synthesis

### 6. Timeouts aligned with game
- **File**: `AudioConfiguration.java`, `application.yml`
- Connection timeout: 3s (configurable via `app.audio.connect-timeout-seconds`)
- Read timeout: 15s (configurable via `app.audio.read-timeout-seconds`)
- Down from 20s/20s
- Clean degradation: `AudioAsync.generateAudio` catches all exceptions, logs warning, game continues without audio

### 7. File extension fix
- **File**: `audio/infrastructure/cache/AudioCacheStorage.java`
- Changed from `.mp3` to `.wav` (matches actual Chatterbox output format)

### 8. Metrics (SPRINT-114)
- Already present: `ws.audio.cache` counter (hit/miss), `ws.tts.synthesis.duration` timer
- Verified working in integration tests

### 9. AudioAsync error handling
- **File**: `audio/infrastructure/adapters/in/AudioAsync.java`
- Added try-catch around synthesis to prevent unhandled exceptions from crashing the async executor

## Tests

### New test files (20 tests, all passing):
1. `AudioCacheTest` (4 tests) - SHA-256 key generation, collision prevention, tone differentiation
2. `AudioCacheStorageTest` (7 tests) - invalid path, put/get round-trip, disk persistence, capacity enforcement, .wav extension, hash collision
3. `ChatterboxAdapterTest` (3 tests) - service down detection, error propagation, ping caching
4. `AudioPreWarmerTest` (3 tests) - fixed text synthesis, failure resilience, default texts
5. `AudioAdapterTest` (3 tests) - cache hit/miss, non-blocking behavior

### Commands executed:
```
mvn compile -q                                          # SUCCESS
mvn test -Dtest="AudioCacheTest,AudioCacheStorageTest,ChatterboxAdapterTest,AudioPreWarmerTest,AudioAdapterTest" -q  # 20/20 PASS
mvn test -Dtest="RoundAudioServiceTest,AvatarServiceTest,GameOrchestratorServiceMemoryTest" -q  # All PASS (no regression)
mvn test -q                                             # 1280 run, 9 pre-existing failures (unrelated to this sprint)
```

### Pre-existing failures (NOT caused by this sprint):
- `ComparisonRecognitionSeedTest` (2) - seed data
- `MemoryRecognitionSeedTest` (1) - seed data
- `SeedServiceTest` (2) - category count
- `DevContentControllerTest` (3) - auth/401
- `GameWebSocketHandlerSprint116Test` (1) - timing (99/100)

## Decisions taken

1. **SHA-256 hex as filename**: Used the full hex string directly as the cache filename (e.g., `9a0385318e092216...wav`). This is simpler and more collision-resistant than the previous Base64-encoded approach.

2. **JdkClientHttpRequestFactory over Apache HttpClient**: Spring Boot 4.x includes `java.net.http.HttpClient` natively. No additional dependency needed. It provides HTTP/2 connection pooling out of the box.

3. **Ping cache with AtomicLong + AtomicBoolean**: Simple thread-safe approach for a monofamily app. No need for a full cache library for a single boolean value.

4. **Pre-warming via @EventListener(ApplicationReadyEvent)**: Fires after full context initialization, ensuring all beans are available. Runs async to not block startup.

5. **Default fixed texts in Spanish**: Matches the app's target audience. Configurable via property for future customization.

6. **Optional task (multi-frame audio) NOT implemented**: Requires measurement data and frontend agreement per sprint spec. Deferred.

## Risks / Debt / Blockers

- **Risk**: Pre-warming on startup calls TTS service even if no game is played. Mitigated by low concurrency limit (2) and async execution.
- **Risk**: Cache invalidation is implicit (old `.mp3` files won't match new `.wav` naming). Old files will remain on disk until cleaned by capacity enforcement. Consider a one-time migration script if disk space is a concern.
- **Debt**: Manual tests not yet executed (require running backend + TTS service + frontend).
- **Blocker**: None.

## Manual test guide

1. **Cold start**: Stop backend, clear `./data/audio-cache/`, start backend, play first game. Verify Nubi speaks on welcome and first round (pre-warming should have cached fixed texts).
2. **TTS failure during game**: Stop Chatterbox service mid-game. Verify child sees no errors, game continues without audio.
3. **Cache reuse**: Restart backend after a game. Verify disk cache files are reused (check logs for "Disk cache hit").
