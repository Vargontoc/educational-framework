<script setup lang="ts">
import { ref } from 'vue'
import NubiGrid from './NubiGrid.vue'
import NubiCard from './NubiCard.vue'

const items = ref([
  { title: 'Card 1', description: 'Descripción 1' },
  { title: 'Card 2', description: 'Descripción 2' },
  { title: 'Card 3', description: 'Descripción 3' },
  { title: 'Card 4', description: 'Descripción 4' },
  { title: 'Card 5', description: 'Descripción 5' },
  { title: 'Card 6', description: 'Descripción 6' }
])
</script>

<template>
  <Story title="Base/NubiGrid">
    <Variant title="3 Columns">
      <NubiGrid :items="items" :cols="3">
        <template #item="{ item }">
          <NubiCard :title="item.title" :description="item.description" />
        </template>
      </NubiGrid>
    </Variant>
    
    <Variant title="2 Columns">
      <NubiGrid :items="items" :cols="2">
        <template #item="{ item }">
          <NubiCard :title="item.title" :description="item.description" />
        </template>
      </NubiGrid>
    </Variant>
    
    <Variant title="4 Columns">
      <NubiGrid :items="items" :cols="4">
        <template #item="{ item }">
          <NubiCard :title="item.title" :description="item.description" />
        </template>
      </NubiGrid>
    </Variant>
    
    <Variant title="Empty">
      <NubiGrid :items="[]" :cols="3" />
    </Variant>
  </Story>
</template>

<docs>
## NubiGrid

Cuadrícula responsive configurable.

### Características
- 1 columna en móvil (< 768px)
- 2 columnas en tablet (768px - 1024px)
- 3-4 columnas en desktop (> 1024px)
- Columnas configurables
- Slot personalizado por item

### Uso
```vue
<NubiGrid :items="items" :cols="3">
  <template #item="{ item }">
    <NubiCard :title="item.title" />
  </template>
</NubiGrid>
```
</docs>
