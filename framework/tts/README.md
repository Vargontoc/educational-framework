# tts-educational

Internal TTS API shell for provider selection, synthesis orchestration, and audio format normalization.

## Local Development

### Prerequisites

- Python 3.12+
- pip

### Setup

```bash
cd framework/tts
pip install -r requirements.txt
```

### Run

```bash
PYTHONPATH=. uvicorn app.main:app --host 0.0.0.0 --port 8081 --reload
```

### Test

```bash
pytest tests/ -v
```

## Docker

### Build

```bash
docker build -f framework/tts/Dockerfile -t tts-educational .
```

### Run

```bash
docker run -p 8081:8081 tts-educational
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/tts/health` | Health check |
| POST | `/api/v1/tts/synthesize` | Placeholder synthesis (501 Not Implemented) |