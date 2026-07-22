from __future__ import annotations

import httpx
import pytest

from app.chatterbox import ChatterboxClient
from app.config import Settings
from app.errors import TtsError
from app.models import SynthesizeRequest


def settings() -> Settings:
    return Settings(
        chatterbox_base_url="http://chatterbox.test",
        chatterbox_synthesis_path="/tts",
        chatterbox_timeout_seconds=1,
        chatterbox_npc_voice="npc-voice",
        chatterbox_storyteller_voice="narrative-voice",
        ffmpeg_binary="ffmpeg",
        mp3_bitrate="128k",
    )


@pytest.mark.asyncio
async def test_sends_storyteller_voice_and_tone_parameters(monkeypatch) -> None:
    observed: dict[str, object] = {}

    async def post(self, url, **kwargs):
        observed["url"] = url
        observed["payload"] = kwargs["json"]
        return httpx.Response(200, content=b"wav", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    wav = await ChatterboxClient(settings()).synthesize(
        SynthesizeRequest(text="Un cuento", voice_profile="storyteller", tone="calm")
    )

    assert wav == b"wav"
    assert observed["url"] == "http://chatterbox.test/tts"
    assert observed["payload"] == {
        "text": "Un cuento",
        "voice": "narrative-voice",
        "locale": "es",
        "emotion": None,
        "exaggeration": 0.25,
        "cfg_weight": 0.3,
        "temperature": 0.7,
    }


@pytest.mark.asyncio
async def test_timeout_is_contractual_error(monkeypatch) -> None:
    async def post(self, url, **kwargs):
        raise httpx.ReadTimeout("slow", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    with pytest.raises(TtsError) as caught:
        await ChatterboxClient(settings()).synthesize(SynthesizeRequest(text="Hola"))

    assert caught.value.status_code == 504
    assert caught.value.code == "SYNTHESIS_TIMEOUT"


@pytest.mark.asyncio
async def test_sends_npc_voice(monkeypatch) -> None:
    observed: dict[str, object] = {}

    async def post(self, url, **kwargs):
        observed["payload"] = kwargs["json"]
        return httpx.Response(200, content=b"wav", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    await ChatterboxClient(settings()).synthesize(
        SynthesizeRequest(text="Hola", voice_profile="npc", tone="calm")
    )
    assert observed["payload"]["voice"] == "npc-voice"


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "tone,exaggeration,cfg_weight,temperature",
    [
        ("calm", 0.25, 0.30, 0.70),
        ("joyful", 0.55, 0.45, 0.85),
        ("enthusiastic", 0.70, 0.50, 0.90),
        ("playful", 0.60, 0.40, 0.90),
        ("serious", 0.20, 0.55, 0.65),
    ],
)
async def test_all_five_tones_send_correct_parameters(
    monkeypatch, tone, exaggeration, cfg_weight, temperature
) -> None:
    observed: dict[str, object] = {}

    async def post(self, url, **kwargs):
        observed["payload"] = kwargs["json"]
        return httpx.Response(200, content=b"wav", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    await ChatterboxClient(settings()).synthesize(
        SynthesizeRequest(text="Test", tone=tone)
    )
    payload = observed["payload"]
    assert payload["exaggeration"] == exaggeration
    assert payload["cfg_weight"] == cfg_weight
    assert payload["temperature"] == temperature


@pytest.mark.asyncio
async def test_connection_error_is_provider_unavailable(monkeypatch) -> None:
    async def post(self, url, **kwargs):
        raise httpx.ConnectError("refused", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    with pytest.raises(TtsError) as caught:
        await ChatterboxClient(settings()).synthesize(SynthesizeRequest(text="Hola"))

    assert caught.value.status_code == 503
    assert caught.value.code == "PROVIDER_UNAVAILABLE"
    assert caught.value.retryable is True


@pytest.mark.asyncio
async def test_server_error_5xx_is_provider_unavailable(monkeypatch) -> None:
    async def post(self, url, **kwargs):
        return httpx.Response(500, content=b"error", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    with pytest.raises(TtsError) as caught:
        await ChatterboxClient(settings()).synthesize(SynthesizeRequest(text="Hola"))

    assert caught.value.status_code == 503
    assert caught.value.code == "PROVIDER_UNAVAILABLE"
    assert caught.value.retryable is True


@pytest.mark.asyncio
async def test_client_error_4xx_is_provider_validation_error(monkeypatch) -> None:
    async def post(self, url, **kwargs):
        return httpx.Response(400, content=b"bad", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    with pytest.raises(TtsError) as caught:
        await ChatterboxClient(settings()).synthesize(SynthesizeRequest(text="Hola"))

    assert caught.value.status_code == 422
    assert caught.value.code == "PROVIDER_VALIDATION_ERROR"
    assert caught.value.retryable is False


@pytest.mark.asyncio
async def test_empty_response_is_provider_error(monkeypatch) -> None:
    async def post(self, url, **kwargs):
        return httpx.Response(200, content=b"", request=httpx.Request("POST", url))

    monkeypatch.setattr(httpx.AsyncClient, "post", post)
    with pytest.raises(TtsError) as caught:
        await ChatterboxClient(settings()).synthesize(SynthesizeRequest(text="Hola"))

    assert caught.value.status_code == 500
    assert caught.value.code == "PROVIDER_ERROR"
    assert caught.value.retryable is True
