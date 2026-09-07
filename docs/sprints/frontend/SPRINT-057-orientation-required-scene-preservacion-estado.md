# SPRINT-057 — OrientationRequiredScene y preservación de estado

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-056 (reconfiguración de viewport y detección de orientación)
- **Impacto estimado:** Implementa la escena Phaser de orientación requerida y el mecanismo de pausa/restauración de escena activa al cambiar orientación, preservando estado de escena y sesión.

## Objetivo

Crear la escena Phaser `OrientationRequiredScene`, implementar el mecanismo de pausa/restauración de escena activa al cambiar orientación, y garantizar que la sesión WebSocket y el estado de juego se preservan durante el cambio.

## Contexto

FEAT-011 requiere que cuando el dispositivo esté en vertical, se muestre un estado de orientación requerida con Nubi y una indicación visual comprensible para girar el dispositivo. Si el cambio a vertical sucede dentro de una escena, al recuperar la orientación horizontal debe restaurarse la misma escena y estado visible, sin reiniciar la sesión ni la carga.

SPRINT-056 ya proporciona:
- Canvas de referencia 1280x720.
- Composable `useGameOrientation` con `isPortrait` reactivo.

Este sprint implementa:
- `OrientationRequiredScene` (escena Phaser).
- Lógica de pausa/restauración en `GameView.vue`.
- Listeners `pause`/`resume` en escenas con lógica temporal (WorldMapScene, RecognitionGameScene).

## Diseño funcional-técnico

### 1. OrientationRequiredScene

**Ubicación:** `framework/frontend/app/src/components/game/OrientationRequiredScene.ts`

**Características:**

| Aspecto | Detalle |
|---------|---------|
| **Key** | `'orientation-required'` |
| **Constructor** | `super({ key: 'orientation-required', active: false })` |
| **Fondo** | Rectangle con color de fondo coherente con el design system (placeholder: `0xf0f4f8`) |
| **Indicación visual** | Placeholder: icono de dispositivo girando (círculo + rectángulo animado con tween de rotación). No depende de texto, color o sonido. |
| **Texto de apoyo** | Placeholder: texto breve, amable, no evaluativo vía `vue-i18n`. Ej: `"Gira tu tablet"` |
| **Animación** | Tween suave de rotación del icono. Respetar `prefers-reduced-motion`: si está activo, mostrar icono estático sin animación. |
| **Interacción** | Ninguna — es una pausa visual, no una pantalla interactiva. |
| **Datos de sesión** | No accede a WebSocket, no modifica sesión, no expone datos del menor. |

### 2. Mecanismo de pausa/restauración

**Flujo:**

```
Cambio a vertical:
  1. Escena activa (loading/base-state/farewell) → scene.pause(key)
  2. scene.start('orientation-required')

Cambio a horizontal:
  1. scene.stop('orientation-required')
  2. scene.resume(keyEscenaAnterior)
```

**Estado a preservar** (en `game.registry`):

| Dato | Dónde se almacena | Vida útil |
|------|-------------------|-----------|
| `previousSceneKey` | registry de Phaser | Hasta restauración |
| `childId`, `sessionId`, `websocket` | registry de Phaser (ya existe) | Sesión completa |
| Preferencias (npcEnabled, ttsEnabled, voiceEnabled) | registry de Phaser (ya existe) | Sesión completa |

### 3. Listeners pause/resume en escenas

**WorldMapScene:**
```typescript
this.events.on('pause', () => {
  // Detener heartbeat interval
  clearInterval(this.registry.get('wsWorldbeat'))
})

this.events.on('resume', () => {
  // Reanudar heartbeat interval
  const heartbeatId = setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(new WorldHeartbeatEvent()))
    }
  }, 1000)
  this.registry.set('wsWorldbeat', heartbeatId)
})
```

**RecognitionGameScene:**
```typescript
this.events.on('pause', () => {
  // Bloquear acciones durante pausa
  this.bloackActions = true
})

this.events.on('resume', () => {
  // Desbloquear acciones al reanudar
  this.bloackActions = false
})
```

**LoadingScene y BaseStateScene:**
- No requieren lógica adicional en pause/resume.
- Sus tweens se gestionan automáticamente por Phaser.

### 4. Accesibilidad: prefers-reduced-motion

```typescript
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

if (!prefersReducedMotion) {
  // Crear tween de rotación
  this.tweens.add({
    targets: icon,
    angle: 360,
    duration: 2000,
    repeat: -1
  })
}
// Si prefers-reduced-motion está activo, icono estático
```

## Contratos y dependencias externas

No se requieren contratos nuevos con backend. Este sprint es puramente frontend.

**Dependencias internas:**

| Dependencia | Estado | Impacto |
|-------------|--------|---------|
| SPRINT-056 | Pendiente | Proporciona canvas 1280x720 y `useGameOrientation`. |
| Phaser 3.x | Instalado | APIs `scene.pause/resume`, eventos `pause`/`resume` disponibles. |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | `scene.pause()` no detiene heartbeats ni timers de escenas | ALTA | Cada escena con lógica temporal escucha eventos `pause`/`resume` de Phaser. |
| R2 | `prefers-reduced-motion` no se respeta en la animación | BAJA | Comprobar `window.matchMedia` antes de crear tweens. |
| R3 | El cambio de orientación rápido produce condiciones de carrera | MEDIA | Usar flag `isTransitioning` en el composable para ignorar eventos durante 150ms. |
| R4 | RecognitionGameScene pierde estado de juego por timeout del backend | MEDIA | El backend gestiona sus propios timeouts. La pausa es transparente para backend. |

---

## Tareas del sprint

### Tarea 57.1: Crear OrientationRequiredScene

**Archivo:** `OrientationRequiredScene.ts` (nuevo)

**Criterios de aceptación:**
- Escena Phaser con key `'orientation-required'`.
- Fondo con color coherente con design system.
- Icono de dispositivo girando (placeholder).
- Texto de apoyo breve y amable vía `vue-i18n`.
- No depende de texto, color o sonido para su comprensión.
- No presenta lenguaje de error, castigo o evaluación.

### Tarea 57.2: Respetar prefers-reduced-motion

**Archivo:** `OrientationRequiredScene.ts`

**Criterios de aceptación:**
- Si `prefers-reduced-motion` está activo, el icono se muestra estático sin tween.
- Si está desactivado, el icono tiene animación suave de rotación.

### Tarea 57.3: Registrar OrientationRequiredScene en GameView

**Archivo:** `GameView.vue`

**Criterios de aceptación:**
- `OrientationRequiredScene` se añade al array de escenas de Phaser.
- Array final: `[LoadingScene, BaseStateScene, FarewellScene, OrientationRequiredScene]`.

### Tarea 57.4: Implementar lógica de pausa/restauración en GameView

**Archivo:** `GameView.vue`

**Criterios de aceptación:**
- Cuando `isPortrait` cambia a `true`, se pausa la escena activa y se lanza `orientation-required`.
- Cuando `isPortrait` cambia a `false`, se detiene `orientation-required` y se reanuda la escena anterior.
- `previousSceneKey` se almacena en registry de Phaser antes de pausar.
- El WebSocket permanece conectado durante el cambio de orientación.

### Tarea 57.5: Añadir listeners pause/resume en WorldMapScene

**Archivo:** `WorldMapScene.ts`

**Criterios de aceptación:**
- En evento `pause`, se detiene el heartbeat interval.
- En evento `resume`, se reanuda el heartbeat interval.
- Los heartbeats se detienen en vertical y se reanudan en horizontal.

### Tarea 57.6: Añadir listeners pause/resume en RecognitionGameScene

**Archivo:** `RecognitionGameScene.ts`

**Criterios de aceptación:**
- En evento `pause`, se bloquean acciones (`bloackActions = true`).
- En evento `resume`, se desbloquean acciones (`bloackActions = false`).

### Tarea 57.7: Verificar LoadingScene, BaseStateScene y FarewellScene

**Archivos:** `LoadingScene.ts`, `BaseStateScene.ts`, `FarewellScene.ts`

**Criterios de aceptación:**
- No requieren lógica adicional en pause/resume.
- Sus tweens se gestionan automáticamente por Phaser.

### Tarea 57.8: Verificación estática

**Criterios de aceptación:**
- `vue-tsc --noEmit` sin errores nuevos en archivos del sprint.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/game/OrientationRequiredScene.ts` | Nuevo |
| `framework/frontend/app/src/views/GameView.vue` | Modificación (registro escena, lógica pausa/restauración) |
| `framework/frontend/app/src/components/game/WorldMapScene.ts` | Modificación (listeners pause/resume) |
| `framework/frontend/app/src/components/game/RecognitionGameScene.ts` | Modificación (listeners pause/resume) |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación (texto de orientación requerida) |

## Estimación

- **Duración:** 2.5 días
- **Complejidad:** Alta
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Con dispositivo en vertical, no se muestra ninguna escena jugable activa; se muestra `OrientationRequiredScene`. *(FEAT-011 AC2)*
2. La indicación visual se comprende sin depender exclusivamente de texto, color o sonido. *(FEAT-011 AC3)*
3. La indicación no presenta lenguaje de error, castigo o evaluación. *(FEAT-011 req4)*
4. Al volver a horizontal, se recupera la misma escena y estado visible anteriores. *(FEAT-011 AC6)*
5. El WebSocket permanece conectado durante el cambio de orientación. *(FEAT-010 req11)*
6. Los heartbeats de WorldMapScene se detienen en vertical y se reanudan en horizontal.
7. `prefers-reduced-motion` desactiva la animación de giro. *(FEAT-011 req11)*
8. `vue-tsc --noEmit` sin errores nuevos.

## Dependencias bloqueantes

- [ ] SPRINT-056 completado (reconfiguración de viewport y detección de orientación).

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Contenido | Proporcionar asset visual de Nubi girando dispositivo (reemplazar placeholder) | Media |
| Contenido | Validar texto i18n de indicación de giro | Media |

## Notas adicionales

Este sprint implementa la escena de orientación requerida y la preservación de estado. La validación cross-viewport se realizará en SPRINT-058.

Los placeholders visuales (icono de giro, texto) deben ser reemplazados por contenido cuando esté disponible. La documentación para contenido se proporciona en SPRINT-058.
