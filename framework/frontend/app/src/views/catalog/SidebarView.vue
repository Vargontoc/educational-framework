<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.sidebars') }}</h2>
      <p class="section-description">
        Menú lateral de navegación con colapso, responsive y secciones configurables.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Expandido</h3>
        <div class="demo-sidebar-container">
          <NubiSidebar :sections="sections" :collapsed="false" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Colapsado</h3>
        <div class="demo-sidebar-container">
          <NubiSidebar :sections="sections" :collapsed="true" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Interactivo</h3>
        <p class="demo-hint">Usa el botón de la esquina superior derecha del sidebar para colapsar/expandir.</p>
        <div class="demo-sidebar-container">
          <NubiSidebar v-model:collapsed="interactiveCollapsed" :sections="sections" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiSidebar from '../../components/base/NubiSidebar.vue'
import type { SidebarSection } from '../../components/base/NubiSidebar.vue'

const interactiveCollapsed = ref(false)

const sections: SidebarSection[] = [
  {
    id: 'panel',
    title: 'Panel',
    items: [
      { label: 'Configuración', icon: 'settings', to: '/dev/components' },
      { label: 'Niños', icon: 'users', to: '/dev/components' },
      { label: 'Chatbot', icon: 'message-circle', to: '/dev/components' },
      { label: 'Documentación', icon: 'book-open', to: '/dev/components' }
    ]
  },
  {
    id: 'experiences',
    title: 'Experiencias',
    items: [
      { label: 'Lectura en familia', icon: 'book', to: '/dev/components' },
      { label: 'Relajación en familia', icon: 'moon', to: '/dev/components' }
    ]
  }
]
</script>

<style scoped>
.catalog-section { max-width: 800px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-sidebar-container { height: 400px; border: var(--nubi-border-width) solid var(--nubi-border-default); border-radius: var(--nubi-radius-lg); overflow: hidden; }
.demo-hint { font-size: var(--nubi-font-size-sm); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-sm); }
</style>
