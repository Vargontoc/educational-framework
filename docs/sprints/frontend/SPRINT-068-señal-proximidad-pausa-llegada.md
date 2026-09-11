# SPRINT-068 — Señal de proximidad, pausa de llegada y transición entre biomas distantes

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-11
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

(Pendiente de implementación)

### Reviewer verification

(Pendiente de revisión)
