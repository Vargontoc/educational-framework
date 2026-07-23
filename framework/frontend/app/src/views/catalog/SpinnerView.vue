<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.spinners') }}</h2>
      <p class="section-description">
        Indicador de carga circular con animación suave.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tamaños</h3>
        <div class="demo-row">
          <NubiSpinner size="sm" />
          <NubiSpinner size="md" />
          <NubiSpinner size="lg" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con label</h3>
        <div class="demo-row">
          <NubiSpinner :show-label="true" label="Cargando datos..." />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Overlay</h3>
        <div class="overlay-demo">
          <p>Contenido detrás del overlay</p>
          <NubiSpinner overlay />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiSpinner from '../../components/base/NubiSpinner.vue'
</script>

<style scoped>
.catalog-section {
  max-width: 1200px;
}

.section-title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-sm);
}

.section-description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin-bottom: var(--nubi-spacing-xl);
}

.component-demo {
  margin-bottom: var(--nubi-spacing-2xl);
}

.subsection-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-md);
}

.demo-row {
  display: flex;
  gap: var(--nubi-spacing-lg);
  flex-wrap: wrap;
  align-items: center;
}

.overlay-demo {
  position: relative;
  width: 100%;
  height: 200px;
  background-color: var(--nubi-bg-surface-secondary);
  border-radius: var(--nubi-radius-md);
  padding: var(--nubi-spacing-md);
}
</style>
