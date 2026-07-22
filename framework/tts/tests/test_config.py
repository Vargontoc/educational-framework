from __future__ import annotations

import pytest

from app.config import Settings


def test_from_environment_uses_defaults_when_no_env_vars(monkeypatch) -> None:
    for var in (
        "CHATTERBOX_BASE_URL",
        "CHATTERBOX_SYNTHESIS_PATH",
        "CHATTERBOX_TIMEOUT_SECONDS",
        "CHATTERBOX_NPC_VOICE",
        "CHATTERBOX_STORYTELLER_VOICE",
        "FFMPEG_BINARY",
        "TTS_MP3_BITRATE",
    ):
        monkeypatch.delenv(var, raising=False)

    s = Settings.from_environment()
    assert s.chatterbox_base_url == "http://127.0.0.1:4123"
    assert s.chatterbox_synthesis_path == "/tts"
    assert s.chatterbox_timeout_seconds == 20.0
    assert s.chatterbox_npc_voice == "npc-voice"
    assert s.chatterbox_storyteller_voice == "narrative-voice"
    assert s.ffmpeg_binary == "ffmpeg"
    assert s.mp3_bitrate == "128k"


def test_from_environment_reads_all_env_vars(monkeypatch) -> None:
    monkeypatch.setenv("CHATTERBOX_BASE_URL", "http://10.0.0.5:9000")
    monkeypatch.setenv("CHATTERBOX_SYNTHESIS_PATH", "/synthesize")
    monkeypatch.setenv("CHATTERBOX_TIMEOUT_SECONDS", "30")
    monkeypatch.setenv("CHATTERBOX_NPC_VOICE", "v-npc")
    monkeypatch.setenv("CHATTERBOX_STORYTELLER_VOICE", "v-story")
    monkeypatch.setenv("FFMPEG_BINARY", "/usr/bin/ffmpeg")
    monkeypatch.setenv("TTS_MP3_BITRATE", "192k")

    s = Settings.from_environment()
    assert s.chatterbox_base_url == "http://10.0.0.5:9000"
    assert s.chatterbox_synthesis_path == "/synthesize"
    assert s.chatterbox_timeout_seconds == 30.0
    assert s.chatterbox_npc_voice == "v-npc"
    assert s.chatterbox_storyteller_voice == "v-story"
    assert s.ffmpeg_binary == "/usr/bin/ffmpeg"
    assert s.mp3_bitrate == "192k"


def test_from_environment_rejects_invalid_base_url(monkeypatch) -> None:
    monkeypatch.setenv("CHATTERBOX_BASE_URL", "ftp://bad-host")
    with pytest.raises(ValueError, match="http:// o https://"):
        Settings.from_environment()


def test_from_environment_rejects_invalid_synthesis_path(monkeypatch) -> None:
    monkeypatch.setenv("CHATTERBOX_SYNTHESIS_PATH", "no-leading-slash")
    with pytest.raises(ValueError, match="'/'"):
        Settings.from_environment()


def test_from_environment_rejects_non_positive_timeout(monkeypatch) -> None:
    monkeypatch.setenv("CHATTERBOX_TIMEOUT_SECONDS", "0")
    with pytest.raises(ValueError, match="mayor que cero"):
        Settings.from_environment()


def test_from_environment_strips_trailing_slash_from_base_url(monkeypatch) -> None:
    monkeypatch.setenv("CHATTERBOX_BASE_URL", "http://chatterbox.local:4123/")
    s = Settings.from_environment()
    assert s.chatterbox_base_url == "http://chatterbox.local:4123"
