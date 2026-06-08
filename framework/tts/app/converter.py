import os
import shutil
import subprocess


_ffmpeg_path: str | None = os.environ.get("FFMPEG_PATH") or shutil.which("ffmpeg")


class ConversionError(Exception):
    def __init__(self, code: str, message: str, retryable: bool):
        self.code = code
        self.message = message
        self.retryable = retryable
        super().__init__(message)


def convert_wav_to_mp3(audio_bytes: bytes) -> bytes:
    ffmpeg = _ffmpeg_path or "ffmpeg"
    try:
        result = subprocess.run(
            [
                ffmpeg,
                "-f", "wav",
                "-i", "pipe:0",
                "-f", "mp3",
                "-ar", "16000",
                "-ac", "1",
                "-b:a", "64k",
                "pipe:1",
            ],
            input=audio_bytes,
            capture_output=True,
            timeout=30,
        )
    except subprocess.TimeoutExpired as e:
        raise ConversionError(
            code="CONVERSION_TIMEOUT",
            message="Audio conversion timed out after 30 seconds",
            retryable=False,
        ) from e
    except OSError as e:
        raise ConversionError(
            code="CONVERSION_ERROR",
            message=f"Failed to invoke ffmpeg: {e}",
            retryable=False,
        ) from e

    if result.returncode != 0:
        raise ConversionError(
            code="CONVERSION_ERROR",
            message=result.stderr.decode("utf-8", errors="replace").strip() or "Unknown conversion error",
            retryable=False,
        )

    if not result.stdout:
        raise ConversionError(
            code="CONVERSION_ERROR",
            message="ffmpeg produced no output",
            retryable=False,
        )

    return result.stdout