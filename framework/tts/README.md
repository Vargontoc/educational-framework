# TTS educational

Servicio interno FastAPI para sintetizar texto autorizado por backend mediante **Chatterbox**, el único proveedor aprobado por ADR-013. No persiste texto ni audio, no incluye caché, ni implementa fallback a otros proveedores.

## Endpoints

- `GET /health`: disponibilidad del proceso TTS.
- `GET /api/v1/tts/status`: declara `chatterbox` como proveedor y modelo activos.
- `POST /api/v1/tts/synthesize`: recibe el contrato `docs/contracts/api/openapi_tts.json` y devuelve `audio/mpeg`.

Los perfiles son `npc` y `storyteller`. Los tonos contractuales se traducen a los parámetros de prosodia de Chatterbox en `app/tone_mapping.py`.

## Configuración

Copiar `.env.example` como referencia. `CHATTERBOX_BASE_URL` es la única URL de proveedor.

`http://127.0.0.1:4123` es válido exclusivamente al ejecutar TTS en el **host** junto a Chatterbox. Dentro del contenedor TTS, esa dirección apunta al propio contenedor; infraestructura debe proporcionar una URL alcanzable desde su red, por ejemplo mediante `CHATTERBOX_BASE_URL` al iniciar el contenedor.

El adaptador usa `POST ${CHATTERBOX_BASE_URL}${CHATTERBOX_SYNTHESIS_PATH}` y envía JSON con `text`, `voice`, `locale`, `emotion`, `exaggeration`, `cfg_weight` y `temperature`. La ruta se mantiene configurable para ajustarse a la API desplegada por infraestructura sin codificar conectividad de red.

## Ejecución local

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
$env:CHATTERBOX_BASE_URL = 'http://127.0.0.1:4123'
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

## Docker

```powershell
docker build -t tts-educational:local framework/tts
docker run --rm -p 8080:8080 -e CHATTERBOX_BASE_URL=http://<ruta-entregada-por-infraestructura> tts-educational:local
```

La imagen instala FFmpeg para la conversión en memoria de WAV a MP3. No se incluye Docker Compose, redes, ni configuración de firewall: corresponden a infraestructura.

## Pruebas

```powershell
pip install -r requirements-dev.txt
pytest -q
```

Las pruebas usan dobles para Chatterbox. La prueba de conversión requiere `ffmpeg` en el host. La síntesis end-to-end en Docker requiere una URL de Chatterbox accesible desde la red del contenedor TTS.
