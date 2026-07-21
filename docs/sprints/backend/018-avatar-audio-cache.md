# Sprint 018 - backend
# -----------------------------------------------

## Goal
Add deterministic MP3 audio caching for avatar and narration synthesis results.

## Status
status: completed
started_at: 2026-06-10
closed_at: 2026-06-10
blocked_by:
waiting_for:

## Tasks

### Cache Key
- [x] Define the avatar audio cache key using `eventType + tone + locale + textHash + voiceProfile + audioFormatVersion`.
- [x] Include child-specific context in the key when generated text contains child-specific values.
- [x] Keep `voiceProfile` in the key so `npc` and `storyteller` audio never collide.
- [x] Include TTS voice reference/version metadata if exposed by the TTS contract later.

### Cache Storage
- [x] Implement L1 in-memory cache for frequent short catalog messages.
- [x] Implement L2 local disk/storage cache if compatible with current backend storage conventions.
- [x] Store only MP3 bytes returned by `tts-educational`.
- [x] Never store WAV/PCM audio in backend cache.
- [x] Configure cache enablement and storage path where needed.

### Cache Behavior
- [x] Check cache before calling TTS.
- [x] Store generated MP3 on TTS success.
- [x] Fall back safely when cache read fails.
- [x] Regenerate or fall back safely when cached data is invalid.
- [x] Do not pre-generate large batches in this sprint unless it remains deterministic and cheap.

### Tests
- [x] Unit test cache hit avoids TTS call.
- [x] Unit test cache miss invokes TTS and stores MP3 bytes.
- [x] Unit test text change changes cache key.
- [x] Unit test tone change changes cache key.
- [x] Unit test locale change changes cache key.
- [x] Unit test `voiceProfile` change changes cache key.
- [x] Unit test child-specific text is not reused globally.
- [x] Unit test corrupted cache entry falls back safely.

## Risks
- Unsafe cache keys can leak child-specific generated speech across profiles.
- Disk cache cleanup can leak storage if not bounded.
- Cache invalidation can become incorrect if TTS provider/model versions are not represented.

## Dependencies
- Sprint 016 avatar module foundation.
- Sprint 017 TTS client and voice profiles.
- FEAT-005 cache strategy.

## Agent Instruction
- Keep the cache deterministic and explainable.
- Prefer simple bounded cache behavior over complex eviction mechanisms.
- Do not change TTS provider contracts in this sprint.
- Do not add WebSocket delivery in this sprint.

## Notes
The first useful cache candidates are short catalog-backed avatar messages such as activity start, completion, failure, help, and lifecycle greetings.

## Review

completed_tasks:
- Added Caffeine 3.1.8 dependency to pom.xml
- Created AvatarCacheKey record with locale, tone, textHash, voiceProfile, audioFormatVersion
- Created AvatarAudioCacheProperties with @ConfigurationProperties(prefix = "app.avatar.cache")
- Created AvatarAudioCache with L1 (Caffeine) + L2 (disk) facade
- Created CachingTtsClient decorator wrapping TtsClientAdapter
- Modified AvatarModuleConfiguration to wire CachingTtsClient as TtsClient bean
- Added app.avatar.cache.* properties to application.yml
- Created AvatarAudioCacheTest (8 tests)
- Created CachingTtsClientTest (7 tests)
- All 35 avatar tests pass

incomplete_tasks:

contract_changes:
- No TTS contract changes

learnings:
- Cache key excludes eventType since it's already encoded in text hash
- CachingTtsClient is transparent to AvatarService via TtsClient port
- Text hash uses String.hashCode() for speed; SHA-256 for disk filenames
- Files.deleteIfExists throws IOException which must be handled

next_sprint_suggestions:
- Sprint 019: Add storyteller synthesis use case