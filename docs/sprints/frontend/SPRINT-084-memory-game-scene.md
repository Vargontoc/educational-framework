# SPRINT-084: MemoryGameScene - Tablero y Volteo de Cartas

**Estado:** implemented

## Objetivo
Implementar la escena del minijuego de memoria con tablero de cartas, animaciones de volteo y lógica de parejas.

## Contexto
MemoryGameScene es una nueva escena que presenta al niño un tablero de cartas boca abajo. El niño debe encontrar parejas de cartas idénticas volteándolas de dos en dos.

**Escalera de dificultad:**
- **EASY**: 2×2 (4 cartas, 2 parejas), 2s de visibilidad
- **MEDIUM**: 2×3 (6 cartas, 3 parejas), 1.5s de visibilidad
- **HARD**: 2×4 (8 cartas, 4 parejas), 1s de visibilidad

## Tareas

### Modelo de Datos
- [x] Crear interfaz `MemoryCard` en `game/GameEvent.ts`:
  ```typescript
  export class MemoryCard {
      cardId: string = ''
      elementId: string = ''
      faceUp: boolean = false
      matched: boolean = false
      row: number = 0
      column: number = 0
  }
  ```
- [x] Crear clase `MemoryState` en `game/GameEvent.ts`:
  ```typescript
  export class MemoryState {
      cards: MemoryCard[] = []
      rows: number = 0
      columns: number = 0
      totalPairs: number = 0
      matchedPairs: number = 0
      firstCardId: string = ''
      secondCardId: string = ''
      waitingForFlipBack: boolean = false
      flipBackDelayMs: number = 0
      roundIndex: number = 0
      totalRounds: number = 0
      guideChromEnabled: boolean = false
      touchEnableDelayMs: number = 0
  }
  ```
- [x] Crear clase `MemoryEnginePayload` en `game/GameEvent.ts`
- [x] Añadir tipo `MEMORY` a `GAME_ENGINE`

### MemoryGameScene
- [x] Crear clase `MemoryGameScene` en `game/MemoryGameScene.ts`
- [x] Extender `Scene` de Phaser
- [x] Implementar `init()`: recibir datos de WorldMap
- [x] Implementar `create()`: crear fondo, barra de progreso, Nubi, botón de salida
- [x] Implementar `preload()`: cargar assets de memoria
- [x] Implementar `manageWs()`: manejar eventos WebSocket
- [x] Implementar `readEvent()`: procesar eventos del backend
- [x] Implementar `renderBoard()`: renderizar tablero de cartas
- [x] Implementar `flipCard()`: animación de volteo de carta
- [x] Implementar `flipBackCards()`: voltear cartas no coincidentes tras tiempo de visibilidad
- [x] Implementar `checkPair()`: verificar si dos cartas forman pareja
- [x] Implementar `playCelebration()`: celebración al completar
- [x] Implementar `sendAbandonAndExit()`: abandonar y volver a WorldMap
- [x] Implementar `cleanup()`: limpiar recursos al destruir escena

### Renderizado de Tablero
- [x] Calcular posición de cada carta según `row` y `column`
- [x] Renderizar cartas boca abajo inicialmente
- [x] Aplicar animación de volteo (flip) al voltear carta
- [x] Mantener cartas coincidentes boca arriba permanentemente
- [x] Voltear cartas no coincidentes tras `flipBackDelayMs`

### Animaciones
- [x] Crear animación de volteo de carta (flip horizontal)
- [x] Animación de carta boca abajo → boca arriba
- [x] Animación de carta boca arriba → boca abajo (para no coincidentes)
- [x] Animación de celebración al completar (estrellas o confeti)

### Integración con Infraestructura
- [x] Reutilizar `MinigameNubiLayer` (SPRINT-078): audio de Nubi, single-tap para repetir
- [x] Reutilizar `ExitButton` (SPRINT-078): doble-tap para abandonar
- [x] Reutilizar `RoundProgressBar` (SPRINT-070): mostrar progreso de parejas encontradas
- [x] Reutilizar `applyTouchEnableDelay()` (SPRINT-074): espera antes de habilitar toque
- [x] Reutilizar `renderGuideChrom()` (SPRINT-074): cromo guía en EASY

### Assets
- [x] Crear bloque `recognition-memory` en `assets-manifest.json`:
  ```json
  "recognition-memory": {
      "files": [
          { "type": "image", "key": "card-back", "url": "assets/images/memory/card-back.png" },
          { "type": "image", "key": "apple", "url": "assets/images/memory/apple.png" },
          { "type": "image", "key": "car", "url": "assets/images/memory/car.png" },
          { "type": "image", "key": "house", "url": "assets/images/memory/house.png" },
          { "type": "image", "key": "tree", "url": "assets/images/memory/tree.png" },
          { "type": "image", "key": "flower", "url": "assets/images/memory/flower.png" },
          { "type": "image", "key": "ball", "url": "assets/images/memory/ball.png" },
          { "type": "image", "key": "book", "url": "assets/images/memory/book.png" },
          { "type": "image", "key": "cup", "url": "assets/images/memory/cup.png" },
          { "type": "image", "key": "hat", "url": "assets/images/memory/hat.png" },
          { "type": "image", "key": "shoe", "url": "assets/images/memory/shoe.png" }
      ]
  }
  ```
- [x] Implementar carga dinámica en `loadResources()` para categoría MEMORY

### Registro de Escena
- [x] Registrar `MemoryGameScene` en `GameView.vue`:
  ```typescript
  const game = new Phaser.Game({
      scene: [LoadingScene, WorldMapScene, RecognitionGameScene, MemoryGameScene, FarewellScene, OrientationRequiredScene]
  })
  ```

### Tests
- [x] Test: escena se registra correctamente
- [x] Test: tablero se renderiza con dimensiones correctas según dificultad
- [x] Test: volteo de carta funciona correctamente
- [x] Test: parejas coincidentes permanecen boca arriba
- [x] Test: parejas no coincidentes se voltean tras tiempo de visibilidad
- [x] Test: audio de Nubi se reproduce al inicio
- [x] Test: botón de salida funciona correctamente
- [x] Test: celebración se muestra al completar

## Criterios de Aceptación

1. MemoryGameScene se registra y funciona correctamente
2. El tablero se renderiza con las dimensiones correctas según dificultad (2×2, 2×3, 2×4)
3. Las cartas se voltean con animación de flip
4. Las parejas coincidentes permanecen boca arriba permanentemente
5. Las parejas no coincidentes se voltean tras el tiempo de visibilidad (2s, 1.5s, 1s)
6. La consigna visual es clara y comprensible sin audio
7. El audio de Nubi se reproduce al inicio de la partida
8. El botón de salida funciona correctamente (doble-tap)
9. Nubi permite repetir el audio con single-tap
10. La celebración se muestra al encontrar todas las parejas
11. La escena funciona sin audio
12. Los tests pasan correctamente

## Notas Técnicas

- **Volteo de cartas**: usar animación de flip horizontal (scaleX de 1 a 0 y luego de 0 a 1)
- **Estado de cartas**: `faceUp` (boca arriba), `matched` (pareja encontrada)
- **Tamaño táctil**: cada carta debe tener al menos 44px de hit area
- **Accesibilidad**: la experiencia debe funcionar sin audio, sin lectura, sin color como única señal

## Dependencias

- SPRINT-078 completado (audio de Nubi y botón de salida)
- SPRINT-079/081 completado (carga dinámica)
- SPRINT-080 completado (renderizado adaptable)
- SPRINT-109 completado (backend: MemoryEngine básico)

## Estimación

- **Tamaño:** L (Large)
- **Complejidad:** Alta (nueva escena completa con animaciones de volteo y lógica de parejas)
- **Riesgo:** Medio (lógica diferente a RecognitionGameScene, pero patrón similar)

## Implementación completada (2026-09-21)

### Resumen técnico
- **Escena `memory-game`** (`src/game/MemoryGameScene.ts`), registrada en `GameView.vue`. Reutiliza `MinigameNubiLayer`, `ExitButton` (doble tap), `RoundProgressBar` (parejas encontradas / parejas totales), `ViewportBackdrop`, el fondo de bioma y el tablero 9-slice de los minijuegos, y el escalado FIT. Consigna de Nubi: `ROUND_PROMPT` una vez al inicio (SPRINT-110) y un toque en Nubi la repite.
- **Cartas** (assets nuevos): `card_cover.png` (carta tapada) y `card_reverse.png` (carta levantada, sobre la que se pinta el elemento). Volteo: `scaleX` 1 → 0, cambio de cara y 0 → 1 (instantáneo con `prefers-reduced-motion`).
- **El servidor manda**: el cliente solo dibuja su estado. Al tocar una carta envía `{"cardId","responseTimeMs"}` y bloquea los toques hasta la respuesta; al llegar el estado voltea la carta (su `elementId` solo llega cuando está boca arriba). Las parejas emparejadas quedan boca arriba (pequeño "pop" al emparejar, sin sonido). Una pareja que no coincide se voltea sola tras `flipBackDelayMs`, **sin sonido, sin vaivén, sin contador de fallos** (ADR-030). Tocar otra carta con la pareja a la vista la voltea al instante (el servidor hace lo mismo con esa acción) y no se pierde el toque; tocar una carta que sigue a la vista no hace nada.
- **Rejilla** (`ResponsiveLayout.getMemoryGrid`): cartas con el aspecto de la imagen (297×451) centradas entre la barra de progreso y el borde inferior, con el margen de Nubi a ambos lados (su esquina queda libre). En 2×2, 2×3 y 2×4 y en las cuatro resoluciones objetivo la carta mide ≥ 44 px CSS de ancho, sin solapes ni salirse de pantalla.
- **Celebración** al completar (fuegos artificiales, ver más abajo) y vuelta al mapa; abandono con doble tap envía `game_abandon`.
- **Carga dinámica**: `DynamicAssetLoader` gana la categoría `MEMORY` (`recognition-memory`); solo se cargan las cartas y los elementos del tablero (`resourceRefs.image`, o `code`).
- **WorldMapScene** elige la escena según el `engine` de `WORLD_ACTIVITY_STARTED` (`MEMORY` → `memory-game`, resto → `recognition-game`).
- **Backend (mínimo necesario, fuera del alcance nominal del sprint):** sin el estado del tablero en el payload la escena no podía dibujar nada. `GameWebSocketHandler.gameStateToPayload` añade `memoryState` para `MEMORY` (tests en `GameWebSocketHandlerTest`) y el contrato `game-state-payload.yaml` lo documenta.

### Decisiones de detalle y desvíos
1. **Modelo de datos distinto al del sprint:** los `MemoryState`/`MemoryCard` del sprint (con `firstCardId`, `secondCardId`, `roundIndex`, `guideChromEnabled`, `touchEnableDelayMs`...) no coinciden con lo que envía el backend. Se define el que envía (`rows`, `columns`, `totalPairs`, `matchedPairs`, `flipBackDelayMs`, `waitingForFlipBack`, `flipBackCardIds`, `cards`, `elements`). No hay cromo guía (eliminado en los minijuegos) ni retardo de toque.
2. **Assets:** el sprint listaba `card-back` y manzana/coche/...; los reales son `card_cover`/`card_reverse` y los 10 elementos del seed (`banana`, `pear`, `orange`, `strawberry`, `cherry`, `bus`, `bicycle`, `train`, `boat`, `plane`). Bloque `recognition-memory` en `assets-manifest.json` con las claves `memory-card-cover` y `memory-card-reverse`.
3. **Sin `MemoryEnginePayload` separado de `BaseEnginePayload`:** se añaden `MemoryEnginePayload` y `MemoryState` a `GameEvent.ts` (`engine` con tipo literal en cada payload para poder discriminar); `RecognitionGameScene` ignora los resultados con `engine === 'MEMORY'`.
4. **Sin bloqueo durante el tiempo de visibilidad:** se puede seguir jugando mientras la pareja está a la vista (el toque la voltea); solo se bloquea entre el toque y la respuesta del servidor.
5. **Duplicación conocida:** el escalado FIT, el fondo, la transición de entrada y el audio de la consigna están copiados de `RecognitionGameScene` (1500 líneas con mucha cobertura E2E); extraerlos a un módulo común queda como deuda técnica.

### Archivos
- **Nuevos:** `src/game/MemoryGameScene.ts`, `cypress/e2e/fase7-gameview/memory-game.cy.ts`.
- **Modificados:** `GameEvent.ts`, `DynamicAssetLoader.ts`, `ResponsiveLayout.ts` (`getMemoryGrid`, `memoryCardCenter`), `WorldMapScene.ts`, `RecognitionGameScene.ts` (guarda de tipo), `GameView.vue` (escena + gancho Cypress `startMemoryScene`/`getMemoryData`/`tapCard`), `assets-manifest.json`; backend `GameWebSocketHandler` y su test; contrato `game-state-payload.yaml`.

### Pruebas
- `npx tsc --noEmit` limpio. Spec `memory-game.cy.ts` (12 de rejilla pura + 16 de escena). Como los demás specs de `fase7-gameview` necesita el backend para crear la familia; se ejecutó **sin backend** contra una página temporal con la misma API del gancho (`__NUBI_GAME_STATE__`), ya eliminada: 28/28 correctos (escena registrada, 2×2/2×3/2×4, carga bajo demanda, envío de `cardId` y bloqueo, volteo, pareja fija, pareja no coincidente que se voltea sola, toque durante la espera, carta a la vista ignorada, audio y repetición con Nubi, sin audio, salida con doble tap, celebración y vuelta al mapa). Revisadas capturas del tablero 2×4 boca abajo y mezclado.
- Backend: `GameWebSocketHandlerTest` con 3 tests nuevos del payload `memoryState`.
- No se ha probado la escena contra el backend real ni el flujo desde el mapa (`world_discovery_interacted` → `memory-game`).

### Riesgos, deuda y handoffs
- **Entrada desde el mundo:** para llegar al juego desde el mapa hace falta un elemento de descubrimiento cuyo `activityId`/`topicId` apunte a "Memoria de Parejas" (`seeds/14-world-discovery-elements.json` no tiene ninguno) — pendiente de backend/contenido.
- El tablero de madera (`tablero-minigame`) y el fondo del bioma vienen del pack del mapa; en la prueba aislada se usó el degradado de respaldo.
- Validar con niños y niñas de 3-4 años el tamaño de las cartas y que el volteo neutro se entiende sin sonido.

### Celebración con fuegos artificiales (2026-09-21)
Sustituye a las estrellas en **los dos minijuegos** (memoria y reconocimiento) mediante `src/game/ui/FireworksCelebration.ts`:
- Usa el spritesheet `assets/animations/effects/fireworks.png` (1536×1280): **6 columnas × 5 filas de 256×256 = 30 fotogramas** (la imagen tiene 5 filas, no 4), a 24 fps (~1,25 s cada fuego).
- Lanza 5–7 fuegos en puntos aleatorios del viewport (rejilla de 3×2 celdas en la parte superior, con desplazamiento aleatorio dentro de cada celda, para que no se amontonen), cada uno con **tinte aleatorio** (paleta de 8 colores sin verde para que se vean sobre el prado), tamaño aleatorio (×0,9–1,5) y comienzo escalonado (0–0,9 s). El tinte es de relleno (`TintModes.FILL`): el amarillo del sprite no condiciona el color.
- La escena sale al mapa cuando termina el último fuego (~2,1 s, antes 1,5 s); el sonido de celebración no cambia. Con `prefers-reduced-motion` no hay animación: fotograma fijo, mismos puntos y tintes, 1,5 s.
- La textura se carga con `FireworksCelebration.preload` al crear la escena (mientras se juega); si no llegara a cargar, la escena sale igual. Los fuegos se destruyen al terminar.
- Comprobado en una página temporal (ya eliminada): 5–7 fuegos, duración 2,0–2,5 s, sin sprites vivos al terminar, y con movimiento reducido 1,5 s. `celebrationStarCount` del gancho de test pasa a `celebrationBurstCount`.
