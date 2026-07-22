# Sprint 005 - Frontend

## Goal
Implementar todos los componentes de entrada (input texto, numérico, PIN, checkbox, toggle, selector, radio buttons) para permitir la interacción del adulto con el panel parental.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-004
waiting_for: Paleta de colores confirmada, librería de iconos confirmada

## Tasks
- [ ] Implementar `NubiTextInput`: label visible, placeholder, validación en tiempo real, estados error/hint
- [ ] Implementar `NubiNumberInput`: teclado numérico en móvil, botones incremento/decremento, validación de rango
- [ ] Implementar `NubiPinInput`: exactamente 4 dígitos numéricos, estilo teclado móvil, dígitos ocultos, feedback visual de completado
- [ ] Implementar `NubiCheckbox`: opción binaria con label claro, estados checked/unchecked/indeterminate
- [ ] Implementar `NubiToggle`: alternativa on/off visual para configuraciones frecuentes, animación suave
- [ ] Implementar `NubiSelect`: selección única entre múltiples opciones, dropdown accesible, opción por defecto
- [ ] Implementar `NubiRadioGroup`: selección única entre opciones mutuamente excluyentes, label por opción
- [ ] Registrar todos los componentes en el catálogo de desarrollo con variantes, estados y tamaños
- [ ] Validar todos los textos con i18n (`$t()`) — sin literales en templates
- [ ] Validar accesibilidad táctil (mínimo 48x48dp) en todos los elementos interactivos
- [ ] Validar navegación por teclado (Tab, Enter, Space, flechas) en todos los componentes

## Acceptance Criteria
- El input de texto muestra label visible, placeholder, y validación en tiempo real con mensajes de error
- El input numérico presenta teclado numérico en móvil y botones de incremento/decremento funcionales
- El input PIN acepta exactamente 4 dígitos numéricos, oculta los dígitos y muestra feedback visual al completar
- El checkbox tiene label claro y estados checked/unchecked diferenciados visualmente
- El toggle cambia entre on/off con animación suave (200-300ms)
- El selector/dropdown permite selección única y es accesible por teclado
- Los radio buttons permiten selección única entre opciones mutuamente excluyentes
- Todos los componentes tienen objetivo táctil mínimo 48x48dp
- Todos los componentes son navegables por teclado (Tab, Enter, Space, flechas según aplique)
- Todos los textos visibles están internacionalizados (i18n)
- Todos los componentes están registrados en el catálogo con sus variantes y estados

## Risks
- El dropdown personalizado puede tener problemas de accesibilidad en navegadores móviles Android
- La validación en tiempo real del input de texto puede requerir debounce para evitar exceso de validaciones
- El PIN input necesita manejo especial del foco entre los 4 campos de dígitos
- El teclado numérico en móvil puede variar entre fabricantes Android

## Dependencies
- SPRINT-004 completado (componentes de acción y estado)
- Tokens CSS del sistema de diseño (SPRINT-003)
- Librería de iconos confirmada (para iconos en inputs: validación, incremento/decremento)

## Agent Instruction
- Los componentes deben seguir la nomenclatura con prefijo `Nubi` (ej: `NubiTextInput`, `NubiPinInput`)
- Usar las variables CSS del sistema de diseño para todos los valores de estilo
- Implementar con Vue 3 + TypeScript, usando `<script setup>` y Composition API
- Todos los componentes deben soportar `v-model` para two-way binding
- Los componentes de entrada deben emitir eventos `update:modelValue`, `@blur`, `@focus`, `@error`
- El `NubiPinInput` debe usar `inputmode="numeric"` y `maxlength="1"` por dígito
- El `NubiSelect` debe implementar focus trapping dentro del dropdown cuando está abierto
- Validar que los componentes funcionan correctamente con lectores de pantalla (aria-labels, roles)
- Validar en Samsung Galaxy A15 físico como criterio de aceptación obligatorio

## Notes
- El `NubiPinInput` es un componente crítico para la seguridad del panel parental
- Los componentes de entrada serán usados extensivamente en formularios de gestión de niños y ajustes
- Considerar implementar un componente `NubiFormField` wrapper que agrupe label + input + error message
- El `NubiSelect` puede requerir implementación custom de dropdown para consistencia visual en móvil
- La validación en tiempo real debe ser configurable (onBlur, onChange, onSubmit)

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
