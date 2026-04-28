#!/usr/bin/env bash
# generate_references.sh
# Source of truth for eSpeak NG voice parameters (ADR-005).
# Re-running this script overwrites existing WAVs — output is deterministic.
#
# Requirements: espeak-ng must be in PATH.
#   Linux/WSL:  apt install espeak-ng
#   Windows:    winget install eSpeak-NG.eSpeak-NG
#               then add "C:\Program Files\eSpeak NG" to PATH, or run from Git Bash
#               using the alias below:
#                 alias espeak-ng="/c/Program Files/eSpeak NG/espeak-ng.exe"
#
# Usage: bash framework/agents/tts/scripts/generate_references.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REFERENCES_DIR="$SCRIPT_DIR/../references"

# Reference text — phonetically diverse, representative of app vocabulary.
# Must be identical for all tones to allow fair voice comparison.
# Length chosen so that even the fastest tone (enthusiastic, 175 wpm) produces ≥6s,
# which is the minimum XTTS v2 requires for reliable voice cloning.
TEXT="Hola, soy Nubi, tu amigo explorador. ¿Sabes que hay cosas increíbles que descubrir? Me encanta aprender cosas nuevas contigo cada día."

# Resolve espeak-ng binary (fallback to Windows install path when not in PATH)
if command -v espeak-ng &>/dev/null; then
    ESPEAK="espeak-ng"
elif [ -f "/c/Program Files/eSpeak NG/espeak-ng.exe" ]; then
    ESPEAK="/c/Program Files/eSpeak NG/espeak-ng.exe"
else
    echo "ERROR: espeak-ng not found. Install it and ensure it is in PATH." >&2
    exit 1
fi

echo "Using: $("$ESPEAK" --version 2>&1 | head -1)"
echo "Output directory: $REFERENCES_DIR"
mkdir -p "$REFERENCES_DIR"

# --- Tone definitions (ADR-005) ---
# Parameters: -v language, -p pitch (0-99), -s speed (wpm), -a amplitude (0-200)

echo "Generating calm.wav    — ages 3-4: slow, neutral pitch, long pauses"
"$ESPEAK" -v es -p 55 -s 130 -a 100 "$TEXT" -w "$REFERENCES_DIR/calm.wav"

echo "Generating joyful.wav  — ages 5-6: fluid rhythm, slightly high pitch"
"$ESPEAK" -v es -p 70 -s 160 -a 100 "$TEXT" -w "$REFERENCES_DIR/joyful.wav"

echo "Generating enthusiastic.wav — ages 7-8: fast, high pitch, more energy"
"$ESPEAK" -v es -p 75 -s 175 -a 120 "$TEXT" -w "$REFERENCES_DIR/enthusiastic.wav"

echo "Generating playful.wav — muletillas (FEAT-005): dynamic, variable pitch"
"$ESPEAK" -v es -p 68 -s 165 -a 110 "$TEXT" -w "$REFERENCES_DIR/playful.wav"

echo "Generating serious.wav — safety override (FEAT-003): slow, low pitch, flat"
"$ESPEAK" -v es -p 45 -s 125 -a 100 "$TEXT" -w "$REFERENCES_DIR/serious.wav"

echo ""
echo "Done. Generated files:"
ls -lh "$REFERENCES_DIR"/*.wav
