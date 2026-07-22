# Sprint 001 - TTS

## Goal
Crear desde cero el servicio TTS con Chatterbox como único proveedor y una imagen Docker reproducible, configurable para la conectividad proporcionada por infraestructura.

## Status
status: completed
started_at: 2026-07-21 00:00:00
closed_at: 2026-07-22 00:00:00
blocked_by:
waiting_for:

## Corrective Tasks for DEF-001

### Tarea C-001: Implementar exception_handler para RequestValidationError
**Objetivo**: Traducir los errores de validación de Pydantic al formato contractual `TTSError` antes de que FastAPI devuelva su formato por defecto `HTTPValidationError`.

**Implementación requerida**:
- Registrar un `@app.exception_handler(RequestValidationError)` en `app/main.py`
- Importar `RequestValidationError` desde `fastapi.exceptions`
- Inspeccionar los errores de validación y mapearlos a códigos contractuales:
  - Errores en campo `tone` (literal_error) → `code: "UNSUPPORTED_TONE"`
  - Errores en campo `voice_profile` (literal_error) → `code: "UNSUPPORTED_VOICE_PROFILE"`
  - Errores en campo `text` (string_too_short, missing) → `code: "EMPTY_TEXT"` o `"MISSING_TEXT"`
- Construir respuesta con formato `TTSError`: `{"error": {"code": "...", "message": "...", "retryable": false}}`
- Devolver `JSONResponse(status_code=422, content=tts_error_body)`
- Mensajes deben ser descriptivos pero concisos, en español o inglés según convención del proyecto

**Criterios de aceptación**:
- `POST /api/v1/tts/synthesize` con `tone` no soportado devuelve 422 con cuerpo `{"error": {"code": "UNSUPPORTED_TONE", "message": "...", "retryable": false}}`
- `POST /api/v1/tts/synthesize` con `voice_profile` no soportado devuelve 422 con cuerpo `{"error": {"code": "UNSUPPORTED_VOICE_PROFILE", "message": "...", "retryable": false}}`
- `POST /api/v1/tts/synthesize` con `text` vacío devuelve 422 con cuerpo `{"error": {"code": "EMPTY_TEXT", "message": "...", "retryable": false}}`
- El handler no rompe el manejo de otros errores de validación (ej: `intensity` fuera de rango 0.0-1.0)
- Los errores de validación no son retryable (`retryable: false`)

**Evidencias esperadas**:
- Comando de reproducción: `TestClient(app).post("/api/v1/tts/synthesize", json={"text":"Hola","tone":"angry"})` devuelve 422 con formato `TTSError`
- Salida JSON completa mostrada en la evidencia

### Tarea C-002: Ampliar pruebas de contrato de errores
**Objetivo**: Verificar que las pruebas validan el cuerpo completo de la respuesta, no solo el status code.

**Implementación requerida**:
- Modificar `test_contract_rejects_unknown_tone` en `tests/test_api.py`:
  - Afirmar `status_code == 422`
  - Afirmar `response.json() == {"error": {"code": "UNSUPPORTED_TONE", "message": str, "retryable": false}}`
  - Verificar que `code` es exactamente `"UNSUPPORTED_TONE"`
  - Verificar que `retryable` es `false`
- Añadir `test_contract_rejects_unknown_voice_profile`:
  - Enviar `{"text": "Hola", "voice_profile": "robot"}`
  - Afirmar 422 con `code: "UNSUPPORTED_VOICE_PROFILE"` y `retryable: false`
- Añadir `test_contract_rejects_empty_text`:
  - Enviar `{"text": ""}`
  - Afirmar 422 con `code: "EMPTY_TEXT"` o `"MISSING_TEXT"` y `retryable: false`
- Opcional: Añadir prueba para `intensity` fuera de rango (ej: `1.5`) para verificar que otros errores de validación también siguen el formato `TTSError`

**Criterios de aceptación**:
- Todas las pruebas nuevas pasan con `pytest -q`
- Las pruebas verifican el cuerpo completo de la respuesta, no solo el status code
- Las pruebas documentan los códigos de error contractuales esperados

**Evidencias esperadas**:
- Salida de `pytest -q` mostrando todas las pruebas pasando
- Número total de pruebas (debe ser >= 10, considerando las 7 originales + 3-4 nuevas)

### Tarea C-003: Documentar códigos de error en el contrato (opcional, para el responsable del contrato)
**Objetivo**: Clarificar los códigos de error de validación en el contrato OpenAPI para que los consumidores puedan distinguirlos.

**Acción requerida**:
- Añadir nota en `docs/contracts/api/openapi_tts.json` o en un documento de decisiones:
  - `UNSUPPORTED_TONE`: tone no está en la enumeración aprobada
  - `UNSUPPORTED_VOICE_PROFILE`: voice_profile no está en la enumeración aprobada
  - `EMPTY_TEXT`: text está vacío o falta
  - `MISSING_TEXT`: text no está presente en la petición (si se distingue de EMPTY_TEXT)
- Considerar si `HTTPValidationError` debe mantenerse en el contrato si no se usa en `/api/v1/tts/synthesize`

**Criterios de aceptación**:
- Documentación actualizada con los códigos de error de validación
- Decisión documentada sobre `HTTPValidationError` (mantener/eliminar)

**Nota**: Esta tarea puede realizarse en paralelo o en el siguiente sprint si no bloquea la corrección de DEF-001.

## Tasks
- [x] Crear la estructura inicial de `framework/tts` y su API Python conforme a las convenciones de la capa.
- [x] Implementar Chatterbox como único proveedor de síntesis, sin abstracciones ni configuración para proveedores alternativos o fallback automático.
- [x] Implementar configuración exclusiva de Chatterbox mediante variables de entorno.
- [x] Implementar los perfiles `npc` y `storyteller` y el mapeo de tonos aprobado para Chatterbox.
- [x] Implementar la respuesta MP3 y la conversión WAV a MP3 conforme a `docs/contracts/api/openapi_tts.json`.
- [x] Crear el `Dockerfile` de `framework/tts` con las dependencias de Python, servidor API y FFmpeg necesarias para la conversión de audio.
- [x] Configurar la URL de Chatterbox mediante variable de entorno; documentar `http://127.0.0.1:4123` exclusivamente como valor para ejecución de TTS en el host, no dentro del contenedor TTS.
- [x] Comprobar que la imagen Docker construye y que el servicio puede arrancar y exponer su endpoint de salud.
- [x] C-001: Implementar exception_handler para RequestValidationError que traduzca errores de Pydantic al formato contractual TTSError. (verificado)
- [x] C-002: Ampliar pruebas de contrato de errores para verificar cuerpo completo de respuestas 422. (verificado)
- [ ] C-003: Documentar códigos de error en el contrato (opcional, diferido a SPRINT-002).

## Risks
- Desde el contenedor TTS, `127.0.0.1:4123` apunta al propio contenedor y no al contenedor Chatterbox de infraestructura.
- La imagen puede omitir FFmpeg y romper la conversión WAV a MP3.
- Cambios internos pueden alterar involuntariamente el contrato público del servicio.

## Dependencies
- `docs/product/features/tts/FEAT-001-Chatterbox-unico-proveedor-TTS.md`.
- `docs/product/decisions/ADR-013-Chatterbox-unico-proveedor-TTS.md`.
- `docs/contracts/api/openapi_tts.json` como contrato de síntesis y errores.
- Infraestructura debe proporcionar una URL de Chatterbox alcanzable desde la red propia del contenedor TTS.
- Chatterbox está disponible en `127.0.0.1:4123` solo desde el host local o un proceso que comparta ese loopback.

## Agent Instruction
- Crear e implementar exclusivamente la capa `framework/tts` y su documentación técnica inmediata.
- No crear Docker Compose, redes Docker, reglas de firewall ni solución de conectividad entre la red TTS y la red de infraestructura: corresponde a infraestructura.
- No fijar en código una URL que suponga conectividad entre contenedores; consumir la URL mediante configuración de entorno.
- No modificar `docs/contracts/api/openapi_tts.json` salvo incompatibilidad demostrada y confirmación del responsable del contrato.
- No implementar fallback a XTTS, Coqui u otro proveedor.
- Mantener audio opcional: los errores deben devolverse conforme al contrato para que backend/frontend permitan continuidad sin audio.
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- El proyecto TTS parte de cero; no hay código, configuración, pruebas ni documentación existente de XTTS/Coqui que deba retirarse.
- Chatterbox es el único proveedor TTS aceptado por ADR-013.
- TTS estará en una red Docker propia; Chatterbox pertenece a un contenedor de infraestructura local en una red diferente.
- La síntesis end-to-end dentro del contenedor queda condicionada a la URL o conectividad que entregue infraestructura.
- La validación de longitud de texto y locales corresponde al backend, no al servicio TTS.

## Review

completed_tasks: Todas las tareas del sprint implementadas y verificadas en `framework/tts`.

verified_tasks:
- Estructura inicial de `framework/tts` y API Python
- Chatterbox como único proveedor de síntesis
- Configuración exclusiva por variables de entorno
- Perfiles `npc` y `storyteller` con mapeo de tonos
- Respuesta MP3 y conversión WAV a MP3
- Dockerfile con FFmpeg y usuario sin privilegios
- URL de Chatterbox por variable de entorno
- Imagen Docker construye y health endpoint funcional
- C-001: exception_handler para RequestValidationError
- C-002: Pruebas ampliadas con verificación de cuerpo completo

incomplete_tasks: La validación end-to-end con Chatterbox real queda pendiente de la URL alcanzable que debe entregar infraestructura; no bloquea la implementación ni el arranque del contenedor.

deferred_tasks: C-003 (documentación de códigos de error en contrato) diferida a SPRINT-002 como mejora no bloqueante.

contract_changes: Ninguno. Se mantiene `docs/contracts/api/openapi_tts.json`. Hallazgo para el responsable del contrato: `StatusResponse` conserva enumeraciones históricas `xtts` y `xtts_v2`, pero la implementación publica exclusivamente `chatterbox`. El esquema `HTTPValidationError` ya no se produce y puede eliminarse en SPRINT-002.

learnings: La URL de Chatterbox se consume exclusivamente desde `CHATTERBOX_BASE_URL`. El valor `http://127.0.0.1:4123` se documentó solo para TTS ejecutado en el host. La imagen incorpora FFmpeg para conversión WAV a MP3 en memoria y se ejecuta como usuario sin privilegios. El handler de errores de validación traduce correctamente los errores de Pydantic al formato contractual `TTSError`.

next_sprint_suggestions: Ejecutar Sprint 002 con los dobles unitarios existentes y una integración explícita cuando infraestructura entregue una ruta Chatterbox accesible desde la red del contenedor TTS. Incluir limpieza del contrato OpenAPI (eliminar `HTTPValidationError` y actualizar enumeraciones de `StatusResponse`).

sprint_verdict: APPROVED_WITH_OBSERVATIONS (2026-07-22)

### Evidencias de implementación

- `python -m pytest -q` desde `framework/tts`: **7 passed** (0.82 s). Incluye contrato de perfiles/tonos, errores controlados, conversión WAV a MP3 y estado Chatterbox único.
- `docker build -t tts-educational:sprint-001 .` desde `framework/tts`: **correcto**.
- Arranque reproducible: `docker run --rm -d --name tts-sprint-001-test -p 18080:8080 -e CHATTERBOX_BASE_URL=http://unreachable.invalid tts-educational:sprint-001` y `GET /health`: **200** `{"status":"ok"}`.
- Latencia medida: pruebas unitarias completas en 0.82 s. No se mide latencia de síntesis real sin Chatterbox accesible.
- Fallbacks: no implementados por ADR-013; timeout devuelve `504 SYNTHESIS_TIMEOUT`, proveedor inaccesible devuelve `503 PROVIDER_UNAVAILABLE` y no se intenta otro proveedor.

### Developer fix DEF-001 — Evidencias de implementación (2026-07-22)

**Archivos modificados**:
- `framework/tts/app/main.py`: Añadido `exception_handler(RequestValidationError)` con mapeo de campos a códigos contractuales (`UNSUPPORTED_TONE`, `UNSUPPORTED_VOICE_PROFILE`, `EMPTY_TEXT`, `MISSING_TEXT`, `VALIDATION_ERROR`)
- `framework/tts/tests/test_api.py`: Ampliada `test_contract_rejects_unknown_tone` con aserciones de cuerpo completo; añadidas 3 pruebas nuevas (`test_contract_rejects_unknown_voice_profile`, `test_contract_rejects_empty_text`, `test_contract_rejects_intensity_out_of_range`)

**Resultados de pytest**:
```
10 passed in 0.27s
```

**Verificación manual con TestClient**:

Caso 1 — Tone inválido:
```
POST /api/v1/tts/synthesize {"text": "Hola", "tone": "angry"}
Status: 422
Body: {"error": {"code": "UNSUPPORTED_TONE", "message": "Input should be 'calm', 'joyful', 'enthusiastic', 'playful' or 'serious'", "retryable": False}}
```

Caso 2 — Voice profile inválido:
```
POST /api/v1/tts/synthesize {"text": "Hola", "voice_profile": "robot"}
Status: 422
Body: {"error": {"code": "UNSUPPORTED_VOICE_PROFILE", "message": "Input should be 'npc' or 'storyteller'", "retryable": False}}
```

Caso 3 — Text vacío:
```
POST /api/v1/tts/synthesize {"text": ""}
Status: 422
Body: {"error": {"code": "EMPTY_TEXT", "message": "String should have at least 1 character", "retryable": False}}
```

**Conformidad contractual**: Los tres casos devuelven formato `TTSError` (`{"error": {"code", "message", "retryable"}}`) conforme a `docs/contracts/api/openapi_tts.json`. Los errores de validación son `retryable: false`. El handler no rompe el manejo de otros errores de validación (ej: `intensity` fuera de rango devuelve `VALIDATION_ERROR`).

**DEF-001**: cerrado.

### Reviewer re-verification — APPROVED_WITH_OBSERVATIONS (2026-07-22)

Re-ejecutadas las pruebas y verificaciones independientes por reviewer-tts:

**Evidencias re-ejecutadas**:
- `pytest -q`: **10 passed** (0.28s) ✅
- `docker build -t tts-educational:sprint-001-review .`: **correcto** ✅
- Contenedor arrancado con `CHATTERBOX_BASE_URL=http://unreachable.invalid`:
  - `GET /health` → 200 `{"status":"ok"}` ✅
  - `GET /api/v1/tts/status` → 200 `{"provider":"chatterbox","model":"chatterbox","state":"ready"}` ✅
- Grep `xtts|coqui` en `framework/tts/**/*.py`: **0 resultados** ✅

**Verificación manual independiente (TestClient)**:

| Caso | Status | Código | `retryable` | Formato TTSError |
|---|---|---|---|---|
| `tone: "angry"` | 422 | `UNSUPPORTED_TONE` | `false` | ✅ Conforme |
| `voice_profile: "robot"` | 422 | `UNSUPPORTED_VOICE_PROFILE` | `false` | ✅ Conforme |
| `text: ""` | 422 | `EMPTY_TEXT` | `false` | ✅ Conforme |
| `intensity: 1.5` | 422 | `VALIDATION_ERROR` | `false` | ✅ Conforme |
| `text` faltante | 422 | `MISSING_TEXT` | `false` | ✅ Conforme |

Todos los casos devuelven `{"error": {"code", "message", "retryable"}}` conforme al esquema `TTSError` de `openapi_tts.json`.

**Revisión de código**:
- `app/main.py`: Handler `RequestValidationError` correctamente registrado con mapeo `_FIELD_CODE_MAP` claro y extensible.
- `app/models.py`: Validaciones Pydantic (`Literal`, `Field(min_length=1)`, `ge=0.0, le=1.0`).
- `app/chatterbox.py`: Cliente exclusivo de Chatterbox, timeout → 504, conexión fallida → 503, sin fallback.
- `app/audio.py`: Conversión WAV→MP3 vía FFmpeg con manejo de errores contractual.
- `Dockerfile`: FFmpeg incluido, usuario sin privilegios (UID 10001), imagen mínima `python:3.12-slim`.

**Completitud del sprint**:
- ✅ Todas las tareas originales implementadas y verificadas
- ✅ C-001: exception_handler RequestValidationError implementado
- ✅ C-002: Pruebas ampliadas con verificación de cuerpo completo
- ⏭️ C-003: Documentación de códigos en contrato (opcional, no bloqueante)

**Observaciones no bloqueantes** (para SPRINT-002):
1. `openapi_tts.json` mantiene el esquema `HTTPValidationError` (líneas 231-272) que ya no se produce en ningún endpoint. Recomendación: eliminarlo en el siguiente sprint.
2. `StatusResponse` en el contrato aún lista `xtts` y `xtts_v2` como valores posibles (líneas 182, 188), pero la implementación solo publica `chatterbox`. Ya documentado como hallazgo para el responsable del contrato.
3. C-003 puede abordarse en SPRINT-002 sin bloquear el cierre actual.

**Veredicto final**: `APPROVED_WITH_OBSERVATIONS`

DEF-001 cerrado. Sprint completado y verificado. Las 3 observaciones son mejoras de limpieza contractual no bloqueantes que pueden abordarse en SPRINT-002.

### Reviewer verdict — CHANGES_REQUIRED (2026-07-21)

Revisado por el reviewer-tts independiente. Re-ejecutadas las pruebas (`pytest -q`: 7 passed), `docker build` (correcto, capas cacheadas) y arranque del contenedor con `-e CHATTERBOX_BASE_URL=http://unreachable.invalid` (`GET /health` → 200, `GET /api/v1/tts/status` → 200 `{"provider":"chatterbox","model":"chatterbox","state":"ready"}`). Confirmadas exclusividad de Chatterbox (sin referencias a `xtts`/`coqui` en `framework/tts`), ausencia de persistencia de texto/audio y usuario Docker sin privilegios.

**DEF-001**
- severity: major
- type: contract
- task: "Implementar la respuesta MP3 y la conversión WAV a MP3 conforme a `docs/contracts/api/openapi_tts.json`" (criterio de aceptación FEAT-001 #3: "Una solicitud con un tono no admitido devuelve el error contractual correspondiente")
- description: `SynthesizeRequest` valida `tone`, `voice_profile` y `text` mediante `Literal`/`Field` de Pydantic (`app/models.py`). FastAPI intercepta estos casos con su manejador por defecto de `RequestValidationError` **antes** de llegar al handler de `synthesize`, por lo que nunca se lanza `TtsError`. La respuesta 422 real no usa el esquema `TTSError` (`{"error": {"code","message","retryable"}}`) definido en `openapi_tts.json` para ese mismo código de estado, sino el esquema por defecto `HTTPValidationError` (`{"detail": [...]}`) que no forma parte del contrato de este endpoint.
- expected: `POST /api/v1/tts/synthesize` con `tone` no soportado, `voice_profile` no soportado o `text` vacío devuelve 422 con cuerpo `{"error": {"code": "...", "message": "...", "retryable": false}}`.
- observed: verificado en vivo (`TestClient`, sin mocks) para tres casos:
  - `{"text":"Hola","tone":"angry"}` → 422 `{"detail":[{"type":"literal_error","loc":["body","tone"],"msg":"Input should be 'calm', 'joyful', 'enthusiastic', 'playful' or 'serious'", ...}]}`
  - `{"text":"Hola","voice_profile":"robot"}` → 422 `{"detail":[{"type":"literal_error","loc":["body","voice_profile"], ...}]}`
  - `{"text":""}` → 422 `{"detail":[{"type":"string_too_short","loc":["body","text"], ...}]}`
- evidence: comando ejecutado — `TestClient(create_app(settings)).post("/api/v1/tts/synthesize", json={"text":"Hola","tone":"angry"})` desde un intérprete Python con `app` en el path; salida arriba.
- reproduction: cualquier petición a `/api/v1/tts/synthesize` con un valor de `tone` o `voice_profile` fuera de las enumeraciones aprobadas, o `text` vacío.
  - required_action: los backends consumidores necesitan distinguir "audio no disponible" del contrato `TTSError` para permitir continuidad sin audio (FEAT-001, criterio de privacidad/continuidad). Añadir un `exception_handler(RequestValidationError)` (o validación manual previa) que traduzca los errores de validación de Pydantic al esquema `TTSError` — p. ej. `code="UNSUPPORTED_TONE"` para `tone`/`voice_profile` inválidos — antes de declarar el sprint conforme al contrato.
  - status: closed (corregido en developer fix 2026-07-22)

**Nota de cobertura de pruebas**: `tests/test_api.py::test_contract_rejects_unknown_tone` solo comprueba `status_code == 422` y no el cuerpo de la respuesta, por lo que el suite verde no detectó este defecto. Al corregir DEF-001, ampliar esta prueba (y añadir casos para `voice_profile` y `text` vacío) para afirmar la forma `TTSError` completa.

**Veredicto**: `CHANGES_REQUIRED` → `REVIEW_PENDING` (2026-07-22). DEF-001 corregido por developer-tts. Tareas C-001 y C-002 implementadas con evidencias reproducibles: 10 pruebas pasando, verificación manual de los 3 casos de validación confirma formato `TTSError` conforme a `openapi_tts.json`. Pendiente de re-verificación por reviewer-tts independiente para promover tareas a `verified` y cerrar el sprint.

## Developer Instructions for DEF-001 Fix

### Contexto del defecto
FastAPI intercepta los errores de validación de Pydantic con su handler por defecto (`RequestValidationError`), devolviendo formato `HTTPValidationError` (`{"detail": [...]}`) en lugar del formato contractual `TTSError` (`{"error": {"code", "message", "retryable"}}`) definido en `docs/contracts/api/openapi_tts.json`.

### Archivos a modificar
1. **`framework/tts/app/main.py`**: Añadir `exception_handler(RequestValidationError)`
2. **`framework/tts/tests/test_api.py`**: Ampliar pruebas para verificar cuerpo completo de respuestas 422

### Pasos de implementación

#### Paso 1: Implementar el exception_handler en main.py
```python
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

# Dentro de create_app(), después del handler de TtsError:

@app.exception_handler(RequestValidationError)
async def handle_validation_error(request: Request, exc: RequestValidationError) -> JSONResponse:
    # Inspeccionar exc.errors() para determinar el código contractual
    errors = exc.errors()
    if not errors:
        return JSONResponse(status_code=422, content={"error": {"code": "VALIDATION_ERROR", "message": "Invalid request", "retryable": False}})
    
    first_error = errors[0]
    field = first_error.get("loc", [])[-1] if first_error.get("loc") else None
    error_type = first_error.get("type", "")
    
    # Mapeo de campos a códigos contractuales
    if field == "tone":
        code = "UNSUPPORTED_TONE"
        message = f"Tone no soportado. Valores permitidos: calm, joyful, enthusiastic, playful, serious"
    elif field == "voice_profile":
        code = "UNSUPPORTED_VOICE_PROFILE"
        message = f"Voice profile no soportado. Valores permitidos: npc, storyteller"
    elif field == "text":
        if error_type == "string_too_short" or error_type == "missing":
            code = "EMPTY_TEXT"
            message = "El texto no puede estar vacío"
        else:
            code = "INVALID_TEXT"
            message = "Texto inválido"
    else:
        code = "VALIDATION_ERROR"
        message = first_error.get("msg", "Error de validación")
    
    return JSONResponse(
        status_code=422,
        content={"error": {"code": code, "message": message, "retryable": False}}
    )
```

#### Paso 2: Ampliar pruebas en test_api.py
Modificar `test_contract_rejects_unknown_tone` para verificar el cuerpo completo:
```python
def test_contract_rejects_unknown_tone() -> None:
    client = TestClient(create_app(settings()))
    response = client.post("/api/v1/tts/synthesize", json={"text": "Hola", "tone": "angry"})
    
    assert response.status_code == 422
    body = response.json()
    assert "error" in body
    assert body["error"]["code"] == "UNSUPPORTED_TONE"
    assert body["error"]["retryable"] is False
    assert "message" in body["error"]
```

Añadir nuevas pruebas:
```python
def test_contract_rejects_unknown_voice_profile() -> None:
    client = TestClient(create_app(settings()))
    response = client.post("/api/v1/tts/synthesize", json={"text": "Hola", "voice_profile": "robot"})
    
    assert response.status_code == 422
    body = response.json()
    assert body["error"]["code"] == "UNSUPPORTED_VOICE_PROFILE"
    assert body["error"]["retryable"] is False

def test_contract_rejects_empty_text() -> None:
    client = TestClient(create_app(settings()))
    response = client.post("/api/v1/tts/synthesize", json={"text": ""})
    
    assert response.status_code == 422
    body = response.json()
    assert body["error"]["code"] in ["EMPTY_TEXT", "MISSING_TEXT"]
    assert body["error"]["retryable"] is False
```

#### Paso 3: Ejecutar pruebas y verificar
```bash
cd framework/tts
python -m pytest -q
```
Esperado: **>= 10 pruebas pasando** (7 originales + 3 nuevas)

#### Paso 4: Verificar manualmente con TestClient
```python
from fastapi.testclient import TestClient
from app.main import create_app
from app.config import Settings

settings = Settings(
    chatterbox_base_url="http://chatterbox.test",
    chatterbox_synthesis_path="/tts",
    chatterbox_timeout_seconds=1,
    chatterbox_npc_voice="npc-voice",
    chatterbox_storyteller_voice="narrative-voice",
    ffmpeg_binary="ffmpeg",
    mp3_bitrate="128k",
)

client = TestClient(create_app(settings))

# Caso 1: tone inválido
response = client.post("/api/v1/tts/synthesize", json={"text": "Hola", "tone": "angry"})
print(f"Tone inválido: {response.status_code} {response.json()}")
# Esperado: 422 {"error": {"code": "UNSUPPORTED_TONE", "message": "...", "retryable": false}}

# Caso 2: voice_profile inválido
response = client.post("/api/v1/tts/synthesize", json={"text": "Hola", "voice_profile": "robot"})
print(f"Voice profile inválido: {response.status_code} {response.json()}")
# Esperado: 422 {"error": {"code": "UNSUPPORTED_VOICE_PROFILE", "message": "...", "retryable": false}}

# Caso 3: text vacío
response = client.post("/api/v1/tts/synthesize", json={"text": ""})
print(f"Text vacío: {response.status_code} {response.json()}")
# Esperado: 422 {"error": {"code": "EMPTY_TEXT", "message": "...", "retryable": false}}
```

### Criterios de verificación del reviewer
El reviewer re-ejecutará:
1. `pytest -q` desde `framework/tts` → todas las pruebas deben pasar
2. Verificar que las pruebas nuevas validan el cuerpo completo de la respuesta
3. Verificar manualmente los tres casos de validación con TestClient
4. Confirmar que el formato de respuesta coincide con `TTSError` definido en `openapi_tts.json`

### Actualización del sprint
Una vez completadas las tareas C-001 y C-002:
- Marcar tareas originales como `verified` (excepto la que dependía de DEF-001)
- Marcar C-001 y C-002 como completadas
- Actualizar status a `completed` si todas las verificaciones pasan
- Añadir evidencias de las nuevas pruebas en la sección "Review"

### Notas importantes
- **No modificar** `docs/contracts/api/openapi_tts.json` salvo que sea estrictamente necesario y se confirme con el responsable del contrato
- **No implementar** fallback a otros proveedores (XTTS, Coqui, etc.)
- **Mantener** la exclusividad de Chatterbox como único proveedor
- Los errores de validación **no son retryable** (`retryable: false`)
- El handler debe manejar **todos** los errores de validación de Pydantic, no solo los tres casos específicos
