<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.badges') }}</h2>
      <p class="section-description">
        Etiqueta pequeña para estados o contadores.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Variantes</h3>
        <div class="demo-row">
          <NubiBadge variant="neutral" label="Neutral" />
          <NubiBadge variant="info" label="Info" />
          <NubiBadge variant="success" label="Success" />
          <NubiBadge variant="warning" label="Warning" />
          <NubiBadge variant="error" label="Error" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Tamaños</h3>
        <div class="demo-row">
          <NubiBadge variant="info" label="Small" size="sm" />
          <NubiBadge variant="info" label="Medium" size="md" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con iconos</h3>
        <div class="demo-row">
          <NubiBadge variant="info" label="Info" icon="info" />
          <NubiBadge variant="success" label="Success" icon="check-circle" />
          <NubiBadge variant="warning" label="Warning" icon="alert-triangle" />
          <NubiBadge variant="error" label="Error" icon="alert-circle" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Contenido personalizado</h3>
        <div class="demo-row">
          <NubiBadge variant="success">
            <strong>5</strong> nuevos
          </NubiBadge>
          <NubiBadge variant="info">
            v2.0.0
          </NubiBadge>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiBadge from '../../components/base/NubiBadge.vue'
</script>

<style scoped>
.catalog-section { max-width: 800px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-row { display: flex; gap: var(--nubi-spacing-md); align-items: center; flex-wrap: wrap; }
</style>
