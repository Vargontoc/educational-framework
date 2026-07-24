<script setup lang="ts">
import { ref } from 'vue'
import NubiStepper from './NubiStepper.vue'

const currentStep = ref(0)
const steps = ['Datos personales', 'Configuración', 'Confirmación']
</script>

<template>
  <Story title="Base/NubiStepper">
    <Variant title="Basic">
      <div style="max-width: 600px;">
        <NubiStepper v-model="currentStep" :steps="steps">
          <template #default="{ currentStep: step }">
            <div style="padding: 24px; background: var(--nubi-bg-surface-secondary); border-radius: 8px;">
              <h3>Paso {{ step + 1 }}: {{ steps[step] }}</h3>
              <p>Contenido del paso {{ step + 1 }}</p>
            </div>
          </template>
        </NubiStepper>
      </div>
    </Variant>
    
    <Variant title="Starting at Step 2">
      <div style="max-width: 600px;">
        <NubiStepper v-model="1" :steps="steps">
          <template #default="{ currentStep: step }">
            <div style="padding: 24px; background: var(--nubi-bg-surface-secondary); border-radius: 8px;">
              <h3>Paso {{ step + 1 }}: {{ steps[step] }}</h3>
              <p>Contenido del paso {{ step + 1 }}</p>
            </div>
          </template>
        </NubiStepper>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiStepper

Navegación entre pasos de un formulario.

### Características
- Indicador visual de paso actual
- Pasos completados clickeables
- Navegación anterior/siguiente
- Slot para contenido del paso

### Uso
```vue
<NubiStepper v-model="currentStep" :steps="['Paso 1', 'Paso 2', 'Paso 3']">
  <template #default="{ currentStep }">
    <div>Contenido del paso {{ currentStep + 1 }}</div>
  </template>
</NubiStepper>
```
</docs>
