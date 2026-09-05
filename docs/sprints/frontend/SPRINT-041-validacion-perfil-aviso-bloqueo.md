# SPRINT-041 — Validación de perfil y aviso de bloqueo

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-05
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-003 (selección de perfiles), ADR-022 (gestión parental de perfiles)
- **Impacto estimado:** Implementa la validación de estado del perfil antes de entrar a GameView y el aviso neutral para perfiles bloqueados, cumpliendo los requisitos 5, 6 y 7 de FEAT-010.

## Objetivo

Implementar la validación de estado del perfil (habilitado/bloqueado) en HomeView antes de navegar a GameView, y mostrar un aviso neutral cuando el perfil está bloqueado, permaneciendo en el selector de perfiles.

## Contexto

FEAT-010 establece que la selección de un perfil bloqueado no debe abrir GameView. En su lugar, debe mostrarse un aviso neutral y la persona debe permanecer en el selector de perfiles. El aviso no debe explicar el motivo del bloqueo, sugerir culpa o castigo, ni mostrar controles parentales.

Actualmente, HomeView permite seleccionar cualquier perfil y navegar a GameView sin validar el estado del perfil. Esta validación debe ocurrir antes de la navegación.

## Diseño funcional-técnico

### 1. Validación de perfil en HomeView

**Flujo propuesto:**

```typescript
// HomeView.vue
async function onSelectProfile(childId: number) {
  // 1. Consultar estado del perfil (implícito en openSession)
  const session = await openSession(childId)
  
  if (!session) {
    // Perfil bloqueado o error → mostrar aviso neutral
    showBlockedProfileNotice()
    return
  }
  
  // 2. Perfil habilitado → navegar a GameView
  sessionStore.selectChild(String(childId))
  router.replace({ name: 'GameView', params: { childId: String(childId) } })
}
```

**Decisión técnica:** La validación se realiza implícitamente en `openSession`. Si el backend devuelve `success: false` o `null`, se interpreta como perfil bloqueado o error.

### 2. Componente de aviso neutral

**Especificaciones:**
- Modal u overlay con icono + mensaje breve
- Accesible: icono + texto, no solo color
- Texto provisional: "Este perfil no puede jugar ahora" (sujeto a validación de producto)
- No muestra causa del bloqueo, ni controles parentales
- Botón de cierre que permanece en HomeView
- Comprensible en móvil y tableta

**Componente propuesto:** `BlockedProfileNotice.vue`

```typescript
// BlockedProfileNotice.vue
<template>
  <div class="blocked-profile-notice" role="alert" aria-live="polite">
    <div class="notice-icon">
      <!-- Icono de candado o pausa -->
      <IconBlocked />
    </div>
    <div class="notice-text">
      {{ $t('game.blockedProfile.notice') }}
    </div>
    <button @click="close" class="notice-close">
      {{ $t('common.close') }}
    </button>
  </div>
</template>
```

### 3. Manejo de respuesta de `openSession`

**Modificación en `sessionService.ts`:**

```typescript
export async function openSession(childId: number): Promise<ChildSession | null> {
  const response = await apiClient.post<ApiChildSession>('/api/v1/sessions/children', {
    childProfileId: childId,
    heartbeatIntervalSeconds: 60
  })

  if (response && response.success && response.data) {
    return response.data
  }
  
  // Si success es false, puede ser perfil bloqueado o error técnico
  // Frontend no distingue entre ambos por seguridad infantil
  return null
}
```

## Contratos y dependencias externas

### Contratos requeridos

**Endpoint:** `POST /api/v1/sessions/children`

**Comportamiento esperado:**
- Si perfil habilitado: devuelve `success: true` con `data: ChildSession`
- Si perfil bloqueado: devuelve `success: false` con `message: "PROFILE_BLOCKED"` o similar

**Handoff a backend:** Confirmar que `openSession` rechaza perfiles bloqueados con error específico.

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Backend | Endpoint `openSession` debe rechazar perfiles bloqueados | Crítico |
| Backend | Estado del perfil (habilitado/bloqueado) | Crítico |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Backend no implementa rechazo de perfiles bloqueados en `openSession` | ALTA | Escalar a backend como dependencia crítica. Frontend puede implementar validación previa si backend provee endpoint de consulta de estado. |
| R2 | Aviso de bloqueo no es suficientemente neutral | MEDIA | Validar texto e iconografía con producto. No mostrar causa, culpa ni controles parentales. |
| R3 | Frontend no puede distinguir entre perfil bloqueado y error técnico | MEDIA | Por diseño, no se distingue. El aviso neutral es el mismo para ambos casos. Esto protege la privacidad infantil. |

---

## Tareas del sprint

### Tarea 41.1: Modificar ChildSelectionModal para validar estado del perfil antes de navegar — **implemented**

**Criterios de aceptación:**
- Al pulsar un perfil habilitado, se navega a `/game/:childId`.
- Al pulsar un perfil bloqueado, se muestra aviso neutral y se permanece en el selector de perfiles.
- La validación se realiza antes de la navegación, no después.

**Nota:** La validación se implementa en `ChildSelectionModal.handleSelectProfile()` (no en HomeView), que es el componente real de selección. Usa `openSession()` como validación dinámica al backend, no el estado local cacheado (`profile.active`). Esto garantiza que si un perfil se bloquea/desbloquea mientras el modal está abierto, la validación refleja el estado actual.

### Tarea 41.2: Crear componente de aviso neutral `BlockedProfileNotice.vue` — **implemented**

**Criterios de aceptación:**
- El aviso muestra icono + texto, no depende solo de color.
- El aviso no muestra causa del bloqueo, ni controles parentales.
- El aviso es comprensible en móvil y tableta.
- El aviso tiene un botón de cierre que permite permanecer en el selector de perfiles.

**Nota de ubicación:** Se ubica en `components/home/` (no `components/game/`) porque se usa exclusivamente dentro del flujo de selección de perfiles en `ChildSelectionModal`.

### Tarea 41.3: Confirmar `sessionService.ts` — **implemented (sin cambios)**

**Criterios de aceptación:**
- `openSession` devuelve `null` cuando el perfil está bloqueado.
- Frontend no distingue entre perfil bloqueado y error técnico (por seguridad infantil).

**Evidencia:** `sessionService.ts` ya devuelve `null` cuando `response.success` es false (líneas 48-59). No se requieren cambios.

### Tarea 41.4: Añadir cadenas i18n para el aviso neutral — **implemented**

**Criterios de aceptación:**
- Ninguna cadena visible como literal en template.
- Cadenas añadidas a `es.ts`.

**Evidencia:** Cadenas añadidas bajo `views.game.blockedProfile` en `es.ts`: `notice`, `close`, `iconLabel`.

### Tarea 41.5: Pruebas unitarias y E2E — **deuda técnica**

**Criterios de aceptación:**
- Prueba unitaria: ChildSelectionModal valida estado del perfil.
- Prueba unitaria: Aviso neutral se muestra para perfil bloqueado.
- Prueba E2E: Flujo completo de selección de perfil habilitado y bloqueado.

**Estado:** No existe framework de tests configurado en el proyecto (ni vitest, ni cypress, ni otro). No se instalarán como parte de este sprint. Registrado como deuda técnica.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/home/ChildSelectionModal.vue` | Modificación |
| `framework/frontend/app/src/components/home/BlockedProfileNotice.vue` | Nuevo |
| `framework/frontend/app/src/services/sessionService.ts` | Sin cambios (ya correcto) |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio (dependencia de backend)

## Criterios de aceptación del sprint

1. Al pulsar un perfil habilitado desde el selector, se muestra GameView con una carga inicial antes del estado visual base. *(FEAT-010 AC1)*
2. Al seleccionar un perfil bloqueado, GameView no se abre. *(FEAT-010 AC5)*
3. Tras seleccionar un perfil bloqueado, se muestra un aviso neutral y la vista continúa siendo el selector de perfiles. *(FEAT-010 AC6)*
4. El aviso de bloqueo no muestra causa, datos parentales, configuración ni lenguaje de castigo. *(FEAT-010 AC7)*
5. Los estados de carga y bloqueo se pueden diferenciar sin depender únicamente de color, texto o sonido y son utilizables en móvil y tableta. *(FEAT-010 AC9)*
6. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] Backend confirma que `POST /api/v1/sessions/children` rechaza perfiles bloqueados.
- [ ] Producto valida texto e iconografía del aviso neutral.

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Backend | Confirmar que `POST /api/v1/sessions/children` rechaza perfiles bloqueados con error específico | Alta |
| Producto | Validar texto e iconografía del aviso neutral | Media |

## Notas adicionales

Este sprint sienta las bases para la validación de acceso a GameView. La validación de perfil bloqueado es crítica para la seguridad infantil (ADR-022). El aviso neutral protege la privacidad del menor al no revelar causas ni controles parentales.

## Evidencia de implementación

- **Fecha:** 2026-09-05
- **Implementado por:** developer-frontend
- **Estado:** implemented (pendiente de review)

### Resumen de cambios

1. **ChildSelectionModal.vue** — `handleSelectProfile()` valida dinámicamente con `openSession(profile.id)` antes de navegar. Si el backend devuelve `null` (perfil bloqueado o error), muestra `BlockedProfileNotice` como overlay dentro del modal de selección.
2. **BlockedProfileNotice.vue** — Nuevo componente con icono `lock` (NubiIcon/@lucide), texto i18n neutral, botón de cierre 44x44px mínimo, `role="alert"`, `aria-live="polite"`, responsive.
3. **sessionService.ts** — Sin cambios. Ya devuelve `null` cuando `success: false`.
4. **es.ts** — Cadenas i18n añadidas bajo `views.game.blockedProfile`.

### Corrección de bug (2026-09-05)

**Problema:** La validación inicial usaba `profile.active` del estado local cacheado al abrir el modal. Si un perfil se bloqueaba/desbloqueaba mientras el modal estaba abierto, la comprobación era obsoleta.

**Solución:** Cambiar a `openSession(profile.id)` como fuente de verdad dinámica. El backend decide en tiempo real si el perfil puede acceder.

### Verificación de tipos

- `npx vue-tsc --noEmit` ejecutado. Sin errores nuevos. Los errores existentes son pre-existentes en archivos `.story.vue` y componentes no relacionados con este sprint.

### Deuda técnica

- **Tarea 41.5 (pruebas):** No existe framework de tests en el proyecto. Registrado como deuda técnica para sprint futuro.

---

## Revisión

- **Fecha:** 2026-09-05
- **Revisado por:** reviewer-frontend
- **Veredicto:** CHANGES_REQUIRED

### Resumen ejecutivo

Implementación funcionalmente correcta con un defecto de manejo de errores que puede dejar al usuario sin feedback ante fallo de red.

### Verificación estática

`npx vue-tsc --noEmit` — **Sin errores nuevos.** Los 24 errores reportados son todos pre-existentes en archivos `.story.vue`, `NubiNumberInput.vue`, `NubiSelect.vue`, `NubiTooltip.vue`, `FamilyRegistrationModal.vue` y `TooltipView.vue`. Ninguno en los archivos del sprint.

### Completitud del sprint

#### Tarea 41.1: Modificar ChildSelectionModal — VERIFICADA

- Criterios verificados:
  - `handleSelectProfile()` (línea 203-217) llama a `openSession(profile.id)` antes de navegar ✓
  - Si `!session` → `showBlockedNotice = true`, sin navegación ✓
  - Si session → `sessionStore.selectChild()` + `router.replace('GameView')` ✓
  - Validación dinámica al backend, no usa `profile.active` cacheado ✓
- Evidencia: `ChildSelectionModal.vue:203-222`
- Incidencias: ver sección Incidencias (MAYOR-1)

#### Tarea 41.2: BlockedProfileNotice.vue — VERIFICADA

- Criterios verificados:
  - Icono `lock` (48px) + texto, no solo color ✓
  - `role="alert"` + `aria-live="polite"` ✓
  - Texto i18n neutral: "Este perfil no puede jugar ahora" ✓
  - Sin causa, sin controles parentales, sin datos del perfil ✓
  - Botón cierre 44x44px mínimo (`min-width/min-height: 44px`) ✓
  - Responsive: media query `max-width: 480px` ajusta padding, icono y texto ✓
  - Overlay posicionado dentro del selector (línea 47-50 del modal) ✓
- Evidencia: `BlockedProfileNotice.vue:1-96`
- Incidencias: ver MENOR-2

#### Tarea 41.3: sessionService.ts — VERIFICADA (sin cambios)

- Criterios verificados:
  - `openSession()` devuelve `null` cuando `!response.success` (líneas 54-58) ✓
  - No distingue entre bloqueado y error técnico ✓
- Evidencia: `sessionService.ts:48-59`
- Incidencias: ninguna

#### Tarea 41.4: Cadenas i18n — VERIFICADA

- Criterios verificados:
  - `views.game.blockedProfile.notice`: "Este perfil no puede jugar ahora" ✓
  - `views.game.blockedProfile.close`: "Cerrar" ✓
  - `views.game.blockedProfile.iconLabel`: "Perfil no disponible" ✓
  - Todas las cadenas del template usan `t()`, sin literales ✓
- Evidencia: `es.ts:352-356`
- Incidencias: ninguna

### Validación de criterios de aceptación

| # | Criterio | Resultado | Evidencia |
|---|----------|-----------|-----------|
| AC1 | Perfil habilitado → GameView con carga inicial | **Cumple** | `ChildSelectionModal.vue:215-216` navega a GameView tras `openSession` exitoso |
| AC5 | Perfil bloqueado → GameView no se abre | **Cumple** | `ChildSelectionModal.vue:210-212` retorna sin navegar si `!session` |
| AC6 | Aviso neutral + permanece en selector | **Cumple** | Overlay dentro de `currentView === 'selection'` (línea 47-50), `handleBlockedNoticeClose` resetea estado |
| AC7 | Aviso sin causa, datos parentales, configuración ni castigo | **Cumple** | Solo icono lock + "Este perfil no puede jugar ahora" |
| AC9 | Diferenciable sin solo color/texto/sonido, usable en móvil/tableta | **Cumple** | Icono + texto + botón 44x44px, media queries responsive |
| vue-tsc | Sin errores nuevos | **Cumple** | Todos los errores son pre-existentes |

### Incidencias encontradas

#### CRÍTICAS

Ninguna

#### MAYORES

**MAYOR-1: `handleSelectProfile` no captura excepciones de `openSession`**

- `openSession()` no tiene `try/catch` interno y `handleSelectProfile()` tampoco.
- Si `apiClient.post` lanza excepción (error de red, timeout), la promesa rechaza sin manejar.
- Resultado: el usuario no recibe feedback, no se muestra aviso, el perfil queda seleccionado sin opción de reintento.
- El diseño del sprint (R3) indica explícitamente que error técnico y perfil bloqueado deben mostrar el mismo aviso neutral.
- Incoherente con el patrón del propio componente: `loadData()` y `handleVerifyPin()` sí usan `try/catch`.
- **Archivo:** `ChildSelectionModal.vue:203-217`
- **Corrección esperada:** Envolver `openSession` en `try/catch`; en caso de excepción, mostrar el mismo `showBlockedNotice = true` (o un estado de error genérico).

#### MENORES

**MENOR-1: Sin estado de carga durante la validación del perfil**

- Entre el click en el perfil y la resolución de `openSession` no hay spinner ni indicador visual.
- El perfil muestra `selected` pero no hay feedback de "validando".
- Impacto: el usuario (posiblemente un niño) puede hacer click repetidamente.

**MENOR-2: `role="alert"` contradice `aria-live="polite"`**

- `role="alert"` implica `aria-live="assertive"` por spec, lo que anula el `aria-live="polite"` declarado.
- Coincide con el pseudo-código del sprint, pero técnicamente es redundante/conflictivo.
- Para un aviso neutral no urgente, `aria-live="polite"` sin `role="alert"` sería más apropiado, o bien usar solo `role="alert"` si se considera urgente.

**MENOR-3: Sin protección contra doble-click**

- No se deshabilita la tarjeta ni se establece flag durante la llamada asíncrona.
- Múltiples clicks podrían generar múltiples llamadas a `openSession`.

#### OBSERVACIONES

**OBS-1:** Tarea 41.5 (tests) correctamente registrada como deuda técnica. Transparente y aceptable dado que no existe framework de tests.

**OBS-2:** El texto "Este perfil no puede jugar ahora" está marcado como provisional en el sprint, sujeto a validación de producto. El handoff a producto sigue pendiente.

### Veredicto

**CHANGES_REQUIRED**

### Justificación del veredicto

La implementación cumple 5 de 6 criterios de aceptación del sprint y la arquitectura es correcta. Sin embargo, existe un defecto MAYOR que viola el diseño del sprint: ante un error de red durante `openSession`, el usuario no recibe ningún feedback ni puede recuperar el estado. El sprint establece explícitamente (R3) que error técnico y perfil bloqueado deben producir el mismo aviso neutral, pero la ausencia de `try/catch` hace que los errores de red no muestren nada. Este defecto es corregible sin cambios de diseño.

### Acciones requeridas

1. **MAYOR-1 (bloqueante para re-review):** Añadir `try/catch` en `handleSelectProfile()` alrededor de `openSession(profile.id)`. En el `catch`, establecer `showBlockedNotice.value = true` (mismo aviso neutral que perfil bloqueado, conforme al diseño del sprint R3). Alternativamente, añadir `try/catch` dentro de `openSession()` en `sessionService.ts` para que devuelva `null` en caso de excepción.

2. **MENOR-1 (recomendado):** Añadir un estado de carga local (`validatingProfile`) con spinner en la tarjeta seleccionada durante la llamada a `openSession`.

3. **MENOR-3 (recomendado):** Deshabilitar la tarjeta o añadir flag `isValidating` para prevenir múltiples llamadas simultáneas.

4. **MENOR-2 (opcional):** Resolver la contradicción `role="alert"` / `aria-live="polite"` — elegir uno según la urgencia del aviso.

---

## Correcciones de review

- **Fecha:** 2026-09-05
- **Implementado por:** developer-frontend
- **Estado:** implemented

### MAYOR-1 — `handleSelectProfile` no captura excepciones de `openSession` ✅ CORREGIDO

**Corrección aplicada:** Se ha envuelto `openSession(profile.id)` en un `try/catch/finally` dentro de `handleSelectProfile()`. En caso de excepción (error de red, timeout, etc.), se establece `showBlockedNotice.value = true` mostrando el mismo aviso neutral que para perfil bloqueado, conforme al diseño del sprint (R3). El flag `validatingProfile` se resetea en `finally`.

### MENOR-1 — Sin estado de carga durante la validación ✅ CORREGIDO

**Corrección aplicada:** Se ha añadido el estado `validatingProfile: ref<boolean>(false)`. Durante la validación se muestra un overlay con `NubiSpinner` sobre el grid de perfiles. Se ha añadido la cadena i18n `views.home.childSelection.validating: 'Validando perfil...'` en `es.ts`.

### MENOR-3 — Sin protección contra doble-click ✅ CORREGIDO

**Corrección aplicada:** Se utiliza el mismo flag `validatingProfile` para prevenir doble-click. Al inicio de `handleSelectProfile`, si `validatingProfile.value` es `true`, la función retorna inmediatamente. Además, se pasa `:disabled="validatingProfile"` a todos los `ChildProfileCard`, deshabilitándolos visual y funcionalmente durante la validación. Se ha añadido el prop `disabled` al componente `ChildProfileCard.vue` con los estilos y atributos ARIA correspondientes (`aria-disabled`, `tabindex="-1"`, `pointer-events: none`).

### MENOR-2 — `role="alert"` contradice `aria-live="polite"` ✅ CORREGIDO

**Corrección aplicada:** Se ha eliminado `role="alert"` de `BlockedProfileNotice.vue`, manteniendo `aria-live="polite"` como único mecanismo de anuncio. El aviso es neutral y no urgente, por lo que `aria-live="polite"` es el atributo correcto.

### Archivos modificados en esta corrección

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/home/ChildSelectionModal.vue` | Modificación (try/catch, validatingProfile, spinner overlay, disabled en tarjetas) |
| `framework/frontend/app/src/components/home/ChildProfileCard.vue` | Modificación (añadido prop `disabled`, estilos y ARIA) |
| `framework/frontend/app/src/components/home/BlockedProfileNotice.vue` | Modificación (eliminación de `role="alert"`) |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación (añadida cadena `validating`) |

### Verificación de tipos

- `npx vue-tsc --noEmit` ejecutado. Sin errores nuevos. Los 24 errores reportados son todos pre-existentes en archivos `.story.vue`, `NubiNumberInput.vue`, `NubiSelect.vue`, `NubiTooltip.vue`, `FamilyRegistrationModal.vue` y `TooltipView.vue`. Ninguno en los archivos del sprint.

---

## Re-revisión

- **Fecha:** 2026-09-05
- **Revisado por:** reviewer-frontend
- **Veredicto:** APPROVED

### Verificación de correcciones

| Incidencia | Estado | Evidencia |
|------------|--------|-----------|
| MAYOR-1: try/catch en handleSelectProfile | ✅ Verificado | `ChildSelectionModal.vue:216-230` — `try/catch/finally` con `showBlockedNotice.value = true` en catch |
| MENOR-1: Estado de carga durante validación | ✅ Verificado | `ChildSelectionModal.vue:44-48` — overlay con spinner; línea 39: `:disabled="validatingProfile"` |
| MENOR-2: role="alert" vs aria-live | ✅ Verificado | `BlockedProfileNotice.vue:2` — solo `aria-live="polite"` sin `role="alert"` |
| MENOR-3: Protección contra doble-click | ✅ Verificado | `ChildSelectionModal.vue:211` — early return si `validatingProfile.value`; línea 39: tarjetas deshabilitadas |

### Verificación estática (re-revisión)

`npx vue-tsc --noEmit` — **Sin errores nuevos.** Los 24 errores reportados son todos pre-existentes en archivos no relacionados con el sprint. Ningún error en `ChildSelectionModal.vue`, `BlockedProfileNotice.vue`, `sessionService.ts` ni `es.ts`.

### Validación de criterios de aceptación (re-revisión)

| # | Criterio | Resultado | Evidencia |
|---|----------|-----------|-----------|
| AC1 | Perfil habilitado → GameView con carga inicial | **Cumple** | `ChildSelectionModal.vue:224-225` navega a GameView tras `openSession` exitoso |
| AC5 | Perfil bloqueado → GameView no se abre | **Cumple** | `ChildSelectionModal.vue:219-221` retorna sin navegar si `!session`; línea 226-227 muestra aviso si excepción |
| AC6 | Aviso neutral + permanece en selector | **Cumple** | Overlay dentro de `currentView === 'selection'` (línea 52-56), `handleBlockedNoticeClose` resetea estado |
| AC7 | Aviso sin causa, datos parentales, configuración ni castigo | **Cumple** | Solo icono lock + "Este perfil no puede jugar ahora" |
| AC9 | Diferenciable sin solo color/texto/sonido, usable en móvil/tableta | **Cumple** | Icono + texto + botón 44x44px, media queries responsive, `aria-live="polite"` |
| vue-tsc | Sin errores nuevos | **Cumple** | Todos los errores son pre-existentes |

### Resumen de cambios adicionales

Además de las correcciones de los defectos, se han realizado las siguientes mejoras:

1. **ChildProfileCard.vue** — Añadido prop `disabled` con estilos y atributos ARIA (`aria-disabled`, `tabindex="-1"`, `pointer-events: none`)
2. **es.ts** — Añadida cadena `views.home.childSelection.validating: 'Validando perfil...'`

### Veredicto final

**APPROVED**

### Justificación

Todas las incidencias identificadas en la revisión inicial han sido corregidas y verificadas:

- **MAYOR-1:** El manejo de errores está correctamente implementado con `try/catch/finally`, mostrando el mismo aviso neutral para errores de red y perfiles bloqueados (conforme a R3 del diseño del sprint).
- **MENOR-1:** Se ha añadido un estado de carga visual (spinner overlay) durante la validación del perfil.
- **MENOR-2:** Se ha resuelto la contradicción de accesibilidad eliminando `role="alert"` y manteniendo `aria-live="polite"`.
- **MENOR-3:** Se ha implementado protección contra doble-click con early return y deshabilitación de tarjetas.

La implementación cumple todos los criterios de aceptación del sprint y la verificación estática no muestra errores nuevos. El sprint puede considerarse completo y verificado.

### Observaciones pendientes

- **OBS-2:** El texto "Este perfil no puede jugar ahora" sigue sujeto a validación de producto (handoff pendiente).
- **Tarea 41.5:** Pruebas unitarias y E2E registradas como deuda técnica (no existe framework de tests en el proyecto).
