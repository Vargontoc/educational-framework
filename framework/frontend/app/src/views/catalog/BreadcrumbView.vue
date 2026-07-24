<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.breadcrumbs') }}</h2>
      <p class="section-description">
        Migas de pan para navegación jerárquica con enlaces a niveles anteriores.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tres niveles</h3>
        <NubiBreadcrumb :items="threeLevels" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Dos niveles</h3>
        <NubiBreadcrumb :items="twoLevels" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Un solo nivel</h3>
        <NubiBreadcrumb :items="oneLevel" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Cuatro niveles</h3>
        <NubiBreadcrumb :items="fourLevels" />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiBreadcrumb from '../../components/base/NubiBreadcrumb.vue'

const oneLevel = [{ label: 'Inicio' }]
const twoLevels = [{ label: 'Inicio', to: '/' }, { label: 'Niños' }]
const threeLevels = [{ label: 'Inicio', to: '/' }, { label: 'Panel', to: '/panel' }, { label: 'Configuración' }]
const fourLevels = [{ label: 'Inicio', to: '/' }, { label: 'Panel', to: '/panel' }, { label: 'Niños', to: '/panel/children' }, { label: 'Editar' }]
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
</style>
