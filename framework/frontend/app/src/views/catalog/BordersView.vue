<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.borders') }}</h2>
      <p class="section-description">
        Bordes redondeados y sombras del sistema de diseño Nubi.
      </p>

      <div class="borders-section">
        <h3 class="subsection-title">Bordes redondeados</h3>
        <div class="radius-grid">
          <div class="radius-item">
            <div class="radius-box" style="border-radius: var(--nubi-radius-sm)"></div>
            <span class="label">sm (4px)</span>
          </div>
          <div class="radius-item">
            <div class="radius-box" style="border-radius: var(--nubi-radius-md)"></div>
            <span class="label">md (6px)</span>
          </div>
          <div class="radius-item">
            <div class="radius-box" style="border-radius: var(--nubi-radius-lg)"></div>
            <span class="label">lg (8px)</span>
          </div>
          <div class="radius-item">
            <div class="radius-box" style="border-radius: var(--nubi-radius-xl)"></div>
            <span class="label">xl (12px)</span>
          </div>
          <div class="radius-item">
            <div class="radius-box" style="border-radius: var(--nubi-radius-full)"></div>
            <span class="label">full (9999px)</span>
          </div>
        </div>
      </div>

      <div class="shadows-section">
        <h3 class="subsection-title">Sombras</h3>
        <div class="shadow-grid">
          <div class="shadow-item">
            <div class="shadow-box" style="box-shadow: var(--nubi-shadow-sm)"></div>
            <span class="label">sm</span>
          </div>
          <div class="shadow-item">
            <div class="shadow-box" style="box-shadow: var(--nubi-shadow-md)"></div>
            <span class="label">md</span>
          </div>
          <div class="shadow-item">
            <div class="shadow-box" style="box-shadow: var(--nubi-shadow-lg)"></div>
            <span class="label">lg</span>
          </div>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
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

.borders-section,
.shadows-section {
  margin-bottom: var(--nubi-spacing-2xl);
}

.subsection-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-md);
}

.radius-grid,
.shadow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--nubi-spacing-lg);
}

.radius-item,
.shadow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

.radius-box,
.shadow-box {
  width: 100px;
  height: 100px;
  background-color: var(--nubi-bg-surface-secondary);
  border: 1px solid var(--nubi-border-default);
}

.label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  font-family: monospace;
}
</style>
