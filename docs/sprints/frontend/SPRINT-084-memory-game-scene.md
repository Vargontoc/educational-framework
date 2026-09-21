# SPRINT-084: MemoryGameScene - Tablero y Volteo de Cartas

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
- [ ] Crear interfaz `MemoryCard` en `game/GameEvent.ts`:
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
- [ ] Crear clase `MemoryState` en `game/GameEvent.ts`:
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
- [ ] Crear clase `MemoryEnginePayload` en `game/GameEvent.ts`
- [ ] Añadir tipo `MEMORY` a `GAME_ENGINE`

### MemoryGameScene
- [ ] Crear clase `MemoryGameScene` en `game/MemoryGameScene.ts`
- [ ] Extender `Scene` de Phaser
- [ ] Implementar `init()`: recibir datos de WorldMap
- [ ] Implementar `create()`: crear fondo, barra de progreso, Nubi, botón de salida
- [ ] Implementar `preload()`: cargar assets de memoria
- [ ] Implementar `manageWs()`: manejar eventos WebSocket
- [ ] Implementar `readEvent()`: procesar eventos del backend
- [ ] Implementar `renderBoard()`: renderizar tablero de cartas
- [ ] Implementar `flipCard()`: animación de volteo de carta
- [ ] Implementar `flipBackCards()`: voltear cartas no coincidentes tras tiempo de visibilidad
- [ ] Implementar `checkPair()`: verificar si dos cartas forman pareja
- [ ] Implementar `playCelebration()`: celebración al completar
- [ ] Implementar `sendAbandonAndExit()`: abandonar y volver a WorldMap
- [ ] Implementar `cleanup()`: limpiar recursos al destruir escena

### Renderizado de Tablero
- [ ] Calcular posición de cada carta según `row` y `column`
- [ ] Renderizar cartas boca abajo inicialmente
- [ ] Aplicar animación de volteo (flip) al voltear carta
- [ ] Mantener cartas coincidentes boca arriba permanentemente
- [ ] Voltear cartas no coincidentes tras `flipBackDelayMs`

### Animaciones
- [ ] Crear animación de volteo de carta (flip horizontal)
- [ ] Animación de carta boca abajo → boca arriba
- [ ] Animación de carta boca arriba → boca abajo (para no coincidentes)
- [ ] Animación de celebración al completar (estrellas o confeti)

### Integración con Infraestructura
- [ ] Reutilizar `MinigameNubiLayer` (SPRINT-078): audio de Nubi, single-tap para repetir
- [ ] Reutilizar `ExitButton` (SPRINT-078): doble-tap para abandonar
- [ ] Reutilizar `RoundProgressBar` (SPRINT-070): mostrar progreso de parejas encontradas
- [ ] Reutilizar `applyTouchEnableDelay()` (SPRINT-074): espera antes de habilitar toque
- [ ] Reutilizar `renderGuideChrom()` (SPRINT-074): cromo guía en EASY

### Assets
- [ ] Crear bloque `recognition-memory` en `assets-manifest.json`:
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
- [ ] Implementar carga dinámica en `loadResources()` para categoría MEMORY

### Registro de Escena
- [ ] Registrar `MemoryGameScene` en `GameView.vue`:
  ```typescript
  const game = new Phaser.Game({
      scene: [LoadingScene, WorldMapScene, RecognitionGameScene, MemoryGameScene, FarewellScene, OrientationRequiredScene]
  })
  ```

### Tests
- [ ] Test: escena se registra correctamente
- [ ] Test: tablero se renderiza con dimensiones correctas según dificultad
- [ ] Test: volteo de carta funciona correctamente
- [ ] Test: parejas coincidentes permanecen boca arriba
- [ ] Test: parejas no coincidentes se voltean tras tiempo de visibilidad
- [ ] Test: audio de Nubi se reproduce al inicio
- [ ] Test: botón de salida funciona correctamente
- [ ] Test: celebración se muestra al completar

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
