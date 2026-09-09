# SPRINT-063 — WorldMap esqueleto con paisaje placeholder y desplazamiento

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-010 completado (transición LoadingScene → WorldMapScene funcional)
- **Impacto estimado:** Establece la estructura base de WorldMapScene con paisaje placeholder, Nubi visual y desplazamiento gradual por drag.

## Objetivo

`WorldMapScene` muestra un paisaje placeholder con fondo, parallax, Nubi estático/idle y desplazamiento gradual por drag, sin elementos interactivos aún.

## Contexto

FEAT-011 define un paseo visual básico tras la carga inicial. Este sprint establece la arquitectura de capas interna de WorldMapScene, reestructurando el esqueleto actual (que solo coloca imágenes estáticas y lanza minijuegos) en módulos co-locados bajo `src/components/game/worldmap/`.

**Decisiones técnicas confirmadas:**
- Alternativa 1 para Nubi: sin reacciones expresivas si NPC desactivado
- No envío de `WorldDiscoveryElementInteractiveEvent` en fase 1
- Ancho de mundo virtual placeholder: 2560 px lógicos (2x viewport de 1280)
- Reacción ambiental genérica única (extensible en fase 4B)

## Diseño funcional-técnico

### Arquitectura de módulos

| Módulo | Responsabilidad |
|--------|-----------------|
| `WorldMapScene.ts` | Orquestador: ciclo de vida, WebSocket, eventos, composición de capas |
| `layers/BackgroundLayer.ts` | Renderiza fondo del bioma (placeholder: rectángulo degradado + patrón) |
| `layers/ParallaxLayer.ts` | Capas decorativas con desplazamiento parallax (nubes, lejanía) |
| `layers/NubiLayer.ts` | Presencia visual de Nubi (estática o idle breathing según NPC) |
| `scroll/GradualScroller.ts` | Controla el desplazamiento gradual del paisaje |
| `config/worldMapConfig.ts` | Constantes de velocidad, límites, tamaños táctiles, tiempos |

### Capas de renderizado (orden de profundidad)

1. **Fondo de bioma** (depth 0): degradado vertical placeholder (cielo → tierra)
2. **Capa parallax lejana** (depth 1): formas simples con desplazamiento 0.3x
3. **Nubi** (depth 3): círculo azul (`0x7ec8e3`) con idle breathing si NPC activado
4. **Capa de elementos interactivos** (depth 2): se añade en Sprint 064

### Mecanismo de desplazamiento

**Opción seleccionada:** Scroll por arrastre suave (drag) con límites

- `GradualScroller` escucha `pointerdown`, `pointermove`, `pointerup` en la capa de fondo
- Velocidad máxima: 40 px/s (configurable)
- Inercia: tween de deceleración tras soltar (500ms, `Cubic.out`)
- Límites: offset mínimo 0, offset máximo 1280 px (2560 - 1280 viewport)
- Las capas parallax se mueven proporcionalmente (factor 0.3x)
- No se muestra indicador de progreso, porcentaje, flechas ni destino

### Respeto a preferencia parental (Alternativa 1)

- `npcEnabled === true`: Nubi con idle breathing (scale 1.0↔1.05, 2s, `Sine.easeInOut`)
- `npcEnabled === false`: Nubi estático, sin animación alguna
- Escucha evento interno `'npc-state-changed'` para ajustar en tiempo real

### Accesibilidad

- Orientación: landscape (1280x720 lógicos). Portrait muestra `OrientationRequiredScene`
- Escalado: `Phaser.Scale.FIT` + `CENTER_BOTH`
- `prefers-reduced-motion`: consulta vía `window.matchMedia`. Si activo, desactiva parallax, idle breathing, inercia

## Contratos y dependencias externas

### Endpoints/contratos que consume

- `WORLD_STATE_SYNC` (WebSocket): ya definido en `docs/contracts/api/asyncapi/schemas/world-state-sync-payload.yaml`
  - `status` (ACTIVE/INACTIVE_CLOSED/NO_WORLD_STATE)
  - `destination.biome` → usado para seleccionar fondo placeholder
  - `destination.discoveryElements[]` → no se procesan en este sprint (Sprint 064)

### Dependencias hacia backend/agents/tts

| Dependencia | Tipo | Estado |
|-------------|------|--------|
| `WORLD_STATE_SYNC` (WS) | Requerida | Ya definido. Backend debe enviarlo tras la carga |
| `world_heartbeat` (WS client→server) | Requerida | Ya implementado en `WorldMapScene:40-45` |
| npc-agent | No requerida | Nubi no produce intervenciones en esta fase |
| tts-service | No requerida | No se reproduce audio en WorldMap en esta fase |

### Handoffs documentados

| Handoff | Capa destino | Contenido |
|---------|--------------|-----------|
| Transición LoadingScene → WorldMapScene | Frontend interno | Ya implementado en `LoadingScene.goToWorldMap():300-311` |
| Contenido de biomas y elementos | Backend/Content (fase 2-3) | Frontend necesita `WORLD_STATE_SYNC` con `destination` poblado |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Assets placeholder no distinguidos visualmente | MEDIA | Cada elemento usa forma geométrica distinta + color diferenciado |
| R2 | `prefers-reduced-motion` no cubre todos los casos | BAJA | Revisión exhaustiva en cada módulo de animación |
| R3 | El desplazamiento por drag confunde al niño | MEDIA | Los elementos interactivos visibles invitan al toque; el desplazamiento se descubre naturalmente |
| R4 | Nubi estático con NPC desactivado resulta "extraño" | BAJA | Nubi mantiene presencia visual coherente; el foco está en los elementos del entorno |

---

## Tareas del sprint

### Tarea 63.1: Crear módulo `worldMapConfig.ts`

**Archivo:** `src/components/game/worldmap/config/worldMapConfig.ts`

**Descripción:** Constantes de configuración para WorldMap.

**Criterios de aceptación:**
- Velocidad scroll: 40 px/s máximo
- Límites mundo virtual: 2560 px ancho, 1280 px desplazamiento máximo
- Tamaño hit-area: 80x80 px mínimo
- Duración reacciones: 300-600ms
- Factor parallax: 0.3x
- Duración inercia: 500ms

### Tarea 63.2: Crear `BackgroundLayer.ts`

**Archivo:** `src/components/game/worldmap/layers/BackgroundLayer.ts`

**Descripción:** Renderiza fondo del bioma con degradado vertical placeholder.

**Criterios de aceptación:**
- Renderiza degradado vertical (cielo → tierra)
- Método `buildFrom(biome: string)` para futura extensión
- Si el asset del bioma no existe, fallback al degradado
- Depth: 0

### Tarea 63.3: Crear `ParallaxLayer.ts`

**Archivo:** `src/components/game/worldmap/layers/ParallaxLayer.ts`

**Descripción:** Capas decorativas con desplazamiento parallax.

**Criterios de aceptación:**
- Formas simples (círculos = nubes, triángulos = montañas)
- Desplazamiento proporcional al scroll (factor 0.3x)
- Respeta `prefers-reduced-motion` (desactiva parallax si activo)
- Depth: 1

### Tarea 63.4: Crear `GradualScroller.ts`

**Archivo:** `src/components/game/worldmap/scroll/GradualScroller.ts`

**Descripción:** Controla el desplazamiento gradual del paisaje por drag.

**Criterios de aceptación:**
- Escucha `pointerdown`, `pointermove`, `pointerup`
- Velocidad máxima: 40 px/s
- Inercia suave tras soltar (500ms, `Cubic.out`)
- Límites: offset mínimo 0, offset máximo 1280 px
- Los límites no se muestran visualmente (deceleración suave)
- Integra con capas para moverlas proporcionalmente
- Respeta `prefers-reduced-motion` (desactiva inercia si activo)

### Tarea 63.5: Crear `NubiLayer.ts`

**Archivo:** `src/components/game/worldmap/layers/NubiLayer.ts`

**Descripción:** Presencia visual de Nubi en el mapa.

**Criterios de aceptación:**
- Placeholder: círculo azul (`0x7ec8e3`)
- Posición: tercio izquierdo de la vista
- Si `npcEnabled === true`: idle breathing (scale 1.0↔1.05, 2s, `Sine.easeInOut`)
- Si `npcEnabled === false`: estático, sin animación
- Escucha evento `'npc-state-changed'` para ajustar en tiempo real
- Depth: 3

### Tarea 63.6: Reestructurar `WorldMapScene.ts`

**Archivo:** `src/components/game/WorldMapScene.ts`

**Descripción:** Integra las nuevas capas en el ciclo de vida de la escena.

**Criterios de aceptación:**
- Integra `BackgroundLayer`, `ParallaxLayer`, `NubiLayer`, `GradualScroller` en `create()`
- Configura depths correctamente
- Conecta `GradualScroller` con capas para movimiento coordinado
- Mantiene WebSocket y heartbeat existentes
- Lee `npcEnabled` del registry al crear la escena
- Se suscribe a `'npc-state-changed'`

### Tarea 63.7: Verificar integración con `GameView.vue`

**Archivo:** `src/views/GameView.vue`

**Descripción:** Confirmar que la escena arranca correctamente tras `LoadingScene.goToWorldMap()`.

**Criterios de aceptación:**
- La escena `world-map` arranca correctamente tras la carga
- No hay parpadeo ni pérdida de WebSocket en la transición
- La orientación portrait muestra `OrientationRequiredScene`

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `src/components/game/worldmap/config/worldMapConfig.ts` | Nuevo |
| `src/components/game/worldmap/layers/BackgroundLayer.ts` | Nuevo |
| `src/components/game/worldmap/layers/ParallaxLayer.ts` | Nuevo |
| `src/components/game/worldmap/layers/NubiLayer.ts` | Nuevo |
| `src/components/game/worldmap/scroll/GradualScroller.ts` | Nuevo |
| `src/components/game/WorldMapScene.ts` | Modificación |

## Estimación

- **Duración:** 3-4 días
- **Complejidad:** Media
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. Tras la carga de LoadingScene, se visualiza WorldMapScene con un fondo de degradado, formas parallax y Nubi.
2. El paisaje se desplaza horizontalmente al arrastrar con el dedo o ratón.
3. El desplazamiento tiene límites laterales suaves (deceleración, no muro).
4. Nubi muestra idle breathing si NPC activado; estático si desactivado.
5. Con `prefers-reduced-motion: reduce`, no hay animaciones de parallax ni idle breathing.
6. La orientación portrait muestra `OrientationRequiredScene`.
7. No se muestra ningún indicador de progreso, nivel, destino ni porcentaje.

## Dependencias bloqueantes

- [ ] FEAT-010 completado (transición LoadingScene → WorldMapScene funcional).
- [ ] Perfil infantil habilitado y configuración parental cargada.

## Handoffs a otras capas

Ninguno en este sprint.

## Notas adicionales

Este sprint establece la arquitectura extensible de WorldMap. Las capas están diseñadas para sustituir placeholders por assets reales en fases posteriores (3 y 4B) sin reescribir la lógica de escena.

El desplazamiento por drag da agencia al niño sin obligación de movimiento. Los límites suaves (deceleración) evitan la sensación de "muro" o "nivel con final".

## Agent Instruction

- No implementar código de producto. Este sprint es para el desarrollador de Frontend.
- Seguir patrón de módulos co-locados bajo `src/components/game/worldmap/`.
- Mantener WebSocket y heartbeat existentes en `WorldMapScene`.
- Respetar preferencia parental de NPC (alternativa 1).
- Consultar `prefers-reduced-motion` vía `window.matchMedia`.
- No enviar `WorldDiscoveryElementInteractiveEvent` en este sprint ni en fase 1.

## Review

completed_tasks:
- Creado `src/components/game/worldmap/config/worldMapConfig.ts` con constantes de velocidad, límites, factor parallax, duración de inercia y parámetros de idle breathing.
- Creado `src/components/game/worldmap/layers/BackgroundLayer.ts`: degradado vertical placeholder vía `Graphics`, método `buildFrom(biome?)` con fallback al degradado si no existe textura `biome-<nombre>-bg`, depth 0.
- Creado `src/components/game/worldmap/layers/ParallaxLayer.ts`: formas simples (círculos = nubes, triángulos = montañas) dentro de un `Container`, depth 1. El registro de este contenedor en el scroller (y por tanto su desplazamiento) se omite por completo cuando `prefers-reduced-motion: reduce` está activo.
- Creado `src/components/game/worldmap/scroll/GradualScroller.ts`: escucha `pointerdown`/`pointermove`/`pointerup`/`pointerupoutside` sobre `scene.input`, mueve el offset hacia el objetivo con velocidad tope de 40 px/s (aplicada en `update(delta)`), aplica inercia por tween (`Cubic.out`, 500ms) tras soltar, y deshabilita la inercia si `prefers-reduced-motion` está activo. Límites de offset [0, 1280] siempre respetados.
- Creado `src/components/game/worldmap/layers/NubiLayer.ts`: círculo placeholder (`0x7ec8e3`) en el tercio izquierdo de la vista, depth 3. Idle breathing (scale 1.0↔1.05, 2s, `Sine.easeInOut`) solo si `npcEnabled === true` y no hay `prefers-reduced-motion`. Escucha el evento local de escena `npc-state-changed` para reaccionar en tiempo real.
- Reestructurado `WorldMapScene.ts`: integra las 4 capas y el scroller en `create()`, añade `update()` para impulsar el scroller, conserva el manejo de WebSocket/heartbeat existente, lee `npcEnabled` del registry al crear, y añade el manejo de `CHILD_AGENT_ACTIVATED`/`CHILD_AGENT_DEACTIVATED` (actualiza registry y emite `npc-state-changed`) necesario para que `NubiLayer` reaccione en vivo. Se retiró el renderizado ad-hoc de `discoveryElements` de `WORLD_STATE_SYNC` (correspondía a Sprint 064, no a este sprint) y se conserva únicamente `backgroundLayer.buildFrom(destination.biome)`.
- Verificada la integración con `GameView.vue`: `WorldMapScene` ya estaba registrada (cambio previo no commiteado) y `LoadingScene.goToWorldMap()` ya transicionaba a `world-map`. Se restauró `FarewellScene` en el array de escenas de Phaser (se había perdido en el cambio previo no commiteado y `BaseStateScene.goToFarewell()` depende de que la clave `farewell` exista).
- Actualizados los specs Cypress de `fase7-gameview` que asumían la transición antigua `loading → base-state`: `loading-scene.cy.ts` ahora espera `world-map`; `base-state-scene.cy.ts` se renombró a `worldmap-scene.cy.ts` y se reescribió para verificar que `world-map` se alcanza sin controles ni progreso y que la escena queda registrada junto al resto del flujo.
- Verificado `npx tsc --noEmit` y `npx vue-tsc --noEmit` sin errores nuevos introducidos por estos cambios (los errores preexistentes en `.story.vue` y otros componentes no relacionados no se tocaron).

incomplete_tasks:
- No se ejecutó Cypress contra un backend real en este sprint (requiere entorno levantado); los specs quedan escritos y alineados con el patrón existente en `fase7-gameview`, a validar en ejecución real.
- `RecognitionGameScene` ('recognition-game') no está registrada en el array de escenas de `GameView.vue`: `WorldMapScene.readEvent` la inicia vía `this.scene.start('recognition-game', ...)` ante `WORLD_ACTIVITY_STARTED`, pero la clave no existe en Phaser. Preexistente, no introducido por este sprint; fuera de alcance (pertenece al motor de minijuegos, no a WorldMap).

contract_changes:
- Ninguno. Se sigue consumiendo `WORLD_STATE_SYNC` tal como está definido en `docs/contracts/api/asyncapi/schemas/world-state-sync-payload.yaml`; `destination.discoveryElements[]` no se procesa en este sprint según lo acordado.

learnings:
- El patrón de comunicación NPC-preference → escena (`registry.set` + `this.events.emit('npc-state-changed', …)`) ya existía en `BaseStateScene`, pero como cada escena Phaser tiene su propio `events` local, `WorldMapScene` necesita su propio manejo de `CHILD_AGENT_ACTIVATED/DEACTIVATED` para que `NubiLayer` (que escucha `this.events` de la escena propietaria) reaccione en tiempo real; no basta con que `BaseStateScene` lo emita.
- El array de escenas de `GameView.vue` y el flujo de `LoadingScene` ya habían sido modificados (sin commitear) antes de este sprint para apuntar a `world-map` en vez de `base-state`; ese cambio dejó caer `FarewellScene` del array, lo cual habría roto la reconexión/despedida. Se restauró como parte de la verificación de integración (tarea 63.7).
- `BaseStateScene` queda registrada pero inalcanzable en el flujo actual (nada llama a `scene.start('base-state')`); no se eliminó por ser una decisión de alcance mayor, fuera de este sprint.

next_sprint_suggestions:
- SPRINT-064: implementar la capa de elementos interactivos (depth 2) y las reacciones ambientales, reintroduciendo el procesamiento de `destination.discoveryElements[]`.
- SPRINT-065: cubrir robustez de conexión en `WorldMapScene` (equivalente a `ConnectionMonitor` de `BaseStateScene`) y decidir si `BaseStateScene` se retira del árbol de escenas.
- Registrar `RecognitionGameScene` en `GameView.vue` cuando se aborde el arranque de minijuegos desde `WorldMapScene`.
