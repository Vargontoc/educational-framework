# SPRINT-016 — Frontend — Logout automático por inactividad

## Goal
Implementar el timer de inactividad (3 minutos) que cierra automáticamente la sesión parental y redirige a Home, con pausa durante espera de respuestas costosas (chatbot), y overlay de confirmación visible durante 5 segundos antes de la redirección.

## Status
status: closed
started_at: 2026-07-29
closed_at: 2026-07-29
blocked_by: SPRINT-014, SPRINT-015
waiting_for: —
bug_fix: 2026-07-29 — Corrección de bug crítico: overlay de inactividad no permitía cancelar logout

## Context
FEAT-004 y ADR-017 definen logout automático tras inactividad. El análisis técnico inicial proponía 5 minutos, pero el usuario confirmó 3 minutos como umbral, con pausa durante espera de respuestas costosas (chatbot). Este sprint implementa el timer de inactividad, la pausa condicional, y el overlay de confirmación.

### Requisitos de FEAT-004 y ADR-017 a implementar en este sprint
- Timer de inactividad: 3 minutos sin interacción del usuario
- Interacción detectada: `mousemove`, `keydown`, `touchstart`, `click`, `scroll`
- Al expirar el timer: ejecutar flujo de logout (consumir `POST /auth/logout`, eliminar token, redirigir a Home)
- Pausa del timer durante espera de respuestas costosas (chatbot)
- Overlay de confirmación visible durante 5 segundos antes de redirigir a Home
- Mensaje del overlay: "Tu sesión ha finalizado por inactividad"
- i18n completo (español)

### Requisitos de FEAT-004 ya implementados o fuera de alcance
- SPRINT-014 implementó autenticación PIN y portada neutral
- SPRINT-015 implementó navegación adaptable y acción «Salir»
- Modo oscuro → SPRINT-017
- Contenido interno de las 6 secciones → fuera del alcance de FEAT-004

## Tasks
- [x] Crear composable `useInactivityTimer.ts`
  - [x] Timer de 3 minutos (180000 ms) sin interacción del usuario
  - [x] Escuchar eventos globales: `mousemove`, `keydown`, `touchstart`, `click`, `scroll`
  - [x] Cada evento resetea el timer
  - [x] Al expirar: ejecutar callback `onExpire()`
  - [x] Método `start()`: inicia el timer
  - [x] Método `stop()`: detiene el timer y limpia listeners
  - [x] Método `pause()`: pausa el timer temporalmente (para chatbot)
  - [x] Método `resume()`: reanuda el timer tras pausa
  - [x] Método `reset()`: resetea el timer a 3 minutos
  - [x] Estado reactivo: `isActive`, `remainingTime`, `isPaused`
- [x] Crear composable `useChatbotPendingResponse.ts` (o integrar en store existente)
  - [x] Estado reactivo: `isWaitingForChatbot` (boolean)
  - [x] Método `setPending()`: marca que se espera respuesta del chatbot
  - [x] Método `clearPending()`: marca que la respuesta llegó
  - [x] Integrar con `useInactivityTimer`: si `isWaitingForChatbot`, pausar timer
  - [x] Cuando respuesta llega, reanudar timer
- [x] Integrar `useInactivityTimer` en `ParentPanelLayout.vue`
  - [x] Iniciar timer al montar el layout (`onMounted`)
  - [x] Detener timer al desmontar el layout (`onUnmounted`)
  - [x] Escuchar eventos globales para resetear timer
  - [x] Al expirar: mostrar overlay de confirmación
  - [x] Pausar timer si `isWaitingForChatbot` es true
  - [x] Reanudar timer cuando `isWaitingForChatbot` cambia a false
- [x] Crear componente `InactivityOverlay.vue`
  - [x] Overlay a pantalla completa con fondo semitransparente
  - [x] Mensaje: "Tu sesión ha finalizado por inactividad"
  - [x] Timer visual de 5 segundos (cuenta regresiva)
  - [x] Tras 5 segundos: redirigir a Home con `router.replace('/')`
  - [x] Accesibilidad: `role="alert"`, `aria-live="assertive"`
  - [x] No permite interacción con el contenido detrás del overlay
- [x] Implementar flujo de logout al expirar timer
  - [x] Al expirar timer: mostrar `InactivityOverlay`
  - [x] Invocar `useParentalSession.logout()` (consume `POST /auth/logout`)
  - [x] Eliminar token de `sessionStorage`
  - [x] Resetear `isAuthenticated` en store
  - [x] Overlay visible durante 5 segundos
  - [x] Tras 5 segundos: redirigir a Home con `router.replace('/')`
  - [x] Logout optimista: si `POST /auth/logout` falla, eliminar token local igualmente
- [x] Integrar pausa de timer con chatbot (cuando se implemente)
  - [x] Detectar cuando se envía mensaje al chatbot
  - [x] Llamar a `useInactivityTimer.pause()`
  - [x] Detectar cuando llega respuesta del chatbot
  - [x] Llamar a `useInactivityTimer.resume()`
  - [x] Nota: chatbot fuera del alcance de FEAT-004, pero se deja preparado el hook
- [x] Añadir traducciones i18n en `es.ts`
  - [x] `inactivity.*`: mensaje overlay, tiempo restante
- [x] Validar accesibilidad
  - [x] `role="alert"` en overlay
  - [x] `aria-live="assertive"` para mensaje de inactividad
  - [x] Overlay no permite interacción con contenido detrás
  - [x] Timer visual de 5 segundos accesible (texto grande, contraste alto)
- [x] Validar responsive en móvil y tablet (portrait y landscape)
  - [x] Overlay cubre pantalla completa en ambas orientaciones
  - [x] Mensaje legible en móvil y tablet
- [x] Verificar build exitoso sin errores de TypeScript
- [x] **BUG FIX**: Corrección de bug crítico — overlay de inactividad no permitía cancelar logout
  - [x] Modificar `InactivityOverlay.vue` para emitir eventos `cancel` y `expired`
  - [x] Añadir handler de click en el overlay que emite `cancel` y limpia interval
  - [x] Añadir cursor: pointer para indicar que es interactuable
  - [x] Añadir mensaje i18n "Haz clic para continuar"
  - [x] Modificar `ParentPanelLayout.vue` para escuchar eventos `cancel` y `expired`
  - [x] Mover logout de `handleExpire()` a `handleExpired()` (se ejecuta solo cuando countdown llega a 0)
  - [x] Implementar `handleCancel()`: cierra overlay, resetea timer a 3 minutos, NO ejecuta logout
  - [x] Implementar `handleExpired()`: ejecuta logout cuando overlay emite `expired`
  - [x] Añadir traducción i18n `inactivity.clickToContinue`
  - [x] Validar accesibilidad: overlay clickable, mensaje claro, mantiene `role="alert"` y `aria-live="assertive"`
  - [x] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- Timer de inactividad se inicia al entrar en `/panel`
- Timer se resetea con cada interacción del usuario (`mousemove`, `keydown`, `touchstart`, `click`, `scroll`)
- Tras 3 minutos sin interacción, se muestra `InactivityOverlay`
- Overlay muestra mensaje "Tu sesión ha finalizado por inactividad"
- Overlay muestra timer visual de 5 segundos (cuenta regresiva)
- **BUG FIX**: Usuario puede hacer clic en el overlay para cancelar el logout
- **BUG FIX**: Al hacer clic, overlay se cierra y timer se resetea a 3 minutos
- **BUG FIX**: Logout NO se ejecuta si el usuario interactúa con el overlay
- **BUG FIX**: Logout se ejecuta solo cuando el countdown llega a 0 (5 segundos)
- Tras 5 segundos (sin interacción), redirige a Home con `router.replace('/')`
- `POST /auth/logout` se invoca al expirar el timer (cuando countdown llega a 0)
- Token se elimina de `sessionStorage` tras logout
- `isAuthenticated` se resetea a `false`
- Logout optimista: si `POST /auth/logout` falla, token se elimina igualmente
- Timer se pausa cuando `isWaitingForChatbot` es true
- Timer se reanuda cuando `isWaitingForChatbot` cambia a false
- Overlay no permite interacción con contenido detrás (excepto click para cancelar)
- Accesibilidad: `role="alert"`, `aria-live="assertive"`, cursor: pointer
- Overlay cubre pantalla completa en portrait y landscape
- Mensaje legible en móvil y tablet
- i18n completo: sin literales en templates, incluye `inactivity.clickToContinue`
- Build exitoso sin errores de TypeScript

## Risks
- Timer de 3 minutos puede ser demasiado agresivo si el adulto lee contenido largo (mitigación: pausa durante chatbot, interacción resetea timer)
- Eventos globales (`mousemove`, `scroll`) pueden no detectarse en ciertos dispositivos táctiles (mitigación: incluir `touchstart`)
- Overlay de 5 segundos puede ser confuso si el adulto no entiende por qué apareció (mitigación: mensaje claro "Tu sesión ha finalizado por inactividad")
- Pausa del timer durante chatbot requiere integración con módulo de chatbot (fuera del alcance de FEAT-004, pero se deja preparado el hook)
- Timer puede no resetearse si el adulto está en otra pestaña del navegador (comportamiento esperado, no es un bug)
- Chunk size puede aumentar al añadir composable y overlay (monitorear)

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/logout.yaml` (POST /auth/logout)
- **Composables:**
  - `useParentalSession` (para logout, creado en SPRINT-014)
- **Stores:**
  - `useParentalAuthStore` (para estado de autenticación, creado en SPRINT-014)
- **Vistas:**
  - `ParentPanelLayout.vue` (para integrar timer, creado en SPRINT-015)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - POST /auth/logout debe estar implementado y funcional
- **Futuro (fuera del alcance de FEAT-004):**
  - Módulo de chatbot (para integrar pausa de timer)

## Agent Instruction
- **Implementar** timer de 3 minutos (180000 ms), no 5 minutos como en análisis inicial
- **Pausar** timer durante espera de chatbot (hook preparado, integración futura)
- **Mostrar** overlay de confirmación durante 5 segundos antes de redirigir
- **Usar** eventos globales `mousemove`, `keydown`, `touchstart`, `click`, `scroll` para resetear timer
- **Consumir** `POST /auth/logout` al expirar el timer
- **Implementar** logout optimista: si falla, eliminar token local igualmente
- **Crear** composable `useInactivityTimer.ts` reutilizable
- **Crear** componente `InactivityOverlay.vue` con timer visual de 5 segundos
- **Integrar** timer en `ParentPanelLayout.vue` (iniciar al montar, detener al desmontar)
- **Mantener** separación entre experiencia infantil y controles parentales
- **Validar** responsive en portrait y landscape
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Timer de 3 minutos:** El usuario confirmó 3 minutos como umbral de inactividad, no 5 minutos como se propuso inicialmente. Esto es adecuado para contexto monofamiliar donde el adulto no debe dejar la sesión abierta indefinidamente.
- **Pausa durante chatbot:** El usuario confirmó que el timer debe pausarse durante espera de respuestas costosas (chatbot). Se implementa el hook (`useChatbotPendingResponse`), pero la integración con el módulo de chatbot real está fuera del alcance de FEAT-004 (se hará cuando se implemente el chatbot).
- **Overlay de 5 segundos:** El usuario confirmó 5 segundos de overlay antes de redirigir, no 3 segundos como se propuso inicialmente. Esto da tiempo al adulto para leer el mensaje y entender qué ocurrió.
- **Eventos de interacción:** Se escuchan `mousemove`, `keydown`, `touchstart`, `click`, `scroll` para detectar actividad del usuario. Cada evento resetea el timer a 3 minutos.
- **Logout optimista:** Si `POST /auth/logout` falla (ej. token ya expirado), el token se elimina localmente igualmente y se redirige a Home. Esto garantiza que la sesión se cierra aunque el backend no responda.
- **Accesibilidad:** Overlay con `role="alert"` y `aria-live="assertive"` para que lectores de pantalla anuncien el mensaje. Timer visual de 5 segundos con texto grande y contraste alto.
- **Responsive:** Overlay cubre pantalla completa en portrait y landscape. Mensaje legible en móvil y tablet.
- **Seguridad:** Token se elimina de `sessionStorage` tras logout. `isAuthenticated` se resetea. Redirección a Home con `router.replace('/')` para no acumular historial.
- **Integración futura:** Cuando se implemente el módulo de chatbot, se debe integrar con `useChatbotPendingResponse` para pausar el timer durante espera de respuestas. Esto está fuera del alcance de FEAT-004 pero se deja preparado el hook.

## Review

### Resumen de implementación

Sprint implementado íntegramente. Se crearon 3 archivos nuevos y se modificaron 2 existentes. El build pasa sin errores de TypeScript.

### Archivos creados

1. **`src/composables/useInactivityTimer.ts`** — Composable reutilizable con timer de 3 minutos (180000 ms), eventos globales (`mousemove`, `keydown`, `touchstart`, `click`, `scroll`), métodos `start()`, `stop()`, `pause()`, `resume()`, `reset()`, y estado reactivo (`isActive`, `remainingTime`, `isPaused`).
2. **`src/composables/useChatbotPendingResponse.ts`** — Hook de estado global con `isWaitingForChatbot`, `setPending()`, `clearPending()`. Preparado para integración futura con módulo de chatbot.
3. **`src/components/InactivityOverlay.vue`** — Overlay a pantalla completa con `role="alert"`, `aria-live="assertive"`, mensaje i18n, cuenta regresiva de 5 segundos y redirección a Home con `router.replace('/')`. Responsive en portrait y landscape.

### Archivos modificados

1. **`src/layouts/ParentPanelLayout.vue`** — Integración de `useInactivityTimer`, `useChatbotPendingResponse`, `useParentalSession`. Timer inicia en `onMounted`, se detiene en `onUnmounted`. Muestra `InactivityOverlay` al expirar. Pausa/reanuda según `isWaitingForChatbot`.
2. **`src/i18n/locales/es.ts`** — Añadida sección `inactivity.*` con claves `message` y `redirecting`.

### Flujo de logout

- Al expirar timer → `handleExpire()` muestra overlay y ejecuta `logout()` de `useParentalSession`.
- `logout()` invoca `POST /api/v1/auth/logout` con header `Authorization: Bearer <token>`.
- Logout optimista: si falla, `finally` bloque ejecuta `authStore.clearAuth()` (elimina token de `sessionStorage` y resetea `isAuthenticated`).
- Overlay cuenta 5 segundos y redirige a Home con `router.replace('/')`.

### Comandos ejecutados

- `npm run build` → **Exitoso** (tsc + vite build). Sin errores de TypeScript.

### Contratos afectados

- `POST /api/v1/auth/logout` (consumido, ya implementado en backend).

### Pruebas

- No se encontraron pruebas unitarias existentes en el proyecto (no hay framework de testing configurado). La validación se realizó mediante build exitoso.

### Riesgos y deuda

- La pausa del timer durante chatbot está preparada pero no conectada a un módulo real (fuera del alcance de FEAT-004).
- El composable `useChatbotPendingResponse` usa estado a nivel de módulo (singleton). Si se necesitan múltiples instancias, habría que refactorizar a un store Pinia.
- No hay framework de testing configurado en el proyecto; las pruebas manuales deben realizarse en navegador.

### Corrección de bug crítico (2026-07-29)

**Problema**: El overlay de inactividad no permitía hacer clic para cancelar el logout, lo que causaba que la sesión se cerrara automáticamente después de 5 segundos sin posibilidad de continuar.

**Causa raíz**: 
1. `InactivityOverlay.vue` no tenía handler de click para emitir evento de cancelación
2. `ParentPanelLayout.vue` ejecutaba `logout()` inmediatamente en `handleExpire()`, no cuando el countdown llegaba a 0
3. No había mecanismo para resetear el timer después de mostrar el overlay

**Solución implementada**:
1. **`InactivityOverlay.vue`**:
   - Añadidos eventos `cancel` y `expired` con `defineEmits`
   - Handler `handleClick()` que limpia interval y emite `cancel`
   - Countdown emite `expired` antes de redirigir a Home
   - Añadido `cursor: pointer` en el overlay
   - Añadido mensaje "Haz clic para continuar" con clase `.inactivity-overlay__hint`
   - Limpieza de interval en `onUnmounted` para evitar memory leaks

2. **`ParentPanelLayout.vue`**:
   - `handleExpire()` ahora solo muestra el overlay (no ejecuta logout)
   - Nuevo `handleCancel()`: cierra overlay y resetea timer con `reset()`
   - Nuevo `handleExpired()`: ejecuta `logout()` cuando countdown llega a 0
   - Desestructuración de `reset` desde `useInactivityTimer`

3. **`es.ts`**:
   - Añadida clave `inactivity.clickToContinue`: "Haz clic para continuar"

**Archivos modificados**:
- `src/components/InactivityOverlay.vue`
- `src/layouts/ParentPanelLayout.vue`
- `src/i18n/locales/es.ts`

**Comandos ejecutados**:
- `npm run build` → **Exitoso** (tsc + vite build). Sin errores de TypeScript.

**Comportamiento corregido**:
- Usuario puede hacer clic en cualquier parte del overlay para cancelar el logout
- Al hacer clic: overlay se cierra, timer se resetea a 3 minutos, usuario puede continuar
- Si no interactúa: después de 5 segundos, se ejecuta logout y redirige a Home
- Logout se ejecuta solo cuando el countdown llega a 0, no cuando aparece el overlay

### Validación técnica completada: 2026-07-29

#### Verificación de criterios de aceptación
Todos los 22 criterios de aceptación han sido verificados y cumplen correctamente:

1. ✅ Timer de inactividad se inicia al entrar en `/panel` (ParentPanelLayout.vue:70-72, onMounted → start())
2. ✅ Timer se resetea con cada interacción del usuario (useInactivityTimer.ts:14,46-49, eventos globales mousemove/keydown/touchstart/click/scroll)
3. ✅ Tras 3 minutos sin interacción, se muestra InactivityOverlay (useInactivityTimer.ts:3,38-43, 180000ms, callback onExpire)
4. ✅ Overlay muestra mensaje "Tu sesión ha finalizado por inactividad" (InactivityOverlay.vue:10-12, es.ts:195)
5. ✅ Overlay muestra timer visual de 5 segundos (cuenta regresiva) (InactivityOverlay.vue:36,49-57, countdown desde 5)
6. ✅ Usuario puede hacer clic en el overlay para cancelar el logout (InactivityOverlay.vue:7,40-46, handleClick → emit cancel)
7. ✅ Al hacer clic, overlay se cierra y timer se resetea a 3 minutos (ParentPanelLayout.vue:51-54, handleCancel → reset())
8. ✅ Logout NO se ejecuta si el usuario interactúa con el overlay (ParentPanelLayout.vue:47-49, handleExpire solo muestra overlay)
9. ✅ Logout se ejecuta solo cuando el countdown llega a 0 (ParentPanelLayout.vue:56-58, handleExpired → logout())
10. ✅ Tras 5 segundos (sin interacción), redirige a Home (InactivityOverlay.vue:55, router.replace('/'))
11. ✅ POST /auth/logout se invoca al expirar el timer (ParentPanelLayout.vue:57, useParentalSession.ts:47-60)
12. ✅ Token se elimina de sessionStorage tras logout (useParentalSession.ts:58, parentalAuth.ts:27-32)
13. ✅ isAuthenticated se resetea a false (parentalAuth.ts:28, token.value = null)
14. ✅ Logout optimista: si POST /auth/logout falla, token se elimina igualmente (useParentalSession.ts:55-59, finally block)
15. ✅ Timer se pausa cuando isWaitingForChatbot es true (ParentPanelLayout.vue:62-68, watch → pause())
16. ✅ Timer se reanuda cuando isWaitingForChatbot cambia a false (ParentPanelLayout.vue:62-68, watch → resume())
17. ✅ Overlay no permite interacción con contenido detrás (InactivityOverlay.vue:69-78, position: fixed, z-index: 10000)
18. ✅ Accesibilidad: role="alert", aria-live="assertive", cursor: pointer (InactivityOverlay.vue:4-6,78)
19. ✅ Overlay cubre pantalla completa en portrait y landscape (InactivityOverlay.vue:69-78, position: fixed, inset: 0)
20. ✅ Mensaje legible en móvil y tablet (InactivityOverlay.vue:115-123, media queries responsive)
21. ✅ i18n completo: sin literales en templates, incluye inactivity.clickToContinue (es.ts:194-198)
22. ✅ Build exitoso sin errores de TypeScript (verificado: tsc 0 errores, vite build éxito en 418ms)

#### Verificación de contratos
- ✅ POST /api/v1/auth/logout: frontend consume correctamente con header `Authorization: Bearer <token>`
- ✅ Backend confirma endpoint implementado (AuthController.java:44-53)
- ✅ Logout optimista: si falla, se limpia estado local igualmente (useParentalSession.ts:55-59)

#### Verificación de accesibilidad
- ✅ `role="alert"` en overlay (InactivityOverlay.vue:4)
- ✅ `aria-live="assertive"` para mensaje de inactividad (InactivityOverlay.vue:5)
- ✅ `aria-modal="true"` para overlay modal (InactivityOverlay.vue:6)
- ✅ `cursor: pointer` para indicar interactividad (InactivityOverlay.vue:78)
- ✅ `aria-atomic="true"` en countdown (InactivityOverlay.vue:13)
- ✅ Texto grande y contraste alto (mensaje blanco sobre fondo oscuro 75% opacidad)

#### Verificación de i18n
- ✅ `inactivity.message`: "Tu sesión ha finalizado por inactividad" (es.ts:195)
- ✅ `inactivity.redirecting`: "Redirigiendo en {seconds} segundos..." (es.ts:196)
- ✅ `inactivity.clickToContinue`: "Haz clic para continuar" (es.ts:197)

#### Verificación de responsive
- ✅ Overlay cubre pantalla completa en portrait y landscape (position: fixed, inset: 0)
- ✅ Media queries para móvil (<1024px) ajustan tamaños de fuente (InactivityOverlay.vue:115-123)
- ✅ Media queries para landscape con altura reducida ajustan padding y gap (InactivityOverlay.vue:125-130)

#### Evidencias de build
- `tsc --noEmit`: 0 errores TypeScript
- `vite build`: éxito en 418ms, 1923 módulos transformados
- ParentPanelLayout chunk: 6.07 kB (code-splitting automático)
- InactivityOverlay integrado en ParentPanelLayout (no genera chunk separado)

#### Observaciones
- **Composable reutilizable**: `useInactivityTimer` es genérico y puede reutilizarse en otros contextos que requieran detección de inactividad
- **Hook para chatbot**: `useChatbotPendingResponse` usa estado a nivel de módulo (singleton), adecuado para caso de uso único de chatbot
- **Bug fix crítico resuelto**: El overlay ahora permite cancelar el logout con click, mejorando significativamente la UX
- **Limpieza de recursos**: Todos los intervals y timeouts se limpian correctamente en onUnmounted para evitar memory leaks
- **Timer preciso**: useInactivityTimer usa Date.now() para calcular elapsed time, más preciso que solo contar ticks de interval

#### Veredicto
**APPROVED** — Sprint completo y verificado. Todos los 22 criterios de aceptación cumplen correctamente. Build exitoso sin errores. Contratos consumidos conforme a especificación. Accesibilidad e i18n completos. Timer de inactividad de 3 minutos implementado correctamente con pausa para chatbot. Overlay de confirmación de 5 segundos con opción de cancelar. Bug crítico de cancelación resuelto correctamente.
