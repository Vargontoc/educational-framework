# SPRINT-067 — Transporte temático y selector visual de destino (mapa con Nubi)

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-11
- **Fecha de verificación:** 2026-09-13
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-066 (consumo de `worldWidth`/posición real); SPRINT-092 backend (contrato de selección de destino); Contenido (ilustración de Nubi+mapa y stickers por bioma)
- **Impacto estimado:** Habilita el único mecanismo de navegación entre biomas descrito en ADR-026/FEAT-012: el transporte junto al punto de inicio de cada bioma.

## Objetivo

Nuevo elemento de transporte visible junto al punto de inicio de Nubi en cada bioma. Al tocarlo, se abre un overlay a pantalla completa con Nubi sosteniendo un mapa con un sticker por cada uno de los 6 biomas disponibles. Tocar un sticker inicia directamente la transición a ese bioma.

## Contexto

**Decisión confirmada por el usuario (2026-09-11):** el selector es un overlay a pantalla completa (no un panel lateral que conviva con el paisaje). La ilustración es Nubi mostrando un mapa con stickers de los biomas; no hay paso de confirmación intermedio entre tocar el sticker y empezar la transición.

Requisitos de FEAT-012/ADR-026 que este sprint debe respetar estrictamente:
- Los 6 destinos se presentan siempre disponibles, sin candados, orden de preferencia, insignias de progreso ni indicación de visitas previas (FEAT-012 §3.11, criterio de aceptación §13).
- El transporte debe ser reconocible como elemento de viaje sin depender exclusivamente de texto, color o sonido (§3.10).
- Elegir un destino no debe presentarse como salto de nivel ni premio (§3.12).

## Diseño funcional-técnico

### `TransportLayer` (nueva capa)

- Mismo patrón que el resto de capas de `worldmap/layers/`: constructor con `Scene`, `create()`/`destroy()`, profundidad propia (por encima de `GroundLayer`, junto a la posición de inicio de Nubi del bioma activo).
- Un único elemento interactivo por bioma (no six-in-one): el icono de transporte temático del bioma actual (globo aerostático en Pradera, tractor en Granja, etc. — a definir por Contenido).
- `zone` interactiva con `useHandCursor`, mismo patrón que `InteractiveLayer.createElement`.

### Overlay de selección (`BiomeSelectorLayer` o escena superpuesta)

- Se evalúa como capa adicional dentro de `WorldMapScene` (consistente con el resto de capas) en vez de una `Scene` de Phaser separada, para reutilizar el mismo `registry`/eventos sin gestionar transferencia de estado entre escenas.
- Contenido: ilustración de Nubi + mapa (asset de Contenido) con 6 stickers posicionados, cada uno representando un bioma mediante icono + forma + color (nunca solo color, por accesibilidad — mismo criterio que `InteractiveLayer`).
- Ningún sticker se muestra distinto por progreso, visitas previas o recomendación. El orden de presentación de los stickers es el orden lineal de biomas (dato de SPRINT-091 backend), pero solo como disposición visual del mapa, nunca como bloqueo de acceso a los posteriores.
- Tocar un sticker: cierra el overlay y dispara directamente el flujo de transición (ver SPRINT-068) hacia ese bioma — sin pantalla de confirmación.
- Cerrar el overlay sin elegir (tocar fuera, o un control de cierre) debe ser posible y no penalizar ni registrar nada.

### Accesibilidad

- El overlay debe ser navegable/comprensible en `prefers-reduced-motion` (sin animación de apertura brusca si está activo).
- Objetivos táctiles ≥ `WORLD_MAP_CONFIG.minHitAreaSize` (80px), igual que el resto de elementos interactivos.

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| Orden lineal de biomas (para disposición visual de stickers) | Deseable, no bloqueante | Pendiente (SPRINT-091 backend) — sin él, se puede fijar un orden local temporal |
| Acción de "solicitar destino" hacia backend | Requerida para completar la transición | Pendiente (SPRINT-092 backend) |
| Ilustración Nubi+mapa y 6 stickers | Requerida para producción real | Pendiente — dependencia de Contenido, fuera de esta capa |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El selector de 6 destinos se percibe como menú adulto (riesgo ya señalado en FEAT-012 §7) | ALTA | Validar layout con Contenido antes de dar por cerrado el diseño visual; no es una decisión que frontend cierre solo |
| R2 | Sin el contrato de SPRINT-092, no hay forma real de completar la transición tras tocar un sticker | MEDIA | Implementar el flujo hasta el punto de disparo del evento de selección; dejar la integración real como tarea acoplada a SPRINT-092 |
| R3 | Confundir "orden lineal de biomas" (dato de producto) con "orden de desbloqueo" (prohibido) | ALTA | Revisión explícita: ningún sticker debe renderizarse deshabilitado o con candado bajo ninguna condición |

## Tareas del sprint

### Tarea 67.1: `TransportLayer`

**Archivo (nuevo):** `framework/frontend/app/src/game/worldmap/layers/TransportLayer.ts`

**Criterios de aceptación:**
- Un elemento de transporte visible junto al punto de inicio de Nubi en el bioma activo.
- Objetivo táctil ≥ 80px, reconocible sin depender solo de color.
- Al tocarlo, emite un evento de escena (p. ej. `'transport-touched'`) que `WorldMapScene` escucha para abrir el selector.

### Tarea 67.2: Overlay de selección de destino

**Archivo (nuevo):** `framework/frontend/app/src/game/worldmap/layers/BiomeSelectorLayer.ts`

**Criterios de aceptación:**
- Overlay a pantalla completa con Nubi + mapa + 6 stickers, todos visualmente equivalentes y disponibles.
- Tocar un sticker cierra el overlay y emite el bioma elegido (p. ej. evento `'destination-selected'` con el id de bioma).
- Se puede cerrar sin elegir ningún destino.
- Ningún sticker depende exclusivamente de color para diferenciarse.

### Tarea 67.3: Integración en `WorldMapScene`

**Archivo:** `framework/frontend/app/src/game/WorldMapScene.ts`

**Criterios de aceptación:**
- `TransportLayer` se crea/destruye junto al resto de capas del bioma activo (se reconstruye si el bioma cambia, igual que `NubiLayer`).
- Al recibir `'destination-selected'`, se dispara el flujo de transición de SPRINT-068 (aunque su implementación completa sea de ese sprint).

## Notas

- Este sprint no implementa la pausa de llegada ni la señal de proximidad — solo el mecanismo de elegir destino. La transición real (fundido, reconstrucción de capas, pausa) se conecta en SPRINT-068.
- La disposición exacta de los 6 stickers (fila, círculo, mapa ilustrado con posiciones fijas) depende del asset final de Contenido; no fijar coordenadas definitivas hasta tener el arte.

## Review

### Developer implementation — Evidencias

**Fecha:** 2026-09-13

#### Tareas implementadas

| Tarea | Archivo | Estado |
|-------|---------|--------|
| 67.1 — `TransportLayer` | `framework/frontend/app/src/game/worldmap/layers/TransportLayer.ts` | ✅ Implementada |
| 67.2 — Overlay de selección | `framework/frontend/app/src/game/worldmap/layers/BiomeSelectorLayer.ts` | ✅ Implementada |
| 67.3 — Integración en `WorldMapScene` | `framework/frontend/app/src/game/WorldMapScene.ts` | ✅ Implementada |

#### Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `framework/frontend/app/src/game/GameEvent.ts` | Añadido `'world_travel'` a `TYPE_SEND_EVENT` y clase `WorldTravelEvent` |
| `framework/frontend/app/src/game/WorldMapScene.ts` | Integración de `TransportLayer` y `BiomeSelectorLayer`, listeners para `transport-touched` y `destination-selected`, envío de `world_travel` vía WebSocket |

#### Criterios de aceptación verificados

**Tarea 67.1 — `TransportLayer`:**
- ✅ Elemento de transporte visible junto al punto de inicio de Nubi en el bioma activo (posición `nubiStartX + 120`, sobre el ground)
- ✅ Objetivo táctil ≥ 80px (`Math.max(WORLD_MAP_CONFIG.minHitAreaSize, TRANSPORT_ICON_SIZE + 16)` = 80px)
- ✅ Reconocible sin depender solo de color: cada bioma tiene forma única (balloon/tractor/mushroom/boat/rocket/dino) + icono + color
- ✅ Emite evento `'transport-touched'` al tocarlo
- ✅ Se reconstruye al cambiar de bioma (destruido y recreado en `rebuildLayersForBiome`)
- ✅ Placeholder geométrico cuando el asset de Contenido no existe (`transport-{biome}`)

**Tarea 67.2 — Overlay de selección:**
- ✅ Overlay a pantalla completa (depth 100, backdrop oscuro semi-transparente)
- ✅ Nubi placeholder + mapa + 6 stickers, todos visualmente equivalentes y disponibles
- ✅ Tocar sticker cierra overlay y emite `'destination-selected'` con id de bioma
- ✅ Se puede cerrar sin elegir: tocar fuera del mapa (backdrop zone) o botón de cierre (✕)
- ✅ Stickers diferenciados por icono + forma + color (nunca solo color): circle/roundedRect/hexagon/diamond/star/triangle
- ✅ Orden por `sequenceOrder` (dato de `world-host-payload.yaml`), sin implicar desbloqueo
- ✅ Navegable en `prefers-reduced-motion` (sin fade de apertura/cierre si está activo)
- ✅ Objetivos táctiles ≥ 80px (`Math.max(WORLD_MAP_CONFIG.minHitAreaSize, STICKER_SIZE)`)
- ✅ Sin candados, requisitos, insignias de progreso ni indicación de visitas previas

**Tarea 67.3 — Integración:**
- ✅ `TransportLayer` se crea/destruye junto al resto de capas del bioma activo
- ✅ Al recibir `'destination-selected'`, se envía `world_travel { biome }` vía WebSocket (conexión con SPRINT-092 backend)
- ✅ Flujo de transición de SPRINT-068 preparado (disparo del evento, implementación completa pendiente de ese sprint)

#### Comandos ejecutados

| Comando | Resultado |
|---------|-----------|
| `npx tsc --noEmit` | ✅ Sin errores |
| `npx vite build` | ✅ Build exitoso (6.32s) |

#### Contratos consumidos

| Contrato | Uso |
|----------|-----|
| `game-client-message.yaml` → `WorldTravelMessage` | `WorldTravelEvent` envía `{ type: 'world_travel', biome: string }` vía WebSocket |
| `world-host-payload.yaml` → `sequenceOrder` | Orden lineal de biomas para disposición visual de stickers (estático: MEADOW=1..PREHISTORY=6) |

#### Riesgos y deuda técnica

| # | Descripción | Severidad |
|---|-------------|-----------|
| R1 | Placeholders geométricos para transporte y stickers — pendientes assets reales de Contenido | MEDIA |
| R2 | Lista de biomas estática en `ALL_BIOME_HOSTS` — si el backend añade biomas dinámicamente, habrá que consumirla del `WORLD_STATE_SYNC` | BAJA |
| R3 | La transición real entre biomas (fundido, reconstrucción de capas, pausa de llegada) no está implementada — corresponde a SPRINT-068 | EXPECTED |

### Reviewer verification

**Veredicto: APPROVED**

#### Verificación de tipos (GameEvent.ts)
- ✅ `'world_travel'` añadido a `TYPE_SEND_EVENT` — coincide con `game-client-message.yaml` → `WorldTravelMessage`
- ✅ `WorldTravelEvent` clase con `biome: string` — coincide con contrato backend

#### Verificación de TransportLayer
- ✅ Elemento de transporte visible junto al punto de inicio de Nubi (posición `nubiStartX + 120`, sobre el ground)
- ✅ Objetivo táctil ≥ 80px (`Math.max(WORLD_MAP_CONFIG.minHitAreaSize, TRANSPORT_ICON_SIZE + 16)` = 80px)
- ✅ Reconocible sin depender solo de color: cada bioma tiene forma única (balloon/tractor/mushroom/boat/rocket/dino) + icono + color
- ✅ Emite evento `'transport-touched'` al tocarlo
- ✅ Se reconstruye al cambiar de bioma (destruido y recreado en `rebuildLayersForBiome`)
- ✅ Placeholder geométrico cuando el asset de Contenido no existe (`transport-{biome}`)
- ✅ Registrado en el scroller con factor 1 (se mueve con el mundo)

#### Verificación de BiomeSelectorLayer
- ✅ Overlay a pantalla completa (depth 100, backdrop oscuro semi-transparente)
- ✅ Nubi placeholder + mapa + 6 stickers, todos visualmente equivalentes y disponibles
- ✅ Tocar sticker cierra overlay y emite `'destination-selected'` con id de bioma
- ✅ Se puede cerrar sin elegir: tocar fuera del mapa (backdrop zone) o botón de cierre (✕)
- ✅ Stickers diferenciados por icono + forma + color (nunca solo color): circle/roundedRect/hexagon/diamond/star/triangle
- ✅ Orden por `sequenceOrder` (dato de `world-host-payload.yaml`), sin implicar desbloqueo
- ✅ Navegable en `prefers-reduced-motion` (sin fade de apertura/cierre si está activo)
- ✅ Objetivos táctiles ≥ 80px (`Math.max(WORLD_MAP_CONFIG.minHitAreaSize, STICKER_SIZE)`)
- ✅ Sin candados, requisitos, insignias de progreso ni indicación de visitas previas
- ✅ `stopPropagation()` en eventos de sticker y botón de cierre para evitar cierre accidental por backdrop

#### Verificación de integración en WorldMapScene
- ✅ `TransportLayer` se crea en `create()` y se reconstruye en `rebuildLayersForBiome()`
- ✅ `BiomeSelectorLayer` se crea en `create()` y se destruye en `cleanupLayers()`
- ✅ Listener para `TRANSPORT_TOUCHED_EVENT` → abre selector con `ALL_BIOME_HOSTS`
- ✅ Listener para `DESTINATION_SELECTED_EVENT` → envía `world_travel` vía WebSocket
- ✅ Listener para `'nubi-double-tap'` → cierra selector (gesto alternativo)
- ✅ `ALL_BIOME_HOSTS` definido con los 6 biomas y `sequenceOrder` 1-6 (coincide con ADR-026)

#### Verificación de compilación y build
- ✅ `npx tsc --noEmit` → 0 errores
- ✅ `npx vite build` → build exitoso

#### Conformidad con ADR-026 / FEAT-012
- ✅ Los 6 destinos se presentan siempre disponibles, sin candados ni insignias de progreso
- ✅ El transporte es reconocible como elemento de viaje sin depender exclusivamente de texto, color o sonido
- ✅ Elegir un destino no se presenta como salto de nivel ni premio (sin confirmación intermedia)
- ✅ Orden lineal de biomas usado solo como disposición visual, no como bloqueo

#### Observaciones
- `ALL_BIOME_HOSTS` es estático en frontend — si el backend añade biomas dinámicamente, habrá que consumirla del `WORLD_STATE_SYNC` (deuda técnica documentada como R2)
- Placeholders geométricos para transporte y stickers — pendientes assets reales de Contenido (R1)
- La transición real entre biomas (fundido, reconstrucción de capas, pausa de llegada) corresponde a SPRINT-068 (R3 esperado)
