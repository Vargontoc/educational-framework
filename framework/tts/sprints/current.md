# Sprint 001 - tts
# -----------------------------------------------

## Goal

Create the initial `tts-educational` API shell and Dockerfile for FEAT-001, without implementing real TTS provider integration.

## Status

status: proposal
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

- [ ] Create the `tts-educational` service directory structure under `framework/tts`.
- [ ] Create a minimal Python API application.
- [ ] Add `GET /health` returning a successful service status response.
- [ ] Add `POST /api/v1/tts/synthesize` as a placeholder endpoint returning `501 Not Implemented`.
- [ ] Add service-level configuration loading for reserved TTS environment variables.
- [ ] Add a Dockerfile for the `tts-educational` image.
- [ ] Add minimal tests for healthcheck and placeholder synthesis behavior.
- [ ] Verify the Docker image builds successfully.
- [ ] Document local build and run instructions if needed.

## Risks

- Scope creep into Chatterbox, Coqui/XTTS, fallback, conversion, cache, backend integration, or frontend integration.
- Accidental coupling with `api-educational` instead of keeping `tts-educational` independent.
- Premature contract drift before the real synthesis API is finalized.
- Dockerfile builds may fail if Python dependency versions are not pinned or if the base image is too heavy.

## Dependencies

- `docs/architecture/decisions/ADR-012-Replain-tts-service.md`
- `docs/product/features/tts/FEAT-001-Rebuild-TTS-Layer.md`

No blocking dependency on backend, frontend, Chatterbox, Coqui, or infrastructure compose changes for this sprint.

## Agent Instruction

- Implement only the API shell and Dockerfile defined by FEAT-001.
- Do not call Chatterbox or Coqui.
- Do not implement provider fallback logic.
- Do not implement WAV/PCM to MP3 conversion.
- Do not implement audio cache.
- Do not modify `framework/infrastructure/docker-compose.yml` or `framework/infrastructure/docker-compose.prod.yml`.
- Do not modify backend or frontend source files.
- The placeholder synthesis endpoint must clearly return `501 Not Implemented`.
- The service may load reserved environment variables but must start without provider containers.

## Notes

This sprint prepares only the service boundary for `tts-educational`. Provider integration and `docs/contracts/api/openapi_tts.json` repopulation are intentionally deferred to later features.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
