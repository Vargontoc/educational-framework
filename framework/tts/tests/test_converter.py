import io
import shutil
import wave

import pytest

from app.converter import ConversionError, convert_wav_to_mp3


def generate_wav_bytes(duration_seconds: float = 0.5, sample_rate: int = 16000) -> bytes:
    num_samples = int(sample_rate * duration_seconds)
    buffer = io.BytesIO()
    with wave.open(buffer, "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(sample_rate)
        for _ in range(num_samples):
            wav.writeframes(b"\x00\x00")
    buffer.seek(0)
    return buffer.read()


@pytest.fixture(autouse=True)
def check_ffmpeg():
    if not shutil.which("ffmpeg"):
        pytest.skip("ffmpeg not installed on this system")


def test_valid_wav_converts_to_non_empty_mp3():
    wav_bytes = generate_wav_bytes(duration_seconds=0.5, sample_rate=16000)
    mp3_bytes = convert_wav_to_mp3(wav_bytes)
    assert len(mp3_bytes) > 0


def test_output_is_recognizable_as_mp3():
    wav_bytes = generate_wav_bytes(duration_seconds=0.5, sample_rate=16000)
    mp3_bytes = convert_wav_to_mp3(wav_bytes)
    assert mp3_bytes[:3] == b"ID3", "MP3 output should start with ID3 tag"


def test_invalid_input_raises_conversion_error():
    invalid_bytes = b"this is not a valid wav file"
    with pytest.raises(ConversionError):
        convert_wav_to_mp3(invalid_bytes)


def test_conversion_error_has_code_and_message():
    invalid_bytes = b"not audio"
    with pytest.raises(ConversionError) as exc_info:
        convert_wav_to_mp3(invalid_bytes)
    error = exc_info.value
    assert hasattr(error, "code")
    assert hasattr(error, "message")
    assert hasattr(error, "retryable")