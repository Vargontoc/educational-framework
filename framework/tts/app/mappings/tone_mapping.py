TONE_MAPPING_VERSION = "1.0"

SEMANTIC_TONES = ["calm", "joyful", "enthusiastic", "playful", "serious"]

CHATTERBOX_MAPPING = {
    "calm": {"exaggeration": 0.3, "cfg_weight": 0.3, "temperature": 0.3},
    "joyful": {"exaggeration": 0.8, "cfg_weight": 0.5, "temperature": 0.7},
    "enthusiastic": {"exaggeration": 0.9, "cfg_weight": 0.6, "temperature": 0.8},
    "playful": {"exaggeration": 0.7, "cfg_weight": 0.5, "temperature": 0.6},
    "serious": {"exaggeration": 0.25, "cfg_weight": 0.4, "temperature": 0.2},
}

XTTS_MAPPING = {
    "calm": {"speed": 0.85},
    "joyful": {"speed": 1.15},
    "enthusiastic": {"speed": 1.25},
    "playful": {"speed": 1.10},
    "serious": {"speed": 0.75},
}

SUPPORTED_PROVIDERS = ["chatterbox", "xtts"]

_MAPPING_TABLE = {
    "chatterbox": CHATTERBOX_MAPPING,
    "xtts": XTTS_MAPPING,
}


class ToneMappingError(Exception):
    def __init__(self, code: str, message: str, retryable: bool):
        self.code = code
        self.message = message
        self.retryable = retryable
        super().__init__(message)


def resolve_tone(tone: str, provider: str) -> dict:
    if provider not in SUPPORTED_PROVIDERS:
        raise ToneMappingError(
            code="UNKNOWN_PROVIDER",
            message=f"Provider '{provider}' is not supported. Supported providers: {SUPPORTED_PROVIDERS}",
            retryable=False,
        )
    mapping = _MAPPING_TABLE[provider]
    if tone not in SEMANTIC_TONES:
        raise ToneMappingError(
            code="UNKNOWN_TONE",
            message=f"Tone '{tone}' is not supported. Supported tones: {SEMANTIC_TONES}",
            retryable=False,
        )
    return mapping[tone]


def get_mapping_version() -> str:
    return TONE_MAPPING_VERSION


def get_supported_tones() -> list[str]:
    return list(SEMANTIC_TONES)


def get_supported_providers() -> list[str]:
    return list(SUPPORTED_PROVIDERS)
