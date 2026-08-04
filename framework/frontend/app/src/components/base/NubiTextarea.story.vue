<script setup lang="ts">
import { ref } from 'vue'
import NubiTextarea from './NubiTextarea.vue'

const defaultValue = ref('')
const withText = ref('Este es un mensaje de ejemplo para el textarea.')
const withError = ref('')
const disabledValue = ref('Texto deshabilitado')
const counterValue = ref('')
</script>

<template>
  <Story title="Base/NubiTextarea">
    <Variant title="Default">
      <div style="max-width: 480px;">
        <NubiTextarea v-model="defaultValue" label="Mensaje" placeholder="Escribe tu mensaje..." />
      </div>
    </Variant>

    <Variant title="Con texto">
      <div style="max-width: 480px;">
        <NubiTextarea v-model="withText" label="Mensaje" />
      </div>
    </Variant>

    <Variant title="Con error">
      <div style="max-width: 480px;">
        <NubiTextarea v-model="withError" label="Mensaje" error="Este campo tiene un error" />
      </div>
    </Variant>

    <Variant title="Disabled">
      <div style="max-width: 480px;">
        <NubiTextarea v-model="disabledValue" label="Mensaje" disabled />
      </div>
    </Variant>

    <Variant title="Con contador">
      <div style="max-width: 480px;">
        <NubiTextarea v-model="counterValue" label="Mensaje" :max-length="2000" placeholder="Escribe tu mensaje..." />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiTextarea

Campo de texto multilínea con contador de caracteres y validación.

### Características
- Label visible asociado al textarea
- Contador de caracteres si maxLength está definido
- Mensaje de error con role="alert"
- aria-invalid cuando hay error
- Estado disabled visual y funcional
- Objetivo táctil ≥ 48×48dp

### Uso
```vue
<NubiTextarea v-model="message" label="Mensaje" :max-length="2000" />
<NubiTextarea v-model="message" label="Mensaje" error="Error de validación" />
```
</docs>
