# Name

TTS Testing Skill

## Unit Tests

- Framework: preferably pytest, unless the implementation chooses another Python test framework.
- Target: configuration loading and API route behavior.
- Rule: tests must not require Chatterbox, Coqui, GPU, or Docker Compose.

## API Tests

- `GET /health` must return a successful status response.
- `POST /api/v1/tts/synthesize` must return `501 Not Implemented` during FEAT-001.
- Placeholder synthesis tests may use a minimal valid future-shaped request body.

## Container Verification

- Docker image must build from `framework/tts/Dockerfile`.
- If the container is run locally, the healthcheck endpoint must be reachable.
- Do not require provider containers for FEAT-001 verification.

## Coverage Rules

- Cover all API shell endpoints introduced in FEAT-001.
- Cover default configuration loading.
- Cover environment variable override behavior if configuration loading is implemented.
