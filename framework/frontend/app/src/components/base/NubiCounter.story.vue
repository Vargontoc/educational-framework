<script setup lang="ts">
import { ref } from 'vue'
import NubiCounter from './NubiCounter.vue'

const count = ref(0)
const animatedCount = ref(0)

function increment() {
  count.value++
  animatedCount.value += Math.floor(Math.random() * 10) + 1
}
</script>

<template>
  <Story title="Base/NubiCounter">
    <Variant title="Basic">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCounter :value="0" />
        <NubiCounter :value="5" />
        <NubiCounter :value="42" />
        <NubiCounter :value="1000" />
      </div>
    </Variant>
    
    <Variant title="With Icon">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCounter :value="5" icon="users" />
        <NubiCounter :value="12" icon="shopping-cart" />
        <NubiCounter :value="3" icon="star" />
      </div>
    </Variant>
    
    <Variant title="With Suffix">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCounter :value="5" suffix="usuarios" />
        <NubiCounter :value="12" suffix="items" />
        <NubiCounter :value="3" suffix="estrellas" />
      </div>
    </Variant>
    
    <Variant title="Animated">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <NubiCounter :value="animatedCount" :animated="true" icon="trending-up" suffix="puntos" />
        <button @click="increment">Incrementar</button>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiCounter

Muestra cantidad con modo estático o animado.

### Características
- Modo estático (valor fijo)
- Modo animado con transición numérica
- Icono opcional
- Sufijo opcional

### Uso
```vue
<NubiCounter :value="42" />
<NubiCounter :value="5" icon="users" suffix="usuarios" />
<NubiCounter :value="count" :animated="true" />
```
</docs>
