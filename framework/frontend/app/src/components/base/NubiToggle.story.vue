<script setup lang="ts">
import { ref } from 'vue'
import NubiToggle from './NubiToggle.vue'

const toggleOn = ref(true)
const toggleOff = ref(false)
</script>

<template>
  <Story title="Base/NubiToggle">
    <Variant title="Basic">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiToggle v-model="toggleOn" label="Notificaciones" />
        <NubiToggle v-model="toggleOff" label="Modo oscuro" />
      </div>
    </Variant>
    
    <Variant title="With Status">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiToggle v-model="toggleOn" label="Sonido" :show-status="true" />
        <NubiToggle v-model="toggleOff" label="Vibración" :show-status="true" />
      </div>
    </Variant>
    
    <Variant title="States">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiToggle :model-value="false" label="Off" />
        <NubiToggle :model-value="true" label="On" />
        <NubiToggle :model-value="false" label="Disabled Off" disabled />
        <NubiToggle :model-value="true" label="Disabled On" disabled />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiToggle

Interruptor on/off para configuraciones frecuentes.

### Características
- Animación suave (200ms)
- Soporte v-model
- Estado visible opcional
- Accesibilidad WCAG AA (role="switch")
- Objetivo táctil mínimo 48x48dp

### Uso
```vue
<NubiToggle v-model="notifications" label="Notificaciones" />
<NubiToggle v-model="darkMode" label="Modo oscuro" :show-status="true" />
```
</docs>
