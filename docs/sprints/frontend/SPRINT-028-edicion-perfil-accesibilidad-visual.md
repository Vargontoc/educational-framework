# SPRINT-028 — Edición de perfil individual y accesibilidad visual

## Estado

- **Estado:** bloqueado
- **Fecha de creación:** 2026-07-31
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-026 (Infraestructura de contratos), Backend (endpoints operativos)
- **Impacto estimado:** Vista funcional de edición de perfil con datos básicos, ajustes de audio/NPC individuales, accesibilidad visual (8 perfiles + NONE), y acciones guardar/eliminar/dashboard

## Objetivo

Implementar `ChildProfileEditView.vue` con edición de datos básicos (nombre, fecha de nacimiento, avatar), ajustes de audio/NPC individuales condicionados por configuración global familiar, sección de accesibilidad visual con 8 perfiles + NONE, y acciones guardar/eliminar/dashboard.

## Contexto

Tras el SPRINT-026, el frontend dispone de:
- Interfaces TypeScript actualizadas (`ChildProfileExtended`, `UpdateChildProfileRequest`)
- Servicio `familyService.ts` con `getChild()`, `updateChild()`, `deleteChild()`
- Enum `ColorVisionMode` con 9 valores y etiquetas

**Ahora se necesita:**
- Vista de edición con breadcrumb «Niños > [Nombre]»
- Formulario de datos básicos (nombre, fecha de nacimiento, avatar)
- Sección de ajustes de audio/NPC individuales con estado deshabilitado por config global
- Sección de accesibilidad visual con toggle y 8 perfiles + NONE
- Acciones guardar/eliminar/dashboard
- Vista dashboard placeholder
- i18n completo y accesibilidad

**Referencias:**
- Propuesta técnica frontend: `docs/product/design/frontend/FEAT-006-propuesta-tecnica-frontend.md`
- FEAT-006: `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
- ADR-022: `docs/product/decisions/ADR-022-Gestion-parental-de-perfiles-infantiles.md`

**Dependencias de producto:**
- FEAT-005 (Configuración global de audio, NPC y PIN)
- ADR-021 (Configuración global de audio, NPC y PIN)

## Tareas

### Tarea 28.1: Crear composable `useChildProfileEdit`

**Descripción:** Crear composable para gestionar el estado de edición de perfil.

**Archivo:** `framework/frontend/app/src/composables/useChildProfileEdit.ts` (nuevo)

**Interfaz:**
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

**Criterios de aceptación:**
- Carga de perfil funciona correctamente.
- Draft se inicializa con los valores del perfil.
- `hasChanges` detecta cambios correctamente.
- `saveChanges` envía solo campos modificados.
- `deleteProfile` invoca DELETE correctamente.
- Detección de ajustes deshabilitados por config global funciona.
- TypeScript compila sin errores.

---

### Tarea 28.2: Añadir rutas de edición y dashboard

**Descripción:** Añadir las rutas de edición y dashboard al router.

**Archivo:** `framework/frontend/app/src/router/index.ts`

**Rutas nuevas:**
```typescript
{
  path: 'ninos/:id',
  name: 'PanelNinoEdit',
  component: () => import('../views/parental/ChildProfileEditView.vue'),
  meta: { requiresParentalAuth: true }
},
{
  path: 'ninos/:id/dashboard',
  name: 'PanelNinoDashboard',
  component: () => import('../views/parental/ChildDashboardView.vue'),
  meta: { requiresParentalAuth: true }
}
```

**Criterios de aceptación:**
- Las rutas están registradas correctamente.
- Ambas rutas requieren autenticación parental.
- TypeScript compila sin errores.

---

### Tarea 28.3: Implementar `ChildProfileEditView.vue`

**Descripción:** Implementar la vista de edición de perfil con breadcrumb, datos básicos, ajustes de audio/NPC, accesibilidad visual y acciones.

**Archivo:** `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` (nuevo)

**Estructura:**
```vue
<template>
  <div class="child-profile-edit-view">
    <NubiBreadcrumb :items="breadcrumbItems" />
    
    <div v-if="loading" class="child-profile-edit-view__loading">
      <NubiLoadingIndicator />
    </div>
    
    <div v-else class="child-profile-edit-view__content">
      <!-- Sección 1: Datos básicos -->
      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.basicData.title') }}</h2>
        
        <NubiTextInput
          v-model="draft.name"
          :label="t('views.ninos.edit.sections.basicData.nameLabel')"
        />
        
        <NubiDateInput
          v-model="draft.birthday"
          :label="t('views.ninos.edit.sections.basicData.birthdayLabel')"
          :max="today"
        />
        
        <AvatarSelector
          v-model="draft.avatar"
          :label="t('views.ninos.edit.sections.basicData.avatarLabel')"
        />
      </section>
      
      <!-- Sección 2: Audio del NPC -->
      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.audio.title') }}</h2>
        
        <ToggleWithPercentage
          v-model:enabled="draft.npcVoiceEnabled"
          v-model:percentage="draft.npcVoiceVolume"
          :label="t('views.ninos.edit.sections.audio.voiceLabel')"
          :disabled="isNpcVoiceDisabledByFamily"
        />
        <p v-if="isNpcVoiceDisabledByFamily" class="child-profile-edit-view__disabled-hint">
          {{ t('views.ninos.edit.disabledByFamily') }}
        </p>
        
        <NubiToggle
          v-model="draft.npcEnabled"
          :label="t('views.ninos.edit.sections.audio.npcLabel')"
          :disabled="isNpcDisabledByFamily"
        />
        <p v-if="isNpcDisabledByFamily" class="child-profile-edit-view__disabled-hint">
          {{ t('views.ninos.edit.disabledByFamily') }}
        </p>
      </section>
      
      <!-- Sección 3: Accesibilidad visual -->
      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.visualAccessibility.title') }}</h2>
        
        <NubiToggle
          v-model="visualAccessibilityActive"
          :label="t('views.ninos.edit.sections.visualAccessibility.toggleLabel')"
        />
        
        <div v-if="visualAccessibilityActive" class="child-profile-edit-view__visual-options">
          <NubiSelect
            v-model="draft.colorVisionMode"
            :options="colorVisionOptions"
            :label="t('views.ninos.edit.sections.visualAccessibility.selectLabel')"
          />
          
          <!-- Ejemplos visuales -->
          <div class="child-profile-edit-view__visual-examples">
            <ColorVisionExamples :mode="draft.colorVisionMode" />
          </div>
          
          <!-- Aviso -->
          <p class="child-profile-edit-view__visual-warning">
            {{ t('views.ninos.edit.sections.visualAccessibility.warning') }}
          </p>
        </div>
      </section>
      
      <!-- Acciones -->
      <div class="child-profile-edit-view__actions">
        <NubiButton
          @click="handleSave"
          :disabled="!hasChanges || saving"
          :loading="saving"
        >
          {{ t('views.ninos.edit.saveButton') }}
        </NubiButton>
        
        <NubiButton
          variant="danger"
          @click="showDeleteModal = true"
        >
          {{ t('views.ninos.edit.deleteButton') }}
        </NubiButton>
        
        <NubiButton
          variant="secondary"
          @click="handleDashboard"
        >
          {{ t('views.ninos.edit.dashboardButton') }}
        </NubiButton>
      </div>
    </div>
    
    <!-- Modal de confirmación de eliminación -->
    <NubiConfirmModal
      v-if="showDeleteModal"
      :title="t('views.ninos.edit.deleteModal.title')"
      :message="t('views.ninos.edit.deleteModal.message', { name: profile?.name })"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>
```

**Lógica:**
```typescript
const route = useRoute()
const router = useRouter()
const childId = computed(() => Number(route.params.id))

const {
  profile,
  draft,
  loading,
  saving,
  hasChanges,
  isNpcVoiceDisabledByFamily,
  isNpcDisabledByFamily,
  loadProfile,
  saveChanges,
  deleteProfile
} = useChildProfileEdit()

// Cargar perfil al montar
onMounted(async () => {
  await loadProfile(childId.value)
})

// Breadcrumb
const breadcrumbItems = computed(() => [
  { label: t('views.ninos.title'), to: { name: 'PanelNinos' } },
  { label: profile.value?.name || '' }
])

// Accesibilidad visual
const visualAccessibilityActive = computed({
  get: () => draft.value.colorVisionMode !== 'NONE',
  set: (val) => {
    draft.value.colorVisionMode = val ? 'DEUTERANOPIA' : 'NONE'
  }
})

const colorVisionOptions = computed(() => 
  Object.entries(COLOR_VISION_LABELS).map(([value, label]) => ({ value, label }))
)

// Acciones
async function handleSave() {
  const success = await saveChanges()
  if (success) {
    toast.success(t('views.ninos.edit.saveSuccess'))
  } else {
    toast.error(t('views.ninos.edit.saveError'))
  }
}

const showDeleteModal = ref(false)

async function confirmDelete() {
  const success = await deleteProfile()
  if (success) {
    toast.success(t('views.ninos.edit.deleteSuccess'))
    router.push({ name: 'PanelNinos' })
  } else {
    toast.error(t('views.ninos.edit.deleteError'))
  }
  showDeleteModal.value = false
}

function cancelDelete() {
  showDeleteModal.value = false
}

function handleDashboard() {
  router.push({ name: 'PanelNinoDashboard', params: { id: childId.value } })
}
```

**Criterios de aceptación:**
- Edición muestra breadcrumb «Niños > [Nombre]».
- Se pueden modificar nombre, fecha de nacimiento y avatar.
- Ajustes `npcVoiceEnabled`+`npcVoiceVolume` y `npcEnabled` visibles y editables.
- Si config global deshabilita un ajuste, el control individual se muestra deshabilitado con etiqueta explicativa.
- Accesibilidad visual: toggle inactivo sin ajuste, activo con valor.
- «Guardar cambios» persiste los cambios via API.
- «Eliminar» pide confirmación; al cancelar, el perfil se conserva; al confirmar, se elimina y redirige.
- «Dashboard» navega a la vista placeholder.
- TypeScript compila sin errores.

---

### Tarea 28.4: Sección de accesibilidad visual

**Descripción:** Implementar la sección de accesibilidad visual con toggle, select de 8 perfiles + NONE, ejemplos visuales y aviso no médico.

**Componente `ColorVisionExamples`:**

**Archivo:** `framework/frontend/app/src/components/ninos/ColorVisionExamples.vue` (nuevo)

**Contenido:**
```vue
<template>
  <div class="color-vision-examples">
    <svg width="200" height="100" viewBox="0 0 200 100">
      <!-- Círculos y cuadrados de colores diferenciados por forma -->
      <circle cx="30" cy="50" r="20" :fill="getCircleColor1(mode)" />
      <rect x="70" y="30" width="40" height="40" :fill="getRectColor1(mode)" />
      <circle cx="140" cy="50" r="20" :fill="getCircleColor2(mode)" />
      <rect x="170" y="30" width="20" height="40" :fill="getRectColor2(mode)" />
    </svg>
  </div>
</template>
```

**Lógica de colores:**
- Los colores se ajustan según el modo de accesibilidad visual seleccionado.
- Las formas (círculos y cuadrados) proporcionan diferenciación adicional al color.
- No se muestran resultados, evaluaciones ni lenguaje diagnóstico.

**Aviso no médico:**
```typescript
// i18n
'views.ninos.edit.sections.visualAccessibility.warning': 
  'Esta configuración es orientativa. No es una sección médica ni diagnóstica. Ante dudas, consulta a un especialista.'
```

**Criterios de aceptación:**
- Toggle inactivo cuando `colorVisionMode === 'NONE'`.
- Toggle activo cuando `colorVisionMode !== 'NONE'`.
- Al desactivar toggle, `colorVisionMode` se establece en `'NONE'`.
- Select muestra 9 opciones (8 perfiles + NONE).
- Ejemplos visuales son simples (círculos/cuadrados).
- Aviso no médico visible.
- TypeScript compila sin errores.

---

### Tarea 28.5: Implementar `ChildDashboardView.vue`

**Descripción:** Implementar la vista placeholder de dashboard individual.

**Archivo:** `framework/frontend/app/src/views/parental/ChildDashboardView.vue` (nuevo)

**Estructura:**
```vue
<template>
  <div class="child-dashboard-view">
    <NubiBreadcrumb :items="breadcrumbItems" />
    
    <div class="child-dashboard-view__placeholder">
      <p>{{ t('views.ninos.dashboard.placeholder') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiBreadcrumb from '@/components/shared/NubiBreadcrumb.vue'

const { t } = useI18n()
const route = useRoute()

const breadcrumbItems = computed(() => [
  { label: t('views.ninos.title'), to: { name: 'PanelNinos' } },
  { label: route.params.name as string, to: { name: 'PanelNinoEdit', params: { id: route.params.id } } },
  { label: 'Dashboard' }
])
</script>
```

**Criterios de aceptación:**
- Breadcrumb muestra «Niños > [Nombre] > Dashboard».
- Contenido placeholder visible.
- TypeScript compila sin errores.

---

### Tarea 28.6: i18n completo

**Descripción:** Implementar todas las traducciones en español para la vista de edición.

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts`

**Claves i18n:**
```typescript
{
  views: {
    ninos: {
      edit: {
        title: 'Editar perfil',
        saveButton: 'Guardar cambios',
        deleteButton: 'Eliminar',
        dashboardButton: 'Dashboard',
        disabledByFamily: 'Deshabilitado a nivel familiar',
        saveSuccess: 'Cambios guardados correctamente',
        saveError: 'No se pudieron guardar los cambios',
        deleteSuccess: 'Perfil eliminado correctamente',
        deleteError: 'No se pudo eliminar el perfil',
        deleteModal: {
          title: 'Eliminar perfil',
          message: '¿Eliminar a {name}? Se eliminarán todos sus datos.'
        },
        sections: {
          basicData: {
            title: 'Datos básicos',
            nameLabel: 'Nombre',
            birthdayLabel: 'Fecha de nacimiento',
            avatarLabel: 'Avatar'
          },
          audio: {
            title: 'Audio del NPC',
            voiceLabel: 'Voz del NPC',
            npcLabel: 'NPC'
          },
          visualAccessibility: {
            title: 'Accesibilidad visual',
            toggleLabel: 'Activar ajuste visual',
            selectLabel: 'Perfil de visualización',
            warning: 'Esta configuración es orientativa. No es una sección médica ni diagnóstica. Ante dudas, consulta a un especialista.'
          }
        }
      },
      dashboard: {
        placeholder: 'Próximamente disponible'
      }
    }
  }
}
```

**Criterios de aceptación:**
- Todas las etiquetas, confirmaciones y mensajes están traducidos.
- El aviso no médico es claro y comprensible.
- TypeScript compila sin errores.

---

### Tarea 28.7: Accesibilidad

**Descripción:** Verificar que la vista cumple con los requisitos de accesibilidad.

**Requisitos:**
1. **Labels:** Todos los controles tienen etiquetas visibles o `aria-label`.
2. **Objetivos táctiles:** Todos los controles tienen objetivo ≥ 48dp.
3. **Estados visibles:** Los estados (activo, desactivado, disabled) son distinguibles sin depender solo del color.
4. **Contraste:** El contraste de texto cumple WCAG 2.1 AA (4.5:1).
5. **Navegación por teclado:** Todos los controles son accesibles por teclado.
6. **Ejemplos visuales:** No dependen exclusivamente del color (formas diferenciadas).

**Criterios de aceptación:**
- Todos los controles tienen etiquetas o aria-labels.
- Objetivos táctiles ≥ 48dp.
- Estados distinguibles sin solo color.
- Contraste WCAG 2.1 AA.
- Navegación por teclado funcional.
- Ejemplos visuales usan formas además de color.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/composables/useChildProfileEdit.ts` | Nuevo archivo |
| `framework/frontend/app/src/router/index.ts` | Añadir rutas de edición y dashboard |
| `framework/frontend/app/src/views/parental/ChildProfileEditView.vue` | Nuevo archivo |
| `framework/frontend/app/src/views/parental/ChildDashboardView.vue` | Nuevo archivo |
| `framework/frontend/app/src/components/ninos/ColorVisionExamples.vue` | Nuevo archivo |
| `framework/frontend/app/src/i18n/locales/es.ts` | Añadir traducciones |

## Estimación

- **Duración:** 2.5 días
- **Complejidad:** Media-Alta
- **Riesgo:** Medio (accesibilidad visual, ajustes deshabilitados por config global)

## Criterios de aceptación del sprint

1. Edición accesible desde tarjeta con breadcrumb «Niños > [Nombre]».
2. Se pueden modificar nombre, fecha de nacimiento y avatar.
3. Ajustes `npcVoiceEnabled`+`npcVoiceVolume` y `npcEnabled` visibles y editables.
4. Si config global deshabilita un ajuste, el control individual se muestra deshabilitado con etiqueta explicativa.
5. Accesibilidad visual: toggle inactivo sin ajuste, activo con valor. Se pueden seleccionar 8 perfiles + NONE.
6. Ejemplos visuales son simples (círculos/cuadrados) y no diagnósticos.
7. Aviso visible: «No es una sección médica ni diagnóstica. Ante dudas, consulta a un especialista.»
8. «Eliminar» requiere confirmación. Cancelar conserva el perfil. Confirmar elimina perfil y datos.
9. «Dashboard» muestra placeholder con breadcrumb «Niños > [Nombre] > Dashboard».
10. TypeScript compila sin errores (`tsc`).

## Evidencias esperadas

- Test manual: editar nombre → guardar → verificar en backend.
- Test manual: deshabilitar npcVoice global → ver ajuste individual bloqueado con etiqueta.
- Test manual: activar accesibilidad visual → seleccionar PROTANOMALY → guardar → verificar.
- Test manual: desactivar accesibilidad visual → guardar → colorVisionMode = NONE.
- Test manual: eliminar → confirmación → perfil eliminado → redirect a cuadrícula.
- Test manual: dashboard → placeholder visible.
- `tsc` sin errores.

## Dependencias bloqueantes de backend

- [ ] SPRINT-026 completado (contratos y servicios disponibles).
- [ ] Endpoint `PUT /family/children/:id` operativo con campos renombrados y `npcVoiceVolume`.
- [ ] Endpoint `DELETE /family/children/:id` operativo.
- [ ] Endpoint `GET /family/children/:id` operativo devolviendo campos renombrados.
- [ ] Enum `colorVisionMode` ampliado con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.

## Handoffs a otras capas

### Backend debe:
1. **Completar Sprint B1 y B2** de la propuesta técnica backend.
2. **Verificar** que los endpoints de edición y eliminación funcionan correctamente.
3. **Confirmar** que el enum `colorVisionMode` tiene los 9 valores.

### Agents/TTS:
- Sin dependencia directa. Los ajustes individuales se aplican via backend.

## Notas adicionales

### Estado del sprint

Este sprint está **BLOQUEADO** hasta que backend complete los cambios de contrato y modelo, y hasta que SPRINT-026 esté completado.

### Orden de ejecución

- **Depende de:** SPRINT-026
- **Puede ejecutarse en paralelo con:** SPRINT-027

### Privacidad infantil

- La vista no muestra datos sensibles de niños a terceros.
- Los ajustes de accesibilidad visual son opcionales y no clínicos.
- La fecha de nacimiento se usa únicamente para adecuar la experiencia jugable.
- La vista es exclusiva para adultos autenticados.

### Riesgos identificados

| Riesgo | Mitigación |
|--------|-----------|
| Nombres de perfiles visuales interpretados como diagnóstico | Aviso visible de no-diagnóstico. Ejemplos simples sin evaluación. |
| Ajustes individuales deshabilitados por configuración global: UX confusa | Etiqueta clara junto al control: «Deshabilitado a nivel familiar». |
| Edición con config global cambiada durante edición | Al guardar, re-validar contra config global actual. |
