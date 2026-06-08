# FEAT-005 - Expand FastAPI Base Structure and Routers

## Status

state: accepted
user_history: Expand the `tts-educational` FastAPI shell into a maintainable internal API structure
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-001-Rebuild-TTS-Layer.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`, `docs/product/features/tts/FEAT-003-Conversor-WAV-MP3.md`, `docs/product/features/tts/FEAT-004-Map-Tone.md`
owned_by: tts
scope: tts-service API structure only
test: Validate routers, configuration loading, provider adapter selection, health, status, and placeholder synthesis behavior without provider containers.

## Description

This feature expands the existing `tts-educational` FastAPI shell into a maintainable internal API structure.

The service already exposes a basic `/health` endpoint. This feature adds domain routers, centralized configuration, and the provider adapter pattern required by ADR-012 so future provider integrations can switch between Chatterbox and XTTS without changing backend-facing endpoints.

This feature does not implement real calls to Chatterbox or Coqui/XTTS.

## In Scope

- Organize FastAPI routes by domain.
- Add or preserve `GET /health`.
- Add `POST /api/v1/tts/synthesize` through a TTS router.
- Add `GET /api/v1/tts/status` through an engine/status router.
- Add centralized configuration loading for active provider and provider URLs.
- Add provider adapter interface or protocol.
- Add provider adapter stubs for Chatterbox and XTTS v2.
- Add provider factory or resolver based on `TTS_PROVIDER`.
- Expose converter availability in service status.
- Add tests for routing, configuration, adapter selection, and placeholder behavior.

## Out Of Scope

- Real Chatterbox integration.
- Real Coqui/XTTS integration.
- Real synthesis.
- Provider fallback implementation.
- Audio cache.
- Backend changes.
- Frontend changes.
- Infrastructure compose changes.
- New public endpoints beyond FEAT-002.
- Docker Compose profile scripts.

## Routers

Expected route organization:

- Health router:
  `GET /health`

- TTS router:
  `POST /api/v1/tts/synthesize`

- Engine/status router:
  `GET /api/v1/tts/status`

## Configuration

The service must load these variables with safe defaults where possible:

- `TTS_PROVIDER`.
- `TTS_ENABLE_FALLBACK`.
- `TTS_FALLBACK_PROVIDER`.
- `CHATTERBOX_BASE_URL`, default `http://chatterbox-educational:4123`.
- `COQUI_BASE_URL`, default `http://coqui-educational:5002`.
- `TTS_OUTPUT_FORMAT`, default `mp3`.
- `TTS_TIMEOUT_MS`.

Provider URLs must not be hardcoded in route handlers.

## Provider Adapter Pattern

The adapter boundary should allow future provider implementations without changing public endpoints.

Expected providers:

- `chatterbox`.
- `xtts`.

Adapters created in this feature are stubs only. They must not call provider containers.

Stub synthesis behavior must remain controlled and explicit. The endpoint may keep returning `501 Not Implemented` until real provider integration is implemented.

## Health And Status

`GET /health` should answer whether `tts-educational` itself is alive.

`GET /api/v1/tts/status` should expose provider-agnostic service status, including:

- Active provider name.
- Fallback enabled flag.
- Output format.
- Converter availability.
- Provider adapter configured status.

The status endpoint must not leak native provider parameters.

## Testing

Tests must not require Chatterbox, Coqui, GPU, or Docker Compose.

Required tests:

- `/health` returns successful service status.
- `/api/v1/tts/status` returns active provider and converter availability.
- `TTS_PROVIDER=chatterbox` resolves Chatterbox adapter stub.
- `TTS_PROVIDER=xtts` resolves XTTS adapter stub.
- Unknown provider returns a controlled configuration error.
- `POST /api/v1/tts/synthesize` still returns controlled not-implemented behavior.
- Provider URLs are loaded from configuration.

## Acceptance Criteria

- FastAPI routes are organized by domain.
- Public endpoints match FEAT-002.
- Provider adapter stubs exist for Chatterbox and XTTS v2.
- Active provider is selected from configuration.
- Provider ports `4123` and `5002` are defaults, not hardcoded in route logic.
- `/health` does not require provider containers.
- `/api/v1/tts/status` reports provider-agnostic status.
- No real provider calls are implemented.
- Backend-facing contract remains unchanged.

## Risks

- API structure grows before real provider needs are known.
- Provider-specific details leak into route handlers.
- Healthcheck becomes too strict and prevents local development without provider containers.
- Adapter stubs are mistaken for production-ready integration.

## Notes

This feature prepares the FastAPI structure for later provider integration features. Chatterbox and Coqui/XTTS calls will be implemented separately.
