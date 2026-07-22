from __future__ import annotations

import wave
from io import BytesIO

import pytest

from app.audio import wav_to_mp3
from app.config import Settings


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


def wav_silence() -> bytes:
    output = BytesIO()
    with wave.open(output, "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(8000)
        wav.writeframes(b"\x00\x00" * 800)
    return output.getvalue()


@pytest.mark.asyncio
async def test_converts_wav_to_mp3_when_ffmpeg_available() -> None:
    mp3 = await wav_to_mp3(wav_silence(), settings())

    assert mp3[:3] == b"ID3" or mp3[:2] == b"\xff\xfb"
