<script setup lang="ts">
import { ref } from 'vue'
import NubiTabs from './NubiTabs.vue'

const activeTab = ref('tab1')

const tabs = [
  { value: 'tab1', label: 'General' },
  { value: 'tab2', label: 'Seguridad' },
  { value: 'tab3', label: 'Notificaciones' }
]

const tabsWithIcons = [
  { value: 'tab1', label: 'Perfil', icon: 'user' },
  { value: 'tab2', label: 'Seguridad', icon: 'shield' },
  { value: 'tab3', label: 'Ajustes', icon: 'settings' }
]
</script>

<template>
  <Story title="Base/NubiTabs">
    <Variant title="Basic">
      <NubiTabs v-model="activeTab" :tabs="tabs">
        <template #tab1>
          <p>Contenido de la pestaña General</p>
        </template>
        <template #tab2>
          <p>Contenido de la pestaña Seguridad</p>
        </template>
        <template #tab3>
          <p>Contenido de la pestaña Notificaciones</p>
        </template>
      </NubiTabs>
    </Variant>
    
    <Variant title="With Icons">
      <NubiTabs v-model="activeTab" :tabs="tabsWithIcons">
        <template #tab1>
          <p>Contenido del perfil</p>
        </template>
        <template #tab2>
          <p>Contenido de seguridad</p>
        </template>
        <template #tab3>
          <p>Contenido de ajustes</p>
        </template>
      </NubiTabs>
    </Variant>
  </Story>
</template>

<docs>
## NubiTabs

Navegación entre subsecciones con indicador visual.

### Uso
```vue
<NubiTabs v-model="activeTab" :tabs="tabs">
  <template #tab1>Contenido 1</template>
  <template #tab2>Contenido 2</template>
</NubiTabs>
```
</docs>
