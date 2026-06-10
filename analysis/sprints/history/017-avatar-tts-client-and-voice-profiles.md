# Sprint 017 - backend
# -----------------------------------------------

## Goal
Integrate the backend avatar module with `tts-educational` using the repository TTS contract, MP3 responses, and stable voice profiles.

## Status
status: closed
started_at: 2026-06-10
closed_at: 2026-06-10
blocked_by:
waiting_for:

## Tasks

### TTS Client
- [ ] Add a backend TTS client that calls `POST /api/v1/tts/synthesize` on `tts-educational`.
- [ ] Configure TTS base URL, request timeout, and optional enablement through backend properties.
- [ ] Send request fields compatible with `docs/contracts/api/openapi_tts.json`: `text`, `locale`, `tone`, and `voice_profile`.
- [ ] Accept only successful `audio/mpeg` responses as generated MP3 audio.
- [ ] Reject unexpected content types and fall back to text-only avatar metadata.
- [ ] Do not call Coqui/XTTS or Chatterbox provider services directly.
- [ ] Do not add backend WAV-to-MP3 conversion.

### Voice Profiles
- [ ] Use `voice_profile: "npc"` for short avatar/NPC game feedback.
- [ ] Use `voice_profile: "storyteller"` for narration and reading flows.
- [ ] Keep provider-specific interpretation of each voice profile inside `tts-educational`.
- [ ] Ensure voice profile is part of the internal avatar audio request model for later cache keys.

### Tone Mapping
- [ ] Map backend `CALM` to TTS `calm`.
- [ ] Map backend `JOYFUL` to TTS `joyful`.
- [ ] Map backend `ENTHUSIASTIC` to TTS `enthusiastic`.
- [ ] Map backend `SERIOUS` to TTS `serious`.
- [ ] Map backend `NEUTRAL` to TTS `calm` for v1 unless TTS adds explicit `neutral` support.

### Failure Handling
- [ ] On TTS timeout, return text-only avatar metadata with `audioAvailable: false`.
- [ ] On TTS 4xx/5xx, return text-only avatar metadata with `audioAvailable: false`.
- [ ] On connection failure, return text-only avatar metadata with `audioAvailable: false`.
- [ ] Ensure child-facing flows never block indefinitely waiting for TTS.

### Tests
- [ ] Unit test request serialization for `npc` voice profile.
- [ ] Unit test request serialization for `storyteller` voice profile.
- [ ] Unit test all backend-to-TTS tone mappings, including `NEUTRAL -> calm`.
- [ ] Unit test `audio/mpeg` response acceptance.
- [ ] Unit test invalid content type fallback.
- [ ] Unit test timeout fallback.
- [ ] Unit test TTS error fallback.
- [ ] Add integration-style client tests with a mocked HTTP server where practical.

## Risks
- Runtime TTS OpenAPI can drift from `docs/contracts/api/openapi_tts.json`.
- TTS latency can make avatar interaction feel delayed if timeouts are too high.
- Voice profile semantics can drift between backend and TTS service.

## Dependencies
- Sprint 016 avatar module foundation.
- `docs/contracts/api/openapi_tts.json`.
- `tts-educational` endpoint available in target environments.

## Agent Instruction
- Treat `docs/contracts/api/openapi_tts.json` as the implementation contract.
- Keep all provider-specific fields out of backend request models.
- Keep failure behavior graceful and text-first.
- Do not introduce cache in this sprint.
- Do not introduce WebSocket binary delivery in this sprint.

## Notes
The live service on port `8081` has been observed returning `audio/mpeg`, but its generated OpenAPI may need alignment with the repository contract.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
