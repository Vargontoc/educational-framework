# Sprint 093 - backend
# -----------------------------------------------

## Goal
Persistir el bioma y la posición de exploración del niño entre sesiones, sustituyendo (o complementando) el registro en memoria actual, para que el paseo se retome exactamente donde se dejó.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-11):

- `WorldState`/`visibleDiscoveryElements` vive hoy únicamente en `InMemoryWorldStateRegistry` (SPRINT-089), explícitamente **no persistido entre sesiones** — al cerrar sesión (o reiniciar el proceso) se pierde. Esto es correcto para "últimos elementos mostrados" (dato de rotación, no debe sobrevivir sesiones por diseño de SPRINT-089), pero es insuficiente para FEAT-012, que exige retomar el mismo estado de exploración en la siguiente sesión.

**Decisión confirmada por el usuario (2026-09-11):** se persiste la posición, no solo el bioma — el niño reanuda exactamente donde dejó el paseo, no en el punto de inicio del bioma.

Este es el cambio de mayor riesgo de privacidad de todo FEAT-012: pasar de "nada persiste" a "algo persiste" exige aplicar minimización de datos estrictamente (ADR-026 §6, FEAT-012 §6) — el campo persistido debe ser el mínimo necesario para reanudar, nunca un historial de movimientos, tiempo de sesión ni interacciones con elementos decorativos.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-091
waiting_for: confirmación de privacidad sobre el campo mínimo a persistir (ver Preguntas de decisión)

## Tasks

### Modelo de persistencia
- [ ] Nueva tabla (p. ej. `world_exploration_state`) o extensión de una entidad de sesión/perfil existente, con como máximo: identificador del niño/perfil, bioma activo, posición normalizada (mismo formato 0.0–1.0 ya usado para `positionX`/`positionY` de elementos, SPRINT-090) y timestamp de última actualización. Sin campos de progreso, interacción o duración.
- [ ] Migración Liquibase correspondiente (numeración siguiente a la última existente).
- [ ] Decidir explícitamente qué ocurre si el bioma persistido ya no existe en el catálogo activo (p. ej. contenido retirado) — no debe romper el arranque de sesión; fallback razonable a documentar (p. ej. Pradera).

### Escritura
- [ ] Actualizar el estado persistido en los mismos puntos donde hoy se actualiza `WorldState` en memoria (tras `buildDestination`, tras `WORLD_TRAVEL_REQUEST` de SPRINT-092) — con la posición más reciente conocida del niño en ese bioma. Confirmar con frontend con qué frecuencia/mecanismo se reporta la posición actual del jugador al backend, si no existe ya un canal para ello (hoy el offset de `GradualScroller` es puramente local al cliente).
- [ ] Evaluar si hace falta un nuevo mensaje WS ligero (p. ej. `WORLD_POSITION_UPDATE`, con throttling) para que el cliente informe su posición sin saturar el canal — **no implementar sin confirmar antes con frontend y con quien revisó privacidad**, ya que reportar posición con frecuencia alta podría interpretarse como tracking de movimiento, algo que ADR-026/FEAT-012 excluyen explícitamente.

### Lectura
- [ ] Al construir el primer destino de una sesión (`WORLD_STATE_SYNC` inicial), si existe estado persistido para el niño, usar su bioma y exponer su posición para que frontend la consuma (SPRINT-069 frontend) en vez de arrancar siempre en offset 0 del bioma por defecto.
- [ ] Primera entrada sin estado previo: comportamiento ya vigente (Pradera, punto de inicio) — este sprint no lo modifica.

### Tests
- [ ] Unit test: tras persistir bioma+posición y reiniciar el registro (simulando fin de proceso/sesión), el siguiente arranque de sesión recupera exactamente ese bioma y posición.
- [ ] Unit test: sin estado previo, arranca en Pradera (comportamiento heredado, no debe romperse).
- [ ] Unit test: bioma persistido que ya no existe en el catálogo activo cae al fallback documentado sin error.
- [ ] Contract test: el campo de posición expuesto en la sincronización inicial no incluye ningún dato adicional a bioma/posición (revisión de minimización de datos).

## Manual Tests
- Explorar hasta cierta posición en un bioma, cerrar sesión, reabrir con el mismo perfil y verificar que el paseo arranca en ese bioma y esa posición aproximada.

## Risks
- Es el cambio de mayor riesgo de privacidad del FEAT — no proceder con la implementación de escritura sin la confirmación explícita pendiente (ver "Preguntas de decisión").
- Si se introduce un mensaje de reporte de posición sin throttling adecuado, podría generar tráfico WS innecesario o percibirse como tracking granular de movimiento del niño — evaluar cuidadosamente antes de implementar esa pieza en concreto.

## Dependencies
- SPRINT-091 (catálogo de biomas, para saber a qué biomas puede referirse el estado persistido).
- Frontend: SPRINT-069 (consumo del estado persistido al reanudar).

## Preguntas de decisión al usuario
1. ¿Cómo llega al backend la posición actual del niño para poder persistirla — un nuevo mensaje WS periódico con throttling, o basta con capturarla en los puntos donde ya hay tráfico WS (p. ej. al cerrar/pausar la escena)? Esto determina si este sprint necesita un contrato nuevo de "reporte de posición" o no.
2. ¿Qué fallback se aplica si el bioma persistido deja de estar disponible en el catálogo (contenido retirado)?

## Agent Instruction
- No persistir ningún dato más allá de bioma y posición normalizada — ni interacciones con elementos, ni tiempo de sesión, ni conteos.
- No implementar el mensaje de reporte de posición sin resolver antes la pregunta de decisión #1 con el usuario.
- Código, comentarios y nombres en inglés.

## Notes
- Este sprint depende de una decisión de producto/privacidad explícita (pregunta #1) que no se puede resolver solo a nivel técnico — no cerrar su diseño final sin esa confirmación.

## Review

### completed_tasks
(Pendiente de implementación)

### incomplete_tasks
(Pendiente de implementación)
