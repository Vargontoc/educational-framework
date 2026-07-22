# Sprint 004 - Frontend

## Goal
Implementar componentes de acción (botón primario, secundario, icono, destructivo) y componentes de estado (loading/spinner, skeleton, empty state, error state) como base funcional mínima para el panel parental.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-003
waiting_for: Paleta de colores confirmada (especialmente color destructivo)

## Tasks
- [ ] Implementar `NubiButton` variante primario: color destacado, objetivo táctil mínimo 48x48dp, estados normal/hover/pressed/disabled
- [ ] Implementar `NubiButton` variante secundario: menos prominente visualmente, mismos estados que primario
- [ ] Implementar `NubiIconButton`: acción rápida con icono, tooltip al hover, objetivo táctil 48x48dp
- [ ] Implementar `NubiButton` variante destructivo: color distintivo rojo suave, confirmación visual de irreversibilidad
- [ ] Implementar `NubiSpinner`: indicador de carga circular, overlay parcial o total, animación suave
- [ ] Implementar `NubiSkeleton`: placeholder animado con formas configurables (línea, círculo, rectángulo, card)
- [ ] Implementar `NubiEmptyState`: vista sin datos con ilustración opcional, mensaje amigable y acción sugerida
- [ ] Implementar `NubiErrorState`: vista de error con mensaje claro, opción de reintentar y detalle opcional
- [ ] Registrar todos los componentes en el catálogo de desarrollo con variantes, estados y tamaños
- [ ] Validar todos los textos con i18n (`$t()`) — sin literales en templates
- [ ] Validar accesibilidad táctil (mínimo 48x48dp) en todos los componentes interactivos
- [ ] Validar contraste WCAG AA en ambos temas (claro y oscuro) para todos los componentes

## Acceptance Criteria
- Todos los botones tienen objetivo táctil mínimo 48x48dp
- Los botones presentan 4 estados diferenciados visualmente: normal, hover, pressed, disabled
- El botón destructivo usa un color distintivo (rojo suave) que lo diferencia claramente de los demás
- El spinner funciona como overlay parcial e independiente
- El skeleton soporta al menos 4 formas configurables (línea, círculo, rectángulo, card)
- El empty state incluye mensaje, ilustración opcional y acción sugerida
- El error state incluye mensaje claro y botón de reintentar
- Todos los componentes están registrados en el catálogo de desarrollo con sus variantes
- Todos los textos visibles están internacionalizados (i18n)
- Las animaciones usan la duración estándar del sistema de diseño (200-300ms)
- Los componentes son responsive (adaptables a móvil y tablet)

## Risks
- Sin paleta de colores confirmada, el color del botón destructivo será provisional
- El skeleton loading requiere coordinación con los componentes de contenido para definir las formas
- Los iconos del `NubiIconButton` dependen de la librería de iconos pendiente de confirmación
- El empty state y error state necesitan ilustraciones o iconografía que puede no estar disponible

## Dependencies
- SPRINT-003 completado (catálogo y sistema de diseño base)
- Tokens CSS del sistema de diseño (colores, tipografía, espaciado, bordes, sombras)
- Librería de iconos confirmada para `NubiIconButton`

## Agent Instruction
- Los componentes deben seguir la nomenclatura con prefijo `Nubi` (ej: `NubiButton`, `NubiSpinner`)
- Usar las variables CSS del sistema de diseño para todos los valores de estilo
- Implementar con Vue 3 + TypeScript, usando `<script setup>` y Composition API
- Todos los componentes deben emitir eventos estandarizados (`@click`, `@retry`, etc.)
- Los estados disabled deben incluir `aria-disabled` y estilos visuales claros
- El catálogo debe mostrar cada componente con todas sus variantes y estados documentados
- No incluir lógica de negocio en los componentes; solo presentación y eventos
- Validar en Samsung Galaxy A15 físico como criterio de aceptación obligatorio

## Notes
- Los componentes de acción son los más utilizados en el panel parental; priorizar calidad y consistencia
- El skeleton loading debe ser configurable para anticipar diferentes formas de contenido (cards, listas, etc.)
- Los componentes de estado (empty, error) serán reutilizados en múltiples vistas del panel
- Las variantes de botón pueden implementarse como un único componente `NubiButton` con prop `variant`
- Considerar slots para iconos en botones y contenido personalizado en empty/error states

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
