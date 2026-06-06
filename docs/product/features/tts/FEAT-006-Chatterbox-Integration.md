# FEAT-006 - Chatterbox Adapter Integration

## Status

state: accepted
user_history: Real adapter integration from `tts-educational` to `chatterbox-educational`
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`, `docs/product/features/tts/FEAT-003-Conversor-WAV-MP3.md`, `docs/product/features/tts/FEAT-004-Map-Tone.md`, `docs/product/features/tts/FEAT-005-Expand-FastAPI.md`
owned_by: tts
scope: tts-service Chatterbox adapter only
test: Validate Chatterbox adapter behavior with mocked HTTP responses, tone mapping, timeout handling, and MP3 normalization without requiring a real Chatterbox container.

## Description

This feature implements the Chatterbox provider adapter inside `tts-educational`.

`tts-educational` must not load the Chatterbox model directly. Chatterbox runs as the external internal provider service `chatterbox-educational`, and `tts-educational` connects to it through the configured `CHATTERBOX_BASE_URL`.

The adapter receives the provider-agnostic synthesis request from the FastAPI layer, applies the internal tone mapping from FEAT-004, calls `chatterbox-educational`, and normalizes the returned audio to MP3 using the conversion module from FEAT-003 when needed.

The backend continues to call only `tts-educational`.

## In Scope

- Implement the Chatterbox provider adapter in `tts-educational`.
- Use `CHATTERBOX_BASE_URL`, default `http://chatterbox-educational:5003`.
- Apply FEAT-004 tone mapping before calling Chatterbox.
- Send provider-specific Chatterbox parameters internally.
- Apply configurable timeout using `TTS_TIMEOUT_MS`.
- Convert returned WAV/PCM audio to MP3 when Chatterbox does not return MP3.
- Return backend-compatible `audio/mpeg`.
- Map Chatterbox failures to stable TTS API errors.
- Add tests using mocked Chatterbox HTTP responses.

## Out Of Scope

- Building the `chatterbox-educational` container.
- Defining Docker Compose services.
- Loading the Chatterbox model inside `tts-educational`.
- XTTS/Coqui integration.
- Provider fallback logic.
- Audio cache.
- Backend changes.
- Frontend changes.
- WebSocket changes.
- Public API contract changes unless FEAT-002 is found incomplete.

## Chatterbox Request Mapping

The adapter must translate semantic TTS fields into Chatterbox-specific parameters using the internal mapping table.

Semantic inputs may include:

- `text`.
- `locale`.
- `tone`.
- `emotion`.
- `intensity`.
- `voiceProfile`.
- `outputFormat`.

Chatterbox internal parameters may include:

- `exaggeration`.
- `cfg_weight`.
- `temperature`.
- `audio_prompt`.

These fields must remain internal and must not be exposed to `api-educational`.

## Audio Normalization

If Chatterbox returns WAV/PCM, the adapter must call the FEAT-003 conversion module.

The final response from `tts-educational` must be:

- Content type: `audio/mpeg`.
- Format: MP3.
- Backend-compatible.

If Chatterbox returns MP3 directly, the adapter may pass through the audio after validating the content type or audio signature.

## Error Handling

The adapter must map failures consistently:

- Chatterbox unavailable -> `503`.
- Chatterbox timeout -> `504`.
- Unsupported tone or mapping -> `422`.
- Invalid Chatterbox response -> controlled `500` provider response error unless the contract is extended later.
- Unexpected failure -> `500`.

The API response shape must remain compatible with `docs/contracts/api/openapi_tts.json`.

## Testing

Tests must not require a real Chatterbox container, GPU, or Docker Compose.

Required tests:

- Adapter uses `CHATTERBOX_BASE_URL` from configuration.
- Adapter applies tone mapping for each supported tone.
- Adapter sends Chatterbox-specific parameters only internally.
- Mocked WAV response converts to MP3.
- Mocked MP3 response passes through if supported.
- Timeout maps to controlled timeout error.
- Connection failure maps to provider unavailable error.
- Invalid provider response maps to controlled error.
- Public API response remains `audio/mpeg`.

## Acceptance Criteria

- `tts-educational` can select and invoke the Chatterbox adapter when `TTS_PROVIDER=chatterbox`.
- Chatterbox URL is configurable and defaults to `http://chatterbox-educational:5003`.
- Chatterbox native parameters do not leak into the backend contract.
- Tone mapping from FEAT-004 is used.
- Audio normalization from FEAT-003 is used when needed.
- Backend receives MP3 `audio/mpeg`.
- Tests pass without a real Chatterbox container.
- No XTTS fallback behavior is implemented in this feature.

## Risks

- Chatterbox provider API shape may change.
- Chatterbox output format may differ from expectations.
- Timeouts may be too aggressive for long narration.
- Tone mapping may not produce good Spanish child-facing prosody without manual validation.
- Adapter integration could accidentally expose provider-specific parameters.

## Notes

This feature integrates only the Chatterbox adapter path. XTTS fallback and provider startup orchestration are handled by later features.
