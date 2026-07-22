from __future__ import annotations

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse, Response

from app.audio import wav_to_mp3
from app.chatterbox import ChatterboxClient
from app.config import Settings
from app.errors import TtsError
from app.models import StatusResponse, SynthesizeRequest


def create_app(settings: Settings | None = None) -> FastAPI:
    app = FastAPI(title="tts-educational", version="0.2.0", docs_url=None, redoc_url=None)
    app.state.settings = settings or Settings.from_environment()
    app.state.chatterbox = ChatterboxClient(app.state.settings)

    @app.exception_handler(TtsError)
    async def handle_tts_error(_: Request, error: TtsError) -> JSONResponse:
        return JSONResponse(status_code=error.status_code, content=error.body())

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.get("/api/v1/tts/status", response_model=StatusResponse)
    async def status_endpoint() -> StatusResponse:
        return StatusResponse(provider="chatterbox", model="chatterbox", state="ready")

    @app.post("/api/v1/tts/synthesize", responses={422: {}, 500: {}, 503: {}, 504: {}})
    async def synthesize(request: SynthesizeRequest) -> Response:
        wav = await app.state.chatterbox.synthesize(request)
        mp3 = await wav_to_mp3(wav, app.state.settings)
        return Response(content=mp3, media_type="audio/mpeg")

    return app


app = create_app()
