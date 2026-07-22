from __future__ import annotations

from fastapi.testclient import TestClient

from app.config import Settings
from app.errors import TtsError
from app.main import create_app


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


def test_health_and_status_report_chatterbox_only() -> None:
    client = TestClient(create_app(settings()))

    assert client.get("/health").json() == {"status": "ok"}
    assert client.get("/api/v1/tts/status").json() == {
        "provider": "chatterbox",
        "model": "chatterbox",
        "state": "ready",
    }


def test_synthesize_returns_mp3(monkeypatch) -> None:
    app = create_app(settings())

    async def synthesize(_: object) -> bytes:
        return b"wav"

    async def convert(_: bytes, __: Settings) -> bytes:
        return b"mp3"

    monkeypatch.setattr(app.state.chatterbox, "synthesize", synthesize)
    monkeypatch.setattr("app.main.wav_to_mp3", convert)
    response = TestClient(app).post(
        "/api/v1/tts/synthesize",
        json={"text": "Hola", "voice_profile": "npc", "tone": "playful"},
    )

    assert response.status_code == 200
    assert response.headers["content-type"] == "audio/mpeg"
    assert response.content == b"mp3"


def test_provider_error_uses_contract_shape(monkeypatch) -> None:
    app = create_app(settings())

    async def unavailable(_: object) -> bytes:
        raise TtsError(
            status_code=503,
            code="PROVIDER_UNAVAILABLE",
            message="Chatterbox no está disponible.",
            retryable=True,
        )

    monkeypatch.setattr(app.state.chatterbox, "synthesize", unavailable)
    response = TestClient(app).post("/api/v1/tts/synthesize", json={"text": "Hola"})

    assert response.status_code == 503
    assert response.json() == {
        "error": {
            "code": "PROVIDER_UNAVAILABLE",
            "message": "Chatterbox no está disponible.",
            "retryable": True,
        }
    }


def test_contract_rejects_unknown_tone() -> None:
    client = TestClient(create_app(settings()))
    response = client.post("/api/v1/tts/synthesize", json={"text": "Hola", "tone": "angry"})

    assert response.status_code == 422
