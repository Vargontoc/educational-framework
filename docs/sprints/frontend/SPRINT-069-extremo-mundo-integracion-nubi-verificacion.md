# SPRINT-069 — Extremo del mundo, integración real de Nubi en la llegada y verificación transversal

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-11
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

### Developer implementation — Evidencias

(Pendiente de implementación)

### Reviewer verification

(Pendiente de revisión)
