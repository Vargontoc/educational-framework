<script setup lang="ts">
import { ref } from 'vue'
import NubiProgressBar from './NubiProgressBar.vue'

const progress = ref(65)
</script>

<template>
  <Story title="Base/NubiProgressBar">
    <Variant title="Basic">
      <div style="max-width: 400px;">
        <NubiProgressBar :value="progress" label="Progreso" />
      </div>
    </Variant>
    
    <Variant title="Different Values">
      <div style="max-width: 400px; display: flex; flex-direction: column; gap: 16px;">
        <NubiProgressBar :value="0" label="0%" />
        <NubiProgressBar :value="25" label="25%" />
        <NubiProgressBar :value="50" label="50%" />
        <NubiProgressBar :value="75" label="75%" />
        <NubiProgressBar :value="100" label="100%" />
      </div>
    </Variant>
    
    <Variant title="Without Label">
      <div style="max-width: 400px;">
        <NubiProgressBar :value="progress" :show-label="false" />
      </div>
    </Variant>
    
    <Variant title="Animated">
      <div style="max-width: 400px;">
        <NubiProgressBar :value="progress" label="Progreso animado" />
        <div style="margin-top: 16px; display: flex; gap: 8px;">
          <button @click="progress = 0">0%</button>
          <button @click="progress = 50">50%</button>
          <button @click="progress = 100">100%</button>
        </div>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiProgressBar

Indicador visual de avance con animación suave.

### Características
- Animación suave al cambiar valor
- Label opcional con porcentaje
- Accesibilidad con role="progressbar"
- Valor clampado entre 0 y 100

### Uso
```vue
<NubiProgressBar :value="75" label="Progreso" />
<NubiProgressBar :value="50" :show-label="false" />
```
</docs>
