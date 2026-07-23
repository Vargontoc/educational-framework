<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.skeletons') }}</h2>
      <p class="section-description">
        Placeholder animado para contenido en carga.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Variantes</h3>
        <div class="demo-column">
          <div class="demo-item">
            <span class="label">Line</span>
            <NubiSkeleton variant="line" />
          </div>
          <div class="demo-item">
            <span class="label">Circle</span>
            <NubiSkeleton variant="circle" width="64px" />
          </div>
          <div class="demo-item">
            <span class="label">Rectangle</span>
            <NubiSkeleton variant="rectangle" width="200px" height="120px" />
          </div>
          <div class="demo-item">
            <span class="label">Card</span>
            <NubiSkeleton variant="card" width="300px" />
          </div>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Ejemplo de layout</h3>
        <div class="layout-example">
          <NubiSkeleton variant="circle" width="64px" />
          <div class="layout-content">
            <NubiSkeleton variant="line" width="70%" />
            <NubiSkeleton variant="line" />
            <NubiSkeleton variant="line" width="50%" />
          </div>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Sin animación</h3>
        <div class="demo-column">
          <NubiSkeleton variant="line" :animate="false" />
          <NubiSkeleton variant="line" width="80%" :animate="false" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiSkeleton from '../../components/base/NubiSkeleton.vue'
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

.demo-column {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.demo-item {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  font-family: monospace;
}

.layout-example {
  display: flex;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-md);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
}

.layout-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}
</style>
