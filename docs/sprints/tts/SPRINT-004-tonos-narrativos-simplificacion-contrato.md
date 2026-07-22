# Sprint 004 - TTS

## Goal
Implementar tonos narrativos (`tender`, `mysterious`), simplificar el contrato reemplazando `voice_profile` por `context`, y añadir validación de tonos por contexto conforme a FEAT-002 y ADR-016.

## Status
status: completed
started_at: 2026-07-22 00:00:00
closed_at: 2026-07-22 00:00:00
blocked_by:
waiting_for:

## Decisiones confirmadas (2026-07-22)

1. **Parámetros de prosodia**: ✅ Confirmados. Los valores propuestos para `tender` y `mysterious` se mantienen como punto de partida. Se ajustarán cuando se realicen pruebas reales con la familia.

2. **Formato del contexto**: ✅ Confirmado. El campo `context` con valores `"npc"` y `"narration"` es la forma elegida para indicar el contexto de uso.

3. **Breaking change**: ✅ Confirmado. El breaking change directo (sin compatibilidad hacia atrás) es aceptable dado que es una aplicación monofamiliar sin consumidores externos.

## Tasks
- [x] Actualizar `app/models.py`: eliminar `voice_profile`, agregar `context: Literal["npc", "narration"]`, ampliar `Tone` para incluir `"tender"` y `"mysterious"`. (verificado)
- [x] Actualizar `app/tone_mapping.py`: añadir mapeo de prosodia para `"tender"` y `"mysterious"`, implementar diccionario `CONTEXT_TONES` y función `validate_tone_for_context(tone, context)`. (verificado)
- [x] Actualizar `app/main.py`: llamar a `validate_tone_for_context` antes de procesar la petición, lanzar `TtsError` con código `TONE_CONTEXT_MISMATCH` si la validación falla. (verificado)
- [x] Actualizar `app/chatterbox.py`: reemplazar `request.voice_profile` por `request.context` en el mapeo de voz. (verificado)
- [x] Actualizar `docs/contracts/api/openapi_tts.json`: eliminar campo `voice_profile`, agregar campo `context` con enum `["npc", "narration"]`, actualizar enum `tone` para incluir `"tender"` y `"mysterious"`, agregar `"TONE_CONTEXT_MISMATCH"` al enum `ErrorCode`. (verificado)
- [x] Actualizar pruebas existentes: modificar todas las pruebas que usan `voice_profile` para que usen `context`, añadir pruebas parametrizadas para los 7 tonos. (verificado)
- [x] Añadir pruebas de validación de contexto: verificar que tonos narrativos (`tender`, `mysterious`) son rechazados en contexto `npc`, verificar que tonos de NPC (`playful`, `serious`) son rechazados en contexto `narration`. (verificado)
- [x] Actualizar `framework/tts/README.md`: documentar nuevos tonos, validación de contexto, código de error `TONE_CONTEXT_MISMATCH`, actualizar ejemplos de petición. (verificado)
- [x] Ejecutar `pytest -v` y verificar que todas las pruebas pasan (>= 50 pruebas esperadas). (verificado: 64 pruebas)
- [x] Construir imagen Docker con `docker build` y verificar que arranca correctamente. (verificado)

## Risks
- Los parámetros de prosodia de `tender` y `mysterious` son provisionales y requieren validación manual con la familia antes de fijar valores definitivos.
- El breaking change en el contrato (eliminación de `voice_profile`) requiere coordinación de despliegue con backend.
- La validación de tonos por contexto puede rechazar peticiones válidas si el backend envía tonos inapropiados, lo que es el comportamiento esperado pero debe documentarse claramente.

## Dependencies
- FEAT-002: Tonos narrativos y simplificación del contrato TTS.
- ADR-016: Tonos narrativos y simplificación del contrato TTS.
- ADR-013: Chatterbox como único proveedor TTS.
- ADR-005: Voice Reference Generation (identidad sonora del NPC).
- Sprint 001, 002, 003 completados.
- Backend debe actualizar llamadas a TTS para enviar `context` en lugar de `voice_profile` (sprint posterior).

## Agent Instruction
- Implementar exclusivamente los cambios en `framework/tts` y su documentación técnica.
- No modificar `docs/contracts/api/openapi_tts.json` salvo los cambios especificados en este sprint.
- No implementar fallback automático de tono ni inferencia de tono por IA.
- No modificar la identidad sonora del NPC (ADR-005).
- Mantener Chatterbox como único proveedor (ADR-013).
- Los parámetros de prosodia de `tender` y `mysterious` son provisionales hasta validación familiar.
- Coordinar con backend para despliegue conjunto (breaking change en contrato).
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- El campo `context` reemplaza a `voice_profile` para simplificar la comunicación entre backend y TTS.
- El servicio TTS elige internamente la voz correcta basándose en el contexto.
- La validación de tonos por contexto previene el uso inapropiado de tonos narrativos en el NPC y viceversa.
- Los tonos `tender` y `mysterious` solo están disponibles en contexto `narration`.
- Los tonos `playful` y `serious` solo están disponibles en contexto `npc`.
- Los tonos `calm`, `joyful` y `enthusiastic` están disponibles en ambos contextos.
- El breaking change es aceptable dado que es una aplicación monofamiliar sin consumidores externos.

## Review

### Developer implementation — Evidencias

(Implementación completada por developer-tts)

### Reviewer verification — APPROVED (2026-07-22)

Revisado por reviewer-tts independiente. Verificaciones re-ejecutadas:

**Evidencias independientes**:
- `pytest -v`: **64 passed** (0.46s) ✅
- `docker build -t tts-educational:sprint-004-review .`: **correcto** ✅
- Contenedor arrancado con `CHATTERBOX_BASE_URL=http://unreachable.invalid`:
  - `GET /health` → 200 `{"status":"ok"}` ✅
  - `GET /api/v1/tts/status` → 200 `{"provider":"chatterbox","model":"chatterbox","state":"ready"}` ✅
- Grep `voice_profile` en `framework/tts/app/**/*.py`: **0 resultados** ✅

**Validación del contrato**:
- JSON válido ✅
- `SynthesizeRequest`: campo `context` con enum `["npc", "narration"]` ✅
- `SynthesizeRequest`: campo `tone` con 7 valores ✅
- `SynthesizeRequest`: campo `voice_profile` **eliminado** ✅
- `ErrorCode`: 11 códigos incluyendo `TONE_CONTEXT_MISMATCH` ✅
- Correspondencia 1:1 entre códigos del contrato y códigos producidos por la implementación ✅

**Verificación manual de validación de contexto**:

| Caso | Contexto | Tono | Status | Código | Resultado |
|------|----------|------|--------|--------|-----------|
| 1 | npc | tender | 422 | TONE_CONTEXT_MISMATCH | ✅ Rechazado |
| 2 | npc | mysterious | 422 | TONE_CONTEXT_MISMATCH | ✅ Rechazado |
| 3 | narration | playful | 422 | TONE_CONTEXT_MISMATCH | ✅ Rechazado |
| 4 | narration | serious | 422 | TONE_CONTEXT_MISMATCH | ✅ Rechazado |
| 5 | npc | calm | 503 | PROVIDER_UNAVAILABLE | ✅ Válido (falla solo por provider) |
| 6 | narration | tender | 503 | PROVIDER_UNAVAILABLE | ✅ Válido (falla solo por provider) |

**Cobertura de pruebas verificada**:
- `test_config.py`: 6 pruebas (defaults, env vars, validaciones)
- `test_tone_mapping.py`: 17 pruebas (7 tonos, valores específicos, intensity, KeyError, context tones, validate_tone_for_context)
- `test_chatterbox.py`: 14 pruebas (npc/storyteller voice, 7 tonos parametrizados, timeout, connect error, 5xx, 4xx, empty)
- `test_audio.py`: 4 pruebas (conversión real, ffmpeg not found, failure, empty)
- `test_api.py`: 23 pruebas (health, status, synthesize, 7 tonos, context validation, errores contractuales, timeout, conversión, tone-context mismatch)

**Completitud del sprint**:
- ✅ Actualizar `app/models.py`: eliminar `voice_profile`, agregar `context`, ampliar `Tone`
- ✅ Actualizar `app/tone_mapping.py`: añadir mapeo `tender` y `mysterious`, implementar `CONTEXT_TONES` y `validate_tone_for_context`
- ✅ Actualizar `app/main.py`: llamar a `validate_tone_for_context` antes de procesar
- ✅ Actualizar `app/chatterbox.py`: reemplazar `request.voice_profile` por `request.context`
- ✅ Actualizar `docs/contracts/api/openapi_tts.json`: eliminar `voice_profile`, agregar `context`, actualizar enums
- ✅ Actualizar pruebas existentes y añadir pruebas de validación de contexto
- ✅ Actualizar `framework/tts/README.md`: documentar nuevos tonos, validación de contexto
- ✅ Ejecutar `pytest -v`: 64 pruebas pasando
- ✅ Construir imagen Docker y verificar arranque

**Breaking change coordinado**:
- Eliminación de `voice_profile` y reemplazo por `context` documentado en sprint
- Backend debe actualizar llamadas para enviar `context` en lugar de `voice_profile` (sprint posterior)
- Aplicación monofamiliar sin consumidores externos: breaking change aceptable

**Parámetros de prosodia provisionales**:
- `tender`: exaggeration=0.35, cfg_weight=0.30, temperature=0.75
- `mysterious`: exaggeration=0.45, cfg_weight=0.40, temperature=0.80
- Requieren validación manual con la familia antes de fijar valores definitivos

sprint_verdict: APPROVED (2026-07-22)

## Design decisions

### 1. Formato del indicador de contexto

**Decisión**: Usar campo `context` con valores `"npc"` y `"narration"`.

**Justificación**:
- Simple y semánticamente claro
- Refleja el propósito del indicador (contexto de uso)
- Consistente con la terminología del FEAT-002
- Escalable si en el futuro se añaden más contextos

### 2. Parámetros de prosodia para nuevos tonos

**Propuesta inicial** (sujeta a validación familiar):

```python
"tender": {
    "exaggeration": 0.35,    # Suave, cálido, cercano
    "cfg_weight": 0.30,      # Estabilidad media
    "temperature": 0.75      # Variación controlada
}
"mysterious": {
    "exaggeration": 0.45,    # Curiosidad moderada
    "cfg_weight": 0.40,      # Estabilidad media-alta
    "temperature": 0.80      # Variación media
}
```

**Criterios de diseño**:
- `tender`: Menor exaggeration que `calm` para transmitir calma y cercanía, sin monotonía
- `mysterious`: Exaggeration entre `calm` y `playful`, temperature ligeramente alta para variación pero sin sobreestimulación
- Ambos tonos deben funcionar correctamente con perfil `storyteller`
- Validación manual obligatoria con la familia antes de fijar valores definitivos

### 3. Comportamiento ante tono inapropiado para contexto

**Decisión**: Rechazo con error contractual, sin fallback.

**Justificación**:
- FEAT-002 dice explícitamente: "rechaza la petición con un error contractual claro"
- Fallback ocultaría errores de lógica en backend
- Backend mantiene control explícito sobre el tono (límite de IA)
- Consistente con filosofía de errores contractuales del Sprint 001/002

**Código de error**: `TONE_CONTEXT_MISMATCH`
- HTTP 422
- `retryable: false`
- Mensaje descriptivo: "El tono '{tone}' no es apropiado para el contexto '{context}'"

### 4. Compatibilidad de contrato y transición

**Decisión**: Breaking change directo con coordinación de despliegue.

**Justificación**:
- Aplicación monofamiliar (5-6 usuarios concurrentes)
- Backend y TTS se despliegan juntos en el mismo entorno
- No hay consumidores externos del servicio TTS
- Mantener ambos campos (`voice_profile` y `context`) añade complejidad innecesaria
- Coordinación de despliegue: actualizar TTS primero, luego backend inmediatamente después

## Contract changes

### SynthesizeRequest

**Antes**:
```json
{
  "text": "string",
  "locale": "string",
  "tone": "calm|joyful|enthusiastic|playful|serious",
  "emotion": "string|null",
  "intensity": "number|null",
  "voice_profile": "npc|storyteller"
}
```

**Después**:
```json
{
  "text": "string",
  "locale": "string",
  "context": "npc|narration",
  "tone": "calm|joyful|enthusiastic|playful|serious|tender|mysterious",
  "emotion": "string|null",
  "intensity": "number|null"
}
```

**Cambios**:
- Eliminar campo `voice_profile`
- Agregar campo `context` con enum `["npc", "narration"]`
- Actualizar enum `tone` para incluir `"tender"` y `"mysterious"`

### ErrorCode

**Antes**:
```json
"ErrorCode": {
  "enum": [
    "UNSUPPORTED_TONE",
    "UNSUPPORTED_VOICE_PROFILE",
    "EMPTY_TEXT",
    "MISSING_TEXT",
    "VALIDATION_ERROR",
    "PROVIDER_UNAVAILABLE",
    "PROVIDER_ERROR",
    "PROVIDER_VALIDATION_ERROR",
    "SYNTHESIS_TIMEOUT",
    "CONVERSION_ERROR"
  ]
}
```

**Después**:
```json
"ErrorCode": {
  "enum": [
    "UNSUPPORTED_TONE",
    "UNSUPPORTED_VOICE_PROFILE",
    "EMPTY_TEXT",
    "MISSING_TEXT",
    "VALIDATION_ERROR",
    "TONE_CONTEXT_MISMATCH",
    "PROVIDER_UNAVAILABLE",
    "PROVIDER_ERROR",
    "PROVIDER_VALIDATION_ERROR",
    "SYNTHESIS_TIMEOUT",
    "CONVERSION_ERROR"
  ]
}
```

**Cambios**:
- Agregar `"TONE_CONTEXT_MISMATCH"` al enum

### Ejemplos de peticiones

**Petición válida (NPC)**:
```json
POST /api/v1/tts/synthesize
{
  "text": "¡Hola! Soy Nubi",
  "locale": "es",
  "context": "npc",
  "tone": "joyful"
}
```

**Petición válida (Narración)**:
```json
POST /api/v1/tts/synthesize
{
  "text": "Había una vez un pequeño ratón",
  "locale": "es",
  "context": "narration",
  "tone": "mysterious",
  "intensity": 0.8
}
```

**Respuesta de error (tono inapropiado para contexto)**:
```json
HTTP 422
{
  "error": {
    "code": "TONE_CONTEXT_MISMATCH",
    "message": "Tone 'tender' not allowed for context 'npc'",
    "retryable": false
  }
}
```
