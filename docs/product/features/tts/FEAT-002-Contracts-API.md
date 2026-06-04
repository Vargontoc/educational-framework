# FEAT-002 - TTS OpenAPI Contract

## Status

state: accepted
user_history: Internal API contract for TTS service
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-001-Rebuild-TTS-Layer.md`
unblocks: `docs/product/features/backend/FEAT-005-Avatar-module.md`
owned_by: tts
scope: tts-service contract only
test: Validate `docs/contracts/api/openapi_tts.json` as a provider-agnostic OpenAPI contract for `tts-educational`.

## Description

This feature defines the definitive backend-to-TTS OpenAPI contract for `tts-educational`, as described in ADR-012.

The contract must be independent from the active provider. `api-educational` always sends the same semantic request to `tts-educational`, and `tts-educational` internally resolves which provider to call and how to map the request to provider-specific parameters.

The contract must describe `tts-educational`, not the native API of Chatterbox or Coqui/XTTS.

## In Scope

- Repopulate `docs/contracts/api/openapi_tts.json` for `tts-educational`.
- Define the stable synthesis endpoint.
- Define health and provider status endpoints.
- Define request and response schemas.
- Define supported audio format as MP3 through `audio/mpeg`.
- Define input limits, supported locales, supported output formats, timeout behavior, and error responses.
- Keep the contract provider-agnostic.

## Out Of Scope

- API implementation.
- Dockerfile changes.
- Chatterbox integration.
- Coqui/XTTS integration.
- Provider fallback implementation.
- WAV/PCM to MP3 conversion implementation.
- Audio cache implementation or invalidation endpoints.
- Backend code changes.
- Frontend code changes.
- WebSocket contract changes.
- Infrastructure compose changes.

## Endpoints

The contract must define:

- `GET /health`
  Returns service liveness/readiness information for `tts-educational`.

- `POST /api/v1/tts/synthesize`
  Synthesizes text into backend-compatible MP3 audio.

- `GET /api/v1/tts/status`
  Returns provider-agnostic status for the active TTS configuration, including whether the selected provider is available.

The contract must not define cache endpoints. Audio cache ownership belongs to the backend Avatar module.

## Synthesis Request

The synthesis endpoint must accept a semantic request shape with at least:

- `text`: required text to synthesize.
- `locale`: optional locale, default `es`.
- `tone`: semantic tone requested by backend.
- `emotion`: optional semantic emotion if exposed by the service.
- `intensity`: optional semantic intensity if exposed by the service.
- `voiceProfile`: stable voice identity selected by backend or defaulted by `tts-educational`.
- `outputFormat`: requested output format, default `mp3`.

The contract must not expose provider-native fields such as `speaker_wav` as stable public API fields.

## Synthesis Response

The successful synthesis response must return MP3 audio:

- Status: `200`.
- Content type: `audio/mpeg`.
- Body: binary MP3 audio.

The response should expose cache-safe metadata through headers or documented response metadata where appropriate:

- Provider name.
- Model version.
- Voice profile version.
- Audio format version.
- Synthesis profile version.

## Errors

The contract must define these error responses:

- `400`: invalid input, such as missing text or text exceeding the maximum length.
- `422`: unsupported synthesis parameters, such as unsupported locale, tone, voice profile, or output format.
- `503`: selected provider or model unavailable.
- `504`: synthesis or internal conversion timeout.
- `500`: unexpected synthesis failure.

Error responses must use a stable JSON shape so backend can map failures to text-only Avatar fallback.

## Limits

The contract must document:

- Maximum text length.
- Supported locales.
- Supported tones.
- Supported output formats.
- Default output format.
- Timeout behavior.
- Maximum expected audio payload size if known.

## Acceptance Criteria

- `docs/contracts/api/openapi_tts.json` describes `tts-educational`.
- The contract does not describe native Chatterbox or Coqui/XTTS APIs.
- `POST /api/v1/tts/synthesize` returns `audio/mpeg` on success.
- `GET /health` is documented.
- `GET /api/v1/tts/status` is documented.
- No `POST /cache/invalidate` or other cache management endpoint is documented.
- Provider-native fields such as `speaker_wav` are not part of the stable public request schema.
- Error responses include `400`, `422`, `503`, `504`, and `500`.
- Backend Avatar remains the owner of audio cache behavior.

## Risks

- Contract leaks provider-specific details and forces backend coupling to Chatterbox or Coqui.
- Contract defines audio formats that frontend/backend Avatar cannot consume safely.
- Contract omits timeout or error semantics, making text fallback unreliable.
- Contract includes cache operations even though cache ownership belongs to backend.

## Notes

This feature unblocks backend Avatar contract work by defining the backend-to-TTS boundary. Backend WebSocket audio delivery remains a separate contract update in `docs/contracts/api/websocket.json`.
