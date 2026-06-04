# Name

TTS Coding Skill

## Pattern

Build `tts-educational` as a small internal Python API service with a clear boundary between API routes, configuration, and future provider adapters.

For FEAT-001:
1. Create the minimal API application.
2. Add configuration loading for reserved environment variables.
3. Add `GET /health`.
4. Add placeholder `POST /api/v1/tts/synthesize` returning `501 Not Implemented`.
5. Add Dockerfile.
6. Keep provider integrations out of scope.

## Conventions

- Keep the API shell minimal.
- Use explicit request and response models if the selected framework supports them.
- Do not hardcode provider URLs or timeout values in code.
- Do not call external provider containers in FEAT-001.
- Do not introduce audio conversion dependencies until a later feature requires them.
- Keep code and comments in English.

## After Coding

- Run local API tests when available.
- Verify `GET /health` works.
- Verify placeholder synthesis returns `501 Not Implemented`.
- Build the Docker image from the new Dockerfile.
- Do not update `docs/contracts/api/openapi_tts.json` unless the sprint explicitly requires it.
