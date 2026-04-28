# Sprint 008 - agents
# -----------------------------------------------

## Goal
Create the eSpeak NG reference WAV generation infrastructure per ADR-005: directory structure, versioned generation script, and the 5 character voice WAV files committed to the repository.

## Status
status: completed
started_at: 2026-04-28 00:00:00
closed_at: 2026-04-28 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Create directory `framework/agents/tts/references/` and `framework/agents/tts/scripts/`.
- [x] Write `framework/agents/tts/scripts/generate_references.sh` with the exact eSpeak NG parameters from ADR-005 for all 5 tones.
- [x] Run `generate_references.sh` to produce `calm.wav`, `joyful.wav`, `enthusiastic.wav`, `playful.wav`, `serious.wav` in `framework/agents/tts/references/`.
- [x] Verify each WAV is non-empty and playable.
- [x] Commit WAV files and script to git.

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
    - Created framework/agents/tts/references/ and framework/agents/tts/scripts/.
    - Wrote generate_references.sh with ADR-005 eSpeak NG parameters for all 5 tones.
    - Extended reference text vs ADR-005 draft to ensure all tones produce ≥6s WAVs (XTTS v2 minimum). Original text produced ~5.2s at 175wpm (enthusiastic); extended text produces 8.3s-11.9s across all tones.
    - Generated and committed 5 WAV files: calm(~11.3s), joyful(~9.1s), enthusiastic(~8.3s), playful(~8.9s), serious(~11.9s).
    - eSpeak NG 1.52.0 installed via winget; not in system PATH — script auto-detects Windows install path as fallback.

incomplete_tasks:
    none

contract_changes:
    none — WAV files are an internal asset consumed by infrastructure/backend. No agent output contract changed.

learnings:
    - ADR-005 reference text produced <6s for faster tones. Extended text required to meet XTTS v2 minimum. ADR-005 text should be updated to reflect the actual text used.
    - eSpeak NG outputs 22050 Hz 16-bit mono WAV. Duration formula: (file_bytes - 44_header) / 44100 bytes_per_sec.
    - Script auto-detects eSpeak NG: prefers PATH, falls back to Windows install at /c/Program Files/eSpeak NG/espeak-ng.exe.
    - Parameters in ADR-005 are v1 starting points — require validation with child user before being fixed as definitive.

next_sprint_suggestions:
    - Infrastructure Sprint 004: add bind-mount ../agents/tts/references:/references:ro to coqui-educational (now unblocked).
    - Backend: pass speaker_wav=/references/{tone}.wav in XTTS v2 synthesis requests.
    - Future: validate voice character with end user (child) and iterate parameters in generate_references.sh if needed.
