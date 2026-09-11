# Sprint 091 - backend
# -----------------------------------------------

## Goal
Ampliar el catálogo de biomas del dominio `world` de un único bioma confinado (MEADOW, SPRINT-089) a los 6 biomas del recorrido acordado en ADR-026, con un orden lineal configurable sin desplegar código.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-11) sobre el estado real del código:

- `WorldOrchestratorService.selectDestination` filtra explícitamente `hosts` por `biome == Biome.MEADOW` (SPRINT-089) y solo cae a ese valor hardcodeado si no hay hosts activos. No hay ningún otro bioma dado de alta hoy.
- `WorldHost` (dominio, JPA, seed `12-world-hosts.json`) ya soporta múltiples hosts de distinto bioma en su esquema — el confinamiento es una regla de negocio en `WorldOrchestratorService`, no una limitación de modelo de datos. Ampliar el catálogo es principalmente seed + relajar ese filtro, no una migración de esquema nueva.
- No existe hoy ningún concepto de "orden" entre biomas en el modelo.

**Decisión confirmada por el usuario (2026-09-11):** el orden de los 6 biomas debe ser configurable por contenido sin desplegar backend — no hardcodeado en el enum `Biome` ni en código de `WorldOrchestratorService`.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-090
waiting_for:

## Tasks

### Catálogo de biomas
- [ ] Dar de alta en `Biome` (o donde resida el catálogo de valores) los 5 biomas restantes: Granja, Bosque encantado (WOODS), Playa, Espacio, Prehistoria (dinosaurios) — manteniendo Pradera (MEADOW) existente. Confirmar codificación exacta de cada valor con quien mantenga el contrato (`biome` es hoy un `string` libre en `world-destination-payload.yaml`, no un enum validado contractualmente).
- [ ] Relajar el filtro `biome == Biome.MEADOW` de `WorldOrchestratorService.selectDestination` para operar sobre "cualquier bioma del catálogo activo", conservando el comportamiento de "mundo no disponible" cuando no hay host activo para la edad del niño en el bioma solicitado (no inventar mapa vacío ni error técnico).
- [ ] Confirmar con frontend/producto si Pradera y Bosque encantado, al ser visualmente distintos pero ambos "de bosque/vegetación", necesitan alguna distinción adicional a nivel de dato (p. ej. un `displayName`/`visualAssetKey` ya existente en `WorldHost` debería bastar; documentar la decisión, no una tarea de código nueva si ya alcanza).

### Orden lineal configurable
- [ ] Añadir un campo de orden (p. ej. `sequence_order`, entero) a `world_host` vía migración Liquibase (numeración siguiente a la última existente en `db/changelog/migrations/`), en vez de derivarlo del orden del enum `Biome` o de un valor hardcodeado en el servicio.
- [ ] Actualizar `WorldHostJpaEntity`, `WorldHostRepository`/`WorldHostJpaRepository`, `WorldHostPersistenceAdapter` y `content.model.WorldHost` con el nuevo campo.
- [ ] Exponer el orden en el contrato (`world-host-payload.yaml`, campo aditivo `nullable`, mismo patrón que `worldWidth` en SPRINT-090) para que frontend pueda disponer los stickers del selector de destino (SPRINT-067 frontend) según este orden, sin que ello implique bloqueo de acceso a biomas posteriores.
- [ ] Seed (`12-world-hosts.json`): un host por bioma con su `sequence_order` según ADR-026 (Pradera=1, Granja=2, Bosque encantado=3, Playa=4, Espacio=5, Prehistoria=6).

### Tests
- [ ] Unit test: `selectDestination` puede devolver un destino para cualquiera de los 6 biomas seedados, no solo MEADOW.
- [ ] Unit test: sin host activo para un bioma solicitado y edad dada, se aplica el mismo comportamiento de "mundo no disponible" ya existente (no error, no mapa vacío inventado).
- [ ] Unit test: el orden (`sequence_order`) se persiste, se mapea y se expone en el payload del host sin afectar a la selección de destino en sí (el orden es solo dato de presentación, no una regla de acceso).
- [ ] Contract test: el nuevo campo de orden es opcional y no rompe deserialización de un payload que no lo incluya.

## Manual Tests
- Con el seed ampliado a 6 biomas, levantar backend y verificar por WebSocket que `world_heartbeat` puede resolver destino para cada uno de los 6 (por ejemplo, forzando el bioma vía el mecanismo de selección de SPRINT-092 una vez exista, o mediante un host de prueba forzado a cada bioma si SPRINT-092 no está listo aún).
- Verificar que el payload de host incluye el orden esperado para cada uno de los 6.

## Risks
- Codificar mal el catálogo de biomas como `string` libre (en vez de un enum validado) puede permitir valores inconsistentes entre backend y frontend — evaluar si conviene formalizar `Biome` como enum contractual en este sprint o dejarlo documentado como deuda explícita si se decide no romper compatibilidad ahora.
- Relajar el filtro de MEADOW sin cuidado podría reintroducir la violación de FEAT-010 §4 AC3 que SPRINT-089 cerró (más de los elementos permitidos visibles a la vez) si algún bioma nuevo tiene un pool de elementos mal configurado — el límite `maxVisibleElements` de `WorldExplorationConfig` ya es por-construcción de destino, no por bioma, así que no debería verse afectado, pero conviene un test de regresión explícito.

## Dependencies
- SPRINT-089 (confinamiento a MEADOW a relajar), SPRINT-090 (worldWidth/posición ya implementados, deben seguir funcionando para todos los biomas nuevos, no solo MEADOW).
- Frontend: SPRINT-066 (worldWidth por host ya se consume de forma genérica, no debería requerir cambios adicionales al añadir biomas).
- Contenido: assets y adecuación por edad de los 5 biomas nuevos (fuera de esta capa).

## Agent Instruction
- No implementar el contrato de selección de destino/transporte (eso es SPRINT-092) ni la persistencia de sesión entre reinicios (SPRINT-093) — este sprint solo amplía el catálogo y el orden.
- Los campos nuevos del contrato son aditivos y `nullable` — no crear una versión nueva de payload ni de canal AsyncAPI.
- No añadir información pedagógica, de dificultad ni de progreso al catálogo de biomas.
- Código, comentarios y nombres en inglés.

## Notes
- Este sprint es prerrequisito de SPRINT-092 (selección de destino) y de SPRINT-067 frontend (selector visual), pero no depende de ninguno de los dos para completarse.

## Review

### completed_tasks
(Pendiente de implementación)

### incomplete_tasks
(Pendiente de implementación)
