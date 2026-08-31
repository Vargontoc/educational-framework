# SPRINT-039 — Lectura: navegación de páginas y controles bajo demanda

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-27
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-038 (vista previa, sesión de lectura, precarga)
- **Impacto estimado:** Pantalla de lectura con swipe/transición entre páginas y controles de navegación que aparecen solo al tocar la pantalla, según la decisión confirmada.

## Objetivo

Implementar la pantalla de lectura: navegación de páginas por swipe y flechas, transición fluida y sobria, y controles que permanecen ocultos por defecto, apareciendo solo al tocar la pantalla (estilo lector de cómics).

## Contexto

**Decisión confirmada (2026-08-27):** los controles de navegación (flechas, volver al catálogo) no están permanentemente visibles; aparecen al tocar la pantalla, para no competir visualmente con el cuento (FEAT-008 §3 req 12, "evitando elementos competitivos").

Esta pantalla consume el `StoryDetail` ya cargado y precargado por SPRINT-038 vía `useStoryReadingSession`; no vuelve a pedir el detalle al backend. Todavía no reproduce audio (eso es SPRINT-040) — solo sienta la navegación visual.

## Diseño funcional-técnico

### 1. `composables/useStoryReader.ts` (nuevo)

```typescript
export function useStoryReader(pages: Ref<StoryPage[]>) {
  const currentPageIndex = ref(0)
  const currentPage = computed(() => pages.value[currentPageIndex.value])
  const isFirstPage = computed(() => currentPageIndex.value === 0)
  const isLastPage = computed(() => currentPageIndex.value === pages.value.length - 1)

  function goNext() {
    if (!isLastPage.value) currentPageIndex.value++
  }
  function goPrevious() {
    if (!isFirstPage.value) currentPageIndex.value--
  }

  return { currentPageIndex, currentPage, isFirstPage, isLastPage, goNext, goPrevious }
}
```

### 2. `composables/useTapControlsVisibility.ts` (nuevo)

```typescript
const HIDE_DELAY_MS = 3000

export function useTapControlsVisibility() {
  const visible = ref(true)
  let hideTimer: ReturnType<typeof setTimeout> | null = null

  function scheduleHide() {
    if (hideTimer) clearTimeout(hideTimer)
    hideTimer = setTimeout(() => { visible.value = false }, HIDE_DELAY_MS)
  }

  function toggle() {
    visible.value = !visible.value
    if (visible.value) scheduleHide()
    else if (hideTimer) clearTimeout(hideTimer)
  }

  function show() {
    visible.value = true
    scheduleHide()
  }

  onMounted(scheduleHide)
  onUnmounted(() => { if (hideTimer) clearTimeout(hideTimer) })

  return { visible, toggle, show }
}
```

Se muestran los controles unos segundos al entrar (para que el adulto sepa que existen) y luego se ocultan; cualquier tap los reactiva.

### 3. `StoryPageView.vue` (nuevo)

Imagen (`imageUrl`) + texto de la página actual. Transición con `translateX` usando los tokens de duración/easing ya utilizados en el resto del panel (`--nubi-duration-fast`, `--nubi-ease-in-out`), sobria, sin efectos tipo "page flip 3D".

### 4. `StoryReaderControls.vue` (nuevo)

Flechas anterior/siguiente (deshabilitadas en `isFirstPage`/`isLastPage`) + botón volver al catálogo. Visibles solo si `visible` (de `useTapControlsVisibility`) es `true`. Reutiliza `NubiIcon`/`NubiButton`; objetivo táctil ≥48px.

### 5. `PanelLecturaFamiliarLecturaView.vue` (nuevo)

- `onMounted`: lee `useStoryReadingSession().story`.
  - Si es `null` (deep link o refresh sin pasar por la previa) → `router.replace` a la ruta de la previa del `storyId` de la URL, para forzar el flujo correcto y evitar cualquier reproducción sin decisión previa del adulto.
- Gestiona swipe táctil (`touchstart`/`touchend` con umbral mínimo, p. ej. 50px) invocando `goNext`/`goPrevious`.
- Tap en la pantalla (fuera de las flechas) → `toggle()` de `useTapControlsVisibility`.
- Ensambla `StoryPageView.vue` + `StoryReaderControls.vue`.

### 6. Ruta nueva

```typescript
{
  path: 'lectura-familiar/:storyId/leer',
  name: 'PanelLecturaFamiliarLectura',
  component: () => import('../views/PanelLecturaFamiliarLecturaView.vue')
}
```

## Contratos y dependencias externas

Ninguno nuevo — usa el `StoryDetail` ya cargado en SPRINT-038 vía `useStoryReadingSession`.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | El gesto de swipe puede confundirse con el gesto nativo de "volver atrás" del navegador en Android/iOS | MEDIA | Umbral de distancia mínima (≈50px) y no interceptar el swipe si empieza muy cerca del borde de la pantalla. |
| R2 | Ocultar los controles por completo podría dejar al adulto sin saber cómo volver al catálogo | BAJA | Controles visibles unos segundos al entrar (`scheduleHide` en el mount); cualquier tap los reactiva. |
| R3 | Acceso directo a la URL de lectura sin pasar por la previa deja la sesión vacía | BAJA | Redirección automática a la previa del `storyId`, coherente con no reproducir audio sin decisión explícita del adulto. |

---

## Tareas del sprint

### Tarea 39.1: Implementar `useStoryReader.ts`

**Criterios de aceptación:** `goNext`/`goPrevious` respetan los límites del cuento; `currentPage` refleja siempre `currentPageIndex`.

### Tarea 39.2: Implementar `useTapControlsVisibility.ts`

**Criterios de aceptación:** controles visibles al montar, ocultos tras `HIDE_DELAY_MS` de inactividad, reaparecen con `toggle()`/`show()`.

### Tarea 39.3: Implementar `StoryPageView.vue`

**Criterios de aceptación:** transición fluida y sobria entre páginas; imagen y texto legibles en portrait móvil y landscape tablet.

### Tarea 39.4: Implementar `StoryReaderControls.vue`

**Criterios de aceptación:** flechas deshabilitadas en primera/última página; visibilidad condicional a `useTapControlsVisibility`; objetivo táctil ≥48px.

### Tarea 39.5: Implementar `PanelLecturaFamiliarLecturaView.vue`

**Criterios de aceptación:** swipe funcional con umbral; redirección a la previa si no hay sesión activa; tap reactiva los controles.

### Tarea 39.6: Añadir ruta `lectura-familiar/:storyId/leer`

**Criterios de aceptación:** navegación funcional desde la previa (SPRINT-038).

### Tarea 39.7: i18n de las cadenas nuevas

**Criterios de aceptación:** ninguna cadena visible como literal en template.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/composables/useStoryReader.ts` | Nuevo |
| `framework/frontend/app/src/composables/useTapControlsVisibility.ts` | Nuevo |
| `framework/frontend/app/src/components/lectura-familiar/StoryPageView.vue` | Nuevo |
| `framework/frontend/app/src/components/lectura-familiar/StoryReaderControls.vue` | Nuevo |
| `framework/frontend/app/src/views/PanelLecturaFamiliarLecturaView.vue` | Nuevo |
| `framework/frontend/app/src/router/index.ts` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media-Alta (gestos táctiles)
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. El adulto puede pasar páginas hacia adelante y hacia atrás, con swipe o con las flechas. *(FEAT-008 AC7)*
2. La transición entre páginas es fluida y sobria, sin resultar llamativa. *(FEAT-008 req 7)*
3. Los controles de navegación permanecen ocultos por defecto tras unos segundos y reaparecen al tocar la pantalla. *(Decisión confirmada)*
4. El botón "volver al catálogo" sigue siendo alcanzable en cualquier momento tocando la pantalla.
5. Acceder directamente a la URL de lectura sin pasar por la previa redirige a la previa del cuento correspondiente.
6. Ningún control requiere interacción del niño — todo pensado para el adulto. *(FEAT-008 req 11)*
7. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-038 completado (sesión de lectura + detalle precargado).

## Handoffs a otras capas

- Ninguno.

## Notas adicionales

Esta pantalla todavía no reproduce audio — eso se añade en SPRINT-040 sobre la misma navegación y los mismos controles.
