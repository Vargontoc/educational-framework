# Sprint 075 - frontend
# -----------------------------------------------

## Goal
Implementar la accesibilidad cromática para la categoría COLOR: cuando `nonChromaticKeyRequired` es true (perfil con preferencia de visión de color configurada), añadir patrones/texturas no cromáticas a los distractores para que el minijuego no dependa únicamente del matiz.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-15):

- **FEAT-014 §2 D9**: "Cuando el perfil tenga configurada una preferencia de visión de color, las rondas de colores añaden un diferenciador no cromático a los distractores."
- **FEAT-014 §4 R8**: "En color con preferencia visual configurada, los distractores deben distinguirse además por patrón o textura."
- **FEAT-014 §5 C8**: "Con una preferencia de visión de color configurada, una ronda de color presenta al menos una clave visual adicional al matiz para cada distractor relevante."
- **ADR-023**: Selector visual de accesibilidad cromática. Exige claves no cromáticas para que los minijuegos de color no dependan únicamente del matiz.
- **ADR-028 §4**: "Los distractores necesitan una clave adicional al matiz —como patrón o textura—."
- **SPRINT-030 (frontend)**: Ya implementó el selector visual de accesibilidad cromática en el panel parental.
- **SPRINT-074**: Ya almacena `nonChromaticKeyRequired` en campo de instancia.
- **Estado actual**:
  - `simulateColorVision.ts` existe en `utils/` pero no se integra en el minijuego.
  - `nonChromaticKeyRequired` se almacena pero no se consume.
  - No hay patrones/texturas superpuestos a las opciones de color.
- **Gap crítico**:
  - Un niño con daltonismo no puede distinguir los colores si solo se usa el matiz.
  - ADR-023 exige diferenciadores no cromáticos, pero no se han implementado en el minijuego.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-074
waiting_for:

## Decisiones confirmadas (2026-09-15)

1. **Patrones solo con preferencia visual configurada.** Confirmado — los patrones/texturas se muestran solo cuando `nonChromaticKeyRequired` es true (backend lo calcula a partir de `colorVisionMode` del perfil). Sin preferencia, no se muestran patrones adicionales.
2. **Patrones como superposición, no sustitución.** Confirmado — el color sigue visible; el patrón es una clave adicional (ej: círculo con líneas diagonales, cuadrado con puntos). No sustituye el matiz.
3. **Patrones simples y distinguibles.** Confirmado — líneas diagonales, puntos, cuadrículas, ondas. Máximo 4-5 patrones diferentes para no saturar visualmente.

## Diseño propuesto

### 1. Catálogo de patrones no cromáticos

**Patrones disponibles** (implementados como `Graphics` de Phaser):
1. **Líneas diagonales** (`pattern-diagonal`): 3 líneas diagonales de arriba-izquierda a abajo-derecha.
2. **Puntos** (`pattern-dots`): 4 puntos distribuidos en cuadrícula 2x2.
3. **Cuadrículas** (`pattern-grid`): líneas horizontales y verticales formando cuadrícula.
4. **Ondas** (`pattern-waves`): 2 ondas sinusoidales horizontales.
5. **Cruz** (`pattern-cross`): líneas horizontal y vertical cruzadas en el centro.

**Asignación de patrones**:
- Cada elemento de color recibe un patrón único dentro de la ronda.
- Si hay más elementos que patrones, se repiten (pero se intenta evitar con el contenido de backend).
- El patrón se asigna por índice (primer elemento → patrón 1, segundo → patrón 2, etc.).

### 2. Renderizado de patrones

**Método `renderNonChromaticPatterns(optionImages)`**:
- Solo se ejecuta si `nonChromaticKeyRequired` es true Y `recognitionCategory` es `'COLOR'`.
- Para cada opción táctil, crear un `Graphics` superpuesto con el patrón asignado.
- El `Graphics` se posiciona sobre el centro de la imagen, con el mismo tamaño.
- Color del patrón: negro (`#000000`, alpha 0.4) para contraste sobre cualquier fondo de color.
- Grosor de líneas: 3px.
- Los patrones no bloquean el toque (se renderizan con `setDepth` inferior al hit area).

**Ciclo de vida**:
- Crear patrones al renderizar opciones (`renderElements()`).
- Destruir patrones al cambiar de ronda (nuevo `GAME_ACTION_RESULT` con `CORRECT`).
- Destruir patrones en `cleanup()`.

### 3. Integración con `simulateColorVision.ts`

**Uso opcional**:
- `simulateColorVision.ts` ya existe como utilidad para simular daltonismo en el panel parental.
- En el minijuego, no se aplica la simulación (el niño ve los colores reales).
- Los patrones son la clave no cromática que permite distinguir sin depender del matiz.

**Nota**: La simulación es para el adulto (panel parental); el niño ve los colores reales con patrones adicionales si tiene preferencia configurada.

### 4. Accesibilidad

**`prefers-reduced-motion: reduce`**:
- Los patrones son estáticos (sin animación), por lo que no hay impacto.

**Sin color como única señal**:
- Los patrones son la clave no cromática. El color es aditivo.
- Un niño con acromatopsia (visión en escala de grises) puede distinguir los elementos por patrón.

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|----------|--------|-------|
| `nonChromaticKeyRequired` en `RecognitionState` | Ya añadido (SPRINT-074) | Se consume en este sprint |
| `recognitionCategory` en `RecognitionState` | Ya existe | Se usa para verificar si es COLOR |
| SPRINT-074 (ladder visual) | Requerida | `nonChromaticKeyRequired` ya almacenado |
| SPRINT-073 (categorías extendidas) | Requerida | COLOR ya soportado |
| Backend SPRINT-100 (contenido COLOR) | Requerida | Elementos de COLOR con assets |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Los patrones son poco claros o confusos | MEDIA | Validar con usuarios reales. Patrones simples y distinguibles. |
| R2 | Los patrones desplazan el reconocimiento del color | MEDIA | Los patrones son superposición (alpha 0.4), no sustitución. El color sigue visible. |
| R3 | Demasiados patrones saturan visualmente | BAJA | Máximo 4-5 patrones. Si hay más elementos, se repiten. |

## Tareas del sprint

### Catálogo de patrones
- [ ] Crear método `createPatternGraphics(patternType, width, height)` que genera un `Graphics` con el patrón especificado.
- [ ] Implementar patrón `diagonal` (líneas diagonales).
- [ ] Implementar patrón `dots` (puntos en cuadrícula).
- [ ] Implementar patrón `grid` (cuadrícula).
- [ ] Implementar patrón `waves` (ondas sinusoidales).
- [ ] Implementar patrón `cross` (cruz central).

### Renderizado de patrones
- [ ] Crear método `renderNonChromaticPatterns(optionImages)` que asigna y renderiza patrones.
- [ ] Solo ejecutar si `nonChromaticKeyRequired` es true Y `recognitionCategory` es `'COLOR'`.
- [ ] Asignar patrón único por opción (por índice).
- [ ] Superponer `Graphics` sobre cada opción con color negro (alpha 0.4) y grosor 3px.
- [ ] Los patrones no bloquean el toque (`setDepth` inferior al hit area).

### Ciclo de vida
- [ ] Crear patrones al renderizar opciones (en `renderElements()` o tras él).
- [ ] Destruir patrones al cambiar de ronda (nuevo `GAME_ACTION_RESULT` con `CORRECT`).
- [ ] Destruir patrones en `cleanup()`.

### Pruebas
- [ ] Test: con `nonChromaticKeyRequired=true` y categoría COLOR, se muestran patrones.
- [ ] Test: con `nonChromaticKeyRequired=false`, no se muestran patrones.
- [ ] Test: con categoría no-COLOR, no se muestran patrones (aunque `nonChromaticKeyRequired=true`).
- [ ] Test: los patrones no bloquean el toque (las opciones siguen siendo táctiles).
- [ ] Test: los patrones se destruyen al cambiar de ronda.

## Manual Tests
- Configurar perfil infantil con `colorVisionMode=DEUTERANOPIA`.
- Iniciar minijuego de COLOR: verificar que cada opción muestra un patrón diferente superpuesto.
- Verificar que el color sigue visible bajo el patrón.
- Verificar que las opciones siguen siendo táctiles (el patrón no bloquea el toque).
- Completar una ronda: verificar que los patrones se destruyen y se crean nuevos para la siguiente ronda.
- Cambiar a perfil sin `colorVisionMode`: verificar que no se muestran patrones.
- Iniciar minijuego de LETTER: verificar que no se muestran patrones (aunque el perfil tenga `colorVisionMode`).

## Dependencies
- SPRINT-074 (ladder visual) — `nonChromaticKeyRequired` ya almacenado.
- SPRINT-073 (categorías extendidas) — COLOR ya soportado.
- Backend SPRINT-100 (contenido COLOR) — elementos de COLOR con assets.

## Agent Instruction
- No modificar la lógica de feedback visual (SPRINT-070), Nubi (SPRINT-071), pista/hint (SPRINT-072), celebración (SPRINT-072) ni ladder visual (SPRINT-074).
- Los patrones son estáticos (sin animación) para no interferir con `prefers-reduced-motion`.
- Código, comentarios y nombres en inglés.
- Los patrones deben ser simples y distinguibles para niños de 3-4 años.

## Notes
- Este sprint cierra la accesibilidad cromática del minijuego de reconocimiento.
- SPRINT-076 externalizará el tamaño táctil a configuración dinámica.
- SPRINT-077 verificará la accesibilidad completa y añadirá pruebas E2E.
- `simulateColorVision.ts` no se modifica; es una utilidad para el panel parental.
