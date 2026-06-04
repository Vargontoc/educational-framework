# Name

TTS Design Skill

## When to Use

- Before adding real provider adapters.
- Before changing the `tts-educational` public API.
- Before adding MP3 conversion or cache behavior.
- Before updating `docs/contracts/api/openapi_tts.json`.

## Design Output Format

Produce a written proposal in `framework/tts/sprints/current.md` Notes with:

- What TTS capability is being designed.
- Proposed API shape.
- Provider impact.
- Configuration impact.
- Contract impact on `docs/contracts/api/openapi_tts.json`.
- Risks and open questions.

## Decision Rules

- Keep `tts-educational` provider-agnostic at its public API boundary.
- Hide Chatterbox and Coqui native parameters behind provider adapters.
- Keep MP3 as the backend-compatible output format unless a later ADR changes it.
- Do not create direct dependencies from frontend or agents to TTS providers.
- Document contract changes before implementation.
