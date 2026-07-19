# ADR-015 — Headers de versión diferidos al módulo Avatar

# ─────────────────────────────────────────────

## Status

status:        accepted
date:          2026-07-18
superseded_by: —

## Context

El Sprint 002 definió que la respuesta de síntesis del TTS debe incluir headers de versión para cache safety:

- `X-Provider-Name`
- `X-Model-Version`
- `X-Voice-Profile-Version`
- `X-Audio-Format-Version`
- `X-Synthesis-Profile-Version`

Sin embargo, la implementación actual de `routes/tts.py` solo devuelve `Response(content=audio_bytes, media_type="audio/mpeg")` sin headers de versión.

Estos headers están diseñados para que el backend Avatar pueda generar cache keys versionadas. Pero el módulo Avatar aún no está implementado.

## Need — Necesidad de la familia y usuarios afectados

**Familia**: No necesita conocimiento técnico sobre headers de cache. La aplicación debe funcionar correctamente sin que los padres piensen en invalidación de cache.

**Niños (3-4 años)**: No son afectados directamente. El audio debe sonar igual sin importar los headers.

**Backend (Avatar)**: Necesita keys de cache que incluyan versión del modelo, proveedor, etc., para invalidar cache cuando cambie la configuración TTS.

## Alternatives considered

### Alternativa 1: Diferir headers hasta implementar Avatar (elegida)

- **Descripción**: No implementar headers de versión en `tts-educational` hasta que el módulo Avatar del backend los necesite.
- **Ventajas**: 
  - No se implementa funcionalidad que nadie consume aún.
  - Se mantiene la interfaz TTS simple mientras se desarrolla.
  - Si la estrategia de cache cambia, no se ha malgastado esfuerzo.
- **Desventajas**: 
  - Si Avatar se implementa antes de que se añadan los headers, habrá que volver al TTS.
  - Riesgo de olvidar implementarlos.
- **Compromiso**: Aceptable. Los headers son un detalle de integración que se puede añadir fácilmente.

### Alternativa 2: Implementar headers ahora (descartada)

- **Descripción**: Añadir todos los headers de versión a la respuesta de síntesis.
- **Razón de descarte**: 
  - Nadie consume estos headers aún.
  - La estrategia de cache del Avatar puede cambiar.
  - Añade complejidad innecesaria al TTS en este momento.

## Decision

**Los headers de versión se diferirán hasta que el módulo Avatar del backend los necesite.**

Consecuencias específicas:

1. **`routes/tts.py` no incluye headers de versión**: La respuesta sigue siendo `Response(content=audio_bytes, media_type="audio/mpeg")`.

2. **El contrato `openapi_tts.json` no documenta headers de versión**: Se mantiene como está.

3. **Cuando Avatar lo necesite**: Se añadirán los headers al endpoint de síntesis y se actualizará el contrato.

## Consequences

### Positive

- TTS más simple mientras se desarrolla.
- No se implementa funcionalidad que nadie consume.
- Flexibilidad para cambiar la estrategia de cache.

### Negative

- Riesgo de olvidar implementarlos cuando Avatar los necesite.
- Si Avatar se implementa primero, habrá que volver al TTS.

### Neutral

- El comportamiento del TTS no cambia para el niño ni para los padres.

## Impacto en experiencia infantil, parental, accesibilidad, seguridad infantil y privacidad

### Experiencia infantil
- **Sin cambio**: Los headers no afectan al audio que escucha el niño.

### Experiencia parental
- **Sin cambio**: Los padres no ven headers técnicos.

### Accesibilidad
- **Sin cambio**: El sistema de fallback a texto sigue funcionando.

### Seguridad infantil
- **Sin cambio**: Los headers no exponen información sensible.

### Privacidad
- **Sin cambio**: Los headers no contienen datos personales.

## Límites, exclusiones y preguntas abiertas

### Límites de la decisión
- Esta decisión es **reversible**: Se pueden añadir headers en cualquier momento.
- La decisión **no afecta** al contrato `openapi_tts.json` ni al comportamiento del TTS.

### Exclusiones
- No se define el formato exacto de los headers (nombres, valores).
- No se define cuándo exactamente se implementarán (depende del Avatar).

### Preguntas abiertas para responsables técnicos

1. **Backend (Avatar)**: ¿Qué headers de versión necesita el módulo Avatar para su estrategia de cache?

2. **Backend (Avatar)**: ¿Cuándo se implementará el módulo Avatar? Esto determina cuándo se deben añadir los headers al TTS.

3. **TTS**: ¿Se debe crear una FEAT específica para añadir headers de versión, o se incluye en la FEAT de Avatar?

## References

- ADR-012-Replain-tts-service.md (define los headers en la sección "Contract Impact")
- Sprint-002 Review (documenta los headers como implementados)
- `framework/tts/routes/tts.py` (implementación actual sin headers)
- `docs/contracts/api/openapi_tts.json` (contrato actual sin headers)
