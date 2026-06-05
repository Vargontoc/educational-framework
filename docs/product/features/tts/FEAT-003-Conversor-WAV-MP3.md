# FEAT-003 - WAV/PCM to MP3 Conversion

## Status

state: accepted
user_history: Normalize TTS provider audio output to MP3
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-001-Rebuild-TTS-Layer.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`
owned_by: tts
scope: tts-service conversion only
test: Convert a valid WAV fixture to MP3, validate invalid input handling, and verify the Docker image includes ffmpeg.

## Description

This feature implements the internal audio conversion layer inside `tts-educational`.

Provider adapters may return WAV or PCM audio. Before responding to `api-educational`, `tts-educational` must normalize provider output to MP3 so the backend always receives `audio/mpeg`, as required by ADR-012 and the TTS OpenAPI contract.

The conversion must be independent from the active provider. Chatterbox and Coqui/XTTS integrations will call this conversion module later, but this feature does not implement either provider integration.

## Technical Decision

Use the `ffmpeg` CLI installed in the `tts-educational` container and invoke it from Python through a small internal conversion module.

Do not use `pydub` for this feature. The implementation should keep the conversion dependency explicit and limited to `ffmpeg`.

Target output:

- Format: MP3.
- Content type: `audio/mpeg`.
- Channels: mono.
- Bitrate: 64 kbps.
- Purpose: child-facing speech and narration.

## In Scope

- Add `ffmpeg` to the `tts-educational` Docker image.
- Add an internal Python conversion module.
- Convert WAV/PCM input bytes to MP3 output bytes.
- Return controlled conversion errors for invalid input.
- Add tests using a small WAV fixture.
- Document conversion assumptions and target output settings.

## Out Of Scope

- Chatterbox integration.
- Coqui/XTTS integration.
- Provider fallback logic.
- Audio cache.
- Backend integration.
- Frontend integration.
- WebSocket contract changes.
- Docker Compose changes.
- New public API endpoints.
- Changes to backend-owned audio cache behavior.

## Conversion Behavior

The conversion module should accept audio bytes from future provider adapters and return MP3 bytes suitable for an `audio/mpeg` response.

Invalid or unsupported audio input must fail with a controlled internal error that can be mapped by `tts-educational` to a stable API error response in a later integration feature.

The conversion module must not write persistent files. Temporary files or pipes are acceptable if they are cleaned up reliably.

## Testing

Tests must not require Chatterbox, Coqui, GPU, or Docker Compose.

Required tests:

- Valid WAV fixture converts to non-empty MP3 bytes.
- Output is recognizable as MP3.
- Invalid input bytes produce a controlled conversion error.
- Conversion can run without provider containers.
- Docker image includes the `ffmpeg` binary.

If bitrate or channel validation is practical in the selected test setup, tests should also verify mono output at 64 kbps.

## Acceptance Criteria

- `tts-educational` includes an internal WAV/PCM to MP3 conversion module.
- Valid WAV input converts to MP3 bytes.
- MP3 output is intended for `audio/mpeg` responses.
- Target output is mono at 64 kbps.
- Invalid audio input is handled with a controlled conversion error.
- Conversion does not require Chatterbox or Coqui to be running.
- Docker image includes `ffmpeg`.
- Backend remains unaware of WAV/PCM and receives only MP3 once provider integration is implemented.
- No cache endpoint or cache invalidation behavior is added.

## Risks

- `ffmpeg` increases the container image size.
- Incorrect cleanup of temporary files could leak disk usage.
- Invalid provider output could surface as unstable API failures if conversion errors are not mapped consistently.
- 64 kbps mono may require validation with real child-facing narration before being considered final.

## Notes

This feature prepares the audio normalization layer required by ADR-012. Provider integration features will call this module later when Chatterbox and Coqui/XTTS adapters are implemented.
