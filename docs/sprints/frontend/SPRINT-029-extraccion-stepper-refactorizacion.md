# SPRINT-029 — Extracción de stepper de registro y refactorización

## Estado

- **Estado:** bloqueado
- **Fecha de creación:** 2026-07-31
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-026 (Infraestructura de contratos), SPRINT-027 (Cuadrícula de perfiles), Backend (endpoints operativos)
- **Impacto estimado:** Componente reutilizable de registro de niños, integración en vista de Niños, refactorización de ChildSelectionModal sin regresiones

## Objetivo

Extraer la lógica de registro de niños a `ChildRegistrationStepper.vue` reutilizable, integrarlo en `NinosView.vue` y refactorizar `ChildSelectionModal.vue` para consumirlo sin introducir regresiones en el flujo existente de FEAT-003.

## Contexto

Tras los sprints SPRINT-026, SPRINT-027 y SPRINT-028, el frontend dispone de:
- Interfaces TypeScript actualizadas (`CreateChildRequest` con campos renombrados)
- Servicio `familyService.ts` con `createChild()` actualizado
- Vista `NinosView.vue` con cuadrícula funcional y botón «Registrar niño» (placeholder)
- Vista `ChildProfileEditView.vue` con edición completa

**Ahora se necesita:**
- Extraer la lógica de registro de `ChildSelectionModal.vue` a un componente reutilizable
- Integrar el stepper en `NinosView.vue` para el botón «Registrar niño»
- Refactorizar `ChildSelectionModal.vue` para consumir el nuevo componente
- Verificar que no hay regresiones en el flujo de alta desde Home
- Verificar que el alta desde Niños funciona correctamente

**Referencias:**
- Propuesta técnica frontend: `docs/product/design/frontend/FEAT-006-propuesta-tecnica-frontend.md`
- FEAT-006: `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
- ADR-022: `docs/product/decisions/ADR-022-Gestion-parental-de-perfiles-infantiles.md`
- FEAT-003: `docs/product/features/frontend/FEAT-003-Seleccion-y-alta-de-perfiles-infantiles.md`

**Dependencias de producto:**
- FEAT-003 (Selección y alta de perfiles infantiles)

## Tareas

### Tarea 29.1: Crear `ChildRegistrationStepper.vue`

**Descripción:** Extraer la lógica de registro de niños de `ChildSelectionModal.vue` a un componente reutilizable.

**Archivo:** `framework/frontend/app/src/components/ninos/ChildRegistrationStepper.vue` (nuevo)

**Props:**
```typescript
interface Props {
  familyId: number
}
```

**Emits:**
```typescript
interface Emits {
  childCreated: [profile: ChildProfileExtended]
  cancel: []
}
```

**Contenido:**
- Usa `NubiStepper` con 2 pasos: nombre → fecha de nacimiento + avatar.
- Lógica de creación existente en `ChildSelectionModal.vue:379-414`.
- Al crear exitosamente: emite `childCreated` con el nuevo perfil.

**Estructura:**
```vue
<template>
  <div class="child-registration-stepper">
    <NubiStepper :steps="steps" v-model:current-step="currentStep">
      <!-- Paso 1: Nombre -->
      <template #step-1>
        <div class="child-registration-stepper__step">
          <h3>{{ t('views.ninos.stepper.step1.title') }}</h3>
          <NubiTextInput
            v-model="name"
            :label="t('views.ninos.stepper.step1.nameLabel')"
            :placeholder="t('views.ninos.stepper.step1.namePlaceholder')"
          />
        </div>
      </template>
      
      <!-- Paso 2: Fecha de nacimiento + Avatar -->
      <template #step-2>
        <div class="child-registration-stepper__step">
          <h3>{{ t('views.ninos.stepper.step2.title') }}</h3>
          <NubiDateInput
            v-model="birthday"
            :label="t('views.ninos.stepper.step2.birthdayLabel')"
            :max="today"
          />
          <AvatarSelector
            v-model="avatar"
            :label="t('views.ninos.stepper.step2.avatarLabel')"
          />
        </div>
      </template>
    </NubiStepper>
    
    <div class="child-registration-stepper__actions">
      <NubiButton
        v-if="currentStep > 1"
        variant="secondary"
        @click="currentStep--"
      >
        {{ t('common.back') }}
      </NubiButton>
      
      <NubiButton
        v-if="currentStep < steps.length"
        :disabled="!canAdvance"
        @click="currentStep++"
      >
        {{ t('common.next') }}
      </NubiButton>
      
      <NubiButton
        v-if="currentStep === steps.length"
        :disabled="!canSubmit || creating"
        :loading="creating"
        @click="handleCreate"
      >
        {{ t('views.ninos.stepper.createButton') }}
      </NubiButton>
      
      <NubiButton
        variant="ghost"
        @click="$emit('cancel')"
      >
        {{ t('common.cancel') }}
      </NubiButton>
    </div>
  </div>
</template>
```

**Lógica:**
```typescript
const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const steps = [
  { title: t('views.ninos.stepper.step1.title') },
  { title: t('views.ninos.stepper.step2.title') }
]

const currentStep = ref(1)
const name = ref('')
const birthday = ref('')
const avatar = ref('')
const creating = ref(false)

const canAdvance = computed(() => {
  if (currentStep.value === 1) {
    return name.value.trim().length > 0
  }
  return true
})

const canSubmit = computed(() => {
  return name.value.trim().length > 0 && 
         birthday.value.length > 0 && 
         avatar.value.length > 0
})

async function handleCreate() {
  creating.value = true
  try {
    const request: CreateChildRequest = {
      name: name.value.trim(),
      birthday: birthday.value,
      avatar: avatar.value,
      npcVoiceEnabled: true,
      npcEnabled: true,
      npcVoiceVolume: 100,
      colorVisionMode: 'NONE'
    }
    
    const newProfile = await createChild(request)
    emit('childCreated', newProfile)
  } catch (error) {
    toast.error(t('views.ninos.stepper.createError'))
  } finally {
    creating.value = false
  }
}
```

**Criterios de aceptación:**
- El stepper funciona como componente independiente.
- Paso 1: nombre obligatorio.
- Paso 2: fecha de nacimiento y avatar obligatorios.
- Botón «Crear» envía los campos renombrados (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`).
- Al crear exitosamente, emite `childCreated` con el nuevo perfil.
- TypeScript compila sin errores.

---

### Tarea 29.2: Integrar stepper en `NinosView.vue`

**Descripción:** Integrar `ChildRegistrationStepper` en `NinosView.vue` para el botón «Registrar niño».

**Archivo:** `framework/frontend/app/src/views/parental/NinosView.vue`

**Cambios:**
```vue
<!-- Modal de registro -->
<NubiModal v-if="showRegisterModal" @close="closeRegisterModal">
  <ChildRegistrationStepper
    :family-id="familyId"
    @child-created="handleChildCreated"
    @cancel="closeRegisterModal"
  />
</NubiModal>
```

**Lógica:**
```typescript
const showRegisterModal = ref(false)

function openRegisterModal() {
  showRegisterModal.value = true
}

function closeRegisterModal() {
  showRegisterModal.value = false
}

function handleChildCreated(profile: ChildProfileExtended) {
  toast.success(t('views.ninos.registerSuccess', { name: profile.name }))
  closeRegisterModal()
  // Recargar cuadrícula
  reloadProfiles()
}
```

**Criterios de aceptación:**
- Botón «Registrar niño» abre el modal con el stepper.
- Creación exitosa → toast de éxito → recarga de cuadrícula.
- Cancelar → cierra el modal sin crear.
- TypeScript compila sin errores.

---

### Tarea 29.3: Refactorizar `ChildSelectionModal.vue`

**Descripción:** Sustituir la lógica inline de registro en `ChildSelectionModal.vue` por `ChildRegistrationStepper`.

**Archivo:** `framework/frontend/app/src/components/home/ChildSelectionModal.vue`

**Cambios:**
- Eliminar la lógica inline de registro (líneas 64-110, 379-414).
- Importar y usar `ChildRegistrationStepper`.
- Mantener el flujo existente de selección de perfil para jugar.

**Antes:**
```vue
<template>
  <!-- Lógica inline de registro -->
  <div v-if="showRegistration">
    <NubiTextInput v-model="newName" />
    <NubiDateInput v-model="newBirthday" />
    <AvatarSelector v-model="newAvatar" />
    <NubiButton @click="handleCreate">Crear</NubiButton>
  </div>
</template>

<script setup>
// Lógica inline de creación
async function handleCreate() {
  const request = {
    name: newName.value,
    birthday: newBirthday.value,
    avatar: newAvatar.value,
    ttsEnabled: true,
    agentEnabled: true,
    colorVisionMode: null
  }
  await createChild(request)
  // ...
}
</script>
```

**Después:**
```vue
<template>
  <ChildRegistrationStepper
    v-if="showRegistration"
    :family-id="familyId"
    @child-created="handleChildCreated"
    @cancel="showRegistration = false"
  />
</template>

<script setup>
function handleChildCreated(profile: ChildProfileExtended) {
  toast.success(t('views.home.childCreated', { name: profile.name }))
  showRegistration.value = false
  // Recargar perfiles
  reloadProfiles()
}
</script>
```

**Criterios de aceptación:**
- El flujo de alta desde Home funciona igual que antes.
- No hay regresiones en la selección de perfil para jugar.
- El stepper se ve y funciona igual que antes.
- TypeScript compila sin errores.

---

### Tarea 29.4: Actualizar `createChild` en el stepper

**Descripción:** Asegurar que el stepper usa los nuevos campos renombrados al crear un perfil.

**Archivo:** `framework/frontend/app/src/components/ninos/ChildRegistrationStepper.vue`

**Lógica:**
```typescript
async function handleCreate() {
  const request: CreateChildRequest = {
    name: name.value.trim(),
    birthday: birthday.value,
    avatar: avatar.value,
    npcVoiceEnabled: true,    // Nuevo nombre
    npcEnabled: true,          // Nuevo nombre
    npcVoiceVolume: 100,       // Nuevo campo
    colorVisionMode: 'NONE'    // Valor por defecto
  }
  
  const newProfile = await createChild(request)
  emit('childCreated', newProfile)
}
```

**Criterios de aceptación:**
- El stepper envía los campos renombrados.
- Los valores por defecto son coherentes (voz y NPC activados, volumen 100, sin ajuste visual).
- TypeScript compila sin errores.

---

### Tarea 29.5: Verificar regresión en Home

**Descripción:** Verificar que el alta desde `ChildSelectionModal` (Home) funciona sin regresiones.

**Flujo a verificar:**
1. Entrar en Home sin perfiles → aparece modal de selección.
2. Pulsar «Registrar niño» → abre stepper.
3. Completar stepper → crear perfil.
4. Perfil creado → modal muestra nuevo perfil → seleccionar para jugar.

**Criterios de aceptación:**
- El flujo completo funciona sin errores.
- No hay regresiones en el flujo existente de FEAT-003.
- TypeScript compila sin errores.

---

### Tarea 29.6: Verificar alta desde Niños

**Descripción:** Verificar que el alta desde `NinosView` funciona correctamente.

**Flujo a verificar:**
1. Entrar en Niños → aparece cuadrícula.
2. Pulsar «Registrar niño» → abre modal con stepper.
3. Completar stepper → crear perfil.
4. Perfil creado → toast de éxito → cuadrícula recargada con nuevo perfil.

**Criterios de aceptación:**
- El flujo completo funciona sin errores.
- La cuadrícula se recarga automáticamente tras crear un perfil.
- TypeScript compila sin errores.

---

### Tarea 29.7: i18n completo

**Descripción:** Implementar todas las traducciones en español para el stepper de registro.

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts`

**Claves i18n:**
```typescript
{
  views: {
    ninos: {
      stepper: {
        step1: {
          title: 'Nombre del niño',
          nameLabel: 'Nombre',
          namePlaceholder: 'Introduce el nombre'
        },
        step2: {
          title: 'Fecha de nacimiento y avatar',
          birthdayLabel: 'Fecha de nacimiento',
          avatarLabel: 'Selecciona un avatar'
        },
        createButton: 'Crear perfil',
        createError: 'No se pudo crear el perfil'
      },
      registerSuccess: '{name} registrado correctamente'
    }
  },
  common: {
    back: 'Atrás',
    next: 'Siguiente',
    cancel: 'Cancelar'
  }
}
```

**Criterios de aceptación:**
- Todas las etiquetas del stepper están traducidas.
- Los mensajes de error son claros y comprensibles.
- TypeScript compila sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/components/ninos/ChildRegistrationStepper.vue` | Nuevo archivo |
| `framework/frontend/app/src/views/parental/NinosView.vue` | Integrar stepper |
| `framework/frontend/app/src/components/home/ChildSelectionModal.vue` | Refactorizar para usar stepper |
| `framework/frontend/app/src/i18n/locales/es.ts` | Añadir traducciones |

## Estimación

- **Duración:** 1.5 días
- **Complejidad:** Media
- **Riesgo:** Medio (refactorización con riesgo de regresión en Home)

## Criterios de aceptación del sprint

1. `ChildRegistrationStepper.vue` funciona como componente independiente.
2. Alta desde Home (`ChildSelectionModal`) funciona sin regresiones.
3. Alta desde Niños (`NinosView`) crea perfil y recarga cuadrícula.
4. `createChild` envía campos renombrados (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`).
5. TypeScript compila sin errores (`tsc`).

## Evidencias esperadas

- Test manual: alta desde Home → perfil creado → visible en selección.
- Test manual: alta desde Niños → perfil creado → visible en cuadrícula.
- Test manual: ambos flujos usan el mismo stepper visual.
- `tsc` sin errores.
- Verificación de que no hay regresión en el flujo existente de FEAT-003.

## Dependencias bloqueantes de backend

- [ ] SPRINT-026 completado (contratos de creación actualizados con campos renombrados).
- [ ] Endpoint `POST /api/v1/family/children` operativo con `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`.

## Handoffs a otras capas

### Backend debe:
1. **Completar Sprint B1 y B2** de la propuesta técnica backend.
2. **Verificar** que el endpoint de creación funciona correctamente con los nuevos campos.

### Agents/TTS:
- Sin dependencia directa. Los valores por defecto (voz y NPC activados) son coherentes con la configuración global.

## Notas adicionales

### Estado del sprint

Este sprint está **BLOQUEADO** hasta que backend complete los cambios de contrato y modelo, y hasta que SPRINT-026 y SPRINT-027 estén completados.

### Orden de ejecución

- **Depende de:** SPRINT-026, SPRINT-027
- **Es el último sprint** de FEAT-006 en frontend.

### Privacidad infantil

- El stepper no solicita datos personales más allá de nombre, fecha de nacimiento y avatar.
- La fecha de nacimiento se usa únicamente para adecuar la experiencia jugable.
- Los valores por defecto (voz y NPC activados) son coherentes con la configuración global familiar.

### Riesgos identificados

| Riesgo | Mitigación |
|--------|-----------|
| `ChildRegistrationStepper` extraído introduce regresión en Home | Refactorización en sprint separado (S29) con verificación explícita de ambos consumidores (Home y Niños). |
| Incoherencia visual entre ambos flujos de alta | El mismo componente se usa en ambos contextos. |
| Valores por defecto no coherentes con config global | Los valores por defecto son coherentes (voz y NPC activados, volumen 100). El backend aplica ceiling si la config global lo requiere. |

### Diagrama de flujo de alta

```
[Home sin perfiles]
  → ChildSelectionModal
  → Pulsar "Registrar niño"
  → ChildRegistrationStepper
  → Completar pasos
  → Crear perfil
  → Emit childCreated
  → Recargar perfiles
  → Seleccionar perfil para jugar

[Niños]
  → Pulsar "Registrar niño"
  → NubiModal con ChildRegistrationStepper
  → Completar pasos
  → Crear perfil
  → Emit childCreated
  → Toast de éxito
  → Recargar cuadrícula
```
