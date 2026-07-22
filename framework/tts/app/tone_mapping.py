from __future__ import annotations

from app.errors import TtsError

TONE_MAPPING: dict[str, dict[str, float]] = {
    "calm": {"exaggeration": 0.25, "cfg_weight": 0.30, "temperature": 0.70},
    "joyful": {"exaggeration": 0.55, "cfg_weight": 0.45, "temperature": 0.85},
    "enthusiastic": {"exaggeration": 0.70, "cfg_weight": 0.50, "temperature": 0.90},
    "playful": {"exaggeration": 0.60, "cfg_weight": 0.40, "temperature": 0.90},
    "serious": {"exaggeration": 0.20, "cfg_weight": 0.55, "temperature": 0.65},
    "tender": {"exaggeration": 0.35, "cfg_weight": 0.30, "temperature": 0.75},
    "mysterious": {"exaggeration": 0.45, "cfg_weight": 0.40, "temperature": 0.80},
}

CONTEXT_TONES: dict[str, set[str]] = {
    "npc": {"calm", "joyful", "enthusiastic", "playful", "serious"},
    "narration": {"calm", "joyful", "enthusiastic", "tender", "mysterious"},
}


def validate_tone_for_context(tone: str, context: str) -> None:
    if tone not in CONTEXT_TONES.get(context, set()):
        raise TtsError(
            status_code=422,
            code="TONE_CONTEXT_MISMATCH",
            message=f"Tone '{tone}' not allowed for context '{context}'",
            retryable=False,
        )


def parameters_for_tone(tone: str, intensity: float | None) -> dict[str, float]:
    parameters = TONE_MAPPING[tone].copy()
    if intensity is not None:
        parameters["exaggeration"] = round(parameters["exaggeration"] * intensity, 2)
    return parameters
