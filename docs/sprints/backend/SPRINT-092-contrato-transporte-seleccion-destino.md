# Sprint 092 - backend
# -----------------------------------------------

## Goal
Añadir un mensaje WebSocket entrante que permita al niño solicitar viajar a cualquiera de los 6 biomas del catálogo desde el transporte de su punto de inicio, con disponibilidad igualitaria (sin candados, requisitos ni datos de visitas previas).

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-11):

- Hoy el único disparador de construcción de destino es `world_heartbeat` (`GameWebSocketHandler.handleWorldHeartbeat` → `WorldOrchestratorService.selectDestination`/`buildDestination`), que resuelve el bioma automáticamente (hoy MEADOW fijo; con SPRINT-091, el bioma activo de la sesión). No existe ningún mensaje entrante que permita al cliente **elegir** un bioma explícitamente.
- `WorldOrchestratorService.buildDestination` ya sabe construir un destino a partir de un host/bioma dado — la pieza que falta es el punto de entrada (mensaje WS) y la validación de que el bioma solicitado es uno de los 6 disponibles, no una construcción nueva del motor de selección.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-091
waiting_for:

## Tasks

### Contrato del mensaje de transporte
- [ ] Definir nuevo mensaje WS entrante, p. ej. `WORLD_TRAVEL_REQUEST { biome: string }`, documentado en el AsyncAPI existente junto a `world_heartbeat`.
- [ ] Validar que `biome` corresponde a uno de los biomas activos del catálogo (SPRINT-091); si no, responder con el mismo tipo de error controlado ya usado para otros casos de `GAME_ERROR` (no inventar un canal de error nuevo).
- [ ] Confirmar explícitamente que la validación **no** comprueba visitas previas, progreso ni ningún dato del niño — cualquiera de los 6 biomas es siempre una opción válida (FEAT-012 criterio de aceptación §11).

### Orquestación
- [ ] `GameWebSocketHandler`: nuevo handler para `WORLD_TRAVEL_REQUEST` que invoca `WorldOrchestratorService.buildDestination` contra el host del bioma solicitado (reutilizando la lógica existente, no duplicándola), y responde con el mismo `WORLD_STATE_SYNC`/`WORLD_DESTINATION_READY` ya usado para el flujo automático.
- [ ] Actualizar el estado de sesión (`WorldState`, `InMemoryWorldStateRegistry` o su sucesor de SPRINT-093) para reflejar el nuevo bioma activo tras un viaje explícito, de forma que el siguiente `world_heartbeat` no lo sobrescriba con el bioma anterior.

### Tests
- [ ] Unit test: `WORLD_TRAVEL_REQUEST` con un bioma válido del catálogo construye destino para ese bioma, independientemente de cuál fuera el bioma activo antes.
- [ ] Unit test: `WORLD_TRAVEL_REQUEST` con un bioma fuera del catálogo (o mal escrito) responde error controlado, sin romper la sesión activa.
- [ ] Unit test: tras un `WORLD_TRAVEL_REQUEST` exitoso, el siguiente `world_heartbeat` mantiene el nuevo bioma como activo.
- [ ] Unit test: pedir el mismo bioma en el que ya se está no produce comportamiento distinto a pedir cualquier otro (no hay estado especial de "ya estás aquí" que deba tratarse como error).

## Manual Tests
- Con backend levantado y catálogo de 6 biomas seedado (SPRINT-091), enviar `WORLD_TRAVEL_REQUEST` para cada uno de los 6 y verificar que `WORLD_STATE_SYNC` refleja el bioma correcto en cada caso.
- Verificar que pedir un bioma inexistente responde error controlado sin cerrar el WebSocket.

## Risks
- Si el nuevo mensaje no actualiza correctamente el estado de sesión, un `world_heartbeat` posterior podría "revertir" silenciosamente el bioma elegido por el niño — cubrir explícitamente con el test correspondiente.
- Este sprint asume que "elegir destino" y "el paseo automático que ya evoluciona por heartbeat" conviven sin conflicto; si en el futuro se decide que el bioma solo cambia por transporte (nunca automáticamente al final de un tramo), esa regla de producto no está definida aquí y debería confirmarse antes de que frontend dependa de un comportamiento u otro.

## Dependencies
- SPRINT-091 (catálogo de 6 biomas, prerrequisito directo).
- Frontend: SPRINT-067 (selector visual que dispara este mensaje), SPRINT-068 (pausa de llegada tras la respuesta).

## Agent Instruction
- No implementar persistencia de estado entre sesiones (eso es SPRINT-093) — este sprint solo cubre el viaje dentro de una sesión activa.
- No añadir ningún criterio de disponibilidad basado en progreso, visitas o resultados — los 6 destinos son siempre accesibles por igual.
- Código, comentarios y nombres en inglés.

## Notes
- El nombre exacto del mensaje (`WORLD_TRAVEL_REQUEST` u otro) es una propuesta; debe confirmarse con quien mantenga la convención de nombres de eventos WS del dominio `world` antes de implementar.

## Review

### completed_tasks
(Pendiente de implementación)

### incomplete_tasks
(Pendiente de implementación)
