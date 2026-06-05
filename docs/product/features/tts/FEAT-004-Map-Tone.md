# FEAT-004 - Tone Mapping Definition

## Status

state: proposal
user_history: Provider-specific mapping for semantic TTS tones
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`
owned_by: tts
scope: tts-service mapping definition only
test: Validate semantic tone mappings resolve to provider-specific parameter definitions without calling provider containers.

## Description

This feature defines the internal tone mapping configuration for `tts-educational`.

`api-educational` sends provider-agnostic semantic values such as `tone`, `emotion`, `intensity`, and `voiceProfile`. `tts-educational` translates those values into provider-specific parameters using a versioned internal mapping table.

Changing the active provider must not change the backend contract. Only the internal mapping table and future provider adapters should change.

## In Scope

- Define supported semantic tones.
- Define provider-specific mapping entries for Chatterbox.
- Define provider-specific mapping entries for XTTS v2.
- Store mappings as versioned internal configuration.
- Define how mapping version metadata will be exposed internally for later cache keys.
- Add tests for mapping resolution.

## Out Of Scope

- Chatterbox provider integration.
- Coqui/XTTS provider integration.
- Real synthesis calls.
- WAV/PCM to MP3 conversion.
- Audio cache implementation.
- Backend changes.
- Frontend changes.
- Infrastructure changes.
- Public API endpoint changes.
- New diagnostic endpoints.

## Supported Semantic Tones

- `calm`
- `joyful`
- `enthusiastic`
- `serious`
- `neutral`

## Provider Mapping

Chatterbox internal mapping may include:

- `exaggeration`
- `cfg_weight`
- `temperature`
- `audio_prompt`

XTTS v2 internal mapping may include:

- `speaker_wav`
- `speed`

These fields must not be exposed as stable public API fields in `docs/contracts/api/openapi_tts.json`.

## Versioning

The mapping must have a stable version identifier, for example:

- `toneMappingVersion`
- `synthesisProfileVersion`

This version will be used later by backend audio cache keys so generated MP3 audio can be invalidated when tone behavior changes.

## Testing

Tests must not require Chatterbox, Coqui, GPU, or Docker Compose.

Required tests:

- Each supported semantic tone resolves for Chatterbox.
- Each supported semantic tone resolves for XTTS v2.
- Unknown tone returns a controlled mapping error.
- Unknown provider returns a controlled mapping error.
- Mapping version is available.
- Provider-native fields are not exposed as public contract fields.

## Acceptance Criteria

- `tts-educational` has a versioned internal tone mapping definition.
- All supported tones resolve for Chatterbox.
- All supported tones resolve for XTTS v2.
- Backend-facing contract remains provider-agnostic.
- No provider-specific fields such as `speaker_wav`, `exaggeration`, or `cfg_weight` are added to the public request schema.
- Mapping can be changed without changing backend code.
- Mapping version can be used later for audio cache invalidation.
- No provider integration or real synthesis is implemented.

## Risks

- Provider-specific parameters leak into the backend contract.
- Tone behavior changes without cache invalidation.
- Tone names drift from the agent/backend tone enum.
- Chatterbox and XTTS produce different emotional results for the same semantic tone.

## Notes

This feature prepares the translation definition only. Real provider calls are implemented in later provider integration features.
