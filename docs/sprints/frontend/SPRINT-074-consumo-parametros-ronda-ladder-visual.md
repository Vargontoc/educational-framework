# Sprint 074 - frontend
# -----------------------------------------------

## Goal
Consumir los parámetros de ronda enviados por backend (`guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`) y aplicar la ladder de dificultad visual conforme a ADR-028: cromo guía en EASY, espera antes del toque en EASY/MEDIUM, y preparación para accesibilidad cromática.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-15):

- **FEAT-014 §2 D4**: "EASY con 2 opciones, distractores lejanos, cromo guía y espera corta; MEDIUM con 3 opciones, distractores de la misma categoría, sin cromo y espera media; HARD con 3–4 opciones, distractores de contorno o grafía similar, sin cromo y espera nula o mínima."
- **FEAT-014 §2 D5**: "La espera de EASY y MEDIUM solo precede a la disponibilidad del toque y no puede generar un fallo, una pérdida ni una presión temporal."
- **ADR-028 §4**: Ladder de dificultad confirmada con cromo guía solo en EASY, espera corta/media, estímulo siempre presente.
- **SPRINT-073**: Ya implementó el layout de dos zonas (estímulo arriba, opciones abajo) y las 5 categorías.
- **Estado actual**:
  - `RecognitionState` en `GameEvent.ts` no incluye `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`.
  - No hay lógica de espera antes de habilitar el toque.
  - No hay cromo guía (distinto del hint de SPRINT-072).
  - Backend SPRINT-102 creará los contratos `round-ready-event.v1.yaml` con estos campos.
- **Gap crítico**:
  - Frontend no consume los parámetros de ronda que definen la ladder visual.
  - Sin espera antes del toque, la experiencia es la misma en EASY y HARD.
  - Sin cromo guía, EASY no se diferencia visualmente de MEDIUM/HARD.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
blocked_by: SPRINT-073
waiting_for:
reviewed_at: 2026-09-15
review_verdict: APPROVED
review_notes: Todas las incidencias resueltas. Tests verifican comportamiento real. Sin regresiones.

## Decisiones confirmadas (2026-09-15)

1. **Cromo guía en EASY: halo suave alrededor de la zona de opciones.** Confirmado — no revela la respuesta correcta, solo señala la zona donde tocar. Distinto del hint de SPRINT-072 (que sí señala la opción correcta).
2. **Espera antes del toque: bloqueo temporal sin cronómetro visible.** Confirmado — las opciones aparecen atenuadas (alpha 0.5) durante `touchEnableDelayMs`, luego se iluminan (alpha 1.0) y se habilitan. Sin números, sin barra de progreso, sin indicación de tiempo restante.
3. **`nonChromaticKeyRequired` se almacena pero no se consume en este sprint.** Confirmado — su implementación es el alcance de SPRINT-075.

## Diseño propuesto

### 1. Extensión de `RecognitionState`

**Nuevos campos en `GameEvent.ts`**:
```typescript
export class RecognitionState {
    elements: RecognitionElement[] = []
    recognitionCategory?: RECOGNITION_TYPE
    roundIndex: number = 0
    totalRounds: number = 0
    hintActive: boolean = false
    targetElementId: string = ''
    optionIds: string[] = []
    guideChromEnabled: boolean = false
    touchEnableDelayMs: number = 0
    nonChromaticKeyRequired: boolean = false
}
```

### 2. Espera antes del toque (`touchEnableDelayMs`)

**Lógica en `RecognitionGameScene`**:
- Al recibir `GAME_READY` o `GAME_ACTION_RESULT` con `resultType: 'CORRECT'` (nueva ronda):
  1. Renderizar opciones con `alpha: 0.5` y `disableInteractive()`.
  2. Iniciar temporizador de `touchEnableDelayMs`.
  3. Al expirar: `alpha: 1.0` y `setInteractive()` en cada opción.
  4. Habilitar `blockActions = false` (permitir toque).
- Si `touchEnableDelayMs` es 0: las opciones se habilitan inmediatamente (comportamiento actual).
- **Sin cronómetro visible**: el niño ve las opciones atenuadas y luego iluminadas, sin saber cuánto tiempo falta.

**Implementación**:
- Método `applyTouchEnableDelay(delayMs, options)` que gestiona la transición.
- Respetar `prefers-reduced-motion: reduce`: si está activo, la transición de alpha es instantánea (sin tween).

### 3. Cromo guía (`guideChromEnabled`)

**Renderizado**:
- Si `guideChromEnabled` es true: dibujar un halo suave (elipse con blur) alrededor de la zona de opciones.
- Color: blanco suave (`#FFFFFF`, alpha 0.2).
- Tamaño: engloba todas las opciones con padding de 20px.
- Animación: pulso lento (alpha 0.15 → 0.25 → 0.15, 2s, loop).
- **No señala la opción correcta**: solo indica la zona general de interacción.

**Diferencia con el hint de SPRINT-072**:
- El cromo guía es una señal ambiental de "aquí es donde tocas".
- El hint es una señal específica de "esta es la opción correcta".
- Ambos pueden coexistir: cromo guía en zona de opciones + hint sobre la opción correcta.

**Implementación**:
- Método `renderGuideChrom()` que dibuja el halo.
- Destruir el halo al cambiar de ronda (nuevo `GAME_ACTION_RESULT` con `CORRECT`).

### 4. Almacenamiento de `nonChromaticKeyRequired`

- Al recibir `GAME_READY` o `GAME_ACTION_RESULT`, almacenar `nonChromaticKeyRequired` en campo de instancia.
- No se consume en este sprint (SPRINT-075 lo usará).

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|----------|--------|-------|
| `RecognitionState` en `GameEvent.ts` | A extender | Añadir `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired` |
| `round-ready-event.v1.yaml` | Pendiente (Backend SPRINT-102) | Define los nuevos campos |
| SPRINT-073 (layout dos zonas) | Requerida | El cromo guía se dibuja alrededor de la zona de opciones |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | La espera se percibe como bloqueo si es demasiado larga | MEDIA | Los valores de backend son 500ms (EASY) y 800ms (MEDIUM). Validar en dispositivo real. Si se percibe lento, reducir. |
| R2 | El cromo guía distrae más que ayuda | BAJA | Alpha bajo (0.2), pulso lento. Si distrae, eliminar en iteración futura. |
| R3 | Los contratos backend no están disponibles a tiempo | MEDIA | Definir los campos en `GameEvent.ts` antes de que backend los envíe. El código frontend puede desarrollarse con valores hardcodeados para testing. |

## Tareas del sprint

### Extensión de contratos
- [x] Añadir `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired` a `RecognitionState` en `GameEvent.ts`.
- [x] Añadir campo de instancia `nonChromaticKeyRequired` en `RecognitionGameScene`.

### Espera antes del toque
- [x] Implementar método `applyTouchEnableDelay(delayMs, optionImages)`.
- [x] Al recibir `GAME_READY`: aplicar espera si `touchEnableDelayMs > 0`.
- [x] Al recibir `GAME_ACTION_RESULT` con `CORRECT` (nueva ronda): aplicar espera si `touchEnableDelayMs > 0`.
- [x] Las opciones aparecen con `alpha: 0.5` y `disableInteractive()` durante la espera.
- [x] Al expirar: `alpha: 1.0` y `setInteractive()`.
- [x] Respetar `prefers-reduced-motion: reduce` (transición instantánea).

### Cromo guía
- [x] Implementar método `renderGuideChrom()` que dibuja halo alrededor de zona de opciones.
- [x] Si `guideChromEnabled` es true: mostrar halo con pulso lento.
- [x] Si `guideChromEnabled` es false: no mostrar halo.
- [x] Destruir halo al cambiar de ronda.
- [x] Respetar `prefers-reduced-motion: reduce` (halo estático, sin pulso).

### Almacenamiento de `nonChromaticKeyRequired`
- [x] Almacenar `nonChromaticKeyRequired` del `RecognitionState` en campo de instancia.
- [x] No consumir en este sprint.

### Pruebas
- [x] Test: `touchEnableDelayMs > 0` bloquea el toque durante el tiempo configurado.
- [x] Test: `touchEnableDelayMs = 0` habilita el toque inmediatamente.
- [x] Test: `guideChromEnabled = true` muestra halo alrededor de opciones.
- [x] Test: `guideChromEnabled = false` no muestra halo.
- [x] Test: `nonChromaticKeyRequired` se almacena correctamente.
- [x] Test: `prefers-reduced-motion: reduce` simplifica animaciones de espera y cromo.

## Manual Tests
- Iniciar minijuego en EASY: verificar que las opciones aparecen atenuadas y se iluminan tras ~500ms.
- Verificar que el cromo guía (halo suave) aparece alrededor de las opciones en EASY.
- Iniciar minijuego en HARD: verificar que las opciones se habilitan inmediatamente y no hay cromo guía.
- Verificar que el cromo guía no señala la opción correcta (solo la zona general).
- Verificar que el hint de SPRINT-072 sigue funcionando correctamente (señala la opción correcta).
- Verificar que ambos (cromo guía + hint) pueden coexistir sin conflicto visual.

## Dependencies
- SPRINT-073 (layout dos zonas) — el cromo guía se dibuja alrededor de la zona de opciones.
- Backend SPRINT-102 (contratos round-ready-event) — define los nuevos campos.

## Agent Instruction
- No modificar la lógica de feedback visual (SPRINT-070), Nubi (SPRINT-071), pista/hint (SPRINT-072) ni celebración (SPRINT-072).
- El cromo guía es distinto del hint: el cromo guía señala la zona, el hint señala la opción correcta.
- Código, comentarios y nombres en inglés.
- Las animaciones deben respetar `prefers-reduced-motion: reduce`.

## Notes
- Este sprint aplica la ladder visual de ADR-028.
- SPRINT-075 añadirá la accesibilidad cromática para COLOR usando `nonChromaticKeyRequired`.
- SPRINT-076 externalizará el tamaño táctil a configuración dinámica.
- Los valores de `touchEnableDelayMs` vienen de backend; frontend no los calcula.

## Review Report (2026-09-15)

**Verdict:** `CHANGES_REQUIRED`

**Reviewer:** reviewer-frontend (automated review)

### Summary

La implementación funcional del sprint es correcta y cumple con los requisitos de FEAT-014 y ADR-028. Sin embargo, existen problemas de completitud y calidad que requieren corrección.

### Completitud

#### Tareas de implementación ✅ (13/13)

**Extensión de contratos:**
- ✅ `RecognitionState` extendido con `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`
- ✅ Campo de instancia `_nonChromaticKeyRequired` en `RecognitionGameScene`

**Espera antes del toque:**
- ✅ Método `applyTouchEnableDelay()` implementado
- ✅ Aplica espera en `GAME_READY` y `GAME_ACTION_RESULT` con `CORRECT`
- ✅ Opciones con `alpha: 0.5` y `disableInteractive()` durante espera
- ✅ Al expirar: `alpha: 1.0` y `setInteractive()`
- ✅ Respeta `prefers-reduced-motion: reduce`

**Cromo guía:**
- ✅ Método `renderGuideChrom()` implementado
- ✅ Muestra halo con pulso lento si `guideChromEnabled` es true
- ✅ Destruye halo al cambiar de ronda
- ✅ Respeta `prefers-reduced-motion: reduce` (halo estático)

**Almacenamiento de `nonChromaticKeyRequired`:**
- ✅ Almacena del `RecognitionState` en `GAME_READY` y `GAME_ACTION_RESULT`
- ✅ No consume en este sprint (preparado para SPRINT-075)

#### Tareas de pruebas ❌ (5/6)

- ✅ Test: `touchEnableDelayMs > 0` bloquea el toque
- ✅ Test: `touchEnableDelayMs = 0` habilita inmediatamente
- ✅ Test: `guideChromEnabled = true` muestra halo
- ✅ Test: `guideChromEnabled = false` no muestra halo
- ✅ Test: `nonChromaticKeyRequired` se almacena
- ❌ **Test: `prefers-reduced-motion: reduce` simplifica animaciones** (pendiente)

### Compilación

✅ TypeScript compila sin errores (`npx tsc --noEmit`)

### Validación de contratos

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| FEAT-014 §2 D4: EASY con cromo guía y espera corta | ✅ | `guideChromEnabled` y `touchEnableDelayMs` consumidos |
| FEAT-014 §2 D4: MEDIUM con espera media, sin cromo | ✅ | `touchEnableDelayMs` consumido, `guideChromEnabled` false |
| FEAT-014 §2 D4: HARD sin espera apreciable | ✅ | `touchEnableDelayMs = 0` habilita inmediatamente |
| FEAT-014 §2 D5: Espera no genera fallo ni presión temporal | ✅ | Sin cronómetro visible, solo alpha 0.5 → 1.0 |
| ADR-028: Ladder de dificultad visual | ✅ | Cromo guía solo en EASY, espera en EASY/MEDIUM |

### Incidencias

| # | Severidad | Descripción | Acción requerida |
|---|-----------|-------------|------------------|
| 1 | CRÍTICA | Test de `prefers-reduced-motion: reduce` no implementado | Implementar test |
| 2 | MEDIA | Tests no verifican comportamiento real (alpha, halo, valor almacenado) | Extender `getSceneData()` y mejorar tests |
| 3 | MEDIA | Logs de debug en producción (líneas 263, 268) | Eliminar logs |

### Observaciones

1. **Logs de debug:** `console.log` en `renderElements()` (líneas 263, 268) no deberían estar en producción
2. **Hook incompleto:** `getSceneData()` no expone `alpha`, `touchEnableTimer`, `guideChromGraphics`, `_nonChromaticKeyRequired`
3. **Tests superficiales:** Los tests solo verifican que la escena no crashea, no verifican el comportamiento real

### Acciones para aprobación

1. Implementar el test de `prefers-reduced-motion: reduce`
2. Extender `getSceneData()` para exponer `alpha`, `touchEnableTimer`, `guideChromGraphics`, `_nonChromaticKeyRequired`
3. Mejorar los tests para verificar comportamiento real (no solo que no crashea)
4. Eliminar logs de debug (líneas 263, 268)
5. Volver a enviar el sprint para revisión

### Regresiones

✅ Sin regresiones detectadas en tests existentes (SPRINT-070/072/073)

---

### Iteración 2: `APPROVED`

**Reviewer:** reviewer-frontend (automated review)
**Date:** 2026-09-15

**Summary:** Todas las incidencias resueltas. Tests verifican comportamiento real. Sin regresiones.

#### Cambios implementados

1. ✅ **Logs de debug eliminados:**
   - Eliminados `console.log` en `renderElements()` (líneas 263, 268)
   - Logs restantes (líneas 206, 223, 932) son logs de ciclo de vida aceptables

2. ✅ **Hook de pruebas extendido** (`GameView.vue`):
   - `getSceneData()` ahora expone:
     - `alpha` de cada imagen
     - `touchEnableTimerActive` (boolean)
     - `guideChromGraphicsExists` (boolean)
     - `nonChromaticKeyRequired` (boolean)

3. ✅ **Tests mejorados** (`recognition-ladder-visual.cy.ts`):
   - Test de `touchEnableDelayMs = 0`: verifica `alpha: 1.0` y `touchEnableTimerActive: false`
   - Test de `touchEnableDelayMs > 0`: verifica `alpha: 0.5` durante espera, `alpha: 1.0` después
   - Test de `guideChromEnabled = true`: verifica `guideChromGraphicsExists: true`
   - Test de `guideChromEnabled = false`: verifica `guideChromGraphicsExists: false`
   - Test de `nonChromaticKeyRequired`: verifica valor almacenado
   - Test de `GAME_ACTION_RESULT` con `CORRECT`: verifica reaplicación de espera
   - Test de destrucción y recreación de cromo guía al cambiar de ronda

4. ✅ **Test de accesibilidad añadido:**
   - Test de `prefers-reduced-motion: reduce` (líneas 368-433)
   - Verifica transición instantánea de alpha (sin tween)
   - Verifica halo estático (sin pulso)

#### Verificación de completitud

**Tareas de implementación ✅ (13/13)**
- ✅ Extensión de `RecognitionState` con 3 nuevos parámetros
- ✅ Campo de instancia `_nonChromaticKeyRequired`
- ✅ Método `applyTouchEnableDelay()` implementado
- ✅ Método `renderGuideChrom()` implementado
- ✅ Almacenamiento de `nonChromaticKeyRequired`
- ✅ Respeta `prefers-reduced-motion: reduce`

**Tareas de pruebas ✅ (6/6)**
- ✅ Test: `touchEnableDelayMs > 0` bloquea el toque con `alpha: 0.5`
- ✅ Test: `touchEnableDelayMs = 0` habilita inmediatamente con `alpha: 1.0`
- ✅ Test: `guideChromEnabled = true` muestra halo
- ✅ Test: `guideChromEnabled = false` no muestra halo
- ✅ Test: `nonChromaticKeyRequired` se almacena
- ✅ Test: `prefers-reduced-motion: reduce` simplifica animaciones

#### Compilación

✅ TypeScript compila sin errores (`npx tsc --noEmit`)

#### Validación de contratos

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| FEAT-014 §2 D4: EASY con cromo guía y espera corta | ✅ | `guideChromEnabled` y `touchEnableDelayMs` consumidos |
| FEAT-014 §2 D4: MEDIUM con espera media, sin cromo | ✅ | `touchEnableDelayMs` consumido, `guideChromEnabled` false |
| FEAT-014 §2 D4: HARD sin espera apreciable | ✅ | `touchEnableDelayMs = 0` habilita inmediatamente |
| FEAT-014 §2 D5: Espera no genera fallo ni presión temporal | ✅ | Sin cronómetro visible, solo alpha 0.5 → 1.0 |
| ADR-028: Ladder de dificultad visual | ✅ | Cromo guía solo en EASY, espera en EASY/MEDIUM |

#### Regresiones

✅ Sin regresiones detectadas en tests existentes (SPRINT-070/072/073)

### Veredicto final: `APPROVED`

El sprint está completo, funcional y verificado. Cumple con todos los requisitos de FEAT-014 y ADR-028. Los tests verifican comportamiento real (alpha, timer, halo, valor almacenado). Sin logs de debug en producción.
