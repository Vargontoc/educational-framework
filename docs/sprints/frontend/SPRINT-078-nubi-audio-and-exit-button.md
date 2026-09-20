# SPRINT-078 — Audio de Nubi y nueva interacción de salida

## Metadata
- **Estado:** implemented
- **Capa:** frontend
- **Dependencias:** SPRINT-077, SPRINT-098 (backend)
- **Feature:** FEAT-014, FEAT-013

## Objetivo

Implementar la reproducción de audio de Nubi al inicio de cada ronda, permitir repetir el audio pulsando en Nubi, y mover el mecanismo de abandono a un botón dedicado en la esquina superior.

## Contexto

Actualmente:
- `MinigameNubiLayer` detecta doble-tap para abandonar el minijuego
- No hay reproducción de audio específica por ronda
- El audio de feedback (acierto/error) existe, pero no el audio de instrucción

Cambios necesarios:
1. **Audio de ronda:** Recibir `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"` y reproducir el audio
2. **Nubi como botón de audio:** Pulsar en Nubi repite el audio de la ronda actual (ya no es doble-tap para salir)
3. **Botón de salir:** Crear botón dedicado en esquina superior izquierda con doble-tap para abandonar

## Requisitos

### Audio de ronda

1. **Recepción de evento:**
   - Escuchar `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`
   - Extraer `audioId`, `audioAvailable`, `text` del payload
   - Si `audioAvailable = true`, descargar/buffer el audio binario

2. **Reproducción automática:**
   - Al recibir el evento, reproducir el audio automáticamente
   - Solo si `audioGeneralEnabled` y `ttsEnabled` están activos
   - No bloquear la interacción mientras se reproduce

3. **Caché de audio:**
   - Almacenar audio por `audioId` para permitir repetición
   - Limpiar caché al cambiar de escena

### Nubi como botón de audio

1. **Cambio de interacción:**
   - Single-tap en Nubi: repetir audio de la ronda actual
   - Eliminar lógica de doble-tap para abandonar

2. **Feedback visual:**
   - Al pulsar Nubi, mostrar indicador visual breve (ej: onda de sonido)
   - Si no hay audio disponible, no hacer nada (o mostrar indicador de "no audio")

3. **Estados de Nubi:**
   - NPC activo: Nubi animado, responde a tap repitiendo audio
   - NPC inactivo: Nubi dormido, no responde a tap (o muestra ZZZ)

### Botón de salir

1. **Ubicación:**
   - Esquina superior izquierda
   - Tamaño: 60x60px (ajustable por resolución)
   - Margen: 20px desde bordes

2. **Apariencia:**
   - Icono de "X" o puerta de salida
   - Color: semi-transparente, no intrusivo
   - Hover/tap: cambio de opacidad

3. **Interacción:**
   - Doble-tap para abandonar (sin confirmación)
   - Single-tap: sin acción (evitar abandonos accidentales)
   - Ventana de doble-tap: 2000ms (igual que antes)

4. **Acción de abandono:**
   - Enviar `game_abandon` al backend
   - Transición de salida (fade a negro)
   - Volver a WorldMap

### Consideraciones de accesibilidad

- El botón de salir debe ser táctil (mínimo 44x44px)
- El audio debe ser opcional (respetar preferencias de audio)
- Sin audio, el juego debe ser completamente jugable (el texto no se muestra al niño, pero el estímulo visual es suficiente)

## Tareas

### Audio de ronda
- [x] Extender `GameEvent.ts` para soportar `eventType = "ROUND_PROMPT"` en `AvatarEvent`
- [x] Crear `RoundAudioCache` en `RecognitionGameScene` para almacenar audios por `audioId`
- [x] Implementar recepción de `GAME_AVATAR_EVENT` en `readEvent()`
- [x] Implementar descarga/buffer de audio binario (usar `AudioService` o nuevo método)
- [x] Implementar reproducción automática al recibir el evento
- [x] Respetar preferencias de audio (`audioGeneralEnabled`, `ttsEnabled`)
- [x] Limpiar caché de audio en `cleanup()`

### Nubi como botón de audio
- [x] Modificar `MinigameNubiLayer.handleTap()`:
  - Single-tap: emitir evento `minigame-nubi-tap` (no doble-tap)
  - Eliminar lógica de doble-tap
- [x] En `RecognitionGameScene`, escuchar `minigame-nubi-tap`:
  - Si hay audio en caché, reproducirlo
  - Mostrar feedback visual (onda de sonido)
- [x] Si no hay audio disponible, no hacer nada

### Botón de salir
- [x] Crear `ExitButton` component en `game/ui/ExitButton.ts`:
  - Icono de salida (X o puerta)
  - Posición: esquina superior izquierda
  - Detección de doble-tap (ventana 2000ms)
  - Emite evento `exit-button-double-tap`
- [x] Integrar `ExitButton` en `RecognitionGameScene.create()`
- [x] Escuchar `exit-button-double-tap` en `RecognitionGameScene`:
  - Llamar a `sendAbandonAndExit()`
- [x] Eliminar listener de `minigame-nubi-double-tap` para abandonar

### Feedback visual
- [x] Crear animación de "onda de sonido" al pulsar Nubi:
  - Círculos concéntricos que se expanden desde Nubi
  - Duración: 500ms
  - Color: blanco semi-transparente
- [x] Mostrar animación solo si hay audio disponible

### Tests
- [x] Test: audio se reproduce automáticamente al recibir `ROUND_PROMPT`
- [x] Test: pulsar Nubi repite el audio de la ronda
- [x] Test: doble-tap en botón de salir abandona el minijuego
- [x] Test: single-tap en Nubi no abandona (solo repite audio)
- [x] Test: sin audio disponible, pulsar Nubi no hace nada
- [x] Test: caché de audio se limpia al cambiar de escena

## Criterios de aceptación

1. Al inicio de cada ronda, se reproduce automáticamente el audio de Nubi (si está habilitado)
2. Pulsar en Nubi repite el audio de la ronda actual
3. El botón de salir en esquina superior permite abandonar con doble-tap
4. Single-tap en Nubi no abandona el minijuego
5. Sin audio disponible, el juego es completamente jugable
6. El caché de audio se limpia correctamente al salir del minijuego

## Notas técnicas

- El audio binario se recibe como `Uint8Array` o `Blob` desde el WebSocket
- Usar `AudioService.playBinary()` o similar para reproducir audio binario
- Considerar precarga del audio en background mientras se muestra la animación de transición
- El botón de salir debe ser accesible pero no intrusivo (semi-transparente)

## Dependencias backend

- SPRINT-098 debe enviar `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"` y audio binario
- El payload debe incluir: `audioAvailable`, `audioId`, `text` (opcional para debug)

## Implementación completada (2026-09-20)

### Resumen
- El audio de ronda llega como `GAME_AVATAR_EVENT` (`ROUND_PROMPT`) en JSON, seguido de un frame binario. `RecognitionGameScene.manageWs` ahora enruta con `MessageRouter` (antes hacía `JSON.parse` directo, que fallaba con el frame binario) y delega los binarios en `AudioService.handleBinaryFrame`.
- Reproducción automática vía la cola de `AudioService` (`enqueue` + `playNext`) para no cortar el sonido de acierto/error de la respuesta anterior. Si el binario aún no está decodificado, se espera al evento `audio-received`.
- Nubi: single-tap emite `minigame-nubi-tap` (solo si el NPC está activo). La escena repite el audio y dispara la onda de sonido solo si hay audio en caché y las preferencias lo permiten.
- Botón de salida (`ExitButton`): esquina superior izquierda, 60x60, margen 20, doble-tap con ventana de 2000 ms, emite `exit-button-double-tap`.

### Archivos
- Creados: `game/RoundAudioCache.ts`, `game/ui/ExitButton.ts`, `cypress/e2e/fase7-gameview/nubi-audio-y-salida.cy.ts`
- Modificados: `game/GameEvent.ts` (`ROUND_PROMPT` en `AVATAR_TYPE_EVENT`), `game/RecognitionGameScene.ts`, `game/worldmap/layers/MinigameNubiLayer.ts` (single-tap, `playSoundWave()`, sin doble-tap), `views/GameView.vue` (ganchos solo bajo Cypress), `cypress/e2e/fase7-gameview/recognition-minigame.cy.ts` (el test de abandono apunta al botón nuevo)

### Decisiones de detalle
1. `RoundAudioCache` solo registra los `audioId` de la escena; los `AudioBuffer` viven en el `AudioCache` compartido del registry y se borran de él en `cleanup()`.
2. Con `audioGeneralEnabled` o `ttsEnabled` desactivados no se reproduce ni al recibir ni al pulsar Nubi (el sprint solo lo exige para el automático; se aplica también al tap por la nota de accesibilidad).
3. Al abandonar se llama a `AudioService.stop()` para que Nubi no siga hablando en el WorldMap.
4. La dependencia backend real es SPRINT-104 (implemented); el sprint cita SPRINT-098, que trata de selección de distractores.

### Pruebas
- `npx tsc --noEmit` (app): sin errores.
- `nubi-audio-y-salida.cy.ts`: 9 tests escritos (audio automático, frame binario tardío, repetir con Nubi, doble-tap salida, single-tap Nubi/salida no abandona, sin audio, audio desactivado, limpieza de caché). **No ejecutados**: requieren backend + BD levantados. Sí se verificó con un arnés temporal (Phaser real + escena, WebSocket y `AudioService` simulados; ver SPRINT-080): ROUND_PROMPT con audio en caché se encola, audio que llega después del evento se encola al recibirlo, tocar Nubi repite el audio, doble toque en Nubi no abandona, sin audio el toque no hace nada y doble toque en el botón de salir abandona.
- `cypress/tsconfig.json`: solo falla `fase5-chatbot/conexion-stomp.cy.ts` (preexistente, ajeno a este sprint).

### Riesgos
- `tapNubi()` del gancho de test no hace nada si el spritesheet de Nubi aún no cargó; posible flakiness en los tests de repetición.
- Hook de test asume `websocket`, `currentBiome`, `sessionId`, `childId` públicos en `WorldMapScene`.
