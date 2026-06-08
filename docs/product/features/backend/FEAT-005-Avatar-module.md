# FEAT-005 - Backend: Avatar Module

## Status

state: unblocked
user_history: Avatar interaction
depends_on: shared, family, session, content, `openapi_tts.json`
future_depends_on: minigame event pipeline, agent
blocked_by: none
deferred_until_game_pipeline: Backend minigame orchestration must exist before activity-driven avatar events are implemented
test: unit + integration + contract
sprints:

## Description

The avatar module is the backend gateway for generated child-facing speech. It covers short NPC/avatar feedback heard by the child during the game experience and also the storyteller voice used by other flows such as reading. It translates finite game/session events into short avatar responses, resolves the effective family and child configuration, selects catalog-backed fallback text from content, invokes TTS when available, manages audio cache, emits lifecycle avatar results through the game WebSocket channel, and later emits activity-driven avatar results once the minigame event pipeline exists.

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
- Manage audio cache for deterministic catalog messages and generated speech.
- Use the shared TTS endpoint for both NPC/avatar speech and storyteller narration, selected through the TTS `voice_profile` request field.
- Emit initial lifecycle avatar metadata/audio through the native game WebSocket channel for connection and controlled disconnection.
- Prepare activity-driven avatar metadata for the native game WebSocket channel once the minigame event pipeline is available.

Out of scope for v1:
- GameAgent integration.
- LLM-generated or enriched avatar copy.
- Conversational memory.
- AdultAgent integration.
- New avatar tone enums.
- Moving `AvatarConfig` ownership out of `family`.
- Implementing backend minigame engines or the activity event pipeline required to trigger activity-driven `GAME_AVATAR_EVENT` messages.

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
- Game WebSocket avatar metadata and audio delivery protocol for lifecycle events first, and activity-driven events once the minigame pipeline is implemented.
- Stable voice-profile selection for child-facing generated speech, including NPC/avatar and storyteller use cases.

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

`eventType + tone + locale + textHash + voiceProfile + ttsVoiceReference + audioFormatVersion`

Two cache levels are expected now that the TTS MP3 format strategy is unblocked:
- L1 memory cache for frequent short catalog messages.
- L2 local disk/storage cache for generated runtime audio.

Cache rules:
- Catalog messages without child-specific text can be globally reusable.
- Messages containing child-specific values, such as the child's name, must be cached with child-specific context in the key or invalidated when that context changes.
- Cache invalidation must happen when message text, tone, TTS reference voice, locale, or audio format changes.
- Frequent active catalog messages may be pre-generated at startup once the TTS service is reachable and configured.

Frequent event candidates:
- `ACTIVITY_STARTED`
- `ACTIVITY_COMPLETED`
- `ACTIVITY_FAILED`
- `HELP_REQUESTED`

## TTS Integration

The current TTS contract is `docs/contracts/api/openapi_tts.json`.

Resolved format decision:
- Backend calls `POST /api/v1/tts/synthesize` on `tts-educational`.
- Successful TTS responses are MP3 bytes with content type `audio/mpeg`.
- WAV/PCM to MP3 normalization is owned by `tts-educational`.
- Backend must not call provider-specific services such as Coqui/XTTS or Chatterbox directly.
- Backend must not convert WAV to MP3 locally.

Voice profile usage:
- NPC/avatar game speech and storyteller narration use the same TTS endpoint.
- The backend selects the intended voice through the request `voice_profile` field.
- `voice_profile: "npc"` is used for short game avatar/NPC feedback.
- `voice_profile: "storyteller"` is used for reading or narration flows.
- TTS service owns provider-specific interpretation of each voice profile.

Tone-to-TTS mapping for the TTS service:
- `CALM` -> `calm`
- `JOYFUL` -> `joyful`
- `ENTHUSIASTIC` -> `enthusiastic`
- `SERIOUS` -> `serious`
- `NEUTRAL` -> `calm` for v1 unless the TTS contract adds an explicit `neutral` tone

Contract alignment note:
- `docs/contracts/api/openapi_tts.json` is the source contract for backend implementation.
- The live TTS service on port `8081` has been observed returning `audio/mpeg` for synthesis.
- The live service OpenAPI must be aligned with the repository contract if it still documents an older response shape.

Timeout behavior:
- TTS timeout must be configurable.
- On timeout or failure, the avatar event must still be emitted as text-only metadata with `audioAvailable: false`.
- No child-facing event should block waiting indefinitely for TTS.

## WebSocket Contract Impact

Avatar events use the native game WebSocket channel, not the parental STOMP channel.

`GAME_AVATAR_EVENT` starts with lifecycle delivery over the existing native game WebSocket flow. Activity-driven avatar events remain deferred until backend minigame orchestration exists.

`docs/contracts/api/websocket.json` must be updated before lifecycle WebSocket avatar delivery implementation to include:
- `GAME_AVATAR_EVENT` server-to-client metadata message.
- `eventType`.
- `audioAvailable`.
- `audioId` correlation id when audio is available.
- `text` fallback for visual rendering.
- Binary audio message format and correlation strategy.
- Maximum binary message size supported by Spring WebSocket.
- Behavior when audio is disabled, unavailable, late, or dropped.

Lifecycle event texts for the first real integration:
- On successful game WebSocket authentication: `Hola, vamos a jugar!`.
- On controlled backend-initiated session close, expulsion, timeout, or equivalent session end: `Vaya, parece que es hora de despedirnos. Hasta la proxima.`.
- Farewell delivery is best-effort and is not guaranteed when the client abruptly disconnects before the backend can send.

Expected two-message protocol when audio is available:

```json
{
  "type": "GAME_AVATAR_EVENT",
  "eventType": "SESSION_CONNECTED",
  "audioAvailable": true,
  "audioId": "uuid-correlation-id",
  "text": "Hola, vamos a jugar!"
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
  "eventType": "SESSION_CONNECTED",
  "audioAvailable": false,
  "text": "Hola, vamos a jugar!"
}
```

## Contract Impact

Required before avatar audio/TTS core implementation:
- Use `docs/contracts/api/openapi_tts.json` as the backend-to-TTS contract.
- Use `POST /api/v1/tts/synthesize` and expect `audio/mpeg` MP3 bytes.
- Send `voice_profile` according to the target use case: `npc` for avatar/game speech and `storyteller` for narration.

Required before lifecycle game WebSocket avatar delivery implementation:
- Update `docs/contracts/api/websocket.json` for avatar metadata and binary audio delivery.
- Use successful game WebSocket authentication as the first welcome trigger.
- Use controlled backend-initiated session end as the first farewell trigger where delivery is still possible.

Required before activity-driven game WebSocket avatar delivery implementation:
- Ensure the backend game/minigame event pipeline can trigger activity avatar events.

Not required for v1:
- Agent contract changes.
- GameAgent payload changes.

Required if REST configuration endpoints change:
- Regenerate and update `docs/contracts/api/openapi.json`.

## Acceptance Criteria

- Avatar audio/TTS core implementation is no longer blocked by the TTS audio format strategy.
- No GameAgent call is introduced in v1.
- Avatar uses `AvatarEventCatalog` as the source of v1 copy.
- Avatar uses existing tones: `CALM`, `JOYFUL`, `ENTHUSIASTIC`, `SERIOUS`, `NEUTRAL`.
- Avatar maps `NEUTRAL` to TTS `calm` for v1 unless the TTS service adds an explicit `neutral` tone.
- Avatar uses existing event types from the content/backend contract.
- Avatar respects `ttsEnabled` and `agentEnabled` from family/child configuration.
- Avatar emits text fallback when audio is disabled, TTS fails, or TTS times out.
- Avatar sends `voice_profile: "npc"` for game avatar/NPC feedback.
- Storyteller or narration flows use the same TTS endpoint with `voice_profile: "storyteller"`.
- WebSocket contract documents `GAME_AVATAR_EVENT` before lifecycle game WebSocket avatar delivery code is implemented.
- Avatar emits a welcome lifecycle message after successful game WebSocket authentication.
- Avatar attempts a farewell lifecycle message before controlled backend-initiated session end when the socket is still open.
- Audio cache behavior is deterministic and safe for child-specific text.

## Risks

- TTS contract drift: repository contract and live service OpenAPI must stay aligned with the actual `audio/mpeg` synthesis response.
- Contract drift: frontend cannot safely consume binary avatar audio until `websocket.json` defines the protocol.
- Latency regression: TTS and future GameAgent calls must never block the child experience beyond configured thresholds.
- Ownership drift: avatar must not duplicate family configuration or content catalog data.
- Tone mismatch: all modules must use the same tone enum to avoid invalid catalog lookups.
- Voice-profile mismatch: backend and TTS service must keep `npc` and `storyteller` semantics aligned.
