<script setup lang="ts">
import { ref } from 'vue'
import NubiSidebar from './NubiSidebar.vue'
import type { SidebarSection } from './NubiSidebar.vue'

const isCollapsed = ref(false)

const sections: SidebarSection[] = [
  {
    id: 'panel',
    title: 'Panel',
    items: [
      { label: 'Configuración', icon: 'settings', to: '/panel/settings' },
      { label: 'Niños', icon: 'users', to: '/panel/children' },
      { label: 'Chatbot', icon: 'message-circle', to: '/panel/chatbot' },
      { label: 'Documentación', icon: 'book-open', to: '/docs' }
    ]
  },
  {
    id: 'experiences',
    title: 'Experiencias',
    items: [
      { label: 'Lectura en familia', icon: 'book', to: '/panel/reading' },
      { label: 'Relajación en familia', icon: 'moon', to: '/panel/relaxation' }
    ]
  }
]
</script>

<template>
  <Story title="Base/NubiSidebar">
    <Variant title="Expanded">
      <div style="height: 500px; border: 1px solid #ccc; overflow: hidden;">
        <NubiSidebar v-model:collapsed="isCollapsed" :sections="sections" />
      </div>
    </Variant>
    
    <Variant title="Collapsed">
      <div style="height: 500px; border: 1px solid #ccc; overflow: hidden;">
        <NubiSidebar v-model:collapsed="isCollapsed" :sections="sections" :collapsed="true" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiSidebar

Menú lateral de navegación con colapso y responsive.

### Uso
```vue
<NubiSidebar :sections="sections" v-model:collapsed="isCollapsed" />
```

### Secciones
```ts
const sections: SidebarSection[] = [
  {
    id: 'panel',
    title: 'Panel',
    items: [
      { label: 'Configuración', icon: 'settings', to: '/settings' },
      { label: 'Niños', icon: 'users', to: '/children' }
    ]
  }
]
```
</docs>
