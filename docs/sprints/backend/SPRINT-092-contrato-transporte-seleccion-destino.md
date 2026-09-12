# Sprint 092 - backend
# -----------------------------------------------

## Goal
Añadir un mensaje WebSocket entrante que permita al niño solicitar viajar a cualquiera de los 6 biomas del catálogo desde el transporte de su punto de inicio, con disponibilidad igualitaria (sin candados, requisitos ni datos de visitas previas).

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-11):

- Hoy el único disparador de construcción de destino es `world_heartbeat` (`GameWebSocketHandler.handleWorldHeartbeat` → `WorldOrchestratorService.selectDestination`/`buildDestination`), que resuelve el bioma automáticamente (hoy MEADOW fijo; con SPRINT-091, el bioma activo de la sesión). No existe ningún mensaje entrante que permita al cliente **elegir** un bioma explícitamente.
- `WorldOrchestratorService.buildDestination` ya sabe construir un destino a partir de un host/bioma dado — la pieza que falta es el punto de entrada (mensaje WS) y la validación de que el bioma solicitado es uno de los 6 disponibles, no una construcción nueva del motor de selección.

## Status
status: verified
started_at: 2026-09-12
closed_at: 2026-09-12
verified_at: 2026-09-12
blocked_by: SPRINT-091
waiting_for:

## Tasks

### Contrato del mensaje de transporte
- [x] Definir nuevo mensaje WS entrante, p. ej. `WORLD_TRAVEL_REQUEST { biome: string }`, documentado en el AsyncAPI existente junto a `world_heartbeat`.
- [x] Validar que `biome` corresponde a uno de los biomas activos del catálogo (SPRINT-091); si no, responder con el mismo tipo de error controlado ya usado para otros casos de `GAME_ERROR` (no inventar un canal de error nuevo).
- [x] Confirmar explícitamente que la validación **no** comprueba visitas previas, progreso ni ningún dato del niño — cualquiera de los 6 biomas es siempre una opción válida (FEAT-012 criterio de aceptación §11).

### Orquestación
- [x] `GameWebSocketHandler`: nuevo handler para `WORLD_TRAVEL_REQUEST` que invoca `WorldOrchestratorService.buildDestination` contra el host del bioma solicitado (reutilizando la lógica existente, no duplicándola), y responde con el mismo `WORLD_STATE_SYNC`/`WORLD_DESTINATION_READY` ya usado para el flujo automático.
- [x] Actualizar el estado de sesión (`WorldState`, `InMemoryWorldStateRegistry` o su sucesor de SPRINT-093) para reflejar el nuevo bioma activo tras un viaje explícito, de forma que el siguiente `world_heartbeat` no lo sobrescriba con el bioma anterior.

### Tests
- [x] Unit test: `WORLD_TRAVEL_REQUEST` con un bioma válido del catálogo construye destino para ese bioma, independientemente de cuál fuera el bioma activo antes.
- [x] Unit test: `WORLD_TRAVEL_REQUEST` con un bioma fuera del catálogo (o mal escrito) responde error controlado, sin romper la sesión activa.
- [x] Unit test: tras un `WORLD_TRAVEL_REQUEST` exitoso, el siguiente `world_heartbeat` mantiene el nuevo bioma como activo.
- [x] Unit test: pedir el mismo bioma en el que ya se está no produce comportamiento distinto a pedir cualquier otro (no hay estado especial de "ya estás aquí" que deba tratarse como error).

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
- Contrato AsyncAPI actualizado: nuevo mensaje `world_travel` en `game-client-message.yaml` y evento `WORLD_TRAVEL_REQUEST` en `websocket.yaml` (v1.9.0).
- `WorldOrchestrator` interface: nuevo método `buildDestinationForBiome(Long childSessionId, String biome, Integer childAge)`.
- `WorldOrchestratorService`: refactor de `buildDestination` extrayendo `buildDestinationForHost(childSessionId, targetBiome, childAge)` que busca el host correspondiente al bioma solicitado.
- `GameErrorCode`: nuevo valor `INVALID_BIOME`.
- `GameWebSocketHandler`: nuevo case `world_travel` en el switch + método `handleWorldTravel` que valida bioma, invoca `buildDestinationForBiome`, actualiza `WorldState` en el registry, y responde con `WORLD_STATE_SYNC`.
- Tests unitarios: 4 tests en `GameWebSocketHandlerTest` (bioma válido, bioma inválido, heartbeat posterior mantiene bioma, mismo bioma actual) + 3 tests en `WorldOrchestratorServiceTest` (bioma válido, bioma inválido, host no encontrado).
- Todos los 67 tests de las clases afectadas pasan correctamente.

### incomplete_tasks
(Ninguna)

### Decisiones de detalle
- **Nombre del mensaje**: `world_travel` (snake_case, coherente con la convención `world_heartbeat`, `world_discovery_interacted`). El evento AsyncAPI se denomina `WORLD_TRAVEL_REQUEST` (client→server).
- **Error channel**: Se usa `GAME_ERROR` con código `INVALID_BIOME` (instrucción explícita del sprint de no inventar canal nuevo).
- **Child age**: Se mantiene el hardcoded `3` usado en `getNewWorld()`, coherente con el rango de edad del producto (3-4 años).
- **Validación de bioma**: Solo se comprueba que el string corresponde a un valor del enum `Biome` (6 valores). No se consulta progreso, visitas previas ni datos del niño.
- **Refactor de `buildDestination`**: El método privado original se simplificó para delegar en `buildDestinationForHost`, evitando duplicación de lógica.
- **Mismo bioma actual**: No se trata como caso especial — se construye un nuevo destino con nuevo `destinationId` (UUID), igual que cualquier otro bioma.

### Review verdict
**APPROVED**

### Review evidence
- **Contrato AsyncAPI**: `game-client-message.yaml` añade `WorldTravelMessage` con `type: world_travel` y `biome` enum (6 valores). `websocket.yaml` añade evento `WORLD_TRAVEL_REQUEST` (client→server).
- **WorldOrchestrator interface**: nuevo método `buildDestinationForBiome(Long childSessionId, String biome, Integer childAge)`.
- **WorldOrchestratorService**: implementa `buildDestinationForBiome` que convierte string a enum `Biome` y delega en `buildDestinationForHost`. Refactor de `buildDestination` para evitar duplicación.
- **GameWebSocketHandler**: nuevo case `world_travel` en switch + método `handleWorldTravel` que valida bioma, invoca orquestador, actualiza `WorldState` en registry, y responde con `WORLD_STATE_SYNC`.
- **GameErrorCode**: nuevo valor `INVALID_BIOME` para errores de validación de bioma.
- **Validación**: solo comprueba que el string corresponde a un valor del enum `Biome` (MEADOW, FARM, WOODS, BEACH, SPACE, PREHISTORY). No consulta progreso, visitas previas ni datos del niño.
- **Estado de sesión**: `WorldState` se actualiza con el nuevo destino tras viaje exitoso, garantizando que el siguiente `world_heartbeat` mantiene el bioma elegido.
- **Tests ejecutados**: 67/67 pasan (43 GameWebSocketHandlerTest + 24 WorldOrchestratorServiceTest).
- **Cobertura de tests**: bioma válido construye destino, bioma inválido responde error sin romper sesión, heartbeat posterior mantiene nuevo bioma, mismo bioma actual no es caso especial.

### ADR-026 compliance
- ✅ Los 6 biomas son siempre accesibles sin candados, requisitos ni datos de visitas previas.
- ✅ Validación solo comprueba que el bioma corresponde al catálogo (enum `Biome`).
- ✅ Transporte permite elegir cualquiera de los 6 biomas disponibles sin bloqueos.
- ✅ Estado de sesión se actualiza correctamente para mantener el bioma elegido entre heartbeats.

### Observations
None. Sprint complete and verified.
