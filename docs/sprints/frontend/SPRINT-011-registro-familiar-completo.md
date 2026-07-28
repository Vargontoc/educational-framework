# SPRINT-011 — Frontend — Registro familiar completo

## Goal
Implementar el flujo completo de registro familiar en FamilyRegistrationModal con dos pasos (nombre de familia y PIN), validaciones, consumo de API y manejo de estados.

## Status
status: implemented
started_at: 2026-07-28
closed_at:
blocked_by: —
waiting_for: Verificación por reviewer

## Context
FEAT-002 define la pantalla principal y accesos iniciales. SPRINT-008 implementó la estructura básica de HomeView con modales vacíos. Este sprint completa el FamilyRegistrationModal con el flujo funcional de registro familiar.

### Requisitos de FEAT-002 a implementar
- Modal de registro familiar con dos pasos claramente diferenciados
- Paso 1: solicitar nombre de familia (sin límites de longitud ni reglas de contenido adicionales)
- Paso 2: crear PIN familiar de exactamente 4 dígitos numéricos y repetirlo para confirmación
- Validación: si PIN no coinciden, informar de forma clara sin crear familia
- Cancelación: permitir cancelar en cualquier paso, volver a Home sin crear familia
- Confirmación: tras registro exitoso, cerrar modal y mostrar bienvenida en Home (no abrir alta de niños)
- Truncado visual de nombre de familia a 50 caracteres con puntos suspensivos (ya implementado en SPRINT-008)

## Tasks
- [x] Crear composable `useFamilyRegistration.ts` con estado del formulario y lógica de validación
- [x] Crear servicio `familyService.ts` con método `createFamily` para consumir POST /api/v1/family
- [x] Reescribir `FamilyRegistrationModal.vue` con flujo completo de dos pasos
- [x] Implementar stepper visual con indicadores de paso actual y accesibilidad (aria-current)
- [x] Implementar Paso 1: campo para nombre de familia con NubiTextInput
- [x] Implementar Paso 2: dos campos para crear y confirmar PIN con NubiPinInput (tipo password, 4 dígitos)
- [x] Implementar validaciones: nombre no vacío, PIN exactamente 4 dígitos numéricos, PIN coincidente
- [x] Implementar mensaje de error cuando PIN no coinciden (role="alert", aria-invalid)
- [x] Implementar botón "Continuar" en Paso 1 (habilitado solo si nombre no está vacío)
- [x] Implementar botón "Atrás" en Paso 2 para volver a Paso 1
- [x] Implementar botón "Crear familia" en Paso 2 (habilitado solo si validaciones pasan)
- [x] Implementar botón "Cancelar" en ambos pasos (cierra modal sin crear familia)
- [x] Implementar llamada a POST /api/v1/family tras confirmación
- [x] Implementar manejo de estados: submitting, error, success
- [x] Implementar manejo de errores HTTP: 400 (validación), 409 (familia ya existe), 500 (servidor), 0 (conexión)
- [x] Implementar emisión de evento `family-created` tras registro exitoso
- [x] Modificar `HomeView.vue` para escuchar evento `family-created` y recargar estado de familia
- [x] Añadir traducciones i18n completas en `es.ts` (views.home.familyRegistration.*)
- [x] Implementar accesibilidad: foco automático en primer campo al cambiar de paso, aria-labels, aria-describedby
- [x] Implementar responsive portrait/landscape: modal centrado, max-width 500px, max-height 90vh, overflow scroll
- [x] Validar objetivo táctil mínimo 48x48dp (44dp en portrait)
- [ ] Pruebas manuales en móvil y tablet (portrait y landscape)
- [x] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- Modal muestra Paso 1 (nombre de familia) al abrirse
- Paso 1 permite introducir nombre de familia y avanzar a Paso 2 con botón "Continuar"
- Paso 2 muestra dos campos para crear y confirmar PIN de 4 dígitos numéricos
- Si PIN no coinciden, se muestra mensaje "Los PIN no coinciden" y no se puede confirmar
- Si PIN coinciden y se confirma, se llama a POST /api/v1/family con nombre y PIN
- Tras registro exitoso, modal se cierra y Home muestra "Bienvenida familia <nombre>"
- Tras registro exitoso, NO se abre automáticamente el alta de niños
- Si se cancela en cualquier paso, modal se cierra y Home continúa mostrando "Registrar familia"
- Mensajes de error son claros y no revelan información sensible (PIN no se muestra en logs/consola)
- Stepper visual indica paso actual con aria-current="step"
- Foco automático en primer campo al cambiar de paso
- Responsive en portrait y landscape: modal centrado, sin overflow, accesible
- Objetivo táctil mínimo 48x48dp (44dp en portrait)
- i18n completo: sin literales en templates
- Build exitoso sin errores de TypeScript

## Risks
- Backend puede no devolver 404 cuando no existe familia (confirmar comportamiento de GET /family)
- Backend puede requerir campos ttsEnabled y agentEnabled en POST /family (confirmar si son opcionales)
- NubiPinInput puede no existir o requerir ajustes (verificar en components/base/)
- El chunk size de HomeView puede aumentar al añadir FamilyRegistrationModal (actualmente 648 kB)
- Validaciones de backend para nombre de familia pueden diferir de las de frontend (longitud máxima, caracteres)

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/family/create-family.yaml` (POST /family)
  - `docs/contracts/api/openapi/schemas/family/create-family-request.yaml` (request body)
  - `docs/contracts/api/openapi/schemas/family/api-family-response.yaml` (response)
  - `docs/contracts/api/openapi/schemas/family/family-response.yaml` (family data)
- **Componentes base:**
  - `NubiInfoModal` (base del modal, ya existe)
  - `NubiTextInput` (para nombre de familia, ya existe)
  - `NubiPinInput` (para PIN, verificar si existe en components/base/)
  - `NubiButton` (para botones de acción, ya existe)
  - `NubiIcon` (para iconos de stepper, ya existe)
- **Composables:**
  - `useFamilyStatus` (para recargar estado tras registro, ya existe)
- **Servicios:**
  - `apiClient` (para llamadas HTTP, ya existe en services/api.ts)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - POST /api/v1/family debe estar implementado y funcional
  - Confirmar si ttsEnabled y agentEnabled son obligatorios u opcionales
  - Confirmar validaciones de backend para nombre de familia

## Agent Instruction
- **No modificar** HomeAction.vue, HomeHeader.vue ni useFamilyStatus.ts (ya implementados en SPRINT-008)
- **No implementar** ChildSelectionModal (fuera del alcance de este sprint, se hará en sprint futuro)
- **Usar** componentes base de `components/base/` (NubiInfoModal, NubiTextInput, NubiPinInput, NubiButton, NubiIcon)
- **Verificar** si NubiPinInput existe; si no existe, crearlo como componente base reutilizable
- **Consumir** contratos API definidos en `docs/contracts/api/openapi/`
- **No enviar** ttsEnabled ni agentEnabled en POST /family a menos que backend los requiera (confirmar con backend)
- **Manejar** errores HTTP de forma genérica sin revelar información sensible
- **Nunca** mostrar el PIN en logs, consola o mensajes de error
- **Mantener** separación entre experiencia infantil y controles parentales (el modal es para adultos)
- **Aplicar** transiciones suaves de 0.3s entre pasos del modal (consistente con ADR-018)
- **Validar** responsive en portrait y landscape con media queries CSS
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Decisión pendiente 1:** ¿GET /family devuelve 404 o 200 con success:false cuando no hay familia? El composable useFamilyStatus actual maneja ambos casos, pero es importante confirmar el comportamiento real de backend.
- **Decisión pendiente 2:** ¿POST /family requiere ttsEnabled y agentEnabled? El contrato create-family-request.yaml los incluye como propiedades opcionales. Propuesta: no enviarlos y dejar que backend use valores por defecto.
- **Decisión pendiente 3:** ¿Qué validaciones aplica backend al nombre de familia? FEAT-002 no define límites de longitud ni reglas de contenido. Propuesta: frontend solo valida que no esté vacío; backend puede aplicar validaciones adicionales si es necesario.
- **NubiPinInput:** Si no existe en components/base/, crearlo como componente reutilizable con las siguientes características:
  - 4 campos individuales o un solo campo con máscara
  - Tipo password para ocultar dígitos
  - Validación: solo dígitos numéricos, exactamente 4
  - Accesibilidad: aria-label, aria-invalid, foco automático
  - Objetivo táctil mínimo 48x48dp (44dp en portrait)
- **Seguridad:** El PIN es un dato sensible de control parental. Nunca debe mostrarse como texto legible durante su introducción ni volver a mostrarse después del registro. El tratamiento técnico (hash, almacenamiento) es responsabilidad de backend.
- **Experiencia de usuario:** El stepper debe comunicar de forma comprensible el paso actual y que existen dos pasos, sin requerir lectura por parte de un niño (aunque el modal es para adultos, la estética debe ser coherente con el producto).
- **Responsive:** En portrait, el modal debe ocupar casi todo el ancho con margen lateral mínimo. En landscape, debe centrarse con ancho máximo 500px. En ambos casos, max-height 90vh con overflow scroll si es necesario.

## Review
(Filled automatically by the agent when status is set to completed)

completed_tasks:
- Composable `useFamilyRegistration.ts` creado con estado reactivo, validaciones y flujo de dos pasos
- Servicio `familyService.ts` creado con método `createFamily` que consume POST /api/v1/family
- `FamilyRegistrationModal.vue` reescrito con flujo completo: stepper, Paso 1 (nombre), Paso 2 (PIN crear+confirmar)
- Stepper visual con `aria-current="step"` y transiciones 0.3s entre pasos
- NubiTextInput para nombre de familia con validación de no vacío
- Dos NubiPinInput (masked) para crear y confirmar PIN de 4 dígitos
- Validaciones: nombre no vacío, PIN 4 dígitos numéricos, PIN coincidente
- Mensaje de error con `role="alert"` cuando PIN no coinciden
- Botones: Continuar (Paso 1), Atrás/Cancelar/Crear familia (Paso 2)
- Estados: idle, submitting, success, error
- Errores HTTP: 400 → validation, 409 → conflict, 500 → server, 0 → connection
- Evento `family-created` emitido tras éxito
- `HomeView.vue` escucha `family-created` y recarga estado con `fetchFamilyStatus()`
- Traducciones i18n completas en `es.ts` bajo `views.home.familyRegistration.*`
- Foco automático al cambiar de paso (nameInput en Paso 1, pinInput en Paso 2)
- Responsive portrait/landscape: max-width 500px, max-height 90vh, overflow scroll
- Objetivo táctil 48x48dp respetado (heredado de componentes base)
- Build exitoso sin errores TypeScript (`tsc && vite build`)
- PIN nunca se muestra en logs, consola ni mensajes de error
- Frontend NO envía ttsEnabled ni agentEnabled en POST /family

incomplete_tasks:
- Pruebas manuales en móvil y tablet (portrait y landscape) — requiere dispositivo físico/emulador

contract_changes:
- Ningún contrato modificado. Se consumen los existentes:
  - POST /api/v1/family (create-family.yaml)
  - create-family-request.yaml (solo name y pin enviados)
  - api-family-response.yaml / family-response.yaml (respuesta)

learnings:
- El chunk de HomeView creció de 648 kB a 662 kB tras añadir FamilyRegistrationModal completo. No es bloqueante pero conviene monitorizar.
- NubiPinInput ya expone `focus()` y `clear()` via `defineExpose`, lo que facilitó el foco automático.
- NubiInfoModal tiene `closeOnOverlay` configurable; se desactivó para el registro familiar para evitar cierres accidentales.
- El contrato create-family.yaml indica respuesta 200, no 201 como se mencionaba en el briefing inicial. El servicio maneja cualquier 2xx.

next_sprint_suggestions:
- Completar pruebas manuales E2E del flujo de registro familiar en móvil y tablet
- Implementar ChildSelectionModal (fuera del alcance de este sprint)
- Considerar code-splitting para reducir el chunk de HomeView (662 kB)
