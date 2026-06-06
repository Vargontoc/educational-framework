from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config import get_config
from routes.health import router as health_router
from routes.tts import router as tts_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    config = get_config()
    yield


def create_app() -> FastAPI:
    app = FastAPI(
        title="tts-educational",
        description="Internal TTS API shell for provider selection, synthesis orchestration, and audio format normalization.",
        version="0.1.0",
        lifespan=lifespan,
    )
    app.include_router(health_router)
    app.include_router(tts_router)
    return app


app = create_app()