from __future__ import annotations

import asyncio

from app.config import Settings
from app.errors import TtsError


async def wav_to_mp3(wav: bytes, settings: Settings) -> bytes:
    try:
        process = await asyncio.create_subprocess_exec(
            settings.ffmpeg_binary,
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            "pipe:0",
            "-vn",
            "-ac",
            "1",
            "-b:a",
            settings.mp3_bitrate,
            "-f",
            "mp3",
            "pipe:1",
            stdin=asyncio.subprocess.PIPE,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        mp3, stderr = await process.communicate(wav)
    except FileNotFoundError as exc:
        raise TtsError(
            status_code=500,
            code="CONVERSION_ERROR",
            message="El conversor de audio no está disponible.",
            retryable=False,
        ) from exc

    if process.returncode != 0 or not mp3:
        raise TtsError(
            status_code=500,
            code="CONVERSION_ERROR",
            message="No se pudo convertir el audio de Chatterbox a MP3.",
            retryable=True,
        )
    return mp3
