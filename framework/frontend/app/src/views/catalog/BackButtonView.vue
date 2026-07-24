<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.backButtons') }}</h2>
      <p class="section-description">
        Botón de navegación hacia atrás con flecha y texto opcional.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Con texto</h3>
        <div class="demo-row">
          <NubiBackButton :use-router="false" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Solo icono</h3>
        <div class="demo-row">
          <NubiBackButton :show-label="false" :use-router="false" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Texto personalizado</h3>
        <div class="demo-row">
          <NubiBackButton label="Ir al inicio" :use-router="false" />
          <NubiBackButton label="Volver al panel" :use-router="false" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiBackButton from '../../components/base/NubiBackButton.vue'
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-row { display: flex; gap: var(--nubi-spacing-md); flex-wrap: wrap; align-items: center; }
</style>
