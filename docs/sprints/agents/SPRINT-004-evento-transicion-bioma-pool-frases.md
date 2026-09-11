# Sprint 004 - Agents

## Goal
Emitir un evento de transición de bioma (`BIOME_TRANSITION`) con una frase corta elegida de un pool por transición (anti-repetición), reproducido como audio estático, y estrictamente gateado por las preferencias parentales vigentes de NPC y voz.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-092 (backend, contrato de transporte/bioma activo)
waiting_for:

## Decisiones confirmadas (2026-09-11)

1. **Audio estático, no dinámico**: las frases de transición se generan durante el desarrollo (pregeneradas), no en tiempo real — mismo tipo de activo que `welcome`/`farewell` hoy, no un pipeline de síntesis por turno.
2. **Pool de variantes por transición, con anti-repetición**: no una única frase fija por transición (aunque ADR-026/FEAT-001 confirman un placeholder por defecto para cada una), sino varias variantes candidatas, seleccionadas con una regla que evita repetir la misma variante en transiciones consecutivas del mismo tramo.
3. **Gateo estricto por preferencias parentales**: reutiliza el mismo mecanismo ya vigente para `WELCOME`/`FAREWELL` — sin excepción nueva aprobada (ADR-026 §6, pregunta abierta ya resuelta como "no hay excepción").

## Alcance estricto de esta capa

- Este sprint diseña el disparo del evento y la selección de variante — **no** redacta el contenido final de las frases (los placeholders de ADR-026/FEAT-001 ya existen; el pool de variantes adicionales es un entregable de Contenido, no de Agents).
- No implementa el contrato de transporte/bioma activo (SPRINT-092 backend) ni la reproducción en frontend (ya cubierta por el patrón existente de `AvatarEvent` en `WorldMapScene`).

## Tasks

### Evento y contrato
- [ ] Definir `eventType: 'BIOME_TRANSITION'` dentro del contrato existente de `AvatarEvent` (mismo esquema que `WELCOME`/`FAREWELL`: `audioAvailable`, `audioId` si aplica), documentado junto a los eventos de avatar ya existentes.
- [ ] Confirmar con backend en qué punto del flujo de `WORLD_TRAVEL_REQUEST`/cambio de bioma (SPRINT-092) se dispara este evento, y qué identifica la transición concreta (bioma origen → bioma destino) para poder resolver el pool de variantes correcto.

### Selección de variante con anti-repetición
- [ ] Modelar el pool de variantes por transición (par origen→destino) como datos de contenido, no hardcodeados en el prompt/lógica de un LLM — esto es selección de audio pregenerado, no generación de texto.
- [ ] Reutilizar el mismo patrón de anti-repetición ya validado en `WorldOrchestratorService` (SPRINT-089, backend): registrar la última variante reproducida por transición dentro de la sesión activa y excluirla de la siguiente selección de esa misma transición, sin necesidad de persistir este dato entre sesiones (no es información sobre el niño, es solo variedad de contenido).
- [ ] Confirmar dónde vive este registro de "última variante reproducida" — si el mecanismo de selección de audio es responsabilidad de Agents o de backend/avatar (donde ya vive la lógica de `WELCOME`/`FAREWELL` estático/dinámico), para no duplicar infraestructura de selección en dos sitios.

### Guardrails
- [ ] El evento `BIOME_TRANSITION` no se emite en absoluto si `npcEnabled`/`voiceEnabled` (según corresponda) están desactivados — validado en origen, no delegado al cliente.
- [ ] La frase seleccionada se refiere exclusivamente al bioma de destino (mismo criterio que FEAT-001 §3.10) — ninguna variante del pool debe incluir preguntas personales, instrucciones obligatorias ni promesas de recompensa.
- [ ] Ninguna variante nueva del pool se activa sin pasar por la misma validación de adecuación por edad que los placeholders ya confirmados (Contenido, fuera de esta capa).

### Tests
- [ ] Test de gateo: con NPC o voz desactivados, ninguna transición de bioma emite `BIOME_TRANSITION`.
- [ ] Test de selección: con un pool de ≥2 variantes para una transición, dos llegadas consecutivas a ese mismo par de biomas dentro de la sesión no repiten la misma variante si hay una no usada disponible.
- [ ] Test de pool pequeño: con una única variante disponible para una transición, se reproduce igualmente sin error (no bloquea el paseo por falta de variedad).
- [ ] Test de contenido: ninguna variante del pool contiene preguntas personales, instrucciones obligatorias ni lenguaje evaluativo (revisión automatizable por patrón/lista de bloqueo, complementaria a la revisión manual de Contenido).

## Acceptance Criteria
- Con NPC y voz activados, llegar a un bioma nuevo reproduce una variante del pool de esa transición, referida exclusivamente al bioma de destino.
- Con NPC o voz desactivados, no se emite ningún evento de este tipo.
- Dos llegadas consecutivas al mismo par de biomas en la misma sesión no repiten variante si existe alternativa no usada.
- Ninguna variante del pool pide datos personales, opera como recompensa/logro, ni abre conversación abierta.

## Evidence
- Esquema del evento `BIOME_TRANSITION` documentado junto al resto de `AvatarEvent`.
- Tests de gateo, selección y contenido descritos arriba, ejecutados y en verde.
- Registro de variantes por transición (aunque el contenido final lo aporte Contenido).

## Risks
- Si el registro de "última variante reproducida" no distingue por par de biomas (origen→destino), podría aplicar anti-repetición incorrectamente entre transiciones distintas — cubrir explícitamente con el test correspondiente.
- Confundir esta selección de audio pregenerado con generación dinámica de frases abriría una superficie de riesgo (alucinación, contenido no revisado) fuera del alcance aprobado — este sprint debe dejar explícito que no hay LLM en este flujo.

## Dependencies
- ADR-026, FEAT-001 (placeholders confirmados y regla de gateo por preferencias).
- SPRINT-092 backend (contrato de transporte/bioma activo, para saber cuándo disparar el evento).
- Frontend: SPRINT-068/069 (mecanismo de pausa de llegada que consume este evento).
- Contenido: producción y validación por edad de las variantes adicionales del pool.

## Agent Instruction
- No generar contenido nuevo de las frases — usar los placeholders ya confirmados como base y coordinar con Contenido el resto del pool.
- No implementar el contrato de transporte de backend ni la reproducción de audio en frontend — ambos ya tienen su propio sprint/patrón existente.
- Mantener el guardrail de gateo por preferencias como responsabilidad de origen (backend/agents), nunca solo del cliente.

## Notes
- Este sprint reutiliza deliberadamente el patrón de anti-repetición ya validado en `WorldOrchestratorService` (SPRINT-089) en vez de diseñar un mecanismo nuevo desde cero.

## Review

### Developer implementation — Evidencias

(Pendiente de implementación por developer-agents)

### Reviewer verification

(Pendiente de revisión por reviewer-agents)
