# FEAT-002 — Tonos narrativos y simplificación del contrato TTS

## Status

state: accepted
user_history:
depends_on: ADR-016
owned_by: tts, backend
test:

## Descripción

### Objetivo y valor para la familia

La narración de cuentos en la experiencia de lectura en familia necesita mayor expresividad emocional para mantener el interés de niños de 3-4 años. Los tonos actuales del servicio TTS son suficientes para el NPC del juego, pero la narración requiere dos tonos adicionales: **tierno** (para momentos de afecto y cercanía) y **misterioso** (para intriga suave y curiosidad, sin miedo).

Además, la comunicación entre el backend y el servicio TTS debe simplificarse para que el backend no necesite conocer los perfiles de voz internos. El backend indica el contexto de uso (juego o narración), y el servicio TTS elige la voz correcta internamente.

### Actores y escenarios de uso

**Niño (3-4 años) en lectura en familia:**
- Escucha un cuento narrado con variedad emocional: momentos tranquilos, alegres, emocionantes, tiernos y de intriga suave
- La voz del narrador adapta el tono al contexto del cuento, creando una experiencia envolvente y apropiada para su edad
- No percibe cambios bruscos ni tonos que generen miedo o ansiedad

**Backend (sistema):**
- Solicita síntesis de voz indicando el contexto (juego o narración) y el tono deseado
- No necesita conocer los perfiles de voz internos del servicio TTS
- Recibe error si solicita un tono inapropiado para el contexto

**Servicio TTS (sistema):**
- Recibe petición con contexto y tono
- Elige internamente la voz correcta basándose en el contexto
- Valida que el tono sea apropiado para el contexto
- Genera audio con los parámetros de prosodia correspondientes al tono

### Requisitos funcionales

#### RF-001: Nuevos tonos narrativos

El servicio TTS debe soportar dos nuevos tonos semánticos para la narración de cuentos:

- **tierno**: Para momentos de afecto, cercanía y calma emocional
- **misterioso**: Para momentos de intriga suave y curiosidad, sin generar miedo ni ansiedad

Los tonos deben ser apropiados para niños de 3-4 años y funcionar correctamente con el perfil de voz storyteller.

#### RF-002: Catálogo de tonos por contexto

El servicio TTS debe validar que los tonos sean apropiados para el contexto indicado:

- **Contexto NPC** (juego): Permite tonos `calm`, `joyful`, `enthusiastic`, `playful`, `serious`
- **Contexto narración** (cuentos): Permite tonos `calm`, `joyful`, `enthusiastic`, `tierno`, `misterioso`

Si el backend solicita un tono inapropiado para el contexto, el servicio TTS debe rechazar la petición con un error contractual claro.

#### RF-003: Simplificación del contrato

El contrato de comunicación entre backend y servicio TTS debe simplificarse:

- El backend no envía el perfil de voz explícito (`npc`/`storyteller`)
- El backend envía un indicador de contexto que el servicio TTS interpreta internamente
- El servicio TTS elige la voz correcta basándose en el contexto

El resto del contrato (texto, locale, tono, intensidad) se mantiene sin cambios.

#### RF-004: Compatibilidad con identidad sonora del NPC

El NPC del juego mantiene su identidad sonora de robot kawaii (según ADR-005). Los tonos del NPC no cambian y no incluyen los nuevos tonos narrativos (`tierno`, `misterioso`).

### Requisitos no funcionales

#### RNF-001: Adecuación a la edad

Los nuevos tonos deben ser apropiados para niños de 3-4 años:
- `tierno`: Debe transmitir cercanía y calma, sin ser excesivamente lento o monótono
- `misterioso`: Debe transmitir curiosidad e intriga suave, nunca miedo, ansiedad o suspense intenso

#### RNF-002: Consistencia de experiencia

La transición entre tonos dentro de un mismo contexto debe ser suave y coherente. No debe haber cambios bruscos de prosodia que puedan confundir al niño.

#### RNF-003: Rendimiento

La adición de nuevos tonos no debe degradar el rendimiento del servicio TTS. Los tiempos de respuesta deben mantenerse dentro de los límites actuales.

### Criterios de aceptación verificables

#### CA-001: Tonos narrativos disponibles

- [ ] El servicio TTS acepta y procesa correctamente los tonos `tierno` y `misterioso` en contexto de narración
- [ ] Los parámetros de prosodia de los nuevos tonos son apropiados para niños de 3-4 años (validación manual con la familia)
- [ ] La narración de cuentos puede usar los 5 tonos narrativos: `calm`, `joyful`, `enthusiastic`, `tierno`, `misterioso`

#### CA-002: Validación de contexto

- [ ] El servicio TTS rechaza con error contractual si se solicita un tono narrativo (`tierno`, `misterioso`) en contexto NPC
- [ ] El servicio TTS rechaza con error contractual si se solicita un tono de NPC (`playful`, `serious`) en contexto narración
- [ ] El mensaje de error indica claramente que el tono no es apropiado para el contexto

#### CA-003: Contrato simplificado

- [ ] El backend no envía el perfil de voz explícito en las peticiones al servicio TTS
- [ ] El backend envía un indicador de contexto (npc/narration) que el servicio TTS interpreta correctamente
- [ ] El servicio TTS elige la voz correcta internamente basándose en el contexto
- [ ] La funcionalidad observable para el usuario final no cambia

#### CA-004: Identidad sonora del NPC

- [ ] El NPC del juego sigue usando los tonos actuales: `calm`, `joyful`, `enthusiastic`, `playful`, `serious`
- [ ] La voz del NPC mantiene su identidad de robot kawaii (según ADR-005)
- [ ] No hay cambios en la experiencia del niño en el juego

### Ámbitos que deben validar los responsables

#### Servicio TTS

- Definir los parámetros de prosodia exactos (exaggeration, cfg_weight, temperature) para los tonos `tierno` y `misterioso`
- Implementar la validación de tonos por contexto
- Implementar el mapeo interno de contexto a perfil de voz
- Definir el formato del indicador de contexto en el contrato simplificado
- Gestionar la transición del contrato actual al nuevo sin romper integraciones existentes

#### Backend

- Actualizar las llamadas al servicio TTS para enviar el indicador de contexto en lugar del perfil de voz
- Mantener el control sobre qué tono usar en cada escenario
- Gestionar los errores contractuales de validación de tono por contexto

#### Contenido

- Validar que los nuevos tonos son apropiados para la narración de cuentos infantiles
- Definir en qué momentos del cuento se usan los tonos `tierno` y `misterioso`

#### Familia (validación manual)

- Escuchar muestras de los nuevos tonos y confirmar que son apropiados para niños de 3-4 años
- Confirmar que `misterioso` transmite curiosidad sin generar miedo

### Privacidad, seguridad infantil, accesibilidad y límites de IA

#### Privacidad

- No se recopilan datos adicionales de los menores
- Los tonos no afectan a la privacidad de la información familiar

#### Seguridad infantil

- Los tonos `tierno` y `misterioso` deben ser apropiados para niños de 3-4 años
- `misterioso` no debe generar miedo, ansiedad o suspense intenso
- La validación de contexto previene el uso inapropiado de tonos narrativos en el NPC

#### Accesibilidad

- Los nuevos tonos deben funcionar correctamente en dispositivos con audio reducido
- Los tonos no deben depender exclusivamente de matices sutiles que puedan perderse en entornos ruidosos

#### Límites de IA

- No se implementa inferencia automática de tono por IA
- El backend mantiene control explícito sobre el tono a usar
- El servicio TTS solo valida y mapea, no decide el tono

### Exclusiones

- No se modifica la identidad sonora base del NPC (ADR-005)
- No se implementa inferencia automática de tono por IA
- No se permiten tonos que generen miedo, suspense intenso o ansiedad
- No se cambia el proveedor TTS (Chatterbox único según ADR-013)
- No se agregan más tonos más allá de `tierno` y `misterioso` en esta iteración

### Riesgos

#### Riesgo 1: Tono misterioso inapropiado

**Descripción**: Los parámetros de `misterioso` pueden generar intriga excesiva o miedo en niños de 3-4 años.

**Mitigación**: Validación manual con la familia antes de fijar los parámetros definitivos. Si el tono no es apropiado, se descarta o se ajusta.

#### Riesgo 2: Transición de contrato

**Descripción**: El cambio de contrato puede romper integraciones existentes entre backend y servicio TTS.

**Mitigación**: Coordinación entre responsables de backend y TTS para gestionar la transición de forma coordinada.

#### Riesgo 3: Complejidad de validación

**Descripción**: La validación de tonos por contexto puede añadir complejidad innecesaria al servicio TTS.

**Mitigación**: Mantener la lógica de validación simple y clara. Documentar los tonos permitidos por contexto.

### Supuestos

- Los responsables técnicos pueden definir parámetros de prosodia apropiados para los nuevos tonos
- La familia puede validar manualmente los nuevos tonos antes de su despliegue
- El cambio de contrato es reversible si surgen problemas durante la transición

### Decisiones pendientes

- Los responsables técnicos deben definir el formato exacto del indicador de contexto en el contrato simplificado
- Los responsables técnicos deben definir los parámetros de prosodia exactos para `tierno` y `misterioso`
- La familia debe validar que los nuevos tonos son apropiados para niños de 3-4 años

## Referencias

- ADR-016: Tonos narrativos y simplificación del contrato TTS
- ADR-005: Voice Reference Generation (identidad sonora del NPC)
- ADR-013: Chatterbox como único proveedor TTS
- FEAT-001: Chatterbox como único proveedor TTS (especificación funcional base)
- README.md: Secciones TTS Service y lectura en familia
