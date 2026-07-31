# SPRINT-026 — Infraestructura de contratos, servicios y tipos para gestión de perfiles infantiles

## Estado

- **Estado:** closed
- **Fecha de creación:** 2026-07-31
- **Fecha de verificación:** 2026-07-31
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** Backend (renombrado de contratos y modelo)
- **Impacto estimado:** Tipos TypeScript actualizados, servicios API para perfiles y sesiones, base para sprints siguientes

## Objetivo

Preparar tipos, servicios y cliente API para que el frontend pueda consumir los contratos actualizados de perfiles infantiles y sesiones activas. Este sprint es prerrequisito para todos los sprints siguientes de FEAT-006.

## Contexto

El análisis técnico de FEAT-006 establece que backend debe completar los siguientes cambios antes de que frontend pueda iniciar:

**Cambios de backend requeridos:**
- Renombrado de `ttsEnabled` → `npcVoiceEnabled` y `agentEnabled` → `npcEnabled` en los tres esquemas de perfil infantil.
- Añadir `npcVoiceVolume: integer` a los tres esquemas de perfil infantil.
- Ampliar enum `colorVisionMode` con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.
- Documentar semántica toggle en `activate-children.yaml`.
- Confirmar respuesta de `get-session-children.yaml` con `startedAt`.

**Referencias:**
- Propuesta técnica frontend: `docs/product/design/frontend/FEAT-006-propuesta-tecnica-frontend.md`
- Propuesta técnica backend: `docs/product/design/backend/FEAT-006-propuesta-tecnica-backend.md`
- FEAT-006: `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
- ADR-022: `docs/product/decisions/ADR-022-Gestion-parental-de-perfiles-infantiles.md`

**Dependencias de producto:**
- FEAT-003 (Selección y alta de perfiles infantiles)
- FEAT-004 (Estructura visual y navegación del panel parental)
- FEAT-005 (Configuración global de audio, NPC y PIN)
- ADR-021 (Configuración global de audio, NPC y PIN)

## Tareas

### Tarea 26.1: Ampliar interfaz `ChildProfileExtended` — verified

**Descripción:** Sustituir la interfaz `ChildProfile` actual en `familyService.ts` con los nuevos campos renombrados y el nuevo campo `npcVoiceVolume`.

**Archivo:** `framework/frontend/app/src/services/familyService.ts`

**Interfaz actual:**
```typescript
export interface ChildProfile {
  id: number
  familyId: number
  name: string
  active: boolean
  birthday: string
  avatar: string
  ttsEnabled: boolean
  agentEnabled: boolean
  colorVisionMode: string
  createdAt: string
  updatedAt: string
}
```

**Interfaz nueva:**
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

**Criterios de aceptación:**
- La interfaz refleja fielmente el contrato YAML actualizado.
- TypeScript compila sin errores.
- No se rompe el código existente que usa `ChildProfile` (se actualizará en tareas siguientes).

---

### Tarea 26.2: Actualizar `CreateChildRequest` — verified

**Descripción:** Renombrar campos y añadir `npcVoiceVolume` en la interfaz de creación de perfil infantil.

**Archivo:** `framework/frontend/app/src/services/familyService.ts`

**Interfaz actual:**
```typescript
export interface CreateChildRequest {
  name: string
  birthday: string
  avatar: string
  ttsEnabled: boolean
  agentEnabled: boolean
  colorVisionMode: string | null
}
```

**Interfaz nueva:**
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

**Criterios de aceptación:**
- La interfaz refleja fielmente el contrato YAML actualizado.
- TypeScript compila sin errores.

---

### Tarea 26.3: Crear interfaz `UpdateChildProfileRequest` — verified

**Descripción:** Crear nueva interfaz para la actualización parcial de perfil infantil. Todos los campos son opcionales para envío parcial.

**Archivo:** `framework/frontend/app/src/services/familyService.ts`

**Interfaz nueva:**
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

**Criterios de aceptación:**
- Todos los campos son opcionales.
- TypeScript compila sin errores.

---

### Tarea 26.4: Crear interfaz `ChildSession` — verified

**Descripción:** Crear interfaz para mapear `child-session-response.yaml`.

**Archivo:** `framework/frontend/app/src/services/sessionService.ts` (nuevo)

**Interfaz nueva:**
```typescript
export interface ChildSession {
  id: number
  childProfileId: number
  familyId: number
  status: string
  startedAt: string
  endedAt: string | null
  durationSeconds: number | null
  lastActivityAt: string
}
```

**Criterios de aceptación:**
- La interfaz refleja fielmente el contrato YAML.
- TypeScript compila sin errores.

---

### Tarea 26.5: Crear `sessionService.ts` — verified

**Descripción:** Crear nuevo servicio para gestionar sesiones activas de perfiles infantiles.

**Archivo:** `framework/frontend/app/src/services/sessionService.ts` (nuevo)

**Funciones:**
```typescript
import { apiClient } from './apiClient'
import type { ChildSession } from './sessionService'

export async function getActiveSessions(familyId: number): Promise<ChildSession[]> {
  const response = await apiClient.get<{ data: ChildSession[] }>(
    `/sessions/children?familyId=${familyId}`
  )
  return response.data.data
}

export async function expelSession(sessionId: number): Promise<boolean> {
  try {
    await apiClient.delete(`/sessions/children/${sessionId}/expel`)
    return true
  } catch (error) {
    console.error('Error al expulsar sesión:', error)
    return false
  }
}
```

**Criterios de aceptación:**
- `getActiveSessions` realiza GET correctamente.
- `expelSession` realiza DELETE correctamente.
- Manejo de errores (0, 400, 401, 404, 5xx).
- TypeScript compila sin errores.

---

### Tarea 26.6: Añadir funciones a `familyService.ts` — verified

**Descripción:** Añadir las 4 funciones nuevas para gestión de perfiles infantiles.

**Archivo:** `framework/frontend/app/src/services/familyService.ts`

**Funciones nuevas:**
```typescript
export async function getChild(id: number): Promise<ChildProfileExtended> {
  const response = await apiClient.get<{ data: ChildProfileExtended }>(
    `/family/children/${id}`
  )
  return response.data.data
}

export async function updateChild(
  id: number,
  request: UpdateChildProfileRequest
): Promise<ChildProfileExtended> {
  const response = await apiClient.put<{ data: ChildProfileExtended }>(
    `/family/children/${id}`,
    request
  )
  return response.data.data
}

export async function deleteChild(id: number): Promise<boolean> {
  try {
    await apiClient.delete(`/family/children/${id}`)
    return true
  } catch (error) {
    console.error('Error al eliminar perfil:', error)
    return false
  }
}

export async function toggleChildActivation(id: number): Promise<boolean> {
  try {
    await apiClient.put(`/family/children/activation/${id}`)
    return true
  } catch (error) {
    console.error('Error al cambiar estado de activación:', error)
    return false
  }
}
```

**Criterios de aceptación:**
- Las 4 funciones están implementadas.
- Manejo de errores (0, 400, 401, 404, 5xx).
- TypeScript compila sin errores.

---

### Tarea 26.7: Actualizar `createChild` en `familyService.ts` — verified

**Descripción:** Actualizar la función `createChild` para usar los nuevos nombres de campos.

**Archivo:** `framework/frontend/app/src/services/familyService.ts`

**Función actualizada:**
```typescript
export async function createChild(request: CreateChildRequest): Promise<ChildProfileExtended> {
  const response = await apiClient.post<{ data: ChildProfileExtended }>(
    '/family/children',
    request
  )
  return response.data.data
}
```

**Nota:** La función ya existe, solo se actualiza para usar `CreateChildRequest` con los nuevos campos.

**Criterios de aceptación:**
- La función usa `CreateChildRequest` con los nuevos campos.
- TypeScript compila sin errores.

---

### Tarea 26.8: Crear tipos de color vision — verified

**Descripción:** Crear enum/constante con los 9 valores de `colorVisionMode`.

**Archivo:** `framework/frontend/app/src/types/colorVision.ts` (nuevo)

**Contenido:**
```typescript
export enum ColorVisionMode {
  NONE = 'NONE',
  PROTANOPIA = 'PROTANOPIA',
  PROTANOMALY = 'PROTANOMALY',
  DEUTERANOPIA = 'DEUTERANOPIA',
  DEUTERANOMALY = 'DEUTERANOMALY',
  TRITANOPIA = 'TRITANOPIA',
  TRITANOMALY = 'TRITANOMALY',
  ACHROMATOMALY = 'ACHROMATOMALY',
  ACHROMATOPSIA = 'ACHROMATOPSIA'
}

export const COLOR_VISION_LABELS: Record<ColorVisionMode, string> = {
  [ColorVisionMode.NONE]: 'Sin ajuste',
  [ColorVisionMode.PROTANOPIA]: 'Protanopia',
  [ColorVisionMode.PROTANOMALY]: 'Protanomalía',
  [ColorVisionMode.DEUTERANOPIA]: 'Deuteranopia',
  [ColorVisionMode.DEUTERANOMALY]: 'Deuteranomalía',
  [ColorVisionMode.TRITANOPIA]: 'Tritanopia',
  [ColorVisionMode.TRITANOMALY]: 'Tritanomalía',
  [ColorVisionMode.ACHROMATOMALY]: 'Acromatomalía',
  [ColorVisionMode.ACHROMATOPSIA]: 'Acromatopsia'
}
```

**Criterios de aceptación:**
- El enum tiene 9 valores.
- Las etiquetas están en español.
- TypeScript compila sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/services/familyService.ts` | Actualizar interfaces y funciones |
| `framework/frontend/app/src/services/sessionService.ts` | Nuevo archivo |
| `framework/frontend/app/src/types/colorVision.ts` | Nuevo archivo |

## Estimación

- **Duración:** 0.5 días
- **Complejidad:** Baja
- **Riesgo:** Bajo (solo tipos y servicios, sin UI)

## Criterios de aceptación del sprint

1. Las interfaces TypeScript reflejan fielmente los contratos YAML actualizados.
2. `sessionService.ts` realiza GET y DELETE correctamente.
3. `familyService.ts` incluye las 4 funciones nuevas con manejo de errores.
4. `createChild` envía los campos renombrados.
5. El enum `ColorVisionMode` tiene 9 valores con etiquetas en español.
6. TypeScript compila sin errores (`tsc`).

## Evidencias esperadas

- `tsc` sin errores.
- Test manual: `getActiveSessions(familyId)` devuelve sesiones con `startedAt`.
- Test manual: `expelSession(sessionId)` invoca DELETE correctamente.
- Test manual: `toggleChildActivation(id)` invoca PUT sin body.
- Test manual: `getChild(id)` devuelve perfil con campos renombrados.
- Test manual: `updateChild(id, request)` envía payload parcial correctamente.
- Test manual: `deleteChild(id)` invoca DELETE correctamente.

## Dependencias bloqueantes de backend

- [ ] Renombrado de `ttsEnabled` → `npcVoiceEnabled` y `agentEnabled` → `npcEnabled` en los tres esquemas de perfil infantil.
- [ ] Añadir `npcVoiceVolume: integer` a los tres esquemas de perfil infantil.
- [ ] Ampliar enum `colorVisionMode` con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.
- [ ] Documentar semántica toggle en `activate-children.yaml`.
- [ ] Confirmar respuesta de `get-session-children.yaml` con `startedAt`.

## Handoffs a otras capas

### Backend debe:
1. **Completar Sprint B1 y B2** de la propuesta técnica backend antes de iniciar este sprint.
2. **Verificar** que los endpoints funcionan correctamente con los nuevos contratos.
3. **Confirmar** que la migración de base de datos se ha ejecutado correctamente.

### Agents/TTS:
- Sin dependencia directa. Los ajustes individuales se aplican via backend.

## Notas adicionales

### Estado del sprint

Este sprint está **BLOQUEADO** hasta que backend complete los cambios de contrato y modelo. No se iniciará la implementación hasta confirmación de backend.

### Orden de ejecución

Este sprint es prerrequisito para:
- SPRINT-027 (Cuadrícula de perfiles, sesiones y acciones)
- SPRINT-028 (Edición de perfil y accesibilidad visual)
- SPRINT-029 (Extracción de stepper y refactorización de registro)

---

## Verificación

- **Fecha:** 2026-07-31
- **Fecha de verificación final:** 2026-07-31
- **Veredicto:** APPROVED
- **Reviewer:** frontend
- **Evidencia:** `vue-tsc --noEmit` — 0 errores en archivos del sprint. Los 24 errores preexistentes son ajenos al SPRINT-026.

### Estado de tareas

| Tarea | Estado | Observaciones |
|-------|--------|---------------|
| 26.1 | ✅ VERIFIED | Interfaz correcta + alias `ChildProfile = ChildProfileExtended` añadido |
| 26.2 | ✅ VERIFIED | Coincide con `create-child-profile-request.yaml` |
| 26.3 | ✅ VERIFIED | Coincide con `update-child-profile-request.yaml` |
| 26.4 | ✅ VERIFIED | Coincide con `child-session-response.yaml` |
| 26.5 | ✅ VERIFIED | `getActiveSessions` y `expelSession` correctos |
| 26.6 | ✅ VERIFIED | 4 funciones implementadas con manejo de errores |
| 26.7 | ✅ VERIFIED | Función correcta + caller en `ChildSelectionModal.vue` actualizado |
| 26.8 | ✅ VERIFIED | Enum con 9 valores y etiquetas en español |

### Defectos encontrados y resueltos

| ID | Severidad | Archivo | Estado | Resolución |
|----|-----------|---------|--------|------------|
| D-026-1 | CRITICAL | `ChildProfileCard.vue:23` | ✅ RESOLVED | Alias `ChildProfile` añadido en `familyService.ts:20-21` |
| D-026-2 | CRITICAL | `ChildSelectionModal.vue:180` | ✅ RESOLVED | Alias `ChildProfile` añadido en `familyService.ts:20-21` |
| D-026-3 | CRITICAL | `ChildSelectionModal.vue:390` | ✅ RESOLVED | Llamada a `createChild` actualizada con `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume: 100` |
