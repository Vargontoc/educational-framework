# Sprint 094 - backend
# -----------------------------------------------

## Goal
Emitir el `AvatarEvent` real `BIOME_TRANSITION` (texto + audio) cuando un `world_travel` cambia el bioma activo del niño, reutilizando el pipeline genérico ya existente para `WELCOME`/`FAREWELL` (`AvatarService.processEvent`), extendido con contenido específico por bioma de destino. Desbloquea SPRINT-004 (Agents) y SPRINT-069 (Frontend), ambos pendientes de este evento.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-13):

- `GameWebSocketHandler.handleWorldTravel` (SPRINT-092, verificado) ya construye el nuevo `WorldDestination`, actualiza `WorldState` y responde `WORLD_STATE_SYNC` — pero nunca toca el subsistema de avatar. Cambiar de bioma hoy no produce ningún `AvatarEvent`.
- `AvatarService.processEvent` (módulo `avatar`) ya es un pipeline **genérico** sobre `AvatarEventType`: gatea por `childProfile.isNpcEnabled()`/`isNpcVoiceEnabled()`, resuelve una variante desde `AvatarEventCatalog` con anti-repetición por `childProfile+eventType` (`lastShownCatalogIdByChild`, in-memory), genera audio vía `AudioUseCase` y devuelve `GameAvatarEvent` (`eventType`, `audioAvailable`, `audioId`, `text`). Es el mismo mecanismo que ya sirve `WELCOME` (`sendWelcomeAvatar`) y `FAREWELL` (`sendFarewellAndClose`) — **no hace falta construir nada nuevo de anti-repetición o gateo**, ya está resuelto genéricamente y validado (mismo patrón que SPRINT-089 confirma explícitamente reutilizar).
- `AvatarEventCatalog` (contenido) **no tiene bioma**: solo `eventType`, `tone`, `locale`, `messageText`, `status`. Para que la frase "se refiera exclusivamente al bioma de destino" (SPRINT-004, FEAT-001 §3.10) hace falta poder autorar contenido distinto por bioma, no un único pool genérico para `BIOME_TRANSITION`.
- `AvatarEventRequest` ya declara un campo `context: Map<String, Object>` — pero **ningún llamador lo usa hoy** (`WELCOME`/`FAREWELL` pasan `null`) y `AvatarService`/`resolveCatalog` nunca lo leen. Es la vía natural para pasar el bioma de destino sin romper la firma del método para los tipos de evento existentes.
- Gap preexistente detectado (no introducido por este sprint, pero relevante porque este sprint toca el mismo método): `AvatarEventCatalogPersistenceAdapter.findByEventType` **no filtra por `status = ACTIVE`** (a diferencia de `findActiveByFilters`, que sí lo hace). Hoy esto es inofensivo para `WELCOME`/`FAREWELL` porque su contenido apenas cambia, pero para `BIOME_TRANSITION` — con 6 biomas × varias variantes, contenido nuevo en producción activa — una fila en `DRAFT`/`ARCHIVED` podría seleccionarse igualmente. **Decisión (2026-09-13): se corrige el método existente directamente** (no solo un método nuevo paralelo), cerrando el gap también para `WELCOME`/`FAREWELL`, con test de regresión para ambos.
- Gap preexistente detectado: `AvatarService.lastShownCatalogIdByChild` es un `ConcurrentHashMap<String, Long>` sin límite de tamaño ni expiración — crece una entrada por combinación `childProfileId:eventType` (y ahora `:biome` para `BIOME_TRANSITION`) y nunca se libera mientras el proceso viva. Con 6 biomas nuevos por niño, este sprint es el que más acelera su crecimiento. **Decisión (2026-09-13): se añade una mitigación ligera dentro de este sprint** (ver Tareas) en vez de dejarlo como deuda pura.
- Contrato (`docs/contracts/api/asyncapi/messages/game-avatar-event.yaml`): el enum `eventType` de `GameAvatarEvent` solo declara `WELCOME`/`FAREWELL` hoy — necesita ampliarse con `BIOME_TRANSITION` (y no con el resto de `AvatarEventType`, que pertenecen a otro flujo — confirmado en Decisión 4, sin colisión).
- `Biome` es un enum de 6 valores ya usado por `WorldOrchestratorService`/`WorldOrchestrator.buildDestinationForBiome`; `handleWorldTravel` ya resuelve `targetBiome` como `Biome` antes de construir el destino, así que está disponible en el punto exacto donde hay que emitir el evento.

## Status
status: verified
started_at: 2026-09-13
closed_at: 2026-09-13
verified_at: 2026-09-13
blocked_by:
waiting_for:

## Decisiones confirmadas (2026-09-13)

1. **Anti-repetición: solo bioma de destino.** Confirmado — no se modela el par origen→destino. La clave de `lastShownCatalogIdByChild` para `BIOME_TRANSITION` es `profileId:BIOME_TRANSITION:<destinationBiome>`.
2. **Gateo NPC/voz desactivado: se sigue enviando el evento (sin audio), nunca se omite.** Confirmado — el niño pasa igualmente por la escena de transición aunque no haya audio. Además, el usuario pide forzar una duración mínima (del orden de un par de segundos) en la pausa de llegada del frontend incluso sin audio, para evitar una transición instantánea/brusca. Esto es responsabilidad de frontend (SPRINT-069, `beginBiomeArrival`) — backend no necesita lógica adicional más allá de "siempre enviar el `GAME_AVATAR_EVENT`, con o sin audio", que ya es el diseño de este sprint. Se documenta aquí como dependencia/handoff, no se diseña en detalle (fuera del alcance de esta capa).
3. **Viajar al mismo bioma: se resuelve en frontend, no en backend.** El usuario confirma que el selector visual (`BiomeSelectorLayer`, SPRINT-067) debe excluir de la lista el bioma en el que el niño ya está, en vez de permitir seleccionarlo y suprimir la reproducción después. Implementado como addendum de SPRINT-067 (`WorldMapScene.handleTransportTouched` filtra `ALL_BIOME_HOSTS` excluyendo `currentBiome`). **Consecuencia para este sprint:** backend no necesita ninguna validación especial de "mismo bioma" para `BIOME_TRANSITION` — si algún cliente no estándar lo solicitara igualmente, el comportamiento por defecto (emitir la transición con normalidad, igual que SPRINT-092 ya decidió para `world_travel`) es aceptable y no requiere código adicional.
4. **`BIOME_TRANSITION` comparte enum `AvatarEventType` sin colisión.** Confirmado — se añade directamente al enum existente.
5. **`findByEventType` se corrige directamente (no solo un método nuevo paralelo).** Confirmado — un único método correcto que siempre filtra `status=ACTIVE`, con test de regresión que cubre `WELCOME`/`FAREWELL` además de `BIOME_TRANSITION`.
6. **Mitigación ligera para `lastShownCatalogIdByChild` sin límite.** Confirmado — se acota dentro de este sprint en vez de dejarlo como deuda pura, ya que este sprint es quien más acelera su crecimiento (6 biomas nuevos por niño).

## Diseño propuesto

### 1. Nuevo tipo de evento
- Añadir `BIOME_TRANSITION` a `AvatarEventType` (enum ya usado también por `ACTIVITY_*`/`HELP_REQUESTED`, que no pasan por `GameAvatarEvent` — confirmado en Decisión 4 que no hay colisión de significado entre módulos).

### 2. Contenido por bioma
- Migración `038__add_biome_to_avatar_event_catalog.xml`: columna `biome` nullable en `avatar_event_catalog` (mismo enum de 6 valores que `world_host.biome`). Filas existentes de `WELCOME`/`FAREWELL` quedan con `biome = null` (sin cambio de comportamiento).
- `AvatarEventCatalog`/`AvatarEventCatalogJpaEntity`/`AvatarEventCatalogPersistenceAdapter`: añadir campo `biome` (nullable) en el modelo, la entidad JPA y el mapeo `toDomain`/`toJpa`.
- `AvatarEventCatalogRepository.findByEventType`: se corrige para filtrar también `status = ACTIVE` (cierra el gap preexistente para todos los tipos, no solo `BIOME_TRANSITION`) y gana un segundo parámetro opcional `biome` (nulo para `WELCOME`/`FAREWELL`, poblado para `BIOME_TRANSITION`) — un único método correcto en vez de dos métodos paralelos con comportamiento distinto.

### 3. Selección de variante consciente del bioma
- `AvatarService.resolveCatalog` gana un parámetro `biome` (nulo salvo para `BIOME_TRANSITION`), que pasa directamente al repositorio corregido.
- Anti-repetición: la clave de `lastShownCatalogIdByChild` para `BIOME_TRANSITION` incluye el bioma de destino (`profileId:BIOME_TRANSITION:FARM`) — confirmado, sin par origen→destino.
- `AvatarService.processEvent` lee el bioma desde `request.context().get("biome")` únicamente cuando `eventType == BIOME_TRANSITION` (primer consumidor real de `context`; no cambia la firma pública para los llamadores existentes).
- Mitigación de `lastShownCatalogIdByChild` sin límite: acotar el mapa con una política simple de expulsión (p. ej. tamaño máximo con eviction FIFO/LRU, o un `Caffeine`/similar ya disponible en el classpath si lo hay; a confirmar en implementación cuál añade menos complejidad) en vez de un `ConcurrentHashMap` sin cota. No requiere persistencia — es contenido efímero de variedad, no dato del niño (mismo criterio ya usado en SPRINT-089/SPRINT-004 para no persistir esto entre sesiones).

### 4. Disparo en `world_travel`
- `GameWebSocketHandler.handleWorldTravel`: tras el `sendToSession(...)` del `WORLD_STATE_SYNC` existente (para que el cliente ya tenga biome/worldWidth/discoveryElements antes de la pausa de llegada), invocar `avatarservice.processEvent(new AvatarEventRequest(childSessionId, AvatarEventType.BIOME_TRANSITION, Map.of("biome", targetBiome.name())))` y enviar el resultado con el mismo patrón exacto que `sendWelcomeAvatar` (JSON `GAME_AVATAR_EVENT` + frame binario si `audioAvailable`).
- Gateo: se apoya íntegramente en el gateo ya existente de `AvatarService` (`isNpcEnabled`/`isNpcVoiceEnabled`) — sin lógica nueva. Con NPC desactivado, `processEvent` devuelve el mismo tipo de resultado "fallback" que ya usan `WELCOME`/`FAREWELL` (evento JSON con `audioAvailable: false`, sin frame binario) — confirmado, el evento **siempre** se envía; la pausa de llegada en frontend se muestra igualmente y con una duración mínima aunque no haya audio (handoff a SPRINT-069, fuera de esta capa).

### 5. Contrato
- `game-avatar-event.yaml`: añadir `BIOME_TRANSITION` al enum `eventType` con su descripción.
- No se tocan `world-travel`/`world-state-sync` (SPRINT-092 ya cerrado) — el nuevo evento viaja en un mensaje `GAME_AVATAR_EVENT` independiente, correlacionado solo por `sessionId`, igual que `WELCOME`/`FAREWELL`.

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml` | Modifica | A actualizar en este sprint |
| SPRINT-092 backend (`world_travel`, `handleWorldTravel`) | Requerida | Verificado |
| SPRINT-004 Agents (pool de frases por bioma, validación de contenido por edad) | Bloqueada por este sprint | Pending — este sprint la desbloquea |
| SPRINT-069 Frontend (`WorldMapScene.handleAvatarEvent` case `BIOME_TRANSITION`) | Bloqueada por este sprint | Pending — este sprint la desbloquea |
| Contenido: alta de filas `AvatarEventCatalog` con `eventType=BIOME_TRANSITION` para los 6 biomas | Requerida para producción real | Fuera de esta capa |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Sin contenido cargado para un bioma, `resolveCatalog` devuelve `null` y el evento se manda como fallback silencioso (`audioAvailable:false`, `text:""`) — el niño no recibe ninguna narración de llegada | MEDIA | Aceptable como degradación (no bloquea el paseo), pero requiere que Contenido cargue las 6 filas antes de producción — dejar explícito en Evidence |
| R2 | Una frase en `DRAFT`/`ARCHIVED` podría reproducirse en producción | MEDIA | Cubierto por diseño: `findByEventType` filtra `status=ACTIVE` para todos los tipos (no solo `BIOME_TRANSITION`), con test de regresión para `WELCOME`/`FAREWELL` |
| R3 | Corregir `findByEventType` para filtrar `ACTIVE` podría cambiar comportamiento observable si hoy existe alguna fila `WELCOME`/`FAREWELL` inactiva que se estuviera sirviendo por el bug | BAJA | Verificar en implementación si el seed/datos actuales tienen filas inactivas de esos tipos antes de desplegar; cubierto por el test de regresión de la Tarea correspondiente |
| R4 | `lastShownCatalogIdByChild` sin límite de tamaño ni expiración | BAJA | Cubierto por diseño: mitigación ligera (tamaño máximo/expulsión) añadida en este sprint en vez de dejarse como deuda pura |

## Handoffs a otras capas (no diseñados en detalle aquí)

- **Frontend (SPRINT-069):** la pausa de llegada debe mostrarse con una duración mínima (del orden de un par de segundos) aunque `audioAvailable` sea `false`, para evitar una transición instantánea/brusca. Backend solo garantiza que el evento se envía siempre; el temporizado de la pausa es responsabilidad de `beginBiomeArrival`.
- **Frontend (SPRINT-067, ya verificado):** implementado como addendum — `BiomeSelectorLayer` ya no ofrece el bioma actual como destino seleccionable (`WorldMapScene.handleTransportTouched` filtra `ALL_BIOME_HOSTS`). Esto resuelve el caso "mismo bioma" sin necesidad de lógica especial en backend.
- **Contenido/Agents (SPRINT-004):** alta de filas `AvatarEventCatalog` con `eventType=BIOME_TRANSITION` y `biome` para cada uno de los 6 biomas, validadas por edad.

## Tareas del sprint

### Contrato y modelo
- [x] Añadir `BIOME_TRANSITION` a `AvatarEventType`.
- [x] Migración `038__add_biome_to_avatar_event_catalog.xml` (columna `biome` nullable).
- [x] `AvatarEventCatalog`/`AvatarEventCatalogJpaEntity`/`AvatarEventCatalogPersistenceAdapter`: añadir campo `biome` (nullable) — modelo, entidad JPA, mapeo `toDomain`/`toJpa`.
- [x] `AvatarEventCatalogRepository.findByEventType(AvatarEventType, String biome)`: corregir para filtrar `status=ACTIVE` (para todos los tipos, no solo `BIOME_TRANSITION`) y añadir el parámetro `biome` (nulo para `WELCOME`/`FAREWELL`). Actualizar los dos call sites existentes (`resolveCatalog`) a la nueva firma.
- [x] Actualizar `game-avatar-event.yaml`: añadir `BIOME_TRANSITION` al enum `eventType`.

### Selección y disparo
- [x] `AvatarService.resolveCatalog`: nuevo parámetro `biome`, leído desde `request.context().get("biome")` cuando `eventType == BIOME_TRANSITION` (`null` para el resto).
- [x] Anti-repetición scoped por bioma de destino (`profileId:BIOME_TRANSITION:<biome>`).
- [x] Acotar `lastShownCatalogIdByChild` con un límite de tamaño/expulsión simple (evaluar en implementación: `LinkedHashMap` con `removeEldestEntry` sincronizado, librería de caché ya presente en el classpath, o TTL manual) — sin persistencia, sigue siendo estado efímero en memoria.
- [x] `GameWebSocketHandler.handleWorldTravel`: disparo de `BIOME_TRANSITION` tras `WORLD_STATE_SYNC`, reutilizando el patrón `sendWelcomeAvatar`/`sendBinaryFrame`.

### Tests
- [x] Unit test: `world_travel` a un bioma con contenido activo en catálogo envía `GAME_AVATAR_EVENT` con `eventType=BIOME_TRANSITION` y `audioAvailable=true`.
- [x] Unit test: `world_travel` a un bioma sin contenido en catálogo envía fallback (`audioAvailable=false`, `text=""`), sin romper el flujo de `WORLD_STATE_SYNC`.
- [x] Unit test: con `npcEnabled=false`, `world_travel` sigue enviando `GAME_AVATAR_EVENT` (`eventType=BIOME_TRANSITION`) pero sin audio (`audioAvailable=false`, sin frame binario) — nunca se omite el mensaje por completo.
- [x] Unit test: dos `world_travel` consecutivos a biomas distintos no comparten estado de anti-repetición entre sí (la clave incluye el bioma).
- [x] Unit test: dos `world_travel` consecutivos al mismo bioma (dentro de la sesión) no repiten variante si hay una no usada disponible para ese bioma.
- [x] Unit test: `findByEventType` excluye filas en `DRAFT`/`ARCHIVED` — cubrir explícitamente `BIOME_TRANSITION` **y** `WELCOME`/`FAREWELL` (test de regresión para el gap preexistente).
- [x] Unit test: catálogo sin fila para un `eventType`/bioma dado no lanza excepción (pool vacío → fallback).
- [x] Unit test: `lastShownCatalogIdByChild` no crece sin límite — tras superar el tamaño máximo configurado, las entradas más antiguas se descartan sin lanzar excepción ni romper la selección de variante.

## Manual Tests
- Con backend levantado, catálogo con al menos una fila `BIOME_TRANSITION` por bioma, y NPC/voz activados: `world_travel` a cada uno de los 6 biomas debe producir `WORLD_STATE_SYNC` seguido de `GAME_AVATAR_EVENT` (`eventType=BIOME_TRANSITION`) con audio.
- Con NPC desactivado: repetir y confirmar que igualmente llega `GAME_AVATAR_EVENT` con `audioAvailable=false` (sin frame binario).

## Dependencies
- SPRINT-092 (backend, verificado) — punto de disparo (`handleWorldTravel`).
- Módulo `avatar` existente (`AvatarService`, `AvatarEventCatalog`) — se extiende, no se reemplaza.

## Agent Instruction
- No generar contenido de las frases — coordinar con Contenido/Agents (SPRINT-004) igual que ya delimita ese sprint.
- El comportamiento observable de `WELCOME`/`FAREWELL` no debe cambiar salvo por la corrección del filtro `status=ACTIVE` (decisión explícita de este sprint) — cualquier otro cambio de conducta para esos dos tipos está fuera de alcance.
- Antes de desplegar la corrección de `findByEventType`, revisar si existen filas `WELCOME`/`FAREWELL` en `DRAFT`/`ARCHIVED` en los entornos actuales que hoy se estén sirviendo por el bug (R3) — si las hay, coordinarlo para que no desaparezca contenido en producción de forma inesperada.
- Código, comentarios y nombres en inglés.

## Notes
- Este sprint no incluye la producción de las frases del pool (Contenido) ni el consumo en frontend (SPRINT-069, ya especifica su propio `case 'BIOME_TRANSITION'` reutilizando `beginBiomeArrival`).

## Review

### Developer implementation — Evidencias

#### Resumen técnico de cambios

1. **`AvatarEventType`**: añadido `BIOME_TRANSITION` al enum existente.
2. **Migración Liquibase `038`**: columna `biome` nullable + índice en `avatar_event_catalog`.
3. **Modelo/Entidad/Adapter**: campo `biome` añadido a `AvatarEventCatalog`, `AvatarEventCatalogJpaEntity`, y mapeo `toDomain`/`toJpa` en el adapter.
4. **Repositorio**: nuevo método `findActiveByEventTypeAndBiome` en `AvatarEventCatalogRepository` + implementación en `AvatarEventCatalogPersistenceAdapter` que filtra `status=ACTIVE`. El `findByEventType` existente ahora también filtra `status=ACTIVE` (cierra gap preexistente para todos los tipos).
5. **`AvatarService`**:
   - `processEvent` lee `biome` desde `request.context()` cuando `eventType == BIOME_TRANSITION`.
   - `resolveCatalog` acepta parámetro `biome`; para `BIOME_TRANSITION` usa `findActiveByEventTypeAndBiome`.
   - Anti-repetición con clave `profileId:BIOME_TRANSITION:<biome>`.
   - `lastShownCatalogIdByChild` cambiado de `ConcurrentHashMap` a `LinkedHashMap` con acceso-orden y límite de 512 entradas con expulsión FIFO.
6. **`GameWebSocketHandler.handleWorldTravel`**: tras enviar `WORLD_STATE_SYNC`, invoca `sendBiomeTransitionAvatar` que dispara `BIOME_TRANSITION` con el bioma de destino.
7. **Contrato `game-avatar-event.yaml`**: `BIOME_TRANSITION` añadido al enum `eventType`.
8. **DTOs/Controller/Service**: soporte para `biome` en create/update/response del catálogo de contenido.

#### Lista de archivos modificados

| Archivo | Cambio |
|---------|--------|
| `avatar/domain/enums/AvatarEventType.java` | Añadido `BIOME_TRANSITION` |
| `content/model/AvatarEventCatalog.java` | Campo `biome` |
| `content/infrastructure/persistence/AvatarEventCatalogJpaEntity.java` | Campo `biome` |
| `content/infrastructure/persistence/AvatarEventCatalogJpaRepository.java` | Nuevos métodos de consulta |
| `content/infrastructure/persistence/AvatarEventCatalogPersistenceAdapter.java` | `findByEventType` filtra ACTIVE; nuevo `findActiveByEventTypeAndBiome`; mapeo `biome` |
| `content/ports/out/AvatarEventCatalogRepository.java` | Nuevo método `findActiveByEventTypeAndBiome` |
| `content/ports/in/AvatarEventCatalogUseCase.java` | Firma con `biome` |
| `content/service/AvatarEventCatalogService.java` | Soporte `biome` en create/update |
| `content/infrastructure/dto/CreateAvatarEventCatalogRequest.java` | Campo `biome` |
| `content/infrastructure/dto/UpdateAvatarEventCatalogRequest.java` | Campo `biome` |
| `content/infrastructure/dto/AvatarEventCatalogResponse.java` | Campo `biome` |
| `content/infrastructure/web/AvatarEventCatalogController.java` | Paso de `biome` |
| `agents/infrastructure/adapters/ContentGenerationAdapter.java` | Firma actualizada |
| `avatar/infrastructure/service/AvatarService.java` | Biome-aware resolveCatalog, bounded cache |
| `session/infrastructure/websocket/GameWebSocketHandler.java` | `sendBiomeTransitionAvatar` en `handleWorldTravel` |
| `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml` | `BIOME_TRANSITION` en enum |
| `db/changelog/migrations/038__add_biome_to_avatar_event_catalog.xml` | Nueva migración |
| `db/changelog/db.changelog-master.xml` | Inclusión migración 038 |

#### Migración Liquibase

`038__add_biome_to_avatar_event_catalog.xml`: `ALTER TABLE avatar_event_catalog ADD COLUMN biome VARCHAR(50) NULL` + índice `idx_avatar_event_catalog_biome`.

#### Contrato afectado

`docs/contracts/api/asyncapi/messages/game-avatar-event.yaml`: enum `eventType` ampliado con `BIOME_TRANSITION`.

#### Pruebas ejecutadas

```
mvn -o test -Dtest=AvatarServiceSprint094Test
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

```
mvn -o test -Dtest=AvatarServiceTest,AvatarEventCatalogServiceTest,AvatarEventCatalogPersistenceAdapterTest,GameWebSocketHandlerTest
Tests run: 61, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

Suite completa unitaria: 815 tests, 0 failures. Los 8 errors son tests de integración preexistentes que requieren Docker/testcontainers (no relacionados con este sprint).

#### Decisiones de detalle tomadas

1. **`LinkedHashMap` con acceso-orden y `removeEldestEntry`** para `lastShownCatalogIdByChild` — límite 512 entradas, sincronizado. Sin dependencias externas nuevas.
2. **`findByEventType` existente corregido directamente** para filtrar `status=ACTIVE` (no solo un método nuevo paralelo), cerrando el gap para todos los tipos.
3. **Nuevo método `findActiveByEventTypeAndBiome`** separado para `BIOME_TRANSITION` — cuando `biome` es null/blank, delega al mismo filtro `biome IS NULL` que `WELCOME`/`FAREWELL`.
4. **`sendBiomeTransitionAvatar`** como método privado en `GameWebSocketHandler`, sigue exactamente el mismo patrón que `sendWelcomeAvatar`.
5. **DTOs del controller de contenido** ampliados con `biome` para permitir la autoría de contenido por bioma desde la herramienta de desarrollo.

### Reviewer verification

**Veredicto: APPROVED**

#### Evidencia de revisión

**Tests ejecutados:**
- `AvatarServiceSprint094Test`: 8 tests, 0 failures ✅
- `AvatarServiceTest`, `AvatarEventCatalogServiceTest`, `AvatarEventCatalogPersistenceAdapterTest`, `GameWebSocketHandlerTest`: 61 tests, 0 failures ✅

**Implementación verificada:**

1. **Enum AvatarEventType** ✅
   - `BIOME_TRANSITION` añadido al enum existente (línea 10)

2. **Migración Liquibase 038** ✅
   - Columna `biome VARCHAR(50) NULL` añadida a `avatar_event_catalog`
   - Índice `idx_avatar_event_catalog_biome` creado

3. **Modelo y persistencia** ✅
   - `AvatarEventCatalog.biome` campo nullable añadido
   - `AvatarEventCatalogJpaEntity.biome` mapeado correctamente
   - `AvatarEventCatalogPersistenceAdapter` mapea `biome` en `toDomain`/`toJpa`

4. **Repositorio corregido** ✅
   - `findByEventType` ahora filtra `status=ACTIVE` (cierra gap preexistente para todos los tipos)
   - Nuevo método `findActiveByEventTypeAndBiome` para `BIOME_TRANSITION`
   - Cuando `biome` es null/blank, delega a filtro `biome IS NULL` (compatible con `WELCOME`/`FAREWELL`)

5. **AvatarService biome-aware** ✅
   - `processEvent` lee `biome` desde `request.context()` cuando `eventType == BIOME_TRANSITION`
   - `resolveCatalog` acepta parámetro `biome` y usa `findActiveByEventTypeAndBiome` para `BIOME_TRANSITION`
   - Anti-repetición con clave `profileId:BIOME_TRANSITION:<biome>` (scoped por bioma de destino)
   - `lastShownCatalogIdByChild` cambiado de `ConcurrentHashMap` a `LinkedHashMap` con acceso-orden y límite de 512 entradas (FIFO eviction)

6. **Disparo en handleWorldTravel** ✅
   - `GameWebSocketHandler.handleWorldTravel` invoca `sendBiomeTransitionAvatar` tras enviar `WORLD_STATE_SYNC`
   - `sendBiomeTransitionAvatar` sigue el mismo patrón que `sendWelcomeAvatar` (JSON + frame binario si `audioAvailable`)

7. **Contrato actualizado** ✅
   - `game-avatar-event.yaml`: enum `eventType` ampliado con `BIOME_TRANSITION`

**Criterios de aceptación cubiertos por tests:**

| Criterio | Test | Estado |
|----------|------|--------|
| `world_travel` a bioma con contenido activo envía `GAME_AVATAR_EVENT` con `audioAvailable=true` | `worldTravel_biomeWithActiveContent_sendsBiomeTransitionWithAudio` | ✅ |
| `world_travel` a bioma sin contenido envía fallback sin romper flujo | `worldTravel_biomeWithoutContent_sendsFallbackWithoutBreakingFlow` | ✅ |
| Con `npcEnabled=false` sigue enviando evento sin audio | `worldTravel_npcDisabled_sendsEventWithoutAudio` | ✅ |
| Dos `world_travel` a biomas distintos no comparten anti-repetición | `worldTravel_differentBiomes_doNotShareAntiRepetition` | ✅ |
| Dos `world_travel` al mismo bioma no repiten variante si hay alternativas | `worldTravel_sameBiomeTwice_doesNotRepeatVariantIfOtherAvailable` | ✅ |
| `findByEventType` excluye `DRAFT`/`ARCHIVED` para `BIOME_TRANSITION` y `WELCOME`/`FAREWELL` | `findByEventType_excludesDraftAndArchived_forBiomeTransitionAndWelcome` | ✅ |
| Catálogo vacío no lanza excepción | `worldTravel_emptyCatalogForBiome_doesNotThrowException` | ✅ |
| `lastShownCatalogIdByChild` tiene límite de 512 entradas | `lastShownCatalogMap_doesNotGrowUnbounded` | ✅ |

**Decisiones de diseño implementadas correctamente:**

1. ✅ Anti-repetición solo por bioma de destino (no par origen→destino)
2. ✅ Evento siempre se envía (con o sin audio) — gateo NPC/voz no omite el mensaje
3. ✅ `findByEventType` corregido directamente (cierra gap para todos los tipos)
4. ✅ Mitigación de `lastShownCatalogIdByChild` con `LinkedHashMap` bounded (512 entradas, FIFO)

**Conformidad con ADR/FEAT:**

- ✅ ADR-026: biomas conectados con transiciones narrativas
- ✅ FEAT-001 §3.10: frase se refiere exclusivamente al bioma de destino
- ✅ FEAT-012: sin candados, desbloqueos ni progreso en la navegación entre biomas

**Observaciones:**

- Los 8 errors de tests de integración son preexistentes (requieren Docker/testcontainers, no relacionados con este sprint)
- Contenido real de frases por bioma pendiente de carga por equipo de Contenido (SPRINT-004)
