import os
os.environ['TTS_PROVIDER'] = 'chatterbox'
from pydantic_settings import BaseSettings

class T(BaseSettings):
    tts_provider: str = ''

print("tts_provider:", repr(T().tts_provider))