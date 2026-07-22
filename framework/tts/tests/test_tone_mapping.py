from __future__ import annotations

import pytest

from app.errors import TtsError
from app.tone_mapping import CONTEXT_TONES, TONE_MAPPING, parameters_for_tone, validate_tone_for_context


def test_all_seven_tones_are_mapped() -> None:
    assert set(TONE_MAPPING) == {"calm", "joyful", "enthusiastic", "playful", "serious", "tender", "mysterious"}


def test_calm_tone_parameters() -> None:
    p = TONE_MAPPING["calm"]
    assert p["exaggeration"] == 0.25
    assert p["cfg_weight"] == 0.30
    assert p["temperature"] == 0.70


def test_joyful_tone_parameters() -> None:
    p = TONE_MAPPING["joyful"]
    assert p["exaggeration"] == 0.55
    assert p["cfg_weight"] == 0.45
    assert p["temperature"] == 0.85


def test_enthusiastic_tone_parameters() -> None:
    p = TONE_MAPPING["enthusiastic"]
    assert p["exaggeration"] == 0.70
    assert p["cfg_weight"] == 0.50
    assert p["temperature"] == 0.90


def test_playful_tone_parameters() -> None:
    p = TONE_MAPPING["playful"]
    assert p["exaggeration"] == 0.60
    assert p["cfg_weight"] == 0.40
    assert p["temperature"] == 0.90


def test_serious_tone_parameters() -> None:
    p = TONE_MAPPING["serious"]
    assert p["exaggeration"] == 0.20
    assert p["cfg_weight"] == 0.55
    assert p["temperature"] == 0.65


def test_intensity_modifies_exaggeration() -> None:
    p = parameters_for_tone("calm", 0.5)
    assert p["exaggeration"] == round(0.25 * 0.5, 2)


def test_intensity_none_does_not_modify_exaggeration() -> None:
    p = parameters_for_tone("calm", None)
    assert p["exaggeration"] == 0.25


def test_unknown_tone_raises_key_error() -> None:
    with pytest.raises(KeyError):
        parameters_for_tone("angry", None)


def test_tender_tone_parameters() -> None:
    p = TONE_MAPPING["tender"]
    assert p["exaggeration"] == 0.35
    assert p["cfg_weight"] == 0.30
    assert p["temperature"] == 0.75


def test_mysterious_tone_parameters() -> None:
    p = TONE_MAPPING["mysterious"]
    assert p["exaggeration"] == 0.45
    assert p["cfg_weight"] == 0.40
    assert p["temperature"] == 0.80


def test_context_tones_npc() -> None:
    assert CONTEXT_TONES["npc"] == {"calm", "joyful", "enthusiastic", "playful", "serious"}


def test_context_tones_narration() -> None:
    assert CONTEXT_TONES["narration"] == {"calm", "joyful", "enthusiastic", "tender", "mysterious"}


def test_validate_tone_for_context_accepts_valid_combination() -> None:
    validate_tone_for_context("playful", "npc")
    validate_tone_for_context("tender", "narration")
    validate_tone_for_context("calm", "npc")


def test_validate_tone_for_context_rejects_tender_in_npc() -> None:
    with pytest.raises(TtsError) as caught:
        validate_tone_for_context("tender", "npc")
    assert caught.value.status_code == 422
    assert caught.value.code == "TONE_CONTEXT_MISMATCH"
    assert caught.value.retryable is False


def test_validate_tone_for_context_rejects_playful_in_narration() -> None:
    with pytest.raises(TtsError) as caught:
        validate_tone_for_context("playful", "narration")
    assert caught.value.status_code == 422
    assert caught.value.code == "TONE_CONTEXT_MISMATCH"
    assert caught.value.retryable is False
