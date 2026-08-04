# SPRINT-031 — Layout de documentación, navegación lateral y secciones estáticas

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-03
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-007 (aceptada), ADR-017 (documentación estática), SPRINT-001-contenido (textos estáticos en progreso)
- **Impacto estimado:** Nueva sección pública de documentación con layout propio, sidebar de 5 secciones, contenido estático desde archivos markdown y routing por sección. Reestructuración de rutas para eliminar `/panel/documentacion` y unificar en `/docs/:section`.

## Objetivo

Transformar el placeholder `DocumentationView.vue` en una sección pública completa con:
- Layout propio (`DocumentationLayout.vue`) independiente del `ParentPanelLayout`.
- Navegación lateral (`DocumentationSidebar.vue`) con 5 secciones: Quién soy, Primeros pasos, Agentes AI, Minijuegos, Contacto.
- Contenido estático cargado desde archivos markdown en build time.
- Routing por sección (`/docs/:section`).
- Eliminación de la ruta `/panel/documentacion` y migración de la navegación parental hacia `/docs`.

## Contexto

**FEAT-007** (aceptada 2026-08-03) requiere una sección pública de documentación con:
- Tres vías de entrada: Home, URL pública directa, panel parental.
- Navegación lateral propia con 5 secciones en orden específico.
- Independencia total del panel parental (no exponer opciones parentales).
- Contenido estático sin búsqueda (ADR-017).

**Estado actual:**
- `router/index.ts:86-89`: ruta `/docs` → `DocumentationView.vue` (placeholder).
- `router/index.ts:63-66`: ruta `/panel/documentacion` → `DocumentationView.vue` (dentro de `ParentPanelLayout`).
- `ParentPanelLayout.vue`: renderiza `ParentSidebar` con opciones parentales.
- `DocumentationView.vue`: placeholder sin contenido real.
- `HomeHeader.vue:67`: navega a `{ name: 'Documentation' }`.
- `ParentSidebar.vue:96`: enlace a `/panel/documentacion`.

**Decisión confirmada:** El contenido se carga desde archivos markdown (`.md`) importados en build time, no como texto hardcoded en i18n.

## Diseño funcional-técnico

### 1. Reestructuración de rutas

**Rutas nuevas:**
```
/docs                        → redirect a /docs/quien-soy
/docs/:section               → DocumentationLayout.vue
                               ├── DocSectionView.vue (quien-soy, primeros-pasos, agentes-ai, minijuegos)
                               └── ContactView.vue (contacto) → SPRINT-033
```

**Rutas eliminadas:**
- `/panel/documentacion` (name: `PanelDocumentacion`) → ELIMINAR

**Tabla de secciones:**

| Ruta | Sección | Componente |
|---|---|---|
| `/docs/quien-soy` | Quién soy | `DocSectionView.vue` |
| `/docs/primeros-pasos` | Primeros pasos | `DocSectionView.vue` |
| `/docs/agentes-ai` | Agentes AI | `DocSectionView.vue` |
| `/docs/minijuegos` | Minijuegos | `DocSectionView.vue` |
| `/docs/contacto` | Contacto | `ContactView.vue` (SPRINT-033) |

### 2. Componente `DocumentationLayout.vue`

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (nuevo)

**Responsabilidad:** Layout con sidebar de documentación propio, cabecera con botón "Volver" (lógica en SPRINT-032) y `<router-view />` para el contenido de sección. Sin `ParentSidebar`, sin `ThemeToggle`, sin `InactivityOverlay`.

**Estructura template:**
```vue
<template>
  <div class="documentation-layout">
    <header class="documentation-layout__header">
      <!-- Botón "Volver" condicional (SPRINT-032) -->
      <div class="documentation-layout__header-actions">
        <button
          class="documentation-layout__menu-toggle"
          @click="sidebarOpen = !sidebarOpen"
          aria-label="Abrir menú de navegación"
        >
          <NubiIcon name="menu" />
        </button>
      </div>
    </header>
    <div class="documentation-layout__body">
      <DocumentationSidebar
        :current-section="currentSection"
        :is-open="sidebarOpen"
        @close="sidebarOpen = false"
      />
      <main class="documentation-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>
```

**Estado interno:**
```typescript
const sidebarOpen = ref(false)
const route = useRoute()
const currentSection = computed(() => route.params.section as string)
```

### 3. Componente `DocumentationSidebar.vue`

**Archivo:** `framework/frontend/app/src/components/documentation/DocumentationSidebar.vue` (nuevo)

**Responsabilidad:** Navegación lateral con 5 secciones (icono + etiqueta), estado activo, responsive (drawer en móvil, fijo en tableta).

**Props:**
```typescript
interface Props {
  currentSection: string
  isOpen: boolean
}
```

**Emits:**
```typescript
interface Emits {
  (e: 'close'): void
}
```

**Secciones (orden confirmado):**
```typescript
const sections = [
  { id: 'quien-soy', label: 'Quién soy', icon: 'nubi-character', route: '/docs/quien-soy' },
  { id: 'primeros-pasos', label: 'Primeros pasos', icon: 'play', route: '/docs/primeros-pasos' },
  { id: 'agentes-ai', label: 'Agentes AI', icon: 'robot', route: '/docs/agentes-ai' },
  { id: 'minijuegos', label: 'Minijuegos', icon: 'gamepad', route: '/docs/minijuegos' },
  { id: 'contacto', label: 'Contacto', icon: 'mail', route: '/docs/contacto' }
]
```

**Responsive:**
- `≤1023px` (móvil): drawer colapsable, se abre/cierra con botón hamburger.
- `≥1024px` (tableta): sidebar visible permanentemente.

**Accesibilidad:**
- `role="navigation"` + `aria-label="Navegación de documentación"`.
- `aria-current="page"` en sección activa.
- Cada etiqueta: icono + texto visible (no solo icono o color).
- Objetivo táctil ≥ 48×48dp.

### 4. Componente `DocSectionView.vue`

**Archivo:** `framework/frontend/app/src/views/documentation/DocSectionView.vue` (nuevo)

**Responsabilidad:** Renderiza el contenido estático de la sección según `route.params.section`. Carga el archivo markdown correspondiente en build time.

**Mecanismo de carga de markdown:**
```typescript
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Importación de archivos markdown en build time
const modules = import.meta.glob('../../content/docs/*.md', { query: '?raw', import: 'default', eager: true })

const sectionContent = computed(() => {
  const section = route.params.section as string
  const modulePath = `../../content/docs/${section}.md`
  return modules[modulePath] || '# Sección no encontrada'
})
```

**Renderizado:** El contenido markdown se renderiza como HTML usando una función de parseo simple o una librería ligera (ver preguntas de decisión).

**Archivos de contenido esperados:**
- `framework/frontend/app/src/content/docs/quien-soy.md`
- `framework/frontend/app/src/content/docs/primeros-pasos.md`
- `framework/frontend/app/src/content/docs/agentes-ai.md`
- `framework/frontend/app/src/content/docs/minijuegos.md`

### 5. Actualización de `ParentSidebar.vue`

**Archivo:** `framework/frontend/app/src/components/parental/ParentSidebar.vue` (modificación)

**Cambio:** La opción "Documentación" debe navegar a `/docs` con `query.from` para el retorno contextual.

```typescript
// Antes:
// router.replace('/panel/documentacion')

// Después:
router.replace({ path: '/docs', query: { from: route.path } })
```

**Nota:** La lógica de "Volver" se implementa en SPRINT-032. En este sprint, solo se cambia el destino de la navegación.

### 6. Actualización de `HomeHeader.vue`

**Archivo:** `framework/frontend/app/src/views/HomeView.vue` o componente relevante (modificación)

**Cambio:** El botón de documentación debe navegar a `/docs` sin `query.from`.

```typescript
// Antes:
// router.replace({ name: 'Documentation' })

// Después:
router.replace('/docs')
```

### 7. i18n — Claves nuevas

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

```typescript
views: {
  docs: {
    sidebar: {
      label: 'Navegación de documentación',
      sections: {
        quienSoy: 'Quién soy',
        primerosPasos: 'Primeros pasos',
        agentesAi: 'Agentes AI',
        minijuegos: 'Minijuegos',
        contacto: 'Contacto'
      }
    },
    menuToggle: 'Abrir menú de navegación',
    notFound: 'Sección no encontrada'
  }
}
```

## Contratos y dependencias externas

### Contratos

- **Sin cambios.** Este sprint no consume endpoints backend.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Backend | Ninguna. | ✅ Sin bloqueo |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |
| Contenido | SPRINT-001-contenido: archivos markdown de las 4 secciones informativas. | ⏳ En progreso |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Los archivos markdown no están disponibles al inicio del sprint. | MEDIA | Usar placeholders mínimos para desarrollo. Los textos definitivos llegan desde Contenido. |
| R2 | El parseo de markdown puede introducir XSS si no se sanitiza. | ALTA | Usar DOMPurify o equivalente para sanitizar el HTML generado. |
| R3 | Eliminar `/panel/documentacion` puede romper tests o links existentes. | MEDIA | Verificar que ningún test o componente referencia esa ruta. Migrar completamente. |
| R4 | El sidebar drawer puede solapar contenido en móvil. | BAJA | Overlay con backdrop al abrir drawer. Cerrar al navegar entre secciones. |

## Preguntas de decisión al usuario

> **Todas las preguntas de decisión han sido resueltas y confirmadas (2026-08-03).**

### P1 — Parseo de markdown — ✅ Confirmada: Opción B

**Opción seleccionada:** Librería ligera `marked` + sanitización con `DOMPurify`.

**Justificación:** `marked` es una librería madura y ligera (~15KB gzipped). `DOMPurify` sanitiza el HTML generado previniendo XSS. Es la combinación más segura y mantenible.

### P2 — Ubicación de archivos markdown — ✅ Confirmada

**Ubicación seleccionada:** `framework/frontend/app/src/content/docs/*.md`

**Justificación:** Separación clara del código fuente. Importación en build time con `import.meta.glob`. No requiere configuración adicional de Vite.

---

## Tareas del sprint

### Tarea 31.1: Crear estructura de directorios y archivos markdown placeholder

**Descripción:** Crear el directorio `src/content/docs/` y los 4 archivos markdown placeholder para las secciones informativas.

**Archivos:**
- `framework/frontend/app/src/content/docs/quien-soy.md` (nuevo)
- `framework/frontend/app/src/content/docs/primeros-pasos.md` (nuevo)
- `framework/frontend/app/src/content/docs/agentes-ai.md` (nuevo)
- `framework/frontend/app/src/content/docs/minijuegos.md` (nuevo)

**Contenido placeholder:**
```markdown
# Quién soy

Contenido pendiente de aprobación por el equipo de Contenido.
```

**Criterios de aceptación:**
- Los 4 archivos existen en `src/content/docs/`.
- Cada archivo tiene un título H1 y contenido placeholder.
- TypeScript/Vite no reporta errores de importación.

---

### Tarea 31.2: Instalar dependencias `marked` y `dompurify`

**Descripción:** Instalar las librerías para parseo de markdown y sanitización HTML.

**Comando:**
```bash
npm install marked dompurify
npm install -D @types/dompurify
```

**Criterios de aceptación:**
- `marked` y `dompurify` aparecen en `package.json` dependencies.
- `@types/dompurify` aparece en devDependencies.
- `npm install` completa sin errores.

---

### Tarea 31.3: Crear utilidad `parseMarkdown.ts`

**Descripción:** Función que convierte markdown a HTML sanitizado.

**Archivo:** `framework/frontend/app/src/utils/parseMarkdown.ts` (nuevo)

**Interfaz:**
```typescript
import { marked } from 'marked'
import DOMPurify from 'dompurify'

export function parseMarkdown(content: string): string {
  const html = marked.parse(content, { async: false }) as string
  return DOMPurify.sanitize(html)
}
```

**Criterios de aceptación:**
- Convierte markdown a HTML correctamente.
- Sanitiza el HTML previniendo XSS.
- TypeScript compila sin errores.

---

### Tarea 31.4: Implementar `DocumentationSidebar.vue`

**Descripción:** Navegación lateral con 5 secciones, responsive, accesible.

**Archivo:** `framework/frontend/app/src/components/documentation/DocumentationSidebar.vue` (nuevo)

**Especificación completa:** Ver sección 3 del diseño funcional-técnico.

**Criterios de aceptación:**
- Muestra 5 secciones en el orden acordado: Quién soy, Primeros pasos, Agentes AI, Minijuegos, Contacto.
- Cada sección tiene icono + etiqueta de texto.
- Sección activa marcada con `aria-current="page"` y estilo visual diferenciado.
- En móvil (≤1023px): drawer colapsable con `isOpen` prop.
- En tableta (≥1024px): visible permanentemente.
- `role="navigation"` + `aria-label` presentes.
- Objetivo táctil ≥ 48×48dp.
- Navegación entre secciones con `router-link`.
- TypeScript compila sin errores.

---

### Tarea 31.5: Implementar `DocSectionView.vue`

**Descripción:** Vista que carga y renderiza contenido markdown según `route.params.section`.

**Archivo:** `framework/frontend/app/src/views/documentation/DocSectionView.vue` (nuevo)

**Especificación completa:** Ver sección 4 del diseño funcional-técnico.

**Criterios de aceptación:**
- Carga el archivo markdown correspondiente a `route.params.section`.
- Renderiza el contenido como HTML sanitizado.
- Si la sección no existe, muestra mensaje "Sección no encontrada".
- TypeScript compila sin errores.

---

### Tarea 31.6: Implementar `DocumentationLayout.vue`

**Descripción:** Layout con sidebar, cabecera y `<router-view />`.

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (nuevo)

**Especificación completa:** Ver sección 2 del diseño funcional-técnico.

**Criterios de aceptación:**
- Renderiza `DocumentationSidebar` con `currentSection` e `isOpen`.
- Renderiza `<router-view />` para contenido de sección.
- Botón hamburger visible para abrir/cerrar sidebar en móvil.
- Sin `ParentSidebar`, sin `ThemeToggle`, sin `InactivityOverlay`.
- TypeScript compila sin errores.

---

### Tarea 31.7: Actualizar `router/index.ts`

**Descripción:** Reestructurar rutas para documentación.

**Archivo:** `framework/frontend/app/src/router/index.ts` (modificación)

**Cambios:**
1. Eliminar ruta `/panel/documentacion` (líneas 63-66).
2. Reemplazar ruta `/docs` (líneas 86-89) por:

```typescript
{
  path: '/docs',
  component: () => import('../layouts/DocumentationLayout.vue'),
  children: [
    {
      path: '',
      redirect: '/docs/quien-soy'
    },
    {
      path: ':section',
      name: 'DocumentationSection',
      component: () => import('../views/documentation/DocSectionView.vue')
    }
  ]
}
```

**Criterios de aceptación:**
- `/docs` redirige a `/docs/quien-soy`.
- `/docs/:section` renderiza `DocSectionView` dentro de `DocumentationLayout`.
- `/panel/documentacion` ya no existe.
- El guard de autenticación parental no afecta a `/docs`.
- TypeScript compila sin errores.

---

### Tarea 31.8: Actualizar `ParentSidebar.vue`

**Descripción:** Cambiar destino de "Documentación" hacia `/docs` con `query.from`.

**Archivo:** `framework/frontend/app/src/components/parental/ParentSidebar.vue` (modificación)

**Cambio:**
```typescript
// Antes:
// router.replace('/panel/documentacion')

// Después:
router.replace({ path: '/docs', query: { from: route.path } })
```

**Criterios de aceptación:**
- Al pulsar "Documentación" en el sidebar parental, navega a `/docs?from=/panel/...`.
- No navega a `/panel/documentacion`.
- TypeScript compila sin errores.

---

### Tarea 31.9: Actualizar navegación desde Home

**Descripción:** Cambiar destino del botón de documentación en Home hacia `/docs`.

**Archivos:** Identificar el componente que contiene el botón de documentación en Home (probablemente `HomeHeader.vue` o `HomeView.vue`).

**Cambio:**
```typescript
// Antes:
// router.replace({ name: 'Documentation' })

// Después:
router.replace('/docs')
```

**Criterios de aceptación:**
- Desde Home, el botón de documentación navega a `/docs`.
- No pasa `query.from`.
- TypeScript compila sin errores.

---

### Tarea 31.10: Actualizar i18n

**Descripción:** Añadir claves de i18n para la navegación de documentación.

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

**Claves nuevas:** Ver sección 7 del diseño funcional-técnico.

**Criterios de aceptación:**
- Claves `views.docs.sidebar.*` disponibles.
- Clave `views.docs.menuToggle` disponible.
- Clave `views.docs.notFound` disponible.
- TypeScript compila sin errores.

---

### Tarea 31.11: Eliminar `DocumentationView.vue` obsoleto

**Descripción:** Eliminar el placeholder antiguo que ya no se usa.

**Archivo:** `framework/frontend/app/src/views/DocumentationView.vue` (eliminar)

**Criterios de aceptación:**
- El archivo ha sido eliminado.
- No hay imports rotos ni referencias al componente eliminado.
- TypeScript compila sin errores.

---

### Tarea 31.12: Verificación cross-browser y responsive

**Descripción:** Verificar que el layout funciona correctamente en distintos dispositivos y navegadores.

**Requisitos:**
1. Móvil portrait (320px-428px): sidebar drawer funciona correctamente.
2. Móvil landscape (640px-926px): sidebar drawer funciona correctamente.
3. Tableta portrait (768px-834px): sidebar visible permanentemente.
4. Tableta landscape (1024px-1194px): sidebar visible permanentemente.
5. Navegación entre secciones cambia el contenido sin recargar.
6. Los archivos markdown se cargan correctamente.
7. El HTML sanitizado no introduce vulnerabilidades XSS.

**Criterios de aceptación:**
- Layout correcto en 320px, 375px, 768px, 1024px y 1194px de ancho.
- Drawer se abre/cierra correctamente en móvil.
- Sidebar fijo en tableta.
- Navegación entre secciones funciona sin recarga.
- `vue-tsc --noEmit` sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/content/docs/quien-soy.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/primeros-pasos.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/agentes-ai.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/minijuegos.md` | Nuevo archivo |
| `framework/frontend/app/src/utils/parseMarkdown.ts` | Nuevo archivo |
| `framework/frontend/app/src/components/documentation/DocumentationSidebar.vue` | Nuevo archivo |
| `framework/frontend/app/src/views/documentation/DocSectionView.vue` | Nuevo archivo |
| `framework/frontend/app/src/layouts/DocumentationLayout.vue` | Nuevo archivo |
| `framework/frontend/app/src/router/index.ts` | Modificación |
| `framework/frontend/app/src/components/parental/ParentSidebar.vue` | Modificación |
| `framework/frontend/app/src/views/HomeView.vue` o `HomeHeader.vue` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |
| `framework/frontend/app/src/views/DocumentationView.vue` | Eliminar |
| `package.json` | Añadir `marked` y `dompurify` |

## Estimación

- **Duración:** 3 días
- **Complejidad:** Media
- **Riesgo:** Medio (reestructuración de rutas, parseo markdown, responsive)

## Criterios de aceptación del sprint

1. Desde Home se abre `/docs/quien-soy` sin solicitar PIN. *(FEAT-007 CA #1)*
2. Desde URL directa `/docs/primeros-pasos` se abre la sección sin PIN. *(CA #2)*
3. Desde panel parental se abre documentación sin mostrar `ParentSidebar` ni opciones parentales. *(CA #3)*
4. El sidebar de documentación muestra las 5 etiquetas en el orden acordado: Quién soy, Primeros pasos, Agentes AI, Minijuegos, Contacto. *(CA #4)*
5. Cada etiqueta se identifica con icono + texto (no solo icono o color). *(CA #5)*
6. En móvil (≤1023px) el sidebar se abre/cierra con botón hamburger. *(Responsive)*
7. La navegación entre secciones cambia el contenido sin recargar la página. *(UX)*
8. El contenido se carga desde archivos markdown. *(ADR-017)*
9. La ruta `/panel/documentacion` ha sido eliminada. *(Reestructuración)*
10. El HTML renderizado está sanitizado contra XSS. *(Seguridad)*
11. `vue-tsc --noEmit` compila sin errores. *(Calidad)*

## Evidencias esperadas

- Test manual: desde Home, pulsar "Documentación" → abre `/docs/quien-soy`.
- Test manual: navegar a `/docs/primeros-pasos` directamente → abre la sección.
- Test manual: desde panel parental, pulsar "Documentación" → abre `/docs?from=/panel/...` sin ParentSidebar.
- Test manual: verificar que el sidebar muestra 5 secciones en orden.
- Test manual: verificar que cada sección tiene icono + texto.
- Test manual: en móvil (375px), verificar drawer abre/cierra.
- Test manual: en tableta (1024px), verificar sidebar fijo.
- Test manual: navegar entre secciones → contenido cambia sin recarga.
- Test manual: verificar que `/panel/documentacion` devuelve 404.
- `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [x] FEAT-007 aceptada.
- [x] ADR-017 (documentación estática) vigente.
- [ ] SPRINT-001-contenido: archivos markdown placeholder disponibles.

## Handoffs a otras capas

### Backend:
- Sin cambios requeridos en este sprint.

### Contenido:
- SPRINT-001-contenido debe proporcionar los archivos markdown definitivos para las 4 secciones informativas.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Relación con otros sprints

- **SPRINT-032** implementará la lógica de "Volver" sobre este layout.
- **SPRINT-033** implementará la sección Contacto dentro de este layout.

### Privacidad infantil

- La documentación es pública y no muestra ni solicita datos familiares o infantiles.
- No hay formularios ni recogida de datos en este sprint.
- El contenido es estático y revisado por el equipo de Contenido.
