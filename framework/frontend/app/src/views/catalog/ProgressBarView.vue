<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.progressBars') }}</h2>
      <p class="section-description">
        Indicador visual de avance con animación suave.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básica</h3>
        <div class="demo-column">
          <NubiProgressBar :value="progress" label="Progreso" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Diferentes valores</h3>
        <div class="demo-column">
          <NubiProgressBar :value="0" label="0%" />
          <NubiProgressBar :value="25" label="25%" />
          <NubiProgressBar :value="50" label="50%" />
          <NubiProgressBar :value="75" label="75%" />
          <NubiProgressBar :value="100" label="100%" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Sin label</h3>
        <div class="demo-column">
          <NubiProgressBar :value="progress" :show-label="false" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Animada</h3>
        <div class="demo-column">
          <NubiProgressBar :value="progress" label="Progreso animado" />
          <div class="demo-controls">
            <NubiButton variant="secondary" size="sm" @click="progress = 0">0%</NubiButton>
            <NubiButton variant="secondary" size="sm" @click="progress = 50">50%</NubiButton>
            <NubiButton variant="secondary" size="sm" @click="progress = 100">100%</NubiButton>
          </div>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiProgressBar from '../../components/base/NubiProgressBar.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const progress = ref(65)
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-column { display: flex; flex-direction: column; gap: var(--nubi-spacing-md); }
.demo-controls { display: flex; gap: var(--nubi-spacing-sm); margin-top: var(--nubi-spacing-sm); }
</style>
