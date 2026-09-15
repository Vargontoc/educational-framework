# Sprint 070 - frontend
# -----------------------------------------------

## Goal
Implementar la base jugable del minijuego: contenedor visual diferenciado de WorldMap, barra de llenado para progreso de rondas, feedback visual básico de acierto/fallo, transiciones de entrada/salida, y mecanismo de abandono por doble-toque. Permite probar el loop jugable completo con el motor de reconocimiento existente.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-14):

- **FEAT-013 D1**: "El recuadro de minijuego debe diferenciarse visualmente de WorldMap y conservar una acción de salida siempre disponible."
- **FEAT-013 D4**: "Dos toques consecutivos sobre Nubi abandonan inmediatamente el minijuego, sin confirmación infantil, penalización ni feedback negativo."
- **FEAT-013 D5/D6**: Acierto → animación visual de éxito; fallo → vaivén visual leve. Sonidos opcionales.
- **FEAT-013 D9**: "Al completar todas las rondas, se muestra una celebración visual breve, sin premios gamificados, y se vuelve a WorldMap."
- **Estado actual**: `RecognitionGameScene` ya existe (160 líneas) y maneja el flujo básico:
  - Recibe `GAME_STARTED` → envía `game_ready`.
  - Recibe `GAME_READY` → carga recursos y renderiza elementos.
  - Al tocar elemento → envía `game_action`.
  - Recibe `GAME_ACTION_RESULT` → si `gameCompleted: true`, hace `scene.start('world-map')`.
- **Gap crítico**: No hay contenedor visual diferenciado, no hay feedback por ronda, no hay transición de entrada/salida, no hay mecanismo de abandono.
- **Indicador de ronda**: FEAT-013 no especifica indicador, pero el análisis propuso puntos visuales. **Decisión confirmada (2026-09-14)**: usar **barra de llenado** porque la dificultad puede aumentar hasta 15 rondas (demasiados puntos para renderizar).
- **Transición de entrada**: **Decisión confirmada (2026-09-14)**: fade a negro 400ms consistente con `BiomeTransition`.
- **Nubi en minijuego**: Se implementa en SPRINT-071. Este sprint solo incluye el mecanismo de doble-toque (inicialmente sobre un placeholder o zona invisible en esquina inferior derecha).
- **Motor de reconocimiento**: Ya existe y funciona. Este sprint integra el loop jugable completo para probar.

## Status
status: proposed
started_at:
closed_at:
verified_at:
blocked_by: SPRINT-069 (WorldMap verificado)
waiting_for:

## Decisiones confirmadas (2026-09-14)

1. **Barra de llenado en lugar de puntos visuales.** Confirmado — la dificultad puede aumentar hasta 15 rondas, haciendo inviable renderizar puntos individuales. La barra de llenado escala visualmente sin importar el número de rondas.
2. **Transición de entrada: fade a negro 400ms.** Confirmado — consistente con `BiomeTransition` de SPRINT-068.
3. **Nubi en minijuego: se implementa en SPRINT-071.** Este sprint solo incluye el mecanismo de doble-toque (placeholder).
4. **Sprite de Nubi dormido: usar existente temporalmente.** Confirmado — se usará el sprite actual hasta que esté el asset dedicado.

## Diseño propuesto

### 1. Contenedor visual diferenciado

`RecognitionGameScene` gana un fondo visual diferenciado de WorldMap:

- **Fondo**: Gradiente suave por bioma (mismo bioma que el elemento interactivo que abrió el minijuego). No usa los assets de WorldMap (skybox, ground, parallax).
- **Marco superior**: Barra de progreso (ver sección 2).
- **Zona central**: Elementos del minijuego (objetivo + opciones) con tamaño táctil mínimo de 80x80px.
- **Zona inferior derecha**: Placeholder para Nubi (zona invisible de 100x100px para doble-toque).

### 2. Barra de llenado (progreso de rondas)

**Componente**: `RoundProgressBar` (nuevo, en `game/ui/`).

- **Posición**: Marco superior, centrado horizontalmente, 20px desde el borde superior.
- **Dimensiones**: 200px ancho × 12px alto, esquinas redondeadas (6px).
- **Visual**: 
  - Fondo: gris suave (`#E0E0E0`).
  - Relleno: gradiente verde-azul (`#4CAF50` → `#2196F3`), ancho proporcional a `roundIndex / totalRounds`.
  - Animación: transición suave (300ms, `Cubic.out`) al avanzar.
- **Consumo**: Lee `recognitionState.roundIndex` y `recognitionState.totalRounds` del `GAME_ACTION_RESULT`.
- **Accesibilidad**: No usa color como única señal (la barra crece visualmente).

### 3. Feedback visual por ronda

**Acierto**:
- Animación sobre el elemento seleccionado: escala (1→1.15→1, 200ms) + brillo verde suave (tint `#4CAF50`, alpha 0.3, 200ms) + partícula expansiva (círculo que crece y desvanece).
- Sonido opcional: `success.wav` (solo si `audioGeneralEnabled && ttsEnabled`).

**Fallo**:
- Animación sobre el elemento seleccionado: vaivén horizontal (±8px, 300ms, 2 ciclos, `Sine.inOut`).
- Sonido opcional: `error-soft.wav` (solo si `audioGeneralEnabled && ttsEnabled`).

**Implementación**: 
- Tras recibir `GAME_ACTION_RESULT`, clasificar `resultType` (`CORRECT`, `INCORRECT`, `TIMEOUT`).
- Aplicar animación correspondiente al `selectedOptionId`.
- Esperar 500ms antes de permitir la siguiente interacción (evitar spam de toques).

### 4. Transiciones de entrada y salida

**Entrada (WorldMap → Minijuego)**:
1. En `WorldMapScene`, al tocar elemento interactivo que abre minijuego:
   - Fade a negro (400ms, `Linear`).
   - En midpoint (200ms), `scene.start('recognition-game', { activityId, biome, ... })`.
2. En `RecognitionGameScene.create()`:
   - Fade desde negro (400ms, `Linear`).

**Salida al completar (Minijuego → WorldMap)**:
1. Al recibir `gameCompleted: true`:
   - Mostrar celebración básica (ver SPRINT-072).
   - Fade a negro (400ms).
   - `scene.start('world-map', { websocket, sessionId, childId })`.

**Salida por abandono (doble-toque)**:
1. Al detectar doble-toque en zona Nubi:
   - Enviar `game_abandon` vía WebSocket.
   - Fade a negro (400ms).
   - `scene.start('world-map', { websocket, sessionId, childId })`.

### 5. Mecanismo de abandono por doble-toque

**Zona de toque**: Placeholder en esquina inferior derecha (100x100px, invisible).

**Detección**:
- Ventana de tiempo: 2000ms entre toques.
- Contador: si segundo toque llega dentro de la ventana → abandono.
- Reset: si pasa más de 2000ms, contador vuelve a 0.

**Acción**:
- Enviar `GameAbandonEvent` (`{type: 'game_abandon'}`).
- Limpiar estado local (detener animaciones, cancelar tweens).
- Transición de salida (fade → WorldMap).

**Nota**: En SPRINT-071, la zona se reemplaza por `MinigameNubiLayer` real.

### 6. Consumo de contratos

| Evento | Dirección | Uso |
|---|---|---|
| `GAME_STARTED` | Servidor → Cliente | Enviar `game_ready` |
| `GAME_READY` | Servidor → Cliente | Cargar recursos, renderizar elementos, iniciar barra |
| `GAME_ACTION_RESULT` | Servidor → Cliente | Aplicar feedback, actualizar barra, verificar completado |
| `game_action` | Cliente → Servidor | Enviar selección de elemento con `responseTimeMs` |
| `game_abandon` | Cliente → Servidor | Abandonar minijuego (doble-toque) |

**`recognitionState`** (ya existe en `GAME_READY` y `GAME_ACTION_RESULT`):
- `roundIndex`: ronda actual (0-based).
- `totalRounds`: total de rondas.
- `hintActive`: si pista está activa (se consume en SPRINT-072).
- `targetElementId`, `optionIds`: elementos de la ronda.

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|---|---|---|
| AsyncAPI `game-state-payload.yaml` | Sin cambios | `recognitionState` ya existe |
| AsyncAPI `game-client-message.yaml` | Sin cambios | `game_abandon` ya existe |
| AsyncAPI `game-action-response.yaml` | Sin cambios | `gameCompleted` ya existe |
| SPRINT-069 (WorldMap verificado) | Requerida | Punto de partida para transición |
| Assets: `success.wav`, `error-soft.wav` | Requeridos | Sonidos de feedback (pueden ser placeholders) |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Los elementos táctiles pueden ser demasiado pequeños | MEDIA | Verificar tamaño mínimo de 80x80px. Usar `setInteractive({ useHandCursor: false })` con hit area ampliado si necesario. |
| R2 | El fade a negro puede ser demasiado lento para niños impacientes | BAJA | 400ms es aceptable (consistente con biomas). Si se percibe lento, reducir a 300ms. |
| R3 | La barra de llenado puede no ser comprensible sin contexto | BAJA | Aceptable: la barra crece visualmente, lo que indica progreso. No se usa como señal evaluativa. |
| R4 | El doble-toque puede causar abandonos accidentales | MEDIA | Aceptado por producto (FEAT-013): minimizar fricción > prevenir error. Sin confirmación. |

## Handoffs a otras capas (no diseñados en detalle aquí)

- **Backend**: sin cambios de contrato. Los sprints 095/096 son transparentes para frontend.
- **Agents**: no aplica en este sprint (Nubi se implementa en SPRINT-071).
- **Contenido**: assets de sonido (`success.wav`, `error-soft.wav`) pueden ser placeholders iniciales.

## Tareas del sprint

### Contenedor visual
- [ ] Añadir fondo diferenciado a `RecognitionGameScene` (gradiente por bioma).
- [ ] Crear componente `RoundProgressBar` en `game/ui/`.
- [ ] Integrar barra en `RecognitionGameScene` (posición superior centrada).
- [ ] Actualizar barra al recibir `GAME_ACTION_RESULT` (leer `roundIndex`/`totalRounds`).
- [ ] Animación de transición de la barra (300ms, `Cubic.out`).

### Feedback visual
- [ ] Implementar animación de acierto: escala + brillo verde + partícula.
- [ ] Implementar animación de fallo: vaivén horizontal (±8px, 300ms, 2 ciclos).
- [ ] Añadir sonidos opcionales: `success.wav` y `error-soft.wav` (gateados por preferencias de audio).
- [ ] Esperar 500ms tras feedback antes de permitir siguiente interacción.

### Transiciones
- [ ] Implementar transición de entrada en `WorldMapScene`: fade a negro 400ms → `scene.start`.
- [ ] Implementar transición de entrada en `RecognitionGameScene`: fade desde negro 400ms.
- [ ] Implementar transición de salida al completar: fade a negro → `scene.start('world-map')`.
- [ ] Implementar transición de salida por abandono: fade a negro → `scene.start('world-map')`.

### Abandono
- [ ] Crear zona invisible en esquina inferior derecha (100x100px).
- [ ] Implementar detección de doble-toque (ventana 2000ms).
- [ ] Al doble-toque: enviar `game_abandon`, limpiar estado, transición de salida.

### Pruebas
- [ ] Verificar tamaños táctiles mínimos (80x80px) en tablet y móvil.
- [ ] Verificar que la barra se actualiza correctamente con 5, 10 y 15 rondas.
- [ ] Verificar que el feedback visual es comprensible sin audio.
- [ ] Verificar que el doble-toque abandona inmediatamente sin confirmación.
- [ ] Verificar que las transiciones son suaves (sin parpadeo ni saltos).

## Manual Tests
- Con backend levantado y un niño con sesión activa: tocar elemento interactivo en WorldMap que abre minijuego. Verificar fade a negro → transición a minijuego → fade desde negro.
- Jugar 5 rondas: verificar que la barra de llenado avanza proporcionalmente.
- Acierto: verificar animación de escala + brillo. Fallo: verificar vaivén.
- Completar las 5 rondas: verificar transición de retorno a WorldMap.
- Iniciar minijuego, realizar 2 rondas, doble-toque en zona inferior derecha: verificar abandono inmediato y retorno a WorldMap.

## Dependencies
- SPRINT-069 (WorldMap verificado) — punto de partida para transición.
- Motor de reconocimiento existente (`RecognitionEngine` en backend) — ya funciona.
- `RecognitionGameScene` existente — se extiende, no se reemplaza.

## Agent Instruction
- No implementar Nubi completo en este sprint (eso es SPRINT-071).
- Usar placeholder invisible para la zona de doble-toque.
- Los sonidos pueden ser placeholders si los assets finales no están disponibles.
- Código, comentarios y nombres en inglés.
- Las animaciones deben respetar `prefers-reduced-motion: reduce` (simplificar a alpha fade).

## Notes
- Este sprint establece la base jugable probables.
- SPRINT-071 añadirá Nubi completo (dormido/activo, frases pre-generadas).
- SPRINT-072 añadirá pista visual (`hintActive`) y celebración elaborada.
- La barra de llenado escala hasta 15 rondas sin cambio de diseño.
