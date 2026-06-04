## Layer Context

layer: tts
service_name: tts-educational
stack: Python API service
purpose: Provide the internal backend-to-TTS API shell and later own provider selection, synthesis orchestration, and audio format normalization
pattern: Small internal service with explicit API boundary and provider adapters when provider integration is introduced
does_not_own: Backend business logic, frontend playback, Docker Compose orchestration, Chatterbox provider implementation, Coqui/XTTS provider implementation

## Service Structure

root: framework/tts
app/                    -> Python API application source
tests/                  -> unit and API tests
Dockerfile              -> container image definition for tts-educational
requirements.txt        -> Python runtime dependencies
README.md               -> local build and run notes when needed

## Current Scope

The initial scope is FEAT-001: TTS API Shell.

In scope:
- Minimal Python API shell.
- Healthcheck endpoint.
- Placeholder synthesis endpoint.
- Service-level configuration loading for future provider selection.
- Dockerfile for the tts-educational image.

Out of scope until later features:
- Chatterbox integration.
- Coqui/XTTS integration.
- Provider fallback logic.
- WAV/PCM to MP3 conversion.
- Audio cache.
- Backend integration.
- Frontend integration.
- Docker Compose changes.

## Contract

output: docs/contracts/api/openapi_tts.json
rule: This contract describes tts-educational, not the native Chatterbox or Coqui APIs
rule: Do not expose provider-specific parameters such as speaker_wav in the stable API unless a later ADR explicitly allows it
rule: After any real TTS endpoint contract change, update docs/contracts/api/openapi_tts.json
rule: FEAT-001 may create only a placeholder endpoint; full contract repopulation is deferred to a later feature

## Configuration

reserved_environment:
- TTS_PROVIDER
- TTS_ENABLE_FALLBACK
- TTS_FALLBACK_PROVIDER
- CHATTERBOX_BASE_URL
- COQUI_BASE_URL
- TTS_OUTPUT_FORMAT
- TTS_TIMEOUT_MS

rule: FEAT-001 may load these variables but must not require provider containers to be running
rule: Never hardcode values that belong in environment files or service configuration

## Testing Strategy

unit: Python test framework selected by implementation, preferably pytest
api: Test healthcheck and placeholder synthesis behavior without external provider containers
container: Docker image must build successfully from Dockerfile
rule: FEAT-001 tests must not call Chatterbox or Coqui

## Skills Available

coding: framework/tts/skills/coding/SKILL.md
testing: framework/tts/skills/testing/SKILL.md
refactor: framework/tts/skills/refactor/SKILL.md
design: framework/tts/skills/design/SKILL.md

## Sprint Context

current_sprint: framework/tts/sprints/current.md

## Workflow

1. Read root agent.md for global context
2. Read this file for TTS layer context
3. Check framework/tts/sprints/current.md - if blocked, stop and report
4. Identify task type (coding, testing, refactor, design)
5. Load the matching skill from framework/tts/skills/
6. Follow the Agent Instructions in current.md for this sprint
7. Keep provider integrations out of FEAT-001 unless the sprint explicitly changes
8. After endpoint contract changes, update docs/contracts/api/openapi_tts.json only when the sprint requires it
9. Commit following: tts/type/short-description

## Agent Compatibility

This file is written in plain structured natural language.
It is compatible with: Claude Code, Gemini, ChatGPT, and local models
