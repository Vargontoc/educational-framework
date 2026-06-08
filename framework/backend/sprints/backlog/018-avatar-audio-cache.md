# Sprint 018 - backend
# -----------------------------------------------

## Goal
Add deterministic MP3 audio caching for avatar and narration synthesis results.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Cache Key
- [ ] Define the avatar audio cache key using `eventType + tone + locale + textHash + voiceProfile + audioFormatVersion`.
- [ ] Include child-specific context in the key when generated text contains child-specific values.
- [ ] Keep `voiceProfile` in the key so `npc` and `storyteller` audio never collide.
- [ ] Include TTS voice reference/version metadata if exposed by the TTS contract later.

### Cache Storage
- [ ] Implement L1 in-memory cache for frequent short catalog messages.
- [ ] Implement L2 local disk/storage cache if compatible with current backend storage conventions.
- [ ] Store only MP3 bytes returned by `tts-educational`.
- [ ] Never store WAV/PCM audio in backend cache.
- [ ] Configure cache enablement and storage path where needed.

### Cache Behavior
- [ ] Check cache before calling TTS.
- [ ] Store generated MP3 on TTS success.
- [ ] Fall back safely when cache read fails.
- [ ] Regenerate or fall back safely when cached data is invalid.
- [ ] Do not pre-generate large batches in this sprint unless it remains deterministic and cheap.

### Tests
- [ ] Unit test cache hit avoids TTS call.
- [ ] Unit test cache miss invokes TTS and stores MP3 bytes.
- [ ] Unit test text change changes cache key.
- [ ] Unit test tone change changes cache key.
- [ ] Unit test locale change changes cache key.
- [ ] Unit test `voiceProfile` change changes cache key.
- [ ] Unit test child-specific text is not reused globally.
- [ ] Unit test corrupted cache entry falls back safely.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
