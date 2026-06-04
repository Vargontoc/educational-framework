from fastapi import APIRouter, HTTPException, status

router = APIRouter(prefix="/api/v1/tts", tags=["tts"])


@router.get("/health")
async def health():
    return {"status": "healthy", "service": "tts-educational"}


@router.post("/synthesize")
async def synthesize(request: dict):
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Synthesis not implemented in FEAT-001. Provider integration deferred to a later feature.",
    )