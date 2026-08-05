# Sprint 003 - Agents

## Goal
Validar el comportamiento completo del agente con el modelo `qwen3:8b-Q3_K_M`, ajustar parámetros finales, verificar compatibilidad con TTS y concurrencia de modelos, y coordinar integración con frontend.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-001, SPRINT-002
waiting_for:

## Decisiones confirmadas (2026-08-05)

1. **Modelo validado**: `qwen3:8b-Q3_K_M` para agent-educational-parent. Se valida que cumple los 14 requisitos funcionales de FEAT-003.

2. **Concurrencia**: npc-game `qwen2.5:7b-Q5_K_M` permanente + parent `qwen3:8b-Q3_K_M` bajo demanda + Chatterbox descarga temporal.

3. **Latencia aceptable**: padre acepta 15-20 s de latencia inicial para consultas al chatbot.

4. **Longitud máxima de respuesta**: 1500 caracteres (coherente con `num_predict 512` y compatibilidad TTS).

## Tasks
- [ ] Validar comportamiento con `qwen3:8b-Q3_K_M`: ejecutar pruebas exhaustivas de los 14 requisitos funcionales de FEAT-003, verificar que el modelo cumple guardrails estrictos (rechazo literal, derivación profesional, no PII, no rankings), verificar que el modelo distingue hechos de síntesis y consejo, verificar que el modelo mantiene coherencia con historial de 5-8 turnos, documentar resultados y ajustes necesarios.
- [ ] Actualizar `framework/agents/agent-educational-parent/load-ollama.ps1`: ajustar parámetros hardcodeados para que coincidan con Modelfile (`temperature 0.3`, `num_predict 512`), añadir lógica de carga bajo demanda (no mantener modelo en memoria permanentemente), documentar latencia esperada de carga (~10-15 s en RTX 4070 SUPER).
- [ ] Validar concurrencia de modelos en RTX 4070 SUPER: medir VRAM ocupada con npc-game 7b-Q5_K_M + parent 8b-Q3_K_M (~11.5 GB total), medir latencia de carga/descarga de parent, validar gestión de Chatterbox (descarga temporal cuando padre consulta, recarga posterior), documentar métricas y comportamiento.
- [ ] Validar compatibilidad con TTS: verificar que las respuestas son texto plano sin Markdown, verificar que la longitud máxima de 1500 caracteres es compatible con Chatterbox, verificar que no hay caracteres problemáticos para síntesis de voz.
- [ ] Coordinar con frontend: confirmar presentación de respuestas (texto plano, longitud adecuada), confirmar flujo de selección de perfil mediante comandos, confirmar presentación de derivaciones profesionales.
- [ ] Documentar resultados finales: actualizar `README.md` con resultados de validación, documentar métricas de latencia y VRAM, documentar limitaciones conocidas (p. ej. Escenario 2 con Chatterbox activo).

## Acceptance Criteria
- **Criterio FEAT-003 §4.7**: solicitud de consejo → recomendación general + derivación visible.
- **Criterio FEAT-003 §4.8**: petición de diagnóstico/terapia/seguridad → derivación, no respuesta directa.
- Los parámetros del Modelfile y `load-ollama.ps1` coinciden y están justificados para RTX 4070 SUPER.
- Las respuestas son compatibles con TTS (sin Markdown, longitud ≤1500 caracteres).
- Todas las pruebas de comportamiento pasan (general, derivación, rechazo, selección de perfil, no-PII, no-diagnóstico, historial).
- La concurrencia de modelos funciona sin OOM en RTX 4070 SUPER.
- La latencia de carga de parent es aceptable (<30 s).

## Evidence
- Resultados de pruebas de comportamiento con `qwen3:8b-Q3_K_M` documentados.
- `framework/agents/agent-educational-parent/load-ollama.ps1` actualizado y coherente con Modelfile.
- Métricas de VRAM y latencia documentadas.
- Confirmación de compatibilidad con TTS.
- Confirmación de coordinación con frontend.
- `framework/agents/agent-educational-parent/README.md` actualizado con resultados finales.

## Risks
- `qwen3:8b-Q3_K_M` puede no cumplir todos los requisitos de FEAT-003 (especialmente guardrails estrictos y distinción hechos/síntesis). Si falla, se debe evaluar migrar a `qwen3:8b-Q4_K_M` (más VRAM) o `qwen2.5:7b-Q4_K_M` (familia diferente).
- La concurrencia de modelos puede generar OOM en escenarios con Chatterbox activo. Se requiere gestión cuidadosa de VRAM.
- La latencia de carga del modelo (~15-20 s) puede ser percibida como excesiva por algunos usuarios. Se debe documentar claramente.
- La descarga temporal de Chatterbox puede generar latencia adicional para el padre (~5-10 s). Se debe evaluar si es aceptable.

## Dependencies
- FEAT-003: Chatbot parental conversacional de Nubi.
- ADR-003: Chatbot parental conversacional de Nubi.
- SPRINT-001: System prompt, guardrails y propuesta de corpus (completado).
- SPRINT-002: Contratos, selección de perfil e historial (completado).
- Backend: implementación del endpoint `/api/v1/parent/chat`, gestión de RAG, gestión de Chatterbox.
- Frontend: presentación de respuestas, flujo de comandos, presentación de derivaciones.
- TTS: compatibilidad con texto plano sin Markdown, longitud máxima 1500 caracteres.

## Agent Instruction
- Validar exclusivamente el comportamiento del agente con `qwen3:8b-Q3_K_M`.
- No modificar el system prompt salvo ajustes menores derivados de las pruebas.
- No implementar código de backend, frontend ni TTS.
- Actualizar `load-ollama.ps1` para que coincida con los parámetros del Modelfile.
- Medir y documentar métricas de VRAM y latencia en RTX 4070 SUPER.
- Validar compatibilidad con TTS (texto plano, longitud ≤1500 caracteres).
- Coordinar con frontend para confirmar presentación de respuestas y flujo de comandos.
- Documentar resultados finales en `README.md`.
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- Este sprint es de validación y ajuste, no de implementación nueva.
- Si `qwen3:8b-Q3_K_M` no cumple los requisitos de FEAT-003, se debe evaluar migrar a un modelo diferente (ver Risks).
- La concurrencia de modelos es crítica para la experiencia de usuario. Se debe validar cuidadosamente en RTX 4070 SUPER.
- La latencia de carga del modelo es aceptable para el padre, pero se debe documentar claramente.
- La descarga temporal de Chatterbox es una solución técnica para permitir concurrencia sin exceder VRAM.
- Los resultados de este sprint determinan si el chatbot parental está listo para producción.

## Review

### Developer implementation — Evidencias

(Pendiente de implementación por developer-agents)

### Reviewer verification

(Pendiente de revisión por reviewer-agents)

## Design decisions

### 1. Validación exhaustiva de guardrails

**Decisión**: Ejecutar pruebas exhaustivas de los 14 requisitos funcionales de FEAT-003 con `qwen3:8b-Q3_K_M`.

**Justificación**:
- `qwen3:8b-Q3_K_M` es un modelo reducido que puede no tener capacidad suficiente para guardrails estrictos.
- Las pruebas exhaustivas garantizan que el modelo cumple los requisitos antes de pasar a producción.
- Si el modelo falla, se puede evaluar migrar a un modelo diferente antes de desplegar.

### 2. Métricas de VRAM y latencia

**Decisión**: Medir y documentar VRAM ocupada y latencia de carga/descarga en RTX 4070 SUPER.

**Justificación**:
- La concurrencia de modelos es crítica para la experiencia de usuario.
- Las métricas permiten validar que la arquitectura propuesta es viable en hardware real.
- Si las métricas exceden los límites, se puede ajustar la arquitectura (p. ej. reducir npc-game a 3b).

### 3. Compatibilidad con TTS

**Decisión**: Validar que las respuestas son texto plano sin Markdown y longitud ≤1500 caracteres.

**Justificación**:
- Chatterbox (TTS) requiere texto plano para síntesis de voz.
- La longitud máxima de 1500 caracteres es coherente con `num_predict 512` y compatibilidad TTS.
- Si las respuestas contienen Markdown o son demasiado largas, Chatterbox puede fallar o generar audio de baja calidad.

### 4. Coordinación con frontend

**Decisión**: Confirmar presentación de respuestas, flujo de comandos y presentación de derivaciones con frontend.

**Justificación**:
- El frontend es el responsable de presentar las respuestas al usuario.
- El flujo de comandos (`/perfil <nombre>`) requiere coordinación entre frontend y backend.
- Las derivaciones profesionales deben presentarse de forma clara y visible para el usuario.

## Contract changes

No aplica. Los contratos YAML se definieron en Sprint 002.
