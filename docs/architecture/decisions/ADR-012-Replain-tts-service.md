# ADR-012 — Refactor TTS Service Layer

## Status

status:        accepted
date:          2026-05-26
superseded_by: ADR-013-Chatterbox-unico-proveedor-TTS.md (parcialmente — XTTS eliminado como fallback)

---

## Context

The current TTS layer is based on XTTS v2 through the Coqui TTS container. The existing contract in `docs/contracts/api/openapi_tts.json` is strongly coupled to that implementation: it exposes `/api/tts`, `speaker_wav`, `language_id`, `audio/wav`, the `coqui-educational:5002` service name, and several XTTS-specific implementation notes.

The application now needs a more expressive internal TTS service for a child-facing experience, especially for the `Family Reading` flow. Long narration benefits from better prosody and from explicit emotional controls such as warmth, calm, urgency, emotion, and intensity. Chatterbox Multilingual is selected as the primary framework because it is actively maintained and fits the target experience for children aged 3 to 4.

Spanish accent remains a core requirement. As with XTTS v2, the accent is expected to be influenced by the voice reference. Chatterbox also supports multilingual zero-shot voice cloning, which keeps it aligned with the current on-premise voice strategy.

XTTS v2 must remain available as a fallback because real-world Spanish quality must still be validated with Chatterbox.

## Decision

The TTS layer will be refactored into an independent internal API named `tts-educational`. This service owns the backend-to-TTS contract, provider selection, provider-specific integration, and final audio format normalization.

The runtime flow becomes:

```text
api-educational
  -> tts-educational
      -> chatterbox-educational
      -> coqui-educational
```

`tts-educational` is developed and configured independently from `api-educational`. It exposes its own port, environment configuration, healthcheck, and OpenAPI contract.

The infrastructure will run two separate provider containers behind `tts-educational`:

- `chatterbox-educational`: primary TTS service.
- `coqui-educational`: XTTS v2 fallback service.

The backend consumes only `tts-educational`. It must not call `chatterbox-educational` or `coqui-educational` directly. The frontend and agents must not call any TTS service directly.

`tts-educational` selects the concrete provider through configuration. The default provider is Chatterbox. XTTS v2 through Coqui remains available as a fallback provider.

The public internal contract for the backend-to-TTS boundary remains `docs/contracts/api/openapi_tts.json`. That file must be repopulated with the API implemented by `tts-educational`, not with the native API of Chatterbox or Coqui.

`tts-educational` owns the final audio format responsibility. The backend must receive backend-compatible audio directly from `tts-educational`. The primary synthesis endpoint must return MP3 as `audio/mpeg`.

If Chatterbox does not natively produce MP3, the conversion from WAV/PCM to MP3 must happen inside `tts-educational`. The backend must not contain audio conversion logic.

The same rule applies to the XTTS fallback path: if XTTS produces WAV, the Coqui adapter inside `tts-educational` must normalize the result to MP3 before returning it to the backend.

Provider container startup may be optimized later. Infrastructure should evaluate Docker Compose profiles or startup scripts so the configured provider can determine whether `chatterbox-educational`, `coqui-educational`, or both are started.

## Contract Impact

`docs/contracts/api/openapi_tts.json` must be updated before backend Avatar implementation proceeds.

The new contract describes `tts-educational`. It must avoid leaking framework-specific parameters such as `speaker_wav` as the stable public API. Provider-specific details belong inside `tts-educational` adapters.

The new synthesis contract should describe a semantic request shape, including at least:

- `text`.
- `locale`.
- `tone` or equivalent semantic delivery style.
- `emotion` and `intensity` if exposed by the service.
- `voiceProfile` or equivalent stable voice identity.
- `outputFormat`, with `mp3` as the required backend-compatible format.

The main successful response must document `audio/mpeg`.

The contract must also define limits and failure modes:

- Maximum text length.
- Supported locales.
- Supported output formats.
- Timeout behavior.
- `400` for invalid input.
- `422` for unsupported synthesis parameters.
- `503` when the selected model or provider is unavailable.
- `504` when synthesis or conversion exceeds the configured timeout.
- `500` for unexpected synthesis failures.

The contract should include version metadata for cache safety:

- Provider name.
- Model version.
- Voice profile version.
- Audio format version.
- Synthesis profile version.

Expected service-level configuration includes:

- `TTS_PROVIDER=chatterbox|xtts`.
- `TTS_ENABLE_FALLBACK=true|false`.
- `TTS_FALLBACK_PROVIDER=xtts`.
- `CHATTERBOX_BASE_URL=http://chatterbox-educational:<port>`.
- `COQUI_BASE_URL=http://coqui-educational:5002`.
- `TTS_OUTPUT_FORMAT=mp3`.
- `TTS_TIMEOUT_MS=<milliseconds>`.

`docs/contracts/api/websocket.json` must also be updated when Avatar audio delivery is implemented. The frontend-facing game WebSocket contract must define `GAME_AVATAR_EVENT`, `audioAvailable`, `audioId`, fallback `text`, binary audio delivery, and MP3 as the expected audio format.

## Layer Impact

### Agents

The agents layer is not expected to change for this refactor.

The child agent contract already exposes semantic fields such as `content_text` and `tone`, and explicitly states that TTS mapping is a backend responsibility. Agents must not emit Chatterbox-specific or XTTS-specific technical parameters.

If richer delivery control is needed later, it should be introduced as semantic fields, not as raw model parameters.

### Backend

The backend must treat TTS as an external service exposed by `tts-educational`.

Backend responsibilities:

- Call only `tts-educational` for synthesis.
- Apply strict timeouts.
- Fall back to text-only Avatar metadata when TTS fails.
- Cache generated MP3 audio, not WAV.
- Include provider, model, voice profile, synthesis profile, and audio format versions in cache keys.

Backend must not:

- Convert WAV to MP3.
- Expose provider-specific parameters to frontend or agents.
- Call `chatterbox-educational` or `coqui-educational` directly.
- Block the child-facing flow indefinitely while waiting for TTS.

### Frontend

The frontend must not depend on Chatterbox or XTTS directly.

The frontend only consumes backend WebSocket events and MP3 audio payloads once the Avatar protocol is defined in `docs/contracts/api/websocket.json`.

Frontend playback must remain progressive: if audio is disabled, late, unavailable, or failed, the UI must continue with text fallback.

### Infrastructure

Infrastructure must support `tts-educational` and the provider containers it can call.

Required infrastructure changes when implemented:

- Add a `tts-educational` service with its own build, port, env file, healthcheck, and internal network access.
- Add a `chatterbox-educational` service.
- Keep `coqui-educational` as XTTS fallback.
- Keep all TTS-related services on the internal Docker network.
- Do not expose TTS services to the host in production.
- Add environment examples for `tts-educational`, Chatterbox, and backend connection to `tts-educational`.
- Add model volumes as needed.
- Add healthchecks that verify synthesis readiness.
- Include MP3 conversion dependencies inside the `tts-educational` image if any provider does not produce MP3 directly.
- Define GPU/CPU behavior clearly because two TTS containers may compete for resources.
- Evaluate Docker Compose profiles or startup scripts so local and production startup can activate only the configured provider when desired.

## Consequences

### Positive

- Chatterbox becomes the primary path for improved long-form prosody and emotion/intensity control.
- XTTS v2 remains available as a fallback for Spanish quality or runtime issues.
- `tts-educational` can be developed, tested, and deployed independently from `api-educational`.
- Backend remains an orchestrator instead of becoming an audio-processing layer.
- Frontend receives one stable audio format: MP3.
- The TTS contract can become provider-agnostic and more stable.
- Avatar implementation can be unblocked once `openapi_tts.json` and `websocket.json` are updated.

### Negative / Trade-offs

- `tts-educational` becomes more complex because it may need internal audio conversion.
- One additional internal API service must be built and operated.
- The Chatterbox container may require additional dependencies such as an MP3 encoder.
- Running two TTS containers increases memory, disk, and potentially GPU/VRAM pressure.
- Healthchecks must validate more than process liveness; they must validate synthesis and conversion readiness.
- Cache invalidation becomes more important because provider/model/format changes can produce different audio for the same text.

## Risks and Mitigations

Risk: Chatterbox Spanish output is not good enough in real child-facing tests.

Mitigation: Keep XTTS v2 as a separate fallback container and make provider selection configurable in `tts-educational`.

Risk: Chatterbox does not output MP3 natively.

Mitigation: Add WAV/PCM to MP3 conversion inside `tts-educational`. Do not move conversion to backend.

Risk: Contract drift between Chatterbox, XTTS, backend, and frontend.

Mitigation: Treat `docs/contracts/api/openapi_tts.json` as the single source of truth for backend-to-TTS calls, and `docs/contracts/api/websocket.json` as the single source of truth for backend-to-frontend audio delivery.

Risk: Cache collisions across providers or model versions.

Mitigation: Include provider, model version, voice profile version, synthesis profile version, and audio format version in the cache key.

Risk: TTS latency harms the child experience.

Mitigation: Use strict backend timeouts, pre-generate deterministic catalog audio where possible, cache MP3 output, and always allow text fallback.

Risk: TTS containers compete for GPU resources.

Mitigation: Define environment-specific GPU policy and allow only one active provider if host resources are constrained.

Risk: The extra `tts-educational` API adds operational complexity.

Mitigation: Keep the API narrow, provider-agnostic, and covered by `openapi_tts.json`; use healthchecks and simple provider configuration to keep backend integration stable.

## Relation to Previous Decisions

ADR-004 remains valid for the original on-premise/private TTS motivation, but its Coqui-specific implementation is superseded by this ADR for the primary TTS path.

ADR-005 remains valid for XTTS voice reference generation and fallback operation. Chatterbox may reuse voice references if compatible, but it is not required to expose `speaker_wav` through the stable TTS API.

## Nota (2026-07-18)

**ADR-013** ha eliminado XTTS v2 como fallback. Chatterbox es ahora el único proveedor de síntesis de voz. Esta decisión simplifica la operativa para el contexto monofamiliar. Ver `docs/decisions/ADR-013-Chatterbox-unico-proveedor-TTS.md` para detalles.

