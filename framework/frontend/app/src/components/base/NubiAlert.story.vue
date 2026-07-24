<script setup lang="ts">
import { ref } from 'vue'
import NubiAlert from './NubiAlert.vue'

const showInfo = ref(true)
const showWarning = ref(true)
const showError = ref(true)
const showSuccess = ref(true)
</script>

<template>
  <Story title="Base/NubiAlert">
    <Variant title="Types">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 500px;">
        <NubiAlert type="info" title="Información" message="Este es un mensaje informativo." />
        <NubiAlert type="warning" title="Advertencia" message="Ten cuidado con esta acción." />
        <NubiAlert type="error" title="Error" message="Ha ocurrido un error inesperado." />
        <NubiAlert type="success" title="Éxito" message="La operación se completó correctamente." />
      </div>
    </Variant>
    
    <Variant title="Without Title">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 500px;">
        <NubiAlert type="info" message="Mensaje sin título." />
        <NubiAlert type="success" message="Operación completada." />
      </div>
    </Variant>
    
    <Variant title="Not Dismissible">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 500px;">
        <NubiAlert type="info" title="Persistente" message="Esta alerta no se puede cerrar." :dismissible="false" />
      </div>
    </Variant>
    
    <Variant title="Dismissible">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 500px;">
        <NubiAlert v-if="showInfo" type="info" title="Info" message="Ciérrame haciendo clic en la X." @dismiss="showInfo = false" />
        <NubiAlert v-if="showWarning" type="warning" title="Warning" message="Ciérrame." @dismiss="showWarning = false" />
        <NubiAlert v-if="showError" type="error" title="Error" message="Ciérrame." @dismiss="showError = false" />
        <NubiAlert v-if="showSuccess" type="success" title="Success" message="Ciérrame." @dismiss="showSuccess = false" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiAlert

Mensaje persistente hasta cierre manual.

### Uso
```vue
<NubiAlert type="info" title="Info" message="Mensaje informativo" />
<NubiAlert type="error" title="Error" message="Mensaje de error" />
<NubiAlert type="success" :dismissible="false" message="No se puede cerrar" />
```
</docs>
