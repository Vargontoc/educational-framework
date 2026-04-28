# Sprint 008 - agents
# -----------------------------------------------

## Goal
Create the eSpeak NG reference WAV generation infrastructure per ADR-005: directory structure, versioned generation script, and the 5 character voice WAV files committed to the repository.

## Status
status: active
started_at: 2026-04-28 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Create directory `framework/agents/tts/references/` and `framework/agents/tts/scripts/`.
- [ ] Write `framework/agents/tts/scripts/generate_references.sh` with the exact eSpeak NG parameters from ADR-005 for all 5 tones.
- [ ] Run `generate_references.sh` to produce `calm.wav`, `joyful.wav`, `enthusiastic.wav`, `playful.wav`, `serious.wav` in `framework/agents/tts/references/`.
- [ ] Verify each WAV is non-empty and playable.
- [ ] Commit WAV files and script to git.

## Risks
- eSpeak NG must be installed on the developer's machine (`espeak-ng` command available). If not installed, WAV generation is blocked.
- WAV files must be at least 6 seconds long for XTTS v2 voice cloning to work correctly. The reference text in ADR-005 should produce ~7-8 seconds at the defined speeds.
- The parameters in ADR-005 are a starting point and require manual validation with the end user (the child) before being fixed as definitive. This sprint commits them as v1 — they will be iterable.

## Dependencies
- eSpeak NG must be installed locally: `winget install eSpeak-NG` (Windows) or `apt install espeak-ng` (Linux/WSL).
- No blocking dependency on other layers for creating the script and WAVs.
- Infrastructure layer Sprint 004 depends on this sprint delivering `framework/agents/tts/references/` with the WAV files.

## Agent Instruction
- Create `generate_references.sh` using exactly the parameters defined in ADR-005. Do not modify pitch, speed, or amplitude values — those are the ADR-defined starting point.
- Reference text for all WAVs (as defined in ADR-005): `"Hola, soy Nubi, tu amigo explorador. ¿Sabes que hay cosas increíbles que descubrir?"`
- Script must be idempotent: re-running it overwrites the existing WAVs with no side effects.
- Add a `.gitkeep` to `references/` only if WAVs cannot be generated locally — prefer committing the actual WAV files.
- WAV files are small (~200 KB each) and deterministic — safe to commit per ADR-005.

## Notes
Sprint triggered by ADR-005 (docs/architecture/decisions/ADR-005-Voice-Reference.md).
ADR-005 status is `proposed` — confirm acceptance with human before merging this sprint branch.
`generate_references.sh` is the single source of truth for voice parameters. Any future voice change goes through this script first, then WAVs are regenerated and committed.
The tones map to the agent's `tone` output field: calm → ages 3-4, joyful → ages 5-6, enthusiastic → ages 7-8, playful → muletillas (FEAT-005), serious → safety override (FEAT-003).

## Acceptance Criteria
- `framework/agents/tts/scripts/generate_references.sh` exists and is executable.
- Running the script produces 5 WAV files in `framework/agents/tts/references/`: calm.wav, joyful.wav, enthusiastic.wav, playful.wav, serious.wav.
- Each WAV file is non-empty and ≥ 6 seconds duration.
- All 5 WAVs and the script are committed to git.
- Re-running the script produces identical output (idempotent).

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
