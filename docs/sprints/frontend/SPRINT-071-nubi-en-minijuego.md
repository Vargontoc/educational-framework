# Sprint 071 - frontend
# -----------------------------------------------

## Goal
Integrar Nubi en el minijuego: estado dormido (NPC desactivado) o activo (NPC activado) con frases pre-generadas en momentos clave, y refinar el mecanismo de abandono por doble-toque sobre el personaje.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-14):

- **FEAT-013 D2**: "Si el NPC está activo, Nubi se sitúa en la esquina inferior derecha y puede mostrar una nube de diálogo breve y contextual."
- **FEAT-013 D3**: "Si el NPC está desactivado, se aplica ADR-027: Nubi aparece solo dormido, con nube de diálogo en estado de sueño, como referencia visual de salida; no habla, se mueve ni acompaña el juego."
- **FEAT-013 D4**: "Dos toques consecutivos sobre Nubi abandonan inmediatamente el minijuego."
- **ADR-027**: Nubi dormido como salida de minijuegos. Aceptado 2026-09-14.
- **SPRINT-070**: Ya implementó la base jugable con placeholder invisible para doble-toque.
- **Estado actual**: `NubiLayer` existe en WorldMapScene con animación idle (respiración suave). No hay Nubi en RecognitionGameScene.
- **Gap**: Falta Nubi en el minijuego con dos estados (activo/dormido) y frases pre-generadas.
- **Sprite de Nubi dormido**: **Decisión confirmada (2026-09-14)**: usar sprite existente temporalmente, sustituir cuando esté el asset dedicado.
- **Frases pre-generadas**: **Decisión confirmada (2026-09-14)**: solo momentos clave (bienvenida, pista, celebración), con patrón antirepetición. Sin conversación libre.
- **Agentes**: No requiere análisis. Las frases se generan durante el desarrollo con patrón antirepetición (confirmado en análisis backend).

## Status
status: proposed
started_at:
closed_at:
verified_at:
blocked_by: SPRINT-070
waiting_for:

## Decisiones confirmadas (2026-09-14)

1. **Sprite de Nubi dormido: usar existente temporalmente.** Confirmado — se usará el sprite actual (`greetings.png` o similar) hasta que esté el asset dedicado. Se documenta como deuda técnica.
2. **Frases pre-generadas en momentos clave.** Confirmado — Nubi activo muestra frases en: bienvenida al minijuego, pista tras fallos repetidos, celebración al completar. Sin conversación libre.
3. **Patrón antirepetición para frases.** Confirmado — no repetir la misma frase dentro de la misma sesión de juego. Implementación local en frontend (sin llamada a backend).
4. **Nubi dormido: solo visual, sin interacción.** Confirmado — cuando NPC está desactivado, Nubi aparece dormido con nube de sueño (ZZZ). No habla, no se mueve, no acompaña. Solo sirve como referencia visual de salida.

## Diseño propuesto

### 1. Componente `MinigameNubiLayer`

**Nuevo componente** en `game/worldmap/layers/` (reutiliza patrón de `NubiLayer` de WorldMap).

**Posicionamiento**:
- Esquina inferior derecha, 20px desde bordes derecho e inferior.
- Tamaño: 100x100px (ajustado para táctil).
- Fijo en pantalla (no se mueve con scroll, aunque el minijuego no tiene scroll).

**Estados**:
- **NPC activo**:
  - Sprite: animación idle (respiración suave, similar a `NubiLayer` de WorldMap).
  - Nube de diálogo: aparece brevemente (3s) con frase pre-generada en momentos clave.
  - Posición de nube: encima de Nubi, con puntero hacia el personaje.
- **NPC desactivado**:
  - Sprite: estado dormido (usar sprite existente temporalmente, con filtro visual si es necesario: ojos cerrados, postura diferente).
  - Nube de sueño: icono ZZZ estático (sin animación de flote para evitar distracción).
  - Sin interacción más allá del doble-toque.

**Visibilidad**:
- Controlada por `registry.get('npcEnabled')` (igual que `NubiLayer` de WorldMap).
- Escucha evento `'npc-state-changed'` para cambiar estado en tiempo real.

### 2. Frases pre-generadas (NPC activo)

**Momentos clave**:
1. **Bienvenida**: al entrar al minijuego (tras `GAME_READY`).
   - Ejemplos: "¡Vamos a jugar!", "¿Listo?", "¡A ver qué encuentras!"
2. **Pista**: tras fallos repetidos (cuando `hintActive` cambia a `true`).
   - Ejemplos: "Mira bien las opciones", "Fíjate en la forma", "Tómate tu tiempo"
3. **Celebración**: al completar todas las rondas (cuando `gameCompleted: true`).
   - Ejemplos: "¡Lo lograste!", "¡Muy bien!", "¡Buen trabajo!"

**Patrón antirepetición**:
- Mantener `Set<string>` de frases ya mostradas en la sesión actual.
- Al seleccionar frase, excluir las ya usadas.
- Si se agotan las frases del pool, no mostrar nube (silencio).
- Reset del set al salir del minijuego.

**Implementación**:
- Pool de frases hardcodeado inicialmente (3-5 por momento clave).
- Método `getRandomPhrase(moment: 'welcome' | 'hint' | 'celebration'): string | null`.
- Nube de diálogo: fondo blanco con borde redondeado, texto centrado, aparece con fade-in (200ms), desaparece con fade-out (200ms) tras 3s.

### 3. Doble-toque refinado

**Cambio respecto a SPRINT-070**:
- La zona invisible se reemplaza por el sprite de Nubi (activo o dormido).
- El doble-toque se detecta sobre el sprite de `MinigameNubiLayer`.
- Ventana de tiempo: 2000ms (igual que SPRINT-070).
- Acción: enviar `game_abandon` → transición de salida.

**Comportamiento adicional**:
- Si NPC está activo y se toca Nubi una vez (sin segundo toque): Nubi puede mostrar una frase corta de saludo (opcional, no obligatorio).
- Si NPC está desactivado: Nubi dormido no reacciona al primer toque, solo espera el segundo.

### 4. Integración con `RecognitionGameScene`

**Cambios en `RecognitionGameScene`**:
- Crear instancia de `MinigameNubiLayer` en `create()`.
- Pasar referencia a `MinigameNubiLayer` para que pueda mostrar frases en momentos clave:
  - Tras `GAME_READY`: llamar a `nubiLayer.showPhrase('welcome')`.
  - Tras detectar `hintActive: true`: llamar a `nubiLayer.showPhrase('hint')`.
  - Tras detectar `gameCompleted: true`: llamar a `nubiLayer.showPhrase('celebration')`.
- Escuchar evento `'minigame-nubi-double-tap'` emitido por `MinigameNubiLayer` → enviar `game_abandon`.

### 5. Accesibilidad

**`prefers-reduced-motion: reduce`**:
- Animación de respiración de Nubi: desactivar (sprite estático).
- Nube de diálogo: fade-in/fade-out simplificado a alpha directo (sin transición).
- ZZZ de Nubi dormido: estático (sin flote).

**Tamaño táctil**:
- Nubi: 100x100px (cumple mínimo de 80x80px).
- Zona de toque: misma que el sprite (no ampliar para evitar abandonos accidentales).

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|---|---|---|
| AsyncAPI | Sin cambios | No se modifican mensajes |
| SPRINT-070 | Requerida | Base jugable ya implementada |
| Assets: sprite de Nubi dormido | Deuda técnica | Usar existente temporalmente |
| Frases pre-generadas | Contenido | Pool hardcodeado inicialmente (3-5 por momento) |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El sprite existente puede no ser visualmente reconocible como "dormido" | MEDIA | Aceptado como deuda técnica. Se documenta la necesidad del asset dedicado. Si es necesario, aplicar filtro visual (tinte azulado, ojos cerrados superpuestos). |
| R2 | Las frases pre-generadas pueden sonar repetitivas si el pool es pequeño | BAJA | Patrón antirepetición asegura que no se repiten en la misma sesión. Pool inicial de 3-5 frases por momento es suficiente para sesiones cortas. |
| R3 | El doble-toque sobre Nubi puede confundirse con un solo toque accidental | BAJA | Ventana de 2000ms es suficiente para distinguir intención. Aceptado por producto (FEAT-013): minimizar fricción > prevenir error. |
| R4 | Nubi activo puede distraer del juego | BAJA | Las frases son breves (3s) y no bloquean la interacción. Nubi está en esquina inferior derecha, lejos de los elementos de juego. |

## Handoffs a otras capas (no diseñados en detalle aquí)

- **Backend**: sin cambios. Las frases pre-generadas son locales al frontend.
- **Agents**: no requiere análisis. Las frases se generan durante el desarrollo.
- **Contenido**: asset de Nubi dormido pendiente. Frases pre-generadas pueden ser placeholders iniciales.

## Tareas del sprint

### Componente MinigameNubiLayer
- [ ] Crear `MinigameNubiLayer` en `game/worldmap/layers/`.
- [ ] Implementar posicionamiento en esquina inferior derecha (100x100px).
- [ ] Implementar dos estados: activo (idle animation) y dormido (sprite estático).
- [ ] Escuchar `'npc-state-changed'` para cambiar estado en tiempo real.
- [ ] Implementar detección de doble-toque (ventana 2000ms) → emitir `'minigame-nubi-double-tap'`.

### Frases pre-generadas
- [ ] Crear pool de frases hardcodeado (3-5 por momento: bienvenida, pista, celebración).
- [ ] Implementar método `getRandomPhrase(moment)` con patrón antirepetición.
- [ ] Implementar nube de diálogo: fondo blanco, borde redondeado, texto centrado.
- [ ] Implementar aparición/desaparición de nube (fade-in 200ms, visible 3s, fade-out 200ms).
- [ ] Integrar con `RecognitionGameScene`: mostrar frases en momentos clave.

### Integración
- [ ] Reemplazar zona invisible de SPRINT-070 con `MinigameNubiLayer`.
- [ ] Escuchar `'minigame-nubi-double-tap'` en `RecognitionGameScene` → enviar `game_abandon`.
- [ ] Llamar a `nubiLayer.showPhrase('welcome')` tras `GAME_READY`.
- [ ] Llamar a `nubiLayer.showPhrase('hint')` al detectar `hintActive: true`.
- [ ] Llamar a `nubiLayer.showPhrase('celebration')` al detectar `gameCompleted: true`.

### Accesibilidad
- [ ] Verificar que `prefers-reduced-motion: reduce` desactiva animaciones de Nubi.
- [ ] Verificar que el tamaño táctil de Nubi cumple 80x80px mínimo.
- [ ] Verificar que la nube de diálogo es legible (contraste, tamaño de fuente).

### Pruebas
- [ ] Verificar que Nubi aparece en esquina inferior derecha durante el minijuego.
- [ ] Verificar que con NPC activo: animación idle + frases en momentos clave.
- [ ] Verificar que con NPC desactivado: solo Nubi dormido con ZZZ.
- [ ] Verificar que el doble-toque abandona inmediatamente sin confirmación.
- [ ] Verificar que las frases no se repiten dentro de la misma sesión.

## Manual Tests
- Con NPC activado: iniciar minijuego, verificar que Nubi aparece con animación idle y muestra frase de bienvenida.
- Jugar hasta que aparezca pista (`hintActive: true`): verificar que Nubi muestra frase de pista.
- Completar minijuego: verificar que Nubi muestra frase de celebración.
- Repetir minijuego en la misma sesión: verificar que las frases no se repiten.
- Con NPC desactivado: iniciar minijuego, verificar que Nubi aparece dormido con ZZZ.
- Doble-toque sobre Nubi (ambos estados): verificar abandono inmediato.

## Dependencies
- SPRINT-070 (base jugable) — punto de partida.
- `NubiLayer` existente en WorldMap — patrón a reutilizar.
- `RecognitionGameScene` existente — se extiende.

## Agent Instruction
- Usar sprite existente temporalmente para Nubi dormido. Documentar como deuda técnica.
- Las frases pre-generadas son hardcodeadas inicialmente. No implementar llamada a backend ni agentes.
- Código, comentarios y nombres en inglés.
- Las animaciones deben respetar `prefers-reduced-motion: reduce`.

## Notes
- Este sprint cierra la integración de Nubi en el minijuego.
- SPRINT-072 añadirá pista visual (`hintActive`) y celebración elaborada.
- El asset dedicado de Nubi dormido queda como deuda técnica pendiente de contenido.
