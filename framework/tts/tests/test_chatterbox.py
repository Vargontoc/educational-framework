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
