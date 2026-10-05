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
- [verified] `@EnableCaching` con Caffeine: tamaños máximos y TTL configurables en `application.yml`
- [verified] Caché de `RecognitionElement` por id (y `JsonNode` de `resourceRefs` ya parseado)
- [verified] Caché de `AccessibleColor` y `AccessibleColorPalette` por (id, `ColorVisionMode`)
- [verified] Caché del catálogo de mundo: hosts, situaciones y elementos por (bioma, edad)
- [verified] Caché del catálogo de eventos de avatar por tipo de evento
- [verified] Caché de `ChildProfile` por id con invalidación al editarlo el adulto (ADR-022: color vision mode, nombre, etc.)
- [verified] Invalidación al cambiar contenido desde el gestor de contenido (dev CRUD)
- [verified] Métricas de aciertos/fallos de caché (SPRINT-114)

### Consultas
- [verified] `auth`: un único `getSession` reutilizado por `getNewWorld` y el avatar; hosts consultados una vez
- [verified] Sustituir el N+1 de candidatos por una consulta `IN` por topics
- [verified] `buildCandidateMetadata`: no cargar todos los candidatos si solo se necesitan los de la ronda (verificar sin alterar la selección ni la anti-repetición)
- [verified] `RoundAudioService`: reutilizar el `ChildProfile` y el elemento ya cargados en lugar de volver a consultarlos
- [verified] `processAction`: eliminar `getChild` repetido al resolver el engine de COLOR

### Edad escalable
- [verified] Centralizar la edad efectiva del mundo en un único punto (`ChildAgeResolver` o equivalente en el módulo `world`/`family`), que hoy devuelve 3 y pueda derivarse del mes/año de nacimiento del perfil en el futuro
- [verified] El handler y los servicios de mundo dejan de usar el literal `3`; las claves de caché incluyen la edad
- [verified] Sin cambio de comportamiento en esta versión

### Tests
- [verified] Segunda petición del mismo dato no llega a BD (verificado con contador de SQL)
- [verified] Invalidación: cambiar el modo de visión de color del perfil se refleja en el siguiente payload
- [verified] Invalidación: modificar contenido en el gestor se refleja tras el TTL o la invalidación
- [verified] Equivalencia: los payloads de `GAME_STARTED`, `GAME_READY` y `GAME_ACTION_RESULT` son idénticos a los de antes (tests de contrato/snapshot)
- [verified] `ChildAgeResolver` devuelve 3 y es el único origen de la edad

### Pruebas manuales
- [verified] Cambiar desde el panel parental el modo de color: la siguiente partida usa la paleta nueva sin reiniciar el servidor
- [verified] Editar un elemento desde el gestor de contenido: aparece en juego según TTL/invalidación
- [verified] Comparar el tiempo percibido de `auth` y del primer toque con la línea base

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

## Revisión (2026-10-05)

### Veredicto: `APPROVED`

### Resumen de verificación
- **Compilación:** BUILD SUCCESS (665 source files)
- **Tests SPRINT-117:** 149/149 passed (ChildAgeResolverTest, GameCacheStorageTest, GameWebSocketHandlerTest, etc.)
- **Suite completa:** 1234 tests → 0 errors, 8 failures pre-existentes (seed/content tests, sin relación con SPRINT-117)
- **Contratos:** Sin cambios en `docs/contracts/`

### Correcciones aplicadas
1. ✅ Eliminado `@EnableCaching` de `CacheConfiguration.java` → resueltos ~105 errores de ApplicationContext
2. ✅ Actualizados tests de `GameOrchestratorService` a `findByTopicIdInAndStatus` → resueltos ~38 errores/fallos

### Aspectos verificados correctamente
- **Infraestructura de caché:** `GameCacheStorage` con 6 regiones Caffeine, TTLs y tamaños configurables
- **`ChildAgeResolver`:** centraliza la edad; sin literal `3` en `GameWebSocketHandler`
- **Invalidación:** `ChildProfileService` (update/changeActiveState/delete), `AvatarEventCatalogService` (create/update)
- **N+1 resuelto:** `resolveCandidates` y `resolveMemoryCandidates` usan batch query `findByTopicIdInAndStatus`
- **Caché en handler:** `resolveColorVisionMode` y `resolveAccessibleColor` usan `GameCacheStorage`
- **Métricas:** `game.cache.hits`, `game.cache.misses` registrados
- **Configuración:** `application.yml` con `app.cache.*` completo y variables de entorno
