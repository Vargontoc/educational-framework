from __future__ import annotations

from dataclasses import dataclass
import os


def _positive_float(name: str, default: float) -> float:
    value = float(os.getenv(name, str(default)))
    if value <= 0:
        raise ValueError(f"{name} debe ser mayor que cero")
    return value


@dataclass(frozen=True)
class Settings:
    chatterbox_base_url: str
    chatterbox_synthesis_path: str
    chatterbox_timeout_seconds: float
    chatterbox_npc_voice: str
    chatterbox_storyteller_voice: str
    ffmpeg_binary: str
    mp3_bitrate: str

    @classmethod
    def from_environment(cls) -> "Settings":
        base_url = os.getenv("CHATTERBOX_BASE_URL", "http://127.0.0.1:4123").rstrip("/")
        if not base_url.startswith(("http://", "https://")):
            raise ValueError("CHATTERBOX_BASE_URL debe usar http:// o https://")

        path = os.getenv("CHATTERBOX_SYNTHESIS_PATH", "/tts").strip()
        if not path.startswith("/"):
            raise ValueError("CHATTERBOX_SYNTHESIS_PATH debe comenzar por '/'")

        return cls(
            chatterbox_base_url=base_url,
            chatterbox_synthesis_path=path,
            chatterbox_timeout_seconds=_positive_float("CHATTERBOX_TIMEOUT_SECONDS", 20.0),
            chatterbox_npc_voice=os.getenv("CHATTERBOX_NPC_VOICE", "npc-voice"),
            chatterbox_storyteller_voice=os.getenv(
                "CHATTERBOX_STORYTELLER_VOICE", "narrative-voice"
            ),
            ffmpeg_binary=os.getenv("FFMPEG_BINARY", "ffmpeg"),
            mp3_bitrate=os.getenv("TTS_MP3_BITRATE", "128k"),
        )
