<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.inactivityOverlays') }}</h2>
      <p class="section-description">
        Overlay de inactividad antes del logout automático.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-controls">
          <NubiButton variant="primary" @click="visible = true">Mostrar overlay</NubiButton>
        </div>
        <NubiInactivityOverlay 
          :visible="visible" 
          :time-left="timeLeft"
          @extend="handleExtend"
          @logout="handleLogout"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Diferentes tiempos</h3>
        <div class="demo-controls">
          <NubiButton variant="secondary" @click="() => { timeLeft = 120; visible = true }">2 min</NubiButton>
          <NubiButton variant="secondary" @click="() => { timeLeft = 60; visible = true }">1 min</NubiButton>
          <NubiButton variant="secondary" @click="() => { timeLeft = 30; visible = true }">30 sec</NubiButton>
        </div>
        <NubiInactivityOverlay 
          :visible="visible" 
          :time-left="timeLeft"
          @extend="handleExtend"
          @logout="handleLogout"
        />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiInactivityOverlay from '../../components/base/NubiInactivityOverlay.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const visible = ref(false)
const timeLeft = ref(60)

function handleExtend() {
  alert('Extender sesión')
  visible.value = false
}

function handleLogout() {
  alert('Cerrar sesión')
  visible.value = false
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-controls { display: flex; gap: var(--nubi-spacing-sm); flex-wrap: wrap; }
</style>
