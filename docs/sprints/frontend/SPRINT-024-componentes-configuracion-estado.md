# SPRINT-024 — Componentes de configuración y estado local

## Estado

- **Estado:** closed
- **Fecha de creación:** 2026-07-30
- **Fecha de revisión:** 2026-07-30
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-023 (Infraestructura de contratos y cliente API)
- **Impacto estimado:** Base de componentes reutilizables y lógica de estado para configuración global

## Objetivo

Construir los componentes visuales reutilizables y la lógica de estado local (draft, conservación de valores, independencia de controles) para la vista de configuración global.

## Contexto

Tras el SPRINT-023, el frontend dispone de:
- Contratos con los 8 campos de configuración global (ya ampliados)
- Método `patch` en el cliente API
- Interfaces de tipos actualizadas
- Servicio `updateFamilyConfig` funcional

**Ahora se necesita:**
- Componentes visuales para las 5 secciones de configuración
- Lógica de estado local con conservación de valores
- Independencia entre controles (audio general, NPC, voces)
- Catálogo de componentes en Histoire para desarrollo y revisión

**Decisiones confirmadas:**
1. **Componente de porcentaje:** Usar componentes existentes, crear/actualizar si es necesario
2. **Lógica de conservación:** Al apagar un control con porcentaje, se conserva el último valor ≠ 0. Al reactivar, se restaura.
3. **Acción rápida:** Establecer porcentaje a 0 apaga el toggle automáticamente

### Componentes existentes reutilizables

Según el catálogo en `framework/frontend/app/src/components/base/`:

| Componente | Estado | Uso en SPRINT-024 |
|------------|--------|-------------------|
| `NubiToggle` | ✅ Existe | Usar directamente en `ToggleWithPercentage` |
| `NubiNumberInput` | ✅ Existe | Usar directamente en `ToggleWithPercentage` |
| `NubiCard` | ✅ Existe | Usar como base para `ConfigSection` |
| `NubiPinInput` | ✅ Existe | Usar en sección PIN (SPRINT-025) |
| `NubiButton` | ✅ Existe | Usar para «Guardar cambios» (SPRINT-025) |
| `NubiSlider` | ❌ No existe | Crear o usar `<input type="range">` nativo |

**Conclusión:** No es necesario crear `NubiToggle` ni `NubiNumberInput`. Solo falta decidir sobre `NubiSlider`.

## Tareas

### Tarea 24.1: Crear componente `ConfigSection`

**Descripción:** Wrapper reutilizable con título de sección, descripción breve y slot para controles. Se usará `NubiCard` como base para mantener coherencia con el sistema de diseño.

**Archivo:** `framework/frontend/app/src/components/config/ConfigSection.vue` (nuevo)

**Relación con componentes existentes:**
- ✅ Usa `NubiCard` como contenedor base
- ✅ Añade estructura de título y descripción sobre el card

**Props:**
```typescript
interface Props {
  title: string
  description?: string
  disabled?: boolean
}
```

**Slots:**
- `default`: contenido de la sección (controles)

**Estructura visual:**
```vue
<template>
  <NubiCard class="config-section" :class="{ 'config-section--disabled': disabled }">
    <template #header>
      <div class="config-section__header">
        <h3 class="config-section__title">{{ title }}</h3>
        <p v-if="description" class="config-section__description">{{ description }}</p>
      </div>
    </template>
    <div class="config-section__content">
      <slot />
    </div>
  </NubiCard>
</template>
```

**Estilos (Tailwind):**
- Título destacado (font-semibold, text-lg)
- Descripción en texto secundario (text-sm, text-gray-600)
- Estado disabled: opacidad reducida, pointer-events: none

**Criterios de aceptación:**
- El componente acepta `title`, `description` y `disabled` como props
- Usa `NubiCard` como contenedor base
- Renderiza correctamente el slot de contenido
- Los estilos son consistentes con el sistema de diseño Nubi
- El estado `disabled` aplica opacidad y bloquea interacción
- TypeScript compila sin errores
- Componente visible en Histoire

---

### Tarea 24.2: Crear componente `ToggleWithPercentage`

**Descripción:** Componente reutilizable que combina toggle on/off con control de porcentaje (slider + input numérico).

**Archivo:** `framework/frontend/app/src/components/config/ToggleWithPercentage.vue` (nuevo)

**Relación con componentes existentes:**
- ✅ Usa `NubiToggle` (ya existe en `components/base/NubiToggle.vue`)
- ✅ Usa `NubiNumberInput` (ya existe en `components/base/NubiNumberInput.vue`)
- ❌ Slider: usar `<input type="range">` nativo con estilos Tailwind (no crear `NubiSlider`)

**Props:**
```typescript
interface Props {
  modelEnabled: boolean
  modelPercentage: number
  label: string
  disabled?: boolean
  min?: number  // default: 0
  max?: number  // default: 100
  step?: number // default: 1
}
```

**Emits:**
```typescript
emit('update:enabled', value: boolean)
emit('update:percentage', value: number)
```

**Comportamiento:**
1. **Toggle on/off:** Cambia el estado del control (usando `NubiToggle`)
2. **Slider:** Ajusta el porcentaje (0-100) usando `<input type="range">` nativo
3. **Input numérico:** Permite ajuste preciso del porcentaje (usando `NubiNumberInput`)
4. **Acción rápida:** Si el usuario establece el porcentaje a 0, el toggle se apaga automáticamente
5. **Sincronización:** Slider e input están sincronizados bidireccionalmente

**Estructura visual:**
```vue
<template>
  <div class="toggle-with-percentage" :class="{ 'toggle-with-percentage--disabled': disabled }">
    <div class="toggle-with-percentage__header">
      <label class="toggle-with-percentage__label">{{ label }}</label>
      <NubiToggle
        :model-value="modelEnabled"
        @update:model-value="onToggleChange"
        :disabled="disabled"
      />
    </div>
    <div v-if="modelEnabled" class="toggle-with-percentage__controls">
      <input
        type="range"
        :min="min"
        :max="max"
        :step="step"
        :value="modelPercentage"
        @input="onSliderChange"
        class="toggle-with-percentage__slider w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
      />
      <NubiNumberInput
        :model-value="modelPercentage"
        @update:model-value="onNumberChange"
        :min="min"
        :max="max"
        :step="step"
        :disabled="disabled"
        class="toggle-with-percentage__input"
      />
    </div>
  </div>
</template>
```

**Lógica de eventos:**
```typescript
function onToggleChange(enabled: boolean) {
  emit('update:enabled', enabled)
}

function onSliderChange(event: Event) {
  const value = Number((event.target as HTMLInputElement).value)
  emit('update:percentage', value)
  if (value === 0) {
    emit('update:enabled', false) // Acción rápida de apagado
  }
}

function onNumberChange(value: number) {
  emit('update:percentage', value)
  if (value === 0) {
    emit('update:enabled', false) // Acción rápida de apagado
  }
}
```

**Accesibilidad:**
- Label asociado al toggle mediante `aria-labelledby`
- Slider con `aria-label` descriptivo
- Input numérico con `aria-label` descriptivo
- Objetivo táctil ≥ 48dp para todos los controles
- Estados visibles sin depender solo del color

**Criterios de aceptación:**
- El componente acepta todas las props definidas
- Usa `NubiToggle` y `NubiNumberInput` existentes
- Emite `update:enabled` y `update:percentage` correctamente
- Slider e input están sincronizados bidireccionalmente
- Establecer porcentaje a 0 apaga el toggle automáticamente
- El estado `disabled` bloquea toda la interacción
- Los controles son accesibles (labels, aria-labels, objetivos táctiles)
- TypeScript compila sin errores
- Componente visible en Histoire con múltiples estados

---

### Tarea 24.3: Implementar composable `useGlobalConfig`

**Descripción:** Composable que gestiona el estado local de configuración (draft, persisted, lastNonZero) y la lógica de conservación de valores.

**Archivo:** `framework/frontend/app/src/composables/useGlobalConfig.ts` (nuevo)

**Estado gestionado:**
```typescript
interface UseGlobalConfigReturn {
  // Estado
  persisted: Ref<FamilyGlobalConfig>
  draft: Ref<FamilyGlobalConfig>
  lastNonZero: Ref<{
    audioGeneralVolume: number
    npcVoiceVolume: number
    narrativeVoiceVolume: number
  }>
  
  // Acciones
  initialize: (data: FamilyData) => void
  onToggleChange: (section: keyof ToggleSections, enabled: boolean) => void
  onPercentageChange: (section: keyof PercentageSections, value: number) => void
  getModifiedFields: () => Partial<FamilyGlobalConfig>
  hasChanges: ComputedRef<boolean>
}
```

**Lógica de inicialización:**
```typescript
function initialize(data: FamilyData) {
  const config: FamilyGlobalConfig = {
    audioGeneralEnabled: data.audioGeneralEnabled ?? true,
    audioGeneralVolume: data.audioGeneralVolume ?? 100,
    npcEnabled: data.npcEnabled ?? true,
    npcVoiceEnabled: data.npcVoiceEnabled ?? true,
    npcVoiceVolume: data.npcVoiceVolume ?? 100,
    narrativeVoiceEnabled: data.narrativeVoiceEnabled ?? true,
    narrativeVoiceVolume: data.narrativeVoiceVolume ?? 100,
  }
  
  persisted.value = { ...config }
  draft.value = { ...config }
  
  // Inicializar lastNonZero con valores ≠ 0
  lastNonZero.value = {
    audioGeneralVolume: config.audioGeneralVolume !== 0 ? config.audioGeneralVolume : 100,
    npcVoiceVolume: config.npcVoiceVolume !== 0 ? config.npcVoiceVolume : 100,
    narrativeVoiceVolume: config.narrativeVoiceVolume !== 0 ? config.narrativeVoiceVolume : 100,
  }
}
```

**Lógica de conservación:**
```typescript
function onToggleChange(section: keyof ToggleSections, enabled: boolean) {
  draft.value[section + 'Enabled'] = enabled
  
  if (!enabled) {
    // Conservar valor actual si es ≠ 0
    const volumeKey = section + 'Volume' as keyof PercentageSections
    if (draft.value[volumeKey] !== 0) {
      lastNonZero.value[volumeKey] = draft.value[volumeKey]
    }
  } else {
    // Restaurar último valor ≠ 0
    const volumeKey = section + 'Volume' as keyof PercentageSections
    if (lastNonZero.value[volumeKey]) {
      draft.value[volumeKey] = lastNonZero.value[volumeKey]
    }
  }
}

function onPercentageChange(section: keyof PercentageSections, value: number) {
  draft.value[section + 'Volume'] = value
  
  if (value === 0) {
    // Acción rápida de apagado
    draft.value[section + 'Enabled'] = false
  } else {
    // Actualizar último valor ≠ 0
    lastNonZero.value[section] = value
  }
}
```

**Lógica de detección de cambios:**
```typescript
function getModifiedFields(): Partial<FamilyGlobalConfig> {
  const modified: Partial<FamilyGlobalConfig> = {}
  
  for (const key in draft.value) {
    if (draft.value[key] !== persisted.value[key]) {
      modified[key] = draft.value[key]
    }
  }
  
  return modified
}

const hasChanges = computed(() => {
  return Object.keys(getModifiedFields()).length > 0
})
```

**Criterios de aceptación:**
- El composable gestiona correctamente los tres estados (persisted, draft, lastNonZero)
- `initialize` carga datos desde API con valores por defecto si faltan
- `onToggleChange` conserva/restaura valores según las reglas
- `onPercentageChange` actualiza estado y aplica acción rápida si value === 0
- `getModifiedFields` retorna solo los campos modificados
- `hasChanges` indica si hay cambios pendientes
- TypeScript compila sin errores

---

### Tarea 24.4: Implementar lógica de conservación de valores

**Descripción:** Asegurar que la lógica de conservación del último valor ≠ 0 funciona correctamente en todos los escenarios.

**Escenarios a validar:**

1. **Apagar toggle con volumen ≠ 0:**
   - Volumen actual: 75
   - Usuario apaga toggle
   - `lastNonZero[section]` = 75
   - `draft[section + 'Volume']` se mantiene en 75 (no se cambia)

2. **Reactivar toggle:**
   - Toggle estaba apagado con `lastNonZero[section]` = 75
   - Usuario reactiva toggle
   - `draft[section + 'Volume']` se restaura a 75

3. **Establecer volumen a 0:**
   - Usuario mueve slider a 0
   - `draft[section + 'Volume']` = 0
   - `draft[section + 'Enabled']` = false (acción rápida)
   - `lastNonZero[section]` se mantiene en el valor anterior ≠ 0

4. **Cambiar volumen a valor ≠ 0:**
   - Usuario establece volumen a 50
   - `draft[section + 'Volume']` = 50
   - `lastNonZero[section]` = 50
   - `draft[section + 'Enabled']` se mantiene en true

**Pruebas manuales:**
```typescript
// Test 1: Apagar y reactivar
initialize({ audioGeneralEnabled: true, audioGeneralVolume: 75, ... })
onToggleChange('audioGeneral', false)
assert(draft.value.audioGeneralEnabled === false)
assert(lastNonZero.value.audioGeneralVolume === 75)

onToggleChange('audioGeneral', true)
assert(draft.value.audioGeneralEnabled === true)
assert(draft.value.audioGeneralVolume === 75)

// Test 2: Establecer a 0
onPercentageChange('audioGeneral', 0)
assert(draft.value.audioGeneralVolume === 0)
assert(draft.value.audioGeneralEnabled === false)
assert(lastNonZero.value.audioGeneralVolume === 75) // Se conserva

// Test 3: Cambiar a valor ≠ 0
onPercentageChange('audioGeneral', 50)
assert(draft.value.audioGeneralVolume === 50)
assert(lastNonZero.value.audioGeneralVolume === 50)
```

**Criterios de aceptación:**
- Todos los escenarios de conservación funcionan correctamente
- Los valores se conservan y restauran según las reglas
- La acción rápida (0 → apagar) funciona
- TypeScript compila sin errores

---

### Tarea 24.5: Implementar independencia de controles

**Descripción:** Asegurar que los controles de audio general, voz NPC y voz narrativa son completamente independientes.

**Reglas de independencia:**

1. **Audio general NO modifica voces:**
   - Cambiar audio general (on/off o volumen) NO afecta a voz NPC ni voz narrativa
   - Cada control tiene su propio estado y lógica

2. **NPC desactivado NO modifica voz NPC:**
   - Desactivar NPC no cambia el estado de voz NPC
   - La configuración de voz NPC se conserva para cuando se reactive el NPC

3. **Voz narrativa es independiente:**
   - Cambiar voz narrativa NO afecta a NPC ni a voz NPC
   - Cambiar NPC o voz NPC NO afecta a voz narrativa

**Pruebas manuales:**
```typescript
// Test 1: Audio general independiente
initialize({ audioGeneralEnabled: true, audioGeneralVolume: 100, npcVoiceEnabled: true, npcVoiceVolume: 100, ... })
onPercentageChange('audioGeneral', 50)
assert(draft.value.npcVoiceEnabled === true)
assert(draft.value.npcVoiceVolume === 100) // No cambia

// Test 2: NPC desactivado conserva voz NPC
initialize({ npcEnabled: true, npcVoiceEnabled: true, npcVoiceVolume: 75, ... })
onToggleChange('npc', false) // NPC es solo toggle, no tiene volumen
assert(draft.value.npcVoiceEnabled === true) // No cambia
assert(draft.value.npcVoiceVolume === 75) // No cambia

// Test 3: Voz narrativa independiente
onPercentageChange('narrativeVoice', 30)
assert(draft.value.npcVoiceEnabled === true)
assert(draft.value.npcVoiceVolume === 75) // No cambia
```

**Criterios de aceptación:**
- Los tres controles con porcentaje son ortogonales
- Cambiar uno no afecta a los otros
- La configuración se conserva correctamente
- TypeScript compila sin errores

---

### Tarea 24.6: Crear historias Histoire para componentes

**Descripción:** Crear historias en Histoire para `ConfigSection` y `ToggleWithPercentage` para desarrollo y revisión visual.

**Archivos:**
- `framework/frontend/app/src/components/config/ConfigSection.story.vue` (nuevo)
- `framework/frontend/app/src/components/config/ToggleWithPercentage.story.vue` (nuevo)

**Historias para `ConfigSection`:**
```vue
<template>
  <Story title="Config/ConfigSection">
    <Variant title="Default">
      <ConfigSection title="Audio general" description="Controla todo el sonido de la aplicación.">
        <div>Contenido de ejemplo</div>
      </ConfigSection>
    </Variant>
    <Variant title="Disabled">
      <ConfigSection title="Audio general" description="Controla todo el sonido de la aplicación." disabled>
        <div>Contenido de ejemplo</div>
      </ConfigSection>
    </Variant>
    <Variant title="Sin descripción">
      <ConfigSection title="Audio general">
        <div>Contenido de ejemplo</div>
      </ConfigSection>
    </Variant>
  </Story>
</template>
```

**Historias para `ToggleWithPercentage`:**
```vue
<template>
  <Story title="Config/ToggleWithPercentage">
    <Variant title="Activo al 100%">
      <ToggleWithPercentage
        v-model:enabled="enabled1"
        v-model:percentage="percentage1"
        label="Volumen"
      />
    </Variant>
    <Variant title="Activo al 50%">
      <ToggleWithPercentage
        v-model:enabled="enabled2"
        v-model:percentage="percentage2"
        label="Volumen"
      />
    </Variant>
    <Variant title="Desactivado">
      <ToggleWithPercentage
        v-model:enabled="enabled3"
        v-model:percentage="percentage3"
        label="Volumen"
      />
    </Variant>
    <Variant title="Disabled">
      <ToggleWithPercentage
        v-model:enabled="enabled4"
        v-model:percentage="percentage4"
        label="Volumen"
        disabled
      />
    </Variant>
  </Story>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const enabled1 = ref(true)
const percentage1 = ref(100)

const enabled2 = ref(true)
const percentage2 = ref(50)

const enabled3 = ref(false)
const percentage3 = ref(75)

const enabled4 = ref(true)
const percentage4 = ref(100)
</script>
```

**Criterios de aceptación:**
- Las historias son visibles en Histoire (`histoire:dev`)
- Muestran todos los estados relevantes (activo, desactivado, disabled, etc.)
- Los componentes son interactivos en las historias
- TypeScript compila sin errores

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/components/config/ConfigSection.vue` | Nuevo archivo (compone sobre `NubiCard`) |
| `framework/frontend/app/src/components/config/ToggleWithPercentage.vue` | Nuevo archivo (compone sobre `NubiToggle` + slider nativo + `NubiNumberInput`) |
| `framework/frontend/app/src/composables/useGlobalConfig.ts` | Nuevo archivo |
| `framework/frontend/app/src/components/config/ConfigSection.story.vue` | Nuevo archivo |
| `framework/frontend/app/src/components/config/ToggleWithPercentage.story.vue` | Nuevo archivo |

**Componentes existentes reutilizados (sin cambios):**
- `framework/frontend/app/src/components/base/NubiCard.vue`
- `framework/frontend/app/src/components/base/NubiToggle.vue`
- `framework/frontend/app/src/components/base/NubiNumberInput.vue`

## Estimación

- **Duración:** 1 día
- **Complejidad:** Media
- **Riesgo:** Medio (lógica de conservación de valores requiere pruebas exhaustivas)

## Criterios de aceptación del sprint

1. `ToggleWithPercentage` permite toggle on/off, ajuste de porcentaje 0-100, establece 0 = apaga toggle
2. Al apagar y reactivar, se recupera el último valor ≠ 0
3. `ConfigSection` muestra título, descripción y slot consistentes
4. `useGlobalConfig` inicializa desde datos de familia, gestiona draft y conservación
5. Los tres controles con porcentaje (audio general, voz NPC, voz narrativa) son independientes entre sí
6. Componentes visibles en Histoire (`histoire:dev`)
7. TypeScript compila sin errores

## Evidencias esperadas

- Historias Histoire funcionales para ambos componentes
- Test manual: secuencia toggle off → toggle on recupera volumen
- Test manual: poner slider a 0 → toggle se apaga
- Test manual: cambiar audio general no afecta a voces
- Test manual: desactivar NPC no afecta a voz NPC
- `tsc` compila sin errores
- `histoire:dev` muestra las historias correctamente

## Dependencias

- **SPRINT-023:** Requiere que los contratos y el cliente API estén listos
- **Componentes existentes reutilizados:**
  - `NubiToggle` → `components/base/NubiToggle.vue`
  - `NubiNumberInput` → `components/base/NubiNumberInput.vue`
  - `NubiCard` → `components/base/NubiCard.vue`

## Notas adicionales

### Componentes existentes reutilizados

Se han identificado los siguientes componentes existentes que se reutilizarán:
- ✅ `NubiToggle`: toggle on/off (usado en `ToggleWithPercentage`)
- ✅ `NubiNumberInput`: input numérico (usado en `ToggleWithPercentage`)
- ✅ `NubiCard`: contenedor base (usado en `ConfigSection`)

**Decisión:** No es necesario crear nuevos componentes base. Solo se crean los componentes de composición específicos para configuración:
- `ConfigSection`: composición sobre `NubiCard`
- `ToggleWithPercentage`: composición sobre `NubiToggle` + slider nativo + `NubiNumberInput`

### Slider nativo vs NubiSlider

**Decisión:** Usar `<input type="range">` nativo con estilos Tailwind en lugar de crear un componente `NubiSlider`.

**Razones:**
1. Pragmatismo: el slider nativo es suficiente para este caso de uso
2. Menos componentes que mantener
3. Estilos Tailwind permiten personalización visual sin crear componente
4. Si en el futuro se necesita un slider más complejo, se puede crear `NubiSlider` y migrar

**Estilos Tailwind para slider:**
```html
<input 
  type="range" 
  class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
/>
```

### Accesibilidad táctil

Todos los controles deben tener objetivos táctiles ≥ 48dp para ser usables en móvil y tableta. Esto es especialmente importante para:
- Toggle switch (`NubiToggle` ya cumple)
- Slider thumb (añadir padding o min-height)
- Input numérico (`NubiNumberInput` ya cumple)

### Estados visuales

Los estados (activo, desactivado, disabled) deben ser distinguibles sin depender solo del color:
- Usar iconos, texto o patrones visuales adicionales
- Ejemplo: toggle desactivado muestra icono de silencio además de cambio de color

---

## Revisión técnica (2026-07-30)

### Veredicto: APPROVED

### Evidencia de implementación

#### Tarea 24.1 — Crear componente ConfigSection ✅
- **Archivo creado:** `src/components/config/ConfigSection.vue`
- **Props implementadas:**
  - `title: string` (requerido)
  - `description?: string` (opcional)
  - `disabled?: boolean` (opcional, default: false)
- **Usa NubiCard como contenedor base** ✅
- **Slot para contenido** ✅
- **Estilos:**
  - Título con font-weight semibold y tamaño lg ✅
  - Descripción con tamaño sm y color secundario ✅
  - Estado disabled con opacidad 0.6 y pointer-events: none ✅
- **TypeScript compila sin errores** ✅

#### Tarea 24.2 — Crear componente ToggleWithPercentage ✅
- **Archivo creado:** `src/components/config/ToggleWithPercentage.vue`
- **Props implementadas:**
  - `modelEnabled: boolean` (requerido)
  - `modelPercentage: number` (requerido)
  - `label: string` (requerido)
  - `disabled?: boolean` (opcional)
  - `min?: number` (default: 0)
  - `max?: number` (default: 100)
  - `step?: number` (default: 1)
- **Emits implementados:**
  - `update:enabled` ✅
  - `update:percentage` ✅
- **Usa componentes existentes:**
  - `NubiToggle` ✅
  - `NubiNumberInput` ✅
  - Slider nativo `<input type="range">` ✅
- **Acción rápida:** Establecer porcentaje a 0 apaga el toggle automáticamente ✅
- **Sincronización bidireccional:** Slider e input sincronizados ✅
- **Accesibilidad:**
  - Labels con aria-label ✅
  - Slider con aria-valuenow, aria-valuemin, aria-valuemax ✅
  - Objetivo táctil ≥ 48px (min-height: 48px en slider) ✅
- **TypeScript compila sin errores** ✅

#### Tarea 24.3 — Implementar composable useGlobalConfig ✅
- **Archivo creado:** `src/composables/useGlobalConfig.ts`
- **Estado gestionado:**
  - `persisted: Ref<FamilyGlobalConfig>` ✅
  - `draft: Ref<FamilyGlobalConfig>` ✅
  - `lastNonZero: Ref<LastNonZeroVolumes>` ✅
  - `hasChanges: ComputedRef<boolean>` ✅
- **Funciones implementadas:**
  - `initialize(data: FamilyData)` ✅
  - `onToggleChange(section: PercentageSection, enabled: boolean)` ✅
  - `onToggleOnlyChange(section: ToggleOnlySection, enabled: boolean)` ✅
  - `onPercentageChange(section: PercentageSection, value: number)` ✅
  - `getModifiedFields(): Partial<FamilyGlobalConfig>` ✅
- **Lógica de inicialización:**
  - Carga datos desde FamilyData ✅
  - Aplica valores por defecto si faltan campos (?? true, ?? 100) ✅
  - Inicializa lastNonZero con valores ≠ 0 ✅
- **TypeScript compila sin errores** ✅

#### Tarea 24.4 — Implementar lógica de conservación de valores ✅
- **Escenario 1: Apagar toggle con volumen ≠ 0** ✅
  - Conserva el valor actual en lastNonZero si es ≠ 0
  - Mantiene el volumen en draft (no lo cambia)
- **Escenario 2: Reactivar toggle** ✅
  - Restaura el último valor ≠ 0 desde lastNonZero
- **Escenario 3: Establecer volumen a 0** ✅
  - Establece draft[section + 'Volume'] = 0
  - Establece draft[section + 'Enabled'] = false (acción rápida)
  - lastNonZero se mantiene en el valor anterior ≠ 0
- **Escenario 4: Cambiar volumen a valor ≠ 0** ✅
  - Actualiza draft[section + 'Volume']
  - Actualiza lastNonZero[section]
  - Mantiene enabled en true
- **TypeScript compila sin errores** ✅

#### Tarea 24.5 — Implementar independencia de controles ✅
- **Regla 1: Audio general NO modifica voces** ✅
  - `onPercentageChange('audioGeneral', value)` solo modifica audioGeneral
  - No afecta a npcVoice ni narrativeVoice
- **Regla 2: NPC desactivado NO modifica voz NPC** ✅
  - `onToggleOnlyChange('npc', false)` solo modifica npcEnabled
  - No afecta a npcVoiceEnabled ni npcVoiceVolume
- **Regla 3: Voz narrativa es independiente** ✅
  - `onPercentageChange('narrativeVoice', value)` solo modifica narrativeVoice
  - No afecta a npc ni npcVoice
- **Implementación explícita:** Cada sección se maneja por separado en if-else ✅
- **TypeScript compila sin errores** ✅

#### Tarea 24.6 — Crear historias Histoire para componentes ✅
- **Archivos creados:**
  - `src/components/config/ConfigSection.story.vue` ✅
  - `src/components/config/ToggleWithPercentage.story.vue` ✅
- **Historias de ConfigSection:**
  - Default ✅
  - Disabled ✅
  - Sin descripción ✅
  - Con controles ✅
- **Historias de ToggleWithPercentage:**
  - Activo al 100% ✅
  - Activo al 50% ✅
  - Desactivado ✅
  - Disabled ✅
  - Rango personalizado (0-50) ✅
  - Acción rápida - Probar poner a 0 ✅
- **Componentes interactivos en historias** ✅
- **TypeScript compila sin errores** ✅

### Criterios de aceptación del sprint

1. ✅ **ToggleWithPercentage permite toggle on/off, ajuste de porcentaje 0-100, establece 0 = apaga toggle**
   - Implementado en líneas 98-112
   - Acción rápida verificada

2. ✅ **Al apagar y reactivar, se recupera el último valor ≠ 0**
   - Implementado en líneas 127-162
   - Lógica de conservación verificada

3. ✅ **ConfigSection muestra título, descripción y slot consistentes**
   - Implementado en líneas 1-13
   - Usa NubiCard como base

4. ✅ **useGlobalConfig inicializa desde datos de familia, gestiona draft y conservación**
   - Implementado en líneas 101-121
   - Valores por defecto aplicados correctamente

5. ✅ **Los tres controles con porcentaje son independientes entre sí**
   - Implementado con if-else explícitos
   - Cada sección modifica solo sus propios campos

6. ✅ **Componentes visibles en Histoire**
   - Historias creadas con múltiples variantes
   - Componentes interactivos

7. ✅ **TypeScript compila sin errores**
   - `tsc` ejecutado sin errores
   - `vite build` exitoso (1.44s)

### Evidencias técnicas

**Build de producción:**
- TypeScript: ✅ Sin errores
- Vite build: ✅ Exitoso (1.44s)
- Tamaño total dist/: 0.47 MB
- Chunks generados correctamente

**Archivos creados:**
1. `src/components/config/ConfigSection.vue` - Wrapper reutilizable (81 líneas)
2. `src/components/config/ToggleWithPercentage.vue` - Componente de composición (200 líneas)
3. `src/composables/useGlobalConfig.ts` - Lógica de estado (254 líneas)
4. `src/components/config/ConfigSection.story.vue` - Historias Histoire (33 líneas)
5. `src/components/config/ToggleWithPercentage.story.vue` - Historias Histoire (78 líneas)

**Calidad del código:**
- Documentación JSDoc completa
- Tipos TypeScript estrictos
- Accesibilidad implementada (aria-labels, objetivos táctiles)
- Estilos con variables CSS del sistema de diseño
- Componentes reutilizan componentes existentes (NubiCard, NubiToggle, NubiNumberInput)

### Observaciones

**Implementación robusta:**
- La lógica de conservación de valores está bien estructurada
- La independencia de controles se garantiza con if-else explícitos
- Los componentes son accesibles y responsivos

**Diseño de componentes:**
- ConfigSection usa NubiCard como base, manteniendo coherencia con el sistema de diseño
- ToggleWithPercentage compone sobre NubiToggle y NubiNumberInput existentes
- Slider nativo con estilos personalizados es suficiente para el caso de uso

**Accesibilidad:**
- Labels asociados mediante aria-label
- Slider con aria-valuenow, aria-valuemin, aria-valuemax
- Objetivo táctil ≥ 48px en slider
- Estados visibles sin depender solo del color

**Historias Histoire completas:**
- Múltiples variantes para cada componente
- Componentes interactivos
- Variante específica para probar acción rápida (poner a 0)

### Conclusión

El sprint cumple con todos los objetivos de componentes y estado local. Los componentes ConfigSection y ToggleWithPercentage están implementados correctamente, reutilizando componentes existentes. El composable useGlobalConfig gestiona el estado local con lógica de conservación de valores e independencia de controles. Las historias de Histoire permiten desarrollo y revisión visual. TypeScript compila sin errores y el build es exitoso. La base de componentes para la vista de configuración global está completamente preparada.
