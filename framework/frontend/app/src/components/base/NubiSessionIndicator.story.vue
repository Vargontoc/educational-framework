<script setup lang="ts">
import { ref } from 'vue'
import NubiSessionIndicator from './NubiSessionIndicator.vue'

const timeLeft = ref(300) // 5 minutos
</script>

<template>
  <Story title="Base/NubiSessionIndicator">
    <Variant title="Normal">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 300px;">
        <NubiSessionIndicator :time-left="300" />
        <NubiSessionIndicator :time-left="180" />
        <NubiSessionIndicator :time-left="60" />
      </div>
    </Variant>
    
    <Variant title="Warning">
      <div style="display: flex; flex-direction: column; gap: 16px; max-width: 300px;">
        <NubiSessionIndicator :time-left="59" />
        <NubiSessionIndicator :time-left="30" />
        <NubiSessionIndicator :time-left="1" />
      </div>
    </Variant>
    
    <Variant title="Expired">
      <div style="max-width: 300px;">
        <NubiSessionIndicator :time-left="0" />
      </div>
    </Variant>
    
    <Variant title="Custom Label">
      <div style="max-width: 300px;">
        <NubiSessionIndicator :time-left="120" label="Sesión del panel" />
      </div>
    </Variant>
    
    <Variant title="Animated">
      <div style="max-width: 300px;">
        <NubiSessionIndicator :time-left="timeLeft" />
        <div style="margin-top: 16px; display: flex; gap: 8px;">
          <button @click="timeLeft = 300">5 min</button>
          <button @click="timeLeft = 60">1 min</button>
          <button @click="timeLeft = 0">Expirar</button>
        </div>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiSessionIndicator

Indicador de tiempo restante de sesión.

### Características
- Muestra tiempo restante antes de logout automático
- Aviso visual cuando queda poco tiempo (< 1 minuto)
- Estado expirado
- Icono dinámico según estado

### Uso
```vue
<NubiSessionIndicator :time-left="300" />
<NubiSessionIndicator :time-left="30" label="Sesión del panel" />
```
</docs>
