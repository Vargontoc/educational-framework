# Sprint 007 - Frontend — Componentes de contenido, progreso y sesión/seguridad

## Goal
Implementar componentes de contenido (card, avatar, badge, lista, grid), componentes de progreso (barra, stepper, contador) y componentes de sesión y seguridad (pantalla PIN, indicador sesión, overlay inactividad).

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-006
waiting_for: Endpoint de validación de PIN del backend

## Tasks
- [ ] Implementar `NubiCard`: contenedor con imagen + título + descripción + acciones, variantes con/sin imagen
- [ ] Implementar `NubiAvatar`: imagen circular/redondeada, tamaños pequeño/mediano/grande, fallback con iniciales
- [ ] Implementar `NubiBadge`: pequeño círculo o etiqueta para estados, variantes por tipo (info, success, warning, error)
- [ ] Implementar `NubiList`: elementos apilados verticalmente con separadores sutiles, soporta slots por item
- [ ] Implementar `NubiGrid`: cuadrícula responsive (1 columna móvil, 2-3 columnas tablet), configurable
- [ ] Implementar `NubiProgressBar`: indicador visual de avance con animación suave, label con porcentaje
- [ ] Implementar `NubiStepper`: formularios multi-paso, muestra paso actual y total, navegación entre pasos
- [ ] Implementar `NubiCounter`: muestra cantidad, soporta modo estático y animado con transición numérica
- [ ] Implementar `NubiAuthScreen`: vista completa para entrada de PIN, teclado numérico integrado, mensaje de error tras fallo
- [ ] Implementar `NubiSessionIndicator`: muestra tiempo restante antes de logout automático, aviso visual cuando queda poco tiempo
- [ ] Implementar `NubiInactivityOverlay`: aparece antes del logout automático, permite extender sesión o cerrar
- [ ] Implementar lógica de logout automático tras 5 minutos de inactividad (detector de eventos de usuario)
- [ ] Validar contraste WCAG AA en todos los componentes implementados
- [ ] Registrar todos los componentes en el catálogo con variantes y estados
- [ ] Validar todos los textos con i18n (`$t()`) — sin literales en templates
- [ ] Validar accesibilidad táctil (mínimo 48x48dp) en todos los elementos interactivos

## Acceptance Criteria
- La card muestra imagen, título, descripción y acciones en layout consistente
- El avatar soporta 3 tamaños (pequeño, mediano, grande) y fallback con iniciales cuando no hay imagen
- El badge distingue visualmente entre tipos de estado (info, success, warning, error)
- La lista presenta separadores sutiles y soporta contenido variable por item
- La grid es responsive: 1 columna en móvil, 2-3 columnas en tablet según breakpoint
- La barra de progreso muestra avance con animación suave y label de porcentaje
- El stepper indica claramente el paso actual y total, permite navegación entre pasos completados
- El contador soporta modo estático y animado con transición numérica fluida
- La pantalla de autenticación acepta PIN de 4 dígitos numéricos con teclado estilo móvil
- La pantalla de autenticación muestra mensaje de error tras fallo de PIN
- El indicador de sesión muestra tiempo restante y avisa cuando queda poco tiempo (< 1 minuto)
- El overlay de inactividad permite extender sesión o cerrar antes del logout automático
- El logout automático se ejecuta tras 5 minutos de inactividad sin interacción del usuario
- Todos los componentes son visualmente correctos en ambos temas (claro y oscuro)
- El contraste cumple WCAG AA en ambos temas
- Todos los textos visibles están internacionalizados (i18n)
- Todos los componentes están registrados en el catálogo con variantes

## Risks
- Los componentes de sesión requieren integración con endpoint de validación de PIN del backend
- El detector de inactividad puede generar falsos positivos si no se monitorizan todos los tipos de interacción (touch, mouse, keyboard)
- La persistencia en localStorage puede no funcionar si el usuario navega en modo incógnito o con almacenamiento deshabilitado
- El overlay de inactividad debe coordinarse con el WebSocket ParentChannel para sincronizar estado de sesión

## Dependencies
- SPRINT-006 completado (componentes de navegación y feedback)
- Componentes de acción del SPRINT-004 (botones para card, modales de sesión)
- Componentes de entrada del SPRINT-005 (PIN input para pantalla de autenticación)
- Sistema de temas (claro/oscuro) implementado en SPRINT-003
- Endpoint de validación de PIN del backend (para `NubiAuthScreen`)

## Agent Instruction
- Los componentes deben seguir la nomenclatura con prefijo `Nubi` (ej: `NubiCard`, `NubiAuthScreen`)
- Usar las variables CSS del sistema de diseño para todos los valores de estilo
- Implementar con Vue 3 + TypeScript, usando `<script setup>` y Composition API
- Los componentes deben ser visualmente correctos en ambos temas (claro y oscuro) implementados en SPRINT-003
- El detector de inactividad debe implementarse como composable (`useInactivityDetector()`) configurable
- El timeout de inactividad (5 minutos) debe ser configurable mediante variable o constante centralizada
- Los componentes de sesión deben integrarse con `useSessionStore` existente (SPRINT-002)
- Validar en Samsung Galaxy A15 físico como criterio de aceptación obligatorio
- Probar todos los componentes en ambos temas antes de dar por completado el sprint

## Notes
- Este sprint cierra el FEAT-001 al completar todos los componentes globales del panel parental
- Los componentes de sesión son la base para el flujo de acceso al panel parental en sprints posteriores
- El `NubiAuthScreen` es el primer componente que requiere integración real con backend (validación PIN)
- Considerar que el stepper puede necesitar integración con formularios de gestión de niños (sprints posteriores)
- El `NubiInactivityOverlay` debe mostrar cuenta atrás visual para que el adulto sepa cuánto tiempo queda
- Los componentes deben consumir el sistema de temas implementado en SPRINT-003

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
