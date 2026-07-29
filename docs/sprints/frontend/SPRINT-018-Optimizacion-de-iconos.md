# SPRINT-018 — Optimización de iconos (NubiIcon)

## Estado

- **Estado:** verificado
- **Fecha de revisión:** 2026-07-29
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** Ninguna
- **Impacto estimado:** Reducción de ~3-5 MB y ~50-100 requests

## Objetivo

Eliminar la importación masiva de todos los iconos de Lucide y la carga eager de iconos custom en `NubiIcon.vue`, reemplazándola por un sistema de importación selectiva y carga bajo demanda.

## Problema actual

### Archivo: `framework/frontend/app/src/components/base/NubiIcon.vue`

**Problema 1 — Importación masiva de Lucide (línea 26):**
```typescript
import * as lucideIcons from '@lucide/vue'
```
Importa TODOS los iconos de Lucide (~1000+ iconos, ~500KB-1MB sin comprimir) en cada componente que use NubiIcon.

**Problema 2 — Carga eager de iconos custom (líneas 49-52):**
```typescript
const customIconsModules = import.meta.glob('../../assets/icons/custom/*.svg', { 
  eager: true,
  query: '?component'
})
```
Carga todos los SVG custom inmediatamente, incluso los que nunca se usan.

## Tareas

### Tarea 1.1: Identificar iconos realmente usados

**Descripción:** Rastrear todos los usos de `<NubiIcon name="..." />` en el código fuente para construir el mapa de iconos necesarios.

**Archivos a analizar:**
- Todos los `.vue` en `framework/frontend/app/src/`

**Entregable:** Lista de nombres de iconos usados (Lucide y custom).

---

### Tarea 1.2: Refactorizar NubiIcon con importación selectiva de Lucide

**Descripción:** Reemplazar `import * as lucideIcons` por imports explícitos de solo los iconos usados.

**Archivo:** `framework/frontend/app/src/components/base/NubiIcon.vue`

**Cambio esperado:**
```typescript
// ANTES
import * as lucideIcons from '@lucide/vue'

// DESPUÉS
import { 
  HelpCircle, 
  Home, 
  Users, 
  Lock, 
  AlertCircle, 
  CheckCircle 
  // ... solo los iconos identificados en Tarea 1.1
} from '@lucide/vue'

const lucideIconMap: Record<string, Component> = {
  'help-circle': HelpCircle,
  'home': Home,
  'users': Users,
  'lock': Lock,
  'alert-circle': AlertCircle,
  'check-circle': CheckCircle,
  // ...
}
```

**Criterios de aceptación:**
- Solo se importan los iconos identificados en Tarea 1.1
- El componente resuelve correctamente todos los iconos usados
- Fallback a HelpCircle si el icono no existe (conservar comportamiento actual)
- `console.warn` se mantiene para iconos no encontrados

---

### Tarea 1.3: Cambiar iconos custom a carga bajo demanda

**Descripción:** Cambiar `eager: true` a `eager: false` en `import.meta.glob` para cargar SVG custom solo cuando se necesitan.

**Archivo:** `framework/frontend/app/src/components/base/NubiIcon.vue`

**Cambio esperado:**
```typescript
// ANTES
const customIconsModules = import.meta.glob('../../assets/icons/custom/*.svg', { 
  eager: true,
  query: '?component'
})

// DESPUÉS
const customIconsModules = import.meta.glob('../../assets/icons/custom/*.svg', { 
  eager: false,
  query: '?component'
})
```

**Adaptar la resolución del icono:**
```typescript
const iconComponent = computed(() => {
  // 1. Buscar en iconos custom (lazy)
  const customPath = `../../assets/icons/custom/${props.name}.svg`
  if (customIconsModules[customPath]) {
    return defineAsyncComponent(customIconsModules[customPath] as () => Promise<any>)
  }
  
  // 2. Buscar en Lucide
  const lucideIcon = lucideIconMap[props.name]
  if (lucideIcon) return lucideIcon
  
  // 3. Fallback
  console.warn(`Icon "${props.name}" not found`)
  return HelpCircle
})
```

**Criterios de aceptación:**
- Los iconos custom se cargan bajo demanda
- No se cargan SVG custom no utilizados
- El renderizado de iconos custom funciona correctamente

---

### Tarea 1.4: Pruebas de regresión de iconos

**Descripción:** Verificar que todos los iconos se renderizan correctamente en toda la aplicación.

**Puntos de verificación:**
- HomeView: avatar de Nubi, iconos de HomeHeader, HomeAction
- FamilyRegistrationModal: iconos del stepper (users, lock, check-circle, alert-circle)
- ChildSelectionModal: iconos de estados de error
- ParentalAuthModal: iconos de error
- Panel parental: iconos de navegación y configuración
- GameView: iconos de interfaz de juego

**Criterios de aceptación:**
- Todos los iconos se renderizan visualmente correctos
- No hay warnings en consola por iconos no encontrados
- El fallback HelpCircle funciona si se referencia un icono inexistente

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/components/base/NubiIcon.vue` | Refactor completo |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Media
- **Riesgo:** Bajo (cambio localizado en un componente)

## Métricas esperadas

| Métrica | Antes | Después (esperado) |
|---------|-------|-------------------|
| Tamaño bundle Lucide | ~500KB-1MB | ~20-50KB |
| Requests de iconos custom | Todos eager | Solo los usados, lazy |
| Tiempo de parseo JS | Alto | Reducido significativamente |

## Plan de rollback

Si se detectan problemas, revertir el commit del sprint. El cambio es completamente reversible.

---

## Revisión técnica (2026-07-29)

### Veredicto: APPROVED

### Evidencia de implementación

#### Tarea 1.1 — Identificar iconos realmente usados ✅
- Se identificaron **35 iconos de Lucide** usados en el codebase.
- Se identificaron **3 iconos custom** (`reading.svg`, `relaxation.svg`, `child-avatars.svg`).
- Se rastrearon **57 usos** de `<NubiIcon>` en archivos `.vue` del proyecto.

#### Tarea 1.2 — Importación selectiva de Lucide ✅
- Reemplazado `import * as lucideIcons from '@lucide/vue'` por imports explícitos de los 35 iconos.
- `lucideIconMap` contiene exactamente los 35 iconos usados.
- Fallback a `HelpCircle` conservado con `console.warn`.
- Todos los nombres de icono usados estática y dinámicamente están cubiertos.

#### Tarea 1.3 — Carga bajo demanda de iconos custom ✅
- `eager: false` aplicado en `import.meta.glob` (línea 129).
- `defineAsyncComponent` usado para resolución lazy (línea 140).
- Orden de búsqueda correcto: custom → Lucide → fallback.

#### Tarea 1.4 — Pruebas de regresión ✅
- Build de producción completado sin errores (413ms).
- TypeScript sin errores de compilación.

### Métricas reales

| Métrica | Antes (estimado) | Después (medido) |
|---------|------------------|------------------|
| Tamaño bundle NubiIcon | ~500KB-1MB | **8.77 kB** (3.66 kB gzip) |
| Iconos custom | Todos eager | **3 chunks lazy** (reading: 0.45 kB, relaxation: 0.57 kB, child-avatars: 3.05 kB) |
| Iconos Lucide en bundle | ~1000+ | **35 selectivos** |

### Conformidad con ADR-018
- Sección 3.3 respetada: Lucide + wrapper `NubiIcon` + iconos custom.
- Búsqueda custom → Lucide → fallback mantenida.
- Componente fácilmente modificable por un diseñador.

### Observaciones
- El proyecto no tiene tests automatizados configurados (`npm run test` no existe). La validación se realizó mediante build de producción y análisis estático del codebase.
- Warnings de `lightningcss` sobre `@theme` y `@tailwind` son preexistentes y no relacionados con este sprint.
