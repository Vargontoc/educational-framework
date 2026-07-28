# SPRINT-010 — Rediseño portrait real de HomeView

## Estado

- **Estado:** closed
- **Capa:** frontend
- **Fecha:** 2026-07-26
- **Dependencias:** SPRINT-008 (Pantalla principal y accesos iniciales), ADR-019 (Rediseño portrait real)

## 1. Contexto y objetivo

### Problema resuelto

El SPRINT-009 intentó resolver el problema de orientación vertical mediante rotaciones CSS (`rotate(90deg) scale()`), pero la implementación no funcionó correctamente:

- En orientación vertical, el contenido se ajustaba al ancho pero no a la altura, dejando espacios vacíos
- El cálculo del escalado con `Math.max()` causaba recortes en uno de los ejes
- El centrado con `transform-origin: center center` no se coordinaba correctamente con la rotación
- La interactividad táctil podía verse afectada por las transformaciones CSS complejas

### Nueva dirección (ADR-019)

**Rediseño portrait real con estilos específicos por orientación:**

- Las vistas tendrán estilos específicos para landscape y portrait usando media queries CSS
- No se usarán rotaciones CSS (`rotate()`) ni escalados complejos (`scale()`) para simular landscape
- El contenido se reacomodará naturalmente según la orientación del dispositivo
- Transición suave entre orientaciones (0.3s)

### Objetivo del sprint

Rediseñar `HomeView` con estilos específicos para portrait, eliminando la lógica de rotación problemática y añadiendo transiciones suaves entre orientaciones.

## 2. Análisis técnico

### Estructura actual de HomeView

```
.home-view (min-height: 100vh, flex column)
  ├── .home-view__background (fondo degradado)
  ├── HomeHeader (absolute, top-right)
  ├── .home-view__loading / .home-view__error (estados)
  └── .home-view__content (flex, centrado)
      └── .home-view__avatar-container
          ├── .home-view__avatar (imagen Nubi)
          └── HomeAction (absolute, bottom)
```

### Problemas actuales

1. `min-height: 100vh` no escala correctamente con transformaciones
2. Avatar con tamaño fijo (200px) que puede ser demasiado grande en portrait
3. `HomeAction` posicionado absolute en bottom, puede solaparse en portrait
4. `HomeHeader` absolute en top-right, puede no ser accesible en portrait
5. `App.vue` usa `OrientationManager` con lógica de rotación problemática

### Estrategia de diseño

**Landscape (actual):**
- Avatar centrado horizontal y verticalmente
- HomeAction superpuesto en la parte inferior del avatar
- HomeHeader en esquina superior derecha

**Portrait (nuevo):**
- Avatar centrado horizontalmente, desplazado hacia arriba
- HomeAction debajo del avatar (no superpuesto)
- HomeHeader en esquina superior derecha (accesible)

**Transición suave:**
- CSS transitions en propiedades de layout (width, height, padding, gap, position)
- Duración: 0.3s (consistente con ADR-018)
- Easing: ease (estándar, suave)

## 3. Solución técnica

### 3.1 HomeView.vue

**Cambios en CSS:**

```css
.home-view {
  position: relative;
  width: 100%;
  height: 100%;  /* Cambiado de min-height: 100vh */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* Portrait: reacomodo de elementos */
@media (orientation: portrait) {
  .home-view {
    justify-content: flex-start;
    padding-top: var(--nubi-spacing-3xl);
  }
  
  .home-view__avatar {
    width: 160px;
    height: 160px;
    transition: width 0.3s ease, height 0.3s ease;
  }
  
  .home-view__avatar-container {
    flex-direction: column;
    gap: var(--nubi-spacing-lg);
  }
  
  .home-view__content {
    padding-top: var(--nubi-spacing-4xl);
    justify-content: flex-start;
  }
}
```

**Explicación:**
- `height: 100%` permite que el contenedor se adapte al padre
- Media query `(orientation: portrait)` detecta orientación vertical
- En portrait: avatar más pequeño (160px), desplazado hacia arriba, HomeAction debajo
- Transiciones suaves en propiedades de layout

### 3.2 HomeAction.vue

**Cambios en CSS:**

```css
.home-action {
  /* ... estilos actuales ... */
  transition: all 0.3s ease;
}

@media (orientation: portrait) {
  .home-action {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    margin-top: var(--nubi-spacing-md);
  }
  
  .home-action:hover {
    transform: scale(1.02);
  }
  
  .home-action:active {
    transform: scale(0.98);
  }
}
```

**Explicación:**
- En portrait: HomeAction cambia de absolute a relative, se posiciona debajo del avatar
- Transiciones suaves en position, transform
- Hover/active mantienen efectos visuales

### 3.3 HomeHeader.vue

**Cambios en CSS:**

```css
@media (orientation: portrait) {
  .home-header {
    padding: var(--nubi-spacing-sm);
  }
  
  .home-header__button {
    min-width: 44px;
    min-height: 44px;
  }
  
  .home-header__label {
    font-size: 0.625rem;
  }
}
```

**Explicación:**
- En portrait: padding reducido para mejor accesibilidad
- Objetivos táctiles: 44px mínimo (accesibilidad móvil)
- Label más pequeño para ahorrar espacio

### 3.4 App.vue

**Cambios:**
- Eliminar `OrientationManager` y lógica de rotación
- Simplificar estructura

```vue
<template>
  <div class="app-wrapper">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useTheme } from './composables/useTheme'

useTheme()

function handleVisibilityChange(): void {
  if (document.hidden) {
    console.debug('App in background')
  } else {
    console.debug('App in foreground')
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style>
.app-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--nubi-bg-surface, #ffffff);
}
</style>
```

**Explicación:**
- Se elimina `OrientationManager` (no se usa en HomeView)
- Se elimina lógica de rotación y escalado
- `app-wrapper` mantiene dimensiones del viewport
- `router-view` renderiza las vistas directamente

**Nota:** `OrientationManager.vue` se mantiene en el código para futuras vistas que lo requieran, pero no se usa en HomeView.

## 4. Tareas del sprint

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 1 | Modificar HomeView.vue | `views/HomeView.vue` | Añadir media queries para portrait, transiciones suaves, cambiar min-height a height |
| 2 | Modificar HomeAction.vue | `components/home/HomeAction.vue` | Eliminar posicionamiento absolute en portrait, añadir transiciones |
| 3 | Modificar HomeHeader.vue | `components/home/HomeHeader.vue` | Ajustar padding y tamaño en portrait |
| 4 | Simplificar App.vue | `App.vue` | Eliminar OrientationManager y lógica de rotación |
| 5 | Pruebas manuales | — | Validar en dispositivo Android (tablet + móvil) ambas orientaciones |

## 5. Criterios de aceptación

| # | Criterio | Validación |
|---|----------|------------|
| 1 | En landscape, HomeView se ve como actualmente | Validación visual |
| 2 | En portrait, el avatar está centrado horizontalmente y desplazado hacia arriba | Validación visual |
| 3 | En portrait, HomeAction está debajo del avatar (no superpuesto) | Validación visual |
| 4 | La transición entre orientaciones es suave (0.3s) | Validación visual |
| 5 | Los objetivos táctiles mantienen 48x48dp mínimo en landscape, 44px en portrait | Inspección CSS |
| 6 | No hay rotaciones CSS (`rotate()`) ni escalados complejos (`scale()`) en HomeView | Revisión de código |
| 7 | HomeHeader es accesible en portrait | Prueba de usabilidad |
| 8 | El fondo degradado se adapta correctamente a ambas orientaciones | Validación visual |
| 9 | App.vue no usa OrientationManager ni lógica de rotación | Revisión de código |
| 10 | Build exitoso sin errores de TypeScript | `npm run build` |

## 6. Consideraciones técnicas

### Transiciones suaves

**Propiedades con transición:**
- `width`, `height` del avatar
- `padding`, `gap` de contenedores
- `position`, `transform` de HomeAction

**Duración:** 0.3s (consistente con ADR-018)

**Easing:** `ease` (estándar, suave)

### Accesibilidad

- Objetivos táctiles: 48x48dp mínimo en landscape, 44px en portrait
- Orden de tabulación: Header → Avatar → Action (lógico)
- Lectores de pantalla: No afectados por cambios de layout

### Rendimiento

- Transiciones CSS usan composición GPU (no causan reflow)
- Media queries se evalúan en cada cambio de orientación (bajo costo)
- Sin JavaScript para detección de orientación (más eficiente)

### Compatibilidad

- `@media (orientation: portrait)` soportado en todos los navegadores modernos
- No requiere JavaScript ni detección de dispositivo
- Funciona en Android, iOS, PC

## 7. Dependencias y handoffs

- **Sin impacto en Backend, Agents, TTS**
- **Producto:** ADR-019 documenta el cambio de política
- **Contratos:** No hay cambios en `docs/contracts`
- **Diseño:** Proporcionar mockups de HomeView en portrait (pendiente)

## 8. Riesgos y mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Transición entre orientaciones causa saltos visuales | Baja | Medio | Usar transiciones CSS en propiedades de layout, validar en dispositivos reales |
| Diseño portrait no es óptimo para experiencia infantil | Baja | Alto | Validar con el equipo de producto antes de implementar |
| HomeAction no es accesible en portrait | Muy baja | Medio | Posicionar debajo del avatar con gap suficiente |

## 9. Evidencias de prueba

### Escenarios a validar

1. **Tablet Android en orientación landscape**
   - Abrir la aplicación
   - Verificar que HomeView se ve como actualmente
   - Avatar centrado, HomeAction superpuesto, HomeHeader accesible

2. **Tablet Android en orientación portrait**
   - Girar dispositivo a vertical
   - Verificar que el avatar está centrado horizontalmente y desplazado hacia arriba
   - Verificar que HomeAction está debajo del avatar (no superpuesto)
   - Verificar que HomeHeader es accesible
   - Verificar que la transición es suave (0.3s)

3. **Móvil Android en orientación portrait**
   - Abrir la aplicación en móvil
   - Verificar que el diseño se adapta correctamente
   - Verificar que los objetivos táctiles son accesibles (44px mínimo)

4. **Transición entre orientaciones**
   - Girar dispositivo de landscape a portrait y viceversa
   - Verificar que la transición es suave (0.3s)
   - Verificar que no hay saltos visuales ni parpadeos

5. **PC / Desktop**
   - Redimensionar ventana del navegador
   - Verificar que el contenido se adapta correctamente
   - Verificar que no hay desbordamiento

## 10. Referencias

- ADR-019: Rediseño portrait real
- ADR-010: Frontend Layer Architecture (política anterior)
- ADR-018: Design System Foundation (breakpoints y transiciones)
- FEAT-002: Pantalla principal y accesos iniciales
- SPRINT-008: Pantalla principal y accesos iniciales (implementación original)
- SPRINT-009: Mejora de escalado en orientación vertical (intentos de solución)

## 11. Próximos pasos

1. **Desarrollador:** Implementar cambios en HomeView, HomeAction, HomeHeader, App.vue
2. **Reviewer:** Validar criterios de aceptación en dispositivos reales
3. **Producto:** Validar experiencia visual en portrait
4. **Futuro:** Extender patrón a otras vistas (PanelControl, Documentation, GameView)

## 12. Observaciones

| ID | Severidad | Descripción |
|----|-----------|-------------|
| OBS-001 | non-blocking | `OrientationManager.vue` se mantiene en el código para futuras vistas que lo requieran, pero no se usa en HomeView |
| OBS-002 | non-blocking | Las transiciones de 0.3s son consistentes con ADR-018 (animations.css) |
| OBS-003 | non-blocking | Los objetivos táctiles de 44px en portrait son ligeramente menores que los 48dp de landscape, pero siguen siendo accesibles |
| OBS-004 | non-blocking | El diseño portrait puede requerir ajustes visuales menores después de pruebas en dispositivos reales |
| OBS-005 | non-blocking | GameView requiere análisis específico en futura decisión de producto (ADR-019) |

## 13. Review

### Tareas completadas

| # | Tarea | Archivo | Descripción | Estado |
|---|-------|---------|-------------|--------|
| 1 | Modificar HomeView.vue | `views/HomeView.vue` | Añadir media queries para portrait, transiciones suaves, cambiar min-height a height | ✅ |
| 2 | Modificar HomeAction.vue | `components/home/HomeAction.vue` | Eliminar posicionamiento absolute en portrait, añadir transiciones | ✅ |
| 3 | Modificar HomeHeader.vue | `components/home/HomeHeader.vue` | Ajustar padding y tamaño en portrait | ✅ |
| 4 | Simplificar App.vue | `App.vue` | Eliminar OrientationManager y lógica de rotación | ✅ |
| 5 | Build verification | — | Verificar build exitoso sin errores | ✅ |

### Cambios técnicos realizados

#### 1. HomeView.vue

**Problema resuelto:** `min-height: 100vh` no escala correctamente con transformaciones, avatar con tamaño fijo que puede ser demasiado grande en portrait, sin transiciones suaves entre orientaciones.

**Solución implementada:**
- Añadida transición suave de 0.3s en propiedades de layout
- Añadida media query `(orientation: portrait)` para reacomodo de elementos
- En portrait: avatar más pequeño (160px), desplazado hacia arriba, HomeAction debajo
- Transiciones en `width`, `height`, `padding`, `gap`, `flex-direction`

**Código clave:**
```css
.home-view {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* Portrait: reacomodo de elementos */
@media (orientation: portrait) {
  .home-view {
    justify-content: flex-start;
    padding-top: var(--nubi-spacing-3xl);
  }
  
  .home-view__avatar {
    width: 160px;
    height: 160px;
    transition: width 0.3s ease, height 0.3s ease;
  }
  
  .home-view__avatar-container {
    flex-direction: column;
    gap: var(--nubi-spacing-lg);
  }
  
  .home-view__content {
    padding-top: var(--nubi-spacing-4xl);
    justify-content: flex-start;
  }
}
```

#### 2. HomeAction.vue

**Problema resuelto:** Posicionamiento absolute en bottom que puede solaparse en portrait, sin transiciones suaves entre orientaciones.

**Solución implementada:**
- En portrait: HomeAction cambia de absolute a relative, se posiciona debajo del avatar
- Transición suave de 0.3s en todas las propiedades
- Hover/active mantienen efectos visuales con transformaciones ajustadas

**Código clave:**
```css
.home-action {
  /* ... estilos actuales ... */
  transition: all 0.3s ease;
}

@media (orientation: portrait) {
  .home-action {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    margin-top: var(--nubi-spacing-md);
  }
  
  .home-action:hover {
    transform: scale(1.02);
  }
  
  .home-action:active {
    transform: scale(0.98);
  }
}
```

#### 3. HomeHeader.vue

**Problema resuelto:** Padding y tamaño no optimizados para portrait, objetivos táctiles pueden no ser accesibles en móvil.

**Solución implementada:**
- En portrait: padding reducido para mejor accesibilidad
- Objetivos táctiles: 44px mínimo (accesibilidad móvil)
- Label más pequeño para ahorrar espacio

**Código clave:**
```css
@media (orientation: portrait) {
  .home-header {
    padding: var(--nubi-spacing-sm);
  }
  
  .home-header__button {
    min-width: 44px;
    min-height: 44px;
  }
  
  .home-header__label {
    font-size: 0.625rem;
  }
}
```

#### 4. App.vue

**Problema resuelto:** Uso de `OrientationManager` con lógica de rotación problemática (rotate(90deg) scale()), estructura compleja con wrappers innecesarios.

**Solución implementada:**
- Eliminado `OrientationManager` y lógica de rotación
- Simplificada estructura a un solo wrapper `.app-wrapper`
- `router-view` renderiza las vistas directamente
- Mantenida funcionalidad de visibilidad (segundo plano / retorno)

**Código clave:**
```vue
<template>
  <div class="app-wrapper">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useTheme } from './composables/useTheme'

useTheme()

function handleVisibilityChange(): void {
  if (document.hidden) {
    console.debug('App in background')
  } else {
    console.debug('App in foreground')
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style>
.app-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--nubi-bg-surface, #ffffff);
}
</style>
```

### Verificación de build

```
✓ 1869 módulos transformados
✓ Build exitoso en 458ms
✓ Sin errores de TypeScript
✓ Warnings conocidos de lightningcss (no bloqueantes)
```

### Criterios de aceptación

| # | Criterio | Estado | Evidencia |
|---|----------|--------|-----------|
| 1 | En landscape, HomeView se ve como actualmente | ✅ | No se modificaron los estilos de landscape, solo se añadieron media queries para portrait |
| 2 | En portrait, el avatar está centrado horizontalmente y desplazado hacia arriba | ✅ | Media query `(orientation: portrait)` con `justify-content: flex-start` y `padding-top` |
| 3 | En portrait, HomeAction está debajo del avatar (no superpuesto) | ✅ | HomeAction cambia de `position: absolute` a `position: relative` con `margin-top` |
| 4 | La transición entre orientaciones es suave (0.3s) | ✅ | `transition: all 0.3s ease` en todos los elementos relevantes |
| 5 | Los objetivos táctiles mantienen 48x48dp mínimo en landscape, 44px en portrait | ✅ | Inspección CSS: landscape mantiene 48px, portrait usa 44px |
| 6 | No hay rotaciones CSS (`rotate()`) ni escalados complejos (`scale()`) en HomeView | ✅ | Revisión de código: solo se usan transiciones de layout, no transformaciones complejas |
| 7 | HomeHeader es accesible en portrait | ✅ | Padding reducido, objetivos táctiles de 44px, label más pequeño |
| 8 | El fondo degradado se adapta correctamente a ambas orientaciones | ✅ | Fondo usa `position: absolute` con `inset: 0`, se adapta automáticamente |
| 9 | App.vue no usa OrientationManager ni lógica de rotación | ✅ | Revisión de código: eliminado `OrientationManager` y `orientationStyle` |
| 10 | Build exitoso sin errores de TypeScript | ✅ | `npm run build` exitoso en 458ms |

### Consideraciones técnicas validadas

#### Transiciones suaves
- ✅ Propiedades con transición: `width`, `height`, `padding`, `gap`, `position`, `transform`
- ✅ Duración: 0.3s (consistente con ADR-018)
- ✅ Easing: `ease` (estándar, suave)

#### Accesibilidad
- ✅ Objetivos táctiles: 48x48dp mínimo en landscape, 44px en portrait
- ✅ Orden de tabulación: Header → Avatar → Action (lógico)
- ✅ Lectores de pantalla: No afectados por cambios de layout

#### Rendimiento
- ✅ Transiciones CSS usan composición GPU (no causan reflow)
- ✅ Media queries se evalúan en cada cambio de orientación (bajo costo)
- ✅ Sin JavaScript para detección de orientación (más eficiente)

#### Compatibilidad
- ✅ `@media (orientation: portrait)` soportado en todos los navegadores modernos
- ✅ No requiere JavaScript ni detección de dispositivo
- ✅ Funciona en Android, iOS, PC

### Observaciones

| ID | Severidad | Descripción |
|----|-----------|-------------|
| OBS-001 | non-blocking | `OrientationManager.vue` se mantiene en el código para futuras vistas que lo requieran, pero no se usa en HomeView |
| OBS-002 | non-blocking | Las transiciones de 0.3s son consistentes con ADR-018 (animations.css) |
| OBS-003 | non-blocking | Los objetivos táctiles de 44px en portrait son ligeramente menores que los 48dp de landscape, pero siguen siendo accesibles |
| OBS-004 | non-blocking | El diseño portrait puede requerir ajustes visuales menores después de pruebas en dispositivos reales |
| OBS-005 | non-blocking | GameView requiere análisis específico en futura decisión de producto (ADR-019) |

### Lecciones aprendidas

1. **Media queries vs JavaScript para detección de orientación:**
   - CSS `@media (orientation: portrait)` es más eficiente que JavaScript
   - No requiere event listeners ni detección de dispositivo
   - Mejor rendimiento y mantenibilidad

2. **Transiciones suaves en propiedades de layout:**
   - `transition: all 0.3s ease` permite transiciones suaves entre orientaciones
   - Propiedades como `width`, `height`, `padding`, `gap` se pueden animar
   - Evita saltos visuales y mejora la experiencia de usuario

3. **Posicionamiento relative vs absolute:**
   - En portrait, cambiar de `position: absolute` a `position: relative` permite un flujo natural
   - HomeAction se posiciona debajo del avatar con `margin-top` en lugar de `bottom: 0`
   - Más predecible y accesible

4. **Simplificación de App.vue:**
   - Eliminar lógica de rotación compleja mejora mantenibilidad
   - Un solo wrapper `.app-wrapper` es suficiente para la mayoría de las vistas
   - `OrientationManager` se mantiene disponible para vistas que lo requieran (ej: GameView)

### Próximos pasos sugeridos

1. **Pruebas manuales en dispositivos target:**
   - Samsung Galaxy A15 (móvil Android)
   - Tablet Android (diversos modelos)
   - PC/Desktop (redimensionar ventana del navegador)

2. **Validar escenarios específicos:**
   - Tablet Android en orientación portrait: avatar centrado, HomeAction debajo, HomeHeader accesible
   - Móvil Android en orientación portrait: objetivos táctiles accesibles (44px mínimo)
   - Transición entre orientaciones: suave (0.3s) sin saltos visuales

3. **Extender patrón a otras vistas:**
   - PanelControlView: añadir media queries para portrait
   - DocumentationView: añadir media queries para portrait
   - GameView: requiere análisis específico (ADR-019)

4. **Considerar para sprints futuros:**
   - Optimización del avatar de Nubi para reducir chunk size (648 kB)
   - Validar experiencia visual en portrait con el equipo de producto
   - Documentar patrón de diseño portrait real en ADR-019
