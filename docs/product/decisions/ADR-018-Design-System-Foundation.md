# ADR-018 — Design System Foundation

## Status

status:        accepted
date:          2026-07-22
superseded_by: —

---

## 1. Context

FEAT-001 define los componentes globales del panel parental de My Friend Nubi, incluyendo modo oscuro y catálogo de componentes de desarrollo. Para implementarlos con consistencia, se necesita una base de sistema de diseño que establezca tokens CSS, tipografía, iconografía, paleta de colores, estrategia responsive y animaciones.

Fuerzas y restricciones:
- El frontend es 100% custom (ADR-010: sin librerías UI externas como Vuetify o Element Plus).
- Vue 3 + TypeScript + Vite + TailwindCSS ya están configurados.
- El panel parental es usado por adultos en móviles y tabletas Android (Samsung Galaxy A15 como referencia).
- La experiencia infantil no se ve afectada por el sistema de diseño del panel parental.
- Todas las decisiones deben ser fácilmente modificables cuando entre un diseñador experto.
- Se requiere modo oscuro con alternancia y persistencia.
- Se requiere un catálogo de componentes accesible solo en desarrollo.

---

## 2. Decision Summary

| # | Decisión | Opción elegida | Resumen |
|---|----------|----------------|---------|
| 1 | Paleta de colores | C: Híbrida | TailwindCSS para neutros/estados + colores personalizados modificables |
| 2 | Tipografía | B: Personalizada | Nunito (Google Fonts) con preload + font-display: swap |
| 3 | Librería de iconos | A: Lucide | Wrapper `NubiIcon` + iconos custom (lectura, relajación) |
| 4 | Herramienta de catálogo | B: Histoire | URL `/dev/components` protegida con `VITE_ENV=development` |
| 5 | Breakpoints responsive | A: TailwindCSS | Mobile-first, grid 1-2-3 columnas |
| 6 | Estrategia de animaciones | B: Funcionales + decorativas sutiles | 200-300ms, respetar `prefers-reduced-motion` |

**Condición transversal:** Todas las decisiones son fácilmente modificables cuando entre un diseñador experto.

---

## 3. Decision Detail

### 3.1 Paleta de colores — Híbrida (TailwindCSS + personalizado)

**Decisión:** Combinar tokens de TailwindCSS para colores neutros (grises, blancos) y estados (success, warning, error, info) con colores de marca personalizados definidos como CSS custom properties.

**Implementación:**
- Capa 1: TailwindCSS provee la escala de neutros (`gray-*`, `slate-*`) y colores de estado estándar.
- Capa 2: Colores de marca Nubi definidos como CSS custom properties (`--nubi-color-primary`, `--nubi-color-secondary`, etc.) en `src/styles/tokens/colors.css`.
- Capa 3: Tokens semánticos (`--nubi-bg-surface`, `--nubi-text-primary`, etc.) que mapean a capas 1 y 2 según el tema activo (claro/oscuro).
- Extensión de TailwindCSS en `tailwind.config.ts` para referenciar los custom properties.

**Modificabilidad:** Un diseñador puede cambiar los colores de marca modificando solo las variables de la capa 2 sin tocar TailwindCSS ni los tokens semánticos. La paleta completa se documenta en `docs/design/color-system.md`.

### 3.2 Tipografía — Nunito personalizada

**Decisión:** Usar Nunito (Google Fonts) como tipografía base del panel parental, cargada mediante preload y `font-display: swap` para evitar FOIT/FOUT.

**Implementación:**
- Archivos de fuente (woff2) almacenados en `public/fonts/nunito/` para autohospedaje.
- Preload en `index.html`: `<link rel="preload" href="/fonts/nunito/nunito-variable.woff2" as="font" type="font/woff2" crossorigin>`.
- CSS: `--nubi-font-family-base: 'Nunito', sans-serif;` definida en `src/styles/tokens/typography.css`.
- Escala tipográfica con variables: `--nubi-font-size-xs` a `--nubi-font-size-2xl`.

**Modificabilidad:** Un diseñador puede cambiar la fuente base modificando `--nubi-font-family-base` y reemplazando los archivos en `public/fonts/`. La guía completa se documenta en `docs/design/typography-system.md`.

### 3.3 Librería de iconos — Lucide con wrapper NubiIcon

**Decisión:** Usar Lucide como librería base de iconos (1,400+ iconos SVG) con un componente wrapper `NubiIcon` que unifica la API y permite iconos personalizados.

**Implementación:**
- Paquete: `lucide-vue-next`.
- Componente wrapper: `src/components/base/NubiIcon.vue` que acepta nombre de icono, tamaño y color.
- Iconos custom almacenados como SVG en `src/assets/icons/custom/` (lectura, relajación, etc.).
- `NubiIcon` busca primero en custom, luego en Lucide como fallback.

**Modificabilidad:** Un diseñador puede añadir iconos custom sin tocar Lucide, o reemplazar la librería base modificando solo `NubiIcon`. La guía completa se documenta en `docs/design/icon-system.md`.

### 3.4 Herramienta de catálogo — Histoire

**Decisión:** Usar Histoire como herramienta de catálogo de componentes, accesible exclusivamente en desarrollo mediante la URL `/dev/components`.

**Implementación:**
- Paquete: `@histoire/plugin-vue`.
- Configuración en `histoire.config.ts`.
- Stories con formato `.story.vue` junto a cada componente.
- Protección: la ruta `/dev/components` se registra condicionalmente solo cuando `import.meta.env.VITE_ENV === 'development'`.
- En producción, la ruta no existe en el bundle (tree-shaking + conditional import).

**Modificabilidad:** Si el equipo prefiere migrar a Storybook u otra herramienta, la configuración está aislada en `histoire.config.ts` y los stories pueden convertirse con scripts de migración. La guía completa se documenta en `docs/design/component-catalog.md`.

### 3.5 Breakpoints responsive — TailwindCSS mobile-first

**Decisión:** Utilizar los breakpoints por defecto de TailwindCSS con estrategia mobile-first.

**Implementación:**
- Breakpoints estándar: `sm:640px`, `md:768px`, `lg:1024px`, `xl:1280px`, `2xl:1536px`.
- Grid system: 1 columna (móvil) → 2 columnas (tablet) → 3 columnas (desktop).
- Clases utilitarias de TailwindCSS: `grid-cols-1`, `md:grid-cols-2`, `lg:grid-cols-3`.
- No se añaden breakpoints personalizados inicialmente.

**Modificabilidad:** Un diseñador puede añadir breakpoints custom en `tailwind.config.ts` bajo `theme.screens`. La guía completa se documenta en `docs/design/responsive-system.md`.

### 3.6 Estrategia de animaciones — Funcionales + decorativas sutiles

**Decisión:** Combinar animaciones funcionales (transiciones de estado, loading) con decorativas sutiles (hover, entrada de elementos), respetando `prefers-reduced-motion`.

**Implementación:**
- Duraciones como variables CSS: `--nubi-duration-fast: 200ms`, `--nubi-duration-normal: 300ms`.
- Easing estándar: `--nubi-ease-default: ease-in-out`.
- Media query `@media (prefers-reduced-motion: reduce)` desactiva animaciones decorativas.
- Clases utilitarias en `src/styles/utilities/animations.css`.

**Modificabilidad:** Un diseñador puede ajustar duraciones y easings modificando variables CSS sin tocar la lógica de componentes. La guía completa se documenta en `docs/design/animation-system.md`.

---

## 4. Consequences and Implications

**Positivas:**
- Base sólida y consistente para todos los componentes del panel parental.
- TailwindCSS ya está integrado, reduciendo la curva de aprendizaje.
- Lucide ofrece una amplia variedad de iconos sin dependencias pesadas.
- Histoire es ligero y nativo de Vue 3, evitando la complejidad de Storybook.
- Las variables CSS facilitan la modificación por un diseñador futuro sin reescribir código.
- El respeto a `prefers-reduced-motion` mejora la accesibilidad.

**Negativas:**
- Nunito como fuente autohospedada añade ~80-120KB al bundle inicial (mitigado con preload y woff2).
- Histoire requiere mantenimiento de stories `.story.vue` para cada componente.
- La estrategia híbrida de colores requiere disciplina para no mezclar capas arbitrariamente.

**Neutrales:**
- Los breakpoints de TailwindCSS son estándar; si se necesitan específicos para Galaxy A15, se pueden añadir sin romper lo existente.
- Lucide puede reemplazarse sin afectar a componentes gracias al wrapper `NubiIcon`.
- La condición transversal de modificabilidad implica documentar cada sistema en `docs/design/`.

---

## 5. Alternatives Considered and Discarded

| Decisión | Alternativa | Razón de descarte |
|----------|-------------|-------------------|
| Paleta de colores | A: Solo TailwindCSS | Limita la personalización de marca; difícil de modificar sin tocar config |
| Paleta de colores | B: Solo CSS custom properties | Reinventar la rueda para neutros y estados que TailwindCSS ya resuelve |
| Tipografía | A: Fuente del sistema (system-ui) | No aporta identidad visual; no transmite la calidez de Nubi |
| Tipografía | C: Fuente personalizada no variable | Mayor peso en KB sin beneficio adicional; variable font es más flexible |
| Iconos | B: Material Icons | Peso elevado, estética no alineada con Nubi, requiere Google Fonts |
| Iconos | C: SVG inline manual | No escalable para 1,400+ iconos; difícil mantenimiento |
| Catálogo | A: Storybook | Complejidad excesiva para Vue 3 puro; configuración pesada |
| Catálogo | C: Custom con VitePress | Requiere desarrollo propio significativo; menos DX que Histoire |
| Breakpoints | B: Breakpoints custom desde inicio | No hay datos reales de uso; los defaults de TailwindCSS son suficientes |
| Breakpoints | C: Container queries exclusivamente | Soporte limitado en versiones antiguas; complementar con breakpoints |
| Animaciones | A: Solo funcionales | El panel parental se siente frío sin micro-interacciones sutiles |
| Animaciones | C: Librería externa (GSAP, Motion) | Dependencia innecesaria; CSS animations/transitions son suficientes |

---

## 6. References

- [TailwindCSS Documentation](https://tailwindcss.com/docs)
- [Nunito — Google Fonts](https://fonts.google.com/specimen/Nunito)
- [Lucide Icons](https://lucide.dev/)
- [Histoire Documentation](https://histoire.dev/)
- [MDN: prefers-reduced-motion](https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-reduced-motion)
- [CSS Custom Properties (MDN)](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties)
- ADR-010 Frontend Layer Architecture
- ADR-017 Componentes globales del panel parental
- FEAT-001 Componentes globales del panel parental

---

## 7. Risks and Mitigations

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Nunito no carga a tiempo y causa FOUT | Baja | Bajo | Preload + font-display: swap + fallback sans-serif |
| Histoire incompatible con versión de Vue 3 | Baja | Medio | Verificar compatibilidad antes de integrar; alternativa: Storybook |
| Variables CSS no soportadas en WebView antiguo | Muy baja | Bajo | Samsung Galaxy A15 usa Chrome actualizado; soporte completo |
| Diseñador futuro quiere cambiar todo el sistema | Media | Bajo | Condición transversal: todo es modificable; documentación en docs/design/ |
| Bundle size de Lucide excesivo | Baja | Bajo | Tree-shaking por icono; solo se importan los usados |
| Confusión entre capas de color (TailwindCSS vs custom) | Media | Medio | Documentación clara en docs/design/color-system.md + convenciones de nomenclatura |
| `prefers-reduced-motion` no cubre todos los casos | Baja | Bajo | Revisión en cada componente nuevo; lint CSS puede ayudar |
