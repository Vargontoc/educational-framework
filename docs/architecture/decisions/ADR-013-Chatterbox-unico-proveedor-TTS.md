# ADR-013 — Chatterbox como único proveedor TTS

# ─────────────────────────────────────────────

## Status

status:        accepted
date:          2026-07-18
superseded_by: —

## Context

El ADR-012 definió Chatterbox como proveedor principal y XTTS v2 como fallback. Tras revisar la implementación actual y las necesidades reales de la familia, se ha confirmado que:

- La aplicación es monofamiliar con concurrencia reducida (5-6 usuarios).
- Chatterbox es suficiente para las necesidades de síntesis de voz infantil.
- Mantener XTTS v2 como fallback añade complejidad operativa sin beneficio proporcional.
- La familia no necesita conmutación automática entre proveedores.

## Need — Necesidad de la familia y usuarios afectados

**Familia**: Necesita una solución TTS simple, fiable y mantenible. No necesita redundancy entre proveedores de síntesis de voz.

**Niños (3-4 años)**: Necesitan audio claro y comprensible para las secciones de juego (NPC) y lectura en familia. La fuente exacta del audio no les afecta.

**Padres/Madres**: Necesitan que el audio funcione. No necesitan configurar proveedores ni gestionar fallbacks.

## Alternatives consideradas

### Alternativa 1: Chatterbox como único proveedor (elegida)

- **Descripción**: Eliminar XTTS v2 del sistema. Chatterbox es el único proveedor de síntesis de voz.
- **Ventajas**: 
  - Reducción de complejidad operativa y de código.
  - Un solo contenedor Docker para gestionar.
  - Configuración más simple para la familia.
  - Menos superficie de error.
- **Desventajas**: 
  - Sin fallback automático si Chatterbox falla.
  - Si Chatterbox tiene problemas de calidad en español, no hay alternativa inmediata.
- **Compromiso**: Aceptable para el contexto monofamiliar. Si Chatterbox no funciona, se puede cambiar manualmente el proveedor más adelante.

### Alternativa 2: Chatterbox + XTTS como fallback (descartada)

- **Descripción**: Mantener XTTS v2 como fallback automático cuando Chatterbox falla.
- **Razón de descarte**: 
  - Añade complejidad de código (lógica de fallback, configuración dual).
  - Añade complejidad operativa (dos contenedores Docker, dos modelos en GPU).
  - Para una familia con 5-6 usuarios, el beneficio del fallback automático no justifica la complejidad.
  - Si Chatterbox falla, la familia puede pausar y reintentar manualmente.

## Decision

**Chatterbox será el único proveedor de síndesis de voz en `tts-educational`.**

Consecuencias específicas:

1. **Eliminar XTTS del sistema**:
   - Eliminar `app/adapters/xtts.py`
   - Eliminar referencias a XTTS en `app/adapters/factory.py`
   - Eliminar configuración de Coqui/XTTS de `app/config.py`
   - Eliminar variables `COQUI_BASE_URL` y `COQUI_SYNTHESIS_ENDPOINT` de `envs/.env`
   - Eliminar mapeo XTTS de `app/mappings/tone_mapping.py`

2. **Eliminar documentación XTTS**:
   - Eliminar `docs/product/features/tts/FEAT-007-XTTS-Integration.md`
   - Eliminar `sprints/current.md` (Sprint 007 de XTTS)
   - Actualizar ADR-012 para reflejar que Chatterbox es el único proveedor

3. **Simplificar configuración**:
   - `TTS_PROVIDER` siempre será `chatterbox`
   - Eliminar `TTS_ENABLE_FALLBACK` y `TTS_FALLBACK_PROVIDER`
   - Mantener `CHATTERBOX_BASE_URL` como la única URL de proveedor

4. **Validación de texto y locales**:
   - La validación de longitud de texto (`tts_max_text_length`) y locales soportados es responsabilidad del backend.
   - `tts-educational` no valida estas restricciones; simplemente pasa el texto al proveedor.

## Consequences

### Positive

- Código más simple y mantenible.
- Menos contenedores Docker que gestionar.
- Configuración más simple para la familia.
- Menos superficie de error.
- Eliminación de dependencia de Coqui/XTTS.

### Negative

- Sin fallback automático si Chatterbox falla.
- Si Chatterbox tiene problemas de calidad, no hay alternativa automática.

### Neutral

- El contrato `openapi_tts.json` no cambia (ya es provider-agnostic).
- Los adaptadores de Chatterbox no cambian.
- El sistema de mapeo de tonos se simplifica (solo Chatterbox).

## Impacto en experiencia infantil, parental, accesibilidad, seguridad infantil y privacidad

### Experiencia infantil
- **Sin cambio**: Los niños seguirán recibiendo audio de Chatterbox para el NPC y la lectura.
- **Riesgo**: Si Chatterbox falla, el juego continúa sin audio (comportamiento ya definido).

### Experiencia parental
- **Mejora**: Menos configuración que gestionar. Un solo proveedor = menos decisiones técnicas.
- **Riesgo**: Si Chatterbox falla, los padres deben pausar y reintentar manualmente.

### Accesibilidad
- **Sin cambio**: El sistema de fallback a texto (cuando no hay audio) sigue funcionando.

### Seguridad infantil
- **Sin cambio**: Chatterbox sigue siendo un proveedor on-premise, sin envío de datos a servicios externos.

### Privacidad
- **Sin cambio**: Los textos sintetizados no se almacenan ni se envían a servicios externos.

## Límites, exclusiones y preguntas abiertas

### Límites de la decisión
- Esta decisión es **reversible**: Si en el futuro se necesita XTTS, se puede reintegrar siguiendo el ADR-012 original.
- La decisión **no afecta** al contrato `openapi_tts.json` ni a la interfaz del backend.

### Exclusiones
- No se elimina la posibilidad futura de añadir otros proveedores.
- No se cambia la arquitectura de capas definida en ADR-012.

### Preguntas abiertas para responsables técnicos

1. **Infraestructura**: ¿Se debe eliminar el servicio `coqui-educational` de Docker Compose, o mantenerlo comentado para referencia futura?

2. **Testing**: ¿Los tests existentes de XTTS (`test_xtts_adapter.py`) se eliminan o se mantienen como referencia?

3. **Documentación**: ¿Se debe actualizar el README del proyecto para eliminar referencias a XTTS/Coqui?

## References

- ADR-012-Replain-tts-service.md (decisión original de arquitectura TTS)
- FEAT-006-Chatterbox-Integration.md (integración de Chatterbox)
- FEAT-007-XTTS-Integration.md (a eliminar)
- Sprint-007 (a eliminar o archivar)
