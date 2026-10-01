# SPRINT-117: Rendimiento WebSocket de juego - Caché de datos estáticos y consultas

## Objetivo
Eliminar consultas repetidas de datos que casi no cambian (catálogo de mundo, elementos, colores accesibles, perfil, catálogo de avatar) y las lecturas redundantes en `auth`, preparando la lógica para más edades.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- `auth` ejecuta unas 10 o más SELECT (`getSession` dos veces, hosts consultados dos veces, 3 consultas de catálogo, 3 lecturas del avatar).
- `gameStateToPayload` consulta y parsea en cada acción: `findAllById` de `RecognitionElement`, `readTree(resourceRefs)`, hasta 2 SELECT por elemento con color accesible y `getChild`.
- N+1 en `resolveCandidates`/`resolveMemoryCandidates` (un `findByTopicIdAndStatus` por topic).
- No existe caché; Caffeine ya es dependencia (solo se usa en audio).
- La edad (3) está fijada en el handler (`GameWebSocketHandler`: `getNewWorld` y `handleWorldTravel`).

## Decisión sobre el contrato (usuario, 2026-10-01)
Se cambia el contrato (p. ej. no reenviar `memoryState.elements` en cada volteo) **solo si no rompe demasiado y compensa**. Por defecto este sprint **no cambia el contrato**: cachea en servidor. La medición posterior a la caché decide si hace falta la optimización de contrato (se abriría un sprint aparte con frontend).

## Tareas
### Infraestructura de caché
- [ ] `@EnableCaching` con Caffeine: tamaños máximos y TTL configurables en `application.yml`
- [ ] Caché de `RecognitionElement` por id (y `JsonNode` de `resourceRefs` ya parseado)
- [ ] Caché de `AccessibleColor` y `AccessibleColorPalette` por (id, `ColorVisionMode`)
- [ ] Caché del catálogo de mundo: hosts, situaciones y elementos por (bioma, edad)
- [ ] Caché del catálogo de eventos de avatar por tipo de evento
- [ ] Caché de `ChildProfile` por id con invalidación al editarlo el adulto (ADR-022: color vision mode, nombre, etc.)
- [ ] Invalidación al cambiar contenido desde el gestor de contenido (dev CRUD)
- [ ] Métricas de aciertos/fallos de caché (SPRINT-114)

### Consultas
- [ ] `auth`: un único `getSession` reutilizado por `getNewWorld` y el avatar; hosts consultados una vez
- [ ] Sustituir el N+1 de candidatos por una consulta `IN` por topics
- [ ] `buildCandidateMetadata`: no cargar todos los candidatos si solo se necesitan los de la ronda (verificar sin alterar la selección ni la anti-repetición)
- [ ] `RoundAudioService`: reutilizar el `ChildProfile` y el elemento ya cargados en lugar de volver a consultarlos
- [ ] `processAction`: eliminar `getChild` repetido al resolver el engine de COLOR

### Edad escalable
- [ ] Centralizar la edad efectiva del mundo en un único punto (`ChildAgeResolver` o equivalente en el módulo `world`/`family`), que hoy devuelve 3 y pueda derivarse del mes/año de nacimiento del perfil en el futuro
- [ ] El handler y los servicios de mundo dejan de usar el literal `3`; las claves de caché incluyen la edad
- [ ] Sin cambio de comportamiento en esta versión

### Tests
- [ ] Segunda petición del mismo dato no llega a BD (verificado con contador de SQL)
- [ ] Invalidación: cambiar el modo de visión de color del perfil se refleja en el siguiente payload
- [ ] Invalidación: modificar contenido en el gestor se refleja tras el TTL o la invalidación
- [ ] Equivalencia: los payloads de `GAME_STARTED`, `GAME_READY` y `GAME_ACTION_RESULT` son idénticos a los de antes (tests de contrato/snapshot)
- [ ] `ChildAgeResolver` devuelve 3 y es el único origen de la edad

### Pruebas manuales
- [ ] Cambiar desde el panel parental el modo de color: la siguiente partida usa la paleta nueva sin reiniciar el servidor
- [ ] Editar un elemento desde el gestor de contenido: aparece en juego según TTL/invalidación
- [ ] Comparar el tiempo percibido de `auth` y del primer toque con la línea base

## Criterios de Aceptación
1. `auth` baja a ≤ 4 SELECT y cumple p95 < 300 ms en el escenario de SPRINT-114
2. `game_action` en Recognition cumple p95 < 100 ms
3. Los payloads WS son idénticos (sin cambio de contrato)
4. Las cachés se invalidan correctamente al cambiar perfil o contenido
5. El literal de edad fija desaparece del handler

## Contratos y dependencias
- Sin cambios en `docs/contracts` salvo decisión posterior sobre `elements` de Memory (queda como propuesta; requiere analista frontend).
- Datos del niño en caché: solo en memoria del proceso, sin persistir nada adicional ni compartir.

## Riesgos
- Datos obsoletos si falla la invalidación: TTL corto como red de seguridad.
- Una caché mal acotada consume memoria: tamaños máximos obligatorios.

## Dependencias
- SPRINT-114 completado; recomendable SPRINT-115 y 116.

## Estimación
- **Tamaño:** L | **Riesgo:** Medio
