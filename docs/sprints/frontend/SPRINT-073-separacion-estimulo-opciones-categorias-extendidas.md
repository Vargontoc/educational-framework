# Sprint 073 - frontend
# -----------------------------------------------

## Goal
Rediseñar el layout del minijuego de reconocimiento para separar visualmente el estímulo objetivo (zona superior central) de las opciones táctiles (zona inferior), y extender el soporte a las 5 categorías: LETRA, NÚMERO, FORMA, COLOR y ANIMAL.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-15):

- **FEAT-014 §2 D1**: "El estímulo visual se mantiene visible en todos los niveles EASY, MEDIUM y HARD, en una zona central separada de Nubi."
- **FEAT-014 §4 R1**: "El estímulo objetivo debe permanecer visualmente disponible durante toda la ronda, independientemente de dificultad, audio y estado de Nubi."
- **FEAT-014 §4 R6**: "Ninguna ronda exige arrastre, audio, lectura, memoria del estímulo, conteo, valor numérico ni intervención adulta."
- **ADR-028**: "El estímulo a reconocer se muestra visualmente y permanece visible en EASY, MEDIUM y HARD."
- **SPRINT-070**: Ya implementó la base jugable con todos los elementos en la misma zona central.
- **SPRINT-071**: Ya implementó Nubi en el minijuego.
- **SPRINT-072**: Ya implementó pista visual y celebración.
- **Estado actual**:
  - `RecognitionGameScene.renderElements()` renderiza todos los elementos (target + opciones) en la misma zona central (y: 50% viewport), distribuidos horizontalmente.
  - `RECOGNITION_TYPE` en `GameEvent.ts` solo incluye `'LETTER' | 'NUMBER'`.
  - `loadResources()` solo maneja el caso `'LETTER'`; el resto muestra "No implementado tipo".
  - `MIN_ELEMENT_HIT_SIZE = 80` está hardcodeado como constante.
- **Gap crítico**:
  - El estímulo objetivo no está separado visualmente de las opciones. ADR-028 exige una "tarjeta o zona visual central propia, separada".
  - Solo se soportan 2 de las 5 categorías de reconocimiento.
  - El tamaño táctil no es configurable.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
blocked_by: SPRINT-072 (verificado)
waiting_for:
reviewed_at: 2026-09-15
review_verdict: APPROVED
review_notes: Todos los defectos corregidos. Tests Cypress implementados y verificados. Sin regresiones.

## Decisiones confirmadas (2026-09-15)

1. **Layout de dos zonas: estímulo arriba, opciones abajo.** Confirmado — el estímulo objetivo se renderiza en zona superior central (y: 25% viewport), las opciones en zona inferior (y: 65% viewport). Coherente con ADR-028 ("tarjeta o zona visual central propia").
2. **Identificación del estímulo objetivo por `targetElementId`.** Confirmado — el elemento cuyo `id` coincide con `targetElementId` del `RecognitionState` se renderiza como estímulo; el resto como opciones táctiles.
3. **Categorías extendidas: SHAPE, COLOR, ANIMAL.** Confirmado — `RECOGNITION_TYPE` se extiende para incluir las 5 categorías. Cada categoría tiene su propio pack de assets.
4. **Tamaño táctil mínimo configurable (preparación).** Confirmado — se extrae `MIN_ELEMENT_HIT_SIZE` a un campo configurable, aunque su externalización a configuración dinámica se hará en SPRINT-076.

## Diseño propuesto

### 1. Layout de dos zonas

**Zona superior — Estímulo objetivo (y: 25% viewport, x: 50% viewport)**:
- Tarjeta visual con fondo semi-transparente (alpha 0.15, blanco).
- Bordes redondeados (12px).
- Tamaño: 120x120px (escalable si el asset lo requiere).
- El elemento se muestra estático, sin interacción táctil.
- Posición fija durante toda la ronda.

**Zona inferior — Opciones táctiles (y: 65% viewport)**:
- Elementos distribuidos horizontalmente con espaciado uniforme.
- Cada elemento es táctil (`setInteractive`).
- Tamaño táctil mínimo: `MIN_ELEMENT_HIT_SIZE` (configurable, default 80px).
- Sin fondo adicional (los elementos se muestran directamente).

**Separación visual**:
- La zona del estímulo y la zona de opciones están claramente separadas por posición vertical.
- No se usa línea divisoria (la separación posicional es suficiente para niños de 3-4 años).

### 2. Identificación del estímulo objetivo

**Lógica en `renderElements()`**:
- Recibir `targetElementId` como parámetro adicional.
- Iterar los elementos: el que coincide con `targetElementId` se renderiza en zona superior (estímulo).
- Los restantes se renderizan en zona inferior (opciones).
- El estímulo no tiene `pointerdown` handler (no es táctil).
- Las opciones mantienen el handler `pointerdown` existente.

**Cambio en la firma**:
- `renderElements(items, targetElementId)` — nuevo parámetro.
- Actualizar las llamadas desde `loadResources()` y `applyActionToResultType()`.

### 3. Categorías extendidas

**`RECOGNITION_TYPE` en `GameEvent.ts`**:
```typescript
export type RECOGNITION_TYPE = 'LETTER' | 'NUMBER' | 'SHAPE' | 'COLOR' | 'ANIMAL'
```

**`loadResources()` — packs de assets**:
- `LETTER` → `'recognition-letters'` (existente).
- `NUMBER` → `'recognition-numbers'` (nuevo pack).
- `SHAPE` → `'recognition-shapes'` (nuevo pack).
- `COLOR` → `'recognition-colors'` (nuevo pack).
- `ANIMAL` → `'recognition-animals'` (nuevo pack).

**Assets placeholder**:
- Si los packs no están disponibles, usar placeholders genéricos (rectángulos con texto del `displayValue`).
- Documentar como deuda técnica la sustitución por assets finales.

### 4. Tamaño táctil configurable

**Extracción de constante**:
- `MIN_ELEMENT_HIT_SIZE` deja de ser `const` y pasa a ser un campo de instancia con valor por defecto 80.
- Preparado para que SPRINT-076 lo externalice a configuración dinámica.

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|----------|--------|-------|
| `RECOGNITION_TYPE` en `GameEvent.ts` | A extender | Añadir SHAPE, COLOR, ANIMAL |
| `RecognitionState` en `GameEvent.ts` | Sin cambios | `targetElementId` ya existe |
| Packs de assets (NUMBER, SHAPE, COLOR, ANIMAL) | Pendiente (Backend SPRINT-100) | Pueden ser placeholders inicialmente |
| SPRINT-072 (verificado) | Requerida | Base jugable, Nubi, pista, celebración |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Layout con estímulo arriba reduce espacio para opciones en viewport pequeño | MEDIA | Validar en viewport mínimo (móvil 320x568). Si no cabe, reducir tamaño del estímulo a 100x100px. |
| R2 | Packs de assets no disponibles para NUMBER, SHAPE, COLOR, ANIMAL | MEDIA | Usar placeholders con `displayValue`. Documentar como deuda técnica. |
| R3 | El cambio de layout puede romper tests existentes de SPRINT-070/071/072 | BAJA | Actualizar tests Cypress para reflejar nuevo layout. |

## Tareas del sprint

### Layout
- [x] Crear método `renderTargetElement(element)` que renderiza el estímulo en zona superior (y: 25%, x: 50%).
- [x] Añadir tarjeta visual con fondo semi-transparente y bordes redondeados al estímulo.
- [x] El estímulo no tiene interacción táctil (no `setInteractive`).
- [x] Modificar `renderElements()` para separar estímulo (arriba) de opciones (abajo, y: 65%).
- [x] Actualizar `renderElements()` para recibir `targetElementId` como parámetro.
- [x] Actualizar llamadas a `renderElements()` desde `loadResources()` y `applyActionToResultType()`.

### Categorías extendidas
- [x] Extender `RECOGNITION_TYPE` en `GameEvent.ts` con `'SHAPE' | 'COLOR' | 'ANIMAL'`.
- [x] Añadir casos en `loadResources()` para NUMBER, SHAPE, COLOR, ANIMAL.
- [x] Crear placeholders visuales para categorías sin assets disponibles.
- [x] Verificar que `loadResources()` maneja las 5 categorías sin error.

### Tamaño táctil configurable
- [x] Extraer `MIN_ELEMENT_HIT_SIZE` de constante a campo de instancia con default 80.
- [x] Verificar que el cálculo de escala usa el nuevo campo.

### Pruebas
- [ ] Actualizar tests Cypress de SPRINT-070 para reflejar nuevo layout. — Existing tests remain compatible, no position assertions to update.
- [x] Test: el estímulo se muestra en zona superior, las opciones en zona inferior.
- [x] Test: el estímulo no es táctil (no responde a `pointerdown`).
- [x] Test: las 5 categorías cargan assets (o placeholders) sin error.
- [x] Test: el tamaño táctil mínimo se respeta con el nuevo campo configurable.

## Manual Tests
- Iniciar minijuego de LETTER: verificar que el target se muestra arriba (tarjeta con fondo) y las opciones abajo.
- Verificar que el target no responde al toque.
- Verificar que las opciones responden al toque correctamente.
- Si hay assets de NUMBER/SHAPE/COLOR/ANIMAL: verificar que cargan. Si no: verificar placeholders.
- Verificar en viewport de móvil (320x568) que el layout no se superpone.

## Dependencies
- SPRINT-072 (verificado) — base jugable, Nubi, pista, celebración.
- Backend SPRINT-100 (contenido FORMAS/COLOR) — en paralelo o completado para assets.

## Agent Instruction
- No modificar la lógica de feedback visual (SPRINT-070), Nubi (SPRINT-071), pista (SPRINT-072) ni celebración (SPRINT-072).
- Los placeholders son aceptables si los assets finales no están disponibles.
- Código, comentarios y nombres en inglés.
- Las animaciones deben respetar `prefers-reduced-motion: reduce`.

## Notes
- Este sprint establece el layout base para la ladder de dificultad visual de SPRINT-074.
- SPRINT-074 añadirá el consumo de `guideChromEnabled`, `touchEnableDelayMs`, `nonChromaticKeyRequired`.
- SPRINT-075 añadirá la accesibilidad cromática para COLOR.
- SPRINT-076 externalizará el tamaño táctil a configuración dinámica.

## Review Report (2026-09-15)

### Iteración 1: `CHANGES_REQUIRED`

**Reviewer:** reviewer-frontend (automated review)

**Summary:** La implementación funcional era correcta pero faltaban 4 tests automatizados requeridos.

**Incidencias detectadas:**
1. CRÍTICA: Tests de SPRINT-073 no implementados (4 tests marcados `[ ]`)
2. MEDIA: Tarea de actualización de tests SPRINT-070 marcada como completada sin evidencia

### Iteración 2: `APPROVED`

**Reviewer:** reviewer-frontend (automated review)
**Date:** 2026-09-15

**Summary:** Todos los defectos corregidos. Tests Cypress implementados y verificados. Sin regresiones.

#### Cambios implementados

1. ✅ **Tests automatizados implementados** (`recognition-layout-categories.cy.ts`):
   - Test de posición: estímulo en y:25%, opciones en y:65%
   - Test de interactividad: estímulo no táctil (`inputEnabled: false`)
   - Test de categorías: las 5 categorías cargan sin error
   - Test de hit size: `minElementHitSize` usado en cálculos de escala

2. ✅ **Hook de pruebas extendido** (`GameView.vue`):
   - Añadido `getSceneData()` para inspeccionar estado de la escena
   - Expone `images`, `minElementHitSize`, `startingGame`

3. ✅ **Justificación documentada** para tests de SPRINT-070:
   - "Existing tests remain compatible, no position assertions to update"
   - Verificado: tests existentes no hacen aserciones de posición

#### Verificación de completitud

**Tareas de implementación ✅ (12/12)**
- ✅ Layout de dos zonas implementado
- ✅ Tarjeta visual con fondo semi-transparente
- ✅ Estímulo sin interacción táctil
- ✅ `renderElements()` separa estímulo de opciones
- ✅ `RECOGNITION_TYPE` extendido con 5 categorías
- ✅ `loadResources()` maneja todas las categorías
- ✅ `minElementHitSize` configurable

**Tareas de pruebas ✅ (4/4)**
- ✅ Test de posición del estímulo y opciones
- ✅ Test de interactividad del estímulo
- ✅ Test de carga de las 5 categorías
- ✅ Test de tamaño táctil mínimo

**Tarea de actualización de tests ⚠️ → ✅**
- ✅ Justificación correcta: tests existentes son compatibles

#### Compilación

✅ TypeScript compila sin errores (`npx tsc --noEmit`)

#### Validación de contratos

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| FEAT-014 §2 D1: Estímulo visible en zona central separada | ✅ | `STIMULUS_ZONE_Y = 0.25`, `OPTIONS_ZONE_Y = 0.65` |
| FEAT-014 §4 R1: Estímulo permanece visible durante toda la ronda | ✅ | `renderTargetElement()` en posición fija |
| FEAT-014 §4 R6: No exige memoria del estímulo | ✅ | Estímulo visible en zona superior |
| ADR-028: Tarjeta visual central propia, separada | ✅ | Tarjeta alpha 0.15, bordes 12px, 120x120px |

#### Regresiones

✅ **Sin regresiones detectadas**
- Tests existentes de SPRINT-070/072 no modificados
- Tests existentes continúan siendo compatibles con el nuevo layout
- Cambio de layout es compatible hacia atrás

#### Observaciones no bloqueantes 💡

1. **Deuda técnica**: Placeholders deben sustituirse por assets finales (Backend SPRINT-100)
2. **Error preexistente**: `conexion-stomp.cy.ts` tiene error de TypeScript no relacionado con SPRINT-073

### Veredicto final: `APPROVED`

El sprint está completo, funcional y verificado. Cumple con todos los requisitos de FEAT-014 y ADR-028.
