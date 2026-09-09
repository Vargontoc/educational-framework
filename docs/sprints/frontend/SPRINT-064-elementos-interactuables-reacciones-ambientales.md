# SPRINT-064 — Elementos interactuables y reacciones ambientales

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-063 completado
- **Impacto estimado:** Añade elementos interactuables al paisaje con reacciones ambientales breves y no evaluativas al toque.

## Objetivo

Los `discoveryElements` del `WORLD_STATE_SYNC` se renderizan como elementos interactuables con reacciones ambientales al toque, sin abrir minijuegos ni enviar eventos al backend.

## Contexto

SPRINT-063 estableció la arquitectura base de WorldMap con paisaje placeholder y desplazamiento. Este sprint añade la capa de elementos interactuables que el niño puede tocar para producir reacciones ambientales breves.

**Decisiones técnicas confirmadas:**
- No se envía `WorldDiscoveryElementInteractiveEvent` al backend en fase 1
- Reacción ambiental genérica única (oscilación de escala + tint temporal)
- Hit-area mínima: 80x80 px lógicos
- Separación mínima entre elementos: 60 px

## Diseño funcional-técnico

### Capa de elementos interactivos

**Módulo:** `InteractiveLayer.ts`

**Responsabilidades:**
- Renderizar `discoveryElements` del `WORLD_STATE_SYNC`
- Posicionar elementos con layout horizontal y separación mínima
- Gestionar interacción táctil (pointerdown)
- Aplicar pistas visuales pasivas según `interactionCueType`

**Placeholder visual:**
- Cada elemento usa una forma geométrica diferenciada por `elementType`:
  - Círculo, cuadrado, triángulo, estrella
- Color diferenciado pero no dependiente exclusivamente de color
- Hit-area: 80x80 px mínimo (ampliado si el asset visual es menor)

### Sistema de reacciones ambientales

**Módulo:** `EnvironmentReaction.ts`

**Responsabilidades:**
- Ejecutar animación breve (300-600ms) sobre el elemento tocado
- No evaluativa, no textual, no dependiente de audio

**Reacción genérica (fase 1):**
- Oscilación de escala: 1.0 → 1.2 → 1.0
- Cambio de tint temporal (brillo)
- Opcionalmente: partícula simple (estrella/burbuja) que se desvanece
- Duración: 300-600ms
- Respeta `prefers-reduced-motion`: solo cambio de opacidad suave

### Flujo de interacción

1. Niño toca un elemento interactuable (hit-area ≥ 80x80 px)
2. `InteractiveLayer` captura `pointerdown`
3. Se invoca `EnvironmentReaction.play(element)`
4. Animación breve se ejecuta sobre el elemento
5. El elemento puede ser tocado repetidamente; cada toque reproduce la reacción
6. No hay bloqueo, no hay contador, no hay feedback negativo
7. **NO se envía `WorldDiscoveryElementInteractiveEvent` al backend**

### Pista visual pasiva

Si `interactionCueType` tiene valor:
- Aplicar brillo sutil intermitente al elemento
- No depende exclusivamente de color
- Combinar con forma, tamaño o movimiento sutil
- Indica que el elemento es tocable sin ser un "botón"

### Integración con desplazamiento

- Los elementos pertenecen al mundo (no a la cámara)
- Se mueven con el scroll del paisaje
- `InteractiveLayer` tiene prioridad de input sobre `BackgroundLayer`
- Phaser resuelve por orden de profundidad: elementos (depth 2) capturan `pointerdown` antes que el scroller

### Desactivación de minijuegos

- `WORLD_ACTIVITY_STARTED` no se procesa (o se logea sin acción)
- `WorldDiscoveryElementInteractiveEvent` no se envía
- `hasActivity` del discovery element se ignora en fase 1

## Contratos y dependencias externas

### Endpoints/contratos que consume

- `WORLD_STATE_SYNC` (WebSocket): `destination.discoveryElements[]`
  - `proposalRuntimeId`
  - `discoveryElementId`
  - `visualAssetKey` → si no existe, usar placeholder geométrico
  - `hasActivity` → ignorado en fase 1
  - `interactionCueType` → usado para pista visual pasiva
  - `elementType` → usado para seleccionar forma geométrica placeholder

### Dependencias hacia backend/agents/tts

| Dependencia | Tipo | Estado |
|-------------|------|--------|
| `WORLD_STATE_SYNC` (WS) | Requerida | Ya definido. Backend envía `discoveryElements` poblados |
| `WorldDiscoveryElementInteractiveEvent` | **NO se envía en fase 1** | La clase existe pero no se invoca |

### Handoffs documentados

| Handoff | Capa destino | Contenido |
|---------|--------------|-----------|
| Catálogo de reacciones (fase 4B) | Frontend/Content | Repertorio de animaciones se define en fase 4B. Fase 1: reacción genérica única |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El drag/scroll interfiere con el toque en elementos | ALTA | `InteractiveLayer` tiene prioridad de input (depth 2 > depth 0) |
| R2 | Los elementos parecen "botones de minijuego" | MEDIA | Formas orgánicas (no rectángulos con bordes de botón). Reacciones ambientales, no "abrir/cerrar" |
| R3 | Assets placeholder no distinguidos visualmente | MEDIA | Cada elemento usa forma geométrica distinta + color diferenciado |
| R4 | `prefers-reduced-motion` no cubre todos los casos | BAJA | Revisión exhaustiva en `EnvironmentReaction` |

---

## Tareas del sprint

### Tarea 64.1: Crear `InteractiveLayer.ts`

**Archivo:** `src/components/game/worldmap/layers/InteractiveLayer.ts`

**Descripción:** Renderiza `discoveryElements` con placeholders geométricos y gestiona interacción.

**Criterios de aceptación:**
- Renderiza cada `discoveryElement` como forma geométrica diferenciada por `elementType`
- Hit-area ≥ 80x80 px lógicos
- Separación mínima entre elementos: 60 px
- Posiciona elementos con layout horizontal
- Si `visualAssetKey` no existe, usa placeholder geométrico
- Depth: 2

### Tarea 64.2: Crear `EnvironmentReaction.ts`

**Archivo:** `src/components/game/worldmap/reactions/EnvironmentReaction.ts`

**Descripción:** Reacción ambiental genérica al tocar un elemento.

**Criterios de aceptación:**
- Oscilación de escala: 1.0 → 1.2 → 1.0 (300-600ms)
- Cambio de tint temporal (brillo)
- Opcionalmente: partícula simple que se desvanece
- Respeta `prefers-reduced-motion`: solo cambio de opacidad suave
- Interfaz `play(element)` reutilizable

### Tarea 64.3: Integrar interacción en `InteractiveLayer`

**Archivo:** `src/components/game/worldmap/layers/InteractiveLayer.ts`

**Descripción:** Conecta `pointerdown` con `EnvironmentReaction.play()`.

**Criterios de aceptación:**
- `pointerdown` en elemento → `EnvironmentReaction.play(element)`
- Sin envío WebSocket
- Sin bloqueo por repetición
- Tocar repetidamente reproduce la reacción cada vez

### Tarea 64.4: Pista visual pasiva por `interactionCueType`

**Archivo:** `src/components/game/worldmap/layers/InteractiveLayer.ts`

**Descripción:** Aplica brillo sutil intermitente si `interactionCueType` tiene valor.

**Criterios de aceptación:**
- Si `interactionCueType` tiene valor, aplicar brillo sutil intermitente
- No depende exclusivamente de color
- Combinar con forma/tamaño

### Tarea 64.5: Integrar `InteractiveLayer` en `WorldMapScene`

**Archivo:** `src/components/game/WorldMapScene.ts`

**Descripción:** Conecta `InteractiveLayer` con `WORLD_STATE_SYNC` existente.

**Criterios de aceptación:**
- Al recibir `WORLD_STATE_SYNC` con `discoveryElements`, se renderizan en `InteractiveLayer`
- Los elementos se mueven con el scroll (pertenecen al mundo)
- Integración con `GradualScroller` para movimiento coordinado

### Tarea 64.6: Desactivar transición a minijuegos en fase 1

**Archivo:** `src/components/game/WorldMapScene.ts`

**Descripción:** Asegura que no se lanzan minijuegos ni se envían eventos de interacción.

**Criterios de aceptación:**
- `WORLD_ACTIVITY_STARTED` no se procesa (o se logea sin acción)
- `WorldDiscoveryElementInteractiveEvent` no se envía
- `hasActivity` del discovery element se ignora

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `src/components/game/worldmap/layers/InteractiveLayer.ts` | Nuevo |
| `src/components/game/worldmap/reactions/EnvironmentReaction.ts` | Nuevo |
| `src/components/game/WorldMapScene.ts` | Modificación |

## Estimación

- **Duración:** 2-3 días
- **Complejidad:** Media
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Al recibir `WORLD_STATE_SYNC` con `discoveryElements`, se muestran en el paisaje como formas diferenciadas.
2. Al tocar un elemento, se observa una reacción visual breve (oscilación + tint) sin texto ni sonido obligatorio.
3. Tocar repetidamente un elemento reproduce la reacción cada vez, sin bloqueo ni feedback negativo.
4. No tocar ningún elemento no produce avisos, insistencia ni consecuencias.
5. La reacción es comprensible sin audio (solo visual).
6. La comprensión de qué es "tocable" no depende exclusivamente de color.
7. Tocar un elemento NO abre minijuego, NO cambia progreso, NO envía evento al backend.
8. Los elementos se desplazan correctamente con el scroll del paisaje.

## Dependencias bloqueantes

- [ ] SPRINT-063 completado.
- [ ] `WORLD_STATE_SYNC` enviado por backend con `destination.discoveryElements[]` poblado.

## Handoffs a otras capas

Ninguno en este sprint.

## Notas adicionales

Este sprint cierra la fase 1 de FEAT-011. Los elementos interactuables producen reacciones ambientales breves y no evaluativas, sin abrir minijuegos ni enviar eventos al backend.

La reacción genérica es extensible en fase 4B a múltiples variantes. La estructura de `EnvironmentReaction` permite añadir nuevas reacciones sin modificar `InteractiveLayer`.

Los elementos se mueven con el scroll del paisaje, dando la sensación de que pertenecen al mundo y no a la cámara.

## Agent Instruction

- No implementar código de producto. Este sprint es para el desarrollador de Frontend.
- Seguir patrón de módulos co-locados bajo `src/components/game/worldmap/`.
- No enviar `WorldDiscoveryElementInteractiveEvent` al backend.
- Respetar preferencia parental de NPC (alternativa 1): Nubi sin reacciones expresivas.
- Consultar `prefers-reduced-motion` vía `window.matchMedia`.
- Hit-area mínima: 80x80 px lógicos.
- Separación mínima entre elementos: 60 px.

## Review

completed_tasks:
- Creado `src/components/game/worldmap/layers/InteractiveLayer.ts`: renderiza `discoveryElements` en un `Container` propio (depth 2). Cada elemento es un sub-contenedor con una `Zone` interactiva de 80x80px (hit-area mínima) más una forma geométrica visual (círculo/cuadrado/triángulo/estrella) elegida y coloreada de forma determinista por `elementType` (hash simple), de modo que un mismo tipo siempre obtiene la misma forma/color y tipos distintos difieren en ambos. Layout horizontal con separación mínima garantizada (`interactiveMinSeparation` = 60px + hit-area, vía `Math.max` sobre el paso calculado). Si `visualAssetKey` no existe no se hace fallback a imagen: siempre se usa el placeholder geométrico (no hay carga de assets reales en esta fase).
- Creado `src/components/game/worldmap/reactions/EnvironmentReaction.ts`: `play(shape)` reutilizable con oscilación de escala 1.0→1.2→1.0 (~440ms con yoyo, dentro de 300-600ms), flash de `fillColor` temporal restaurado por `time.delayedCall`, y una partícula simple (círculo blanco) reparentada al mismo contenedor del elemento que se desvanece y se destruye. Bajo `prefers-reduced-motion: reduce` sustituye todo lo anterior por un cambio de opacidad suave (0.5→1, 400ms) — nada de escala, tint ni partícula.
- Conectada la interacción: `InteractiveLayer.setOnTouch()` invoca `EnvironmentReaction.play()` en cada `pointerdown` sobre la `Zone`; no hay envío WebSocket ni bloqueo por repetición (cada toque relanza la reacción independientemente del anterior, matando cualquier tween en curso sobre el mismo shape).
- Pista visual pasiva: si `interactionCueType` tiene valor, se aplica un tween lento (1.4s, yoyo, repeat infinito) que combina opacidad (0.75↔1) y escala (1↔1.04), no solo color. Se omite bajo `prefers-reduced-motion`.
- Integrado `InteractiveLayer` en `WorldMapScene.ts`: se crea junto al resto de capas en `create()`, su contenedor se registra en `GradualScroller` con factor 1.0 (los elementos pertenecen al mundo, se desplazan 1:1 con el drag, a diferencia del factor 0.3 de `ParallaxLayer`), y `WORLD_STATE_SYNC` (status ACTIVE) invoca `interactiveLayer.render(destination.discoveryElements)` además de `backgroundLayer.buildFrom(biome)`.
- Prioridad de input: `GradualScroller.onPointerDown` ahora recibe el segundo argumento `currentlyOver` del evento `pointerdown` de `scene.input` y no inicia el drag si el puntero está sobre un objeto interactivo (los elementos, al estar en depth 2, aparecen en `currentlyOver` y "capturan" el toque antes que el scroller, evitando que tocar un elemento también arranque un arrastre).
- Desactivada la transición a minijuegos en fase 1: `WORLD_ACTIVITY_STARTED` ya no llama a `this.scene.start('recognition-game', …)`; solo se registra en consola sin acción. `hasActivity` del discovery element no se lee en ningún punto. `WorldDiscoveryElementInteractiveEvent` no se construye ni se envía en todo el módulo.
- `npx tsc --noEmit` y `npx vue-tsc --noEmit`: sin errores nuevos en los ficheros tocados (verificado explícitamente por grep sobre la salida).

incomplete_tasks:
- No se ejecutó Cypress contra un backend real (requiere entorno levantado); no se añadieron specs E2E nuevos para esta capa por la dificultad de verificar visualmente formas/colores sin introspección adicional en el harness `__NUBI_GAME_STATE__`. Queda como deuda de cobertura E2E.

contract_changes:
- Ninguno. Se sigue consumiendo `destination.discoveryElements[]` de `WORLD_STATE_SYNC` tal como está definido; no se envía ningún evento nuevo al backend.

hallazgo_importante (bloqueante para SPRINT-065, no corregido aquí por estar fuera de alcance):
- Al auditar los specs Cypress de `fase7-gameview` para esta entrega se detectó que el cambio de enrutamiento `LoadingScene → world-map` (heredado de SPRINT-063, ya no commiteado como parte de un cambio previo) dejó **sin cobertura funcional real** tres specs que asumen el comportamiento de `BaseStateScene`, escena que ya no se alcanza en el flujo normal:
  - `websocket-sesion.cy.ts`: el test de expulsión (`CHILD_EXPELLED`) fallará — `WorldMapScene.readEvent` no maneja ese evento (cae al `default`), a diferencia de `BaseStateScene.handleExpulsion()`.
  - `recuperacion-despedida.cy.ts`: los tests de reconexión (`closeWs()`) y `SESSION_EXPIRED` fallarán — `WorldMapScene` no tiene equivalente de `ConnectionMonitor` ni maneja `SESSION_EXPIRED`/`SESSION_INVALIDATED`.
  - `preferencias-dinamicas.cy.ts`: el test de `CHILD_TTS_ACTIVATED` fallará (`WorldMapScene` no actualiza `ttsEnabled`); los tests de `GAME_ERROR` (recuperable/crítico) fallarán porque no hay `ErrorClassifier` integrado en `WorldMapScene`.
  - Estos tres ficheros solo se ajustaron parcialmente en SPRINT-063 (`loading-scene.cy.ts` y el renombrado `worldmap-scene.cy.ts`); no se tocaron aquí porque arreglar el string `'base-state' → 'world-map'` no los haría pasar: la funcionalidad que verifican (reconexión, expulsión, expiración de sesión, error crítico, sincronización de TTS) simplemente no existe todavía en `WorldMapScene`. Parchear solo el nombre de escena habría dado una falsa sensación de cobertura.
  - Esto coincide exactamente con el objetivo declarado de SPRINT-065 ("Robustez, integración y verificación transversal"): portar a `WorldMapScene` el equivalente de `ConnectionMonitor`, el manejo de `CHILD_EXPELLED`/`SESSION_EXPIRED`/`SESSION_INVALIDATED`, `GAME_ERROR` vía `ErrorClassifier`, y `CHILD_TTS_ACTIVATED`/`DEACTIVATED` (que sí se añadió el equivalente para `CHILD_AGENT_ACTIVATED`/`DEACTIVATED` en SPRINT-063, pero no para TTS). Recomendado abordarlo como la primera tarea de SPRINT-065, actualizando estos tres specs a la vez que se implementa la paridad funcional.

learnings:
- El "layout horizontal" de discoveryElements en esta fase es puramente sintético en el frontend (índice → posición): el backend (FEAT-010) aún no envía coordenadas ni orden estable de los elementos, solo su lista. Cuando FEAT-010 fase 2/3 defina posiciones reales, `InteractiveLayer.render()` deberá leerlas del payload en vez de calcular el layout por índice.
- Usar `scene.input.on('pointerdown', …)` a nivel de escena para el drag-scroll y objetos interactivos individuales (`zone.on('pointerdown', …)`) para los elementos funciona bien en conjunto gracias al segundo argumento `currentlyOver` que Phaser pasa al handler de escena; no hace falta `stopPropagation` manual.
- Reparentar la partícula de la reacción dentro del mismo `Container` que el elemento tocado (en vez de añadirla directamente a la escena) evita tener que traducir coordenadas locales↔mundo y hace que la partícula se desplace correctamente con el scroll sin lógica adicional.

next_sprint_suggestions:
- SPRINT-065 (ver "hallazgo_importante" arriba): portar robustez de conexión/errores/preferencias dinámicas de `BaseStateScene` a `WorldMapScene` y corregir los tres specs Cypress afectados.
- Fase 4B: repertorio de reacciones ambientales múltiples (la estructura de `EnvironmentReaction.play()` ya está pensada para añadir variantes sin tocar `InteractiveLayer`).
- Cuando el backend defina posiciones estables por elemento (FEAT-010 fase 2/3), sustituir el layout sintético por índice en `InteractiveLayer.render()`.
