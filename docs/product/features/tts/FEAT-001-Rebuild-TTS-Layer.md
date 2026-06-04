# FEAT-001 - TTS API Shell

## Status

state: accepted
user_history: Internal API for TTS service
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`
owned_by: tts
scope: tts-service only (API shell and Dockerfile)
test: Healthcheck endpoint responds successfully and Docker image builds.

## Description

This feature creates the initial shell for the internal `tts-educational` service described in ADR-012.

The goal is to create a minimal Python API service that can be developed independently from `api-educational`. This service will later own provider selection, Chatterbox/XTTS integration, MP3 normalization, and the backend-to-TTS contract.

This feature only creates the service skeleton and its Dockerfile. It does not implement real synthesis or provider integration.

## In Scope

- Create the `tts-educational` service directory under `framework`.
- Create a minimal Python API application.
- Add a healthcheck endpoint.
- Add a placeholder synthesis endpoint.
- Add service-level configuration loading for future provider selection.
- Add a Dockerfile for the service.
- Document how to build and run the container locally if needed.

## Out Of Scope

- Chatterbox integration.
- Coqui/XTTS integration.
- Provider fallback logic.
- WAV/PCM to MP3 conversion.
- Audio cache.
- Backend integration.
- Frontend integration.
- Changes to `framework/infrastructure/docker-compose.yml`.
- Changes to `framework/infrastructure/docker-compose.prod.yml`.
- Full repopulation of `docs/contracts/api/openapi_tts.json`.

## Expected API Shell

The API shell should expose:

- `GET /health`
  Returns service status and is intended for Docker healthchecks.

- `POST /api/v1/tts/synthesize`
  Placeholder endpoint. It accepts the future synthesis request shape and returns `501 Not Implemented` until provider integration is implemented.

## Configuration

Initial configuration should reserve the following environment variables for later implementation:

- `TTS_PROVIDER`.
- `TTS_ENABLE_FALLBACK`.
- `TTS_FALLBACK_PROVIDER`.
- `CHATTERBOX_BASE_URL`.
- `COQUI_BASE_URL`.
- `TTS_OUTPUT_FORMAT`.
- `TTS_TIMEOUT_MS`.

For this feature, the API may load these variables but must not require provider containers to be running.

## Acceptance Criteria

- `tts-educational` API starts locally.
- `GET /health` returns a successful response.
- `POST /api/v1/tts/synthesize` exists and clearly returns not implemented.
- Docker image builds successfully from the new Dockerfile.
- The service does not call Chatterbox or Coqui.
- No infrastructure compose files are changed.
- No backend or frontend files are changed.

## Risks

- Scope creep into real provider integration.
- Accidental coupling with `api-educational`.
- Premature contract drift before the real synthesis API is finalized.

## Notes

This feature prepares the service boundary only. Provider integration and `openapi_tts.json` repopulation should be handled in later features.

