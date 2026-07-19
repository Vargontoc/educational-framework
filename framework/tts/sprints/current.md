# Sprint 007 - tts
# -----------------------------------------------

## Goal

**ELIMINADO** (2026-07-18) — Chatterbox es el único proveedor TTS. Ver ADR-013.

Implement real XTTS provider adapter inside `tts-educational` using the same adapter interface as Chatterbox (FEAT-006). Connect to `coqui-educational` via `COQUI_BASE_URL`, apply FEAT-004 tone mapping, always convert WAV→MP3 via FEAT-003, return `audio/mpeg` with proper error codes (503/504/422/500). No real Coqui/XTTS container in tests.

## Status

status: **cancelled**
started_at: 2026-06-06
closed_at: 2026-07-18
blocked_by:
waiting_for:
cancelled_reason: ADR-013 — Chatterbox es el único proveedor TTS. XTTS eliminado.

## Tasks

- [ ] `app/adapters/xtts.py` — replace stub with real HTTP: httpx.Client with per-provider timeout, POST to {COQUI_BASE_URL}/..., resolve tone mapping, always convert WAV→MP3, map errors (503/504/422/500)
- [ ] `app/config.py` — add `xtts_timeout_ms: int = 90000` as provider-specific timeout; Chatterbox keeps `tts_timeout_ms = 30000`; adapter selects appropriate timeout based on active provider
- [ ] `app/adapters/factory.py` — already maps "xtts" to `XttsAdapter`; no changes needed
- [ ] `routes/tts.py` — already calls adapter via `get_provider_adapter()`; no changes needed for routing, only error handling already present
- [ ] `envs/.env` — change `TTS_PROVIDER=xtts` to select XTTS adapter
- [ ] `tests/test_xtts_adapter.py` — mock httpx responses, 5+ tests: WAV→MP3, timeout→504, connection error→503, tone mapping applied, TTS_PROVIDER=xtts resolves adapter
- [ ] `pytest` passes without Coqui/XTTS container

## Risks

- Coqui/XTTS endpoint shape may differ from adapter assumptions
- XTTS output format may differ if patched server changes
- Timeout (`TTS_TIMEOUT_MS`) may be too aggressive for XTTS synthesis — mitigated: per-provider `xtts_timeout_ms = 90000` vs `tts_timeout_ms = 30000`
- Tone mapping may not produce consistent results across XTTS and Chatterbox
- `coqui_base_url` default is `http://localhost:5002` (local dev) not `http://coqui-educational:5002` — may need verification

## Dependencies

- `docs/architecture/decisions/ADR-012-Replain-tts-service.md`
- `docs/product/features/tts/FEAT-002-Contracts-API.md`
- `docs/product/features/tts/FEAT-003-Conversor-WAV-MP3.md`
- `docs/product/features/tts/FEAT-004-Map-Tone.md`
- `docs/product/features/tts/FEAT-005-Expand-FastAPI.md`
- `docs/product/features/tts/FEAT-006-Chatterbox-Integration.md` (FEAT-007 depends on same interface)

No blocking dependency on backend, frontend, infrastructure compose, or fallback changes.

## Agent Instruction

- Implement only the XTTS adapter path — no provider fallback logic
- Do NOT call real Coqui/XTTS container — use mocked HTTP responses in tests
- Do NOT implement audio cache behavior
- Do NOT modify `framework/infrastructure/docker-compose.yml` or `framework/infrastructure/docker-compose.prod.yml`
- Do NOT modify backend or frontend source files
- Do NOT modify `docs/contracts/api/openapi_tts.json`
- XTTS/Coqui always returns WAV — always call `convert_wav_to_mp3()`
- If `TTS_PROVIDER` is empty at startup, raise `ValueError("TTS_PROVIDER is required")` — already enforced in config
- Return only audio bytes with `audio/mpeg` content type — no version headers
- Keep XTTS native parameters (`speaker_wav`, `speed`, `language_id`) internal to the adapter
- Reuse existing `resolve_tone()` from `app.adapters.tone_mapping`
- Reuse existing `convert_wav_to_mp3()` from converter module
- Reuse existing `ProviderConfigError` from factory.py
- Error codes: `httpx.TimeoutException` → 504, `httpx.ConnectError` → 503, unsupported tone → 422, invalid response → 500
- Use per-provider timeout: `config.xtts_timeout_ms` for XTTS, `config.tts_timeout_ms` for Chatterbox
- Follow same adapter pattern as `ChatterboxAdapter` for consistency

## Notes

This sprint integrates only the XTTS adapter path. Provider fallback and provider startup orchestration are handled by later features.