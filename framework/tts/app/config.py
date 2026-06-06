from pydantic_settings import BaseSettings, SettingsConfigDict


class Config(BaseSettings):
    model_config = SettingsConfigDict(
        env_file="envs/.env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    tts_provider: str = ""
    tts_enable_fallback: bool = False
    tts_fallback_provider: str = ""
    chatterbox_base_url: str = "http://chatterbox-educational:5003"
    chatterbox_synthesis_endpoint: str = "/v1/synthesize"
    coqui_base_url: str = "http://localhost:5002"
    coqui_synthesis_endpoint: str = "/v1/synthesize"
    xtts_timeout_ms: int = 90000
    tts_output_format: str = "mp3"
    tts_timeout_ms: int = 30000
    tts_max_text_length: int = 300
    tts_port: int = 8081
    tts_host: str = "0.0.0.0"


def get_config() -> Config:
    config = Config()
    if not config.tts_provider:
        raise ValueError("TTS_PROVIDER is required")
    return config
