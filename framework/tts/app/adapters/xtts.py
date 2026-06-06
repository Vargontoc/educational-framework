from app.adapters.base import ProviderAdapter


class XttsAdapter(ProviderAdapter):
    def synthesize(self, text: str, tone: str, locale: str, voice_profile: str) -> bytes:
        return b""