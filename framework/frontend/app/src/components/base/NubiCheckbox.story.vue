<script setup lang="ts">
import { ref } from 'vue'
import NubiCheckbox from './NubiCheckbox.vue'

const checked = ref(false)
const unchecked = ref(false)
const indeterminate = ref<boolean | 'indeterminate'>('indeterminate')
</script>

<template>
  <Story title="Base/NubiCheckbox">
    <Variant title="Basic">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCheckbox v-model="checked" label="Acepto los términos y condiciones" />
        <NubiCheckbox label="Opción sin v-model" />
      </div>
    </Variant>
    
    <Variant title="States">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCheckbox :model-value="false" label="Unchecked" />
        <NubiCheckbox :model-value="true" label="Checked" />
        <NubiCheckbox :model-value="'indeterminate'" label="Indeterminate" />
        <NubiCheckbox :model-value="false" label="Disabled" disabled />
        <NubiCheckbox :model-value="true" label="Checked & Disabled" disabled />
      </div>
    </Variant>
    
    <Variant title="Slot Content">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCheckbox v-model="checked">
          Label con <strong>contenido HTML</strong> personalizado
        </NubiCheckbox>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiCheckbox

Casilla de verificación con label claro.

### Características
- Opción binaria con label
- Estados checked/unchecked/indeterminate
- Soporte v-model
- Accesibilidad WCAG AA
- Objetivo táctil mínimo 48x48dp

### Uso
```vue
<NubiCheckbox v-model="accepted" label="Acepto los términos" />
<NubiCheckbox :model-value="'indeterminate'" label="Parcial" />
```
</docs>
