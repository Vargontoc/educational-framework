<script setup lang="ts">
import { ref } from 'vue'
import NubiList from './NubiList.vue'
import NubiAvatar from './NubiAvatar.vue'

const items = ref([
  'Elemento 1',
  'Elemento 2',
  'Elemento 3'
])

const complexItems = ref([
  { name: 'Ana García', email: 'ana@example.com' },
  { name: 'Pedro López', email: 'pedro@example.com' },
  { name: 'María Rodríguez', email: 'maria@example.com' }
])
</script>

<template>
  <Story title="Base/NubiList">
    <Variant title="Basic">
      <div style="max-width: 400px;">
        <NubiList :items="items" />
      </div>
    </Variant>
    
    <Variant title="Bordered">
      <div style="max-width: 400px;">
        <NubiList :items="items" :bordered="true" />
      </div>
    </Variant>
    
    <Variant title="Clickable">
      <div style="max-width: 400px;">
        <NubiList :items="items" :bordered="true" :clickable="true" @item-click="(item) => alert(item)" />
      </div>
    </Variant>
    
    <Variant title="Custom Slot">
      <div style="max-width: 400px;">
        <NubiList :items="complexItems" :bordered="true">
          <template #item="{ item }">
            <div style="display: flex; align-items: center; gap: 12px;">
              <NubiAvatar :name="item.name" size="sm" />
              <div>
                <div style="font-weight: 600;">{{ item.name }}</div>
                <div style="font-size: 14px; color: var(--nubi-text-secondary);">{{ item.email }}</div>
              </div>
            </div>
          </template>
        </NubiList>
      </div>
    </Variant>
    
    <Variant title="Empty">
      <div style="max-width: 400px;">
        <NubiList :items="[]" :bordered="true" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiList

Lista de elementos apilados verticalmente.

### Características
- Elementos con separadores sutiles
- Slot personalizado por item
- Variante bordered con borde exterior
- Items clickeables opcionales
- Estado vacío personalizable

### Uso
```vue
<NubiList :items="['Item 1', 'Item 2']" />

<NubiList :items="users" :bordered="true" :clickable="true">
  <template #item="{ item }">
    <div>{{ item.name }}</div>
  </template>
</NubiList>
```
</docs>
