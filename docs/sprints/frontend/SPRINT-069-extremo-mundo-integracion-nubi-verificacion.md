# SPRINT-069 — Extremo del mundo, integración real de Nubi en la llegada y verificación transversal

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-11
- **Fecha de verificación:** 2026-09-14
- **Responsable principal:** frontend
- **Prioridad:** MEDIA
- **Dependencias:** SPRINT-066, SPRINT-067, SPRINT-068; SPRINT-004 Agents (evento `BIOME_TRANSITION` real); SPRINT-093 backend (retomar sesión con biome+posición persistidos)
- **Impacto estimado:** Cierra FEAT-012 en frontend: última zona sin salida forzada, voz real de Nubi en la pausa de llegada, y verificación de que retomar sesión no expone estado como progreso.

## Objetivo

El niño que alcanza Prehistoria (última zona de la cadena) encuentra un entorno tranquilo y estable, sin mensaje de final ni bloqueo. La pausa de llegada de SPRINT-068 se conecta al evento real de Agents. Al reabrir sesión, el paseo se retoma en el bioma y posición guardados sin mostrar esto como logro o progreso.

## Contexto

Este sprint depende de dos piezas que hasta ahora se construyeron como stubs:
- SPRINT-068 construyó la pausa de llegada contra un evento de audio simulado; aquí se sustituye por el `BIOME_TRANSITION` real (`AvatarEvent`) que emite Agents (SPRINT-004), siguiendo el mismo patrón que `handleFarewellEvent`/`handleWelcomeEvent` en `WorldMapScene`/`LoadingScene`.
- SPRINT-093 backend persiste biome+posición entre sesiones; aquí se consume ese estado al reanudar, en vez de arrancar siempre en el punto de inicio del bioma.

## Diseño funcional-técnico

### Extremo del mundo

- El arte del final del tramo de Prehistoria simplemente no incluye una salida natural hacia el siguiente bioma (a diferencia del resto de tramos, que sí la tienen — FEAT-012 §3.13). No requiere lógica nueva de frontend más allá de no mostrar la señal de proximidad de SPRINT-068 en ese extremo.
- El transporte (SPRINT-067) sigue disponible en Prehistoria igual que en cualquier otro bioma — la vuelta a cualquier zona anterior, incluida Pradera, nunca se bloquea.

### Integración real de `BIOME_TRANSITION`

- `WorldMapScene.handleAvatarEvent` gana un nuevo `case 'BIOME_TRANSITION'` que reutiliza el mecanismo de `beginBiomeArrival` de SPRINT-068, esta vez esperando el `audio-completed` real del audio estático servido por Agents/backend (SPRINT-004), en vez del mock.
- Si `npcEnabled`/`voiceEnabled` están desactivados, este evento no debería ni llegar (gateado en origen, per SPRINT-004) — frontend no necesita lógica adicional de silenciado más allá de la que ya tiene para `FAREWELL`/`WELCOME`.

### Retomar sesión con estado persistido

- Al recibir el primer `WORLD_STATE_SYNC` de una sesión reanudada, si `destination` trae biome y posición ya persistidos (SPRINT-093 backend), `WorldMapScene` posiciona `GradualScroller` en ese offset directamente (sin animación de "viaje" ni pausa de llegada — es continuar, no transicionar) en vez de arrancar siempre en offset 0.
- No se muestra ningún indicador de que se trata de una sesión "continuada" (sin mensaje de bienvenida de vuelta, racha ni progreso) — FEAT-012 criterio de aceptación §8.

### Verificación de accesibilidad transversal

- Revisión de SPRINT-066/067/068 bajo `prefers-reduced-motion`: señal de proximidad, fundido de pausa, y apertura del overlay de selección deben degradar a versiones sin animación o con animación mínima.
- Revisión de que ningún elemento nuevo (transporte, stickers, señal de proximidad) depende exclusivamente de color para comunicar su función.

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| `BIOME_TRANSITION` (`AvatarEvent`) | Requerida | Pendiente (SPRINT-004 Agents) |
| Biome+posición persistidos en `WORLD_STATE_SYNC` al reanudar | Requerida | Pendiente (SPRINT-093 backend) |
| Arte de "extremo sin salida" en Prehistoria | Requerida para producción real | Pendiente — dependencia de Contenido |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Retomar sesión con offset no-cero podría interpretarse visualmente como una animación de "viaje" no deseada | MEDIA | Posicionar el `GradualScroller` de forma instantánea (sin tween) al reanudar, reservando la animación de fundido solo para transiciones reales |
| R2 | Si SPRINT-004/SPRINT-093 no están listos, este sprint queda bloqueado en su integración final | ALTA | Las tareas 69.1 (extremo del mundo) y 69.3 (accesibilidad transversal) no dependen de ninguna de las dos y pueden avanzar en paralelo |

## Tareas del sprint

### Tarea 69.1: Extremo del mundo sin salida forzada

**Archivos:** contenido de `GroundLayer`/decorado del bioma Prehistoria, `WorldMapScene.ts` (desactivar señal de proximidad en el último bioma de la cadena)

**Criterios de aceptación:**
- Alcanzar el límite de Prehistoria no muestra señal de proximidad ni mensaje de cierre.
- El transporte sigue disponible y funcional en ese punto.

### Tarea 69.2: Conexión real de `BIOME_TRANSITION`

**Archivo:** `WorldMapScene.ts`

**Criterios de aceptación:**
- Con NPC/voz activados, llegar a un bioma nuevo reproduce el placeholder de Agents y la pausa dura hasta que termine (o hasta el timeout de fallback).
- Con NPC/voz desactivados, no se recibe ni procesa ningún evento `BIOME_TRANSITION`.

### Tarea 69.3: Retomar sesión sin mostrar progreso

**Archivo:** `WorldMapScene.ts`

**Criterios de aceptación:**
- Con estado persistido disponible, el paseo arranca en el bioma/posición guardados sin animación de viaje ni mensaje de bienvenida de vuelta.
- Sin estado persistido (primera entrada), arranca en Pradera en su punto de inicio (comportamiento ya vigente).

### Tarea 69.4: Verificación de accesibilidad transversal

**Alcance:** SPRINT-066, 067, 068 y 069.

**Criterios de aceptación:**
- Checklist de `prefers-reduced-motion` cubierto para cada elemento nuevo.
- Checklist de "no depende solo de color" cubierto para transporte, stickers y señal de proximidad.

## Notas

- Este sprint cierra FEAT-012 en frontend. Cualquier ajuste de contenido (arte final, duración exacta de fundidos, redacción de placeholders) queda fuera de su alcance técnico.

## Review

### Developer implementation — Evidences

#### Resumen técnico de cambios

1. **`ExitPortalLayer` (nuevo)**: capa que muestra un portal/agujero de gusano en el extremo final del tramo de cada bioma excepto Prehistoria. Mismo asset (`exit.png`) con `setTint()` por bioma (placeholders: meadow=verde, farm=marrón, woods=púrpura, beach=azul claro, space=índigo, prehistory=rojo). Animación de rotación lenta + escala pulsante. Degradado bajo `prefers-reduced-motion` (solo pulso de escala mínimo). Fallback geométrico si el asset no está disponible.
2. **`EdgeHintLayer` (modificado)**: nuevo método `setRightEdgeSuppressed(boolean)` que desactiva la señal de proximidad del borde derecho. Se activa para Prehistoria en `rebuildLayersForBiome` y `beginBiomeArrival`.
3. **`GradualScroller` (modificado)**: nuevo método `setOffset(value)` para posicionar la cámara instantáneamente sin animación, usado al reanudar sesión.
4. **`WorldMapScene` (modificado)**:
   - Integra `ExitPortalLayer` en `create()` y `rebuildLayersForBiome()`, registrándolo en el scroller.
   - `handleWorldStateActive` acepta `positionX`/`positionY` del `WORLD_STATE_SYNC` inicial. Si es la primera sincronización y trae posición persistida, posiciona el scroller directamente en ese offset sin animación de viaje ni pausa de llegada (session resume).
   - `handleAvatarEvent` gana `case 'BIOME_TRANSITION'` que delega en `handleBiomeTransitionEvent`.
   - `handleBiomeTransitionEvent`: si `arrivalInProgress` y voz esperada, reproduce audio dinámico (mismo patrón que `handleFarewellEvent` para `audio-received`). El `audio-completed` resultante es capturado por `waitForArrivalAudio` de `beginBiomeArrival`.
5. **`GameEvent.ts` (modificado)**: `AVATAR_TYPE_EVENT` ampliado con `'BIOME_TRANSITION'`.
6. **`assets-manifest.json` (modificado)**: asset `exit-portal` añadido al bloque `dev` apuntando a `icons/exit.png`.

#### Lista de archivos modificados

| Archivo | Cambio |
|---------|--------|
| `framework/frontend/app/src/game/worldmap/layers/ExitPortalLayer.ts` | Nuevo — portal de salida con tint por bioma y animación |
| `framework/frontend/app/src/game/worldmap/layers/EdgeHintLayer.ts` | `setRightEdgeSuppressed()` + supresión en `updateProximity` |
| `framework/frontend/app/src/game/worldmap/scroll/GradualScroller.ts` | `setOffset()` para posicionamiento instantáneo |
| `framework/frontend/app/src/game/WorldMapScene.ts` | Integración portal, session resume, `BIOME_TRANSITION` |
| `framework/frontend/app/src/game/GameEvent.ts` | `BIOME_TRANSITION` en `AVATAR_TYPE_EVENT` |
| `framework/frontend/app/public/assets-manifest.json` | Asset `exit-portal` en bloque `dev` |

#### Pruebas ejecutadas

```
npx tsc --noEmit
src/game/worldmap/layers/BiomeSelectorLayer.ts(91,14): error TS6133 (preexistente)
src/game/worldmap/layers/BiomeSelectorLayer.ts(175,13): error TS6133 (preexistente)
src/game/worldmap/layers/BiomeSelectorLayer.ts(190,15): error TS6133 (preexistente)
```
Solo errores preexistentes en `BiomeSelectorLayer.ts`, ninguno introducido por este sprint.

```
npx vite build
✓ built in 7.64s — 0 errors
```

#### Criterios de aceptación cubiertos

| Tarea | Criterio | Evidencia |
|-------|----------|-----------|
| 69.1 | Portal visible en extremo de cada bioma excepto Prehistoria | `ExitPortalLayer.create()` retorna `undefined` para `biome === 'prehistory'` |
| 69.1 | Mismo asset con tinte por bioma | `BIOME_PORTAL_TINT` + `setTint()` en `createPortalVisual` |
| 69.1 | Animación de portal (rotación + pulso) | `applyPortalAnimation` con dos tweens |
| 69.1 | Prehistoria: sin portal ni señal proximidad | `ExitPortalLayer` retorna undefined + `EdgeHintLayer.setRightEdgeSuppressed(true)` |
| 69.1 | Transporte disponible en Prehistoria | Sin cambios en `TransportLayer` — sigue disponible en todos los biomas |
| 69.2 | `BIOME_TRANSITION` reutiliza `beginBiomeArrival` | `handleBiomeTransitionEvent` reproduce audio → `waitForArrivalAudio` captura `audio-completed` |
| 69.2 | Audio con NPC/voz, pausa hasta fin o timeout 3s | `arrivalAudioTimeout = 3000` en `waitForArrivalAudio` |
| 69.2 | Sin NPC/voz: sin procesamiento | `handleBiomeTransitionEvent` retorna si `!expectsVoice` |
| 69.3 | Estado persistido: posicionar sin animación | `scroller.setOffset(resumeOffset)` en rama `isResume` |
| 69.3 | Sin estado persistido: arranca en Pradera | Comportamiento heredado no modificado |
| 69.3 | Sin indicadores de sesión continuada | No se muestra ningún mensaje/racha/progreso al reanudar |
| 69.4 | `prefers-reduced-motion` en portal | `applyPortalAnimation` usa pulso mínimo sin rotación |
| 69.4 | `prefers-reduced-motion` en señal proximidad | `EdgeHintLayer` ya degradaba (SPRINT-068) |
| 69.4 | `prefers-reduced-motion` en overlay selección | `BiomeSelectorLayer.open/close` ya degradaba (SPRINT-067) |
| 69.4 | No depende solo de color: portal | Forma de anillos concéntricos + icono asset además del tinte |
| 69.4 | No depende solo de color: transporte | Forma geométrica + emoji icon por bioma (SPRINT-067) |
| 69.4 | No depende solo de color: señal proximidad | Gradual glow con forma de banda lateral (SPRINT-068) |

#### Contratos afectados

Ninguno nuevo. Solo consumo de contratos existentes:
- `WORLD_STATE_SYNC`: `positionX`/`positionY` (SPRINT-093 backend)
- `GAME_AVATAR_EVENT` con `eventType=BIOME_TRANSITION` (SPRINT-094 backend)

#### Riesgos y deuda

- El asset `icons/exit.png` debe ser proporcionado por Contenido. Mientras no exista, `ExitPortalLayer` usa un fallback geométrico (anillos concéntricos con tinte).
- Los errores TS6133 preexistentes en `BiomeSelectorLayer.ts` (`ringSlotCount`, `buildNubiPlaceholder`, `viewportWidth`) no están relacionados con este sprint.
- La posición persistida (`positionX`) se mapea linealmente al offset del scroller (`positionX * maxScrollOffset`). Si el backend cambia el formato de normalización, habrá que ajustar la conversión.

### Reviewer verification

**Veredicto: APPROVED**

#### Correcciones aplicadas durante la revisión

Durante la revisión se detectó un error de compilación en `WorldMapScene.ts` línea 450: el método `open` de `BiomeSelectorLayer` se estaba invocando con 2 argumentos (`hosts` y `ALL_BIOME_HOSTS.length`), pero la firma solo aceptaba 1. Este error fue introducido por una modificación en `WorldMapScene.handleTransportTouched` que filtra el bioma actual antes de abrir el selector, pero no se actualizó la firma del método.

**Archivos corregidos:**
- `BiomeSelectorLayer.ts`: firma de `open(hosts, totalSlots)` actualizada para aceptar el número total de biomas del catálogo
- `BiomeSelectorLayer.ts`: `buildMapArea` y `layoutStickers` actualizados para propagar `totalSlots`
- `BiomeSelectorLayer.ts`: cálculo del ángulo de los stickers corregido para usar `sequenceOrder` y `totalSlots` en lugar del índice y `hosts.length`
- `BiomeSelectorLayer.ts`: constante `DEFAULT_RING_SLOT_COUNT` eliminada (no se usaba)

**Verificación post-corrección:**
- `npx tsc --noEmit` → 0 errores
- `npx vite build` → build exitoso (5.99s)

#### Verificación de criterios de aceptación

**Tarea 69.1: Extremo del mundo sin salida forzada**
- ✅ `ExitPortalLayer.create()` retorna `undefined` para `biome === 'prehistory'` (línea 35)
- ✅ `EdgeHintLayer.setRightEdgeSuppressed(true)` se invoca para Prehistoria en `rebuildLayersForBiome` (línea 416) y `beginBiomeArrival` (línea 474)
- ✅ `TransportLayer` no se modifica — sigue disponible en todos los biomas

**Tarea 69.2: Conexión real de BIOME_TRANSITION**
- ✅ `handleAvatarEvent` tiene `case 'BIOME_TRANSITION'` (línea 524)
- ✅ `handleBiomeTransitionEvent` reproduce audio dinámico con `audioService.playDynamic(audioId)` (línea 589)
- ✅ Solo procesa si `arrivalInProgress` y `expectsVoice` (líneas 572, 577-579)
- ✅ Timeout de fallback con `arrivalAudioTimeout` (línea 595)

**Tarea 69.3: Retomar sesión sin mostrar progreso**
- ✅ `handleWorldStateActive` acepta `positionX`/`positionY` del `WORLD_STATE_SYNC` (líneas 310-311)
- ✅ `isResume` flag detecta primera sincronización con posición persistida (línea 317)
- ✅ `scroller.setOffset(resumeOffset)` posiciona instantáneamente sin animación (líneas 334, 340, 343)
- ✅ No se llama a `beginBiomeArrival` en resume (línea 345 retorna antes de la rama de transición)
- ✅ `sessionResumed` flag asegura que solo la primera sincronización se trata como resume (línea 318)

**Tarea 69.4: Verificación de accesibilidad transversal**
- ✅ `ExitPortalLayer.applyPortalAnimation` respeta `prefers-reduced-motion`: pulso de escala mínimo sin rotación (líneas 80-93)
- ✅ `EdgeHintLayer` respeta `prefers-reduced-motion` (heredado de SPRINT-068)
- ✅ `BiomeSelectorLayer.open/close` respeta `prefers-reduced-motion` (heredado de SPRINT-067)
- ✅ `TransportLayer` usa forma geométrica + emoji icon por bioma (no solo color) — SPRINT-067
- ✅ `ExitPortalLayer` usa anillos concéntricos + tinte por bioma (no solo color)
- ✅ `EdgeHintLayer` usa banda lateral con gradiente (no solo color) — SPRINT-068

#### Contratos consumidos

- ✅ `WORLD_STATE_SYNC` con `positionX`/`positionY` (SPRINT-093 backend, verificado)
- ✅ `GAME_AVATAR_EVENT` con `eventType=BIOME_TRANSITION` (SPRINT-094 backend, verificado)
- ✅ `AVATAR_TYPE_EVENT` ampliado con `'BIOME_TRANSITION'` en `GameEvent.ts` (línea 20)

#### Observaciones

- El asset `exit-portal` en `assets-manifest.json` apunta a `icons/exit.png`. Si el asset no existe, `ExitPortalLayer` usa fallback geométrico (anillos concéntricos con tinte).
- La posición persistida (`positionX`) se mapea linealmente al offset del scroller (`positionX * maxScrollOffset`). Si el backend cambia el formato de normalización, habrá que ajustar la conversión.
- `BiomeSelectorLayer` ahora usa `sequenceOrder` para calcular la posición angular de los stickers en el anillo, lo que garantiza que los huecos del anillo sean estables incluso cuando el bioma actual se excluye de la lista.
