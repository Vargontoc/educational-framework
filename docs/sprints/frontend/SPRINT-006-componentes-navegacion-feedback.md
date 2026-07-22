# Sprint 006 - Frontend

## Goal
Implementar componentes de navegación (sidebar colapsable, tabs, breadcrumb, botón atrás) y componentes de feedback (modales, toast, alertas, tooltips) para estructurar la interacción y comunicación del panel parental.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-005
waiting_for: Definición de secciones del panel parental para configurar el sidebar

## Tasks
- [ ] Implementar `NubiSidebar`: menú lateral izquierdo, iconos + texto expandido, solo iconos colapsado, animación suave (200-300ms)
- [ ] Implementar responsive del sidebar: overlay en móvil, lateral fijo en tablet
- [ ] Implementar `NubiTabs`: navegación entre subsecciones, indicador visual de tab activa, accesible por teclado
- [ ] Implementar `NubiBreadcrumb`: muestra ruta de navegación, enlaces a niveles anteriores clicables
- [ ] Implementar `NubiBackButton`: flecha + texto opcional, posición consistente (izquierda superior)
- [ ] Implementar `NubiConfirmModal`: diálogo centrado, overlay oscuro, título/mensaje/botones confirmar y cancelar
- [ ] Implementar `NubiInfoModal`: muestra detalles o ayuda, solo botón de cierre, scroll interno si contenido extenso
- [ ] Implementar `NubiToast`: mensaje temporal no intrusivo, desaparece en 3-5 segundos, posiciones configurable
- [ ] Implementar sistema de cola de toasts para múltiples notificaciones simultáneas
- [ ] Implementar `NubiAlert`: mensaje persistente hasta cierre manual, tipos info/warning/error/success
- [ ] Implementar `NubiTooltip`: información contextual al hover, configurable en posición (top/bottom/left/right)
- [ ] Registrar todos los componentes en el catálogo de desarrollo con variantes, estados y tamaños
- [ ] Validar todos los textos con i18n (`$t()`) — sin literales en templates
- [ ] Validar accesibilidad táctil (mínimo 48x48dp) en todos los elementos interactivos

## Acceptance Criteria
- El sidebar se expande y colapsa con animación suave (200-300ms)
- El sidebar muestra iconos + texto en modo expandido y solo iconos en modo colapsado
- El sidebar es responsive: overlay en móvil (< 768dp), lateral fijo en tablet (>= 768dp)
- El indicador de tab activa es visualmente claro (color, subrayado o fondo)
- El breadcrumb muestra la ruta completa y permite volver a niveles anteriores
- El botón atrás tiene posición consistente (izquierda superior) en todas las vistas
- El modal de confirmación tiene overlay oscuro y botones de confirmar y cancelar
- El modal informativo tiene solo botón de cierre y soporta scroll interno
- El toast desaparece automáticamente en 3-5 segundos y soporta cola de múltiples mensajes
- La alerta persiste hasta cierre manual y distingue tipos (info, warning, error, success)
- El tooltip aparece al hover en la posición configurada y no se usa como única forma de comunicación
- Todos los modales implementan focus trapping para accesibilidad
- Todos los textos visibles están internacionalizados (i18n)
- Todos los componentes están registrados en el catálogo con sus variantes y estados

## Risks
- El sidebar es el componente más complejo de este sprint y requiere definición clara de las secciones del panel
- Los modales necesitan focus trapping para accesibilidad, lo que añade complejidad de implementación
- El sistema de cola de toasts requiere gestión de temporizadores y animaciones de entrada/salida
- El tooltip no funciona en dispositivos táctiles sin hover; se necesita estrategia alternativa para móvil
- La integración del sidebar con el router requiere coordinación con los guards de navegación existentes

## Dependencies
- SPRINT-005 completado (componentes de entrada)
- Componentes de acción del SPRINT-004 (botones para modales, sidebar, tabs)
- Tokens CSS del sistema de diseño (SPRINT-003)
- Definición de secciones del panel parental (para configurar items del sidebar)

## Agent Instruction
- Los componentes deben seguir la nomenclatura con prefijo `Nubi` (ej: `NubiSidebar`, `NubiTabs`)
- Usar las variables CSS del sistema de diseño para todos los valores de estilo
- Implementar con Vue 3 + TypeScript, usando `<script setup>` y Composition API
- El sidebar debe integrarse con Vue Router para navegación entre secciones del panel
- Los modales deben usar `<Teleport>` de Vue 3 para renderizarse en el body
- El sistema de toasts debe implementarse como composable (`useToast()`) + componente de presentación
- Los modales deben emitir eventos `@confirm`, `@cancel`, `@close` estandarizados
- El tooltip debe detectar posición disponible en viewport para evitar desbordamiento
- Validar focus trapping en modales con Tab y Shift+Tab
- Validar en Samsung Galaxy A15 físico como criterio de aceptación obligatorio

## Notes
- El sidebar es el componente de navegación principal del panel parental; su diseño condiciona la arquitectura de navegación
- El tooltip en táctiles puede implementarse como tap-to-show con dismiss automático
- Los modales deben bloquear el scroll del body cuando están abiertos (`overflow: hidden`)
- El sistema de toasts debe permitir diferentes niveles: success, error, warning, info
- El breadcrumb puede usar la metadata de las rutas de Vue Router para generar la ruta automáticamente
- Considerar que el sidebar debe soportar selector de contexto (global vs. niño) en cabecera (ADR-017)

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
