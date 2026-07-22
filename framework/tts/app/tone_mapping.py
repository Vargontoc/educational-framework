from __future__ import annotations

# Valores semánticos propios del contrato, traducidos a controles de Chatterbox.
TONE_MAPPING: dict[str, dict[str, float]] = {
    "calm": {"exaggeration": 0.25, "cfg_weight": 0.30, "temperature": 0.70},
    "joyful": {"exaggeration": 0.55, "cfg_weight": 0.45, "temperature": 0.85},
    "enthusiastic": {"exaggeration": 0.70, "cfg_weight": 0.50, "temperature": 0.90},
    "playful": {"exaggeration": 0.60, "cfg_weight": 0.40, "temperature": 0.90},
    "serious": {"exaggeration": 0.20, "cfg_weight": 0.55, "temperature": 0.65},
}


def parameters_for_tone(tone: str, intensity: float | None) -> dict[str, float]:
    parameters = TONE_MAPPING[tone].copy()
    if intensity is not None:
        parameters["exaggeration"] = round(parameters["exaggeration"] * intensity, 2)
    return parameters
