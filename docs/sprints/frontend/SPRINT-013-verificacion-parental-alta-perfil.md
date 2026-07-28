# SPRINT-013 — Frontend — Verificación parental y alta de perfil

## Goal
Implementar el flujo completo de verificación parental mediante PIN y alta de perfil infantil con stepper de 2 pasos (nombre, fecha de nacimiento + avatar) dentro de ChildSelectionModal.

## Status
status: pending
started_at: —
closed_at: —
blocked_by: —
waiting_for: —

## Context
FEAT-003 define la selección y alta de perfiles infantiles. SPRINT-012 implementó la infraestructura de datos y el listado de perfiles en cuadrícula. Este sprint completa el modal con verificación parental y alta de nuevos perfiles.

### Requisitos de FEAT-003 a implementar en este sprint
- Botón "Registrar niño" bajo la cuadrícula requiere verificación parental mediante PIN antes de iniciar alta
- Verificación parental: solicitar PIN de 4 dígitos y validar contra backend (POST /login)
- Si PIN es correcto, mostrar stepper de alta con 2 pasos
- Si PIN es incorrecto, mostrar error y permitir reintentar
- Stepper Paso 1: introducir nombre del niño (obligatorio)
- Stepper Paso 2: introducir fecha de nacimiento (obligatoria) + seleccionar avatar (opcional, primer avatar preseleccionado)
- No se puede confirmar alta sin nombre y fecha de nacimiento
- Solo se pueden elegir avatares del catálogo predefinido (6 avatares)
- No se permiten imágenes propias ni carga de archivos
- Tras confirmar alta, nuevo perfil aparece en cuadrícula (selección manual, no automática)
- Cancelación: permitir cancelar en cualquier paso, volver a vista de selección
- Manejo de errores de API (400, 409, 500, 0)
- i18n completo (español)

### Requisitos de FEAT-003 ya implementados en SPRINT-012
- Modal muestra título "Familia <nombre de familia>"
- Listado de perfiles en cuadrícula
- Selección de perfil existente y navegación a GameView
- Botón "Registrar niño" visible (sin acción)

## Tasks
- [ ] Extender `familyService.ts` con método `verifyPin(pin: string)` para consumir POST /login
- [ ] Extender `familyService.ts` con método `createChild(request: CreateChildRequest)` para consumir POST /family/children
- [ ] Crear interfaz TypeScript `CreateChildRequest` con campos: name, birthday, avatar, ttsEnabled (true), agentEnabled (true), colorVisionMode (null)
- [ ] Crear interfaz TypeScript `VerifyPinResult` con campos: success, errorKey
- [ ] Crear interfaz TypeScript `CreateChildResult` con campos: success, data?, errorKey?
- [ ] Añadir vista `pin-verification` en ChildSelectionModal:
  - NubiPinInput con 4 dígitos, tipo password, masked
  - Botón "Verificar"
  - Botón "Cancelar" (vuelve a vista selection)
  - Manejo de error PIN incorrecto (mensaje claro, permite reintentar)
- [ ] Crear componente `AvatarSelector.vue`:
  - Grid de 6 avatares SVG (2x3 en móvil, 3x2 en tablet)
  - Cada avatar es seleccionable (radio button visual)
  - Avatar inicial seleccionado: "avatar-1"
  - Accesibilidad: role="radiogroup", aria-label="Selecciona un avatar"
  - Identificación por icono + color (no solo color)
  - Objetivo táctil mínimo 48x48dp
- [ ] Añadir vista `registration` en ChildSelectionModal con NubiStepper (2 pasos):
  - Paso 1: NubiTextInput para nombre del niño
  - Paso 2: NubiTextInput type="date" para fecha de nacimiento + AvatarSelector
  - Validación: nombre no vacío, fecha no vacía, fecha no futura
  - Avatar inicial: "avatar-1" por defecto
- [ ] Implementar botón "Siguiente" en Paso 1 (habilitado solo si nombre no está vacío)
- [ ] Implementar botón "Atrás" en Paso 2 (vuelve a Paso 1)
- [ ] Implementar botón "Confirmar alta" en Paso 2 (habilitado solo si validaciones pasan)
- [ ] Implementar botón "Cancelar" en ambos pasos (vuelve a vista selection sin crear perfil)
- [ ] Implementar llamada a POST /family/children tras confirmación con valores fijos ttsEnabled: true, agentEnabled: true, colorVisionMode: null
- [ ] Implementar manejo de estados: submitting, error, success
- [ ] Implementar manejo de errores HTTP:
  - 400 Bad Request: mostrar mensaje de validación desde error.details.message
  - 409 Conflict: nombre duplicado (si backend lo valida) → mostrar error
  - 500 Server Error: mensaje genérico "Error al guardar. Inténtalo de nuevo."
  - 0 Connection: mensaje "Sin conexión. Revisa tu red."
- [ ] Implementar refresco de lista de perfiles tras alta exitosa (llamar a fetchProfiles() del composable)
- [ ] Implementar navegación: tras alta exitosa, volver a vista `selection` con lista actualizada
- [ ] Implementar transiciones suaves entre vistas del modal (selection → pin → registration → selection)
- [ ] Implementar foco automático en primer campo al cambiar de vista/paso
- [ ] Añadir traducciones i18n completas en `es.ts` (views.home.childSelection.pinVerification.*, views.home.childSelection.registration.*)
- [ ] Implementar accesibilidad: aria-live para mensajes de error, aria-invalid en campos con error
- [ ] Validar objetivo táctil mínimo 48x48dp en todos los elementos interactivos
- [ ] Pruebas manuales en móvil y tablet (portrait y landscape)
- [ ] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- Botón "Registrar niño" abre vista de verificación parental con NubiPinInput
- Vista de verificación parental solicita PIN de 4 dígitos numéricos
- Si PIN es correcto (POST /login responde 200), se muestra stepper de alta
- Si PIN es incorrecto (POST /login responde 401), se muestra mensaje "PIN incorrecto. Inténtalo de nuevo." y permite reintentar
- Stepper muestra 2 pasos: Paso 1 (nombre), Paso 2 (fecha + avatar)
- Paso 1 permite introducir nombre del niño y avanzar a Paso 2 con botón "Siguiente"
- Paso 2 permite introducir fecha de nacimiento y seleccionar avatar
- Primer avatar ("avatar-1") está seleccionado inicialmente en Paso 2
- Solo se pueden elegir avatares del catálogo predefinido (6 opciones: avatar-1 a avatar-6)
- No hay opción de subir imágenes propias ni cargar archivos
- No se puede confirmar alta si nombre está vacío
- No se puede confirmar alta si fecha de nacimiento está vacía
- No se puede introducir fecha de nacimiento futura (validación frontend)
- Tras confirmar alta, se llama a POST /family/children con name, birthday, avatar, ttsEnabled: true, agentEnabled: true, colorVisionMode: null
- Tras alta exitosa, modal vuelve a vista `selection` con lista de perfiles actualizada (nuevo perfil visible)
- Nuevo perfil NO se selecciona automáticamente (selección manual)
- Si se cancela en cualquier paso (PIN o registro), modal vuelve a vista `selection` sin crear perfil
- Errores de API (400, 409, 500, 0) muestran mensajes apropiados y claros
- Foco automático en primer campo al cambiar de vista/paso
- Transiciones suaves entre vistas (0.3s, consistente con ADR-018)
- Responsive en móvil y tablet (portrait y landscape)
- Accesibilidad táctil mínimo 48x48dp en todos los elementos interactivos
- i18n completo: sin literales en templates
- Build exitoso sin errores de TypeScript

## Risks
- Backend puede no devolver mensajes de error detallados en 400 Bad Request (confirmar formato de error.details)
- POST /login puede requerir campos adicionales además de PIN (confirmar contrato login-request.yaml)
- NubiStepper puede no existir o requerir ajustes (verificar en components/base/)
- El chunk size de HomeView puede aumentar al añadir lógica de verificación y alta (actualmente 661 kB en SPRINT-011)
- Validaciones de backend para nombre y fecha pueden diferir de las de frontend (longitud máxima, formato de fecha)
- Fecha de nacimiento puede ser sensible a formato de locale (DD/MM/YYYY vs MM/DD/YYYY) — usar input type="date" nativo del navegador

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/login.yaml` (POST /login)
  - `docs/contracts/api/openapi/schemas/auth/login-request.yaml` (request body: pin)
  - `docs/contracts/api/openapi/schemas/auth/api-login-response.yaml` (response)
  - `docs/contracts/api/openapi/paths/family/create-children.yaml` (POST /family/children)
  - `docs/contracts/api/openapi/schemas/family/create-child-profile-request.yaml` (request body)
  - `docs/contracts/api/openapi/schemas/family/api-child-profile-response.yaml` (response)
- **Assets:**
  - `src/assets/icons/custom/child-avatars.svg` (sprite SVG con 6 avatares, ya existe)
- **Componentes base:**
  - `NubiInfoModal` (base del modal, ya existe)
  - `NubiStepper` (para stepper de alta, ya existe en components/base/)
  - `NubiPinInput` (para verificación parental, ya existe en components/base/)
  - `NubiTextInput` (para nombre y fecha, ya existe)
  - `NubiButton` (para botones de acción, ya existe)
  - `NubiIcon` (para iconos, ya existe)
- **Composables:**
  - `useChildProfiles` (para refrescar lista tras alta, creado en SPRINT-012)
- **Stores:**
  - `useSessionStore` (para mantener estado de familia, ya existe)
- **Servicios:**
  - `apiClient` (para llamadas HTTP, ya existe en services/api.ts)
  - `familyService.ts` (para extender con nuevos métodos, ya existe)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - POST /login debe estar implementado y funcional
  - POST /family/children debe estar implementado y funcional
  - Confirmar si POST /login requiere campos adicionales (familyId, etc.)
  - Confirmar validaciones de backend para nombre y fecha de nacimiento

## Agent Instruction
- **No modificar** la vista `selection` implementada en SPRINT-012 (listado de perfiles, selección, navegación a GameView)
- **Usar** componentes base de `components/base/` (NubiInfoModal, NubiStepper, NubiPinInput, NubiTextInput, NubiButton, NubiIcon)
- **Verificar** si NubiStepper existe; si no existe, crearlo como componente base reutilizable
- **Consumir** sprite SVG `child-avatars.svg` con sintaxis `<use href="/src/assets/icons/custom/child-avatars.svg#avatar-{id}" />`
- **Enviar** valores fijos en POST /family/children: `ttsEnabled: true`, `agentEnabled: true`, `colorVisionMode: null`
- **No limitar** longitud de nombre en frontend (backend no valida), solo truncar visualmente a 30 caracteres en ChildProfileCard (ya implementado en SPRINT-012)
- **Validar** fecha de nacimiento en frontend: no vacía, no futura, formato correcto (usar input type="date")
- **Consumir** contratos API definidos en `docs/contracts/api/openapi/`
- **Manejar** errores HTTP de forma genérica sin revelar información sensible
- **Nunca** mostrar el PIN en logs, consola o mensajes de error
- **Mantener** separación entre experiencia infantil y controles parentales (la verificación PIN es para adultos)
- **Aplicar** transiciones suaves de 0.3s entre vistas del modal (consistent con ADR-018)
- **Validar** responsive en portrait y landscape con media queries CSS
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Verificación parental:** El PIN se valida contra backend con POST /login. Si backend responde 200, el PIN es correcto. Si responde 401, el PIN es incorrecto. Frontend no almacena el PIN ni lo muestra en logs.
- **Stepper de alta:** NubiStepper ya existe en `components/base/NubiStepper.vue`. Soporta navegación entre pasos con indicadores visuales. Se usará con 2 pasos: nombre; fecha + avatar.
- **AvatarSelector:** Componente nuevo que muestra 6 avatares en grid. Cada avatar es un radio button visual. El primer avatar ("avatar-1") está seleccionado por defecto. Accesibilidad: role="radiogroup", aria-label="Selecciona un avatar".
- **Fecha de nacimiento:** Se usa input type="date" nativo del navegador. Esto garantiza compatibilidad con formatos de locale y evita problemas de parsing. Validación frontend: no vacía, no futura.
- **Valores fijos:** El contrato `create-child-profile-request.yaml` incluye campos `ttsEnabled`, `agentEnabled`, `colorVisionMode`. FEAT-003 no menciona estos campos, por lo que se envían con valores fijos: `ttsEnabled: true`, `agentEnabled: true`, `colorVisionMode: null`.
- **Selección manual tras alta:** Tras alta exitosa, el modal vuelve a vista `selection` con la lista actualizada. El adulto debe seleccionar manualmente el nuevo perfil para iniciar su experiencia. No se llama a `useSessionStore.selectChild()` automáticamente.
- **Seguridad:** El PIN es un dato sensible de control parental. Nunca debe mostrarse como texto legible durante su introducción ni volver a mostrarse después. El tratamiento técnico (hash, almacenamiento) es responsabilidad de backend.
- **Experiencia de usuario:** El stepper debe comunicar de forma comprensible el paso actual y que existen dos pasos. Los avatares deben ser visualmente distintos (icono + color, no solo color) para facilitar la selección por parte de niños pequeños (aunque el alta es para adultos, la estética debe ser coherente con el producto).
- **Responsive:** En móvil (<768px), AvatarSelector muestra 2 columnas (3 filas). En tablet/desktop (≥768px), 3 columnas (2 filas). Esto permite mostrar los 6 avatares en una cuadrícula compacta y accesible.
- **Errores de API:** Backend puede devolver mensajes de error en `error.details.message` (400 Bad Request). Frontend debe mostrar estos mensajes si están disponibles, o mensajes genéricos si no lo están. Ejemplos:
  - 400: "El nombre ya existe" (si backend valida duplicados)
  - 409: "Ya existe un perfil con ese nombre"
  - 500: "Error al guardar. Inténtalo de nuevo."
  - 0: "Sin conexión. Revisa tu red."

## Review
(completar al finalizar el sprint)
