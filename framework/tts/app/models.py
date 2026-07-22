from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


Tone = Literal["calm", "joyful", "enthusiastic", "playful", "serious", "tender", "mysterious"]
Context = Literal["npc", "narration"]


class SynthesizeRequest(BaseModel):
    text: str = Field(min_length=1, description="Text to synthesize")
    locale: str = Field(default="es", description="Language locale")
    context: Context = Field(default="npc", description="Usage context: npc or narration")
    tone: Tone = Field(default="calm", description="Semantic tone")
    emotion: str | None = Field(default=None, description="Optional emotion hint")
    intensity: float | None = Field(default=None, ge=0.0, le=1.0)


class StatusResponse(BaseModel):
    provider: Literal["chatterbox"]
    model: Literal["chatterbox"]
    state: str
