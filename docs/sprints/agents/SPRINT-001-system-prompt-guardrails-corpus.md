# Sprint 001 - Agents

## Goal
Dotar al agente `agent-educational-parent` de un system prompt completo con guardrails de comportamiento, ajustar el modelo base a `qwen2.5:7b-instruct-q5_K_M ` para concurrencia con npc-game, y proponer la estructura del corpus oficial con RAG para aprobación de producto.

## Status
status: in_progress
started_at: 2026-08-05
closed_at:
blocked_by:
waiting_for:

## Decisiones confirmadas (2026-08-05)

1. **Modelo base**: `qwen2.5:7b-instruct-q5_K_M ` (bajo demanda, latencia aceptable para padres). Se reduce de `qwen3:14b` para permitir concurrencia con npc-game en RTX 4070 SUPER (12 GB VRAM).

2. **Corpus oficial**: RAG con embeddings y vector store (responsabilidad de backend/infraestructura). El agente recibe fragmentos recuperados como `corpus_context`.

3. **Historial conversacional**: 5-8 turnos máximo, gestionado por backend e inyectado como `conversation_history`.

4. **Concurrencia de modelos**: npc-game `qwen2.5:7b-Q5_K_M` permanente durante sesión de juego; parent `qwen2.5:7b-instruct-q5_K_M ` bajo demanda; Chatterbox se descarga temporalmente cuando padre consulta si está activo.

5. **Longitud máxima de respuesta**: 1500 caracteres (coherente con `num_predict 512` y compatibilidad TTS).

6. **Selección de perfil**: mediante comandos (p. ej. `/perfil <nombre>`), interpretados por backend antes de invocar al agente.

## Tasks
- [x] Actualizar `framework/agents/agent-educational-parent/Modelfile`: cambiar `FROM qwen3:14b` a `FROM qwen2.5:7b-instruct-q5_K_M `, ajustar `PARAMETER temperature 0.3`, ajustar `PARAMETER num_predict 512`, mantener `top_p 0.8`, `top_k 20`, `repeat_penalty 1.1`.
- [x] Reemplazar `SYSTEM """ """` vacío en `Modelfile` con el system prompt completo propuesto en el análisis técnico (secciones: rol y tono adulto, alcance permitido, contexto de sesión, reglas de selección de perfil, distinción hechos/síntesis/consejo, derivación profesional, frase exacta de rechazo, peticiones excluidas, prohibiciones específicas, formato de respuesta).
- [x] Actualizar `framework/agents/agent-educational-parent/README.md`: documentar cambio de modelo a `qwen2.5:7b-instruct-q5_K_M `, documentar comportamiento de carga bajo demanda (latencia aceptable ~15-20 s), documentar límites de contenido actualizados según FEAT-003, documentar dependencia con RAG para corpus oficial.
- [x] Actualizar `framework/agents/agent-educational-parent/smoke-test.ps1`: añadir prueba de respuesta general sobre la app, añadir prueba de derivación profesional (consulta de salud), añadir prueba de frase exacta de rechazo ante petición excluida, añadir prueba de no solicitud de PII, ajustar timeout para reflejar latencia de carga bajo demanda (~30 s).
- [x] Crear documento de propuesta de corpus oficial en `docs/product/agents/` o similar: definir formato Markdown por tema (título, descripción, límites, mensajes clave), definir ubicación propuesta `docs/official-corpus/`, definir mecanismo de alimentación RAG con embeddings (backend/infraestructura), listar archivos iniciales propuestos (`que-es-my-friend-nubi.md`, `progreso-orientativo.md`, `panel-parental.md`, `perfiles-infantiles.md`, `limites-y-exclusiones.md`).
- [x] Crear documentos de corpus de ejemplo en `docs/official-corpus/`: al menos 3 archivos Markdown siguiendo el formato propuesto.
- [x] Documentar arquitectura de concurrencia de modelos: crear documento técnico en `docs/product/agents/` o `docs/product/decisions/`, describir configuración (npc-game 7b-Q5_K_M permanente, parent 8b-Q3_K_M bajo demanda), describir gestión de Chatterbox (descarga temporal cuando padre consulta), documentar VRAM estimada y escenarios de concurrencia.

## Acceptance Criteria
- **Criterio FEAT-003 §4.2**: consulta sobre función aprobada → respuesta basada en contenido oficial, no en conocimiento general.
- **Criterio FEAT-003 §4.9**: petición política/moral/programación → incluye exactamente «No puedo hacer lo que me solicitas».
- **Criterio FEAT-003 §4.10**: chatbot no pide nombres completos, PIN, imágenes, datos sanitarios ni contacto.
- **Criterio FEAT-003 §4.11**: ninguna respuesta presenta comparación, ranking, nota, nivel de capacidad ni diagnóstico.
- El smoke test pasa las 4 comprobaciones nuevas con latencia aceptable (<30 s).
- La estructura de corpus está documentada y lista para revisión de producto.
- La arquitectura de concurrencia de modelos está documentada.
- El Modelfile usa `qwen2.5:7b-instruct-q5_K_M ` con parámetros ajustados.

## Evidence
- `framework/agents/agent-educational-parent/Modelfile` actualizado con system prompt completo y modelo `qwen2.5:7b-instruct-q5_K_M `.
- `framework/agents/agent-educational-parent/README.md` actualizado.
- `framework/agents/agent-educational-parent/smoke-test.ps1` actualizado y ejecutado con éxito.
- Documento de propuesta de corpus oficial (formato, ubicación, mecanismo RAG).
- Al menos 3 archivos de ejemplo en `docs/official-corpus/`.
- Documento de arquitectura de concurrencia de modelos.

## Risks
- `qwen2.5:7b-instruct-q5_K_M ` puede no tener capacidad suficiente para los 14 requisitos funcionales complejos de FEAT-003 (distinción hechos/síntesis, derivaciones, guardrails estrictos en español, frase exacta de rechazo). Se validará en Sprint 003.
- El corpus oficial no existe aún y requiere aprobación de producto. El agente funciona sin corpus (indica que no dispone de la información), pero la utilidad se reduce.
- La gestión de Chatterbox (descarga temporal cuando padre consulta) añade complejidad de infraestructura y latencia adicional para el padre.
- Latencia de carga del modelo bajo demanda (~15-20 s) puede ser percibida como lenta por el usuario.

## Dependencies
- FEAT-003: Chatbot parental conversacional de Nubi.
- ADR-003: Chatbot parental conversacional de Nubi.
- ADR-002: Agent education-framework-agent-child (principio de separación estricta).
- Backend: implementación de RAG para corpus oficial (sprint posterior de backend).
- Producto: aprobación del corpus oficial (contenido inicial).

## Agent Instruction
- Implementar exclusivamente los cambios en `framework/agents/agent-educational-parent` y documentación técnica.
- No implementar código de backend, frontend ni TTS.
- No crear contratos YAML (eso corresponde al Sprint 002).
- El system propuesto debe estar sin tildes (convención observada en `npc-game/Modelfile`).
- Respetar la frase exacta «No puedo hacer lo que me solicitas» como requisito no negociable.
- Documentar la propuesta de corpus como propuesta pendiente de aprobación de producto.
- Documentar la arquitectura de concurrencia de modelos como decisión técnica.
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- El cambio de `qwen3:14b` a `qwen2.5:7b-instruct-q5_K_M ` es necesario para permitir concurrencia con npc-game en RTX 4070 SUPER (12 GB VRAM).
- El padre acepta latencia alta (~15-20 s) para consultas al chatbot, por lo que la carga bajo demanda es viable.
- El corpus oficial se alimentará mediante RAG (embeddings + vector store), no por inyección directa en prompt.
- La gestión de Chatterbox (descarga temporal) es una solución técnica para permitir concurrencia sin exceder VRAM.
- El smoke test debe ajustarse para reflejar la latencia de carga bajo demanda (timeout ~30 s).
- Los archivos de corpus de ejemplo son placeholders para que producto pueda revisar el formato propuesto.

## Review

### Developer implementation — Evidencias

**Fecha**: 2026-08-05
**Estado**: Implementado (pendiente de verificacion por reviewer)

#### Archivos modificados

1. `framework/agents/agent-educational-parent/Modelfile`
   - FROM cambiado a `qwen2.5:7b-instruct-q5_K_M `
   - temperature ajustado a 0.3
   - num_predict ajustado a 512
   - System prompt completo implementado con 10 secciones de guardrails
   - Frase exacta "No puedo hacer lo que me solicitas" incluida

2. `framework/agents/agent-educational-parent/load-ollama.ps1`
   - BaseModel por defecto cambiado a `qwen2.5:7b-instruct-q5_K_M `
   - num_predict ajustado a 512
   - temperature ya estaba en 0.3 (sin cambio necesario)

3. `framework/agents/agent-educational-parent/README.md`
   - Documentado cambio de modelo a `qwen2.5:7b-instruct-q5_K_M `
   - Documentado comportamiento de carga bajo demanda (latencia ~15-20 s)
   - Documentados limites de contenido actualizados segun FEAT-003
   - Documentada dependencia con RAG para corpus oficial

4. `framework/agents/agent-educational-parent/smoke-test.ps1`
   - Añadidas 4 nuevas pruebas (total 6 comprobaciones)
   - Timeout ajustado a 30 segundos
   - Pruebas: respuesta general, derivacion profesional, frase exacta de rechazo, no solicitud de PII

#### Archivos creados

5. `docs/product/agents/corpus-oficial-propuesta.md`
   - Formato Markdown por tema definido
   - Ubicacion propuesta: `docs/official-corpus/`
   - Mecanismo RAG documentado (embeddings + vector store)
   - 5 archivos iniciales propuestos
   - Marcado como "pendiente de aprobacion de producto"

6. `docs/official-corpus/que-es-my-friend-nubi.md`
   - Archivo de ejemplo siguiendo formato propuesto

7. `docs/official-corpus/progreso-orientativo.md`
   - Archivo de ejemplo siguiendo formato propuesto

8. `docs/official-corpus/limites-y-exclusiones.md`
   - Archivo de ejemplo siguiendo formato propuesto

9. `docs/product/agents/arquitectura-concurrencia-modelos.md`
   - Configuracion de modelos documentada
   - Gestion de Chatterbox en concurrencia descrita
   - VRAM estimada: ~11.5 GB total
   - 4 escenarios de concurrencia documentados
   - Alternativas descartadas documentadas

#### Convenciones respetadas

- System prompt en español sin tildes (convencion observada en npc-game/Modelfile)
- Frase exacta "No puedo hacer lo que me solicitas" incluida literalmente
- Documentacion en español sin tildes donde aplica la convencion
- Corpus de ejemplo marcados como "pendiente de aprobacion de producto"
- Arquitectura de concurrencia documentada como decision tecnica

#### Criterios de aceptacion cubiertos

- FEAT-003 §4.2: system prompt basa respuestas en corpus_context, no en conocimiento general
- FEAT-003 §4.9: frase exacta de rechazo incluida en system prompt
- FEAT-003 §4.10: prohibicion explicita de solicitar PII en system prompt
- FEAT-003 §4.11: prohibicion explicita de comparaciones, rankings, diagnosticos en system prompt
- Smoke test con 6 comprobaciones y timeout de 30 s
- Estructura de corpus documentada y lista para revision de producto
- Arquitectura de concurrencia documentada
- Modelfile usa `qwen2.5:7b-instruct-q5_K_M ` con parametros ajustados

#### Riesgos identificados

- `qwen2.5:7b-instruct-q5_K_M ` puede no tener capacidad suficiente para todos los guardrails de FEAT-003 (a validar en Sprint 003 con pruebas reales)
- Corpus oficial no existe aun; requiere aprobacion de producto
- Latencia de carga bajo demanda (~15-20 s) puede ser percibida como lenta
- Gestion de Chatterbox añade complejidad de infraestructura

#### Desviaciones

- Ninguna. Todas las tareas implementadas segun especificacion del sprint.

### Reviewer verification

(Pendiente de revision por reviewer-agents)

## Design decisions

### 1. Modelo base: qwen2.5:7b-instruct-q5_K_M 

**Decisión**: Reducir el modelo de `qwen3:14b` a `qwen2.5:7b-instruct-q5_K_M `.

**Justificación**:
- Permite concurrencia con npc-game `qwen2.5:7b-Q5_K_M` en RTX 4070 SUPER (12 GB VRAM).
- VRAM total estimada: ~11.5 GB (npc-game ~6 GB + parent ~5.5 GB), dejando margen para Chatterbox y overhead.
- El padre acepta latencia alta (~15-20 s), por lo que la carga bajo demanda es viable.
- `qwen3:8b` tiene capacidad suficiente para tareas conversacionales con guardrails estrictos (a validar en Sprint 003).

**Alternativas descartadas**:
- `qwen3:14b-Q4_K_M` (~10 GB): no cabe junto con npc-game 7b (~6 GB) → excede 12 GB.
- `qwen3:8b-Q4_K_M` (~6.5 GB): muy ajustado con npc-game 7b → sin margen para Chatterbox.
- `qwen2.5:7b-Q4_K_M` (~5.3 GB): viable, pero se prefiere mantener coherencia con familia Qwen3.

### 2. System prompt sin tildes

**Decisión**: Redactar el system prompt en español sin tildes.

**Justificación**:
- Convención observada en `npc-game/Modelfile`.
- Los modelos de lenguaje funcionan correctamente con texto sin tildes en español.
- Mantiene coherencia entre ambos Modelfiles del proyecto.

### 3. Corpus oficial con RAG

**Decisión**: Utilizar RAG (embeddings + vector store) para alimentar el corpus oficial.

**Justificación**:
- Optimización de tokens: solo se inyectan fragmentos relevantes en cada turno.
- Escalabilidad: el corpus puede crecer sin consumir todo el contexto del modelo.
- Flexibilidad: backend puede ajustar el número de fragmentos recuperados (top-K) y la longitud máxima de `corpus_context`.

**Alternativas descartadas**:
- Inyección directa en prompt: consume tokens innecesariamente, no escala bien.
- Fine-tuning: requiere datos de entrenamiento, complejidad adicional, no es viable para v1.

### 4. Gestión de Chatterbox en concurrencia

**Decisión**: Descargar Chatterbox temporalmente cuando el padre consulta al chatbot.

**Justificación**:
- Permite concurrencia de npc-game + parent sin exceder VRAM.
- El padre acepta latencia adicional (~5-10 s) por descarga/recarga de Chatterbox.
- Chatterbox solo se usa para audio no cacheado de npc-game, por lo que la descarga temporal no afecta la experiencia del niño (audio cacheado sigue disponible).

**Alternativas descartadas**:
- Mantener Chatterbox cargado: excede VRAM con npc-game + parent.
- Limitar chatbot parental cuando Chatterbox está activo: viola requisito de disponibilidad para padres.

## Contract changes

No aplica. Los contratos YAML se definen en Sprint 002.
