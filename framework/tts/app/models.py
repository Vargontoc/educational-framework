from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


Tone = Literal["calm", "joyful", "enthusiastic", "playful", "serious"]
VoiceProfile = Literal["npc", "storyteller"]


class SynthesizeRequest(BaseModel):
    text: str = Field(min_length=1, description="Text to synthesize")
    locale: str = Field(default="es", description="Language locale")
    tone: Tone = Field(default="calm", description="Semantic tone")
    emotion: str | None = Field(default=None, description="Optional emotion hint")
    intensity: float | None = Field(default=None, ge=0.0, le=1.0)
    voice_profile: VoiceProfile = Field(default="npc")


class StatusResponse(BaseModel):
    provider: Literal["chatterbox"]
    model: Literal["chatterbox"]
    state: str
