# ADR-016 — Tonos narrativos y simplificación del contrato TTS

## Status

status: accepted
date: 2026-07-22
superseded_by: —

## Contexto

El servicio TTS actual soporta dos perfiles de voz (npc y storyteller) y cinco tonos semánticos (calm, joyful, enthusiastic, playful, serious). La experiencia de lectura en familia requiere mayor expresividad emocional en la narración de cuentos para mantener el interés de niños de 3-4 años.

Además, el contrato actual requiere que el backend envíe explícitamente el perfil de voz (npc/storyteller), lo que añade complejidad innecesaria en la comunicación entre capas. El servicio TTS ya conoce internamente qué voz corresponde a cada perfil, por lo que esta información podría simplificarse.

### Problema

1. Los tonos actuales son suficientes para el NPC del juego, pero la narración de cuentos necesita mayor variedad emocional para crear una experiencia envolvente y apropiada para la edad.
2. El backend no debería preocuparse por los perfiles de voz internos del servicio TTS; solo necesita indicar el contexto de uso (juego o narración).

### Restricciones

- Aplicación monofamiliar para niños de 3-4 años
- Chatterbox como único proveedor TTS (ADR-013)
- Los tonos deben ser apropiados para la edad, sin generar miedo, ansiedad o sobreestimulación
- El NPC mantiene su identidad sonora de robot kawaii (ADR-005)

## Decisión

Se confirman las siguientes decisiones de producto:

### 1. Ampliación del catálogo de tonos narrativos

Se agregan dos nuevos tonos semánticos específicos para la narración de cuentos:

- **tierno**: Para momentos de afecto, cercanía y calma emocional en los cuentos
- **misterioso**: Para momentos de intriga suave y curiosidad, sin generar miedo ni ansiedad

Los tonos existentes (calm, joyful, enthusiastic) se mantienen para narración. Los tonos del NPC (playful, serious) no cambian.

### 2. Simplificación del contrato de comunicación

El backend deja de enviar el perfil de voz explícito (`npc`/`storyteller`) y en su lugar envía un indicador de contexto más simple. El servicio TTS es responsable de elegir internamente la voz correcta basándose en este contexto.

### 3. Validación de tonos por contexto

El servicio TTS valida que los tonos sean apropiados para el contexto indicado:
- Contexto NPC: solo permite tonos del NPC (calm, joyful, enthusiastic, playful, serious)
- Contexto narración: permite tonos narrativos (calm, joyful, enthusiastic, tierno, misterioso)

Si el backend solicita un tono inapropiado para el contexto, el servicio TTS rechaza la petición con un error contractual.

## Consecuencias

### Positivas

- **Experiencia infantil enriquecida**: La narración de cuentos tiene mayor variedad emocional, manteniendo el interés del niño sin sobreestimulación
- **Simplificación del backend**: El backend no necesita conocer los perfiles de voz internos del servicio TTS, solo el contexto de uso
- **Validación centralizada**: El servicio TTS garantiza que los tonos sean apropiados para cada contexto, reduciendo riesgo de uso inapropiado
- **Cohéncia de experiencia**: El NPC mantiene su identidad sonora actual, mientras la narración tiene su propio conjunto de tonos

### Negativas

- **Complejidad adicional en el servicio TTS**: Debe implementar validación de tonos por contexto y mapeo de nuevos tonos a parámetros de Chatterbox
- **Riesgo de tono inapropiado**: Si los parámetros de `misterioso` no se calibran correctamente, podría generar intriga excesiva o miedo en niños de 3-4 años
- **Cambio de contrato existente**: Requiere actualización coordinada entre backend y servicio TTS

### Neutrales

- El backend mantiene control sobre qué tono usar en cada momento
- Los responsables técnicos deben definir los parámetros exactos de prosodia para los nuevos tonos
- La comunicación entre capas cambia, pero la funcionalidad observable para el usuario final es la misma

## Alternativas consideradas

### Alternativa A: Extensión mínima con simplificación de contrato

**Descripción**: Agregar tonos narrativos y simplificar contrato, pero sin validación de tonos por contexto. El backend es responsable de enviar tonos apropiados.

**Razón de rechazo**: No aprovecha la oportunidad de centralizar la validación en el servicio TTS, lo que aumenta el riesgo de uso inapropiado de tonos narrativos en el NPC.

### Alternativa B: Simplificación completa con inferencia de tono

**Descripción**: El backend solo envía el contexto, y el servicio TTS infiere automáticamente el tono apropiado basándose en el texto o el contexto.

**Razón de rechazo**: El backend pierde control sobre el tono específico, lo que limita la expresividad en escenarios del juego donde se necesitan reacciones variadas (ej: refuerzo positivo vs. pista suave).

## Preguntas abiertas para responsables técnicos

1. **Contrato de comunicación**: ¿Cuál es la forma más clara y escalable de indicar el contexto (npc/narration) en el contrato simplificado?

2. **Parámetros de prosodia**: ¿Cómo se traducen los tonos `tierno` y `misterioso` a parámetros de Chatterbox (exaggeration, cfg_weight, temperature) apropiados para niños de 3-4 años?

3. **Validación de contexto**: ¿Cómo debe comportarse el servicio TTS cuando recibe un tono inapropiado para el contexto? ¿Rechazo con error o fallback a tono por defecto?

4. **Compatibilidad de contrato**: ¿Cómo gestionar la transición del contrato actual al nuevo sin romper integraciones existentes durante el despliegue?

## Referencias

- ADR-005: Voice Reference Generation (identidad sonora del NPC)
- ADR-013: Chatterbox como único proveedor TTS
- README.md: Secciones TTS Service y lectura en familia
- framework/tts/README.md: Estado actual del servicio TTS
