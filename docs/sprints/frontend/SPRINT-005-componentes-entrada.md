# Sprint 005 - Frontend

## Goal
Implementar todos los componentes de entrada (input texto, numérico, PIN, checkbox, toggle, selector, radio buttons) para permitir la interacción del adulto con el panel parental.

## Status
status: implemented
started_at: 2026-07-23
closed_at: 2026-07-23
blocked_by:
waiting_for:

## Tasks
- [x] Implementar `NubiTextInput`: label visible, placeholder, validación en tiempo real, estados error/hint
- [x] Implementar `NubiNumberInput`: teclado numérico en móvil, botones incremento/decremento, validación de rango
- [x] Implementar `NubiPinInput`: exactamente 4 dígitos numéricos, estilo teclado móvil, dígitos ocultos, feedback visual de completado
- [x] Implementar `NubiCheckbox`: opción binaria con label claro, estados checked/unchecked/indeterminate
- [x] Implementar `NubiToggle`: alternativa on/off visual para configuraciones frecuentes, animación suave
- [x] Implementar `NubiSelect`: selección única entre múltiples opciones, dropdown accesible, opción por defecto
- [x] Implementar `NubiRadioGroup`: selección única entre opciones mutuamente excluyentes, label por opción
- [x] Registrar todos los componentes en el catálogo de desarrollo con variantes, estados y tamaños
- [x] Validar todos los textos con i18n (`$t()`) — sin literales en templates
- [x] Validar accesibilidad táctil (mínimo 48x48dp) en todos los elementos interactivos
- [x] Validar navegación por teclado (Tab, Enter, Space, flechas) en todos los componentes

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
  - NubiTextInput: implementado con label, placeholder, validación en tiempo real (blur/change), estados error/hint, iconos prefix/suffix, contador de caracteres, soporte v-model, tipos text/email/password/tel/url
  - NubiNumberInput: implementado con teclado numérico (inputmode="numeric"), botones incremento/decremento, validación de rango min/max, step configurable, navegación por teclado (flechas arriba/abajo), aria-valuenow/min/max
  - NubiPinInput: implementado con 4 dígitos numéricos (configurable hasta 6), inputmode="numeric", modo masked con dígitos ocultos, feedback visual al completar, auto-avance entre campos, navegación backspace/flechas
  - NubiCheckbox: implementado con label claro, estados checked/unchecked/indeterminate (aria-checked="mixed"), soporte v-model, objetivo táctil 48px
  - NubiToggle: implementado con role="switch", animación suave 200ms, soporte v-model, estado visible opcional (on/off), objetivo táctil 48px
  - NubiSelect: implementado con role="combobox"/"listbox", dropdown con Teleport al body, focus trapping, navegación completa por teclado (Enter, Space, flechas, Escape, Home, End), aria-activedescendant, posicionamiento dinámico
  - NubiRadioGroup: implementado con role="radiogroup", opciones verticales/horizontales, navegación por teclado (flechas), soporte opciones disabled individuales, objetivo táctil 48px
  - Catálogo: 7 vistas de catálogo registradas con rutas /dev/components/{text-input, number-input, pin-input, checkbox, toggle, select, radio-group}
  - Catálogo: 7 archivos .story.vue para Histoire con variantes y documentación
  - i18n: traducciones completas para todos los componentes en es.ts (components.textInput, components.numberInput, components.pinInput, components.checkbox, components.toggle, components.select, components.radioGroup)
  - Catálogo: navegación actualizada en CatalogLayout.vue con sección "Componentes de entrada"
  - Build exitoso sin errores (202ms)

incomplete_tasks:

contract_changes:

learnings:
  - useId() de Vue 3 es ideal para generar IDs únicos de accesibilidad (aria-describedby, aria-labelledby) sin conflictos
  - Teleport al body es necesario para dropdowns que pueden ser cortados por overflow de contenedores padres
  - El posicionamiento fixed con getBoundingClientRect() funciona mejor que absolute para dropdowns en contenedores con scroll
  - inputmode="numeric" es más fiable que type="number" para inputs de un solo dígito en móvil Android
  - La validación con touched ref evita mostrar errores antes de que el usuario interactúe con el campo
  - Las opciones de NubiSelect y NubiRadioGroup aceptan tanto strings/numbers simples como objetos {value, label} para flexibilidad
  - El patrón de normalización de opciones (normalizeOption) permite una API más flexible sin duplicar lógica

next_sprint_suggestions:
  - SPRINT-006: Componentes de layout (NubiCard, NubiModal, NubiFormField wrapper)
  - SPRINT-007: Vistas del panel parental usando los componentes de entrada
