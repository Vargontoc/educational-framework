# FEAT-007 - XTTS Adapter Integration

## Status

state: proposal
user_history: Real adapter integration from `tts-educational` to `coqui-educational` for XTTS v2
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`, `docs/product/features/tts/FEAT-003-Conversor-WAV-MP3.md`, `docs/product/features/tts/FEAT-004-Map-Tone.md`, `docs/product/features/tts/FEAT-005-Expand-FastAPI.md`
owned_by: tts
scope: tts-service XTTS adapter only
test: Validate XTTS adapter behavior with mocked HTTP responses, tone mapping, timeout handling, and MP3 normalization without requiring a real Coqui/XTTS container.

## Description

This feature implements the XTTS v2 provider adapter inside `tts-educational` using the same internal adapter interface introduced for Chatterbox.

`tts-educational` must not expose XTTS-native parameters to `api-educational`. The backend continues to send the same provider-agnostic synthesis request, while `tts-educational` selects the active adapter with `TTS_PROVIDER=chatterbox|xtts`.

XTTS v2 runs behind the external internal provider service `coqui-educational`, and `tts-educational` connects to it through the configured `COQUI_BASE_URL`.

The adapter applies the internal tone mapping from FEAT-004, calls `coqui-educational`, and normalizes the returned WAV audio to MP3 using the conversion module from FEAT-003.

## In Scope

- Implement the XTTS v2 provider adapter in `tts-educational`.
- Use `COQUI_BASE_URL`, default `http://coqui-educational:5002`.
- Apply FEAT-004 tone mapping before calling XTTS.
- Send XTTS-specific parameters internally.
- Apply configurable timeout using `TTS_TIMEOUT_MS`.
- Convert returned WAV audio to MP3.
- Return backend-compatible `audio/mpeg`.
- Select XTTS when `TTS_PROVIDER=xtts`.
- Map XTTS/Coqui failures to stable TTS API errors.
- Add tests using mocked Coqui/XTTS HTTP responses.

## Out Of Scope

- Building or changing the `coqui-educational` container.
- Defining Docker Compose services.
- Loading XTTS inside `tts-educational`.
- Chatterbox adapter changes unless required by shared adapter interface fixes.
- Provider fallback logic.
- Audio cache.
- Backend changes.
- Frontend changes.
- WebSocket changes.
- Public API contract changes unless FEAT-002 is found incomplete.

## XTTS Request Mapping

The adapter must translate semantic TTS fields into XTTS-specific parameters using the internal mapping table.

Semantic inputs may include:

- `text`.
- `locale`.
- `tone`.
- `emotion`.
- `intensity`.
- `voiceProfile`.
- `outputFormat`.

XTTS internal parameters may include:

- `speaker_wav`.
- `speed`.

These fields must remain internal and must not be exposed to `api-educational`.

## Expected Coqui/XTTS Call

The adapter should call the configured Coqui endpoint using the current XTTS HTTP shape available in the existing infrastructure.

Expected internal request shape may include:

- `text`.
- `speaker_wav`.
- `language_id` or equivalent locale parameter.
- `speed` if supported by the provider endpoint.

The adapter must keep this native request shape isolated from the public `tts-educational` OpenAPI contract.

## Audio Normalization

XTTS/Coqui is expected to return WAV audio.

The adapter must call the FEAT-003 conversion module so the final response from `tts-educational` is:

- Content type: `audio/mpeg`.
- Format: MP3.
- Backend-compatible.

## Error Handling

The adapter must map failures consistently:

- Coqui/XTTS unavailable -> `503`.
- Coqui/XTTS timeout -> `504`.
- Unsupported tone or mapping -> `422`.
- Invalid Coqui/XTTS response -> controlled `500` provider response error unless the contract is extended later.
- Unexpected failure -> `500`.

The API response shape must remain compatible with `docs/contracts/api/openapi_tts.json`.

## Testing

Tests must not require a real Coqui/XTTS container, GPU, or Docker Compose.

Required tests:

- Adapter uses `COQUI_BASE_URL` from configuration.
- `TTS_PROVIDER=xtts` resolves the XTTS adapter.
- Adapter applies tone mapping for each supported tone.
- Adapter sends XTTS-specific parameters only internally.
- Mocked WAV response converts to MP3.
- Timeout maps to controlled timeout error.
- Connection failure maps to provider unavailable error.
- Invalid provider response maps to controlled error.
- Public API response remains `audio/mpeg`.

## Acceptance Criteria

- `tts-educational` can select and invoke the XTTS adapter when `TTS_PROVIDER=xtts`.
- Coqui/XTTS URL is configurable and defaults to `http://coqui-educational:5002`.
- XTTS native parameters do not leak into the backend contract.
- Tone mapping from FEAT-004 is used.
- Audio normalization from FEAT-003 is always used for XTTS WAV output.
- Backend receives MP3 `audio/mpeg`.
- Tests pass without a real Coqui/XTTS container.
- No provider fallback behavior is implemented in this feature.

## Risks

- Existing Coqui/XTTS endpoint shape may differ from adapter assumptions.
- XTTS output format may differ from expectations if the patched server changes.
- Timeout settings may be too aggressive for XTTS synthesis.
- Tone mapping may not produce consistent results across XTTS and Chatterbox.
- Adapter integration could accidentally expose XTTS-specific parameters.

## Notes

This feature integrates only the XTTS adapter path. Provider fallback and provider startup orchestration are handled by later features.
