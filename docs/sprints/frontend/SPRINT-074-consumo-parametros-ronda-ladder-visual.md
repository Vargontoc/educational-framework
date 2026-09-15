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
status: pending
started_at:
closed_at:
blocked_by: SPRINT-073
waiting_for: Backend SPRINT-102 (contratos round-ready-event)

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
- [ ] Añadir `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired` a `RecognitionState` en `GameEvent.ts`.
- [ ] Añadir campo de instancia `nonChromaticKeyRequired` en `RecognitionGameScene`.

### Espera antes del toque
- [ ] Implementar método `applyTouchEnableDelay(delayMs, optionImages)`.
- [ ] Al recibir `GAME_READY`: aplicar espera si `touchEnableDelayMs > 0`.
- [ ] Al recibir `GAME_ACTION_RESULT` con `CORRECT` (nueva ronda): aplicar espera si `touchEnableDelayMs > 0`.
- [ ] Las opciones aparecen con `alpha: 0.5` y `disableInteractive()` durante la espera.
- [ ] Al expirar: `alpha: 1.0` y `setInteractive()`.
- [ ] Respetar `prefers-reduced-motion: reduce` (transición instantánea).

### Cromo guía
- [ ] Implementar método `renderGuideChrom()` que dibuja halo alrededor de zona de opciones.
- [ ] Si `guideChromEnabled` es true: mostrar halo con pulso lento.
- [ ] Si `guideChromEnabled` es false: no mostrar halo.
- [ ] Destruir halo al cambiar de ronda.
- [ ] Respetar `prefers-reduced-motion: reduce` (halo estático, sin pulso).

### Almacenamiento de `nonChromaticKeyRequired`
- [ ] Almacenar `nonChromaticKeyRequired` del `RecognitionState` en campo de instancia.
- [ ] No consumir en este sprint.

### Pruebas
- [ ] Test: `touchEnableDelayMs > 0` bloquea el toque durante el tiempo configurado.
- [ ] Test: `touchEnableDelayMs = 0` habilita el toque inmediatamente.
- [ ] Test: `guideChromEnabled = true` muestra halo alrededor de opciones.
- [ ] Test: `guideChromEnabled = false` no muestra halo.
- [ ] Test: `nonChromaticKeyRequired` se almacena correctamente.
- [ ] Test: `prefers-reduced-motion: reduce` simplifica animaciones de espera y cromo.

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
