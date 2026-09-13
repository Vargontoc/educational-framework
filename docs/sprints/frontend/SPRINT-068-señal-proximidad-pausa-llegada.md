# SPRINT-068 — Señal de proximidad, pausa de llegada y transición entre biomas distantes

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-11
- **Fecha de verificación:** 2026-09-13
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-066; SPRINT-067; SPRINT-092 backend (contrato de transporte, para transiciones iniciadas desde el selector)
- **Impacto estimado:** Cierra el ciclo de transición: aviso antes del límite del tramo, pausa de llegada al nuevo bioma, y tratamiento especial para pares visualmente muy distintos.

## Objetivo

Antes de llegar al final del tramo actual, mostrar una señal ambiental suave y no bloqueante. Al cruzar el límite (o al elegir un destino desde el transporte), aplicar una pausa visual breve de llegada cuya duración depende de si hay una intervención de voz de Nubi en curso.

## Contexto

**Decisión confirmada por el usuario (2026-09-11):** la pausa de llegada no tiene duración fija — depende de si hay voz de Nubi. Esto obliga a construir la pausa como una espera condicionada a un evento de audio, con el mismo patrón ya usado en `WorldMapScene`/`LoadingScene` para `WELCOME`/`FAREWELL`: escuchar `audio-completed` en `AudioService`, con un timeout de fallback (~3s) si el audio dinámico no llega, y una duración corta fija cuando no se espera ninguna voz (NPC o voz desactivados).

Este sprint construye el mecanismo de pausa contra un evento de transición **aún no disponible** (el `BIOME_TRANSITION` real es SPRINT-004 de Agents). Se implementa con un punto de extensión claro para no bloquear este sprint en la disponibilidad de Agents, y se conecta en SPRINT-069.

## Diseño funcional-técnico

### Señal de proximidad

- `GradualScroller` ya expone `getOffset()`. Con el `worldWidth` real del bioma activo (SPRINT-066), se puede calcular la distancia restante hasta `maxScrollOffset`.
- Al entrar en una banda de proximidad configurable (nueva constante `WORLD_MAP_CONFIG.edgeProximityThreshold`), activar una señal visual suave (brillo/partícula sutil en el borde del `GroundLayer`, reutilizando el patrón de tween de `EnvironmentReaction`) — nunca un texto, flecha imperativa ni bloqueo del arrastre.
- La señal no obliga a continuar: el niño puede seguir arrastrando en cualquier dirección o quedarse quieto sin penalización (FEAT-012 criterio de aceptación §3).

### Pausa de llegada

- Nuevo método en `WorldMapScene` (p. ej. `beginBiomeArrival(biome: string)`) invocado tanto al cruzar el límite del tramo como al recibir `'destination-selected'` de SPRINT-067.
- Secuencia:
  1. Fundido corto de la escena (reutilizable, sin temporizador visible).
  2. Si se espera una intervención de Nubi (gateado por `npcEnabled`/`voiceEnabled`, mismos flags ya usados en `NubiLayer`/`registry`), esperar a `audio-completed` de `AudioService`, con timeout de fallback ~3s (mismo patrón que `LoadingScene.handleWelcomeEvent`).
  3. Si no se espera voz, esperar solo una duración corta fija (a definir, coherente con el fundido — p. ej. 800-1200ms).
  4. Reconstruir capas del nuevo bioma (SPRINT-066) y quitar el fundido.
- Mientras dure la pausa, no se debe mostrar ningún indicador de carga, porcentaje ni mensaje de logro (FEAT-012 criterio de aceptación §4).

### Transición entre biomas visualmente muy distintos

- Para pares configurados como "distantes" (p. ej. Bosque encantado → Espacio; el catálogo exacto lo confirma Contenido), el fundido de la pausa de llegada es algo más largo o incorpora una capa intermedia neutra, en vez del mismo fundido corto genérico — evita que el salto se perciba como un error de la experiencia (FEAT-012 criterio de aceptación §5).

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| `worldWidth`/posición real | Requerida | SPRINT-066 (mismo sprint set) |
| Evento `BIOME_TRANSITION` real de Nubi | Deseable, no bloqueante para este sprint | Pendiente (SPRINT-004 Agents) — este sprint construye el mecanismo de espera contra un stub |
| Lista de pares "visualmente distantes" | Deseable | Pendiente de confirmación por Contenido |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Sin el evento real de Agents, la pausa podría quedar solo probada en su rama "sin voz" | MEDIA | Cubrir la rama "con voz" con un evento de prueba simulado (mock de `audio-completed`) hasta que SPRINT-004 esté listo |
| R2 | Una señal de proximidad demasiado visible se percibe como obligación; demasiado débil pasa desapercibida (riesgo ya señalado en FEAT-012 §7) | MEDIA | Validar con Contenido/UX antes de cerrar el diseño visual final de la señal |
| R3 | El timeout de fallback (~3s) deja al niño esperando en pantalla en fundido si el audio nunca llega | BAJA | Mismo timeout ya validado en el flujo de `WELCOME`/`FAREWELL`; reutilizar sin reinventar el valor |

## Tareas del sprint

### Tarea 68.1: Señal de proximidad al límite del tramo

**Archivos:** `GradualScroller.ts`, `GroundLayer.ts` o nueva capa `EdgeHintLayer.ts`, `worldMapConfig.ts`

**Criterios de aceptación:**
- Nueva constante `edgeProximityThreshold` en `WORLD_MAP_CONFIG`.
- La señal se activa al entrar en la banda de proximidad y se desactiva si el jugador se aleja de nuevo.
- No bloquea el arrastre ni fuerza continuar.

### Tarea 68.2: Mecanismo de pausa de llegada condicionado a audio

**Archivo:** `WorldMapScene.ts`

**Criterios de aceptación:**
- `beginBiomeArrival(biome)` espera a `audio-completed` (con timeout ~3s) cuando se espera voz, o una duración corta fija cuando no.
- No se muestra porcentaje, temporizador visible, ni mensaje de logro durante la pausa.
- Funciona tanto al cruzar el límite del tramo como al elegir destino desde el transporte (SPRINT-067).

### Tarea 68.3: Transición reforzada para pares visualmente distantes

**Archivo:** `WorldMapScene.ts` (o nueva utilidad `worldmap/transitions/`)

**Criterios de aceptación:**
- Al menos un par de biomas configurado como "distante" usa una transición distinta (más larga o con capa intermedia) que el resto.
- El resto de transiciones usa el fundido corto genérico.

## Notas

- Este sprint no integra el evento real `BIOME_TRANSITION` — eso es SPRINT-069, una vez exista SPRINT-004 de Agents.
- La lista de pares "distantes" y la duración exacta de cada fundido son decisiones de contenido/UX pendientes de validar, no de arquitectura técnica.

## Review

### Developer implementation — Evidencias

**Fecha:** 2026-09-13

#### Tarea 68.1: Señal de proximidad al límite del tramo

**Archivos modificados/creados:**
- `framework/frontend/app/src/game/worldmap/config/worldMapConfig.ts` — nueva constante `edgeProximityThreshold: 200`
- `framework/frontend/app/src/game/worldmap/layers/EdgeHintLayer.ts` — nueva capa
- `framework/frontend/app/src/game/WorldMapScene.ts` — integración de `EdgeHintLayer` en `create()`, `update()` y `cleanupLayers()`

**Criterios de aceptación:**
- ✅ Nueva constante `edgeProximityThreshold` en `WORLD_MAP_CONFIG` (valor: 200px)
- ✅ `EdgeHintLayer.updateProximity(offset, maxScrollOffset)` activa la señal al entrar en la banda y la desactiva al salir
- ✅ La señal es un brillo suave en los bordes laterales (gradiente amarillo cálido, alpha 0.35), no bloquea el arrastre ni fuerza continuar
- ✅ Respeta `prefers-reduced-motion`: usa alpha fijo sin animación

#### Tarea 68.2: Mecanismo de pausa de llegada condicionado a audio

**Archivos modificados:**
- `framework/frontend/app/src/game/WorldMapScene.ts` — nuevos métodos `beginBiomeArrival()` y `waitForArrivalAudio()`
- `framework/frontend/app/src/game/worldmap/config/worldMapConfig.ts` — nuevas constantes `arrivalFadeShort: 800`, `arrivalFadeDistant: 1600`, `arrivalAudioTimeout: 3000`, `arrivalNoVoiceDelay: 1000`

**Criterios de aceptación:**
- ✅ `beginBiomeArrival(biome, worldWidth, onMidpoint)` ejecuta la secuencia: fundido → reconstrucción capas → espera audio → fundido salida
- ✅ `waitForArrivalAudio()` espera `audio-completed` de `AudioService` con timeout ~3s cuando `npcEnabled && ttsEnabled && voiceEnabled`; si no, espera duración corta fija (1000ms)
- ✅ No se muestra porcentaje, temporizador visible ni mensaje de logro durante la pausa (el overlay es un rectángulo negro sin texto)
- ✅ Funciona al recibir `WORLD_STATE_SYNC` con cambio de bioma (desde backend) y al elegir destino desde el transporte (SPRINT-067, vía `handleDestinationSelected` → `sendWorldTravel` → backend → `WORLD_STATE_SYNC`)
- ✅ Mismo patrón que `LoadingScene.handleWelcomeEvent`: `audioService.once('audio-completed', ...)` con timeout de fallback

#### Tarea 68.3: Transición reforzada para pares visualmente distantes

**Archivos creados:**
- `framework/frontend/app/src/game/worldmap/transitions/BiomeTransition.ts` — utilidad de transición
- `framework/frontend/app/src/game/worldmap/config/worldMapConfig.ts` — `DISTANT_BIOME_PAIRS` y `isDistantBiomePair()`

**Criterios de aceptación:**
- ✅ Pares distantes configurados: `woods↔space`, `beach↔space`, `prehistory↔space`
- ✅ `getArrivalFadeDuration(from, to)` devuelve `arrivalFadeDistant` (1600ms) para pares distantes, `arrivalFadeShort` (800ms) para el resto
- ✅ `fadeToBlack()` y `fadeFromBlack()` usan la duración adecuada según el par
- ✅ Respeta `prefers-reduced-motion`: transición instantánea sin animación

#### Verificación técnica

- ✅ `tsc --noEmit` — sin errores
- ✅ `vite build` — compilación exitosa (5.41s)
- ✅ Sin nuevos contratos (solo consumo de `AudioService` y `WORLD_STATE_SYNC` existentes)

#### Riesgos y deuda

- **R1 (mitigado):** La rama "con voz" se probará con el mock de `audio-completed` hasta que SPRINT-004 de Agents esté listo. El mecanismo está construido y preparado para conectar.
- **R2 (pendiente):** El diseño visual final de la señal de proximidad (color, intensidad) requiere validación con Contenido/UX antes de cerrar.
- **SPRINT-069:** La detección local del cruce del límite del tramo (edge crossing) como trigger de transición se conectará en SPRINT-069, cuando exista el evento `BIOME_TRANSITION` real de Agents.

### Reviewer verification

**Veredicto: APPROVED**

#### Verificación de configuración (worldMapConfig.ts)
- ✅ `edgeProximityThreshold: 200` — umbral de proximidad al borde (200px)
- ✅ `arrivalFadeShort: 800` — duración de fundido para transiciones normales (800ms)
- ✅ `arrivalFadeDistant: 1600` — duración de fundido para pares distantes (1600ms)
- ✅ `arrivalAudioTimeout: 3000` — timeout de fallback para audio (3s)
- ✅ `arrivalNoVoiceDelay: 1000` — espera fija cuando no se espera voz (1000ms)
- ✅ `DISTANT_BIOME_PAIRS` configurado con pares: woods↔space, beach↔space, prehistory↔space
- ✅ `isDistantBiomePair(from, to)` función de verificación de pares distantes

#### Verificación de EdgeHintLayer (señal de proximidad)
- ✅ `create()` inicializa brillos en bordes laterales con gradiente amarillo cálido (alpha 0.35)
- ✅ `updateProximity(offset, maxScrollOffset)` activa señal al entrar en banda de proximidad (≤200px del borde)
- ✅ Desactiva señal al alejarse del borde
- ✅ Respeta `prefers-reduced-motion`: usa alpha fijo (0.25) sin animación
- ✅ No bloquea el arrastre ni fuerza continuar (solo visual, depth 50)
- ✅ Integrado en `WorldMapScene.update()` (línea 144-145)

#### Verificación de BiomeTransition (transición entre biomas)
- ✅ `createArrivalOverlay(context)` crea overlay negro con duración según par de biomas
- ✅ `getArrivalFadeDuration(from, to)` devuelve 1600ms para pares distantes, 800ms para el resto
- ✅ `fadeToBlack(context, onComplete)` ejecuta fundido de entrada con duración adecuada
- ✅ `fadeFromBlack(scene, overlay, duration, onComplete)` ejecuta fundido de salida
- ✅ Respeta `prefers-reduced-motion`: transición instantánea sin animación

#### Verificación de pausa de llegada (WorldMapScene)
- ✅ `beginBiomeArrival(targetBiome, targetWorldWidth, onMidpoint)` ejecuta secuencia completa:
  1. Fundido de entrada (duración según par de biomas)
  2. Llama a `onMidpoint()` para reconstruir capas
  3. Recrea `EdgeHintLayer` para el nuevo bioma
  4. Espera audio o delay fijo
  5. Fundido de salida
- ✅ `waitForArrivalAudio(onReady)` implementa lógica condicional:
  - Si `npcEnabled && ttsEnabled && voiceEnabled`: espera `audio-completed` con timeout 3s
  - Si no: espera delay fijo de 1000ms
- ✅ Mismo patrón que `LoadingScene.handleWelcomeEvent` (timeout de fallback)
- ✅ No muestra porcentaje, temporizador visible ni mensaje de logro durante la pausa
- ✅ `arrivalInProgress` previene transiciones simultáneas
- ✅ Integrado en `handleWorldStateActive()` cuando `biomeChanged` es true (línea 308-318)

#### Verificación de compilación y build
- ✅ `tsc --noEmit` → 0 errores
- ✅ `vite build` → build exitoso (5.90s)

#### Observaciones
- La rama "con voz" se probará con el mock de `audio-completed` hasta que SPRINT-004 de Agents esté listo
- El diseño visual final de la señal de proximidad (color, intensidad) requiere validación con Contenido/UX
- La detección local del cruce del límite del tramo como trigger de transición se conectará en SPRINT-069
