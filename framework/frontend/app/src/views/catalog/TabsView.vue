<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.tabsComponent') }}</h2>
      <p class="section-description">
        Navegación entre subsecciones con indicador visual de tab activa.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiTabs v-model="activeTab1" :tabs="basicTabs">
          <template #general><p>Contenido de la pestaña General</p></template>
          <template #security><p>Contenido de la pestaña Seguridad</p></template>
          <template #notifications><p>Contenido de la pestaña Notificaciones</p></template>
        </NubiTabs>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con iconos</h3>
        <NubiTabs v-model="activeTab2" :tabs="iconTabs">
          <template #profile><p>Contenido del perfil</p></template>
          <template #security><p>Contenido de seguridad</p></template>
          <template #settings><p>Contenido de ajustes</p></template>
        </NubiTabs>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Muchas pestañas</h3>
        <NubiTabs v-model="activeTab3" :tabs="manyTabs">
          <template v-for="tab in manyTabs" :key="tab.value" #[tab.value]>
            <p>Contenido de {{ tab.label }}</p>
          </template>
        </NubiTabs>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiTabs from '../../components/base/NubiTabs.vue'

const activeTab1 = ref('general')
const activeTab2 = ref('profile')
const activeTab3 = ref('tab1')

const basicTabs = [
  { value: 'general', label: 'General' },
  { value: 'security', label: 'Seguridad' },
  { value: 'notifications', label: 'Notificaciones' }
]

const iconTabs = [
  { value: 'profile', label: 'Perfil', icon: 'user' },
  { value: 'security', label: 'Seguridad', icon: 'shield' },
  { value: 'settings', label: 'Ajustes', icon: 'settings' }
]

const manyTabs = Array.from({ length: 6 }, (_, i) => ({
  value: `tab${i + 1}`,
  label: `Pestaña ${i + 1}`
}))
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
</style>
