<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.spacing') }}</h2>
      <p class="section-description">
        Sistema de espaciado del sistema de diseño Nubi.
      </p>

      <div class="spacing-grid">
        <div class="spacing-item">
          <span class="label">xs (4px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-xs); height: var(--nubi-spacing-xs)"></div>
        </div>
        <div class="spacing-item">
          <span class="label">sm (8px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-sm); height: var(--nubi-spacing-sm)"></div>
        </div>
        <div class="spacing-item">
          <span class="label">md (16px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-md); height: var(--nubi-spacing-md)"></div>
        </div>
        <div class="spacing-item">
          <span class="label">lg (24px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-lg); height: var(--nubi-spacing-lg)"></div>
        </div>
        <div class="spacing-item">
          <span class="label">xl (32px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-xl); height: var(--nubi-spacing-xl)"></div>
        </div>
        <div class="spacing-item">
          <span class="label">2xl (48px)</span>
          <div class="spacing-box" style="width: var(--nubi-spacing-2xl); height: var(--nubi-spacing-2xl)"></div>
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

.spacing-grid {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.spacing-item {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-md);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
}

.label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  min-width: 80px;
  font-family: monospace;
}

.spacing-box {
  background-color: var(--nubi-color-primary);
  border-radius: var(--nubi-radius-sm);
}
</style>
