# WorldMap — Estructura extensible

Documenta cómo está organizado `WorldMapScene` y sus módulos (`src/components/game/worldmap/`) tras SPRINT-063/064/065, y cómo extenderlos en las fases 3, 4A y 4B de FEAT-011 sin reescribir la lógica de escena.

## Estructura de módulos y responsabilidades

```
src/components/game/
├── WorldMapScene.ts              Orquestador: ciclo de vida, WebSocket, eventos, composición de capas
└── worldmap/
    ├── config/
    │   └── worldMapConfig.ts     Constantes (velocidades, límites, tamaños, duraciones)
    ├── layers/
    │   ├── BackgroundLayer.ts    Fondo de bioma (depth 0)
    │   ├── ParallaxLayer.ts      Formas de lejanía con desplazamiento proporcional (depth 1)
    │   ├── InteractiveLayer.ts   Elementos interactuables del `WORLD_STATE_SYNC` (depth 2)
    │   └── NubiLayer.ts          Presencia visual de Nubi (depth 3)
    ├── scroll/
    │   └── GradualScroller.ts    Desplazamiento por drag con inercia y límites
    └── reactions/
        └── EnvironmentReaction.ts Reacción ambiental reutilizable al tocar un elemento
```

Cada capa es una clase independiente que recibe la `Scene` en su constructor, expone `create()`/`render()` para construir sus Game Objects y `destroy()` para liberarlos. `WorldMapScene` solo compone estas piezas: no conoce el detalle interno de cada capa, y ninguna capa conoce a las demás directamente (la coordinación pasa siempre por `WorldMapScene` o por `GradualScroller.registerLayer()`).

## Cómo sustituir placeholders por assets reales (fases 3 y 4B)

**`BackgroundLayer`**: `buildFrom(biome?)` ya comprueba `this.scene.textures.exists('biome-<bioma>-bg')`; si existe, usa un `Image` en vez del degradado `Graphics`. Para activar assets reales de bioma solo hace falta:
1. Precargar la textura con esa clave (`biome-<bioma>-bg`, minúsculas) en el `preload()` de la escena o en el manifest de assets.
2. No tocar `BackgroundLayer` ni `WorldMapScene`: el fallback al degradado desaparece automáticamente en cuanto la textura existe.

**`InteractiveLayer`**: `createVisual(element)` ya comprueba `element.visualAssetKey` contra `this.scene.textures.exists(...)`; si la textura está cacheada, crea un `Phaser.GameObjects.Image` en vez del placeholder geométrico (círculo/cuadrado/triángulo/estrella). Si no existe, hace `console.debug` (no `warn`, para no ensuciar la consola en producción con contenido aún no ilustrado) y cae al placeholder. Para activar assets reales de elementos:
1. Precargar las texturas referenciadas por `visualAssetKey` de los `discoveryElements` que envíe el backend.
2. No tocar `InteractiveLayer`: el placeholder geométrico se sustituye automáticamente elemento a elemento, según qué texturas estén cacheadas.
3. `EnvironmentReaction.play()` ya acepta tanto `Phaser.GameObjects.Shape` como `Phaser.GameObjects.Image` (tipo `ReactionTarget`); el flash de tinte se omite automáticamente sobre `Image` (no tiene `fillColor`/`setFillStyle`) mientras que la oscilación de escala y la partícula funcionan igual para ambos. Si en el futuro se quiere un efecto de "flash" también sobre imágenes reales, la forma más simple es usar `setTintFill`/`clearTint` de `Image` en un método `playImageFlash` paralelo a `playTintFlash`, sin cambiar la firma pública de `play()`.

## Cómo añadir nuevas reacciones ambientales (fase 4B)

`EnvironmentReaction` expone una única interfaz pública: `play(target: ReactionTarget)`. Para pasar de "reacción única" a un catálogo:

1. Definir el repertorio como una lista de funciones o estrategias (por ejemplo, `type ReactionVariant = (scene: Scene, target: ReactionTarget) => void`) y mover la implementación actual (`playScalePulse` + `playTintFlash` + `spawnFadingParticle`) a una de esas variantes con nombre (`genericPulse`).
2. `InteractiveLayer` decide qué variante corresponde a cada elemento (por `elementType`, por una futura propiedad del contrato, o por selección aleatoria dentro de un subconjunto) y se la pasa a `play()`, o bien `EnvironmentReaction` recibe un selector en el constructor.
3. Mantener siempre la rama de `prefers-reduced-motion` (`playReducedMotion`) como comportamiento por defecto compartido por todas las variantes: ninguna reacción nueva debe saltarse la comprobación de `window.matchMedia('(prefers-reduced-motion: reduce)')`.
4. No hace falta tocar `InteractiveLayer.createElement()` más allá de la línea que invoca `play()`: la interfaz `play(target)` ya es la única superficie de acoplamiento entre capas.

## Cómo extender `GradualScroller` para límites dinámicos (fase 4A)

Hoy `GradualScroller` usa los límites fijos de `WORLD_MAP_CONFIG` (`worldWidth: 2560`, `maxScrollOffset: 1280`). Para que el backend/contenido definan límites de zona reales:

1. Añadir un método `setBounds(maxScrollOffset: number)` (o un segundo parámetro en el constructor) que actualice el límite usado por `clamp(...)` en `onPointerMove` y en el cálculo de `target` de la inercia (`onPointerUp`). Mantener `WORLD_MAP_CONFIG.maxScrollOffset` como valor por defecto/fallback si el backend no informa límites.
2. `WorldMapScene` leería el límite de `WORLD_STATE_SYNC.destination` (cuando el contrato lo incluya, ver FEAT-010 §3 "límites funcionales de exploración") y llamaría a `scroller.setBounds(...)` tras recibir el evento, antes o después de `interactiveLayer.render(...)`.
3. La señal visual suave de "hay más paisaje por descubrir" (FEAT-010, escenario "niño que alcanza una zona aún no visible") se puede implementar como una capa adicional (`layers/EdgeHintLayer.ts`, mismo patrón `create()`/`destroy()`) que `GradualScroller` activa cuando `offset` se acerca al límite, sin modificar la lógica de arrastre existente.
4. `GradualScroller.registerLayer(container, factor)` ya soporta cualquier número de capas con distinto factor de paralaje; una futura capa de "límite de zona" se registraría igual que `ParallaxLayer`/`InteractiveLayer`.

## Notas de robustez (SPRINT-065)

- **`WORLD_STATE_SYNC` con `status !== ACTIVE`** (`INACTIVE_CLOSED` / `NO_WORLD_STATE`): `WorldMapScene.readEvent` llama a `interactiveLayer.render([])` para retirar los elementos interactuables sin tocar fondo, parallax ni Nubi, y sin mostrar aviso ni bloqueo.
- **Tweens en bucle infinito (`repeat: -1`)**: tanto `NubiLayer` (idle breathing) como `InteractiveLayer` (pista visual pasiva por `interactionCueType`) los rastrean en un campo propio y los detienen explícitamente antes de destruir sus Game Objects. Phaser no detiene automáticamente un tween cuando su target se destruye a mitad de animación (solo limpia el `TweenManager` completo cuando la propia `Scene` se destruye) — cualquier capa nueva con animaciones `repeat: -1` debe seguir el mismo patrón: guardar la referencia al `Tween` y llamar a `.stop()` en su `destroy()` (y antes de volver a crear el mismo tipo de tween, como hace `InteractiveLayer.render()` en cada re-sincronización).
- **Cambio de preferencia parental en sesión**: `WorldMapScene` traduce `CHILD_AGENT_ACTIVATED`/`CHILD_AGENT_DEACTIVATED` a `registry.set('npcEnabled', …)` + `this.events.emit('npc-state-changed', …)`. `NubiLayer` escucha ese evento local de la escena (no el registry global) y hace una transición corta (200ms) de vuelta a escala 1 al desactivar, en vez de un salto instantáneo, para evitar un "parpadeo" de tamaño si el cambio llega a mitad de una respiración.
- **`sessionId`/`childId`**: `WorldMapScene.init()` los recibe y almacena (igual que hacía `BaseStateScene`), aunque en fase 1 no se usan todavía para reconexión — quedan disponibles para cuando se implemente el equivalente de `ConnectionMonitor` (ver deuda técnica más abajo).
- **Tiempo hasta que aparecen los elementos interactuables al entrar a WorldMap**: el backend solo envía `WORLD_STATE_SYNC` (con `destination.discoveryElements`) como respuesta directa a un mensaje `world_heartbeat` del cliente (`GameWebSocketHandler.handleWorldHeartbeat`, backend) — no lo envía de forma proactiva al autenticar ni en ningún otro punto. Antes, `WorldMapScene.startWorldHeartbeat()` solo arrancaba un `setInterval(…, 1000)`, así que el primer `world_heartbeat` (y por tanto el primer `WORLD_STATE_SYNC`) tardaba hasta 1 segundo en salir tras crear la escena, dejando el mapa visiblemente vacío (fondo/Nubi sin elementos) durante ese primer segundo. Ahora `startWorldHeartbeat()` envía un `world_heartbeat` inmediato antes de arrancar el intervalo, tanto en la conexión inicial como al `resume` (volver de `orientation-required`) y tras una reconexión (`attemptReconnect`) — en los tres casos es donde se abre o retoma la ventana en la que el mapa podría verse sin elementos.
- **Aparición de elementos interactuables**: se probó una animación de entrada (fundido + escala) en `InteractiveLayer`, pero interfería con la reacción de toque (`EnvironmentReaction.play()` mataba/pisaba el tween de entrada de forma visible al tocar el elemento) y se revirtió. Los elementos aparecen con su aspecto final directamente en cuanto `render()` los crea; solo la pista visual pasiva (`applyPassiveCue`, si `interactionCueType` tiene valor) sigue animándose, igual que en SPRINT-064. Si se retoma esta idea en el futuro, evitar que la animación de entrada y `EnvironmentReaction.play()` compitan por el mismo tween del `visual` (por ejemplo, animando un elemento contenedor aparte, o dejando que `EnvironmentReaction` espere a que la entrada termine antes de reaccionar).

## Cómo aumentar la velocidad del scroll de desplazamiento

El desplazamiento del paisaje lo controla exclusivamente `GradualScroller` (`src/components/game/worldmap/scroll/GradualScroller.ts`), usando dos constantes de `WORLD_MAP_CONFIG` (`src/components/game/worldmap/config/worldMapConfig.ts`):

1. **Velocidad durante el arrastre — `scrollMaxSpeed`** (hoy `40`, en píxeles lógicos por segundo). Es un tope, no una velocidad fija: mientras el dedo/ratón está pulsado, `GradualScroller.update(deltaMs)` calcula `maxStep = scrollMaxSpeed * (deltaMs / 1000)` y mueve el `offset` hacia la posición del puntero como máximo ese paso por frame (`clamp(diff, -maxStep, maxStep)`), **nunca** de golpe a la posición exacta del dedo. Subir `scrollMaxSpeed` (p. ej. a `80` o `120`) hace que el paisaje siga al dedo con menos retraso perceptible.
2. **Distancia/duración de la inercia tras soltar — `inertiaDuration`** (hoy `500` ms) y el factor `0.5` fijo en `GradualScroller.onPointerUp()` (`distance = velocity * (inertiaDuration / 1000) * 0.5`). Aumentar `inertiaDuration`, o subir ese factor `0.5` a algo más alto, hace que el paisaje "deslice" más lejos y durante más tiempo después de soltar, para una sensación más fluida/veloz al arrastrar rápido.

**Para aumentar la velocidad de scroll**, el cambio mínimo es editar `WORLD_MAP_CONFIG.scrollMaxSpeed` en `worldMapConfig.ts` — ningún otro módulo necesita tocarse, ya que todas las capas leen los límites/velocidades desde esa única fuente. Si además se quiere una inercia más larga, subir también `inertiaDuration` (y opcionalmente extraer el factor `0.5` de `onPointerUp` a una constante de configuración, hoy está inline).

**Importante — no es un valor arbitrario:** `40 px/s` fue una decisión de producto explícita de SPRINT-063 (ver "Decisiones técnicas confirmadas" en `docs/sprints/frontend/SPRINT-063-...md`), pensada para mantener el paseo "tranquilo y acotado" para niños de 3-4 años (FEAT-011 §2, riesgo R3: "El desplazamiento por drag confunde al niño"). Subir mucho esta velocidad cambia la sensación de la experiencia de un paseo sereno a un scroll rápido tipo videojuego, lo cual contradice el objetivo funcional del sprint. Cualquier cambio a `scrollMaxSpeed` más allá de un ajuste fino debería confirmarse con producto/contenido antes de mergear, no decidirse solo a nivel técnico.

## Deuda técnica

### Resuelta: paridad de robustez de sesión en `WorldMapScene`

La Review original de SPRINT-065 documentaba que `WorldMapScene` no replicaba la robustez de sesión de `BaseStateScene` (sin manejo de `CHILD_EXPELLED`, `SESSION_EXPIRED`/`SESSION_INVALIDATED`, `GAME_ERROR`, `CHILD_TTS_ACTIVATED`/`DEACTIVATED`, ni reconexión tras pérdida de WebSocket). Esto se corrigió después de cerrar SPRINT-065, portando a `WorldMapScene` los mismos mecanismos de `BaseStateScene`:

- `ConnectionMonitor` (mismo patrón: `onConnectionLost` → `attemptReconnect()`, `onRecoveryFailed` → `goToFarewell()`).
- `attemptReconnect()` usando `connectWebSocket(sessionId)` (igual que `BaseStateScene`), reinstalando handlers y reiniciando el heartbeat propio de WorldMap (`wsWorldbeat`) sobre el nuevo socket.
- `handleExpulsion()`, `handleSessionExpired()`, `handleGameError()` (vía `ErrorClassifier`), `handleFarewellEvent()` (evento `GAME_AVATAR_EVENT`/`FAREWELL`) y el manejo de `CHILD_TTS_ACTIVATED`/`DEACTIVATED` (`registry.set('ttsEnabled', …)` + `events.emit('tts-state-changed', …)`).
- `goToFarewell()` / `cleanupWebSocket()`, que ahora también limpian el heartbeat `wsWorldbeat` además del heartbeat genérico de conexión (`clearWebSocketHeartbeat`).

Los tres specs Cypress afectados (`websocket-sesion.cy.ts`, `recuperacion-despedida.cy.ts`, `preferencias-dinamicas.cy.ts`) se actualizaron para esperar `activeScene === 'world-map'` en vez de `'base-state'`; no hicieron falta más cambios porque la funcionalidad que verifican ya existe ahora en `WorldMapScene`. **No se ejecutaron contra un backend real en esta sesión** (sin entorno levantado) — quedan alineados con el comportamiento porteado, a confirmar en una ejecución real.

### Nueva, descubierta al portar la robustez: `shutdown()`/`destroy()` como métodos nunca se invocan

Al portar la limpieza de `BaseStateScene` se comprobó (leyendo el código fuente de Phaser en `node_modules/phaser/src/scene/SceneManager.js` y `Systems.js`) que Phaser **no** llama automáticamente a métodos de instancia llamados `shutdown()`/`destroy()` definidos en una subclase de `Scene`: solo invoca por nombre `init`, `preload`, `create` y `update` (`scene.preload.call(scene)`, `scene.create.call(scene, …)`, etc. en `SceneManager.js`). Lo que sí existen son los **eventos** `'shutdown'`/`'destroy'` emitidos en `this.events` (`Systems.shutdown()` hace `events.emit(Events.SHUTDOWN, …)`); cualquier limpieza debe registrarse explícitamente con `this.events.on(...)`/`.once(...)`, tal como ya hacía el `WorldMapScene` original (`cleanupLayers`) y como hace ahora `handleSceneTeardown` en este sprint.

Esto significa que **`BaseStateScene.shutdown()`/`.destroy()`, `LoadingScene.shutdown()`/`.destroy()` y `FarewellScene.shutdown()`/`.destroy()` nunca se ejecutan** — son código muerto. En la práctica el impacto es acotado porque los puntos de salida explícitos (`goToFarewell()`, `handleExpulsion()`, `goToWorldMap()` con `transferredWebSocket`) ya llaman a la limpieza de WebSocket directamente sin depender de esos métodos; lo que sí queda sin ejecutar de forma fiable es `audioService.dispose()`/`audioCache.clear()` en esas tres escenas. No se ha corregido en esta sesión por ser un cambio transversal a tres escenas ajenas a WorldMap (y `BaseStateScene` en concreto ya es inalcanzable en el flujo actual, ver nota en la Review de SPRINT-063). Recomendado: o bien reemplazar esos métodos por registro explícito de eventos (mismo patrón que `WorldMapScene`), o bien eliminar el código muerto si se decide retirar `BaseStateScene`.
