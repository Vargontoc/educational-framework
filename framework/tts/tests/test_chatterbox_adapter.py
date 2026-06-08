import io
import wave
from unittest.mock import patch

import pytest

from app.adapters.chatterbox import ChatterboxAdapter
from app.converter import ConversionError


def generate_wav_bytes(duration: float = 0.1, sample_rate: int = 16000) -> bytes:
    num_frames = int(sample_rate * duration)
    buf = io.BytesIO()
    with wave.open(buf, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(bytes(num_frames * 2))
    return buf.getvalue()


def test_wav_response_converts_to_mp3():
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class, patch(
        "app.adapters.chatterbox.convert_wav_to_mp3", return_value=b"mp3-bytes"
    ) as mock_convert:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = ChatterboxAdapter()
        result = adapter.synthesize("hello", "calm", "es", "npc")

        assert result == b"mp3-bytes"
        mock_convert.assert_called_once_with(wav_bytes)


def test_chatterbox_timeout_maps_to_504():
    import httpx
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_client.post.side_effect = httpx.TimeoutException("timed out")

        adapter = ChatterboxAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "SYNTHESIS_TIMEOUT"


def test_connection_error_maps_to_503():
    import httpx
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_client.post.side_effect = httpx.ConnectError("connection refused")

        adapter = ChatterboxAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "PROVIDER_UNAVAILABLE"


def test_tone_mapping_applied_to_request():
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class, patch(
        "app.adapters.chatterbox.convert_wav_to_mp3", return_value=b"mp3-bytes"
    ):
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = ChatterboxAdapter()
        adapter.synthesize("hello", "joyful", "es", "npc")

        call_args = mock_client.post.call_args
        assert call_args.args[0].endswith("/v1/audio/speech")
        payload = call_args.kwargs["json"]
        assert payload["input"] == "hello"
        assert payload["voice"] == "nubi-npc-voice"
        assert payload["response_format"] == "wav"
        assert payload["speed"] == 1.0
        assert payload["exaggeration"] == 0.8
        assert payload["cfg_weight"] == 0.5
        assert payload["temperature"] == 0.7
        assert "text" not in payload
        assert "language" not in payload


def test_voice_mapping_npc(monkeypatch):
    monkeypatch.setenv("CHATTERBOX_VOICE_NPC", "test-npc-voice")
    monkeypatch.setenv("CHATTERBOX_VOICE_STORYTELLER", "test-storyteller-voice")
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class, patch(
        "app.adapters.chatterbox.convert_wav_to_mp3", return_value=b"mp3-bytes"
    ):
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = ChatterboxAdapter()
        adapter.synthesize("hello", "calm", "es", "npc")

        payload = mock_client.post.call_args.kwargs["json"]
        assert payload["voice"] == "test-npc-voice"


def test_voice_mapping_storyteller(monkeypatch):
    monkeypatch.setenv("CHATTERBOX_VOICE_NPC", "test-npc-voice")
    monkeypatch.setenv("CHATTERBOX_VOICE_STORYTELLER", "test-storyteller-voice")
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class, patch(
        "app.adapters.chatterbox.convert_wav_to_mp3", return_value=b"mp3-bytes"
    ):
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = ChatterboxAdapter()
        adapter.synthesize("hello", "calm", "es", "storyteller")

        payload = mock_client.post.call_args.kwargs["json"]
        assert payload["voice"] == "test-storyteller-voice"


def test_voice_mapping_default_is_npc(monkeypatch):
    monkeypatch.setenv("CHATTERBOX_VOICE_NPC", "test-npc-voice")
    monkeypatch.setenv("CHATTERBOX_VOICE_STORYTELLER", "test-storyteller-voice")
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class, patch(
        "app.adapters.chatterbox.convert_wav_to_mp3", return_value=b"mp3-bytes"
    ):
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = ChatterboxAdapter()
        adapter.synthesize("hello", "calm", "es", "default")

        payload = mock_client.post.call_args.kwargs["json"]
        assert payload["voice"] == "test-npc-voice"


def test_chatterbox_validation_error_maps_to_422():
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 422
        mock_response.text = "validation error"

        adapter = ChatterboxAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "PROVIDER_VALIDATION_ERROR"


def test_non_wav_response_is_controlled_error():
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = b"not-wav"

        adapter = ChatterboxAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "PROVIDER_ERROR"


def test_empty_tts_provider_raises_at_startup(monkeypatch):
    monkeypatch.setenv("TTS_PROVIDER", "")
    with pytest.raises(ValueError, match="TTS_PROVIDER is required"):
        from app.config import get_config
        get_config()
