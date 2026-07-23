<script setup lang="ts">
import { ref } from 'vue'
import NubiNumberInput from './NubiNumberInput.vue'

const basicValue = ref(5)
const rangedValue = ref(50)
const stepValue = ref(0)
</script>

<template>
  <Story title="Base/NubiNumberInput">
    <Variant title="Basic">
      <div style="max-width: 300px;">
        <NubiNumberInput v-model="basicValue" label="Cantidad" />
      </div>
    </Variant>
    
    <Variant title="With Range">
      <div style="max-width: 300px; display: flex; flex-direction: column; gap: 16px;">
        <NubiNumberInput v-model="rangedValue" label="Edad" :min="0" :max="100" />
        <NubiNumberInput label="Porcentaje" :min="0" :max="100" :step="5" />
      </div>
    </Variant>
    
    <Variant title="With Step">
      <div style="max-width: 300px;">
        <NubiNumberInput v-model="stepValue" label="Paso de 0.5" :step="0.5" :min="0" :max="10" />
      </div>
    </Variant>
    
    <Variant title="States">
      <div style="max-width: 300px; display: flex; flex-direction: column; gap: 16px;">
        <NubiNumberInput label="Normal" :model-value="10" />
        <NubiNumberInput label="Disabled" :model-value="10" disabled />
        <NubiNumberInput label="Con error" error="El valor no es válido" />
        <NubiNumberInput label="Con hint" hint="Introduce un número entre 1 y 10" />
      </div>
    </Variant>
    
    <Variant title="Validation">
      <div style="max-width: 300px; display: flex; flex-direction: column; gap: 16px;">
        <NubiNumberInput label="Mínimo 5" :min="5" :model-value="3" />
        <NubiNumberInput label="Máximo 10" :max="10" :model-value="15" />
        <NubiNumberInput label="Obligatorio" :required="true" :model-value="null" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiNumberInput

Campo de entrada numérico con botones de incremento/decremento.

### Características
- Teclado numérico en móvil
- Botones de incremento/decremento
- Validación de rango (min/max)
- Soporte v-model
- Navegación por teclado (flechas)
- Accesibilidad WCAG AA

### Uso
```vue
<NubiNumberInput v-model="age" label="Edad" :min="0" :max="100" />
<NubiNumberInput v-model="quantity" label="Cantidad" :step="5" />
```
</docs>
