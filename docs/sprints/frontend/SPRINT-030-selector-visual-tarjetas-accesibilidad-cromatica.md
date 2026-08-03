# SPRINT-030 — Selector visual de tarjetas de accesibilidad cromática

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-08-02
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-028 (verificado y cerrado)
- **Impacto estimado:** Sustitución del `NubiSelect` textual y `ColorVisionExamples.vue` por un selector de 9 tarjetas visuales con previsualización integrada, patrones no cromáticos y explicaciones cotidianas

## Objetivo

Reemplazar la sección de accesibilidad visual actual (menú desplegable `NubiSelect` + componente `ColorVisionExamples.vue` con formas geométricas simples) por un **selector de tarjetas visuales** (`ColorVisionCardSelector.vue`) compuesto por 9 tarjetas individuales (`ColorVisionCard.vue`) que muestran nombre del modo, explicación cotidiana y una muestra de tres globos con patrones no cromáticos. Cada tarjeta permite previsualizar el efecto del modo de visión de color mediante filtros SVG (`<feColorMatrix>`), respondiendo a toque, puntero y foco. Se actualiza el aviso visible y las traducciones i18n con las explicaciones cotidianas aprobadas en ADR-023.

## Contexto

El SPRINT-028 implementó la sección de accesibilidad visual con:
- `NubiToggle` para activar/desactivar el ajuste
- `NubiSelect` con 9 opciones textuales (NONE + 8 modos)
- `ColorVisionExamples.vue` con formas geométricas SVG (círculos y rectángulos) y colores precalculados por modo
- Aviso no médico

**ADR-023** (aceptada 2026-08-02) requiere sustituir el menú textual por tarjetas visuales con:
- Nombre del modo + explicación cotidiana
- Muestra de 3 globos (rojo, verde, azul) con patrones/símbolos no cromáticos
- Previsualización del modo al explorar la tarjeta (hover/focus/selección)
- Funcionamiento equivalente en táctil sin dependencia de hover

**Referencias:**
- ADR-023: `docs/product/decisions/ADR-023-Selector-visual-de-accesibilidad-cromatica.md`
- FEAT-006 actualizado: `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
- SPRINT-028 verificado: `docs/sprints/frontend/SPRINT-028-edicion-perfil-accesibilidad-visual.md`

**Estado actual de los archivos afectados:**
- `framework/frontend/app/src/types/colorVision.ts` — Enum `ColorVisionMode` con 9 valores y `COLOR_VISION_LABELS`. No requiere cambios.
- `framework/frontend/app/src/components/ninos/ColorVisionExamples.vue` — Será sustituido por los nuevos componentes.
- `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` — Sección 3 (líneas 71-95) será reemplazada.
- `framework/frontend/app/src/i18n/locales/es.ts` — Claves de `visualAccessibility` serán ampliadas.

## Diseño funcional-técnico

### 1. Componente `ColorVisionCardSelector.vue`

**Archivo:** `framework/frontend/app/src/components/ninos/ColorVisionCardSelector.vue` (nuevo)

**Responsabilidad:** Contenedor del selector de tarjetas. Renderiza las 9 tarjetas, gestiona el estado de exploración (hover/focus/selected) y emite el valor seleccionado al padre. Muestra el aviso visible.

**Props:**
```typescript
interface Props {
  modelValue: string
  modes: ReadonlyArray<{
    value: string
    label: string
    description: string
  }>
}
```

**Emits:**
```typescript
interface Emits {
  (e: 'update:modelValue', value: string): void
}
```

**Estado interno:**
```typescript
const previewedMode = ref<string | null>(null)
```

**Lógica:**
- Renderiza un `ColorVisionCard` por cada entrada de `modes`.
- Gestiona `previewedMode`: se activa al recibir eventos `explore` (hover/focus/pointerdown según decisión de accesibilidad táctil) de una tarjeta y se desactiva al salir.
- La tarjeta seleccionada (`modelValue`) mantiene siempre su previsualización activa.
- Layout: CSS Grid responsive (ver sección de preguntas de decisión).
- Emite `update:modelValue` al recibir `select` de una tarjeta.
- El aviso visible se renderiza debajo de la cuadrícula de tarjetas.

**Estructura template:**
```vue
<template>
  <div class="color-vision-card-selector" role="radiogroup" :aria-label="selectorLabel">
    <div class="color-vision-card-selector__grid">
      <ColorVisionCard
        v-for="mode in modes"
        :key="mode.value"
        :mode="mode"
        :selected="modelValue === mode.value"
        :previewed="previewedMode === mode.value || modelValue === mode.value"
        @select="$emit('update:modelValue', mode.value)"
        @explore="previewedMode = mode.value"
        @unexplore="previewedMode = null"
      />
    </div>
    <aside class="color-vision-card-selector__warning" role="note">
      {{ warningText }}
    </aside>
  </div>
</template>
```

---

### 2. Componente `ColorVisionCard.vue`

**Archivo:** `framework/frontend/app/src/components/ninos/ColorVisionCard.vue` (nuevo)

**Responsabilidad:** Tarjeta individual. Muestra nombre del modo, explicación cotidiana y la muestra de 3 globos con patrones no cromáticos. Aplica el filtro SVG de simulación cromática cuando está en estado `previewed`.

**Props:**
```typescript
interface Props {
  mode: {
    value: string
    label: string
    description: string
  }
  selected: boolean
  previewed: boolean
}
```

**Emits:**
```typescript
interface Emits {
  (e: 'select'): void
  (e: 'explore'): void
  (e: 'unexplore'): void
}
```

**Estructura visual de la tarjeta:**
```
┌─────────────────────────────────┐
│  [Muestra: 3 globos con filtro] │
│  🔴 ●  🟢 ▲  🔵 ■              │
│                                 │
│  Nombre del modo                │
│  Explicación cotidiana          │
│                                 │
│  ○ Seleccionado (si aplica)     │
└─────────────────────────────────┘
```

**Muestra de globos (SVG inline):**
- Tres globos en fila: rojo (`#e53e3e`), verde (`#38a169`), azul (`#3182ce`).
- Cada globo incluye un patrón/símbolo no cromático superpuesto (ver preguntas de decisión para la selección final).
- Cuando `previewed === true`, se aplica un filtro SVG `<feColorMatrix>` generado por `simulateColorVision(mode.value)` sobre el grupo de globos.

**Interacción:**
- `role="radio"` + `aria-checked` para accesibilidad semántica.
- `tabindex="0"` para foco por teclado.
- Eventos:
  - `pointerenter` / `focus` → emite `explore`
  - `pointerleave` / `blur` → emite `unexplore`
  - `click` / `keydown.enter` / `keydown.space` → emite `select`
  - `pointerdown` → emite `explore` (para táctil, ver preguntas de decisión)
- Objetivo táctil mínimo: 48×48dp (la tarjeta completa).
- Estado `selected`: borde destacado + indicador visual (check/icono) no dependiente del color.
- Estado `previewed`: la muestra aplica el filtro de simulación.
- Estado `focus-visible`: anillo de foco visible.

**Estados visuales:**
| Estado | Borde | Fondo | Indicador |
|--------|-------|-------|-----------|
| Normal | `--nubi-border-default` | `--nubi-bg-surface` | — |
| Hover/Explorada | `--nubi-border-hover` | `--nubi-bg-surface` | Filtro activo en muestra |
| Seleccionada | `--nubi-color-primary` (borde 2px) | `--nubi-bg-surface` | Check/icono + borde |
| Focus-visible | — | — | Anillo de foco 3px |

---

### 3. Utilidad `simulateColorVision`

**Archivo:** `framework/frontend/app/src/utils/simulateColorVision.ts` (nuevo)

**Responsabilidad:** Generar matrices de transformación cromática (`<feColorMatrix>`) para cada modo de visión de color. Estas matrices se aplican como filtros SVG inline sobre la muestra de globos.

**Interfaz:**
```typescript
export function simulateColorVision(mode: string): string
```

**Retorno:** Cadena con los 20 valores de la matriz (5×4) separados por espacios, lista para usar en `<feColorMatrix values="...">`.

**Matrices de referencia (approximation de Brettel/Viénot):**

| Modo | Matriz (valores aproximados) |
|------|-----|
| NONE | Identidad: `1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 1 0` |
| PROTANOPIA | `0.567 0.433 0 0 0  0.558 0.442 0 0 0  0 0.244 0.756 0 0  0 0 0 1 0` |
| PROTANOMALY | `0.817 0.183 0 0 0  0.333 0.667 0 0 0  0 0.125 0.875 0 0  0 0 0 1 0` |
| DEUTERANOPIA | `0.625 0.375 0 0 0  0.7 0.3 0 0 0  0 0.3 0.7 0 0  0 0 0 1 0` |
| DEUTERANOMALY | `0.8 0.2 0 0 0  0.258 0.742 0 0 0  0 0.242 0.758 0 0  0 0 0 1 0` |
| TRITANOPIA | `0.95 0.05 0 0 0  0 0.433 0.567 0 0  0 0.475 0.525 0 0  0 0 0 1 0` |
| TRITANOMALY | `0.967 0.033 0 0 0  0 0.733 0.267 0 0  0 0.183 0.817 0 0  0 0 0 1 0` |
| ACHROMATOMALY | `0.3 0.59 0.11 0 0  0.3 0.59 0.11 0 0  0.3 0.59 0.11 0 0  0 0 0 1 0` (desaturación parcial) |
| ACHROMATOPSIA | `0.299 0.587 0.114 0 0  0.299 0.587 0.114 0 0  0.299 0.587 0.114 0 0  0 0 0 1 0` (escala de grises) |

**Nota:** Los valores son aproximaciones. La decisión final sobre si usar estas matrices hard-coded o un mecanismo alternativo corresponde al desarrollador (ver preguntas de decisión).

**Uso en `ColorVisionCard.vue`:**
```vue
<svg ...>
  <defs>
    <filter :id="`cvf-${mode.value}`">
      <feColorMatrix :values="simulateColorVision(mode.value)" />
    </filter>
  </defs>
  <g :filter="previewed ? `url(#cvf-${mode.value})` : undefined">
    <!-- globos -->
  </g>
</svg>
```

---

### 4. Integración en `ChildProfileEditView.vue`

**Archivo:** `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` (modificación)

**Cambios en la sección 3 (líneas 71-95):**

Se sustituye el bloque `NubiSelect` + `ColorVisionExamples` por `ColorVisionCardSelector`:

```vue
<section class="child-profile-edit-view__section">
  <h2>{{ t('views.ninos.edit.sections.visualAccessibility.title') }}</h2>

  <NubiToggle
    :model-value="visualAccessibilityActive"
    @update:model-value="visualAccessibilityActive = $event"
    :label="t('views.ninos.edit.sections.visualAccessibility.toggleLabel')"
  />

  <div v-if="visualAccessibilityActive" class="child-profile-edit-view__visual-options">
    <ColorVisionCardSelector
      :model-value="draft.colorVisionMode"
      :modes="colorVisionModes"
      @update:model-value="draft.colorVisionMode = $event"
    />
  </div>
</section>
```

**Datos para las tarjetas:**
```typescript
import { ColorVisionMode } from '../../types/colorVision'
import { COLOR_VISION_DESCRIPTIONS } from '../../types/colorVision'

const colorVisionModes = computed(() =>
  Object.values(ColorVisionMode).map((value) => ({
    value,
    label: COLOR_VISION_LABELS[value],
    description: COLOR_VISION_DESCRIPTIONS[value]
  }))
)
```

**Imports modificados:**
- Eliminar: `NubiSelect`, `ColorVisionExamples`
- Añadir: `ColorVisionCardSelector`

---

### 5. Ampliación de `colorVision.ts`

**Archivo:** `framework/frontend/app/src/types/colorVision.ts` (modificación)

Añadir el mapa de explicaciones cotidianas:

```typescript
export const COLOR_VISION_DESCRIPTIONS: Record<ColorVisionMode, string> = {
  [ColorVisionMode.NONE]: 'Sin ajuste de visualización.',
  [ColorVisionMode.DEUTERANOPIA]: 'Rojo y verde pueden verse muy parecidos.',
  [ColorVisionMode.DEUTERANOMALY]: 'Rojo y verde pueden costar más de distinguir.',
  [ColorVisionMode.PROTANOPIA]: 'Algunos rojos y verdes pueden confundirse.',
  [ColorVisionMode.PROTANOMALY]: 'Algunos rojos y verdes pueden parecerse.',
  [ColorVisionMode.TRITANOPIA]: 'Azul y amarillo pueden verse parecidos.',
  [ColorVisionMode.TRITANOMALY]: 'Azul y amarillo pueden costar más de distinguir.',
  [ColorVisionMode.ACHROMATOMALY]: 'Los colores pueden verse menos intensos o apagados.',
  [ColorVisionMode.ACHROMATOPSIA]: 'Los colores pueden verse en tonos grises.'
}
```

---

### 6. i18n — Actualización de claves

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

**Claves modificadas/añadidas en `visualAccessibility`:**
```typescript
visualAccessibility: {
  title: 'Accesibilidad visual',
  toggleLabel: 'Activar ajuste visual',
  selectorLabel: 'Selecciona un perfil de visualización para comparar',
  warning: 'Esta comparación es orientativa para adaptar el juego. No identifica la visión del niño ni sustituye la orientación de un especialista.',
  cardAriaLabel: 'Perfil de visualización {mode}: {description}'
}
```

**Clave eliminada:**
- `selectLabel` (sustituida por `selectorLabel`)

**Nota:** Las explicaciones cotidianas residen en `colorVision.ts` como constantes tipadas, no en i18n, porque están aprobadas como texto fijo en español por ADR-023. Si se requiere internacionalización futura, se migrarán a i18n.

---

## Contratos y dependencias externas

### Contratos

- **Sin cambios.** Los 9 valores del enum `ColorVisionMode` ya existen en `docs/contracts/schemas` y en `colorVision.ts`.
- El campo `colorVisionMode` del endpoint `PUT /api/v1/family/children/:id` no se modifica.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Backend | Ninguna nueva. El enum ya tiene los 9 valores. | ✅ Sin bloqueo |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |
| Frontend | SPRINT-028 verificado (base sobre la que se sustituye la sección 3). | ✅ Completado |

---

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Las matrices `<feColorMatrix>` son aproximaciones y pueden no reflejar con fidelidad clínica cada modo. | MEDIA | La muestra es orientativa (no diagnóstica). El aviso visible lo comunica explícitamente. Los patrones no cromáticos garantizan la diferenciación sin color. |
| R2 | 9 tarjetas pueden no caber cómodamente en pantallas de móvil estrecho. | MEDIA | Layout responsive con CSS Grid (ver preguntas de decisión). Se prioriza scroll vertical sobre reducción excesiva de tamaño. Cada tarjeta mantiene objetivo táctil ≥ 48dp. |
| R3 | La previsualización táctil puede confundirse con la selección definitiva. | BAJA | La exploración (preview) y la selección (commit) son estados visualmente diferenciados. La selección se marca con borde + indicador; la exploración solo activa el filtro en la muestra. |
| R4 | Los filtros SVG pueden tener coste de rendimiento en dispositivos de gama baja. | BAJA | Cada tarjeta renderiza un SVG pequeño (3 globos). El filtro solo se aplica a la tarjeta explorada/seleccionada, no a las 9 simultáneamente. |
| R5 | Los patrones/símbolos de los globos pueden no ser suficientemente distinguibles sin color. | MEDIA | Se validará con pruebas manuales de accesibilidad. Los patrones deben ser formas geométricas simples con alto contraste de trazo (ver preguntas de decisión). |
| R6 | El texto del aviso puede no ser suficientemente visible o comprensible. | BAJA | Se actualiza el texto al aprobado en ADR-023. Se mantiene el estilo `<aside>` con borde lateral visible. |

---

## Preguntas de decisión al usuario

> **Todas las preguntas de decisión han sido resueltas y confirmadas (2026-08-02).**

### P1 — Mecanismo de previsualización táctil — ✅ Confirmada: Opción A

**Opción seleccionada:** `pointerdown` activa preview, `pointerup`+`click` confirma selección.

**Justificación:** Es la más sencilla, cumple el requisito de ADR-023 («la selección ofrece la misma previsualización sin requerir una acción de pasar el cursor») y la selección es siempre deliberada mediante `click`.

### P2 — Mecanismo de simulación cromática — ✅ Confirmada: Opción A

**Opción seleccionada:** Filtros SVG `<feColorMatrix>` con matrices hard-coded.

**Justificación:** Es la más idiomática para SVG inline, no introduce dependencias y el rendimiento es adecuado para 3 globos por tarjeta.

### P3 — Patrones/símbolos para los globos — ✅ Confirmada: Opción A

**Opción seleccionada:** ● (círculo) para rojo, ▲ (triángulo) para verde, ■ (cuadrado) para azul.

**Justificación:** Máxima simplicidad geométrica, fácil renderizado SVG, alta distinguibilidad por silueta incluso en tamaños pequeños.

### P4 — Layout responsive de tarjetas — ✅ Confirmada: Opción B

**Opción seleccionada:** `grid-template-columns: repeat(auto-fill, minmax(160px, 1fr))`.

**Justificación:** Se adapta naturalmente a móvil (2 columnas), tablet (3 columnas) y escritorio (3-4 columnas) sin media queries adicionales.

---

## Sprints propuestos

### SPRINT-030 — Selector visual de tarjetas de accesibilidad cromática

**Objetivo:** Sustituir `NubiSelect` + `ColorVisionExamples.vue` por `ColorVisionCardSelector.vue` con 9 tarjetas visuales (`ColorVisionCard.vue`), previsualización mediante filtros SVG, explicaciones cotidianas y aviso actualizado, integrados en `ChildProfileEditView.vue`.

---

#### Tarea 30.1: Crear utilidad `simulateColorVision`

**Descripción:** Implementar la función que devuelve la matriz `<feColorMatrix>` para cada modo.

**Archivo:** `framework/frontend/app/src/utils/simulateColorVision.ts` (nuevo)

**Interfaz:**
```typescript
export function simulateColorVision(mode: string): string
```

**Lógica:**
- Mapa `Record<string, string>` con las 9 matrices.
- Devuelve la matriz del modo solicitado o la identidad si el modo no existe.

**Criterios de aceptación:**
- Devuelve cadenas de 20 valores numéricos separados por espacios.
- Para `NONE` devuelve la matriz identidad.
- Para cada modo devuelve la matriz de simulación correspondiente.
- TypeScript compila sin errores.

---

#### Tarea 30.2: Ampliar `colorVision.ts` con descripciones cotidianas

**Descripción:** Añadir el mapa `COLOR_VISION_DESCRIPTIONS` con las 9 explicaciones cotidianas aprobadas en ADR-023.

**Archivo:** `framework/frontend/app/src/types/colorVision.ts` (modificación)

**Contenido:**
```typescript
export const COLOR_VISION_DESCRIPTIONS: Record<ColorVisionMode, string> = {
  [ColorVisionMode.NONE]: 'Sin ajuste de visualización.',
  [ColorVisionMode.DEUTERANOPIA]: 'Rojo y verde pueden verse muy parecidos.',
  [ColorVisionMode.DEUTERANOMALY]: 'Rojo y verde pueden costar más de distinguir.',
  [ColorVisionMode.PROTANOPIA]: 'Algunos rojos y verdes pueden confundirse.',
  [ColorVisionMode.PROTANOMALY]: 'Algunos rojos y verdes pueden parecerse.',
  [ColorVisionMode.TRITANOPIA]: 'Azul y amarillo pueden verse parecidos.',
  [ColorVisionMode.TRITANOMALY]: 'Azul y amarillo pueden costar más de distinguir.',
  [ColorVisionMode.ACHROMATOMALY]: 'Los colores pueden verse menos intensos o apagados.',
  [ColorVisionMode.ACHROMATOPSIA]: 'Los colores pueden verse en tonos grises.'
}
```

**Criterios de aceptación:**
- Las 9 descripciones coinciden exactamente con las aprobadas en ADR-023.
- Las claves del mapa cubren todos los valores del enum.
- TypeScript compila sin errores.

---

#### Tarea 30.3: Implementar `ColorVisionCard.vue`

**Descripción:** Tarjeta individual con muestra SVG de 3 globos (rojo, verde, azul) con patrones no cromáticos, nombre del modo, explicación cotidiana, y aplicación de filtro de simulación cromática.

**Archivo:** `framework/frontend/app/src/components/ninos/ColorVisionCard.vue` (nuevo)

**Especificación visual:**
- SVG inline con 3 globos: rojo (`#e53e3e`, ●), verde (`#38a169`, ▲), azul (`#3182ce`, ■).
- Filtro SVG `<feColorMatrix>` aplicado al grupo `<g>` cuando `previewed === true`.
- Cada tarjeta tiene `id` único para el filtro (evitar colisiones SVG).
- Nombre del modo en negrita.
- Explicación cotidiana debajo.
- Indicador de selección (check/icono) visible cuando `selected === true`.

**Especificación de interacción:**
- `role="radio"`, `aria-checked`, `tabindex="0"`.
- `@pointerenter`, `@focus` → emite `explore`.
- `@pointerleave`, `@blur` → emite `unexplore`.
- `@click`, `@keydown.enter`, `@keydown.space` → emite `select`.
- `@pointerdown` → emite `explore` (previsualización táctil).
- Objetivo táctil ≥ 48×48dp.
- Estado `focus-visible` con anillo de foco.
- Estado `selected` con borde primario + indicador.

**Criterios de aceptación:**
- Muestra 3 globos con colores y patrones (● ▲ ■).
- Patrones distinguibles sin percepción de color.
- Al recibir `previewed`, aplica el filtro SVG al grupo de globos.
- Al no recibir `previewed`, muestra los globos sin filtro.
- Emite `select`, `explore`, `unexplore` correctamente.
- Accesible por teclado (Tab, Enter, Space).
- Objetivo táctil ≥ 48×48dp.
- `role="radio"` + `aria-checked` presentes.
- TypeScript compila sin errores.

---

#### Tarea 30.4: Implementar `ColorVisionCardSelector.vue`

**Descripción:** Contenedor del selector de tarjetas. Renderiza las 9 tarjetas, gestiona el estado de previsualización y muestra el aviso visible.

**Archivo:** `framework/frontend/app/src/components/ninos/ColorVisionCardSelector.vue` (nuevo)

**Especificación:**
- `role="radiogroup"` con `aria-label`.
- Layout CSS Grid: `grid-template-columns: repeat(auto-fill, minmax(160px, 1fr))`.
- Gap entre tarjetas: `var(--nubi-spacing-md)`.
- Estado interno `previewedMode: ref<string | null>(null)`.
- Propaga `explore`/`unexplore` de las tarjetas a `previewedMode`.
- La tarjeta seleccionada (`modelValue`) tiene siempre `previewed === true`.
- Aviso visible debajo de la cuadrícula con el texto actualizado de ADR-023.

**Criterios de aceptación:**
- Renderiza 9 tarjetas (NONE + 8 modos).
- La tarjeta seleccionada muestra siempre la previsualización.
- Al explorar una tarjeta (hover/focus/touch), su previsualización se activa.
- Al dejar de explorar, la previsualización se desactiva (salvo la seleccionada).
- `v-model` funciona correctamente con el padre.
- Aviso visible con el texto aprobado en ADR-023.
- Layout responsive funciona en móvil (≥ 2 columnas) y tablet (≥ 3 columnas).
- `role="radiogroup"` presente.
- TypeScript compila sin errores.

---

#### Tarea 30.5: Integrar en `ChildProfileEditView.vue` y limpiar componentes obsoletos

**Descripción:** Sustituir la sección 3 de `ChildProfileEditView.vue` para usar `ColorVisionCardSelector` en lugar de `NubiSelect` + `ColorVisionExamples`. Eliminar `ColorVisionExamples.vue`.

**Archivos:**
- `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` (modificación)
- `framework/frontend/app/src/components/ninos/ColorVisionExamples.vue` (eliminar)

**Cambios en `ChildProfileEditView.vue`:**
- Eliminar imports de `NubiSelect` y `ColorVisionExamples`.
- Añadir import de `ColorVisionCardSelector`.
- Sustituir el bloque `<NubiSelect>` + `<ColorVisionExamples>` por `<ColorVisionCardSelector>`.
- Crear `colorVisionModes` computed con `value`, `label`, `description` para cada modo.
- Eliminar `colorVisionOptions` (ya no necesario).

**Criterios de aceptación:**
- La sección 3 muestra tarjetas visuales en lugar del menú desplegable.
- `NubiToggle` sigue controlando la activación/desactivación del ajuste.
- Al activar el toggle, se muestran las 9 tarjetas.
- Al seleccionar una tarjeta, `draft.colorVisionMode` se actualiza.
- Al desactivar el toggle, `draft.colorVisionMode` vuelve a `NONE`.
- `ColorVisionExamples.vue` eliminado.
- No hay imports rotos ni referencias al componente eliminado.
- TypeScript compila sin errores.

---

#### Tarea 30.6: Actualizar i18n

**Descripción:** Actualizar las claves de `visualAccessibility` en `es.ts` con el nuevo aviso y las nuevas etiquetas.

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

**Claves modificadas:**
```typescript
visualAccessibility: {
  title: 'Accesibilidad visual',
  toggleLabel: 'Activar ajuste visual',
  selectorLabel: 'Selecciona un perfil de visualización para comparar',
  warning: 'Esta comparación es orientativa para adaptar el juego. No identifica la visión del niño ni sustituye la orientación de un especialista.',
  cardAriaLabel: 'Perfil de visualización {mode}: {description}'
}
```

**Clave eliminada:**
- `selectLabel`

**Criterios de aceptación:**
- El aviso coincide exactamente con el texto aprobado en ADR-023.
- No hay claves huérfanas (`selectLabel` eliminada).
- `cardAriaLabel` soporta interpolación de `{mode}` y `{description}`.
- TypeScript compila sin errores.

---

#### Tarea 30.7: Accesibilidad y verificación cross-input

**Descripción:** Verificar que el selector completo cumple los requisitos de accesibilidad para toque, puntero y foco.

**Requisitos:**
1. Las 9 tarjetas son alcanzables por Tab.
2. Cada tarjeta tiene `role="radio"` + `aria-checked`.
3. El `radiogroup` tiene `aria-label` descriptivo.
4. Los patrones (● ▲ ■) distinguen los globos sin percepción de color.
5. El estado `selected` no depende exclusivamente del color (borde + indicador).
6. El estado `focus-visible` muestra anillo de foco.
7. Objetivo táctil ≥ 48×48dp en todas las tarjetas.
8. La previsualización funciona con: hover (ratón), focus (teclado), pointerdown (táctil).
9. El aviso es legible y tiene contraste WCAG 2.1 AA.
10. El layout responsive no solapa tarjetas en móvil (≥ 320px de ancho).

**Criterios de aceptación:**
- Navegación por teclado: Tab recorre las 9 tarjetas, Enter/Space seleccionan.
- Ratón: hover activa preview, click selecciona.
- Táctil: tap activa preview + selecciona en una interacción.
- Los patrones son distinguibles en escala de grises.
- Todos los objetivos táctiles ≥ 48×48dp.
- Contraste del aviso ≥ 4.5:1.
- Layout correcto en 320px, 375px, 768px y 1024px de ancho.
- TypeScript compila sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/utils/simulateColorVision.ts` | Nuevo archivo |
| `framework/frontend/app/src/types/colorVision.ts` | Añadir `COLOR_VISION_DESCRIPTIONS` |
| `framework/frontend/app/src/components/ninos/ColorVisionCard.vue` | Nuevo archivo |
| `framework/frontend/app/src/components/ninos/ColorVisionCardSelector.vue` | Nuevo archivo |
| `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` | Modificación (sección 3) |
| `framework/frontend/app/src/components/ninos/ColorVisionExamples.vue` | Eliminar |
| `framework/frontend/app/src/i18n/locales/es.ts` | Actualizar claves `visualAccessibility` |

## Estimación

- **Duración:** 3 días
- **Complejidad:** Media-Alta
- **Riesgo:** Medio (filtros SVG, accesibilidad táctil, layout responsive con 9 tarjetas)

## Criterios de aceptación del sprint

1. La sección de accesibilidad visual muestra 9 tarjetas visuales en lugar de un menú desplegable textual.
2. Cada tarjeta muestra el nombre del modo, la explicación cotidiana aprobada en ADR-023 y una muestra de 3 globos (rojo, verde, azul) con patrones no cromáticos (● ▲ ■).
3. Al explorar una tarjeta mediante puntero, foco o toque, la muestra previsualiza el modo correspondiente mediante filtro SVG.
4. En táctil, la selección activa la previsualización sin requerir hover.
5. La tarjeta seleccionada muestra siempre la previsualización activa.
6. El aviso visible comunica que la comparación es orientativa, no identifica la visión del niño y no sustituye la orientación de un especialista.
7. Las tarjetas son navegables por teclado (Tab, Enter, Space) con `role="radio"` + `role="radiogroup"`.
8. Los patrones distinguen los globos sin percepción de color.
9. El objetivo táctil de cada tarjeta es ≥ 48×48dp.
10. El layout responsive funciona correctamente en móvil (≥ 320px), tablet y escritorio.
11. `ColorVisionExamples.vue` ha sido eliminado y no hay imports rotos.
12. `draft.colorVisionMode` se actualiza correctamente al seleccionar una tarjeta.
13. El toggle sigue funcionando: activo → muestra tarjetas, inactivo → `colorVisionMode = NONE`.
14. TypeScript compila sin errores (`vue-tsc --noEmit`).

## Evidencias esperadas

- Test manual: activar toggle → se muestran 9 tarjetas con nombre, explicación y muestra de globos.
- Test manual: hover sobre DEUTERANOPIA → los globos muestran simulación de deuteranopia.
- Test manual: focus con Tab sobre PROTANOPIA → los globos muestran simulación de protanopia.
- Test manual: tap táctil sobre TRITANOPIA → preview + selección simultáneos.
- Test manual: seleccionar ACHROMATOPSIA → globos en escala de grises + tarjeta marcada.
- Test manual: desactivar toggle → `colorVisionMode = NONE`.
- Test manual: guardar cambios → backend recibe el `colorVisionMode` correcto.
- Test manual: verificar que los patrones (● ▲ ■) son distinguibles en modo escala de grises del SO.
- Test manual: verificar objetivo táctil ≥ 48dp en todas las tarjetas.
- Test manual: verificar layout en 320px, 375px, 768px y 1024px.
- `vue-tsc --noEmit` sin errores en archivos del sprint.

## Dependencias bloqueantes

- [x] SPRINT-028 completado y verificado.
- [x] Enum `ColorVisionMode` con 9 valores disponible.
- [x] ADR-023 aceptada.

**No hay dependencias bloqueantes de backend, agents ni TTS.**

## Handoffs a otras capas

### Backend:
- Sin cambios requeridos. El enum `colorVisionMode` ya tiene los 9 valores.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Estado del sprint

**PENDIENTE** — No bloqueado. SPRINT-028 verificado y ADR-023 aceptada.

### Orden de ejecución

- **Depende de:** SPRINT-028 (completado y verificado)
- **Puede ejecutarse en paralelo con:** cualquier sprint de otra capa

### Privacidad infantil

- El selector es exclusivo del panel parental (adultos autenticados).
- El niño no ve ni interactúa con las tarjetas.
- No se recogen, almacenan ni infieren datos médicos o visuales del menor.
- La preferencia visual permanece bajo control parental y vinculada al perfil familiar.

### Relación con FEAT-006

Este sprint satisface los requisitos 14-22 del FEAT-006 actualizado:
- Req. 14: Tarjetas visuales sustituyen el menú desplegable.
- Req. 15: Cada tarjeta muestra nombre, explicación cotidiana y muestra de globos con patrones.
- Req. 16: Previsualización por puntero, foco y selección. Táctil sin hover.
- Req. 17: La muestra no contiene evaluaciones ni recomendaciones.
- Req. 18: Aviso visible de carácter orientativo.
- Req. 19-22: Accesibilidad, controles no dependientes del color, separación de experiencias.
