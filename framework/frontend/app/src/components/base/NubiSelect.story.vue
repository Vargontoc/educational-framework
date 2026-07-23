<script setup lang="ts">
import { ref } from 'vue'
import NubiSelect from './NubiSelect.vue'

const selectedValue = ref<string | number | null>(null)
const selectedAge = ref<string | number | null>(null)

const colorOptions = [
  { value: 'red', label: 'Rojo' },
  { value: 'green', label: 'Verde' },
  { value: 'blue', label: 'Azul' },
  { value: 'yellow', label: 'Amarillo' }
]

const ageOptions = [
  { value: 3, label: '3 años' },
  { value: 4, label: '4 años' },
  { value: 5, label: '5 años' },
  { value: 6, label: '6 años' }
]

const simpleOptions = ['Opción A', 'Opción B', 'Opción C']
</script>

<template>
  <Story title="Base/NubiSelect">
    <Variant title="Basic">
      <div style="max-width: 300px;">
        <NubiSelect v-model="selectedValue" :options="colorOptions" label="Color favorito" placeholder="Selecciona un color" />
      </div>
    </Variant>
    
    <Variant title="Simple Options">
      <div style="max-width: 300px;">
        <NubiSelect :options="simpleOptions" label="Opciones simples" placeholder="Elige una" />
      </div>
    </Variant>
    
    <Variant title="States">
      <div style="max-width: 300px; display: flex; flex-direction: column; gap: 16px;">
        <NubiSelect :options="colorOptions" label="Normal" placeholder="Selecciona..." />
        <NubiSelect :options="colorOptions" label="Disabled" disabled placeholder="Deshabilitado" />
        <NubiSelect :options="colorOptions" label="Con error" error="Este campo es obligatorio" />
        <NubiSelect :options="colorOptions" label="Con hint" hint="Selecciona tu color preferido" />
        <NubiSelect :options="[]" label="Sin opciones" placeholder="No hay opciones" />
      </div>
    </Variant>
    
    <Variant title="Required">
      <div style="max-width: 300px;">
        <NubiSelect v-model="selectedAge" :options="ageOptions" label="Edad del niño" :required="true" placeholder="Selecciona edad" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiSelect

Selector de opción única con dropdown accesible.

### Características
- Selección única entre opciones
- Dropdown accesible con focus trapping
- Navegación por teclado completa
- Soporte v-model
- Accesibilidad WCAG AA

### Uso
```vue
<NubiSelect v-model="color" :options="colorOptions" label="Color" />
<NubiSelect :options="['A', 'B', 'C']" label="Opciones" />
```
</docs>
