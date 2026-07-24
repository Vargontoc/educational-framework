<script setup lang="ts">
import NubiBreadcrumb from './NubiBreadcrumb.vue'

const items = [
  { label: 'Inicio', to: '/' },
  { label: 'Panel', to: '/panel' },
  { label: 'Configuración' }
]
</script>

<template>
  <Story title="Base/NubiBreadcrumb">
    <Variant title="Basic">
      <NubiBreadcrumb :items="items" />
    </Variant>
    
    <Variant title="Two Levels">
      <NubiBreadcrumb :items="[{ label: 'Inicio', to: '/' }, { label: 'Niños' }]" />
    </Variant>
    
    <Variant title="Single Level">
      <NubiBreadcrumb :items="[{ label: 'Inicio' }]" />
    </Variant>
  </Story>
</template>

<docs>
## NubiBreadcrumb

Migas de pan para navegación jerárquica.

### Uso
```vue
<NubiBreadcrumb :items="[
  { label: 'Inicio', to: '/' },
  { label: 'Panel', to: '/panel' },
  { label: 'Configuración' }
]" />
```
</docs>
