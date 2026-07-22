# Sprint 001 - TTS

## Goal
Crear desde cero el servicio TTS con Chatterbox como único proveedor y una imagen Docker reproducible, configurable para la conectividad proporcionada por infraestructura.

## Status
status: changes_required
started_at: 2026-07-21 00:00:00
closed_at:
blocked_by:
waiting_for: developer fix for DEF-001 (ver Reviewer verdict)

## Tasks
- [x] Crear la estructura inicial de `framework/tts` y su API Python conforme a las convenciones de la capa.
- [x] Implementar Chatterbox como único proveedor de síntesis, sin abstracciones ni configuración para proveedores alternativos o fallback automático.
- [x] Implementar configuración exclusiva de Chatterbox mediante variables de entorno.
- [x] Implementar los perfiles `npc` y `storyteller` y el mapeo de tonos aprobado para Chatterbox.
- [x] Implementar la respuesta MP3 y la conversión WAV a MP3 conforme a `docs/contracts/api/openapi_tts.json`.
- [x] Crear el `Dockerfile` de `framework/tts` con las dependencias de Python, servidor API y FFmpeg necesarias para la conversión de audio.
- [x] Configurar la URL de Chatterbox mediante variable de entorno; documentar `http://127.0.0.1:4123` exclusivamente como valor para ejecución de TTS en el host, no dentro del contenedor TTS.
- [x] Comprobar que la imagen Docker construye y que el servicio puede arrancar y exponer su endpoint de salud.

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

completed_tasks: Todas las tareas del sprint implementadas en `framework/tts`.

incomplete_tasks: La validación end-to-end con Chatterbox real queda pendiente de la URL alcanzable que debe entregar infraestructura; no bloquea la implementación ni el arranque del contenedor.

contract_changes: Ninguno. Se mantiene `docs/contracts/api/openapi_tts.json`. Hallazgo para el responsable del contrato: `StatusResponse` conserva enumeraciones históricas `xtts` y `xtts_v2`, pero la implementación publica exclusivamente `chatterbox`.

learnings: La URL de Chatterbox se consume exclusivamente desde `CHATTERBOX_BASE_URL`. El valor `http://127.0.0.1:4123` se documentó solo para TTS ejecutado en el host. La imagen incorpora FFmpeg para conversión WAV a MP3 en memoria y se ejecuta como usuario sin privilegios.

next_sprint_suggestions: Ejecutar Sprint 002 con los dobles unitarios existentes y una integración explícita cuando infraestructura entregue una ruta Chatterbox accesible desde la red del contenedor TTS.

### Evidencias de implementación

- `python -m pytest -q` desde `framework/tts`: **7 passed** (0.82 s). Incluye contrato de perfiles/tonos, errores controlados, conversión WAV a MP3 y estado Chatterbox único.
- `docker build -t tts-educational:sprint-001 .` desde `framework/tts`: **correcto**.
- Arranque reproducible: `docker run --rm -d --name tts-sprint-001-test -p 18080:8080 -e CHATTERBOX_BASE_URL=http://unreachable.invalid tts-educational:sprint-001` y `GET /health`: **200** `{"status":"ok"}`.
- Latencia medida: pruebas unitarias completas en 0.82 s. No se mide latencia de síntesis real sin Chatterbox accesible.
- Fallbacks: no implementados por ADR-013; timeout devuelve `504 SYNTHESIS_TIMEOUT`, proveedor inaccesible devuelve `503 PROVIDER_UNAVAILABLE` y no se intenta otro proveedor.

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
- status: open

**Nota de cobertura de pruebas**: `tests/test_api.py::test_contract_rejects_unknown_tone` solo comprueba `status_code == 422` y no el cuerpo de la respuesta, por lo que el suite verde no detectó este defecto. Al corregir DEF-001, ampliar esta prueba (y añadir casos para `voice_profile` y `text` vacío) para afirmar la forma `TTSError` completa.

**Veredicto**: `CHANGES_REQUIRED`. El resto del sprint (exclusividad Chatterbox, perfiles, mapeo de tonos, conversión MP3, Dockerfile, arranque y timeout/indisponibilidad de proveedor) está implementado y verificado con evidencia reproducible. Las tareas permanecen en `implemented`; ninguna se promueve a `verified` hasta corregir DEF-001 y ampliar la prueba de contrato de errores. No se escala al usuario: es un defecto técnico ordinario corregible por el developer.
