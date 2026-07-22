from __future__ import annotations

import pytest
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
    body = response.json()
    assert body["error"]["code"] == "UNSUPPORTED_TONE"
    assert body["error"]["retryable"] is False
    assert isinstance(body["error"]["message"], str)


def test_contract_rejects_unknown_voice_profile() -> None:
    client = TestClient(create_app(settings()))
    response = client.post(
        "/api/v1/tts/synthesize", json={"text": "Hola", "voice_profile": "robot"}
    )

    assert response.status_code == 422
    body = response.json()
    assert body["error"]["code"] == "UNSUPPORTED_VOICE_PROFILE"
    assert body["error"]["retryable"] is False
    assert isinstance(body["error"]["message"], str)


def test_contract_rejects_empty_text() -> None:
    client = TestClient(create_app(settings()))
    response = client.post("/api/v1/tts/synthesize", json={"text": ""})

    assert response.status_code == 422
    body = response.json()
    assert body["error"]["code"] in ("EMPTY_TEXT", "MISSING_TEXT")
    assert body["error"]["retryable"] is False
    assert isinstance(body["error"]["message"], str)


def test_contract_rejects_intensity_out_of_range() -> None:
    client = TestClient(create_app(settings()))
    response = client.post(
        "/api/v1/tts/synthesize", json={"text": "Hola", "intensity": 1.5}
    )

    assert response.status_code == 422
    body = response.json()
    assert body["error"]["code"] == "VALIDATION_ERROR"
    assert body["error"]["retryable"] is False
    assert isinstance(body["error"]["message"], str)


def test_synthesize_with_npc_profile(monkeypatch) -> None:
    app = create_app(settings())

    async def synthesize(_: object) -> bytes:
        return b"wav"

    async def convert(_: bytes, __: Settings) -> bytes:
        return b"mp3"

    monkeypatch.setattr(app.state.chatterbox, "synthesize", synthesize)
    monkeypatch.setattr("app.main.wav_to_mp3", convert)
    response = TestClient(app).post(
        "/api/v1/tts/synthesize", json={"text": "Hola", "voice_profile": "npc"}
    )
    assert response.status_code == 200
    assert response.headers["content-type"] == "audio/mpeg"


@pytest.mark.parametrize("tone", ["calm", "joyful", "enthusiastic", "playful", "serious"])
def test_all_five_tones_accepted(monkeypatch, tone) -> None:
    app = create_app(settings())

    async def synthesize(_: object) -> bytes:
        return b"wav"

    async def convert(_: bytes, __: Settings) -> bytes:
        return b"mp3"

    monkeypatch.setattr(app.state.chatterbox, "synthesize", synthesize)
    monkeypatch.setattr("app.main.wav_to_mp3", convert)
    response = TestClient(app).post(
        "/api/v1/tts/synthesize", json={"text": "Hola", "tone": tone}
    )
    assert response.status_code == 200


def test_unreachable_chatterbox_returns_provider_unavailable(monkeypatch) -> None:
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
    body = response.json()
    assert body["error"]["code"] == "PROVIDER_UNAVAILABLE"
    assert body["error"]["retryable"] is True


def test_synthesis_timeout_returns_504(monkeypatch) -> None:
    app = create_app(settings())

    async def timeout(_: object) -> bytes:
        raise TtsError(
            status_code=504,
            code="SYNTHESIS_TIMEOUT",
            message="Chatterbox excedió el tiempo de síntesis.",
            retryable=True,
        )

    monkeypatch.setattr(app.state.chatterbox, "synthesize", timeout)
    response = TestClient(app).post("/api/v1/tts/synthesize", json={"text": "Hola"})

    assert response.status_code == 504
    body = response.json()
    assert body["error"]["code"] == "SYNTHESIS_TIMEOUT"
    assert body["error"]["retryable"] is True


def test_conversion_error_returns_500(monkeypatch) -> None:
    app = create_app(settings())

    async def synthesize(_: object) -> bytes:
        return b"wav"

    async def convert(_: bytes, __: Settings) -> bytes:
        raise TtsError(
            status_code=500,
            code="CONVERSION_ERROR",
            message="No se pudo convertir el audio.",
            retryable=True,
        )

    monkeypatch.setattr(app.state.chatterbox, "synthesize", synthesize)
    monkeypatch.setattr("app.main.wav_to_mp3", convert)
    response = TestClient(app).post("/api/v1/tts/synthesize", json={"text": "Hola"})

    assert response.status_code == 500
    body = response.json()
    assert body["error"]["code"] == "CONVERSION_ERROR"
    assert body["error"]["retryable"] is True
