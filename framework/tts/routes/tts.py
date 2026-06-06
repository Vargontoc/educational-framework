from fastapi import APIRouter, HTTPException, Response, status
from pydantic import BaseModel, Field

from app.adapters.factory import ProviderConfigError, get_provider_adapter
from app.config import get_config
from app.converter import ConversionError


class SynthesizeRequest(BaseModel):
    text: str = Field(..., min_length=1)
    locale: str = Field(default="es")
    tone: str = Field(default="calm")
    emotion: str | None = None
    intensity: float | None = Field(default=None, ge=0.0, le=1.0)
    voice_profile: str = Field(default="default")
    output_format: str = Field(default="mp3")


router = APIRouter(prefix="/api/v1/tts", tags=["tts"])


@router.get("/status")
async def status_endpoint():
    config = get_config()
    return {
        "provider": config.tts_provider,
        "model": "chatterbox" if config.tts_provider == "chatterbox" else "xtts_v2",
        "voiceProfile": "default",
        "state": "ready",
    }


@router.post("/synthesize")
async def synthesize(request: SynthesizeRequest):
    config = get_config()
    try:
        adapter = get_provider_adapter(config.tts_provider)
    except ProviderConfigError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"error": {"code": e.code, "message": e.message, "retryable": e.retryable}},
        )

    try:
        audio_bytes = adapter.synthesize(
            text=request.text,
            tone=request.tone,
            locale=request.locale,
            voice_profile=request.voice_profile,
        )
    except ConversionError as e:
        status_code = {
            "SYNTHESIS_TIMEOUT": status.HTTP_504_GATEWAY_TIMEOUT,
            "PROVIDER_UNAVAILABLE": status.HTTP_503_SERVICE_UNAVAILABLE,
            "UNSUPPORTED_TONE": status.HTTP_422_UNPROCESSABLE_ENTITY,
        }.get(e.code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        raise HTTPException(
            status_code=status_code,
            detail={"error": {"code": e.code, "message": e.message, "retryable": e.retryable}},
        )

    return Response(content=audio_bytes, media_type="audio/mpeg")