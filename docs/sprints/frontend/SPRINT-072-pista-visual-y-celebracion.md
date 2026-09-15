# Sprint 072 - frontend
# -----------------------------------------------

## Goal
Implementar la pista visual tras fallos repetidos (consumo de `hintActive`) y la celebración de cierre al completar todas las rondas. Cierra FEAT-013 en frontend.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-14):

- **FEAT-013 D7**: "Tras varios fallos de contenido, cada minijuego puede ofrecer una pista visual breve. El umbral se define por actividad y la pista no debe revelar necesariamente la solución ni presentar los intentos previos como fracaso."
- **FEAT-013 D9**: "Al completar todas las rondas, se muestra una celebración visual breve, sin premios gamificados, y se vuelve a WorldMap."
- **FEAT-013 D10**: "El niño puede repetir libremente un minijuego completado durante la sesión actual, pero ni sus resultados ni un eventual abandono de esa repetición se registran para tracking."
- **SPRINT-070**: Ya implementó la base jugable con feedback básico de acierto/fallo.
- **SPRINT-071**: Ya implementó Nubi en el minijuego con frases pre-generadas (incluye frase de pista y celebración).
- **Estado actual**: 
  - El backend ya envía `hintActive: true` en `recognitionState` del `GAME_ACTION_RESULT`.
  - `RecognitionGameScene` ya detecta `gameCompleted: true` y hace `scene.start('world-map')`.
- **Gap**:
  - No se consume `hintActive` para mostrar pista visual.
  - No hay celebración elaborada al completar (solo transición directa).
- **Pista visual**: El backend define el umbral de fallos para activar `hintActive`. El frontend solo debe renderizar la pista cuando el flag cambia a `true`.
- **Celebración**: Debe ser breve, sin premios gamificados, sin puntos, sin desbloqueos.

## Status
status: proposed
started_at:
closed_at:
verified_at:
blocked_by: SPRINT-071
waiting_for:

## Decisiones confirmadas (2026-09-14)

1. **Pista visual: resaltar opción correcta sin revelar solución.** Confirmado — la pista no debe ser explícita. Se propone un borde pulsante alrededor de la opción correcta, visible pero no obvio.
2. **Celebración: confeti suave o estrellas, sin puntuaciones.** Confirmado — la celebración es un cierre de actividad, no una recompensa. No se muestran puntos, estrellas numéricas, ni llamadas a repetir.
3. **Repetición libre: permitir reabrir el mismo minijuego.** Confirmado — el backend gestiona la detección de repeticiones (SPRINT-095). El frontend solo necesita permitir la interacción con el elemento de WorldMap que abre el minijuego, sin bloqueos.

## Diseño propuesto

### 1. Pista visual (`hintActive`)

**Consumo de `hintActive`**:
- En `RecognitionGameScene`, al recibir `GAME_ACTION_RESULT`, comparar `recognitionState.hintActive` con el valor anterior.
- Si cambia de `false` a `true`: activar pista visual.
- Si ya era `true`: mantener pista activa.

**Renderizado de la pista**:
- **Borde pulsante**: alrededor del `targetElementId` (opción correcta).
  - Grosor: 4px.
  - Color: amarillo suave (`#FFC107`).
  - Animación: pulso de alpha (0.4 → 1.0 → 0.4, 1.5s, `Sine.inOut`, loop infinito).
- **Duración**: la pista permanece visible hasta que el niño seleccione una opción o hasta que termine la ronda.
- **Accesibilidad**: no usar color como única señal (el borde pulsante es una señal adicional, no la única).

**Implementación**:
- Crear un `Graphics` object en `RecognitionGameScene` que dibuje el borde alrededor del elemento correcto.
- Actualizar la posición del borde cuando cambie el `targetElementId` (nueva ronda).
- Destruir el borde al seleccionar una opción (fin de ronda).

**Nota**: SPRINT-071 ya muestra una frase de pista de Nubi cuando `hintActive` cambia a `true`. La pista visual es complementaria.

### 2. Celebración de cierre

**Trigger**: al recibir `GAME_ACTION_RESULT` con `gameCompleted: true`.

**Secuencia**:
1. **Overlay de celebración** (1.5s):
   - Confeti suave: partículas de colores (amarillo, azul, verde) que caen lentamente desde la parte superior.
   - O estrellas: 3-5 estrellas que aparecen en posiciones aleatorias con scale-up (0→1, 300ms) y fade-out (500ms tras 1s).
   - **Decisión propuesta**: usar **estrellas** por simplicidad y consistencia con la estética infantil.
2. **Sonido opcional**: `celebration.wav` (solo si `audioGeneralEnabled && ttsEnabled`).
3. **Transición de salida**: fade a negro (400ms) → `scene.start('world-map')`.

**Implementación**:
- Crear método `playCelebration()` en `RecognitionGameScene`.
- Generar 3-5 estrellas en posiciones aleatorias (x: 20%-80% viewport, y: 20%-60% viewport).
- Animación: scale-up (0→1, 300ms, `Back.out`) → esperar 1s → fade-out (500ms).
- Tras 1.5s total, iniciar fade a negro y transición a WorldMap.

**No incluir**:
- Puntuaciones, números, "¡Ganaste!", "¡Nivel completado!".
- Desbloqueos, premios acumulativos, llamadas a repetir.
- Barras de progreso, logros, medallas.

### 3. Repetición libre

**Comportamiento**:
- Tras completar un minijuego y volver a WorldMap, el niño puede tocar el mismo elemento interactivo para reabrir el minijuego.
- No hay bloqueos, mensajes de "ya completado", ni indicadores de progreso.
- El backend detecta la repetición (SPRINT-095) y no registra tracking. El frontend es transparente a esto.

**Implementación**:
- No se requiere lógica adicional en frontend. El elemento interactivo de WorldMap sigue siendo interactivo tras el retorno.
- `RecognitionGameScene` se reinicia normalmente (nuevo `create()`).

### 4. Accesibilidad

**`prefers-reduced-motion: reduce`**:
- Pista visual: borde estático (sin pulso, solo alpha 0.7 fijo).
- Celebración: estrellas aparecen sin animación de scale-up (alpha directo 0→1), sin fade-out (desaparecen al cambiar de escena).

**Tamaño táctil**:
- La pista visual no afecta el tamaño táctil de los elementos.
- La celebración no bloquea la interacción (es un overlay no interactivo).

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|---|---|---|
| AsyncAPI `game-state-payload.yaml` | Sin cambios | `recognitionState.hintActive` ya existe |
| SPRINT-071 | Requerida | Nubi en minijuego ya implementado |
| Assets: `celebration.wav` | Requerido | Sonido de celebración (puede ser placeholder) |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | La pista visual puede ser demasiado evidente y revelar la solución | MEDIA | El borde pulsante solo resalta la opción correcta, pero no la señala explícitamente (no hay flecha, no hay texto). Aceptable como ayuda visual. |
| R2 | La celebración puede parecer una recompensa gamificada | BAJA | Diseño: estrellas suaves, sin puntos, sin "¡Ganaste!", sin desbloqueos. Es un cierre de actividad, no una recompensa. |
| R3 | La pista visual puede no ser comprensible sin contexto | BAJA | Aceptable: el borde pulsante atrae la atención hacia la opción correcta. Combinado con la frase de Nubi (SPRINT-071), es suficiente. |
| R4 | Las estrellas de celebración pueden tapar los elementos del juego | BAJA | Las estrellas aparecen en posiciones aleatorias lejos de los elementos de juego (zona central). Tras 1.5s, desaparecen y se transiciona a WorldMap. |

## Handoffs a otras capas (no diseñados en detalle aquí)

- **Backend**: sin cambios. El flag `hintActive` ya se envía.
- **Agents**: no aplica. Las frases de Nubi ya se implementaron en SPRINT-071.
- **Contenido**: asset de sonido `celebration.wav` puede ser placeholder inicial.

## Tareas del sprint

### Pista visual
- [ ] Consumir `hintActive` del `recognitionState` en `RecognitionGameScene`.
- [ ] Detectar cambio de `false` a `true` en `hintActive`.
- [ ] Crear borde pulsante alrededor del `targetElementId` (opción correcta).
- [ ] Animación de pulso: alpha 0.4 → 1.0 → 0.4, 1.5s, `Sine.inOut`, loop.
- [ ] Actualizar posición del borde cuando cambie `targetElementId` (nueva ronda).
- [ ] Destruir borde al seleccionar una opción (fin de ronda).
- [ ] Respetar `prefers-reduced-motion: reduce`: borde estático (alpha 0.7 fijo).

### Celebración
- [ ] Crear método `playCelebration()` en `RecognitionGameScene`.
- [ ] Generar 3-5 estrellas en posiciones aleatorias (x: 20%-80%, y: 20%-60%).
- [ ] Animación de estrellas: scale-up (0→1, 300ms, `Back.out`) → esperar 1s → fade-out (500ms).
- [ ] Añadir sonido opcional: `celebration.wav` (gateado por preferencias de audio).
- [ ] Tras 1.5s total, iniciar fade a negro y transición a WorldMap.
- [ ] Respetar `prefers-reduced-motion: reduce`: estrellas sin scale-up, sin fade-out.

### Repetición libre
- [ ] Verificar que el elemento interactivo de WorldMap sigue siendo interactivo tras completar minijuego.
- [ ] Verificar que se puede reabrir el mismo minijuego sin bloqueos.

### Pruebas
- [ ] Verificar que la pista visual aparece tras fallos repetidos (cuando `hintActive: true`).
- [ ] Verificar que la pista no revela explícitamente la solución.
- [ ] Verificar que la celebración aparece al completar todas las rondas.
- [ ] Verificar que la celebración no muestra puntuaciones ni premios.
- [ ] Verificar que se puede repetir el minijuego libremente.
- [ ] Verificar que las animaciones respetan `prefers-reduced-motion: reduce`.

## Manual Tests
- Con backend levantado y un niño con sesión activa: iniciar minijuego, fallar repetidamente hasta que aparezca pista (`hintActive: true`). Verificar borde pulsante alrededor de la opción correcta.
- Completar minijuego: verificar celebración con estrellas y sonido opcional.
- Volver a WorldMap, tocar el mismo elemento interactivo: verificar que se puede reabrir el minijuego sin bloqueos.
- Repetir minijuego: verificar que no se muestran métricas ni historial.
- Con `prefers-reduced-motion: reduce`: verificar que la pista es estática y las estrellas aparecen sin animación.

## Dependencies
- SPRINT-071 (Nubi en minijuego) — punto de partida.
- Motor de reconocimiento existente (backend) — `hintActive` ya se envía.
- `RecognitionGameScene` existente — se extiende.

## Agent Instruction
- La pista visual es complementaria a la frase de Nubi (SPRINT-071). No duplicar información.
- La celebración no debe incluir elementos gamificados (puntos, desbloqueos, llamadas a repetir).
- Código, comentarios y nombres en inglés.
- Las animaciones deben respetar `prefers-reduced-motion: reduce`.

## Notes
- Este sprint cierra FEAT-013 en frontend.
- La pista visual y la celebración son los últimos elementos de la experiencia del minijuego.
- No se implementa dashboard parental de abandonos (diferido a feature de dashboard).
