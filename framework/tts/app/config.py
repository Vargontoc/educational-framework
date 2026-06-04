from pydantic_settings import BaseSettings, SettingsConfigDict


class Config(BaseSettings):
    model_config = SettingsConfigDict(
        env_file="envs/.env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",
    )

    tts_provider: str = ""
    tts_enable_fallback: bool = False
    tts_fallback_provider: str = ""
    chatterbox_base_url: str = "http://localhost:8000"
    coqui_base_url: str = "http://localhost:5002"
    tts_output_format: str = "mp3"
    tts_timeout_ms: int = 30000
    tts_port: int = 8081
    tts_host: str = "0.0.0.0"


def get_config() -> Config:
    return Config()