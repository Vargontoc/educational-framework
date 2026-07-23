<script setup lang="ts">
import { ref } from 'vue'
import NubiPinInput from './NubiPinInput.vue'

const pinValue = ref('')
const maskedPin = ref('')
</script>

<template>
  <Story title="Base/NubiPinInput">
    <Variant title="Basic">
      <NubiPinInput v-model="pinValue" label="Introduce tu PIN" />
    </Variant>
    
    <Variant title="Masked">
      <NubiPinInput v-model="maskedPin" label="PIN de seguridad" :masked="true" />
    </Variant>
    
    <Variant title="States">
      <div style="display: flex; flex-direction: column; gap: 32px; align-items: center;">
        <NubiPinInput label="Normal" model-value="" />
        <NubiPinInput label="Parcial" model-value="12" />
        <NubiPinInput label="Completado" model-value="1234" />
        <NubiPinInput label="Con error" model-value="1234" error="PIN incorrecto" />
        <NubiPinInput label="Disabled" model-value="1234" disabled />
      </div>
    </Variant>
    
    <Variant title="Custom Length">
      <NubiPinInput label="PIN de 6 dígitos" :pin-length="6" />
    </Variant>
  </Story>
</template>

<docs>
## NubiPinInput

Entrada de PIN numérico de 4 dígitos (configurable).

### Características
- Exactamente 4 dígitos numéricos
- Estilo teclado móvil
- Dígitos ocultos (modo masked)
- Feedback visual al completar
- Auto-avance entre campos
- Accesibilidad WCAG AA

### Uso
```vue
<NubiPinInput v-model="pin" label="Introduce tu PIN" />
<NubiPinInput v-model="pin" :masked="true" label="PIN de seguridad" />
```
</docs>
