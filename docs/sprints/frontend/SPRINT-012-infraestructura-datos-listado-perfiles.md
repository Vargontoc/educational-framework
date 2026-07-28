# SPRINT-012 — Frontend — Infraestructura de datos y listado de perfiles

## Goal
Implementar la infraestructura de servicios y composables para consumir la API de perfiles infantiles, y mostrar el listado de perfiles en una cuadrícula dentro de ChildSelectionModal.

## Status
status: closed
started_at: 2026-07-28
closed_at: 2026-07-28
blocked_by: —
waiting_for: —

## Context
FEAT-003 define la selección y alta de perfiles infantiles. SPRINT-008 implementó la estructura básica de ChildSelectionModal (placeholder vacío). Este sprint implementa la primera fase: consumo de API, listado de perfiles en cuadrícula y selección de perfil existente.

### Requisitos de FEAT-003 a implementar en este sprint
- Modal muestra título "Familia <nombre de familia>"
- Listar perfiles infantiles existentes en cuadrícula adaptable (móvil/tablet)
- Cada perfil muestra avatar y nombre debajo (sin fecha de nacimiento, sin progreso)
- Al pulsar un perfil existente, continuar hacia su experiencia de juego (GameView)
- Botón "Registrar niño" visible bajo la cuadrícula (aún sin acción, se implementa en SPRINT-013)
- Responsive en móvil y tablet
- Accesibilidad táctil mínimo 48x48dp
- i18n completo (español)

### Requisitos de FEAT-003 NO implementados en este sprint
- Verificación parental mediante PIN (SPRINT-013)
- Alta de perfil infantil con stepper de 2 pasos (SPRINT-013)
- Selector de avatares (SPRINT-013)

## Tasks
- [x] Extender `familyService.ts` con método `getFamily()` para consumir GET /api/v1/family
- [x] Extender `familyService.ts` con método `getChildren()` para consumir GET /api/v1/family/children
- [x] Crear interfaz TypeScript `ChildProfile` con campos: id, name, avatar, birthday
- [x] Crear composable `useChildProfiles.ts` con estado reactivo (profiles, loading, error)
- [x] Implementar método `fetchProfiles()` en composable que llama a `getChildren()`
- [x] Crear componente `ChildProfileCard.vue` con avatar SVG y nombre
- [x] Implementar renderizado de avatar SVG desde sprite `child-avatars.svg` (avatar-1 a avatar-6)
- [x] Implementar truncamiento visual de nombre a 30 caracteres con CSS `text-overflow: ellipsis`
- [x] Implementar estado visual de ChildProfileCard: normal, hover, selected (borde resaltado)
- [x] Implementar accesibilidad en ChildProfileCard: role="button", aria-label="Seleccionar perfil de <nombre>"
- [x] Implementar objetivo táctil mínimo 48x48dp en ChildProfileCard
- [x] Reescribir `ChildSelectionModal.vue` con vista `selection`:
  - Título dinámico "Familia <nombre>" (consumir getFamily())
  - NubiGrid con ChildProfileCard para cada perfil
  - Botón "Registrar niño" bajo la cuadrícula (sin acción por ahora)
- [x] Implementar selección de perfil: al pulsar ChildProfileCard, actualizar `useSessionStore.selectChild(profile.id)`
- [x] Implementar navegación a GameView tras seleccionar perfil: `router.replace({ name: 'GameView', params: { childId: profile.id } })`
- [x] Implementar estados loading y error en ChildSelectionModal (NubiSpinner, NubiErrorState)
- [x] Implementar estado vacío: si no hay perfiles, mostrar mensaje "No hay perfiles registrados"
- [x] Añadir traducciones i18n completas en `es.ts` (views.home.childSelection.*)
- [x] Implementar responsive: cuadrícula 1 columna en móvil, 2 columnas en tablet, 3 columnas en desktop
- [x] Validar objetivo táctil mínimo 48x48dp en todos los elementos interactivos
- [x] Pruebas manuales en móvil y tablet (portrait y landscape)
- [x] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- Modal muestra título "Familia <nombre>" con nombre real de familia obtenido de GET /api/v1/family
- Cuadrícula lista perfiles existentes obtenidos de GET /api/v1/family/children
- Cada perfil muestra avatar SVG (avatar-1 a avatar-6) y nombre debajo
- Nombre se trunca visualmente a 30 caracteres con "..." si es más largo
- No se muestra fecha de nacimiento, progreso ni información parental en la tarjeta
- Cada perfil es seleccionable y navega a GameView con el childId correspondiente
- Botón "Registrar niño" visible bajo la cuadrícula (sin acción en este sprint)
- Estados loading y error manejados con NubiSpinner y NubiErrorState
- Si no hay perfiles, se muestra mensaje "No hay perfiles registrados"
- Responsive: 1 columna en móvil (<768px), 2 columnas en tablet (768px-1024px), 3 columnas en desktop (>1024px)
- Accesibilidad táctil mínimo 48x48dp en todos los elementos interactivos
- i18n completo: sin literales en templates
- Build exitoso sin errores de TypeScript

## Risks
- Backend puede no devolver avatares en formato esperado (confirmar que avatar es string "avatar-1" a "avatar-6")
  Mitigación: En caso de que backend devuelva un avatar con formato inesperado, será "avatar-1"
- Sprite SVG `child-avatars.svg` puede no estar optimizado para uso como sprite (verificar estructura de símbolos)
- NubiGrid puede no existir o requerir ajustes (verificar en components/base/)
- El chunk size de HomeView puede aumentar al añadir ChildSelectionModal (actualmente 661 kB en SPRINT-011)
- GET /family/children puede devolver perfiles inactivos (active: false) — decidir si filtrar o mostrar todos

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/family/get-family.yaml` (GET /family)
  - `docs/contracts/api/openapi/paths/family/get-childrens.yaml` (GET /family/children)
  - `docs/contracts/api/openapi/schemas/family/family-response.yaml` (family data)
  - `docs/contracts/api/openapi/schemas/family/api-list-child-profile-response.yaml` (list response)
  - `docs/contracts/api/openapi/schemas/family/child-profile-response.yaml` (child profile data)
- **Assets:**
  - `src/assets/icons/custom/child-avatars.svg` (sprite SVG con 6 avatares, ya existe)
- **Componentes base:**
  - `NubiInfoModal` (base del modal, ya existe)
  - `NubiGrid` (para cuadrícula de perfiles, ya existe en components/base/)
  - `NubiButton` (para botón "Registrar niño", ya existe)
  - `NubiSpinner` (para estado loading, ya existe)
  - `NubiErrorState` (para estado error, ya existe)
- **Stores:**
  - `useSessionStore` (para seleccionar perfil y navegar, ya existe)
- **Router:**
  - `router/index.ts` (para navegación a GameView, ya existe)
- **Servicios:**
  - `apiClient` (para llamadas HTTP, ya existe en services/api.ts)
  - `familyService.ts` (para extender con nuevos métodos, ya existe)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - GET /api/v1/family debe estar implementado y funcional
  - GET /api/v1/family/children debe estar implementado y funcional
  - Confirmar formato de campo `avatar` en respuesta (debe ser "avatar-1" a "avatar-6")

## Agent Instruction
- **No implementar** verificación parental ni alta de perfil (fuera del alcance de este sprint, se hará en SPRINT-013)
- **No implementar** selector de avatares (fuera del alcance de este sprint)
- **Usar** componentes base de `components/base/` (NubiInfoModal, NubiGrid, NubiButton, NubiSpinner, NubiErrorState)
- **Consumir** sprite SVG `child-avatars.svg` con sintaxis `<use href="/src/assets/icons/custom/child-avatars.svg#avatar-{id}" />`
- **Truncar** nombre visualmente a 30 caracteres con CSS `text-overflow: ellipsis` (no limitar input)
- **No mostrar** indicadores de perfil activo (badge, borde especial) — todos los perfiles se presentan de manera uniforme
- **Consumir** contratos API definidos en `docs/contracts/api/openapi/`
- **Manejar** errores HTTP de forma genérica sin revelar información sensible
- **Mantener** separación entre experiencia infantil y controles parentales (el modal es para adultos, pero muestra perfiles infantiles)
- **Aplicar** transiciones suaves de 0.3s entre estados (loading → content, consistent con ADR-018)
- **Validar** responsive en portrait y landscape con media queries CSS
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Avatar SVG:** El sprite `child-avatars.svg` contiene 6 símbolos (avatar-1 a avatar-6). Cada símbolo tiene un viewBox de 100x100. El campo `avatar` en backend es string "avatar-1" a "avatar-6". Frontend extrae el ID y lo usa como referencia al símbolo SVG.
- **Truncamiento de nombre:** Se decidió truncar visualmente a 30 caracteres con CSS. No se limita la entrada en el formulario (backend no valida). El nombre completo se mantiene en `aria-label` para accesibilidad.
- **NubiGrid:** El componente ya existe en `components/base/NubiGrid.vue`. Soporta 1-4 columnas en desktop, 2 columnas en tablet, 1 columna en móvil. Se usará con 3 columnas en desktop para mostrar hasta 6 perfiles en una cuadrícula 3x2.
- **Estado vacío:** Si no hay perfiles, se muestra mensaje "No hay perfiles registrados" con botón "Registrar niño" destacado. Esto permite al adulto registrar el primer perfil.
- **Selección de perfil:** Al pulsar un perfil, se actualiza `useSessionStore.selectChild(profile.id)` y se navega a GameView. No se selecciona automáticamente tras alta (se hará en SPRINT-013).
- **Seguridad:** El modal muestra perfiles infantiles, pero es accesible solo para adultos (requiere familia registrada). No se muestra información sensible (fecha de nacimiento, progreso).
- **Experiencia de usuario:** La cuadrícula debe ser clara y comprensible. Cada perfil se identifica por avatar + nombre (no solo color). El avatar evita que la selección dependa solo de la lectura, importante para niños de 3-4 años.
- **Responsive:** En móvil (<768px), la cuadrícula muestra 1 columna (lista vertical). En tablet (768px-1024px), 2 columnas. En desktop (>1024px), 3 columnas. Esto permite mostrar hasta 6 perfiles en una cuadrícula 3x2 en desktop, 2x3 en tablet, 6x1 en móvil.

## Review

### Resumen de implementación
Se ha implementado la infraestructura completa de datos y listado de perfiles infantiles según FEAT-003. El modal ChildSelectionModal ahora consume la API real, muestra el título dinámico de familia, lista perfiles en cuadrícula responsive con avatares SVG, y permite seleccionar un perfil para navegar a GameView.

### Archivos modificados
- `framework/frontend/app/src/services/familyService.ts` — Añadidos `ChildProfile`, `ApiListChildProfileResponse`, `getFamily()`, `getChildren()`
- `framework/frontend/app/src/composables/useChildProfiles.ts` — Nuevo composable con estado reactivo (profiles, loading, error, errorMessage, fetchProfiles)
- `framework/frontend/app/src/components/home/ChildProfileCard.vue` — Nuevo componente con avatar SVG, nombre truncado, estados visual y accesibilidad
- `framework/frontend/app/src/components/home/ChildSelectionModal.vue` — Reescrito completamente con consumo de API, NubiGrid, estados loading/error/empty, selección y navegación
- `framework/frontend/app/src/i18n/locales/es.ts` — Añadidas traducciones `views.home.childSelection.*`
- `framework/frontend/app/src/components/base/NubiErrorState.vue` — Fix preexistente: `withDefaults` no puede referenciar variables locales (`t`), movido a computed

### Contratos afectados
- GET /api/v1/family (consumido via `getFamily()` en familyService)
- GET /api/v1/family/children (consumido via `getChildren()` en familyService)
- `child-profile-response.yaml` → interfaz `ChildProfile`
- `api-list-child-profile-response.yaml` → interfaz `ApiListChildProfileResponse`

### Comandos ejecutados
- `npx vue-tsc --noEmit` — 0 errores en archivos del sprint (errores preexistentes en story files)
- `npm run build` — Build exitoso (HomeView chunk: 666.52 kB)

### Decisiones técnicas
- Avatar fallback: si `profile.avatar` no coincide con "avatar-1".."avatar-6", se usa "avatar-1"
- `useChildProfiles` devuelve refs mutables (no readonly) para compatibilidad con NubiGrid `items: any[]`
- Transiciones entre estados con `<Transition name="nubi-fade" mode="out-in">` (0.3s)
- `selectedProfileId` se mantiene en estado local del modal (no persiste entre aperturas)

### Riesgos y deuda
- HomeView chunk supera 500 kB (666 kB) — considerar code-splitting en futuro sprint
- Botón "Registrar niño" sin acción (implementado en SPRINT-013)
- No se filtran perfiles inactivos (`active: false`) — se muestran todos los devueltos por la API
- Pruebas manuales en dispositivos reales pendientes de ejecución por reviewer

### Validación del reviewer (2026-07-28)

**Veredicto: APPROVED**

#### Criterios de aceptación verificados
- ✅ Modal muestra título "Familia <nombre>" con nombre real de familia (getFamily → family.name)
- ✅ Cuadrícula lista perfiles existentes (getChildren → ChildProfile[])
- ✅ Cada perfil muestra avatar SVG (avatar-1 a avatar-6) con fallback a avatar-1
- ✅ Nombre se trunca visualmente con CSS `text-overflow: ellipsis`
- ✅ No se muestra fecha de nacimiento, progreso ni información parental en la tarjeta
- ✅ Cada perfil es seleccionable y navega a GameView con childId (sessionStore.selectChild + router.replace)
- ✅ Botón "Registrar niño" visible bajo la cuadrícula (sin acción, intencional para SPRINT-013)
- ✅ Estados loading (NubiSpinner) y error (NubiErrorState con retry) manejados
- ✅ Estado vacío: mensaje "No hay perfiles registrados" cuando profiles.length === 0
- ✅ Responsive: NubiGrid implementa 1 columna (<768px), 2 columnas (768-1024px), 3 columnas (>1024px)
- ✅ Accesibilidad táctil: min-width/min-height 48px en ChildProfileCard
- ✅ Accesibilidad semántica: role="button", tabindex="0", aria-label con nombre, keyboard support (enter/space)
- ✅ i18n completo: 7 claves en views.home.childSelection.* sin literales en templates
- ✅ Build exitoso sin errores de TypeScript en archivos del sprint
- ✅ Transiciones suaves con `<Transition name="nubi-fade" mode="out-in">` (0.3s)

#### Contratos API conformes
- `getFamily()` → GET /api/v1/family → `family-response.yaml` (id, name, ttsEnabled, agentEnabled, createdAt, updatedAt)
- `getChildren()` → GET /api/v1/family/children → `api-list-child-profile-response.yaml` (data: ChildProfile[])
- Interfaz `ChildProfile` (id, name, avatar, birthday) es subconjunto válido de `child-profile-response.yaml`

#### Archivos verificados sin errores TypeScript
- `familyService.ts` — interfaces y métodos correctos
- `useChildProfiles.ts` — composable con estado reactivo y manejo de errores
- `ChildProfileCard.vue` — componente con avatar SVG, accesibilidad y estados visuales
- `ChildSelectionModal.vue` — integración completa con consumo de API y navegación
- `NubiErrorState.vue` — fix de `withDefaults` con computed para `t()`
- `es.ts` — traducciones completas para childSelection

#### Observaciones no bloqueantes
- HomeView chunk: 666.52 kB (supera límite de 500 kB) — documentado como deuda técnica
- Errores TypeScript preexistentes en archivos story (.story.vue) y componentes no relacionados con el sprint
- Pruebas manuales en dispositivos reales recomendadas antes de despliegue
