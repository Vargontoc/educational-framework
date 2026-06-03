# FEAT-005 - Backend: Avatar Module

## Status

state: blocked
user_history: Avatar interaction
depends_on: shared, family, session, content, `websocket.json`, `openapi_tts.json`
future_depends_on: agent
blocked_by: TTS output format and backend audio delivery strategy are not aligned yet
waiting_for: TTS layer must provide backend-compatible MP3 generation, or backend must define an approved WAV-to-MP3 conversion strategy before avatar audio delivery is implemented
test: unit + integration + contract
sprints:

## Description

The avatar module is the backend gateway for the audio and avatar feedback heard by the child during the game experience. It translates finite game/session events into short avatar responses, resolves the effective family and child configuration, selects catalog-backed fallback text from content, invokes TTS when available, manages audio cache, and emits avatar results through the game WebSocket channel.

Latency is critical. Any delay or failure is visible to the child, so the first implementation must prioritize deterministic catalog messages, cache hits, and graceful text fallback over dynamic generation.

## Scope v1

In scope:
- Process avatar events for a concrete active child session.
- Consume effective child configuration from `family` using `ttsEnabled` and `agentEnabled`.
- Consume predefined avatar messages from `content.AvatarEventCatalog`.
- Use current avatar tones: `CALM`, `JOYFUL`, `ENTHUSIASTIC`, `SERIOUS`, `NEUTRAL`.
- Use current avatar event types: `ACTIVITY_STARTED`, `ACTIVITY_COMPLETED`, `ACTIVITY_FAILED`, `HELP_REQUESTED`, `OUT_OF_SCOPE_QUERY`, `CURIOSITY_REQUESTED`.
- Respect disabled TTS by emitting text-only avatar metadata with `audioAvailable: false`.
- Respect disabled avatar/agent behavior through `agentEnabled` by suppressing avatar interaction when disabled.
- Manage audio cache for deterministic catalog messages once the TTS/audio format blocker is resolved.
- Emit avatar metadata through the native game WebSocket channel.

Out of scope for v1:
- GameAgent integration.
- LLM-generated or enriched avatar copy.
- Conversational memory.
- AdultAgent integration.
- New avatar tone enums.
- Moving `AvatarConfig` ownership out of `family`.

Future scope:
- Invoke GameAgent when the agent module is ready and latency budgets are validated.
- Use GameAgent only as an optional enrichment layer with a strict timeout and fallback to `AvatarEventCatalog`.
- Add richer event context for topic, streak, result, and difficulty once game/tracking modules provide it.

## Ownership Boundaries

Avatar owns:
- Runtime avatar event orchestration.
- TTS invocation.
- Audio cache.
- Runtime fallback to text when audio is unavailable.
- Game WebSocket avatar metadata and audio delivery protocol.

Family owns:
- Family-level `ttsEnabled` and `agentEnabled` flags.
- Child-level `ttsEnabled` and `agentEnabled` overrides or effective values, depending on the current family implementation.
- Child avatar display data already stored in the family profile model.
- Future `AvatarConfig` fields if they are introduced according to the backend design.

Content owns:
- `AvatarEventCatalog`.
- Fallback message text.
- Event type, tone, locale, status, and TTS-optimized copy.

Session/game own:
- Child session validation.
- Native game WebSocket connection lifecycle.
- Dispatching game/session events that may trigger avatar responses.

Agent is not used in v1:
- The avatar module must not call GameAgent in the first implementation.
- If dynamic copy is needed later, GameAgent must be introduced in a separate feature/sprint.

## Runtime Flow v1

Game/session event arrives.

Avatar module:
- Validates that the child session is active.
- Resolves the child profile and effective family/child configuration.
- If `agentEnabled` is false, suppresses avatar behavior for that event.
- Selects an active `AvatarEventCatalog` message by `eventType`, `tone`, and locale.
- If `ttsEnabled` is false, emits metadata with fallback `text` and `audioAvailable: false`.
- If audio is requested and TTS/audio delivery is available, checks audio cache.
- On cache hit, emits metadata and correlated audio bytes.
- On cache miss, invokes TTS, stores the generated audio, then emits metadata and correlated audio bytes.
- On TTS timeout/failure, emits metadata with fallback `text` and `audioAvailable: false`.

## Configuration

The avatar module must consume existing effective configuration instead of duplicating it locally.

Effective flags:
- `ttsEnabled`: controls whether audio generation/playback is allowed.
- `agentEnabled`: controls whether avatar interaction is allowed.

Tone:
- Source: child/family avatar configuration once available through `family`.
- Allowed values: `CALM`, `JOYFUL`, `ENTHUSIASTIC`, `SERIOUS`, `NEUTRAL`.
- If no tone is configured, the backend must use a safe default aligned with existing family/content contracts.

Avatar name:
- Source: existing child profile/avatar configuration from `family`.
- Default: system avatar name, currently `Nubi`, unless family configuration overrides it.

## Cache Strategy

The cache key must identify the generated audio deterministically:

`eventType + tone + locale + textHash + ttsVoiceReference + audioFormatVersion`

Two cache levels are expected once TTS is unblocked:
- L1 memory cache for frequent short catalog messages.
- L2 local disk/storage cache for generated runtime audio.

Cache rules:
- Catalog messages without child-specific text can be globally reusable.
- Messages containing child-specific values, such as the child's name, must be cached with child-specific context in the key or invalidated when that context changes.
- Cache invalidation must happen when message text, tone, TTS reference voice, locale, or audio format changes.
- Frequent active catalog messages may be pre-generated at startup only after the TTS/audio blocker is resolved.

Frequent event candidates:
- `ACTIVITY_STARTED`
- `ACTIVITY_COMPLETED`
- `ACTIVITY_FAILED`
- `HELP_REQUESTED`

## TTS Integration

The current TTS contract is `docs/contracts/api/openapi_tts.json`.

Important blocker:
- The backend design requires MP3 bytes sent through WebSocket.
- The current Coqui TTS contract returns `audio/wav` from `/api/tts`.
- XTTS v2 tone is controlled through `speaker_wav`, not direct numeric pitch/rate/emphasis parameters.

Implementation must not proceed until one of these is decided and documented:
- TTS service returns MP3 directly.
- Backend converts WAV to MP3 with an approved local conversion strategy.
- Backend design and WebSocket contract are changed to accept WAV.

Tone-to-TTS mapping must use reference voices such as:
- `CALM` -> `/references/calm.wav`
- `JOYFUL` -> `/references/joyful.wav`
- `ENTHUSIASTIC` -> `/references/enthusiastic.wav`
- `SERIOUS` -> `/references/serious.wav`
- `NEUTRAL` -> safe default reference voice to be decided in the TTS layer

Timeout behavior:
- TTS timeout must be configurable.
- On timeout or failure, the avatar event must still be emitted as text-only metadata with `audioAvailable: false`.
- No child-facing event should block waiting indefinitely for TTS.

## WebSocket Contract Impact

Avatar events use the native game WebSocket channel, not the parental STOMP channel.

`docs/contracts/api/websocket.json` must be updated before implementation to include:
- `GAME_AVATAR_EVENT` server-to-client metadata message.
- `eventType`.
- `audioAvailable`.
- `audioId` correlation id when audio is available.
- `text` fallback for visual rendering.
- Binary audio message format and correlation strategy.
- Maximum binary message size supported by Spring WebSocket.
- Behavior when audio is disabled, unavailable, late, or dropped.

Expected two-message protocol when audio is available:

```json
{
  "type": "GAME_AVATAR_EVENT",
  "eventType": "ACTIVITY_COMPLETED",
  "audioAvailable": true,
  "audioId": "uuid-correlation-id",
  "text": "Muy bien, lo has conseguido."
}
```

Binary message:

```text
[header: audioId uuid][bytes: compressed audio]
```

If audio is unavailable:

```json
{
  "type": "GAME_AVATAR_EVENT",
  "eventType": "ACTIVITY_COMPLETED",
  "audioAvailable": false,
  "text": "Muy bien, lo has conseguido."
}
```

## Contract Impact

Required before implementation:
- Update `docs/contracts/api/websocket.json` for avatar metadata and binary audio delivery.
- Resolve `docs/contracts/api/openapi_tts.json` vs backend MP3 delivery mismatch.

Not required for v1:
- Agent contract changes.
- GameAgent payload changes.

Required if REST configuration endpoints change:
- Regenerate and update `docs/contracts/api/openapi.json`.

## Acceptance Criteria

- Avatar implementation remains blocked until TTS audio format strategy is resolved.
- No GameAgent call is introduced in v1.
- Avatar uses `AvatarEventCatalog` as the source of v1 copy.
- Avatar uses existing tones: `CALM`, `JOYFUL`, `ENTHUSIASTIC`, `SERIOUS`, `NEUTRAL`.
- Avatar uses existing event types from the content/backend contract.
- Avatar respects `ttsEnabled` and `agentEnabled` from family/child configuration.
- Avatar emits text fallback when audio is disabled, TTS fails, or TTS times out.
- WebSocket contract documents `GAME_AVATAR_EVENT` before code implementation.
- Audio cache behavior is deterministic and safe for child-specific text.

## Risks

- TTS format drift: backend design expects MP3 but current TTS contract returns WAV.
- Contract drift: frontend cannot safely consume binary avatar audio until `websocket.json` defines the protocol.
- Latency regression: TTS and future GameAgent calls must never block the child experience beyond configured thresholds.
- Ownership drift: avatar must not duplicate family configuration or content catalog data.
- Tone mismatch: all modules must use the same tone enum to avoid invalid catalog lookups.
