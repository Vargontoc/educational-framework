<script setup lang="ts">
import { ref } from 'vue'
import NubiRadioGroup from './NubiRadioGroup.vue'

const selectedColor = ref<string | number | null>(null)
const selectedSize = ref<string | number | null>('md')

const colorOptions = [
  { value: 'red', label: 'Rojo' },
  { value: 'green', label: 'Verde' },
  { value: 'blue', label: 'Azul' }
]

const sizeOptions = [
  { value: 'sm', label: 'Pequeño' },
  { value: 'md', label: 'Mediano' },
  { value: 'lg', label: 'Grande' }
]

const simpleOptions = ['Opción A', 'Opción B', 'Opción C']

const disabledOptions = [
  { value: 'a', label: 'Opción A' },
  { value: 'b', label: 'Opción B (deshabilitada)', disabled: true },
  { value: 'c', label: 'Opción C' }
]
</script>

<template>
  <Story title="Base/NubiRadioGroup">
    <Variant title="Vertical">
      <NubiRadioGroup v-model="selectedColor" :options="colorOptions" label="Color favorito" direction="vertical" />
    </Variant>
    
    <Variant title="Horizontal">
      <NubiRadioGroup v-model="selectedSize" :options="sizeOptions" label="Tamaño" direction="horizontal" />
    </Variant>
    
    <Variant title="Simple Options">
      <NubiRadioGroup :options="simpleOptions" label="Opciones simples" />
    </Variant>
    
    <Variant title="States">
      <div style="display: flex; flex-direction: column; gap: 24px;">
        <NubiRadioGroup :options="colorOptions" label="Normal" />
        <NubiRadioGroup :options="colorOptions" label="Disabled" disabled />
        <NubiRadioGroup :options="colorOptions" label="Con error" error="Selecciona una opción" />
        <NubiRadioGroup :options="colorOptions" label="Con hint" hint="Elige tu color preferido" />
      </div>
    </Variant>
    
    <Variant title="Disabled Options">
      <NubiRadioGroup :options="disabledOptions" label="Opciones parciales" />
    </Variant>
  </Story>
</template>

<docs>
## NubiRadioGroup

Grupo de radio buttons para selección única.

### Características
- Selección única entre opciones mutuamente excluyentes
- Label por opción
- Dirección configurable (vertical/horizontal)
- Soporte v-model
- Navegación por teclado (flechas)
- Accesibilidad WCAG AA

### Uso
```vue
<NubiRadioGroup v-model="color" :options="colorOptions" label="Color" />
<NubiRadioGroup :options="['A', 'B', 'C']" label="Opciones" direction="horizontal" />
```
</docs>
