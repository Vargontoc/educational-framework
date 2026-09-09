# Sprint 089 - backend
# -----------------------------------------------

## Goal
Confinar la selección de destino a bioma MEADOW y limitar los elementos de descubrimiento visibles simultáneamente a un máximo configurable (2-3), con selección rotativa y anti-repetición dentro de la sesión activa. Cierra dos violaciones ya activas de los criterios de aceptación de FEAT-010 (§4 AC3 y §3.8).

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-07) sobre el código actual:

- `WorldOrchestratorService.selectDestination` toma `hosts.get(0)` del primer host activo devuelto para la edad del niño, sin filtrar por bioma; solo cae a `Biome.MEADOW` hardcodeado si la lista de hosts está vacía. No hay confinamiento real a MEADOW.
- `WorldOrchestratorService.buildDestination` añade **todos** los elementos activos del bioma a `discoveryProposals`, sin ningún límite. Con más de un elemento de descubrimiento activo, esto ya viola FEAT-010 AC3 ("La vista nunca presenta más de tres elementos claramente interactuables a la vez").
- `WorldState.visibleDiscoveryElements` (campo `List<WorldDiscoveryProposal>`) existe pero no está conectado a ningún flujo de construcción de destino — es el campo natural para guardar qué elementos se mostraron la última vez y así aplicar anti-repetición.
- Los seeds actuales (`seeds/12-world-hosts.json`, `seeds/14-world-discovery-elements.json`) solo tienen **un** host MEADOW y **un** elemento de descubrimiento MEADOW — insuficiente para poder verificar rotación o tope; este sprint amplía el seed.

Este sprint **no** añade posición ni ancho de mundo (eso es SPRINT-090, que depende de este). La separación "perceptible" entre elementos visibles (parte de AC3) no se puede verificar todavía sin datos de posición — queda cubierta en SPRINT-090, que añade una comprobación de distancia mínima sobre el algoritmo de selección que este sprint deja preparado.

## Status
status: implemented
started_at: 2026-09-09
closed_at:
blocked_by:
waiting_for:

## Tasks

### Confinamiento de bioma
- [x] En `WorldOrchestratorService.selectDestination`, filtrar `hosts` por `biome == Biome.MEADOW` antes de seleccionar un host, en vez de depender del fallback hardcodeado solo cuando la lista está vacía.
- [x] Si no hay ningún host MEADOW activo para la edad del niño, aplicar el mismo comportamiento de "mundo no disponible" ya definido (no inventar un mapa vacío ni un error técnico).

### Configuración de exploración
- [x] Crear `WorldExplorationConfig` (mismo patrón que `WorldInactivityConfig`, en `world/service/`) con al menos `maxVisibleElements` (config, valor inicial 3, dentro del rango 2-3 confirmado por FEAT-010).
- [x] Exponer la configuración vía `application.yml`/properties existente, sin hardcodear el valor en el servicio.

### Selección rotativa con anti-repetición
- [x] Conectar `WorldState.visibleDiscoveryElements` como el registro de "últimos elementos mostrados" de la sesión activa (in-memory, vía `InMemoryWorldStateRegistry`; no se persiste entre sesiones — respeta la exclusión de FEAT-010 §7).
- [x] En `WorldOrchestratorService.buildDestination`, seleccionar como máximo `maxVisibleElements` candidatos del pool de elementos activos elegibles (edad, bioma), priorizando los que **no** estén en `visibleDiscoveryElements` de la construcción anterior.
- [x] Si el pool elegible es menor o igual que `maxVisibleElements`, no hay rotación posible (se muestran todos) — comportamiento esperado, no es un error.
- [x] Si excluir todos los elementos ya mostrados deja menos candidatos que `maxVisibleElements`, completar con elementos ya mostrados antes que dejar el mapa con menos elementos de los configurados (el tope de cantidad y bioma tienen prioridad sobre la variedad).
- [x] Actualizar `WorldState.visibleDiscoveryElements` con la selección final tras cada `buildDestination`.

### Seed de contenido
- [x] Ampliar `seeds/14-world-discovery-elements.json` con al menos 5 elementos de descubrimiento MEADOW adicionales (edad 3-4, `status: ACTIVE`), variando `elementType`/`interactionCueType`, para poder verificar rotación y tope con un pool real. Mantener `MEADOW_SHINY_FLOWER` existente.
- [x] No añadir `activityId`/`topicId` a los nuevos elementos salvo que ya exista contenido de actividad asociado (mantener el carácter puramente decorativo de esta fase, igual que el elemento existente).

### Tests
- [x] Unit test: `selectDestination` nunca selecciona un host fuera de MEADOW, incluso si existen hosts activos de otros biomas para la misma edad.
- [x] Unit test: `buildDestination` nunca genera más de `maxVisibleElements` proposals en `discoveryProposals`.
- [x] Unit test: con un pool de eligibles mayor que `maxVisibleElements`, dos construcciones sucesivas de destino (simulando cambio de situación narrativa dentro de la misma sesión) no repiten el conjunto completo de elementos si hay candidatos no vistos disponibles.
- [x] Unit test: con un pool de eligibles menor o igual que `maxVisibleElements`, se muestran todos sin error.
- [x] Unit test: `WorldState.visibleDiscoveryElements` refleja la última selección construida.

## Manual Tests
- Con el seed ampliado, levantar el backend y verificar por WebSocket (`world_heartbeat` → `WORLD_STATE_SYNC`) que `destination.discoveryElements` nunca trae más de 3 elementos y que `destination.host`/`biome` siempre es MEADOW.

## Risks
- Si `maxVisibleElements` se configura mal (0 o negativo) podría dejar el mapa sin elementos — validar en la config al arrancar.
- La regla "completar con elementos ya mostrados si no hay suficientes no vistos" podría interpretarse como poca variedad si el pool es pequeño — es una consecuencia esperada del pool actual (6 elementos tras el seed ampliado), no un defecto del algoritmo.

## Dependencies
- Ninguna hacia atrás. SPRINT-090 depende de este sprint (añade posición y separación sobre la selección aquí construida).

## Agent Instruction
- No añadir campos de posición ni de ancho de mundo en este sprint — eso es SPRINT-090.
- No enviar al frontend ningún campo nuevo en el contrato en este sprint; los cambios son puramente de selección/orquestación server-side sobre el contrato ya existente.
- Mantener `visibleDiscoveryElements` en memoria (registro de World), nunca persistirlo en base de datos — FEAT-010 excluye explícitamente la persistencia de exploración entre sesiones.
- Código, comentarios y nombres en inglés.

## Notes
Este sprint implementa la decisión técnica confirmada en FEAT-010 §8 punto 3 ("selección rotativa con patrón anti-repetición"). La separación perceptible entre elementos (también parte de AC3) se resuelve en SPRINT-090 una vez exista dato de posición.

## Review

completed_tasks:
- **Confinamiento de bioma**: `WorldOrchestratorService.buildDestination` ahora filtra `listActiveHostsForAge(childAge)` por `biome == Biome.MEADOW` antes de elegir un host, en vez de depender del fallback (que antes solo actuaba si la lista de hosts estaba vacía). Los elementos de descubrimiento pasaron de `listActiveElementsForAge(childAge)` (sin filtro de bioma) a `listActiveElementsByBiomeAndAge(Biome.MEADOW, childAge)` — método que ya existía en `WorldCatalogUseCase`/`WorldCatalogService` pero no estaba conectado a la orquestación del mundo.
- **`WorldExplorationConfig`**: creada en `world/service/` (mismo paquete que `WorldInactivityConfig`), pero además anotada `@ConfigurationProperties(prefix = "app.world.exploration")` y registrada vía `@EnableConfigurationProperties` en `WorldModuleConfiguration`, para cumplir explícitamente "exponer vía application.yml/properties, sin hardcodear" (más allá de lo que hace `WorldInactivityConfig` hoy, que no está enlazada a properties). Añadida `app.world.exploration.max-visible-elements` (default `3`, override por `APP_WORLD_EXPLORATION_MAX_VISIBLE_ELEMENTS`) en `application.yml`, siguiendo el mismo patrón que `app.tracking.*`.
- **Selección rotativa con anti-repetición**: `WorldOrchestratorService` recibe ahora `WorldStateRegistry` (solo lectura) para consultar `WorldState.visibleDiscoveryElements` del `childSessionId` antes de construir la nueva selección. Algoritmo en `selectVisibleProposals`: ordena el pool elegible por `sortOrder`; si el pool cabe entero dentro de `maxVisibleElements`, lo devuelve completo; si no, separa candidatos no-vistos-recientemente de vistos-recientemente (por `discoveryElementId`, no por `proposalRuntimeId`, que es un UUID nuevo en cada construcción) y prioriza los no vistos, completando con vistos si no alcanzan el tope. `GameWebSocketHandler.getNewWorld` es quien persiste la selección resultante en `WorldState.visibleDiscoveryElements` (junto a `setCurrentDestination`) antes de `worldStateRegistry.save(...)` — el propio `WorldOrchestratorService` nunca escribe en el registro, solo lee (ver "decisiones de diseño" más abajo).
- **Seed de contenido**: `seeds/14-world-discovery-elements.json` ampliado de 1 a 6 elementos MEADOW (variando `elementType` entre `DISCOVERY`/`SIMPLE_INTERACTIVE`/`DECORATIVE` y `interactionCueType` entre `BREATHING_GLOW`/`null` — es el único valor de cue existente en el enum), sin `activityId`/`topicId` en los nuevos, manteniendo el carácter decorativo.
- **Tests**: añadidos 6 tests nuevos a `WorldOrchestratorServiceTest` (confinamiento a MEADOW con hosts mixtos, fallback sin host MEADOW, tope nunca superado, pool pequeño muestra todo, rotación evita repetir conjunto completo entre dos construcciones sucesivas, relleno con elementos ya vistos cuando no hay suficientes no-vistos). Los 5 tests existentes se adaptaron al nuevo `listActiveElementsByBiomeAndAge` y al constructor con 2 parámetros nuevos.

incomplete_tasks:
- Ninguna de las tareas listadas quedó sin hacer.

decisiones_de_diseno (no eran parte de las preguntas ya resueltas en FEAT-010, tomadas dentro del sprint):
- **"Mundo no disponible" sin host MEADOW**: no existía (ni existe hoy) un mecanismo para que `buildDestination` señalice "destino inválido" hacia arriba (`WorldDestinationSelectionResult` nunca es null, y no hay un status `NO_WORLD_STATE` que dependa de esto). Interpreté "aplicar el mismo comportamiento ya definido" como reusar el fallback YA EXISTENTE (host* sin datos, `biome: MEADOW`, sin excepción ni mapa técnico), que antes solo se activaba si `listActiveHostsForAge` devolvía vacío y ahora también cubre "hay hosts pero ninguno es MEADOW". No se inventó un nuevo mecanismo de señalización — habría sido un cambio de arquitectura mayor no pedido explícitamente por las tareas.
- **Quién escribe `visibleDiscoveryElements`**: decidí que `WorldOrchestratorService` solo LEE el registro (para poder testear el algoritmo de rotación de forma aislada, inyectando estados previos vía mock) y que el CALLER que ya posee y guarda el `WorldState` completo es quien escribe el campo. Esto cubre `GameWebSocketHandler.getNewWorld` (único punto donde antes de este sprint se guardaba una `WorldState` nueva con `currentDestination`). **No toqué `WorldGameStartService.buildFallbackResult`**: ese método ya construía un `WorldDestination` de fallback sin persistirlo en el `WorldState` (ni `setCurrentDestination` ni `save`) — es una respuesta puntual/efímera para el cliente, no una transición de estado real. Extender la persistencia ahí habría sido un cambio de comportamiento fuera de lo que pide este sprint; lo dejé como estaba.
- **Patrón de rotación**: determinista por `sortOrder` + exclusión de lo último mostrado, no aleatorio. Es suficiente para cumplir "no repite el conjunto completo si hay candidatos no vistos" y mantiene los tests reproducibles sin mockear un generador aleatorio.

contract_changes:
- Ninguno. `WorldStateSyncPayload`/`WorldDestinationPayload`/`WorldDiscoveryElementPayload` no cambiaron; solo cambió qué subconjunto de elementos y qué host llegan poblados en `discoveryProposals`/`host`, dentro del contrato ya existente.

tests_ejecutados:
- `mvn -o test -Dtest=WorldOrchestratorServiceTest`: 11/11 OK.
- `mvn -o test -Dtest="World*Test,GameWebSocketHandlerTest"`: 149/149 OK (incluye `WorldGameStartServiceTest`, `WorldHeartbeatServiceTest`, `WorldProposalServiceTest`, `WorldStateTest`, etc. — sin regresiones).
- `mvn -o test` (suite completa): 37 errores, todos `NoClassDefFoundError`/`IllegalStateException: Could not find a valid Docker environment` en tests de integración con Testcontainers (`AbstractIntegrationTest` y subclases: `AuthControllerTest`, `FamilyControllerTest`, `ChildProfileControllerTest`, `ChildSessionControllerTest`, `ContactControllerIntegrationTest`, `AdultProfileControllerTest`) — verificado en `target/surefire-reports`, causa raíz es falta de Docker en este entorno de ejecución, no relacionado con ningún fichero tocado en este sprint. 0 failures reales, 0 errores en dominios distintos a integración con Docker.
- Se detectó y corrigió durante la verificación un `NullPointerException` inducido por el propio cambio: `GameWebSocketHandlerTest` stubea `worldOrchestrator.selectDestination(...)` devolviendo `new WorldDestinationSelectionResult()` (constructor vacío, `destination == null`); mi primera versión de `GameWebSocketHandler.getNewWorld` llamaba a `select.getDestination().getDiscoveryProposals()` sin comprobar null, rompiendo 4 tests de `GameWebSocketHandlerTest` (excepción capturada por el `catch` de `handleAuth`, cerrando la sesión con `SERVER_ERROR`). Corregido con una comprobación de null antes de `setVisibleDiscoveryElements`.

next_sprint_suggestions:
- SPRINT-090 puede continuar: añadir `positionX`/`positionY` a `WorldDiscoveryElementProjection`/`WorldDiscoveryElement` y `worldWidth` a `WorldHost`, y extender `selectVisibleProposals` con la comprobación de separación mínima (usar `previouslyVisibleElementIds`/`eligible` ya construidos aquí como base).
- Pendiente fuera de este sprint: `WorldGameStartService.buildFallbackResult` no persiste su destino de fallback ni actualiza `visibleDiscoveryElements` — si en el futuro se decide que el fallback SÍ debe considerarse una transición real de estado, requeriría revisar esa decisión explícitamente.
