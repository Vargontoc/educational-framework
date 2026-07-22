from __future__ import annotations

import httpx

from app.config import Settings
from app.errors import TtsError
from app.models import SynthesizeRequest
from app.tone_mapping import parameters_for_tone


class ChatterboxClient:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    async def synthesize(self, request: SynthesizeRequest) -> bytes:
        voice = (
            self._settings.chatterbox_npc_voice
            if request.context == "npc"
            else self._settings.chatterbox_storyteller_voice
        )
        payload = {
            "text": request.text,
            "voice": voice,
            "locale": request.locale,
            "emotion": request.emotion,
            **parameters_for_tone(request.tone, request.intensity),
        }
        url = f"{self._settings.chatterbox_base_url}{self._settings.chatterbox_synthesis_path}"
        try:
            async with httpx.AsyncClient(timeout=self._settings.chatterbox_timeout_seconds) as client:
                response = await client.post(url, json=payload)
        except httpx.TimeoutException as exc:
            raise TtsError(
                status_code=504,
                code="SYNTHESIS_TIMEOUT",
                message="Chatterbox excedió el tiempo de síntesis.",
                retryable=True,
            ) from exc
        except httpx.RequestError as exc:
            raise TtsError(
                status_code=503,
                code="PROVIDER_UNAVAILABLE",
                message="Chatterbox no está disponible.",
                retryable=True,
            ) from exc

        if response.status_code >= 500:
            raise TtsError(
                status_code=503,
                code="PROVIDER_UNAVAILABLE",
                message="Chatterbox no está disponible.",
                retryable=True,
            )
        if response.status_code >= 400:
            raise TtsError(
                status_code=422,
                code="PROVIDER_VALIDATION_ERROR",
                message="Chatterbox rechazó la solicitud de síntesis.",
                retryable=False,
            )
        if not response.content:
            raise TtsError(
                status_code=500,
                code="PROVIDER_ERROR",
                message="Chatterbox devolvió una respuesta de audio vacía.",
                retryable=True,
            )
        return response.content
