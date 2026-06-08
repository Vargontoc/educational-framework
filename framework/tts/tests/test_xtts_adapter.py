import io
import wave
from unittest.mock import patch

import pytest

from app.adapters.xtts import XttsAdapter
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


@pytest.fixture(autouse=True)
def check_ffmpeg():
    import shutil
    if not shutil.which("ffmpeg"):
        pytest.skip("ffmpeg not installed on this system")


def test_wav_response_converts_to_mp3():
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = XttsAdapter()
        result = adapter.synthesize("hello", "calm", "es", "default")

        assert isinstance(result, bytes)
        assert len(result) > 0
        assert result[:3] == b"ID3"


def test_xtts_timeout_maps_to_504():
    import httpx
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_client.post.side_effect = httpx.TimeoutException("timed out")

        adapter = XttsAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "SYNTHESIS_TIMEOUT"


def test_connection_error_maps_to_503():
    import httpx
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_client.post.side_effect = httpx.ConnectError("connection refused")

        adapter = XttsAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "calm", "es", "default")
        assert exc_info.value.code == "PROVIDER_UNAVAILABLE"


def test_tone_mapping_applied_to_request():
    wav_bytes = generate_wav_bytes()
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = XttsAdapter()
        adapter.synthesize("hello", "joyful", "es", "default")

        call_args = mock_client.post.call_args
        payload = call_args.kwargs["json"]
        assert payload["speed"] == 1.15
        assert payload["language_id"] == "es"
        assert payload["speaker_wav"] == "default"


def test_unsupported_tone_raises_422():
    with patch("httpx.Client") as mock_client_class:
        adapter = XttsAdapter()
        with pytest.raises(ConversionError) as exc_info:
            adapter.synthesize("hello", "unknown_tone", "es", "default")
        assert exc_info.value.code == "UNSUPPORTED_TONE"


def test_xtts_provider_resolves_correctly(monkeypatch):
    monkeypatch.setenv("TTS_PROVIDER", "xtts")
    from app.adapters.factory import get_provider_adapter
    adapter = get_provider_adapter("xtts")
    assert isinstance(adapter, XttsAdapter)