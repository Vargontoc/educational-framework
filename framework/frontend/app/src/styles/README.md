# Sistema de Diseño - My Friend Nubi

## Overview

Sistema de diseño base para el panel parental de My Friend Nubi, implementado según ADR-018 Design System Foundation.

## Estructura

```
src/styles/
├── tokens/              # Tokens de diseño base
│   ├── colors.css       # Colores de marca Nubi
│   ├── typography.css   # Tipografía (Nunito)
│   ├── spacing.css      # Espaciado y breakpoints
│   ├── borders.css      # Bordes redondeados y sombras
│   └── animations.css   # Duraciones y easing
├── themes/              # Temas claro/oscuro
│   ├── light.css        # Tokens semánticos (tema claro)
│   └── dark.css         # Tokens semánticos (tema oscuro)
└── main.css             # CSS principal (importa todo)
```

## Tokens de Diseño

### Colores

**Capa 1: TailwindCSS** - Neutros y estados estándar
**Capa 2: Colores de marca** - `--nubi-color-primary`, `--nubi-color-secondary`, `--nubi-color-accent`
**Capa 3: Tokens semánticos** - `--nubi-bg-surface`, `--nubi-text-primary`, etc.

### Tipografía

- **Fuente base**: Nunito (Google Fonts)
- **Escala**: xs (12px) → 2xl (24px)
- **Pesos**: light (300) → bold (700)

### Espaciado

- **Escala**: xs (4px) → 3xl (64px)
- **Estrategia**: Mobile-first con breakpoints de TailwindCSS

### Bordes y Sombras

- **Bordes redondeados**: sm (4px) → full (9999px)
- **Sombras**: sm, md, lg, xl

### Animaciones

- **Duraciones**: fast (200ms), normal (250ms), slow (300ms)
- **Easing**: ease-in, ease-out, ease-in-out
- **Accesibilidad**: Respeta `prefers-reduced-motion`

## Temas

### Modo Claro (por defecto)

```css
:root, [data-theme="light"] {
  --nubi-bg-surface: #FFFFFF;
  --nubi-text-primary: #111827;
  /* ... */
}
```

### Modo Oscuro

```css
[data-theme="dark"] {
  --nubi-bg-surface: #111827;
  --nubi-text-primary: #F9FAFB;
  /* ... */
}
```

## Uso

### Composable useTheme

```typescript
import { useTheme } from './composables/useTheme'

const { 
  theme, 
  isDark, 
  isLight, 
  setDarkMode, 
  setLightMode, 
  toggleTheme,
  forceLightMode 
} = useTheme()
```

### TailwindCSS con tokens

```vue
<div class="bg-surface text-primary border border-default rounded-lg shadow-md">
  <!-- Usa tokens semánticos automáticamente -->
</div>
```

### Variables CSS directas

```css
.custom-element {
  background-color: var(--nubi-bg-surface);
  color: var(--nubi-text-primary);
  padding: var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-lg);
}
```

## Catálogo de Componentes

### Desarrollo

```bash
npm run histoire:dev
```

Accesible en: `http://localhost:6006/dev/components`

### Producción

La ruta `/dev/components` no existe en el bundle de producción (tree-shaking).

## Iconos

### Componente NubiIcon

```vue
<NubiIcon name="home" :size="24" color="currentColor" />
```

- **Librería base**: @lucide/vue
- **Iconos custom**: `src/assets/icons/custom/`
- **Búsqueda**: Primero custom, luego Lucide como fallback

## Breakpoints Responsive

```css
--nubi-breakpoint-sm: 640px;   /* Móvil landscape */
--nubi-breakpoint-md: 768px;   /* Tablet portrait */
--nubi-breakpoint-lg: 1024px;  /* Tablet landscape */
--nubi-breakpoint-xl: 1280px;  /* Desktop */
```

**Estrategia**: Mobile-first con grid 1-2-3 columnas

## Modificabilidad

Todas las decisiones son fácilmente modificables cuando entre un diseñador experto:

- **Colores**: Modificar `src/styles/tokens/colors.css`
- **Tipografía**: Modificar `src/styles/tokens/typography.css`
- **Temas**: Modificar `src/styles/themes/light.css` y `dark.css`
- **Espaciado**: Modificar `src/styles/tokens/spacing.css`

## Referencias

- [ADR-018 Design System Foundation](../../../docs/product/decisions/ADR-018-Design-System-Foundation.md)
- [Color System Guide](../../../docs/design/color-system.md)
- [Typography System Guide](../../../docs/design/typography-system.md)
- [Icon System Guide](../../../docs/design/icon-system.md)
- [Component Catalog Guide](../../../docs/design/component-catalog.md)
- [Responsive System Guide](../../../docs/design/responsive-system.md)
- [Animation System Guide](../../../docs/design/animation-system.md)
