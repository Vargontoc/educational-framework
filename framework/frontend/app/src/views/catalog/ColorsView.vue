<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.colors') }}</h2>
      <p class="section-description">
        Paleta de colores del sistema de diseño Nubi.
      </p>

      <div class="color-section">
        <h3 class="subsection-title">Colores de marca</h3>
        <div class="color-grid">
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-primary)"></div>
            <div class="color-info">
              <span class="color-name">Primary</span>
              <span class="color-value">#4DBA87</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-secondary)"></div>
            <div class="color-info">
              <span class="color-name">Secondary</span>
              <span class="color-value">#F5A623</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-accent)"></div>
            <div class="color-info">
              <span class="color-name">Accent</span>
              <span class="color-value">#9B59B6</span>
            </div>
          </div>
        </div>
      </div>

      <div class="color-section">
        <h3 class="subsection-title">Colores de estado</h3>
        <div class="color-grid">
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-success)"></div>
            <div class="color-info">
              <span class="color-name">Success</span>
              <span class="color-value">#10B981</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-warning)"></div>
            <div class="color-info">
              <span class="color-name">Warning</span>
              <span class="color-value">#F59E0B</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-error)"></div>
            <div class="color-info">
              <span class="color-name">Error</span>
              <span class="color-value">#EF4444</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-info)"></div>
            <div class="color-info">
              <span class="color-name">Info</span>
              <span class="color-value">#3B82F6</span>
            </div>
          </div>
        </div>
      </div>

      <div class="color-section">
        <h3 class="subsection-title">Neutros</h3>
        <div class="color-grid">
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-white)"></div>
            <div class="color-info">
              <span class="color-name">White</span>
              <span class="color-value">#FFFFFF</span>
            </div>
          </div>
          <div class="color-card">
            <div class="color-swatch" style="background-color: var(--nubi-color-black)"></div>
            <div class="color-info">
              <span class="color-name">Black</span>
              <span class="color-value">#000000</span>
            </div>
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

.color-section {
  margin-bottom: var(--nubi-spacing-2xl);
}

.subsection-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-md);
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--nubi-spacing-md);
}

.color-card {
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  overflow: hidden;
}

.color-swatch {
  height: 100px;
  width: 100%;
}

.color-info {
  padding: var(--nubi-spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.color-name {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.color-value {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
  font-family: monospace;
}
</style>
