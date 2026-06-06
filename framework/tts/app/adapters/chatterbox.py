import httpx
from app.adapters.base import ProviderAdapter
from app.config import get_config
from app.converter import ConversionError, convert_wav_to_mp3
from app.mappings.tone_mapping import ToneMappingError, resolve_tone


class ChatterboxAdapter(ProviderAdapter):
    def __init__(self):
        config = get_config()
        self.base_url = config.chatterbox_base_url
        self.endpoint = config.chatterbox_synthesis_endpoint
        self.timeout_ms = config.tts_timeout_ms

    def synthesize(self, text: str, tone: str, locale: str, voice_profile: str) -> bytes:
        try:
            tone_params = resolve_tone(tone, "chatterbox")
        except ToneMappingError:
            raise ConversionError(
                code="UNSUPPORTED_TONE",
                message=f"Tone '{tone}' is not supported",
                retryable=False,
            )

        payload = {
            "text": text,
            "language": locale,
            "exaggeration": tone_params["exaggeration"],
            "cfg_weight": tone_params["cfg_weight"],
            "temperature": tone_params["temperature"],
        }

        try:
            with httpx.Client(timeout=self.timeout_ms / 1000) as client:
                response = client.post(
                    f"{self.base_url}{self.endpoint}",
                    json=payload,
                )
        except httpx.TimeoutException as e:
            raise ConversionError(
                code="SYNTHESIS_TIMEOUT",
                message=f"Chatterbox request timed out after {self.timeout_ms}ms",
                retryable=False,
            ) from e
        except (httpx.ConnectError, httpx.RemoteProtocolError) as e:
            raise ConversionError(
                code="PROVIDER_UNAVAILABLE",
                message=f"Could not connect to Chatterbox at {self.base_url}: {e}",
                retryable=False,
            ) from e

        if response.status_code != 200:
            raise ConversionError(
                code="PROVIDER_ERROR",
                message=f"Chatterbox returned status {response.status_code}: {response.text}",
                retryable=False,
            )

        audio_bytes = response.content
        if not audio_bytes:
            raise ConversionError(
                code="PROVIDER_ERROR",
                message="Chatterbox returned empty audio",
                retryable=False,
            )

        return convert_wav_to_mp3(audio_bytes)