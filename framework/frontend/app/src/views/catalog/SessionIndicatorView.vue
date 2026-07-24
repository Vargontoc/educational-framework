<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.sessionIndicators') }}</h2>
      <p class="section-description">
        Indicador de tiempo restante de sesión.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Normal</h3>
        <div class="demo-column">
          <NubiSessionIndicator :time-left="300" />
          <NubiSessionIndicator :time-left="180" />
          <NubiSessionIndicator :time-left="60" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Advertencia (< 1 minuto)</h3>
        <div class="demo-column">
          <NubiSessionIndicator :time-left="59" />
          <NubiSessionIndicator :time-left="30" />
          <NubiSessionIndicator :time-left="1" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Expirado</h3>
        <div class="demo-column">
          <NubiSessionIndicator :time-left="0" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Label personalizado</h3>
        <div class="demo-column">
          <NubiSessionIndicator :time-left="120" label="Sesión del panel" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Animado</h3>
        <div class="demo-column">
          <NubiSessionIndicator :time-left="timeLeft" />
          <div class="demo-controls">
            <NubiButton variant="secondary" size="sm" @click="timeLeft = 300">5 min</NubiButton>
            <NubiButton variant="secondary" size="sm" @click="timeLeft = 60">1 min</NubiButton>
            <NubiButton variant="secondary" size="sm" @click="timeLeft = 0">Expirar</NubiButton>
          </div>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiSessionIndicator from '../../components/base/NubiSessionIndicator.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const timeLeft = ref(300)
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-column { display: flex; flex-direction: column; gap: var(--nubi-spacing-md); max-width: 300px; }
.demo-controls { display: flex; gap: var(--nubi-spacing-sm); margin-top: var(--nubi-spacing-sm); }
</style>
