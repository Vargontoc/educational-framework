# Propuesta técnica frontend — FEAT-006: Gestión parental de perfiles infantiles

## 1. Capa principal

**Frontend** — Vue 3 + TypeScript + Pinia + Tailwind CSS + vue-i18n

## 2. Objetivo técnico

Implementar la sección **Niños** dentro del panel parental, permitiendo al adulto autenticado:

- Consultar los perfiles infantiles de la familia en una cuadrícula con duración de sesión activa.
- Editar datos individuales (nombre, fecha de nacimiento, avatar), ajustes de audio/NPC (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`) y accesibilidad visual (8 perfiles + `NONE`).
- Expulsar sesiones activas, bloquear/desbloquear perfiles y eliminar perfiles con confirmación.
- Acceder a un dashboard placeholder por perfil.
- Registrar nuevos niños reutilizando el stepper de creación ya existente.

Todos los sprints de este análisis están **BLOQUEADOS** hasta que backend complete los cambios de contrato y modelo detallados en la sección 4.

## 3. Diseño funcional-técnico

### 3.1 Navegación y rutas

La ruta `/panel/ninos` ya existe en `router/index.ts:42`. Se amplía con rutas hijas para edición y dashboard:

| Ruta | Nombre | Componente | Descripción |
|------|--------|------------|-------------|
| `/panel/ninos` | `PanelNinos` | `NinosView.vue` | Cuadrícula de perfiles |
| `/panel/ninos/:id` | `PanelNinoEdit` | `ChildProfileEditView.vue` | Edición de perfil |
| `/panel/ninos/:id/dashboard` | `PanelNinoDashboard` | `ChildDashboardView.vue` | Placeholder dashboard |

Todas las rutas hijas heredan `meta: { requiresParentalAuth: true }` del layout `ParentPanelLayout.vue`.

```typescript
{
  path: 'ninos',
  name: 'PanelNinos',
  component: () => import('../views/NinosView.vue')
},
{
  path: 'ninos/:id',
  name: 'PanelNinoEdit',
  component: () => import('../views/ChildProfileEditView.vue')
},
{
  path: 'ninos/:id/dashboard',
  name: 'PanelNinoDashboard',
  component: () => import('../views/ChildDashboardView.vue')
}
```

### 3.2 Vista de cuadrícula — `NinosView.vue`

Sustituye el placeholder actual (`NinosView.vue:1-7`). Estructura:

```
┌─────────────────────────────────────────────┐
│  Breadcrumb: Niños                           │
├─────────────────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐              │
│  │Avatar│  │Avatar│  │Avatar│              │
│  │Nombre│  │Nombre│  │Nombre│              │
│  │ 12:34│  │      │  │ 03:21│              │
│  │[Expl]│  │[Blq] │  │[Expl]│              │
│  │[Blq] │  │      │  │[Blq] │              │
│  └──────┘  └──────┘  └──────┘              │
│                                             │
│         [ Registrar niño ]                  │
└─────────────────────────────────────────────┘
```

#### 3.2.1 Componente `ParentalChildCard`

Nuevo componente en `components/ninos/ParentalChildCard.vue`. Difiere de `ChildProfileCard.vue` (que es para selección de juego):

| Propósito | `ChildProfileCard` (existente) | `ParentalChildCard` (nuevo) |
|-----------|-------------------------------|----------------------------|
| Contexto | Modal de selección de juego | Panel parental |
| Acciones | Seleccionar para jugar | Editar, expulsar, bloquear |
| Duración | No muestra | Muestra duración de sesión activa |
| Click | Inicia sesión de juego | Abre edición de perfil |

**Props:**

```typescript
interface Props {
  profile: ChildProfileExtended
  activeSession: ChildSession | null
  isBlocked: boolean
}
```

**Emits:**

```typescript
interface Emits {
  edit: [profileId: number]
  expel: [profileId: number]
  toggleBlock: [profileId: number]
}
```

**Contenido de la tarjeta:**
- Avatar SVG (reutiliza lógica de `ChildProfileCard:40-45`).
- Nombre del perfil.
- Etiqueta de duración de sesión (solo si `activeSession !== null`). Formato `MM:SS`. No se presenta como progreso ni capacidad.
- Acción **«Expulsar»** (solo si `activeSession !== null`).
- Acción **«Bloquear»** / **«Desbloquear»** según `isBlocked`.
- Click en la tarjeta → emit `edit`.

#### 3.2.2 Cuadrícula responsive

Usar `NubiGrid` con `cols` adaptativo:
- Móvil portrait: 2 columnas.
- Móvil landscape / tableta: 3 columnas.
- Tableta landscape: 4 columnas.

#### 3.2.3 Acción «Registrar niño»

Botón `NubiButton` centrado bajo la cuadrícula. Al pulsarlo, abre el modal con `ChildRegistrationStepper` (ver 3.6).

### 3.3 Vista de edición — `ChildProfileEditView.vue`

Accesible desde `/panel/ninos/:id`. Estructura:

```
┌─────────────────────────────────────────────────┐
│  ← Niños > [Nombre]                              │
├─────────────────────────────────────────────────┤
│                                                  │
│  ┌─ Datos básicos ─────────────────────────────┐ │
│  │ Nombre: [____________]                      │ │
│  │ Fecha nacimiento: [____-__-__]              │ │
│  │ Avatar: [selector]                          │ │
│  └─────────────────────────────────────────────┘ │
│                                                  │
│  ┌─ Audio del NPC ─────────────────────────────┐ │
│  │ Voz del NPC  [toggle] [===●===] 75%        │ │
│  │   ↳ Deshabilitado a nivel familiar          │ │
│  │ NPC          [toggle]                       │ │
│  │   ↳ Deshabilitado a nivel familiar          │ │
│  └─────────────────────────────────────────────┘ │
│                                                  │
│  ┌─ Accesibilidad visual ──────────────────────┐ │
│  │ [toggle inactivo] Sin ajuste                │ │
│  │ ○ ○ ○  (ejemplos simples)                   │ │
│  │ ⚠ No es una sección médica...               │ │
│  └─────────────────────────────────────────────┘ │
│                                                  │
│  [Guardar cambios]  [Eliminar]  [Dashboard]     │
└─────────────────────────────────────────────────┘
```

#### 3.3.1 Breadcrumb

Componente `NubiBreadcrumb` con estructura:
- `Niños` (navegable → `/panel/ninos`)
- `[Nombre del perfil]` (actual, no navegable)

#### 3.3.2 Sección de datos básicos

- **Nombre:** `NubiTextInput` con `v-model`.
- **Fecha de nacimiento:** `<input type="date">` con `max` = fecha actual (mismo patrón que `ChildSelectionModal.vue:88-100`).
- **Avatar:** `AvatarSelector` existente (`components/home/AvatarSelector.vue`).

#### 3.3.3 Sección de audio/NPC individual

Reutiliza `ToggleWithPercentage` para voz del NPC y `NubiToggle` para NPC:

| Ajuste individual | Componente | Campo contrato | Condición de deshabilitado |
|-------------------|-----------|---------------|---------------------------|
| Voz del NPC | `ToggleWithPercentage` | `npcVoiceEnabled` + `npcVoiceVolume` | `familyGlobalConfig.npcVoiceEnabled === false` |
| NPC | `NubiToggle` | `npcEnabled` | `familyGlobalConfig.npcEnabled === false` |

Cuando el ajuste global está deshabilitado:
- El control individual se muestra con `disabled=true`.
- Se muestra una etiqueta: «Deshabilitado a nivel familiar».
- El valor se muestra pero no es modificable.

#### 3.3.4 Sección de accesibilidad visual

**Toggle principal:** inactivo si `colorVisionMode === 'NONE'`, activo si tiene otro valor.

**Al activarse:** muestra `NubiSelect` con las 8 opciones + `NONE`:

| Valor | Etiqueta visible |
|-------|-----------------|
| `NONE` | Sin ajuste |
| `DEUTERANOPIA` | Deuteranopia |
| `DEUTERANOMALY` | Deuteranomalía |
| `PROTANOPIA` | Protanopia |
| `PROTANOMALY` | Protanomalía |
| `TRITANOPIA` | Tritanopia |
| `TRITANOMALY` | Tritanomalía |
| `ACHROMATOMALY` | Acromatomalía |
| `ACHROMATOPSIA` | Acromatopsia |

**Al desactivar y guardar:** se envía `colorVisionMode: 'NONE'`.

**Ejemplos visuales:** SVG con círculos y cuadrados de colores diferenciados por forma (no solo por color). No incluyen resultados ni evaluaciones.

**Aviso:** texto visible: *«Esta configuración es orientativa. No es una sección médica ni diagnóstica. Ante dudas, consulta a un especialista.»*

#### 3.3.5 Acciones

| Acción | Comportamiento |
|--------|---------------|
| **Guardar cambios** | Envía `PUT /api/v1/family/children/:id` con payload parcial. Toast de éxito. |
| **Eliminar** | Abre `NubiConfirmModal`: «¿Eliminar a [Nombre]? Se eliminarán todos sus datos.» Confirmación → `DELETE /api/v1/family/children/:id` → navegación a `/panel/ninos`. |
| **Dashboard** | Navega a `/panel/ninos/:id/dashboard` (placeholder). |

### 3.4 Vista dashboard placeholder — `ChildDashboardView.vue`

```
┌─────────────────────────────────────────────────┐
│  ← Niños > [Nombre] > Dashboard                  │
├─────────────────────────────────────────────────┤
│                                                  │
│             [Contenido placeholder]              │
│         «Próximamente disponible»                │
│                                                  │
└─────────────────────────────────────────────────┘
```

Breadcrumb: `Niños` > `[Nombre]` > `Dashboard`.

### 3.5 Composables

#### 3.5.1 `useChildSessions` — Polling de sesiones activas

```typescript
interface ChildSession {
  id: number
  childProfileId: number
  familyId: number
  status: string
  startedAt: string
  endedAt: string | null
  durationSeconds: number | null
  lastActivityAt: string
}

interface UseChildSessionsReturn {
  sessions: Ref<ChildSession[]>
  loading: Ref<boolean>
  error: Ref<boolean>
  errorMessage: Ref<string>
  activeSessionByChildId: ComputedRef<Map<number, ChildSession>>
  getSessionDuration: (childProfileId: number) => number | null
  startPolling: (familyId: number, intervalMs?: number) => void
  stopPolling: () => void
  expelChild: (sessionId: number) => Promise<boolean>
}
```

**Lógica de polling:**
- `startPolling(familyId, 5000)` invoca `GET /sessions/children?familyId={id}` cada 5 segundos.
- `stopPolling()` limpia el intervalo.
- `activeSessionByChildId` filtra sesiones con `status === 'ACTIVE'` y las indexa por `childProfileId`.
- `getSessionDuration(childProfileId)` calcula la duración en segundos desde `startedAt` hasta ahora (o `durationSeconds` si la sesión terminó).
- `expelChild(sessionId)` invoca `DELETE /sessions/children/{id}` y retorna éxito/error.

**Ciclo de vida:**
- Se inicia al montar `NinosView.vue`.
- Se detiene al desmontar o al navegar fuera de `/panel/ninos`.

#### 3.5.2 `useChildProfileEdit` — Estado de edición de perfil

```typescript
interface UseChildProfileEditReturn {
  profile: Ref<ChildProfileExtended | null>
  draft: Ref<ChildProfileDraft>
  loading: Ref<boolean>
  saving: Ref<boolean>
  error: Ref<boolean>
  errorMessage: Ref<string>
  hasChanges: ComputedRef<boolean>
  isNpcVoiceDisabledByFamily: ComputedRef<boolean>
  isNpcDisabledByFamily: ComputedRef<boolean>
  loadProfile: (id: number) => Promise<void>
  saveChanges: () => Promise<boolean>
  deleteProfile: () => Promise<boolean>
}

interface ChildProfileDraft {
  name: string
  birthday: string
  avatar: string
  npcVoiceEnabled: boolean
  npcVoiceVolume: number
  npcEnabled: boolean
  colorVisionMode: string
}
```

**Lógica:**
- `loadProfile(id)`: `GET /api/v1/family/children/:id` → mapea a `profile` y `draft`.
- `saveChanges()`: construye payload parcial (diff `draft` vs `profile`) → `PUT /api/v1/family/children/:id`.
- `deleteProfile()`: `DELETE /api/v1/family/children/:id`.
- `isNpcVoiceDisabledByFamily` / `isNpcDisabledByFamily`: consulta `useGlobalConfig` para determinar si el ajuste global correspondiente está deshabilitado.

#### 3.5.3 `useChildActivation` — Toggle de bloqueo

```typescript
interface UseChildActivationReturn {
  toggling: Ref<boolean>
  toggleActivation: (childId: number) => Promise<boolean>
}
```

Invoca `PUT /api/v1/family/children/activation/{id}` (toggle sin body, decisión P2).

### 3.6 Componente reutilizable `ChildRegistrationStepper.vue`

Extraído de la lógica actual de `ChildSelectionModal.vue:64-110`. Ubicación: `components/ninos/ChildRegistrationStepper.vue`.

**Props:**

```typescript
interface Props {
  familyId: number
}
```

**Emits:**

```typescript
interface Emits {
  childCreated: [profile: ChildProfile]
  cancel: []
}
```

**Contenido:**
- Usa `NubiStepper` con 2 pasos: nombre → fecha de nacimiento + avatar.
- Lógica de creación existente en `ChildSelectionModal.vue:379-414`.
- Al crear exitosamente: emite `childCreated` con el nuevo perfil.

**Consumidores:**
1. `NinosView.vue` — modal de registro bajo la cuadrícula.
2. `ChildSelectionModal.vue` — refactorización para consumir el componente en lugar de lógica inline.

### 3.7 Modelo de datos frontend

#### 3.7.1 Interfaz `ChildProfileExtended`

Ampliación de `ChildProfile` en `familyService.ts:5-10`:

```typescript
export interface ChildProfileExtended {
  id: number
  familyId: number
  name: string
  active: boolean
  birthday: string
  avatar: string
  npcVoiceEnabled: boolean
  npcEnabled: boolean
  npcVoiceVolume: number
  colorVisionMode: string
  createdAt: string
  updatedAt: string
}
```

#### 3.7.2 Interfaz `CreateChildRequest` (actualizada)

```typescript
export interface CreateChildRequest {
  name: string
  birthday: string
  avatar: string
  npcVoiceEnabled: boolean
  npcEnabled: boolean
  npcVoiceVolume: number
  colorVisionMode: string | null
}
```

#### 3.7.3 Interfaz `UpdateChildProfileRequest`

```typescript
export interface UpdateChildProfileRequest {
  name?: string
  birthday?: string
  avatar?: string
  npcVoiceEnabled?: boolean
  npcVoiceVolume?: number
  npcEnabled?: boolean
  colorVisionMode?: string
}
```

### 3.8 Flujo completo de interacción

```
[Entrar en Niños]
  → GET /api/v1/family/children → cargar perfiles
  → GET /sessions/children?familyId={id} → cargar sesiones activas
  → Iniciar polling cada 5s

[Ver cuadrícula]
  → Cada tarjeta muestra avatar, nombre, duración si hay sesión activa
  → Tarjeta con sesión activa → muestra «Expulsar»
  → Tarjeta bloqueada → muestra «Desbloquear»

[Pulsar «Expulsar»]
  → NubiConfirmModal: «¿Terminar la sesión de [Nombre]?»
  → Confirmar → DELETE /sessions/children/{sessionId}
  → Éxito → toast + actualizar sesiones
  → Cancelar → cerrar modal

[Pulsar «Bloquear»]
  → PUT /api/v1/family/children/activation/{id}
  → Éxito → actualizar estado local (active = false)
  → La tarjeta muestra «Desbloquear»

[Pulsar tarjeta → Edición]
  → router.push(`/panel/ninos/${id}`)
  → GET /api/v1/family/children/:id → cargar perfil
  → GET /api/v1/family → cargar config global (para determinar deshabilitados)

[Modificar ajustes en edición]
  → draft se actualiza
  → Si ajuste global deshabilitado → control individual deshabilitado con etiqueta

[Pulsar «Guardar cambios»]
  → PUT /api/v1/family/children/:id con payload parcial
  → Éxito → toast + recargar perfil

[Pulsar «Eliminar»]
  → NubiConfirmModal: «¿Eliminar a [Nombre]? Se eliminarán todos sus datos.»
  → Confirmar → DELETE /api/v1/family/children/:id
  → Éxito → navigateTo('PanelNinos')

[Pulsar «Dashboard»]
  → navigateTo('PanelNinoDashboard', { id })

[Pulsar «Registrar niño»]
  → Abre modal con ChildRegistrationStepper
  → Creación exitosa → recargar cuadrícula
```

## 4. Contratos y dependencias externas

### 4.1 Contratos a extender (dependencia de backend)

| Fichero | Acción | Decisión |
|---------|--------|----------|
| `schemas/family/child-profile-response.yaml` | Renombrar `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`. Añadir `npcVoiceVolume: integer`. Añadir `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY` al enum `colorVisionMode`. | P1, P5 |
| `schemas/family/update-child-profile-request.yaml` | Mismos cambios que `child-profile-response.yaml`. | P1, P5 |
| `schemas/family/create-child-profile-request.yaml` | Mismos cambios por coherencia. | P1, P5 |
| `paths/family/activate-children.yaml` | Documentar semántica toggle sin body. | P2 |
| `paths/session/get-session-children.yaml` | Confirmar que devuelve sesiones activas con `startedAt`. | P3 |

### 4.2 Estado actual de contratos

**`child-profile-response.yaml` actual:**
- Campos: `id`, `familyId`, `name`, `active`, `birthday`, `avatar`, `ttsEnabled`, `agentEnabled`, `colorVisionMode`, `createdAt`, `updatedAt`.
- Enum `colorVisionMode`: `NONE`, `PROTANOPIA`, `DEUTERANOMALY`, `DEUTERANOPIA`, `TRITANOPIA`, `ACHROMATOPSIA`.

**Cambios requeridos:**
```yaml
# Renombrar
ttsEnabled → npcVoiceEnabled
agentEnabled → npcEnabled

# Añadir
npcVoiceVolume:
  type: integer
  minimum: 0
  maximum: 100

# Ampliar enum colorVisionMode
enum:
  - NONE
  - PROTANOPIA
  - PROTANOMALY        # nuevo
  - DEUTERANOPIA
  - DEUTERANOMALY
  - TRITANOPIA
  - TRITANOMALY        # nuevo
  - ACHROMATOMALY      # nuevo
  - ACHROMATOPSIA
```

### 4.3 Endpoints que consume frontend

| Método | Endpoint | Schema request | Schema response | Uso | Sprint |
|--------|----------|---------------|-----------------|-----|--------|
| GET | `/api/v1/family/children` | — | `api-list-child-profile-response.yaml` | Cargar perfiles | S2 |
| GET | `/api/v1/family/children/:id` | — | `api-child-profile-response.yaml` | Cargar perfil para edición | S3 |
| PUT | `/api/v1/family/children/:id` | `update-child-profile-request.yaml` | `api-child-profile-response.yaml` | Guardar cambios edición | S3 |
| DELETE | `/api/v1/family/children/:id` | — | — | Eliminar perfil | S3 |
| PUT | `/api/v1/family/children/activation/{id}` | — (toggle sin body) | — | Bloquear/desbloquear | S2 |
| GET | `/sessions/children?familyId={id}` | — | `api-list-child-session-response.yaml` | Sesiones activas (polling) | S2 |
| DELETE | `/sessions/children/{id}` | — | — | Expulsar sesión | S2 |
| POST | `/api/v1/family/children` | `create-child-profile-request.yaml` | `api-child-profile-response.yaml` | Crear perfil (stepper) | S4 |
| GET | `/api/v1/family` | — | `api-family-response.yaml` | Config global (deshabilitados) | S3 |

### 4.4 Servicios a añadir en `familyService.ts`

```typescript
export async function getChild(id: number): Promise<ChildProfileExtended>
export async function updateChild(id: number, request: UpdateChildProfileRequest): Promise<UpdateChildResult>
export async function deleteChild(id: number): Promise<boolean>
export async function toggleChildActivation(id: number): Promise<boolean>
```

### 4.5 Servicio a crear: `sessionService.ts`

```typescript
export async function getActiveSessions(familyId: number): Promise<ChildSession[]>
export async function expelSession(sessionId: number): Promise<boolean>
```

### 4.6 Handoffs a backend (bloqueantes)

#### A. Extensión de contratos

| Fichero | Acción |
|---------|--------|
| `schemas/family/child-profile-response.yaml` | Renombrar `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`. Añadir `npcVoiceVolume: integer`. Añadir `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY` al enum `colorVisionMode`. |
| `schemas/family/update-child-profile-request.yaml` | Mismos cambios. |
| `schemas/family/create-child-profile-request.yaml` | Mismos cambios por coherencia. |

#### B. Cambios en modelo backend

| Entidad | Acción |
|---------|--------|
| `ChildProfile` (o equivalente) | Renombrar campos `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`. Añadir campo `npcVoiceVolume: Integer`. |
| Migraciones de base de datos | Actualizar nombres de columnas y añadir nueva columna `npc_voice_volume`. |
| DTOs y mappers | Reflejar los cambios de nombre y nuevos campos. |

#### C. Documentación de endpoints

| Fichero | Acción |
|---------|--------|
| `paths/family/activate-children.yaml` | Documentar que `PUT /family/children/activation/{id}` funciona como toggle (intercambia el valor booleano interno) sin requerir body. |
| `paths/session/get-session-children.yaml` | Confirmar que `GET /sessions/children?familyId={id}` devuelve sesiones activas con `startedAt` para cálculo de duración. |

## 5. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Backend no completa renombrado de contratos a tiempo | Frontend no puede consumir campos correctos; sprints bloqueados indefinidamente | Todos los sprints marcados como BLOQUEADOS. No se inicia implementación hasta confirmación de contratos. |
| Polling de 5s genera carga innecesaria en backend | Rendimiento degradado con 5-6 usuarios concurrentes | Intervalo de 5s es conservador para ≤6 usuarios. Se puede ampliar a 10s si se detecta carga. El polling se detiene al salir de la vista. |
| Confundir «Bloquear» con «Eliminar» | Expectativas equivocadas sobre datos del niño | Confirmaciones explícitas con texto diferenciado. «Bloquear» conserva datos; «Eliminar» los borra. Etiqueta visual en tarjeta bloqueada. |
| Nombres de perfiles visuales interpretados como diagnóstico | Preocupación innecesaria en la familia | Aviso visible de no-diagnóstico. Ejemplos simples sin evaluación. Texto: «Consulta a un especialista ante dudas.» |
| Duración de sesión sin contexto sugiere control de uso | Ansiedad familiar | Etiqueta limitada a «tiempo de sesión actual». No se muestra histórico, no se compara, no se presenta como progreso ni capacidad. |
| Renombrado de `ttsEnabled`/`agentEnabled` rompe compatibilidad | Perfiles existentes con valores perdidos | Backend debe migrar datos existentes. Frontend usa los nuevos nombres exclusivamente tras confirmación de contratos. |
| `ChildRegistrationStepper` extraído introduce regresión en Home | El alta desde Home deja de funcionar | Refactorización en sprint separado (S4) con verificación explícita de ambos consumidores (Home y Niños). |
| Race condition: polling activo + expulsión manual | Estado inconsistente tras expulsión | Tras expulsión exitosa, forzar refresco inmediato de sesiones (invalidar intervalo). |
| Edición con config global cambiada durante edición | Ajuste individual muestra estado obsoleto | Al guardar, re-validar contra config global actual. Si cambió, mostrar aviso. |

## 6. Preguntas de decisión al usuario

Todas las preguntas de decisión han sido resueltas y confirmadas:

| # | Decisión | Estado |
|---|----------|--------|
| P1 | Ajustes individuales incluyen valor porcentual (`npcVoiceVolume`). Backend renombra `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`. | **Confirmada** |
| P2 | Endpoint `/activation/{id}` funciona como toggle sin body. | **Confirmada** |
| P3 | Polling cada 5s para duración de sesión. | **Confirmada** |
| P4 | Extraer stepper a componente reutilizable `ChildRegistrationStepper.vue`. | **Confirmada** |
| P5 | Estado sin ajuste = `'NONE'`. Enum mantiene valor `NONE`. | **Confirmada** |

## 7. Sprints propuestos

> **Todos los sprints están en estado BLOQUEADO.**
> No se iniciará la implementación hasta que backend complete los cambios de contrato y modelo descritos en la sección 4.6.

---

### Sprint 1 — Infraestructura de contratos, servicios y tipos

**ESTADO: BLOQUEADO — Dependiente de cambios de backend**

**Dependencias de backend requeridas:**
- [ ] Renombrado de `ttsEnabled` → `npcVoiceEnabled` y `agentEnabled` → `npcEnabled` en los tres esquemas de perfil infantil.
- [ ] Añadir `npcVoiceVolume: integer` a los tres esquemas de perfil infantil.
- [ ] Ampliar enum `colorVisionMode` con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.
- [ ] Documentar semántica toggle en `activate-children.yaml`.
- [ ] Confirmar respuesta de `get-session-children.yaml` con `startedAt`.

**Objetivo:** Preparar tipos, servicios y cliente API para que el frontend pueda consumir los contratos actualizados de perfiles infantiles y sesiones.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 1.1 | Ampliar interfaz `ChildProfileExtended` | Sustituir `ChildProfile` en `familyService.ts` con los nuevos campos: `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`, `colorVisionMode`, `active`, `familyId`, `createdAt`, `updatedAt`. |
| 1.2 | Actualizar `CreateChildRequest` | Renombrar `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`. Añadir `npcVoiceVolume: number`. |
| 1.3 | Crear interfaz `UpdateChildProfileRequest` | Todos los campos opcionales para envío parcial. |
| 1.4 | Crear interfaz `ChildSession` | Mapear `child-session-response.yaml`: `id`, `childProfileId`, `familyId`, `status`, `startedAt`, `endedAt`, `durationSeconds`, `lastActivityAt`. |
| 1.5 | Crear `sessionService.ts` | Funciones `getActiveSessions(familyId)` y `expelSession(sessionId)`. |
| 1.6 | Añadir funciones a `familyService.ts` | `getChild(id)`, `updateChild(id, request)`, `deleteChild(id)`, `toggleChildActivation(id)`. |
| 1.7 | Actualizar `createChild` en `familyService.ts` | Usar nuevos nombres de campos (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`). |
| 1.8 | Crear tipos de color vision | Enum/constante con los 9 valores: `NONE`, `DEUTERANOPIA`, `DEUTERANOMALY`, `PROTANOPIA`, `PROTANOMALY`, `TRITANOPIA`, `TRITANOMALY`, `ACHROMATOMALY`, `ACHROMATOPSIA`. |

**Criterios de aceptación:**
- Las interfaces TypeScript reflejan fielmente los contratos YAML actualizados.
- `sessionService.ts` realiza GET y DELETE correctamente.
- `familyService.ts` incluye las 4 funciones nuevas con manejo de errores (0, 400, 401, 404, 5xx).
- `createChild` envía los campos renombrados.
- TypeScript compila sin errores (`tsc`).

**Evidencias esperadas:**
- `tsc` sin errores.
- Test manual: `getActiveSessions(familyId)` devuelve sesiones con `startedAt`.
- Test manual: `toggleChildActivation(id)` invoca PUT sin body.
- Test manual: `getChild(id)` devuelve perfil con campos renombrados.

---

### Sprint 2 — Cuadrícula de perfiles, sesiones y acciones

**ESTADO: BLOQUEADO — Dependiente de cambios de backend**

**Dependencias de backend requeridas:**
- [ ] Sprint 1 completado (contratos y servicios disponibles).
- [ ] Endpoint `GET /sessions/children?familyId={id}` operativo con `startedAt`.
- [ ] Endpoint `PUT /family/children/activation/{id}` operativo como toggle.
- [ ] Endpoint `DELETE /sessions/children/{id}` operativo para expulsión.

**Objetivo:** Implementar `NinosView.vue` con cuadrícula de perfiles, polling de sesiones activas, duración en tarjetas, y acciones de expulsar/bloquear.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 2.1 | Crear composable `useChildSessions` | Polling cada 5s a `GET /sessions/children`. Mapa de sesiones activas por `childProfileId`. Cálculo de duración desde `startedAt`. |
| 2.2 | Crear composable `useChildActivation` | Invoca `PUT /activation/{id}` como toggle. Estado `toggling`. |
| 2.3 | Crear componente `ParentalChildCard` | Tarjeta con avatar, nombre, duración de sesión, acciones expulsar/bloquear. |
| 2.4 | Implementar `NinosView.vue` | Sustituir placeholder. Cuadrícula con `ParentalChildCard`. Breadcrumb «Niños». Botón «Registrar niño». |
| 2.5 | Integrar polling en `NinosView` | `startPolling` al montar, `stopPolling` al desmontar. |
| 2.6 | Acción «Expulsar» | `NubiConfirmModal` → `expelSession` → refresco. |
| 2.7 | Acción «Bloquear/Desbloquear» | `toggleChildActivation` → actualizar estado local de tarjeta. |
| 2.8 | Formato de duración | `MM:SS` en etiqueta de tarjeta. No se presenta como progreso. |
| 2.9 | Cuadrícula responsive | 2 cols móvil, 3 cols tableta portrait, 4 cols tableta landscape. |
| 2.10 | i18n completo | Etiquetas, confirmaciones, mensajes de error y toasts. |
| 2.11 | Accesibilidad | Labels, aria-labels, objetivos táctiles ≥ 48dp, estados distinguibles sin solo color. |

**Criterios de aceptación:**
- Cuadrícula muestra perfiles de la familia con avatar, nombre y duración de sesión activa.
- Tarjeta con sesión activa muestra «Expulsar»; sin sesión, no lo muestra.
- «Expulsar» requiere confirmación; cancelar no termina la sesión.
- «Bloquear» impide acceso al juego (toggle `active`). Tarjeta muestra «Desbloquear».
- Duración se actualiza con polling cada 5s. No se presenta como progreso ni capacidad.
- «Registrar niño» visible bajo la cuadrícula (funcionalidad completa en S4).
- Responsive en móvil portrait, móvil landscape y tableta.
- TypeScript compila sin errores (`tsc`).

**Evidencias esperadas:**
- Test manual: entrar en Niños → ver perfiles con duración de sesión.
- Test manual: expulsar → confirmación → sesión terminada → tarjeta actualizada.
- Test manual: bloquear → tarjeta muestra «Desbloquear».
- Test manual: esperar 5s → duración actualizada.
- Test manual: responsive en 3 tamaños de pantalla.
- `tsc` sin errores.

---

### Sprint 3 — Edición de perfil y accesibilidad visual

**ESTADO: BLOQUEADO — Dependiente de cambios de backend**

**Dependencias de backend requeridas:**
- [ ] Sprint 1 completado (contratos y servicios disponibles).
- [ ] Endpoint `PUT /family/children/:id` operativo con campos renombrados y `npcVoiceVolume`.
- [ ] Endpoint `DELETE /family/children/:id` operativo.
- [ ] Endpoint `GET /family/children/:id` operativo devolviendo campos renombrados.
- [ ] Enum `colorVisionMode` ampliado con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.

**Objetivo:** Implementar `ChildProfileEditView.vue` con edición de datos, ajustes de audio/NPC individuales con estado deshabilitado por config global, sección de accesibilidad visual con 8 perfiles + NONE, y acciones guardar/eliminar/dashboard.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 3.1 | Crear composable `useChildProfileEdit` | Carga perfil, draft, detección de cambios, guardado parcial, eliminación. |
| 3.2 | Añadir rutas de edición y dashboard | `router/index.ts`: `/panel/ninos/:id` y `/panel/ninos/:id/dashboard`. |
| 3.3 | Implementar `ChildProfileEditView.vue` | Breadcrumb, datos básicos, audio/NPC, accesibilidad visual, acciones. |
| 3.4 | Sección datos básicos | `NubiTextInput` (nombre), `<input type="date">` (fecha), `AvatarSelector` (avatar). |
| 3.5 | Sección audio/NPC individual | `ToggleWithPercentage` para `npcVoiceEnabled`+`npcVoiceVolume`. `NubiToggle` para `npcEnabled`. |
| 3.6 | Lógica de deshabilitado por config global | Consultar `useGlobalConfig` para determinar si ajuste individual está bloqueado. Mostrar etiqueta «Deshabilitado a nivel familiar». |
| 3.7 | Sección accesibilidad visual | Toggle activo/inactivo. `NubiSelect` con 9 opciones. Ejemplos SVG simples. Aviso no médico. |
| 3.8 | Acción «Guardar cambios» | PUT con payload parcial. Toast éxito. |
| 3.9 | Acción «Eliminar» | `NubiConfirmModal` → DELETE → navegar a `/panel/ninos`. |
| 3.10 | Acción «Dashboard» | Navegar a `/panel/ninos/:id/dashboard`. |
| 3.11 | Implementar `ChildDashboardView.vue` | Placeholder con breadcrumb «Niños > [Nombre] > Dashboard». |
| 3.12 | i18n completo | Etiquetas, mensajes, aviso no médico, confirmaciones. |
| 3.13 | Accesibilidad | Labels, aria-labels, objetivos táctiles ≥ 48dp, ejemplos no solo por color. |

**Criterios de aceptación:**
- Edición accesible desde tarjeta con breadcrumb «Niños > [Nombre]».
- Se pueden modificar nombre, fecha de nacimiento y avatar.
- Ajustes `npcVoiceEnabled`+`npcVoiceVolume` y `npcEnabled` visibles y editables.
- Si config global deshabilita un ajuste, el control individual se muestra deshabilitado con etiqueta explicativa.
- Accesibilidad visual: toggle inactivo sin ajuste, activo con valor. Se pueden seleccionar 8 perfiles + NONE.
- Ejemplos visuales son simples (círculos/cuadrados) y no diagnósticos.
- Aviso visible: «No es una sección médica ni diagnóstica. Ante dudas, consulta a un especialista.»
- «Eliminar» requiere confirmación. Cancelar conserva el perfil. Confirmar elimina perfil y datos.
- «Dashboard» muestra placeholder con breadcrumb «Niños > [Nombre] > Dashboard».
- TypeScript compila sin errores (`tsc`).

**Evidencias esperadas:**
- Test manual: editar nombre → guardar → verificar en backend.
- Test manual: deshabilitar npcVoice global → ver ajuste individual bloqueado con etiqueta.
- Test manual: activar accesibilidad visual → seleccionar PROTANOMALY → guardar → verificar.
- Test manual: desactivar accesibilidad visual → guardar → colorVisionMode = NONE.
- Test manual: eliminar → confirmación → perfil eliminado → redirect a cuadrícula.
- Test manual: dashboard → placeholder visible.
- `tsc` sin errores.

---

### Sprint 4 — Extracción de stepper y refactorización de registro

**ESTADO: BLOQUEADO — Dependiente de cambios de backend**

**Dependencias de backend requeridas:**
- [ ] Sprint 1 completado (contratos de creación actualizados con campos renombrados).
- [ ] Endpoint `POST /api/v1/family/children` operativo con `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`.

**Objetivo:** Extraer la lógica de registro de niños a `ChildRegistrationStepper.vue` reutilizable, integrarlo en `NinosView.vue` y refactorizar `ChildSelectionModal.vue` para consumirlo.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 4.1 | Crear `ChildRegistrationStepper.vue` | Extraer lógica de `ChildSelectionModal.vue:64-110, 379-414`. 2 pasos: nombre → fecha+avatar. |
| 4.2 | Integrar stepper en `NinosView.vue` | Modal con `ChildRegistrationStepper` al pulsar «Registrar niño». |
| 4.3 | Refactorizar `ChildSelectionModal.vue` | Sustituir lógica inline de registro por `ChildRegistrationStepper`. |
| 4.4 | Actualizar `createChild` en stepper | Usar nuevos campos: `npcVoiceEnabled: true`, `npcEnabled: true`, `npcVoiceVolume: 100`, `colorVisionMode: null`. |
| 4.5 | Verificar regressión en Home | El alta desde `ChildSelectionModal` (Home) funciona igual que antes. |
| 4.6 | Verificar alta desde Niños | El alta desde `NinosView` funciona y recarga la cuadrícula. |
| 4.7 | i18n completo | Reutilizar etiquetas existentes del stepper. |

**Criterios de aceptación:**
- `ChildRegistrationStepper.vue` funciona como componente independiente.
- Alta desde Home (`ChildSelectionModal`) funciona sin regresiones.
- Alta desde Niños (`NinosView`) crea perfil y recarga cuadrícula.
- `createChild` envía campos renombrados (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`).
- TypeScript compila sin errores (`tsc`).

**Evidencias esperadas:**
- Test manual: alta desde Home → perfil creado → visible en selección.
- Test manual: alta desde Niños → perfil creado → visible en cuadrícula.
- Test manual: ambos flujos usan el mismo stepper visual.
- `tsc` sin errores.
- Verificación de que no hay regresión en el flujo existente de FEAT-003.

---

## Resumen de dependencias entre sprints

```
Sprint 1 (Contratos y servicios)
  ↓
  ├── Sprint 2 (Cuadrícula y sesiones)
  │     ↓
  │     └── Sprint 4 (Stepper y refactorización)
  │
  └── Sprint 3 (Edición y accesibilidad visual)
```

Sprint 2 y Sprint 3 pueden ejecutarse en paralelo una vez Sprint 1 esté completado. Sprint 4 depende de Sprint 1 (contratos de creación) y conceptualmente de Sprint 2 (NinosView debe existir para integrar el stepper).
