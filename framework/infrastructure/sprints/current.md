# Sprint 003 - infrastructure
# -----------------------------------------------

## Goal
Migrate the Coqui TTS service from `tts_models/es/css10/vits` to `tts_models/multilingual/multi-dataset/xtts_v2` per FEAT-001, and update compose configuration and env templates accordingly.

## Status
status: active
started_at: 2026-04-27 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Update `coqui-educational` command in `docker-compose.yml`: replace `--model_name tts_models/es/css10/vits` with `--model_name tts_models/multilingual/multi-dataset/xtts_v2` and add `--language_idx es`.
- [ ] Update `healthcheck.start_period` in `docker-compose.yml` from 120s to 300s to account for XTTS v2 model download (~1.8 GB on first start).
- [ ] Update `envs/coqui.env.example`: change default value for `COQUI_MODEL_NAME` and add `COQUI_LANGUAGE_IDX=es`.
- [ ] Update `envs/coqui.env` locally (gitignored) to match new defaults.
- [ ] Validate both compose stacks with `docker compose config`.

## Risks
- XTTS v2 model weighs ~1.8 GB; first container startup requires a full download — the healthcheck must allow enough time or it will report unhealthy before the model is ready.
- The API endpoint and request schema may differ between VITS and XTTS v2 (VITS uses `/api/tts`, XTTS v2 may use `/v1/audio/speech`). Infrastructure does not own this contract, but the change must be flagged to the backend layer.
- Voice cloning requires a reference audio file passed as a parameter; without one, XTTS v2 uses a default voice that may not match the character identity.
- Audio cache built against the VITS model is incompatible with XTTS v2 output — the cache volume must be cleared before first use.

## Dependencies
- Backend layer must update its TTS client to use the XTTS v2 endpoint (`/v1/audio/speech`) and add the `language` parameter — infrastructure change unblocks this but cannot complete it.
- Backend layer must invalidate and rebuild the audio cache after the model change.
- No blocking dependency from other layers for the compose changes themselves.

## Agent Instruction
- Only modify `docker-compose.yml`, `docker-compose.prod.yml` (if needed), and `envs/coqui.env.example`.
- Do not change the service name, network, volume name, or runtime — those are fixed by Sprint 002.
- The service hostname referenced in FEAT-001 as `educational-coqui` is a typo; the correct hostname is `coqui-educational`.
- FEAT-001 acceptance criteria mention the healthcheck endpoint as `http://educational-coqui:5002/health` — validate whether `/health` is a valid endpoint for XTTS v2 before using it; fall back to `curl -sf http://localhost:5002/` if not.
- After changes, run `docker compose config` and `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` to confirm both stacks are valid.
- Never commit real `.env` files.

## Notes
Sprint triggered by FEAT-001 (docs/product/features/tts/FEAT-001-XTTS-Model.md).
FEAT-001 is owned by `agents` and `infrastructure`; this sprint covers the infrastructure scope only.
The agents layer has no changes required per FEAT-001 (agent output contract is unchanged).
XTTS v2 supports voice cloning with a 6-second reference audio — this is out of scope for this sprint; tracked as FEAT-008 if default voice is insufficient.
The `coqui_models` volume already persists across restarts from Sprint 002; the XTTS v2 model will be downloaded into the same volume on first start.

## Acceptance Criteria
- `docker-compose.yml` command for `coqui-educational` references `tts_models/multilingual/multi-dataset/xtts_v2` and includes `--language_idx es`.
- `envs/coqui.env.example` reflects the new model name and includes `COQUI_LANGUAGE_IDX`.
- `docker compose config` and `docker compose -f docker-compose.yml -f docker-compose.prod.yml config` both exit 0.
- Container starts and healthcheck eventually reports healthy after model download on a fresh volume.

## Review

completed_tasks:
    {}

incomplete_tasks:
    {}

contract_changes:
    {}

learnings:
    {}

next_sprint_suggestions:
    {}
