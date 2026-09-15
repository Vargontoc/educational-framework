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
status: pending
started_at:
closed_at:
blocked_by: SPRINT-072 (verificado)
waiting_for:

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
- [ ] Crear método `renderTargetElement(element)` que renderiza el estímulo en zona superior (y: 25%, x: 50%).
- [ ] Añadir tarjeta visual con fondo semi-transparente y bordes redondeados al estímulo.
- [ ] El estímulo no tiene interacción táctil (no `setInteractive`).
- [ ] Modificar `renderElements()` para separar estímulo (arriba) de opciones (abajo, y: 65%).
- [ ] Actualizar `renderElements()` para recibir `targetElementId` como parámetro.
- [ ] Actualizar llamadas a `renderElements()` desde `loadResources()` y `applyActionToResultType()`.

### Categorías extendidas
- [ ] Extender `RECOGNITION_TYPE` en `GameEvent.ts` con `'SHAPE' | 'COLOR' | 'ANIMAL'`.
- [ ] Añadir casos en `loadResources()` para NUMBER, SHAPE, COLOR, ANIMAL.
- [ ] Crear placeholders visuales para categorías sin assets disponibles.
- [ ] Verificar que `loadResources()` maneja las 5 categorías sin error.

### Tamaño táctil configurable
- [ ] Extraer `MIN_ELEMENT_HIT_SIZE` de constante a campo de instancia con default 80.
- [ ] Verificar que el cálculo de escala usa el nuevo campo.

### Pruebas
- [ ] Actualizar tests Cypress de SPRINT-070 para reflejar nuevo layout.
- [ ] Test: el estímulo se muestra en zona superior, las opciones en zona inferior.
- [ ] Test: el estímulo no es táctil (no responde a `pointerdown`).
- [ ] Test: las 5 categorías cargan assets (o placeholders) sin error.
- [ ] Test: el tamaño táctil mínimo se respeta con el nuevo campo configurable.

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
