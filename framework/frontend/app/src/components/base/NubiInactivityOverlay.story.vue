<script setup lang="ts">
import { ref } from 'vue'
import NubiInactivityOverlay from './NubiInactivityOverlay.vue'

const visible = ref(false)
const timeLeft = ref(60)
</script>

<template>
  <Story title="Base/NubiInactivityOverlay">
    <Variant title="Basic">
      <div>
        <button @click="visible = true">Mostrar overlay</button>
        <NubiInactivityOverlay 
          :visible="visible" 
          :time-left="timeLeft"
          @extend="() => { alert('Extender sesión'); visible = false }"
          @logout="() => { alert('Cerrar sesión'); visible = false }"
        />
      </div>
    </Variant>
    
    <Variant title="Different Times">
      <div style="display: flex; gap: 8px;">
        <button @click="() => { timeLeft = 120; visible = true }">2 min</button>
        <button @click="() => { timeLeft = 60; visible = true }">1 min</button>
        <button @click="() => { timeLeft = 30; visible = true }">30 sec</button>
      </div>
      <NubiInactivityOverlay 
        :visible="visible" 
        :time-left="timeLeft"
        @extend="() => { alert('Extender sesión'); visible = false }"
        @logout="() => { alert('Cerrar sesión'); visible = false }"
      />
    </Variant>
  </Story>
</template>

<docs>
## NubiInactivityOverlay

Overlay de inactividad antes del logout automático.

### Características
- Aparece antes del logout automático
- Cuenta atrás visual
- Permite extender sesión o cerrar
- Teleport al body
- Animación de entrada/salida

### Uso
```vue
<NubiInactivityOverlay 
  :visible="showOverlay" 
  :time-left="60"
  @extend="handleExtend"
  @logout="handleLogout"
/>
```
</docs>
