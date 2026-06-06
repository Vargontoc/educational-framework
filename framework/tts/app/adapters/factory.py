from app.adapters.base import ProviderAdapter
from app.adapters.chatterbox import ChatterboxAdapter
from app.adapters.xtts import XttsAdapter


class ProviderConfigError(Exception):
    def __init__(self, code: str, message: str, retryable: bool):
        self.code = code
        self.message = message
        self.retryable = retryable
        super().__init__(message)


SUPPORTED_PROVIDERS = ["chatterbox", "xtts"]

_ADAPTERS = {
    "chatterbox": ChatterboxAdapter,
    "xtts": XttsAdapter,
}


def get_provider_adapter(provider_name: str) -> ProviderAdapter:
    if provider_name not in SUPPORTED_PROVIDERS:
        raise ProviderConfigError(
            code="UNKNOWN_PROVIDER",
            message=f"Provider '{provider_name}' is not supported. Supported providers: {SUPPORTED_PROVIDERS}",
            retryable=False,
        )
    return _ADAPTERS[provider_name]()