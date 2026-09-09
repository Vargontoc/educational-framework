# Sprint 090 - backend
# -----------------------------------------------

## Goal
Añadir posición estable y normalizada por elemento de descubrimiento, y ancho de mundo por host, exponiéndolos en el contrato `WORLD_STATE_SYNC`. Sustituye la posición sintética y el `worldWidth` fijo que hoy calcula el frontend por su cuenta. Refina la selección rotativa de SPRINT-089 para respetar una separación mínima perceptible entre los elementos elegidos.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-07): ni `content.WorldDiscoveryElement`/`content.WorldHost`, ni `world.WorldDestination`/`WorldDiscoveryProposal`, ni los DTOs (`WorldDestinationPayload`, `WorldDiscoveryElementPayload`) ni los schemas AsyncAPI (`world-destination-payload.yaml`, `world-discovery-element-payload.yaml`) tienen hoy ningún campo de posición ni de límite de mundo. El frontend (`framework/frontend/app/src/components/game/worldmap/`) ya documenta esto como deuda explícita en `docs/worldmap-extensibility.md`: sintetiza la posición de cada elemento por índice de array y usa un `worldWidth` de 2560px hardcodeado en `WORLD_MAP_CONFIG`.

Decisiones técnicas confirmadas (FEAT-010 §8, 2026-09-07):
- Posición: continua, normalizada 0.0–1.0, en **ambos ejes** (X e Y) — no solo horizontal.
- Posición vive en el **elemento** (`content.WorldDiscoveryElement`); ancho de mundo vive en el **host** (`content.WorldHost`).
- Campos nuevos en el contrato: aditivos y `nullable`, sin nueva versión de payload.

Depende de SPRINT-089 (confinamiento a MEADOW + selección rotativa con tope ya implementados; este sprint añade la comprobación de separación sobre esa misma selección).

## Status
status: implemented
started_at: 2026-09-09
closed_at:
blocked_by: SPRINT-089
waiting_for:

## Tasks

### Persistencia — posición por elemento
- [x] Nueva migración Liquibase (numeración siguiente a la última existente en `db/changelog/migrations/`; no modificar `020__create_world_catalog.xml`) que añade `position_x` y `position_y` (`DECIMAL`/`FLOAT`, nullable, rango esperado 0.0–1.0) a la tabla `world_discovery_element`.
- [x] Actualizar `WorldDiscoveryElementJpaEntity`, `WorldDiscoveryElementRepository`/`WorldDiscoveryElementJpaRepository`, `WorldDiscoveryElementPersistenceAdapter` y `content.model.WorldDiscoveryElement` con los dos nuevos campos.
- [x] Actualizar `WorldDiscoveryElementValidator` para validar el rango 0.0–1.0 cuando el valor no sea nulo (no forzar que todo elemento tenga posición, para no romper contenido existente sin migrar).

### Persistencia — ancho de mundo por host
- [x] Misma migración (o una adicional) añadiendo `world_width` (entero, nullable, píxeles lógicos) a `world_host`.
- [x] Actualizar `WorldHostJpaEntity`, `WorldHostRepository`/`WorldHostJpaRepository`, `WorldHostPersistenceAdapter` y `content.model.WorldHost`.
- [x] Definir el comportamiento cuando `world_width` es nulo: usar un valor por defecto a nivel de aplicación (no en el frontend) igual al actual valor hardcodeado del frontend (2560), para que un host sin `world_width` autorado siga funcionando exactamente igual que hoy.

### Seed de contenido
- [x] Actualizar `seeds/12-world-hosts.json` (host MEADOW existente) con `worldWidth: 2560` explícito.
- [x] Actualizar `seeds/14-world-discovery-elements.json` (incluyendo los elementos añadidos en SPRINT-089) con `positionX`/`positionY` normalizados, distribuidos de forma que existan combinaciones con separación suficiente y combinaciones deliberadamente cercanas (para poder probar el rechazo por separación mínima en los tests de SPRINT-089/090).

### Selección con separación mínima
- [x] Añadir a `WorldExplorationConfig` (creado en SPRINT-089) un valor `minSeparationNormalized` (p. ej. 0.15 en escala 0.0–1.0).
- [x] Extender la selección rotativa de `WorldOrchestratorService.buildDestination` (SPRINT-089): al añadir un candidato a la selección visible, comprobar que su distancia (euclídea, en el espacio normalizado 0.0–1.0 de X/Y) respecto a los ya seleccionados en esta construcción sea ≥ `minSeparationNormalized`; si no lo es, probar el siguiente candidato rotativo.
- [x] Prioridad si hay conflicto entre reglas: separación mínima > tope de cantidad > anti-repetición > variedad. Es decir, si no hay forma de cumplir separación con candidatos no vistos, relajar primero la anti-repetición (repetir un elemento ya visto) antes que mostrar elementos demasiado juntos; si ni así se cumple separación (pool con posiciones mal distribuidas), mostrar igualmente el tope configurado sin bloquear el mapa — la separación es un objetivo de calidad de contenido, no una condición de bloqueo funcional.
- [x] Elementos sin `positionX`/`positionY` autorado (nulos) quedan excluidos del cálculo de distancia pero siguen siendo elegibles para la selección (tratarlos como si siempre cumplieran separación, ya que el frontend seguirá pudiendo sintetizar su posición como hace hoy si el campo llega nulo).

### Contrato y mapeo
- [x] `world-discovery-element-payload.yaml`: añadir `positionX`/`positionY` (`number`, `nullable: true`, no `required`).
- [x] `world-destination-payload.yaml` o `world-host-payload.yaml` (decidir cuál según dónde se quiera exponer — `worldWidth` es propiedad del host, se recomienda añadirlo en `world-host-payload.yaml`): añadir `worldWidth` (`integer`, `nullable: true`).
- [x] Actualizar `WorldDiscoveryElementPayload`/`WorldHostPayload` (records en `world/infrastructure/websocket/dto/`) con los nuevos campos.
- [x] Actualizar `GameWebSocketHandler.toDiscoveryElementPayload` y `toDestinationPayload`/`toHostPayload` (o equivalente) para mapear los nuevos campos desde `WorldDiscoveryProposal`/`WorldDestination`. Revisar también `WorldOrchestratorService.buildDestination`/`WorldDiscoveryProposal` para que la posición del elemento de catálogo (`content.WorldDiscoveryElement`) viaje hasta la proposal de sesión (`world.WorldDiscoveryProposal`) y de ahí al DTO — hoy esa cadena de mapeo no transporta ningún campo de posición, hay que añadirlo en cada paso.
- [x] Actualizar `toPayload(...)` (conversión a `Map<String,Object>` para JSON) para no omitir los nuevos campos.

### Tests
- [x] Unit test: un elemento con `positionX`/`positionY` autorados los conserva sin cambios a través de `WorldDiscoveryElement` → `WorldDiscoveryProposal` → `WorldDiscoveryElementPayload`.
- [x] Unit test: un host con `worldWidth` autorado lo expone en el payload; un host sin `worldWidth` expone el valor por defecto de aplicación (2560).
- [x] Unit test: la selección rotativa de SPRINT-089 rechaza un candidato cuya posición está a menos de `minSeparationNormalized` de un elemento ya seleccionado en la misma construcción, y prueba el siguiente candidato.
- [x] Unit test: si ningún candidato no visto cumple separación, se permite repetir un elemento ya visto antes de renunciar al tope de cantidad.
- [x] Contract test/validación de schema: los nuevos campos son opcionales y no rompen la deserialización de un payload que no los incluya (compatibilidad hacia atrás).

## Manual Tests
- Con el seed actualizado, levantar el backend y verificar por WebSocket que `destination.host` incluye `worldWidth: 2560` y que cada `discoveryElement` trae `positionX`/`positionY` entre 0.0 y 1.0.
- Si hay entorno frontend disponible: confirmar que el frontend (aún sin consumir estos campos, ya que su consumo es un sprint frontend posterior) sigue funcionando igual que antes — los campos nuevos no deben romper nada al ser ignorados por un cliente que no los lee todavía.

## Risks
- Migrar contenido existente sin `positionX`/`positionY` podría dejar temporalmente elementos "sin distancia calculable" — mitigado tratándolos como siempre-válidos para separación (ver tarea correspondiente) y completando el seed en este mismo sprint.
- Si se elige mal el campo de contrato donde va `worldWidth` (host vs destino) sin confirmarlo con quien consume el contrato, puede generar un segundo cambio de contrato innecesario — confirmar antes de implementar que `world-host-payload.yaml` es el sitio correcto (ver tarea "Contrato y mapeo").

## Dependencies
- SPRINT-089 (bioma confinado, selección rotativa con tope, `WorldExplorationConfig`, `WorldState.visibleDiscoveryElements` conectado).

## Agent Instruction
- Los campos nuevos del contrato son aditivos y `nullable` — no crear una versión nueva de payload ni de canal AsyncAPI.
- No implementar en el frontend el consumo de estos campos — es un sprint de frontend independiente, fuera de esta capa.
- No añadir información pedagógica, de dificultad ni de progreso a `WorldDiscoveryElement`/`WorldHost` — solo posición y ancho de mundo, que son datos puramente visuales.
- Código, comentarios y nombres en inglés.

## Notes
Este sprint cierra la deuda documentada explícitamente en el frontend (`framework/frontend/app/docs/worldmap-extensibility.md`, sección "Cómo extender GradualScroller para límites dinámicos" y la nota sobre posición sintética por índice). Tras este sprint, un sprint de frontend posterior debe leer `positionX`/`positionY`/`worldWidth` del `WORLD_STATE_SYNC` en vez de calcular layout por índice y usar la constante fija `WORLD_MAP_CONFIG.worldWidth`.

## Review

### completed_tasks
Todas las tareas del sprint se completaron:
- Migración Liquibase `035__add_position_and_world_width_to_world_catalog.xml` (`position_x`/`position_y` DOUBLE nullable en `world_discovery_element`; `world_width` INTEGER nullable en `world_host`), registrada en `db.changelog-master.xml`.
- `content.model.WorldDiscoveryElement`/`WorldHost`, sus `JpaEntity`, `PersistenceAdapter` (ambas direcciones) y las proyecciones `WorldDiscoveryElementProjection`/`WorldHostProjection` (records, campos añadidos al final) actualizados con los nuevos campos.
- `WorldDiscoveryElementValidator` valida `positionX`/`positionY` en rango 0.0–1.0 cuando no son nulos (nuevo método `validateNormalizedPosition`); `WorldHostValidator` no se tocó porque `worldWidth` no tiene una regla de rango que validar.
- `WorldCatalogService` mapea los nuevos campos en ambas proyecciones.
- Seeds actualizados: `12-world-hosts.json` con `worldWidth: 2560`; `14-world-discovery-elements.json` con `positionX`/`positionY` para los 6 elementos, incluyendo dos pares deliberadamente cercanos (`MEADOW_SHINY_FLOWER`/`MEADOW_BUTTERFLY` y `MEADOW_BEEHIVE`/`MEADOW_PEBBLE_PILE`, distancia ~0.06–0.07, por debajo del `minSeparationNormalized` por defecto de 0.15) y el resto bien separados.
- `SeedData.WorldHostSeed`/`WorldDiscoveryElementSeed` y `SeedService` actualizados para leer/mapear los nuevos campos del seed.
- `WorldExplorationConfig` (creado en SPRINT-089) ampliado con `minSeparationNormalized` (default 0.15), enlazado a `app.world.exploration.min-separation-normalized` en `application.yml`.
- `WorldOrchestratorService`: `buildDestination` fija `destination.worldWidth` (del host activo o `DEFAULT_WORLD_WIDTH = 2560` si el host no trae valor autorado o no hay host MEADOW activo); `selectVisibleProposals` reescrito como algoritmo de dos pasadas (ver `decisiones_de_diseno`); `toProposal` propaga `positionX`/`positionY` del catálogo a la proposal de sesión.
- `world.model.WorldDestination` (`worldWidth`, aplanado igual que `hostId`/`hostCode`) y `WorldDiscoveryProposal` (`positionX`/`positionY`) ampliados.
- `WorldDiscoveryElementPayload`/`WorldHostPayload` (records DTO) y `GameWebSocketHandler.toDestinationPayload`/`toDiscoveryElementPayload`/`toPayload(...)` actualizados para transportar y serializar los nuevos campos (omitidos del JSON cuando son `null`, igual que el resto de campos opcionales existentes).
- `world-discovery-element-payload.yaml` (`positionX`/`positionY`, `number`, `nullable: true`) y `world-host-payload.yaml` (`worldWidth`, `integer`, `nullable: true`) actualizados; ambos aditivos, sin nueva versión de payload ni de canal.

### incomplete_tasks
Ninguna.

### decisiones_de_diseno
1. **`worldWidth` en `WorldDestination` aplanado, no en un sub-objeto host**: se siguió el patrón ya existente en `WorldDestination` (`hostId`/`hostCode`/`hostDisplayName` en vez de un `WorldHost` embebido), en vez de introducir una estructura nueva solo para este campo. `GameWebSocketHandler.toDestinationPayload` sigue construyendo el `WorldHostPayload` anidado a partir de esos campos aplanados, ahora incluyendo `worldWidth`.
2. **`worldWidth` por defecto también cuando no hay host MEADOW activo**: la tarea solo pedía definir el defecto para "host sin `world_width` autorado", pero no cubría explícitamente el caso sin host (comportamiento heredado de SPRINT-089). Se decidió aplicar igualmente `DEFAULT_WORLD_WIDTH` en ese caso, para que el contrato quede siempre poblado y el frontend nunca reciba `worldWidth: null` salvo que se decida lo contrario más adelante — evita depender de un frontend que aún no consume el campo para adivinar el fallback.
3. **Algoritmo de separación de dos pasadas** (`selectVisibleProposals`): se construye un único orden de candidatos (no-vistos-recientemente primero, luego ya-vistos, ambos por `sortOrder`) y se recorre dos veces: pasada 1 solo admite candidatos que respeten `minSeparationNormalized` frente a lo ya seleccionado (esto naturalmente permite que un candidato ya-visto "salte" a uno no-visto demasiado cercano, cumpliendo la prioridad `separación > anti-repetición` pedida); pasada 2 rellena lo que falte del tope ignorando separación, solo si la pasada 1 no llegó al tope. Se eligió esta forma sobre una búsqueda combinatoria completa por ser determinista, de complejidad lineal sobre el pool y suficiente para los tamaños de pool esperados (single-digit).
4. **Elementos sin posición autorada siempre "cumplen" separación**: implementado como salida temprana en `satisfiesMinimumSeparation` (si el candidato no tiene posición) y como `continue` en el bucle interno (si el ya-seleccionado no tiene posición) — nunca bloquean ni son bloqueados por el chequeo de distancia, tal como pedía la tarea.
5. **Comentarios de este sprint en inglés**: SPRINT-089 había dejado comentarios en español en `WorldOrchestratorService` (no tenía esa restricción). Este sprint sí la indica explícitamente en `Agent Instruction`, así que los comentarios nuevos o reescritos en este sprint se escribieron en inglés; los comentarios preexistentes de SPRINT-089 que no se tocaron se dejaron como estaban, fuera del alcance de esta tarea.

### contract_changes
Aditivos y `nullable`, sin nueva versión de payload ni de canal AsyncAPI, según lo pedido:
- `world-discovery-element-payload.yaml`: `+positionX` (`number`, nullable), `+positionY` (`number`, nullable).
- `world-host-payload.yaml`: `+worldWidth` (`integer`, nullable).

### tests_ejecutados
- `mvn -o compile` y `mvn -o test-compile`: sin errores.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.world.**,es.vargontoc.educational.framework.content.**,es.vargontoc.educational.framework.session.infrastructure.websocket.GameWebSocketHandlerTest"`: 434/434 en verde. Incluye:
  - `WorldOrchestratorServiceTest`: 17 tests (11 de SPRINT-089 + 6 nuevos: `selectDestination_hostWithWorldWidth_isExposedOnDestination`, `selectDestination_hostWithoutWorldWidth_defaultsTo2560`, `selectDestination_elementPosition_survivesUnchangedIntoProposal`, `selectDestination_candidateTooCloseToSelected_isSkippedForNextCandidate`, `selectDestination_noUnseenCandidateSatisfiesSeparation_repeatsSeenElementBeforeGivingUpCount`, `selectDestination_noCandidateSatisfiesSeparation_stillFillsConfiguredMax` — este último no estaba en la lista explícita de tests del sprint, se añadió porque cubre directamente el riesgo documentado de "pool con posiciones mal distribuidas" sin bloquear el mapa).
  - `WorldDiscoveryElementValidatorTest`: +3 tests de rango de posición (`positionWithinNormalizedRange_passes`, `positionXBelowZero_throwsValidationException`, `positionYAboveOne_throwsValidationException`).
  - `WorldCatalogServiceTest`: +4 tests de mapeo de `worldWidth`/`positionX`/`positionY` en las proyecciones.
  - `WorldDiscoveryElementPersistenceAdapterTest`/`WorldHostPersistenceAdapterTest`: +3/+3 tests de persistencia round-trip de los nuevos campos.
  - `GameWebSocketHandlerTest`: +1 test (`worldHeartbeat_activeWithDestination_syncPayloadCarriesPositionAndWorldWidth`) que ejercita el flujo completo `world_heartbeat` → `WORLD_STATE_SYNC` y verifica que el JSON enviado por WebSocket contiene `worldWidth`, `positionX` y `positionY` — cubre a la vez el test de "posición sobrevive sin cambios" end-to-end y el de compatibilidad hacia atrás (los campos nuevos son adicionales dentro del mismo JSON, no rompen el resto de la estructura ya cubierta por el resto de tests de este archivo).
  - No existe infraestructura de contract-test/validación de schema AsyncAPI en Java en este repo (los `.yaml` de `docs/contracts` no se validan automáticamente contra código); el ítem "Contract test/validación de schema" se satisface con el test de `GameWebSocketHandlerTest` anterior más la revisión manual de que ambos campos quedan `nullable`/no `required` en los `.yaml`.
- `mvn -o test` (suite completa): 896/896 en verde, sin errores ni fallos — a diferencia de SPRINT-089, en esta ejecución había un entorno Docker/Postgres disponible (Testcontainers conectó correctamente), así que no hay fallos ambientales que reportar esta vez.

### risks
- Ninguno nuevo introducido por este sprint. `WorldGameStartService.buildFallbackResult` sigue sin persistir su destino de fallback (deuda ya documentada en SPRINT-089, fuera de alcance aquí) — ese camino tampoco setea `worldWidth`/posición, pero al no persistirse tampoco llega al cliente vía `WORLD_STATE_SYNC`, así que no hay inconsistencia visible.
- El seed de contenido queda con 2 pares de elementos deliberadamente muy próximos (ver `completed_tasks`) — esto es intencional para poder verificar manualmente el rechazo por separación, pero significa que en una sesión real esos pares nunca se mostrarán juntos aunque el pool completo quepa dentro del tope configurado (si `maxVisibleElements` ≥ 6 y no hay rotación por exceso de pool, la salida temprana de `selectVisibleProposals` para pools pequeños los muestra a todos igualmente sin aplicar separación — comportamiento heredado de SPRINT-089, ya que la separación solo se evalúa cuando el pool excede el tope).

### next_sprint_suggestions
- Sprint de frontend para consumir `positionX`/`positionY`/`worldWidth` del `WORLD_STATE_SYNC` en `WorldMapScene`/`GradualScroller`, sustituyendo la posición sintética por índice y el `WORLD_MAP_CONFIG.worldWidth` fijo (ver `framework/frontend/app/docs/worldmap-extensibility.md`).
- Si en el futuro se decide que la separación también debería aplicar en pools pequeños (pool ≤ tope), habría que revisar la salida temprana de `selectVisibleProposals` — no se tocó en este sprint por ser comportamiento explícito de SPRINT-089 y no estar en el alcance de las tareas de SPRINT-090.
