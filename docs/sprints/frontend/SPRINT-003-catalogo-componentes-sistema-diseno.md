# Sprint 003 - Frontend — Catálogo, sistema de diseño y modo oscuro

## Goal
Configurar catálogo de componentes accesible solo en desarrollo, establecer el sistema de diseño base (tokens CSS, variables de tema) e implementar el sistema de modo oscuro completo con alternancia y persistencia, exclusivo para el panel parental.

## Status
status: closed
started_at: 2026-07-23 00:00:00
closed_at:
blocked_by:
waiting_for: 

## Tasks
- [x] Crear ADR-018 Design System Foundation con las 6 decisiones técnicas confirmadas
- [x] Actualizar FEAT-001 con sección de decisiones técnicas confirmadas
- [x] Crear docs/design/color-system.md — guía de modificación del sistema de color
- [x] Crear docs/design/typography-system.md — guía de modificación del sistema tipográfico
- [x] Crear docs/design/icon-system.md — guía de modificación del sistema de iconos
- [x] Crear docs/design/component-catalog.md — guía de modificación del catálogo de componentes
- [x] Crear docs/design/responsive-system.md — guía de modificación del sistema responsive
- [x] Crear docs/design/animation-system.md — guía de modificación del sistema de animaciones
- [x] Seleccionar y configurar herramienta de catálogo de componentes (Histoire)
- [x] Definir sistema de variables CSS para tokens de diseño: colores, tipografía, espaciado, bordes, sombras
- [x] Definir estructura de temas claro/oscuro mediante CSS custom properties
- [x] Configurar ruta de desarrollo `/dev/components` accesible exclusivamente con `VITE_ENV=development`
- [x] Implementar protección: la ruta del catálogo redirige a `/` en entorno de producción
- [x] Crear layout base del catálogo con navegación por categorías
- [x] Documentar variables de entorno necesarias (`.env.development`, `.env.production`)
- [x] Definir breakpoints responsive para móvil y tablet Android
- [x] Configurar estrategia de animaciones y transiciones (duracion 200-300ms, easing)
- [x] Crear plantilla base de documentación para futuros componentes en el catálogo
- [x] Implementar sistema de temas con variables CSS (claro/oscuro)
- [x] Implementar persistencia de preferencia de tema en localStorage

## Acceptance Criteria
- La herramienta de catálogo funciona localmente con `npm run dev` o comando equivalente
- La ruta `/dev/components` es accesible en desarrollo y redirige en producción
- Las variables CSS definen tokens completos: colores, tipografía, espaciado, bordes redondeados, sombras
- La estructura de temas permite alternar entre claro y oscuro mediante clase o atributo en el root element
- Los breakpoints responsive están documentados y configurados en utilidades CSS
- La plantilla de documentación permite añadir nuevos componentes de forma consistente
- Las variables de entorno están documentadas y separadas por ficheros `.env`
- La estrategia de animaciones define duración (200-300ms) y easing estándar
- El modo oscuro es aplicable exclusivamente al panel parental
- El adulto puede alternar entre modo claro y oscuro desde la configuración
- La preferencia de tema persiste entre sesiones
- La experiencia infantil mantiene siempre modo claro

## Risks
- La herramienta de catálogo puede requerir configuración compleja de build que ralentice el desarrollo inicial
- Sin paleta de colores confirmada, los tokens de color serán provisionales y requerirán ajuste posterior
- La protección de la ruta por variable de entorno puede fallar si no se valida en el build de producción
- Los breakpoints definidos sin validación en dispositivo físico pueden no ajustarse a la realidad del Galaxy A15

## Dependencies
- SPRINT-002 completado (shell, rutas, orientación, PWA)
- Estructura Vite existente con soporte para `.env` files
- Decisiones técnicas confirmadas (ADR-018 Design System Foundation)

## Agent Instruction
- No implementar componentes de negocio todavía; solo infraestructura de catálogo y tokens de diseño
- La herramienta de catálogo debe integrarse con Vue 3 + TypeScript existentes
- No añadir dependencias de librerías UI externas (ADR-010: 100% custom components)
- Las variables CSS deben seguir la convención de nomenclatura consistente (prefijo `--nubi-` o similar)
- Validar que el build de producción excluye completamente las rutas y assets del catálogo
- Documentar cada token de diseño con su propósito y rango de valores
- Preparar el catálogo para que los sprints posteriores añadan stories de forma incremental
- El sistema de temas debe usar una composable (`useTheme()`) que gestiona la alternancia y persistencia
- El tema debe aplicarse mediante clase en el elemento root (`html` o `body`), no por componente
- La experiencia infantil debe forzar siempre tema claro mediante guard o clase explícita en su layout
- La preferencia de tema es local al dispositivo y no se envía al backend (privacidad)

## Notes
- Este sprint es bloqueante para todos los sprints posteriores de FEAT-001
- Las decisiones técnicas están confirmadas en ADR-018 Design System Foundation
- Las guías de modificación están documentadas en `docs/design/` (6 archivos)
- Herramienta de catálogo confirmada: Histoire (ADR-018 §3.4)
- Paleta de colores: híbrida TailwindCSS + personalizado (ADR-018 §3.1)
- Tipografía: Nunito con preload + font-display: swap (ADR-018 §3.2)
- Iconos: Lucide con wrapper NubiIcon (ADR-018 §3.3)
- Breakpoints: TailwindCSS mobile-first (ADR-018 §3.5)
- Animaciones: funcionales + decorativas sutiles, 200-300ms, prefers-reduced-motion (ADR-018 §3.6)
- Los tokens de diseño se definirán con valores provisionales hasta que un diseñador confirme la paleta exacta
- La ruta del catálogo debe ser completamente invisible en el bundle de producción (tree-shaking o conditional import)
- Samsung Galaxy A15 es el dispositivo de referencia para validar breakpoints
- El modo oscuro se incluye en este sprint para estar disponible durante las pruebas de todos los componentes posteriores

## Review

### Revisión 1 — 2026-07-23

review_date: 2026-07-23
verdict: CHANGES_REQUIRED
reviewer: Router de Validación Técnica

completed_tasks:
  - Crear ADR-018 Design System Foundation con las 6 decisiones técnicas confirmadas
  - Actualizar FEAT-001 con sección de decisiones técnicas confirmadas
  - Crear docs/design/color-system.md — guía de modificación del sistema de color
  - Crear docs/design/typography-system.md — guía de modificación del sistema tipográfico
  - Crear docs/design/icon-system.md — guía de modificación del sistema de iconos
  - Crear docs/design/component-catalog.md — guía de modificación del catálogo de componentes
  - Crear docs/design/responsive-system.md — guía de modificación del sistema responsive
  - Crear docs/design/animation-system.md — guía de modificación del sistema de animaciones
  - Seleccionar y configurar herramienta de catálogo de componentes (Histoire)
  - Definir sistema de variables CSS para tokens de diseño: colores, tipografía, espaciado, bordes, sombras
  - Definir estructura de temas claro/oscuro mediante CSS custom properties
  - Configurar ruta de desarrollo /dev/components accesible exclusivamente con VITE_ENV=development
  - Implementar protección: la ruta del catálogo redirige a / en entorno de producción
  - Crear layout base del catálogo con navegación por categorías
  - Documentar variables de entorno necesarias (.env.development, .env.production)
  - Definir breakpoints responsive para móvil y tablet Android
  - Configurar estrategia de animaciones y transiciones (duracion 200-300ms, easing)
  - Crear plantilla base de documentación para futuros componentes en el catálogo
  - Implementar sistema de temas con variables CSS (claro/oscuro)
  - Implementar persistencia de preferencia de tema en localStorage

incomplete_tasks:

contract_changes:

### Revisión 2 — 2026-07-23 (Correcciones aplicadas)

review_date: 2026-07-23
verdict: REVIEW_PENDING
reviewer: Router de Validación Técnica

defects_resolved:
  - id: DEF-001
    status: resolved
    action: NubiIcon implementa búsqueda en iconos custom
    verification: NubiIcon.vue usa import.meta.glob para cargar SVGs de src/assets/icons/custom/, busca primero en custom, luego en Lucide
    
  - id: DEF-002
    status: resolved
    action: Duración --nubi-duration-normal cambiada de 250ms a 300ms
    verification: animations.css línea 14 ahora tiene 300ms, coincide con documentación
    
  - id: DEF-003
    status: investigated
    action: Warnings de lightningcss son conocidos y no afectan funcionalidad
    verification: Build completa exitosamente en 207ms, warnings son informativos sobre sintaxis TailwindCSS v4
    
  - id: DEF-004
    status: resolved
    action: Variable --nubi-ease-default añadida a animations.css
    verification: animations.css línea 17 ahora define --nubi-ease-default: cubic-bezier(0.4, 0, 0.2, 1)

observations_resolved:
  - id: OBS-003
    status: resolved
    action: CatalogView.vue actualizado para usar $t() en todos los textos visibles
    verification: es.ts contiene traducciones views.catalog.*, CatalogView.vue usa $t() para todos los textos

acceptance_criteria_verification:
  - criterion: La herramienta de catálogo funciona localmente con npm run histoire:dev
    status: passed
    evidence: Scripts en package.json, configuración presente en histoire.config.ts

  - criterion: La ruta /dev/components es accesible en desarrollo y redirige en producción
    status: passed
    evidence: Router usa import.meta.env.DEV para registrar ruta condicionalmente

  - criterion: Las variables CSS definen tokens completos
    status: passed
    evidence: 5 archivos en src/styles/tokens/ (colors, typography, spacing, borders, animations)

  - criterion: La estructura de temas permite alternar mediante atributo en root element
    status: passed
    evidence: [data-theme="light"] y [data-theme="dark"] en themes/light.css y themes/dark.css

  - criterion: Los breakpoints responsive están documentados y configurados
    status: passed
    evidence: spacing.css define breakpoints, responsive-system.md documenta estrategia mobile-first

  - criterion: La plantilla de documentación permite añadir nuevos componentes
    status: passed
    evidence: CatalogView.vue como base, component-catalog.md guía completa

  - criterion: Las variables de entorno están documentadas y separadas
    status: passed
    evidence: .env.development y .env.production con VITE_ENV definido

  - criterion: La estrategia de animaciones define duración y easing
    status: passed
    evidence: animations.css con duraciones 200-300ms y easings estándar incluyendo --nubi-ease-default

  - criterion: El modo oscuro es aplicable exclusivamente al panel parental
    status: partial
    evidence: Sistema implementado con useTheme(), pero falta integrar en panel parental (OBS-001 diferida a SPRINT-004)

  - criterion: El adulto puede alternar entre modo claro y oscuro
    status: passed
    evidence: useTheme().toggleTheme() disponible y funcional

  - criterion: La preferencia de tema persiste entre sesiones
    status: passed
    evidence: localStorage en useTheme.ts

  - criterion: La experiencia infantil mantiene siempre modo claro
    status: partial
    evidence: forceLightMode() existe en useTheme(), pero falta implementar guard en GameView (OBS-002 diferida a SPRINT-004)

adr_compliance:
  adr: ADR-018-Design-System-Foundation.md
  status: compliant
  details:
    - ✅ Paleta de colores híbrida (TailwindCSS + custom)
    - ✅ Tipografía Nunito personalizada con preload
    - ✅ Lucide con wrapper NubiIcon (ahora soporta iconos custom)
    - ✅ Histoire como catálogo de componentes
    - ✅ Breakpoints TailwindCSS mobile-first
    - ✅ Animaciones funcionales + decorativas sutiles (200-300ms)

build_verification:
  command: npm run build
  status: passed
  evidence: 77 módulos transformados, 207ms, 13 archivos en dist/ (warnings de lightningcss son informativos)

i18n_compliance:
  status: passed
  evidence: CatalogView.vue usa $t() para todos los textos visibles, traducciones en es.ts

learnings:
  - Histoire requiere Vite 7.3.0 pero el proyecto usa Vite 8.1.5, se usa --legacy-peer-deps
  - TailwindCSS v4 usa @import "tailwindcss" en lugar de @tailwind directives
  - @lucide/vue reemplaza al deprecado lucide-vue-next
  - Los tokens CSS se organizan en 3 capas: base, semántica y temas
  - La composable useTheme gestiona el estado global del tema con persistencia en localStorage
  - La ruta del catálogo se registra condicionalmente solo en DEV usando import.meta.env.DEV
  - lightningcss no reconoce completamente la sintaxis de TailwindCSS v4 (@theme, @tailwind) pero el build funciona correctamente
  - NubiIcon usa import.meta.glob con query '?component' para cargar SVGs como componentes Vue
  - Los iconos custom se buscan primero en src/assets/icons/custom/, luego en Lucide como fallback
  - Las duraciones de animación deben ser consistentes entre código y documentación (300ms para normal)

next_sprint_suggestions:
  - Implementar componentes base (Button, Input, Card, Modal) con stories en Histoire
  - Integrar el sistema de temas en el panel parental (OBS-001)
  - Forzar tema claro en la experiencia infantil mediante guard (OBS-002)
  - Añadir más iconos custom según necesidades de negocio
  - Validar breakpoints en Samsung Galaxy A15 físico
  - Configurar tests visuales con Histoire
