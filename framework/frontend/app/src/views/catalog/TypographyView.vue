<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.typography') }}</h2>
      <p class="section-description">
        Sistema tipográfico del sistema de diseño Nubi.
      </p>

      <div class="typography-section">
        <h3 class="subsection-title">Escala de tamaños</h3>
        <div class="typography-grid">
          <div class="typography-item">
            <span class="label">xs (12px)</span>
            <p style="font-size: var(--nubi-font-size-xs)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">sm (14px)</span>
            <p style="font-size: var(--nubi-font-size-sm)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">base (16px)</span>
            <p style="font-size: var(--nubi-font-size-base)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">lg (18px)</span>
            <p style="font-size: var(--nubi-font-size-lg)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">xl (20px)</span>
            <p style="font-size: var(--nubi-font-size-xl)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">2xl (24px)</span>
            <p style="font-size: var(--nubi-font-size-2xl)">Texto de ejemplo</p>
          </div>
        </div>
      </div>

      <div class="typography-section">
        <h3 class="subsection-title">Pesos de fuente</h3>
        <div class="typography-grid">
          <div class="typography-item">
            <span class="label">light (300)</span>
            <p style="font-weight: var(--nubi-font-weight-light)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">regular (400)</span>
            <p style="font-weight: var(--nubi-font-weight-regular)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">medium (500)</span>
            <p style="font-weight: var(--nubi-font-weight-medium)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">semibold (600)</span>
            <p style="font-weight: var(--nubi-font-weight-semibold)">Texto de ejemplo</p>
          </div>
          <div class="typography-item">
            <span class="label">bold (700)</span>
            <p style="font-weight: var(--nubi-font-weight-bold)">Texto de ejemplo</p>
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

.typography-section {
  margin-bottom: var(--nubi-spacing-2xl);
}

.subsection-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-md);
}

.typography-grid {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.typography-item {
  padding: var(--nubi-spacing-md);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
}

.label {
  display: block;
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-tertiary);
  margin-bottom: var(--nubi-spacing-xs);
  font-family: monospace;
}
</style>
