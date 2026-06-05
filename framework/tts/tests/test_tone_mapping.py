import pytest
from app.mappings.tone_mapping import (
    ToneMappingError,
    resolve_tone,
    get_mapping_version,
    get_supported_tones,
    get_supported_providers,
    SEMANTIC_TONES,
)


def test_resolve_tone_chatterbox_all():
    for tone in SEMANTIC_TONES:
        result = resolve_tone(tone, "chatterbox")
        assert isinstance(result, dict)
        assert len(result) > 0
        assert "exaggeration" in result
        assert "cfg_weight" in result
        assert "temperature" in result


def test_resolve_tone_xtts_all():
    for tone in SEMANTIC_TONES:
        result = resolve_tone(tone, "xtts")
        assert isinstance(result, dict)
        assert len(result) > 0
        assert "speed" in result


def test_resolve_tone_unknown_tone():
    with pytest.raises(ToneMappingError) as exc_info:
        resolve_tone("unknown_tone", "chatterbox")
    assert exc_info.value.code == "UNKNOWN_TONE"
    assert exc_info.value.message != ""
    assert exc_info.value.retryable is False


def test_resolve_tone_unknown_provider():
    with pytest.raises(ToneMappingError) as exc_info:
        resolve_tone("calm", "unknown_provider")
    assert exc_info.value.code == "UNKNOWN_PROVIDER"
    assert exc_info.value.message != ""
    assert exc_info.value.retryable is False


def test_mapping_version_available():
    assert get_mapping_version() == "1.0"


def test_supported_tones():
    assert get_supported_tones() == ["calm", "joyful", "enthusiastic", "playful", "serious"]


def test_supported_providers():
    assert get_supported_providers() == ["chatterbox", "xtts"]
