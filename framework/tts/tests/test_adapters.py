from unittest.mock import patch

import pytest
from app.adapters.factory import (
    ProviderConfigError,
    get_provider_adapter,
    SUPPORTED_PROVIDERS,
)


def test_chatterbox_adapter_resolved():
    import shutil
    if not shutil.which("ffmpeg"):
        pytest.skip("ffmpeg not installed on this system")

    adapter = get_provider_adapter("chatterbox")
    assert adapter is not None
    assert hasattr(adapter, "synthesize")


def test_xtts_adapter_resolved():
    import shutil
    if not shutil.which("ffmpeg"):
        pytest.skip("ffmpeg not installed on this system")

    wav_bytes = b"RIFF" + b"\x00" * 40 + b"WAVE"
    with patch("httpx.Client") as mock_client_class:
        mock_client = mock_client_class.return_value.__enter__.return_value
        mock_response = mock_client.post.return_value
        mock_response.status_code = 200
        mock_response.content = wav_bytes

        adapter = get_provider_adapter("xtts")
        assert adapter is not None
        result = adapter.synthesize("text", "calm", "es", "default")
        assert isinstance(result, bytes)


def test_unknown_provider_raises():
    with pytest.raises(ProviderConfigError) as exc_info:
        get_provider_adapter("unknown_provider")
    assert exc_info.value.code == "UNKNOWN_PROVIDER"
    assert exc_info.value.retryable is False


def test_provider_factory_returns_adapter_interface():
    adapter = get_provider_adapter("chatterbox")
    assert hasattr(adapter, "synthesize")
    adapter = get_provider_adapter("xtts")
    assert hasattr(adapter, "synthesize")


def test_supported_providers_list():
    assert "chatterbox" in SUPPORTED_PROVIDERS
    assert "xtts" in SUPPORTED_PROVIDERS
    assert len(SUPPORTED_PROVIDERS) == 2
