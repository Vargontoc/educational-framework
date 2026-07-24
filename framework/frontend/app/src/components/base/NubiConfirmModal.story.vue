<script setup lang="ts">
import { ref } from 'vue'
import NubiConfirmModal from './NubiConfirmModal.vue'
import NubiButton from './NubiButton.vue'

const showModal1 = ref(false)
const showModal2 = ref(false)
const showModal3 = ref(false)
const lastAction = ref('')
</script>

<template>
  <Story title="Base/NubiConfirmModal">
    <Variant title="Basic">
      <div>
        <NubiButton variant="primary" @click="showModal1 = true">Abrir modal</NubiButton>
        <p v-if="lastAction">Última acción: {{ lastAction }}</p>
        
        <NubiConfirmModal
          v-model="showModal1"
          title="Confirmar acción"
          message="¿Estás seguro de que quieres continuar con esta acción?"
          @confirm="lastAction = 'Confirmado'"
          @cancel="lastAction = 'Cancelado'"
        />
      </div>
    </Variant>
    
    <Variant title="Destructive">
      <div>
        <NubiButton variant="destructive" @click="showModal2 = true">Eliminar elemento</NubiButton>
        
        <NubiConfirmModal
          v-model="showModal2"
          title="Eliminar elemento"
          message="Esta acción no se puede deshacer. ¿Estás seguro de que quieres eliminar este elemento?"
          confirm-text="Eliminar"
          confirm-variant="destructive"
          icon="trash-2"
        />
      </div>
    </Variant>
    
    <Variant title="With Icon">
      <div>
        <NubiButton variant="secondary" @click="showModal3 = true">Guardar cambios</NubiButton>
        
        <NubiConfirmModal
          v-model="showModal3"
          title="Guardar cambios"
          message="¿Quieres guardar los cambios realizados?"
          icon="save"
        />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiConfirmModal

Diálogo de confirmación con overlay oscuro.

### Uso
```vue
<NubiConfirmModal
  v-model="isOpen"
  title="Confirmar"
  message="¿Estás seguro?"
  @confirm="handleConfirm"
  @cancel="handleCancel"
/>
```
</docs>
