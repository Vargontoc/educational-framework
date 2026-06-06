# Sprint 006 - tts
# -----------------------------------------------

## Goal

Implement real Chatterbox provider adapter inside `tts-educational` with HTTP calls to `chatterbox-educational`, tone mapping from FEAT-004, and WAV→MP3 conversion from FEAT-003. Chatterbox always returns WAV so conversion is always required.

## Status

status: active
started_at: 2026-06-05
closed_at:
blocked_by:
waiting_for:

## Tasks

- [ ] `app/config.py` — validate `TTS_PROVIDER` is not empty at startup, raise `ValueError` if empty; add `chatterbox_synthesis_endpoint: str = "/v1/synthesize"`
- [ ] `app/adapters/chatterbox.py` — replace stub with real HTTP: httpx.Client, POST to {CHATTERBOX_BASE_URL}/v1/synthesize, resolve tone mapping, always convert WAV→MP3, map errors (503/504/500)
- [ ] `app/adapters/factory.py` — propagate empty provider error
- [ ] `routes/tts.py` — replace 501 with real call to adapter, return `Response(content=mp3_bytes, media_type="audio/mpeg")`, request model with text/locale/tone/emotion/intensity/voice_profile/output_format
- [ ] `envs/.env` — add `CHATTERBOX_SYNTHESIS_ENDPOINT=/v1/synthesize`, `TTS_PROVIDER=chatterbox`
- [ ] `tests/test_chatterbox_adapter.py` — mock httpx responses, 5 tests: WAV→MP3, timeout→504, connection error→503, tone mapping applied, TTS_PROVIDER empty→startup error
- [ ] `pytest` passes without Chatterbox container

## Risks

- Chatterbox API shape may differ from assumptions
- Tone mapping may not produce good Spanish child-facing prosody without manual validation
- Timeout too aggressive for long narration
- Adapter integration could accidentally expose provider-specific parameters

## Dependencies

- `docs/architecture/decisions/ADR-012-Replain-tts-service.md`
- `docs/product/features/tts/FEAT-002-Contracts-API.md`
- `docs/product/features/tts/FEAT-003-Conversor-WAV-MP3.md`
- `docs/product/features/tts/FEAT-004-Map-Tone.md`
- `docs/product/features/tts/FEAT-005-Expand-FastAPI.md`
- `docs/product/features/tts/FEAT-006-Chatterbox-Integration.md`

No blocking dependency on backend, frontend, infrastructure compose, or XTTS/fallback changes.

## Agent Instruction

- Implement only the Chatterbox adapter path — no XTTS fallback, no provider fallback logic
- Do NOT call real Chatterbox container — use mocked HTTP responses in tests
- Do NOT implement audio cache behavior
- Do NOT modify `framework/infrastructure/docker-compose.yml` or `framework/infrastructure/docker-compose.prod.yml`
- Do NOT modify backend or frontend source files
- Do NOT modify `docs/contracts/api/openapi_tts.json`
- Chatterbox always returns WAV — always call `convert_wav_to_mp3()`
- If `TTS_PROVIDER` is empty at startup, raise `ValueError("TTS_PROVIDER is required")` — do not default silently
- Return only audio bytes with `audio/mpeg` content type — no version headers in this sprint
- Keep Chatterbox native parameters (exaggeration, cfg_weight, temperature, audio_prompt) internal

## Notes

This sprint integrates only the Chatterbox adapter path. Provider fallback and XTTS integration are handled by later features.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions: