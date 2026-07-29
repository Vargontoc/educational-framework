# SPRINT-014 — Frontend — Autenticación y portada neutral

## Goal
Implementar el flujo de acceso al panel parental mediante un modal de autenticación PIN sobre Home (sin vista independiente), consumo del contrato `/auth/login`, almacenamiento seguro del token de sesión, y presentación de una portada neutral con texto placeholder y tarjetas de acceso rápido agrupadas por sección.

## Status
status: closed
started_at: 2026-07-29
closed_at: 2026-07-29
blocked_by: —
waiting_for: —

## Context
FEAT-004 define la estructura visual y navegación del panel parental. Este sprint implementa el primer bloque: autenticación mediante PIN (como modal sobre Home) y portada neutral. SPRINT-015 completará la navegación adaptable y la acción «Salir».

La decisión de usar un modal en lugar de una vista independiente se basa en:
- FEAT-004 indica que el acceso es "desde Home mediante el icono de configuración"
- SPRINT-013 ya estableció el patrón de `ChildSelectionModal` con vista de verificación PIN integrada
- Evita sobrecargar la navegación con vistas adicionales
- Mantiene al adulto en el contexto de Home antes y después de la autenticación

### Requisitos de FEAT-004 a implementar en este sprint
- Acceso al panel parental requiere validación correcta de PIN familiar
- Modal de autenticación con teclado numérico estilo móvil (4 dígitos)
- Consumo de `POST /auth/login` con `{ pin: string }`
- Almacenamiento del token de sesión en `sessionStorage`
- Tras validación exitosa, cerrar modal y navegar a portada neutral
- Portada neutral: sin datos infantiles, sin progreso, sin avisos evaluativos, sin destacar secciones
- Tarjetas de acceso rápido agrupadas por sección (Panel / Experiencias)
- Guard de navegación que protege `/panel` y sub-rutas
- Manejo de errores: mensaje genérico "PIN incorrecto", bloqueo tras 3 intentos fallidos con cooldown de 15 segundos
- i18n completo (español)

### Requisitos de FEAT-004 ya implementados o fuera de alcance
- SPRINT-013 implementó verificación parental mediante PIN en ChildSelectionModal (reutilizar patrón)
- Navegación adaptable y acción «Salir» → SPRINT-015
- Logout automático por inactividad → SPRINT-016
- Modo oscuro → SPRINT-017

## Tasks
- [x] Crear componente `ParentalAuthModal.vue` (modal de autenticación PIN sobre Home)
  - [x] Reutilizar `NubiInfoModal` como base (patrón de SPRINT-013 con ChildSelectionModal)
  - [x] Vista interna `pin-verification` con `NubiPinInput` (masked, 4 dígitos, teclado numérico estilo móvil)
  - [x] Botón "Entrar" (deshabilitado hasta completar 4 dígitos)
  - [x] Botón "Cancelar" (cierra modal sin validar PIN, vuelve a Home)
  - [x] Mensaje de error "PIN incorrecto. Inténtalo de nuevo." tras respuesta 401
  - [x] Bloqueo tras 3 intentos fallidos consecutivos: cooldown 15 segundos
  - [x] Contador visual de intentos restantes (opcional, solo tras primer fallo)
  - [x] Timer de cooldown visible (cuenta regresiva 15s)
  - [x] Título del modal: "Acceso parental"
  - [x] `closeOnOverlay` desactivado durante verificación para prevenir cierre accidental
  - [x] Foco automático en primer dígito del PIN al abrir modal
- [x] Integrar `ParentalAuthModal` en `HomeView.vue`
  - [x] Icono de configuración (ajustes) visible en Home
  - [x] Al pulsar icono: abrir `ParentalAuthModal`
  - [x] Estado reactivo: `showParentalAuthModal` (boolean)
  - [x] Tras PIN correcto: cerrar modal y navegar a `/panel` con `router.replace()`
  - [x] Tras cancelar: cerrar modal y permanecer en Home
- [x] Crear composable `useParentalSession.ts` para gestión de sesión parental
  - [x] Método `login(pin: string)`: consume `POST /auth/login`
  - [x] Almacenar `token` en `sessionStorage`
  - [x] Actualizar estado `isAuthenticated` en store
  - [x] Método `logout()`: consume `POST /auth/logout` con `Authorization: Bearer <token>`
  - [x] Eliminar token de `sessionStorage` tras logout
  - [x] Resetear estado `isAuthenticated`
  - [x] Manejo de errores HTTP: 0 (conexión), 401 (PIN incorrecto), 500 (servidor)
- [x] Crear store Pinia `useParentalAuthStore.ts`
  - [x] Estado: `isAuthenticated`, `token`, `familyId`, `sessionId`, `loginAttempts`, `cooldownUntil`
  - [x] Actions: `login(pin)`, `logout()`, `resetAttempts()`, `incrementAttempts()`
  - [x] Getter: `isInCooldown` (calcula si timestamp actual < cooldownUntil)
- [x] Crear vista `PanelCoverView.vue` (portada neutral)
  - [x] Título: "Panel parental"
  - [x] Texto placeholder: "PLACEHOLDER: Descripción breve del panel parental, sujeta a validación de contenido."
  - [x] Tarjetas de acceso rápido agrupadas:
    - **Panel**: Configuración, Niños, Chatbot, Documentación
    - **Experiencias**: Lectura familiar, Relajación familiar
  - [x] Cada tarjeta: icono + etiqueta de texto (etiqueta obligatoria, icono opcional)
  - [x] Tarjetas no navegables en este sprint (solo visuales, navegación en SPRINT-015)
  - [x] Objetivo táctil mínimo 48x48dp
- [x] Crear guard de navegación `requiresParentalAuth`
  - [x] Proteger ruta `/panel` y sub-rutas
  - [x] Si `!isAuthenticated`, redirigir a Home con `router.replace('/')` (no a vista independiente)
  - [x] Home abrirá automáticamente el modal si se pasa query param `?parentalAuth=true` (opcional, no implementado en este sprint)
  - [x] Aplicar guard en `router/index.ts` para ruta `/panel`
- [x] Añadir rutas en `router/index.ts`
  - [x] `/panel` → `PanelCoverView.vue` (protegida con `requiresParentalAuth`)
  - [x] NO crear ruta `/parental-auth` (la autenticación es modal sobre Home)
- [x] Implementar manejo de errores de login
  - [x] 401 Unauthorized: mensaje "PIN incorrecto. Inténtalo de nuevo."
  - [x] 0 Connection: mensaje "Sin conexión. Revisa tu red."
  - [x] 500 Server Error: mensaje "Error al iniciar sesión. Inténtalo de nuevo."
  - [x] Tras 3 intentos fallidos consecutivos: bloquear entrada 15 segundos
  - [x] Mostrar timer de cooldown (cuenta regresiva)
  - [x] Tras cooldown, resetear contador de intentos
- [x] Añadir traducciones i18n en `es.ts`
  - [x] `modals.parentalAuth.*`: título, botón entrar, botón cancelar, error pin incorrecto, error conexión, error servidor, cooldown mensaje, intentos restantes
  - [x] `views.panelCover.*`: título, descripción placeholder, grupos (panel, experiencias), secciones (configuración, niños, chatbot, documentación, lectura familiar, relajación familiar)
- [x] Validar accesibilidad
  - [x] `aria-live` para mensajes de error
  - [x] `aria-invalid` en `NubiPinInput` tras error
  - [x] Foco automático en primer dígito del PIN al abrir modal
  - [x] Objetivo táctil mínimo 48x48dp en botones y tarjetas
  - [x] `aria-label` en icono de configuración de Home
- [x] Validar responsive en móvil y tablet (portrait y landscape)
  - [x] Modal adaptable en portrait y landscape
  - [x] Tarjetas en columna única en móvil, grid 2 columnas en tablet
- [x] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- Icono de configuración visible en Home con `aria-label` accesible
- Pulsar icono de configuración abre `ParentalAuthModal` sobre Home
- `ParentalAuthModal` muestra vista de autenticación con `NubiPinInput` (masked, 4 dígitos)
- Botón "Entrar" está deshabilitado hasta completar 4 dígitos
- Botón "Cancelar" cierra modal sin validar PIN y permanece en Home
- `POST /auth/login` se invoca con `{ pin: string }` al pulsar "Entrar"
- Si PIN correcto (200 OK): token se almacena en `sessionStorage`, `isAuthenticated = true`, modal se cierra, redirige a `/panel`
- Si PIN incorrecto (401): mensaje "PIN incorrecto. Inténtalo de nuevo." y permite reintentar
- Tras 3 intentos fallidos consecutivos: entrada bloqueada 15 segundos
- Timer de cooldown visible (cuenta regresiva 15s)
- Tras cooldown, contador de intentos se resetea y permite nuevos intentos
- Si conexión falla (0): mensaje "Sin conexión. Revisa tu red."
- Si servidor falla (500): mensaje "Error al iniciar sesión. Inténtalo de nuevo."
- `PanelCoverView` muestra título "Panel parental" y texto placeholder
- Portada neutral: sin datos infantiles, sin progreso, sin avisos evaluativos
- Tarjetas de acceso rápido agrupadas: Panel (Configuración, Niños, Chatbot, Documentación) / Experiencias (Lectura familiar, Relajación familiar)
- Cada tarjeta tiene etiqueta de texto obligatoria + icono opcional
- Tarjetas no son navegables en este sprint (solo visuales)
- Guard `requiresParentalAuth` protege `/panel`: si `!isAuthenticated`, redirige a Home (no a vista independiente)
- Token se elimina de `sessionStorage` al cerrar pestaña (comportamiento nativo de `sessionStorage`)
- `closeOnOverlay` desactivado en `ParentalAuthModal` durante verificación
- i18n completo: sin literales en templates
- Accesibilidad táctil mínimo 48x48dp en botones y tarjetas
- Responsive en móvil y tablet (portrait y landscape)
- Build exitoso sin errores de TypeScript

## Risks
- `sessionStorage` se elimina al cerrar pestaña, pero no al cerrar navegador en móvil (comportamiento varía por plataforma)
- Cooldown de 15s puede ser frustrante si el adulto introduce PIN incorrecto por error (pero es adecuado para contexto monofamiliar)
- Tarjetas de acceso rápido pueden confundir si no son navegables (pero en este sprint solo son visuales, navegación en SPRINT-015)
- Texto placeholder puede filtrarse a producción si no se marca claramente (usar prefijo "PLACEHOLDER:")
- Modal puede no cerrar correctamente si hay error de navegación tras PIN correcto (mitigación: cerrar modal antes de navegar)
- Icono de configuración en Home puede no ser suficientemente visible para adultos (mitigación: tamaño adecuado, `aria-label` claro)
- Chunk size de HomeView puede aumentar al integrar modal (monitorear, ya era 677 kB en SPRINT-013)

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/login.yaml` (POST /auth/login)
  - `docs/contracts/api/openapi/schemas/auth/login-request.yaml` (request body: pin)
  - `docs/contracts/api/openapi/schemas/auth/login-response.yaml` (response: token, sessionId, familyId, createAt)
- **Componentes base:**
  - `NubiInfoModal` (base del modal, ya existe en components/base/)
  - `NubiPinInput` (para autenticación PIN, ya existe en components/base/)
  - `NubiButton` (para botones de acción, ya existe)
  - `NubiIcon` (para iconos en tarjetas y botón configuración, ya existe)
- **Vistas existentes:**
  - `HomeView.vue` (para integrar modal e icono de configuración, ya existe)
- **Stores:**
  - `useSessionStore` (para mantener estado de familia, ya existe)
- **Servicios:**
  - `apiClient` (para llamadas HTTP, ya existe en services/api.ts)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - POST /auth/login debe estar implementado y funcional
  - Confirmar que POST /auth/login devuelve `{ token, sessionId, familyId, createAt }`

## Agent Instruction
- **Reutilizar** patrón de SPRINT-013 (ChildSelectionModal con vista pin-verification) para consistencia
- **Usar** `NubiInfoModal` como base del modal de autenticación
- **NO crear** vista independiente `ParentalAuthView.vue` ni ruta `/parental-auth`
- **Integrar** `ParentalAuthModal` en `HomeView.vue` con estado reactivo `showParentalAuthModal`
- **Usar** `NubiPinInput` con `masked=true` y teclado numérico estilo móvil
- **Almacenar** token en `sessionStorage` (no en `localStorage` por seguridad básica)
- **No implementar** navegación de tarjetas en este sprint (solo visuales, SPRINT-015)
- **Marcar** texto placeholder con prefijo "PLACEHOLDER:" para evitar filtrado a producción
- **Implementar** cooldown de 15s tras 3 intentos fallidos (no 30s como en análisis inicial)
- **Consumir** contratos API definidos en `docs/contracts/api/openapi/`
- **Manejar** errores HTTP de forma genérica sin revelar información sensible
- **Nunca** mostrar el PIN en logs, consola o mensajes de error
- **Mantener** separación entre experiencia infantil y controles parentales
- **Desactivar** `closeOnOverlay` en modal durante verificación para prevenir cierre accidental
- **Cerrar** modal antes de navegar a `/panel` tras PIN correcto
- **Validar** responsive en portrait y landscape con media queries CSS
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Modal sobre Home:** La autenticación PIN se realiza mediante un modal (`ParentalAuthModal`) que se abre sobre Home al pulsar el icono de configuración. No se crea una vista independiente ni ruta `/parental-auth`. Esto sigue el patrón de SPRINT-013 con `ChildSelectionModal` y evita sobrecargar la navegación con vistas adicionales.
- **Patrón de SPRINT-013:** `ChildSelectionModal` ya implementó vista de verificación PIN interna (`pin-verification`). Se reutiliza este patrón: modal con vistas internas, transiciones suaves, `closeOnOverlay` desactivado durante verificación, foco automático.
- **Autenticación parental:** El PIN se valida contra backend con POST /auth/login. Si backend responde 200, el PIN es correcto y se devuelve token. Si responde 401, el PIN es incorrecto. Frontend no almacena el PIN ni lo muestra en logs.
- **Token de sesión:** Se almacena en `sessionStorage` para sobrevivir recargas de pestaña, pero se elimina al cerrar pestaña. Esto es adecuado para contexto monofamiliar donde el adulto no quiere sesión persistente.
- **Cooldown de 15s:** Tras 3 intentos fallidos consecutivos, se bloquea la entrada 15 segundos. Esto previene fuerza bruta básica pero no es excesivamente restrictivo para contexto monofamiliar.
- **Portada neutral:** No muestra datos infantiles, progreso, avisos evaluativos ni destaca secciones. Solo texto placeholder y tarjetas visuales (no navegables en este sprint).
- **Tarjetas agrupadas:** Panel (Configuración, Niños, Chatbot, Documentación) / Experiencias (Lectura familiar, Relajación familiar). Cada tarjeta tiene icono + etiqueta. En este sprint solo son visuales; la navegación se implementa en SPRINT-015.
- **Guard de navegación:** `requiresParentalAuth` protege `/panel` y sub-rutas. Si `!isAuthenticated`, redirige a Home (no a vista independiente). Home puede abrir automáticamente el modal si se pasa query param `?parentalAuth=true` (opcional, para mejorar UX si el adulto intenta acceder directamente a `/panel`).
- **Seguridad PIN:** El PIN es un dato sensible de control parental. Nunca debe mostrarse como texto legible durante su introducción ni volver a mostrarse después. El tratamiento técnico (hash, almacenamiento) es responsabilidad de backend.
- **Responsive:** Modal adaptable en portrait y landscape. En móvil (<768px), tarjetas en columna única. En tablet/desktop (≥768px), tarjetas en grid 2 columnas.
- **Errores de API:** Backend puede devolver mensajes de error en `error.details.message` (400 Bad Request). Frontend debe mostrar estos mensajes si están disponibles, o mensajes genéricos si no lo están.
- **Chunk size:** HomeView ya era 677 kB en SPRINT-013. Al integrar `ParentalAuthModal`, el chunk puede aumentar. Monitorear y considerar code-splitting si supera 800 kB.

## Review

### Implementación completada: 2026-07-29

#### Archivos creados
- `app/src/stores/parentalAuth.ts` — Store Pinia con estado de autenticación parental, intentos, cooldown y persistencia en sessionStorage
- `app/src/composables/useParentalSession.ts` — Composable con métodos `login(pin)` y `logout()`, consumo de contratos `/auth/login` y `/auth/logout`
- `app/src/components/home/ParentalAuthModal.vue` — Modal de autenticación PIN sobre Home, reutiliza NubiInfoModal + NubiPinInput (masked), cooldown 15s tras 3 intentos
- `app/src/views/PanelCoverView.vue` — Portada neutral con título, texto placeholder y tarjetas agrupadas (Panel / Experiencias)

#### Archivos modificados
- `app/src/services/api.ts` — `apiClient.post` y `apiClient.delete` ahora aceptan headers personalizados (para Authorization Bearer)
- `app/src/components/home/HomeHeader.vue` — Botón configuración emite `openParentalAuth` en lugar de navegar directamente a /panel
- `app/src/views/HomeView.vue` — Integra `ParentalAuthModal` con estado reactivo `showParentalAuthModal`, escucha evento de HomeHeader
- `app/src/router/index.ts` — Ruta `/panel` renombrada a `PanelCover` con componente `PanelCoverView.vue`, guard actualizado a `requiresParentalAuth` usando `useParentalAuthStore`
- `app/src/i18n/locales/es.ts` — Añadidas secciones `modals.parentalAuth.*` y `views.panelCover.*`

#### Contratos consumidos
- `docs/contracts/api/openapi/paths/login.yaml` — POST /api/v1/auth/login con `{ pin: string }`, respuesta con token, sessionId, familyId, createAt
- `docs/contracts/api/openapi/paths/logout.yaml` — POST /api/v1/auth/logout con header Authorization: Bearer <token>

#### Evidencias de build
- `tsc --noEmit`: 0 errores TypeScript
- `vite build`: éxito en 536ms, 1898 módulos transformados
- HomeView chunk: 43.83 kB (bien dentro del límite de 800 kB)
- PanelCoverView chunk: 2.06 kB (code-splitting automático)

#### Decisiones técnicas
- **Store separado**: Se crea `useParentalAuthStore` independiente del `useSessionStore` existente, como indica el sprint. El guard de navegación usa `useParentalAuthStore.isAuthenticated`
- **Logout resiliente**: Si la llamada POST /auth/logout falla (red caída), se limpia el estado local igualmente para no bloquear al usuario
- **Cooldown con timer automático**: El store inicia un `setInterval` que resetea los intentos al expirar el cooldown de 15s. El modal muestra cuenta regresiva visual
- **Tarjetas no navegables**: Se renderizan como divs con `role="button"` y `tabindex="0"` pero sin handler de navegación (SPRINT-015)
- **Query param `?parentalAuth=true`**: No implementado (marcado como opcional en el sprint). Se puede añadir en SPRINT-015 si se considera necesario

#### Deuda técnica / Riesgos
- `PanelControlView.vue` antiguo permanece en el proyecto sin uso; puede eliminarse en limpieza posterior
- NubiIcon chunk (637 kB) sigue siendo el mayor; no relacionado con este sprint
- No se han creado pruebas unitarias (no hay framework de testing configurado en el proyecto)
- El query param `?parentalAuth=true` para auto-abrir el modal no está implementado (opcional)

### Validación técnica completada: 2026-07-29

#### Verificación de criterios de aceptación
Todos los 25 criterios de aceptación han sido verificados y cumplen correctamente:

1. ✅ Icono de configuración visible en Home con `aria-label` accesible (HomeHeader.vue:20)
2. ✅ Pulsar icono de configuración abre `ParentalAuthModal` sobre Home (HomeHeader.vue:73-75, HomeView.vue:7)
3. ✅ `ParentalAuthModal` muestra vista de autenticación con `NubiPinInput` (masked, 4 dígitos) (ParentalAuthModal.vue:14-21)
4. ✅ Botón "Entrar" está deshabilitado hasta completar 4 dígitos (ParentalAuthModal.vue:58)
5. ✅ Botón "Cancelar" cierra modal sin validar PIN y permanece en Home (ParentalAuthModal.vue:53, 129-133)
6. ✅ `POST /auth/login` se invoca con `{ pin: string }` al pulsar "Entrar" (useParentalSession.ts:24)
7. ✅ Si PIN correcto (200 OK): token se almacena en `sessionStorage`, `isAuthenticated = true`, modal se cierra, redirige a `/panel` (parentalAuth.ts:20-25, ParentalAuthModal.vue:149-153)
8. ✅ Si PIN incorrecto (401): mensaje "PIN incorrecto. Inténtalo de nuevo." y permite reintentar (useParentalSession.ts:40-41, es.ts:179)
9. ✅ Tras 3 intentos fallidos consecutivos: entrada bloqueada 15 segundos (parentalAuth.ts:34-39)
10. ✅ Timer de cooldown visible (cuenta regresiva 15s) (ParentalAuthModal.vue:32-39, 166-177)
11. ✅ Tras cooldown, contador de intentos se resetea y permite nuevos intentos (parentalAuth.ts:48-55)
12. ✅ Si conexión falla (0): mensaje "Sin conexión. Revisa tu red." (useParentalSession.ts:37-38, es.ts:180)
13. ✅ Si servidor falla (500): mensaje "Error al iniciar sesión. Inténtalo de nuevo." (useParentalSession.ts:43, es.ts:181)
14. ✅ `PanelCoverView` muestra título "Panel parental" y texto placeholder (PanelCoverView.vue:3-4, es.ts:269-270)
15. ✅ Portada neutral: sin datos infantiles, sin progreso, sin avisos evaluativos (PanelCoverView.vue)
16. ✅ Tarjetas de acceso rápido agrupadas: Panel (Configuración, Niños, Chatbot, Documentación) / Experiencias (Lectura familiar, Relajación familiar) (PanelCoverView.vue:56-66)
17. ✅ Cada tarjeta tiene etiqueta de texto obligatoria + icono opcional (PanelCoverView.vue:18-19)
18. ✅ Tarjetas no son navegables en este sprint (solo visuales) (PanelCoverView.vue: role="button" y tabindex="0" sin handler)
19. ✅ Guard `requiresParentalAuth` protege `/panel`: si `!isAuthenticated`, redirige a Home (router/index.ts:264-267)
20. ✅ Token se elimina de `sessionStorage` al cerrar pestaña (comportamiento nativo de `sessionStorage`) (parentalAuth.ts:24)
21. ✅ `closeOnOverlay` desactivado en `ParentalAuthModal` durante verificación (ParentalAuthModal.vue:5)
22. ✅ i18n completo: sin literales en templates (todos los textos usan `t()`)
23. ✅ Accesibilidad táctil mínimo 48x48dp en botones y tarjetas (HomeHeader.vue:110-111, PanelCoverView.vue:126)
24. ✅ Responsive en móvil y tablet (portrait y landscape) (PanelCoverView.vue:146-171)
25. ✅ Build exitoso sin errores de TypeScript (verificado: tsc 0 errores, vite build éxito)

#### Verificación de contratos
- ✅ POST /api/v1/auth/login: frontend consume correctamente con `{ pin: string }` y espera respuesta con `token`, `sessionId`, `familyId`, `createAt`
- ✅ POST /api/v1/auth/logout: frontend consume correctamente con header `Authorization: Bearer <token>`
- ✅ Backend confirma ambos endpoints implementados (AuthController.java:33-53)

#### Verificación de accesibilidad
- ✅ `aria-live` para mensajes de error (ParentalAuthModal.vue:27, 36, 45)
- ✅ `aria-invalid` gestionado por `NubiPinInput` tras error
- ✅ Foco automático en primer dígito del PIN al abrir modal (ParentalAuthModal.vue:200-211)
- ✅ Objetivo táctil mínimo 48x48dp en botones y tarjetas
- ✅ `aria-label` en icono de configuración de Home (HomeHeader.vue:20)

#### Verificación de i18n
- ✅ `modals.parentalAuth.*`: título, descripción, entrar, errorInvalid, errorConnection, errorServer, cooldown, attemptsRemaining (es.ts:175-184)
- ✅ `views.panelCover.*`: título, descripción placeholder, grupos (panel, experiencias), secciones (settings, children, chatbot, documentation, readingFamily, relaxationFamily) (es.ts:268-283)

#### Observaciones
- **Discrepancia menor en documentación**: El sprint declara archivos en `framework/frontend/src/` pero la estructura real es `framework/frontend/app/src/`. Los archivos existen y son correctos.
- **Logout usa POST**: El contrato logout.yaml no especifica método HTTP, pero el backend implementa POST /logout y el frontend lo consume correctamente. Esta es la convención estándar para logout.
- **Texto placeholder marcado**: El texto de PanelCoverView incluye prefijo "PLACEHOLDER:" como se especificó, evitando filtrado accidental a producción.

#### Veredicto
**APPROVED** — Sprint completo y verificado. Todos los criterios de aceptación cumplen correctamente. Build exitoso sin errores. Contratos consumidos conforme a especificación. Accesibilidad e i18n completos. Portada neutral implementada correctamente con tarjetas visuales no navegables (pendiente de navegación en SPRINT-015).
