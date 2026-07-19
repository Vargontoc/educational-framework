# ADR-014 — Validación de texto y locales: responsabilidad del backend

# ─────────────────────────────────────────────

## Status

status:        accepted
date:          2026-07-18
superseded_by: —

## Context

Tras la revisión de coherencia de la capa TTS, surgió la pregunta sobre quién debe validar:

1. La longitud del texto enviado a síntesis (actualmente `tts_max_text_length = 300` en `config.py`).
2. Los locales soportados (actualmente `es` y `en` según el contrato `openapi_tts.json`).

El código actual de `tts-educational` tiene la configuración `tts_max_text_length` pero no la valida en el endpoint. El endpoint acepta cualquier texto y lo envía directamente al proveedor.

## Need — Necesidad de la familia y usuarios afectados

**Familia**: No necesita conocimiento técnico sobre límites de texto. La aplicación debe funcionar sin que los padres piensen en restricciones técnicas.

**Niños (3-4 años)**: Los textos que escuchan son cortos (refuerzo del NPC, cuentos). No deben recibir audio largo o incomprensible.

**Backend**: Necesita controlar qué textos envía al TTS para garantizar una buena experiencia infantil.

## Alternatives considered

### Alternativa 1: Backend valida texto y locale (elegida)

- **Descripción**: El backend (`api-educational`) valida la longitud del texto y el locale antes de enviarlo a `tts-educational`.
- **Ventajas**: 
  - Separación clara de responsabilidades: el backend conoce las reglas de negocio; el TTS es un servicio de síntesis.
  - El backend puede adaptar la validación según el contexto (NPC vs lectura).
  - El TTS se mantiene simple y reutilizable.
- **Desventajas**: 
  - Si el backend no valida, `tts-educational` enviará texto excesivo al proveedor.
- **Compromiso**: Aceptable. El backend es el único consumidor de `tts-educational`.

### Alternativa 2: `tts-educational` valida texto y locale (descartada)

- **Descripción**: `tts-educational` rechaza textos mayores a 300 caracteres con un 400, y locales no soportados con un 422.
- **Razón de descarte**: 
  - Mezcla responsabilidades de negocio (¿cuánto texto es adecuado para un niño?) con síntesis de voz.
  - El TTS no conoce el contexto (¿es un cuento largo o una frase corta del NPC?).
  - Dificulta cambiar límites sin modificar el TTS.

## Decision

**La validación de longitud de texto y locales soportados es responsabilidad exclusiva del backend.**

Consecuencias específicas:

1. **`tts-educational` no valida longitud de texto**: Acepta cualquier texto y lo envía al proveedor. Si el texto es demasiado largo, el proveedor puede fallar o devolver audio de baja calidad.

2. **`tts-educational` no valida locales**: Acepta cualquier string de locale y lo envía al proveedor. Si el locale no es soportado, el proveedor puede fallar.

3. **El backend debe validar antes de llamar al TTS**: 
   - Longitud máxima de texto según el contexto (NPC: ~100 caracteres, cuentos: ~300 caracteres).
   - Locale soportado (`es` o `en`).

4. **Los errores del proveedor se propagan**: Si el backend envía texto inválido, `tts-educational` devolverá un error del proveedor (500, 503, etc.), no un 400 o 422 de validación.

## Consequences

### Positive

- Separación clara de responsabilidades.
- El TTS se mantiene simple y reutilizable.
- El backend puede adaptar límites según el contexto sin cambiar el TTS.

### Negative

- Si el backend no valida, el TTS puede enviar texto excesivo al proveedor.
- Los errores de validación no son tan específicos (500 en lugar de 400).

### Neutral

- La configuración `tts_max_text_length` en `config.py` se mantiene como referencia, pero no se usa en validación.

## Impacto en experiencia infantil, parental, accesibilidad, seguridad infantil y privacidad

### Experiencia infantil
- **Protegida**: El backend garantiza que los textos son adecuados para la edad.

### Experiencia parental
- **Sin cambio**: Los padres no ven errores de validación técnica.

### Accesibilidad
- **Sin cambio**: El sistema de fallback a texto sigue funcionando.

### Seguridad infantil
- **Protegida**: El backend controla qué contenido llega al TTS.

### Privacidad
- **Sin cambio**: No se almacenan textos en el TTS.

## Límites, exclusiones y preguntas abiertas

### Límites de la decisión
- Esta decisión es **reversible**: Se puede añadir validación en `tts-educational` más adelante si se necesita.

### Exclusiones
- No se define el valor exacto de los límites de texto por contexto (NPC vs cuentos). Eso es responsabilidad del backend.
- No se define qué locales exactos soporta el backend.

### Preguntas abiertas para responsables técnicos

1. **Backend**: ¿Qué longitud máxima de texto debe validar el backend para cada contexto (NPC, cuentos, relajación)?

2. **Backend**: ¿Qué locales debe soportar el backend y cómo los valida?

3. **TTS**: ¿Se debe eliminar la variable `tts_max_text_length` de `config.py` para evitar confusión, o mantenerla como referencia?

## References

- ADR-012-Replain-tts-service.md
- ADR-013-Chatterbox-unico-proveedor-TTS.md
- FEAT-002-Contracts-API.md (define maxTextLength en el contrato)
- FEAT-008-TTS-Product-Requirements.md (requisitos de producto del TTS)
- `framework/tts/app/config.py` (contiene tts_max_text_length)
