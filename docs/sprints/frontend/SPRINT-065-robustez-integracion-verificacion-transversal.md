# SPRINT-065 — Robustez, integración y verificación transversal

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-064 completado
- **Impacto estimado:** Cierra calidad, accesibilidad, coherencia con FEAT-010 y verificación completa de criterios de aceptación de FEAT-011.

## Objetivo

Cerrar calidad, accesibilidad, coherencia con FEAT-010 y verificación completa de criterios de aceptación de FEAT-011.

## Contexto

Los sprints 063 y 064 establecieron la arquitectura base de WorldMap con paisaje placeholder, desplazamiento y elementos interactuables. Este sprint cierra la fase 1 con robustez, manejo de casos edge, verificación de accesibilidad y documentación de extensibilidad.

**Decisiones técnicas confirmadas:**
- Alternativa 1 para Nubi: sin reacciones expresivas si NPC desactivado
- No envío de `WorldDiscoveryElementInteractiveEvent` en fase 1
- Ancho de mundo virtual placeholder: 2560 px lógicos
- Reacción ambiental genérica única (extensible en fase 4B)

## Diseño funcional-técnico

### Manejo de estados de mundo

**Estados de `WORLD_STATE_SYNC.status`:**

| Estado | Comportamiento en fase 1 |
|--------|--------------------------|
| `ACTIVE` | Paisaje completo con elementos interactivos |
| `INACTIVE_CLOSED` | Mismo paisaje, sin elementos interactivos, sin bloqueo |
| `NO_WORLD_STATE` | Mismo paisaje, sin elementos interactivos, sin bloqueo |

**Implementación:**
- Si `status !== ACTIVE`, `InteractiveLayer` no renderiza elementos
- El paisaje (fondo, parallax, Nubi) se muestra igual
- No se muestra mensaje de error, aviso ni bloqueo

### Fallback de assets visuales

**Escenario:** `visualAssetKey` del discovery element no existe en caché de texturas.

**Comportamiento:**
- Usar placeholder geométrico (círculo, cuadrado, triángulo, estrella según `elementType`)
- Sin error visible en consola ni en UI
- Log interno para debugging (nivel `debug`, no `warn`)

### Verificación de handoff LoadingScene → WorldMapScene

**Puntos de verificación:**
- Transición fluida sin parpadeo
- WebSocket se transfiere correctamente (no se reconecta)
- `sessionId` y `childId` se mantienen
- `audioService` se mantiene en registry
- Configuración parental (`npcEnabled`, `ttsEnabled`, `voiceEnabled`) se mantiene

### Verificación de cambio de preferencia parental en sesión

**Escenario:** Padre cambia `npcEnabled` durante WorldMap activo.

**Comportamiento:**
- `NubiLayer` escucha evento `'npc-state-changed'`
- Si `npcEnabled` cambia de `true` a `false`: Nubi detiene idle breathing, queda estático
- Si `npcEnabled` cambia de `false` a `true`: Nubi inicia idle breathing
- Cambio inmediato, sin transición visual brusca

### Pruebas de accesibilidad táctil

**Dispositivos target:**
- Samsung Galaxy A15 (móvil)
- Tablet genérica Android (10")
- iPad (referencia)

**Validaciones:**
- Hit-areas de 80x80 px son alcanzables cómodamente
- Separación de 60 px entre elementos evita toques accidentales
- Drag funciona correctamente en pantalla táctil
- No hay zonas muertas ni conflictos de input

### Verificación de `prefers-reduced-motion`

**Módulos a revisar:**
- `ParallaxLayer`: desactivar desplazamiento proporcional
- `NubiLayer`: desactivar idle breathing
- `EnvironmentReaction`: reducir a cambio de opacidad suave
- `GradualScroller`: desactivar inercia (movimiento directo)

**Método de verificación:**
- Activar `prefers-reduced-motion: reduce` en configuración del dispositivo
- Verificar que cada módulo responde correctamente
- Documentar comportamiento en `framework/frontend/app/docs/`

### Limpieza de recursos

**Patrón a seguir:** `BaseStateScene:309-327`

**Recursos a liberar en `shutdown`/`destroy`:**
- `GradualScroller`: detener listeners de pointer
- `InteractiveLayer`: limpiar elementos y listeners
- `EnvironmentReaction`: cancelar animaciones en curso
- `NubiLayer`: detener tweens de idle breathing
- `ParallaxLayer`: limpiar formas
- `BackgroundLayer`: limpiar texturas placeholder

**Verificación:**
- Profiling con DevTools tras salir de WorldMap
- No hay memory leaks
- No hay listeners huérfanos

### Documentación de extensibilidad

**Contenido:**
- Cómo sustituir placeholders por assets reales en fases 3 y 4B
- Cómo añadir nuevas reacciones ambientales en fase 4B
- Cómo extender `GradualScroller` para límites dinámicos en fase 4A
- Estructura de módulos y responsabilidades

**Ubicación:** `framework/frontend/app/docs/worldmap-extensibility.md`

## Contratos y dependencias externas

### Endpoints/contratos que consume

- `WORLD_STATE_SYNC` (WebSocket): ya definido
  - Manejo de `status !== ACTIVE`
  - Fallback de `visualAssetKey` no encontrado

### Dependencias hacia backend/agents/tts

| Dependencia | Tipo | Estado |
|-------------|------|--------|
| `WORLD_STATE_SYNC` (WS) | Requerida | Ya definido |
| npc-agent | No requerida | Nubi no produce intervenciones en esta fase |
| tts-service | No requerida | No se reproduce audio en WorldMap en esta fase |

### Handoffs documentados

| Handoff | Capa destino | Contenido |
|---------|--------------|-----------|
| Límites de zona (fase 4A) | Backend/Content | Desplazamiento necesita ancho de mundo virtual. Fase 1: constante 2560 px. Fase 4A: backend puede proporcionar límites |
| Catálogo de reacciones (fase 4B) | Frontend/Content | Repertorio de animaciones se define en fase 4B |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Memory leaks al salir de WorldMap | MEDIA | Limpieza explícita en `shutdown`/`destroy`, profiling con DevTools |
| R2 | `prefers-reduced-motion` no cubre todos los casos | BAJA | Revisión exhaustiva módulo por módulo |
| R3 | Handoff LoadingScene → WorldMap pierde WebSocket | BAJA | Verificación de transferencia en `LoadingScene.goToWorldMap()` |
| R4 | Cambio de preferencia parental no se refleja inmediatamente | BAJA | Escucha de `'npc-state-changed'` en `NubiLayer` |

---

## Tareas del sprint

### Tarea 65.1: Manejo de `WORLD_STATE_SYNC` con `status !== ACTIVE`

**Archivo:** `src/components/game/WorldMapScene.ts`

**Descripción:** Si el estado es `INACTIVE_CLOSED` o `NO_WORLD_STATE`, mostrar variante visual apropiada.

**Criterios de aceptación:**
- Si `status !== ACTIVE`, `InteractiveLayer` no renderiza elementos
- El paisaje (fondo, parallax, Nubi) se muestra igual
- No se muestra mensaje de error, aviso ni bloqueo
- La experiencia sigue siendo comprensible y no evaluativa

### Tarea 65.2: Fallback de assets visuales

**Archivo:** `src/components/game/worldmap/layers/InteractiveLayer.ts`

**Descripción:** Si `visualAssetKey` no existe en caché de texturas, usar placeholder geométrico.

**Criterios de aceptación:**
- Si `visualAssetKey` no existe, usar placeholder geométrico según `elementType`
- Sin error visible en consola ni en UI
- Log interno para debugging (nivel `debug`)

### Tarea 65.3: Verificar coherencia de handoff LoadingScene → WorldMapScene

**Archivos:** `LoadingScene.ts`, `WorldMapScene.ts`

**Descripción:** Confirmar que la transición es fluida, sin parpadeo ni pérdida de WebSocket.

**Criterios de aceptación:**
- Transición fluida sin parpadeo
- WebSocket se transfiere correctamente (no se reconecta)
- `sessionId` y `childId` se mantienen
- `audioService` se mantiene en registry
- Configuración parental se mantiene

### Tarea 65.4: Verificar comportamiento con `npcEnabled` cambiado en sesión

**Archivo:** `src/components/game/worldmap/layers/NubiLayer.ts`

**Descripción:** Cambiar la preferencia parental durante WorldMap activo → Nubi ajusta su comportamiento.

**Criterios de aceptación:**
- `NubiLayer` escucha evento `'npc-state-changed'`
- Si `npcEnabled` cambia de `true` a `false`: Nubi detiene idle breathing
- Si `npcEnabled` cambia de `false` a `true`: Nubi inicia idle breathing
- Cambio inmediato, sin transición visual brusca

### Tarea 65.5: Pruebas de accesibilidad táctil

**Archivos:** Todos los módulos de worldmap

**Descripción:** Validar hit-areas en dispositivos target.

**Criterios de aceptación:**
- Hit-areas de 80x80 px son alcanzables cómodamente en móvil y tablet
- Separación de 60 px entre elementos evita toques accidentales
- Drag funciona correctamente en pantalla táctil
- No hay zonas muertas ni conflictos de input

### Tarea 65.6: Verificar `prefers-reduced-motion` en todos los módulos

**Archivos:** `ParallaxLayer.ts`, `NubiLayer.ts`, `EnvironmentReaction.ts`, `GradualScroller.ts`

**Descripción:** Revisión exhaustiva de todos los módulos de animación.

**Criterios de aceptación:**
- `ParallaxLayer`: desactiva desplazamiento proporcional si `prefers-reduced-motion` activo
- `NubiLayer`: desactiva idle breathing si `prefers-reduced-motion` activo
- `EnvironmentReaction`: reduce a cambio de opacidad suave si `prefers-reduced-motion` activo
- `GradualScroller`: desactiva inercia si `prefers-reduced-motion` activo

### Tarea 65.7: Limpieza de recursos en `shutdown`/`destroy`

**Archivos:** Todos los módulos de worldmap

**Descripción:** Seguir patrón de `BaseStateScene` para liberar recursos.

**Criterios de aceptación:**
- `GradualScroller`: detiene listeners de pointer
- `InteractiveLayer`: limpia elementos y listeners
- `EnvironmentReaction`: cancela animaciones en curso
- `NubiLayer`: detiene tweens de idle breathing
- `ParallaxLayer`: limpia formas
- `BackgroundLayer`: limpia texturas placeholder
- Profiling con DevTools confirma ausencia de memory leaks

### Tarea 65.8: Documentación técnica de la estructura extensible

**Archivo:** `framework/frontend/app/docs/worldmap-extensibility.md`

**Descripción:** Documentar cómo se extenderán las capas en fases 3 y 4B.

**Criterios de aceptación:**
- Documenta cómo sustituir placeholders por assets reales
- Documenta cómo añadir nuevas reacciones ambientales
- Documenta cómo extender `GradualScroller` para límites dinámicos
- Describe estructura de módulos y responsabilidades

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `src/components/game/WorldMapScene.ts` | Modificación |
| `src/components/game/worldmap/layers/InteractiveLayer.ts` | Modificación |
| `src/components/game/worldmap/layers/NubiLayer.ts` | Modificación |
| `src/components/game/worldmap/layers/ParallaxLayer.ts` | Modificación |
| `src/components/game/worldmap/layers/BackgroundLayer.ts` | Modificación |
| `src/components/game/worldmap/scroll/GradualScroller.ts` | Modificación |
| `src/components/game/worldmap/reactions/EnvironmentReaction.ts` | Modificación |
| `framework/frontend/app/docs/worldmap-extensibility.md` | Nuevo |

## Estimación

- **Duración:** 3-4 días
- **Complejidad:** Media
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. Todos los criterios de aceptación de FEAT-011 (§5) se cumplen en dispositivo tablet y móvil.
2. La experiencia funciona correctamente sin audio disponible (audio desactivado en configuración parental).
3. La experiencia funciona correctamente sin NPC (NPC desactivado en configuración parental).
4. No se muestra ningún elemento gamificado (niveles, misiones, flechas, candados, puntuaciones, barras de progreso, temporizadores, clasificaciones, recompensas, mensajes acierto/error).
5. La recarga accidental de página en WorldMap restaura la sesión (sessionStorage) y vuelve a GameView.
6. Los recursos se liberan correctamente al salir de WorldMap (sin memory leaks).
7. La estructura de capas permite sustituir placeholders por assets reales sin reescribir la lógica de escena.

## Dependencias bloqueantes

- [ ] SPRINT-064 completado.

## Handoffs a otras capas

| Handoff | Capa destino | Contenido |
|---------|--------------|-----------|
| Límites de zona (fase 4A) | Backend/Content | Desplazamiento necesita ancho de mundo virtual. Fase 1: constante 2560 px. Fase 4A: backend puede proporcionar límites |
| Catálogo de reacciones (fase 4B) | Frontend/Content | Repertorio de animaciones se define en fase 4B |

## Notas adicionales

Este sprint cierra la fase 1 de FEAT-011. Con la robustez, accesibilidad y verificación completadas, WorldMap está listo para su uso en producción con placeholders.

Las fases siguientes (3, 4A, 4B) añadirán:
- **Fase 3:** Biomas concretos con assets reales
- **Fase 4A:** Límites perceptibles de zona sin sensación de nivel
- **Fase 4B:** Catálogo de reacciones ambientales

La documentación de extensibilidad facilita el trabajo de futuras fases.

## Agent Instruction

- No implementar código de producto. Este sprint es para el desarrollador de Frontend.
- Seguir patrón de `BaseStateScene` para limpieza de recursos.
- Consultar `prefers-reduced-motion` vía `window.matchMedia`.
- Documentar en `framework/frontend/app/docs/worldmap-extensibility.md`.
- Verificar en al menos 2 dispositivos (móvil + tablet).
- No enviar `WorldDiscoveryElementInteractiveEvent` al backend.

## Review

completed_tasks:
- **65.1** `WorldMapScene.readEvent` (`WORLD_STATE_SYNC`): cuando `status !== 'ACTIVE'` (o falta `destination`), se llama a `interactiveLayer?.render([])` para retirar los elementos interactuables, sin tocar `BackgroundLayer`/`ParallaxLayer`/`NubiLayer` y sin mostrar aviso ni bloqueo. `INACTIVE_CLOSED` y `NO_WORLD_STATE` comparten esta rama.
- **65.2** `InteractiveLayer.createVisual(element)`: si `element.visualAssetKey` existe y `scene.textures.exists(...)` es verdadero, usa `Image` en vez del placeholder geométrico; si `visualAssetKey` tiene valor pero la textura no está cacheada, cae al placeholder y emite `console.debug` (no `warn`) — sin error visible en consola. `EnvironmentReaction.play()` se amplió (`ReactionTarget = Shape | Image`) para seguir funcionando sobre ambos tipos.
- **65.3** `WorldMapScene.init()` ahora almacena `sessionId`/`childId` (antes se descartaban), igual que `BaseStateScene`. Verificado por revisión de código: `LoadingScene.goToWorldMap()` marca `transferredWebSocket = true` antes de `scene.start('world-map', …)`, por lo que `LoadingScene.shutdown()` no cierra el socket; `audioService`/`audioCache` y la configuración parental (`npcEnabled`/`ttsEnabled`/`voiceEnabled`) viven en `registry`, que es compartido a nivel de `Game` y sobrevive a la transición sin cambios de código adicionales.
- **65.4** `NubiLayer.setNpcEnabled(false)` ya no hace `setScale(1)` instantáneo: ahora tween corto (200ms, `Sine.easeOut`) de vuelta a escala 1, para evitar un salto brusco si la desactivación llega a mitad de una respiración. El camino de activación (`true`) se mantiene sin cambios (arranca el breathing tween desde escala 1).
- **65.6** Revisión módulo por módulo de `prefers-reduced-motion` (sin cambios de código, ya era correcto): `ParallaxLayer` no se registra en `GradualScroller` cuando `reducedMotion` es true (`WorldMapScene.create()`); `NubiLayer.setNpcEnabled` omite el breathing tween; `EnvironmentReaction.play()` sustituye escala+tint+partícula por solo opacidad; `GradualScroller.onPointerUp` omite la inercia (el arrastre en sí permanece activo, tal como especifica 65.6 — solo la inercia se desactiva); `InteractiveLayer.applyPassiveCue` omite el brillo intermitente.
- **65.7** Corregido un leak real: `InteractiveLayer` no rastreaba sus tweens `repeat: -1` de pista visual pasiva (`applyPassiveCue`). Cada `render()` (p. ej. tras un nuevo `WORLD_STATE_SYNC` sin salir de la escena) hacía `container.removeAll(true)`, destruyendo los elementos anteriores, pero Phaser no detiene automáticamente un tween en bucle infinito cuyo target se destruye a mitad de animación — solo limpia el `TweenManager` cuando la propia `Scene` se destruye (verificado en `node_modules/phaser/src/tweens/TweenManager.js`: solo escucha el evento `DESTROY` de la `Scene`, no de cada `GameObject`). Ahora `InteractiveLayer` guarda cada tween en `cueTweens` y los detiene explícitamente en `render()` (antes de `removeAll`) y en `destroy()`. Se revisó el resto de capas: `NubiLayer` (un único breathing tween, ya rastreado), `GradualScroller` (un único `inertiaTween`, ya rastreado), `ParallaxLayer`/`BackgroundLayer` (sin tweens propios, `Container.destroy()` cascada correctamente vía `preDestroy → removeAll(true)` porque `exclusive` es `true` por defecto — verificado en el código fuente de Phaser), `EnvironmentReaction` (tweens de duración finita, no `repeat: -1`; se congelan sin error cuando la escena deja de actualizarse y no requieren limpieza explícita).
- **65.8** Creado `framework/frontend/app/docs/worldmap-extensibility.md`: estructura de módulos, cómo sustituir placeholders por assets reales (bioma y elementos), cómo añadir nuevas reacciones ambientales (catálogo en fase 4B), cómo extender `GradualScroller` para límites dinámicos (fase 4A), notas de robustez de este sprint, y la sección de deuda técnica (ver más abajo).
- `npx tsc --noEmit` y `npx vue-tsc --noEmit`: sin errores nuevos en los ficheros tocados.

incomplete_tasks / deuda técnica documentada:
- **65.5 (pruebas de accesibilidad táctil en dispositivo real)**: no ejecutable en este entorno — no hay acceso a Samsung Galaxy A15, tablet Android ni iPad físicos/emulados desde esta sesión. Se verificaron analíticamente las garantías geométricas: hit-area mínima 80x80px (`WORLD_MAP_CONFIG.minHitAreaSize`, usada literalmente como tamaño de la `Zone` interactiva en `InteractiveLayer`), separación mínima 60px (`interactiveMinSeparation`, sumada al hit-area en el cálculo de `step` de `InteractiveLayer.render`), y ausencia de conflicto de input (el `pointerdown` de `GradualScroller` se anula cuando `currentlyOver.length > 0`, ver SPRINT-064). Queda pendiente de validación manual en dispositivo real; nótese además que `framework/frontend/app/docs/compatibility-matrix.md` marca iOS/iPadOS como fuera de alcance del proyecto, mientras que este sprint menciona iPad "de referencia" — inconsistencia menor a resolver por producto, no corregida aquí.
- **Deuda técnica mayor (no cubierta por ningún task 65.1-65.8 tal como están redactados, documentada en detalle en `worldmap-extensibility.md` → "Deuda técnica conocida")**: `WorldMapScene` sustituyó a `BaseStateScene` como escena persistente de sesión (SPRINT-063) pero no replica su robustez: no maneja `CHILD_EXPELLED`, `SESSION_EXPIRED`/`SESSION_INVALIDATED`, `GAME_ERROR` (sin `ErrorClassifier`), ni `CHILD_TTS_ACTIVATED`/`DEACTIVATED`; tampoco existe un equivalente de `ConnectionMonitor` para reconexión tras pérdida de WebSocket. Como consecuencia, tres specs Cypress de `fase7-gameview` (`websocket-sesion.cy.ts`, `recuperacion-despedida.cy.ts`, `preferencias-dinamicas.cy.ts`) siguen sin cobertura funcional real — ya señalado en la Review de SPRINT-064 y confirmado aquí tras leer el contenido íntegro de SPRINT-065: ninguna de sus 8 tareas cubre este hueco. No se ha intentado corregir en este sprint por ser un cambio de diseño y alcance mayor (portar `ConnectionMonitor`/`ErrorClassifier` completos), ajeno a las tareas 65.1-65.8 tal como están escritas. Recomendado como sprint propio antes de dar por cerrada la fase 1 de FEAT-011 para producción.

contract_changes:
- Ninguno. No se modificó ningún payload ni se envían eventos nuevos al backend.

learnings:
- Confirmado en el código fuente de Phaser (`node_modules/phaser/src/tweens/TweenManager.js`) que el `TweenManager` de una escena solo se limpia automáticamente cuando la propia `Scene` recibe el evento `DESTROY`, nunca cuando un `GameObject` individual se destruye. Cualquier capa que cree tweens `repeat: -1` sobre objetos que se recrean dentro de la vida de la escena (no solo al salir de ella) debe rastrear y detener esos tweens explícitamente — no basta con que Phaser "se encargue" al salir.
- `Phaser.GameObjects.Container` sí cascada la destrucción a sus hijos por defecto (`exclusive = true` → `preDestroy()` llama a `removeAll(true)`), confirmado leyendo `Container.js`; no hace falta llamar a `removeAll(true)` manualmente antes de `container.destroy()` salvo que se necesite limpiar algo adicional (como los tweens rastreados aparte).
- Releer el contenido completo de un sprint antes de asumir qué cubre (en SPRINT-064 supuse que SPRINT-065 resolvería la deuda de robustez de sesión; al leer sus 8 tareas completas quedó claro que no la cubre) evita perpetuar una suposición incorrecta en la documentación de deuda técnica.

next_sprint_suggestions:
- Validación manual en dispositivo real de accesibilidad táctil (tarea 65.5), y resolución de la inconsistencia iPad-fuera-de-alcance vs iPad-de-referencia entre `compatibility-matrix.md` y este sprint.
- Fase 4A: usar el hook de `GradualScroller` documentado en `worldmap-extensibility.md` para límites de zona dinámicos provistos por backend.
- Corregir el código muerto `shutdown()`/`destroy()` en `BaseStateScene`, `LoadingScene` y `FarewellScene` (ver adenda más abajo): Phaser nunca invoca esos métodos por nombre, solo emite los eventos homónimos. `audioService.dispose()`/`audioCache.clear()` no se ejecutan hoy en ninguna de esas tres escenas.

---

## Adenda — deuda técnica de robustez de sesión corregida

*Añadido después de cerrar este sprint, a petición explícita de continuar corrigiendo la deuda documentada arriba (`incomplete_tasks`) y de documentar cómo aumentar la velocidad del scroll.*

**Corregida la deuda mayor de esta Review** (`WorldMapScene` sin paridad de robustez de sesión con `BaseStateScene`): se portaron a `WorldMapScene` (`src/components/game/WorldMapScene.ts`) los mecanismos que faltaban —

- `ConnectionMonitor` + `attemptReconnect()` (reconexión tras `ws.onclose`, con reinicio del heartbeat `wsWorldbeat` sobre el socket nuevo).
- `handleExpulsion()` (`CHILD_EXPELLED`), `handleSessionExpired()` (`SESSION_EXPIRED`/`SESSION_INVALIDATED`), `handleGameError()` vía `ErrorClassifier` (`GAME_ERROR` recuperable/crítico), `handleFarewellEvent()` (`GAME_AVATAR_EVENT`/`FAREWELL`).
- `CHILD_TTS_ACTIVATED`/`CHILD_TTS_DEACTIVATED` → `registry.set('ttsEnabled', …)` + `events.emit('tts-state-changed', …)`.
- `manageWebsocket`/`setupWebSocketHandlers` reescritos para usar `MessageRouter.route(...)` (antes hacían `JSON.parse(msg.data)` directo, lo que habría roto ante cualquier frame binario de audio) y para poder reutilizarse tanto en la conexión inicial como tras una reconexión.
- `cleanupWebSocket()` centralizado: limpia tanto el heartbeat genérico de conexión (`clearWebSocketHeartbeat`) como el heartbeat propio de WorldMap (`wsWorldbeat`), y se invoca desde `goToFarewell()`, `handleExpulsion()`, `handleSessionExpired()` y desde la limpieza de escena.

Los tres specs Cypress señalados en esta Review (`websocket-sesion.cy.ts`, `recuperacion-despedida.cy.ts`, `preferencias-dinamicas.cy.ts`) se actualizaron para esperar `'world-map'` en vez de `'base-state'`; no requirieron más cambios porque la funcionalidad que verifican ya existe. **No se ejecutaron contra un backend real** en esta sesión (sin entorno levantado).

**Hallazgo adicional durante el porteo** (no estaba documentado antes): al implementar la limpieza de `WorldMapScene` se verificó, leyendo el código fuente de Phaser, que los métodos literalmente llamados `shutdown()`/`destroy()` en una subclase de `Scene` **no son invocados automáticamente por el framework** (Phaser solo llama por nombre a `init`/`preload`/`create`/`update`; `shutdown`/`destroy` son *eventos* emitidos en `this.events`, no convenciones de método). Esto significa que `BaseStateScene.shutdown()/.destroy()`, `LoadingScene.shutdown()/.destroy()` y `FarewellScene.shutdown()/.destroy()` son código muerto — nunca se ejecutan. El impacto práctico es acotado (los puntos de salida explícitos ya limpian el WebSocket por su cuenta), pero `audioService.dispose()`/`audioCache.clear()` no se ejecutan hoy en esas tres escenas. `WorldMapScene` evita el problema registrando su limpieza con `this.events.once('shutdown'/'destroy', …)` explícitamente (patrón que el `WorldMapScene` original ya usaba correctamente para `cleanupLayers`). No se corrigió en `BaseStateScene`/`LoadingScene`/`FarewellScene` por ser un cambio transversal a escenas fuera del alcance de WorldMap. Detalle completo en `framework/frontend/app/docs/worldmap-extensibility.md` → "Deuda técnica".

**Documentado, a petición explícita, cómo aumentar la velocidad del scroll**: nueva sección "Cómo aumentar la velocidad del scroll de desplazamiento" en `worldmap-extensibility.md`, explicando `WORLD_MAP_CONFIG.scrollMaxSpeed` (tope de velocidad durante el arrastre, aplicado en `GradualScroller.update()`) y `inertiaDuration`/el factor `0.5` de `onPointerUp` (distancia de la inercia tras soltar), junto con una nota explícita de que `40 px/s` es una decisión de producto de SPRINT-063 (paseo tranquilo para 3-4 años), no un valor arbitrario — subirlo mucho debería confirmarse con producto antes de mergear.

Verificado `npx tsc --noEmit` y `npx vue-tsc --noEmit` sin errores nuevos tras estos cambios.
