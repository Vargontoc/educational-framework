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


@pytest.mark.asyncio
async def test_ffmpeg_not_found_raises_conversion_error(monkeypatch) -> None:
    s = Settings(
        chatterbox_base_url="http://chatterbox.test",
        chatterbox_synthesis_path="/tts",
        chatterbox_timeout_seconds=1,
        chatterbox_npc_voice="npc-voice",
        chatterbox_storyteller_voice="narrative-voice",
        ffmpeg_binary="nonexistent_ffmpeg",
        mp3_bitrate="128k",
    )
    from app.errors import TtsError

    with pytest.raises(TtsError) as caught:
        await wav_to_mp3(wav_silence(), s)

    assert caught.value.status_code == 500
    assert caught.value.code == "CONVERSION_ERROR"
    assert caught.value.retryable is False


@pytest.mark.asyncio
async def test_ffmpeg_failure_raises_conversion_error(monkeypatch) -> None:
    import asyncio

    from app.errors import TtsError

    class FakeProcess:
        returncode = 1

        async def communicate(self, data):
            return (b"", b"error")

    async def fake_exec(*args, **kwargs):
        return FakeProcess()

    monkeypatch.setattr(asyncio, "create_subprocess_exec", fake_exec)

    with pytest.raises(TtsError) as caught:
        await wav_to_mp3(wav_silence(), settings())

    assert caught.value.status_code == 500
    assert caught.value.code == "CONVERSION_ERROR"
    assert caught.value.retryable is True


@pytest.mark.asyncio
async def test_ffmpeg_empty_output_raises_conversion_error(monkeypatch) -> None:
    import asyncio

    from app.errors import TtsError

    class FakeProcess:
        returncode = 0

        async def communicate(self, data):
            return (b"", b"")

    async def fake_exec(*args, **kwargs):
        return FakeProcess()

    monkeypatch.setattr(asyncio, "create_subprocess_exec", fake_exec)

    with pytest.raises(TtsError) as caught:
        await wav_to_mp3(wav_silence(), settings())

    assert caught.value.status_code == 500
    assert caught.value.code == "CONVERSION_ERROR"
    assert caught.value.retryable is True
