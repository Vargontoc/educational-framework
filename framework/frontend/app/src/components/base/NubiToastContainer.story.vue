<script setup lang="ts">
import NubiToastContainer from './NubiToastContainer.vue'
import NubiButton from './NubiButton.vue'
import { useToast } from '../../composables/useToast'

const toast = useToast()

function showSuccess() {
  toast.success('Operación completada correctamente')
}

function showError() {
  toast.error('Ha ocurrido un error inesperado')
}

function showWarning() {
  toast.warning('Ten cuidado con esta acción')
}

function showInfo() {
  toast.info('Este es un mensaje informativo')
}

function showMultiple() {
  toast.info('Primer mensaje')
  toast.success('Segundo mensaje')
  toast.warning('Tercer mensaje')
}
</script>

<template>
  <Story title="Base/NubiToast">
    <Variant title="Types">
      <div style="display: flex; gap: 16px; flex-wrap: wrap;">
        <NubiButton variant="primary" @click="showSuccess">Success</NubiButton>
        <NubiButton variant="destructive" @click="showError">Error</NubiButton>
        <NubiButton variant="secondary" @click="showWarning">Warning</NubiButton>
        <NubiButton variant="secondary" @click="showInfo">Info</NubiButton>
      </div>
    </Variant>
    
    <Variant title="Multiple">
      <NubiButton variant="primary" @click="showMultiple">Mostrar 3 toasts</NubiButton>
    </Variant>
    
    <NubiToastContainer position="top-right" />
  </Story>
</template>

<docs>
## NubiToast

Notificaciones temporales no intrusivas.

### Uso
```vue
<script setup>
import { useToast } from '@/composables/useToast'
const toast = useToast()
toast.success('Operación completada')
toast.error('Ha ocurrido un error')
</script>

<template>
  <NubiToastContainer position="top-right" />
</template>
```
</docs>
