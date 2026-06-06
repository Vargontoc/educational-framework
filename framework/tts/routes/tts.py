from fastapi import APIRouter, HTTPException, status

router = APIRouter(prefix="/api/v1/tts", tags=["tts"])


@router.get("/status")
async def status_endpoint():
    return {
        "provider": "chatterbox",
        "model": "xtts_v2",
        "voiceProfile": "default",
        "state": "ready",
    }


@router.post("/synthesize")
async def synthesize(request: dict):
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Synthesis not implemented in FEAT-001. Provider integration deferred to a later feature.",
    )