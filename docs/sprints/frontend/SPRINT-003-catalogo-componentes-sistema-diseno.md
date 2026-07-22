# Sprint 003 - Frontend — Catálogo, sistema de diseño y modo oscuro

## Goal
Configurar catálogo de componentes accesible solo en desarrollo, establecer el sistema de diseño base (tokens CSS, variables de tema) e implementar el sistema de modo oscuro completo con alternancia y persistencia, exclusivo para el panel parental.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-002
waiting_for: —

## Tasks
- [x] Crear ADR-018 Design System Foundation con las 6 decisiones técnicas confirmadas
- [x] Actualizar FEAT-001 con sección de decisiones técnicas confirmadas
- [x] Crear docs/design/color-system.md — guía de modificación del sistema de color
- [x] Crear docs/design/typography-system.md — guía de modificación del sistema tipográfico
- [x] Crear docs/design/icon-system.md — guía de modificación del sistema de iconos
- [x] Crear docs/design/component-catalog.md — guía de modificación del catálogo de componentes
- [x] Crear docs/design/responsive-system.md — guía de modificación del sistema responsive
- [x] Crear docs/design/animation-system.md — guía de modificación del sistema de animaciones
- [ ] Seleccionar y configurar herramienta de catálogo de componentes (Histoire)
- [ ] Definir sistema de variables CSS para tokens de diseño: colores, tipografía, espaciado, bordes, sombras
- [ ] Definir estructura de temas claro/oscuro mediante CSS custom properties
- [ ] Configurar ruta de desarrollo `/dev/components` accesible exclusivamente con `VITE_ENV=development`
- [ ] Implementar protección: la ruta del catálogo redirige a `/` en entorno de producción
- [ ] Crear layout base del catálogo con navegación por categorías
- [ ] Documentar variables de entorno necesarias (`.env.development`, `.env.production`)
- [ ] Definir breakpoints responsive para móvil y tablet Android
- [ ] Configurar estrategia de animaciones y transiciones (duracion 200-300ms, easing)
- [ ] Crear plantilla base de documentación para futuros componentes en el catálogo
- [ ] Implementar sistema de temas con variables CSS (claro/oscuro)
- [ ] Implementar alternancia de tema desde configuración del panel parental
- [ ] Implementar persistencia de preferencia de tema en localStorage
- [ ] Garantizar que la experiencia infantil mantiene siempre modo claro

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

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
