from typing import Protocol


class ProviderAdapter(Protocol):
    def synthesize(self, text: str, tone: str, locale: str, voice_profile: str) -> bytes:
        ...