import pytest
from httpx import ASGITransport, AsyncClient
from unittest.mock import Mock, patch

from app.main import app


@pytest.mark.asyncio
async def test_healthcheck_returns_200():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["service"] == "tts-educational"


@pytest.mark.asyncio
async def test_synthesize_returns_audio_mpeg():
    adapter = Mock()
    adapter.synthesize.return_value = b"mp3-bytes"
    transport = ASGITransport(app=app)
    with patch("routes.tts.get_provider_adapter", return_value=adapter):
        async with AsyncClient(transport=transport, base_url="http://test") as client:
            response = await client.post("/api/v1/tts/synthesize", json={"text": "Hello"})
    assert response.status_code == 200
    assert response.headers["content-type"] == "audio/mpeg"
    assert response.content == b"mp3-bytes"
    adapter.synthesize.assert_called_once_with(
        text="Hello",
        tone="calm",
        locale="es",
        voice_profile="npc",
    )


@pytest.mark.asyncio
async def test_status_returns_200():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/api/v1/tts/status")
    assert response.status_code == 200
    data = response.json()
    assert "provider" in data
    assert "state" in data
