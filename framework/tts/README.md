# TTS educational

Servicio interno de síntesis de voz para **My Friend Nubi**. Aplica FastAPI sobre un único proveedor, **Chatterbox**, conforme a ADR-013. No persiste texto ni audio, no incluye caché y no implementa fallback automático a otros proveedores.

Aplicación monofamiliar: niños de 3-4 años, concurrencia aproximada de 5-6 usuarios.

## Referencias

- **ADR-013**: `docs/product/decisions/ADR-013-Chatterbox-unico-proveedor-TTS.md` — Chatterbox como único proveedor TTS.
- **FEAT-001**: `docs/product/features/tts/FEAT-001-Chatterbox-unico-proveedor-TTS.md` — Especificación funcional.
- **Contrato OpenAPI**: `docs/contracts/api/openapi_tts.json` — Contrato público del servicio.

## Contextos de uso

| Contexto | Descripción | Variable de entorno |
|----------|-------------|---------------------|
| `npc` | Diálogos de personaje NPC (child-friendly) | `CHATTERBOX_NPC_VOICE` |
| `narration` | Narración de cuentos | `CHATTERBOX_STORYTELLER_VOICE` |

El mapeo de contextos a voces de Chatterbox se configura mediante variables de entorno.

## Tonos soportados

Los tonos semánticos del contrato se traducen a parámetros de prosodia de Chatterbox en `app/tone_mapping.py`.

| Tono | exaggeration | cfg_weight | temperature |
|------|-------------|------------|-------------|
| `calm` | 0.25 | 0.30 | 0.70 |
| `joyful` | 0.55 | 0.45 | 0.85 |
| `enthusiastic` | 0.70 | 0.50 | 0.90 |
| `playful` | 0.60 | 0.40 | 0.90 |
| `serious` | 0.20 | 0.55 | 0.65 |
| `tender` | 0.35 | 0.30 | 0.75 |
| `mysterious` | 0.45 | 0.40 | 0.80 |

La **intensidad** es opcional (0.0-1.0). Cuando se proporciona, modula el valor de `exaggeration` del tono: `exaggeration_final = exaggeration_base × intensity`.

## Validación de contexto

Cada contexto solo admite ciertos tonos. No todos los tonos son válidos en todos los contextos:

- **Tonos compartidos** (válidos en ambos contextos): `calm`, `joyful`, `enthusiastic`
- **Tonos exclusivos NPC**: `playful`, `serious`
- **Tonos exclusivos narración**: `tender`, `mysterious`

Si se solicita un tono no permitido para el contexto indicado, el servicio devuelve un error `TONE_CONTEXT_MISMATCH` (HTTP 422, `retryable: false`). No se realiza inferencia automática ni fallback de tono.

## Formato de salida

- **Entrada**: texto + contexto de uso (`context`) y tono (`tone`), conforme al contrato `openapi_tts.json`.
- **Proceso**: Chatterbox genera audio WAV → FFmpeg convierte a MP3 en memoria.
- **Salida exitosa**: `audio/mpeg` (MP3) binario, HTTP 200.
- **Salida de error**: formato `TTSError` en JSON:

```json
{
  "error": {
    "code": "PROVIDER_UNAVAILABLE",
    "message": "Chatterbox no responde",
    "retryable": true
  }
}
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/health` | Estado de salud del proceso TTS. |
| `GET` | `/api/v1/tts/status` | Información del proveedor activo (Chatterbox único). |
| `POST` | `/api/v1/tts/synthesize` | Síntesis de voz. Recibe `SynthesizeRequest`, devuelve `audio/mpeg`. |

## Códigos de error

### Validación

| Código | Descripción |
|--------|-------------|
| `UNSUPPORTED_TONE` | Tono no admitido por el servicio. |
| `UNSUPPORTED_VOICE_PROFILE` | Perfil de voz no admitido. |
| `EMPTY_TEXT` | Texto vacío. |
| `MISSING_TEXT` | Campo `text` ausente en la petición. |
| `VALIDATION_ERROR` | Error genérico de validación de entrada. |
| `TONE_CONTEXT_MISMATCH` | Tono no permitido para el contexto indicado. |

### Proveedor

| Código | Descripción |
|--------|-------------|
| `PROVIDER_UNAVAILABLE` | Chatterbox no está accesible (HTTP 503). |
| `PROVIDER_ERROR` | Chatterbox devolvió un error inesperado (HTTP 500). |
| `PROVIDER_VALIDATION_ERROR` | Chatterbox rechazó la petición por validación (HTTP 422). |

### Timeout

| Código | Descripción |
|--------|-------------|
| `SYNTHESIS_TIMEOUT` | Chatterbox no respondió dentro del tiempo configurado (HTTP 504). |

### Conversión

| Código | Descripción |
|--------|-------------|
| `CONVERSION_ERROR` | Error en la conversión WAV a MP3 (HTTP 500). |

## Variables de entorno

| Variable | Descripción | Default | Ejemplo |
|----------|-------------|---------|---------|
| `CHATTERBOX_BASE_URL` | URL base del servicio Chatterbox | `http://127.0.0.1:4123` | `http://chatterbox:4123` |
| `CHATTERBOX_SYNTHESIS_PATH` | Ruta de síntesis de Chatterbox | `/tts` | `/synthesize` |
| `CHATTERBOX_TIMEOUT_SECONDS` | Timeout de síntesis en segundos | `20.0` | `30` |
| `CHATTERBOX_NPC_VOICE` | Voz de Chatterbox para contexto `npc` | `npc-voice` | `voice-npc-es` |
| `CHATTERBOX_STORYTELLER_VOICE` | Voz de Chatterbox para contexto `narration` | `narrative-voice` | `voice-storyteller-es` |
| `FFMPEG_BINARY` | Binario de FFmpeg para conversión WAV→MP3 | `ffmpeg` | `/usr/bin/ffmpeg` |
| `TTS_MP3_BITRATE` | Bitrate de codificación MP3 | `128k` | `192k` |

> **Nota**: `CHATTERBOX_BASE_URL` debe ser alcanzable desde el contenedor TTS si se ejecuta en Docker. Ver sección *Ejecución en host vs Docker*.

## Ejecución en host vs Docker

### Ejecución en host local

- Chatterbox accesible en `http://127.0.0.1:4123`.
- TTS puede usar el valor por defecto de `CHATTERBOX_BASE_URL`.
- Útil para desarrollo y pruebas locales.

### Ejecución en Docker

- TTS corre en red Docker propia.
- Chatterbox puede estar en un contenedor separado gestionado por infraestructura.
- `127.0.0.1:4123` **NO funciona** desde el contenedor TTS (apunta al propio contenedor).
- Infraestructura debe proporcionar una URL alcanzable desde la red TTS.
- Ejemplo: `CHATTERBOX_BASE_URL=http://chatterbox.infra:4123`.

### Responsabilidades

- **TTS**: consume la URL configurada en `CHATTERBOX_BASE_URL`. No define redes Docker externas.
- **Infraestructura**: proporciona conectividad entre la red TTS y Chatterbox.
- **Backend/frontend**: gestionan continuidad de experiencia cuando el audio no está disponible.

## Handoff a infraestructura

### Lo que TTS no hace

- No define redes Docker externas.
- No define reglas de firewall ni políticas de red.
- No incluye Docker Compose ni orquestación de servicios.

### Lo que infraestructura debe proporcionar

- URL de Chatterbox alcanzable desde la red donde corre el contenedor TTS.
- Mecanismo de acceso (DNS, red compartida, proxy) definido por infraestructura.
- Conectividad entre la red TTS y la red donde reside Chatterbox.
- TTS consume esta URL mediante la variable `CHATTERBOX_BASE_URL` (configuración externa al código).

### Dependencias operativas

- Chatterbox debe estar disponible y accesible desde la red TTS.
- FFmpeg debe estar instalado en la imagen TTS (incluido en el `Dockerfile`).
- La conectividad entre redes es responsabilidad de infraestructura.

### Contacto responsable

- Infraestructura es responsable de la conectividad entre el contenedor TTS y Chatterbox.
- TTS solo consume la URL configurada, no gestiona redes ni resuelve nombres DNS internos.

## Responsabilidades de backend y frontend

### Responsabilidades de TTS

- Síntesis de voz con Chatterbox.
- Conversión WAV a MP3.
- Devolver errores contractuales (`TTSError`) cuando el audio no está disponible.
- No persistir texto ni audio, salvo necesidad aprobada expresamente.

### Responsabilidades de backend

- Validar longitud de texto y locales antes de llamar a TTS.
- Gestionar errores de TTS y decidir si reintentar.
- Permitir continuidad de experiencia sin audio: el juego y la lectura continúan aunque TTS devuelva error.

### Responsabilidades de frontend

- Mostrar estado de audio (disponible / no disponible) cuando corresponda.
- Permitir continuidad de experiencia sin audio.
- No bloquear la experiencia infantil si el audio falla.

### Criterio de protección infantil

- La ausencia de audio **no debe bloquear** el juego ni la lectura.
- Backend y frontend mantienen continuidad cuando TTS devuelve error.
- TTS comunica la ausencia de audio mediante errores contractuales explícitos, no silencios ambiguos.

## Notas de transparencia y configuración

### Proveedor único

- Chatterbox es el único proveedor TTS configurado, conforme a ADR-013.
- No existe opción para familias o menores de seleccionar proveedor.
- La decisión arquitectónica se basa en simplicidad y protección infantil para una aplicación monofamiliar.

### Sin fallback automático

- No hay fallback automático a XTTS, Coqui u otro proveedor.
- Si Chatterbox no está disponible, TTS devuelve un error contractual (`PROVIDER_UNAVAILABLE`, `SYNTHESIS_TIMEOUT`, etc.).
- Backend y frontend deciden cómo gestionar la ausencia de audio.

### Configuración externa

- La configuración de proveedor es externa al código (variables de entorno).
- Cambios de proveedor requieren actualización de ADR y contrato.
- No es una opción configurable por usuarios finales.

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
