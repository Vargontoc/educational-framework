from app.adapters.base import ProviderAdapter


class ChatterboxAdapter(ProviderAdapter):
    def synthesize(self, text: str, tone: str, locale: str, voice_profile: str) -> bytes:
        return b""