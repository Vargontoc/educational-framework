<script setup lang="ts">
import { ref } from 'vue'
import NubiInfoModal from './NubiInfoModal.vue'
import NubiButton from './NubiButton.vue'

const showModal1 = ref(false)
const showModal2 = ref(false)
const showModal3 = ref(false)
</script>

<template>
  <Story title="Base/NubiInfoModal">
    <Variant title="Basic">
      <div>
        <NubiButton variant="primary" @click="showModal1 = true">Abrir modal</NubiButton>
        
        <NubiInfoModal
          v-model="showModal1"
          title="Información"
          message="Este es un modal informativo con contenido de ayuda para el usuario."
        />
      </div>
    </Variant>
    
    <Variant title="With Slot Content">
      <div>
        <NubiButton variant="secondary" @click="showModal2 = true">Abrir con contenido</NubiButton>
        
        <NubiInfoModal v-model="showModal2" title="Ayuda">
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <p>Este modal tiene contenido personalizado usando slots.</p>
            <p>Puedes incluir cualquier contenido HTML dentro del modal.</p>
            <ul style="padding-left: 20px;">
              <li>Elemento 1</li>
              <li>Elemento 2</li>
              <li>Elemento 3</li>
            </ul>
          </div>
        </NubiInfoModal>
      </div>
    </Variant>
    
    <Variant title="Long Content (Scroll)">
      <div>
        <NubiButton variant="secondary" @click="showModal3 = true">Contenido largo</NubiButton>
        
        <NubiInfoModal v-model="showModal3" title="Términos y condiciones">
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <p v-for="i in 20" :key="i">
              Párrafo {{ i }}: Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
              Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
              Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.
            </p>
          </div>
        </NubiInfoModal>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiInfoModal

Modal informativo con scroll interno para contenido extenso.

### Uso
```vue
<NubiInfoModal v-model="isOpen" title="Ayuda" message="Contenido de ayuda" />

<NubiInfoModal v-model="isOpen" title="Términos">
  <p>Contenido personalizado con scroll</p>
</NubiInfoModal>
```
</docs>
