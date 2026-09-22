# SPRINT-111: Migración de WorldDiscoveryElements a Carga Dinámica desde JSON

## Objetivo
Migrar la carga de `WorldDiscoveryElement` desde la base de datos a una carga dinámica directa desde el archivo JSON (`14-world-discovery-elements.json`), eliminando la necesidad de limpiar la BD y reiniciar el seed cada vez que se modifican los elementos del mapa.

## Contexto
Actualmente, los `WorldDiscoveryElement` se cargan desde el seed JSON a la base de datos durante el inicio de la aplicación. Esto obliga a limpiar la BD y reiniciar el seed cada vez que se quiere modificar la posición o agregar/quitar elementos del mapa.

Con esta migración, los elementos se leerán directamente desde el JSON en tiempo de ejecución, permitiendo que los cambios se reflejen inmediatamente con solo reiniciar el servidor.

## Requisitos

### Carga desde JSON
- Los elementos se leen directamente desde `14-world-discovery-elements.json` en cada consulta
- No se persisten en la base de datos
- No se requiere limpieza de BD ni reinicio de seed
- Los cambios en el JSON se reflejan inmediatamente al reiniciar el servidor

### Rendimiento
- Los elementos se cargan en memoria al iniciar la aplicación (cache)
- Las consultas filtran por biome y edad desde el cache en memoria
- No se realizan lecturas de disco en cada consulta

### Compatibilidad
- La interfaz `WorldDiscoveryElementRepository` se mantiene sin cambios
- `WorldCatalogService` no requiere modificaciones
- El frontend no requiere cambios (recibe los elementos vía WebSocket de la misma manera)

## Tareas

### Nuevo Repositorio JSON
- [ ] Crear clase `JsonWorldDiscoveryElementRepository` que implemente `WorldDiscoveryElementRepository`
- [ ] Implementar carga de elementos desde `14-world-discovery-elements.json` al iniciar la aplicación
- [ ] Implementar cache en memoria de los elementos cargados
- [ ] Implementar método `findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual()` que filtre desde el cache
- [ ] Implementar método `findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual()` que filtre desde el cache
- [ ] Implementar método `findByCode()` que busque desde el cache
- [ ] Implementar método `save()` como no-op (o lanzar excepción si se intenta usar)

### Conversión de Datos
- [ ] Crear método para convertir los datos del JSON a objetos `WorldDiscoveryElement` del dominio
- [ ] Mapear campos del JSON a las propiedades del dominio:
  - `code` → `code`
  - `displayName` → `displayName`
  - `elementType` → `elementType` (enum)
  - `biome` → `biome` (enum)
  - `minAge` → `minAge`
  - `maxAge` → `maxAge`
  - `status` → `status` (enum)
  - `activityId` → `activityId`
  - `topicId` → `topicId`
  - `visualAssetKey` → `visualAssetKey`
  - `interactionCueType` → `interactionCueType` (enum)
  - `sortOrder` → `sortOrder`
  - `positionX` → `positionX`
  - `positionY` → `positionY`

### Configuración de Spring
- [ ] Registrar `JsonWorldDiscoveryElementRepository` como bean de Spring en `ContentModuleConfiguration`
- [ ] Reemplazar el bean de `WorldDiscoveryElementJpaRepository` (o mantenerlo pero no usarlo)
- [ ] Asegurar que el repositorio JSON se inyecta correctamente en `WorldCatalogService`

### Eliminación de Código Obsoleto
- [ ] Eliminar o marcar como obsoleto `WorldDiscoveryElementJpaEntity`
- [ ] Eliminar o marcar como obsoleto `WorldDiscoveryElementJpaRepository`
- [ ] Eliminar o marcar como obsoleto `WorldDiscoveryElementPersistenceAdapter`
- [ ] Eliminar el método `loadWorldDiscoveryElements()` de `SeedService`
- [ ] Eliminar la tabla `world_discovery_element` del esquema de BD (si existe migración)

### Tests
- [ ] Test unitario: carga de elementos desde JSON al iniciar
- [ ] Test unitario: filtrado por biome y edad desde el cache
- [ ] Test unitario: búsqueda por código desde el cache
- [ ] Test unitario: conversión correcta de tipos (enums)
- [ ] Test de integración: `WorldCatalogService` devuelve elementos correctos desde el JSON
- [ ] Test de integración: los cambios en el JSON se reflejan al reiniciar la aplicación

## Criterios de Aceptación

1. Los elementos se cargan desde `14-world-discovery-elements.json` al iniciar la aplicación
2. No se persisten en la base de datos
3. Las consultas filtran correctamente por biome y edad desde el cache en memoria
4. Los cambios en el JSON se reflejan inmediatamente al reiniciar el servidor
5. No se requiere limpieza de BD ni reinicio de seed
6. La interfaz `WorldDiscoveryElementRepository` se mantiene sin cambios
7. `WorldCatalogService` funciona correctamente con el nuevo repositorio
8. El frontend recibe los elementos vía WebSocket de la misma manera
9. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Cache en memoria**: Los elementos se cargan una vez al iniciar y se mantienen en memoria para evitar lecturas de disco en cada consulta
- **Filtrado en memoria**: Los filtros por biome y edad se aplican sobre el cache, no sobre la BD
- **No-op en save()**: El método `save()` no hace nada (o lanza excepción) porque los elementos no se persisten
- **Compatibilidad**: La interfaz `WorldDiscoveryElementRepository` se mantiene para no afectar a `WorldCatalogService`
- **Rendimiento**: La carga inicial es más lenta (lee el JSON completo), pero las consultas son más rápidas (filtro en memoria)

## Dependencias

- SPRINT-097 completado (infraestructura de similitud)
- FEAT-013 completado (interacción visual básica)
- ADR-030 aceptado

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (refactorización de capa de persistencia)
- **Riesgo:** Bajo (cambio interno, no afecta a frontend ni a la lógica de negocio)
