<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.grids') }}</h2>
      <p class="section-description">
        Cuadrícula responsive configurable.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">3 columnas</h3>
        <NubiGrid :items="items" :cols="3">
          <template #item="{ item }">
            <NubiCard :title="item.title" :description="item.description" />
          </template>
        </NubiGrid>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">2 columnas</h3>
        <NubiGrid :items="items" :cols="2">
          <template #item="{ item }">
            <NubiCard :title="item.title" :description="item.description" />
          </template>
        </NubiGrid>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">4 columnas</h3>
        <NubiGrid :items="items" :cols="4">
          <template #item="{ item }">
            <NubiCard :title="item.title" :description="item.description" />
          </template>
        </NubiGrid>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Vacía</h3>
        <NubiGrid :items="[]" :cols="3" />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiGrid from '../../components/base/NubiGrid.vue'
import NubiCard from '../../components/base/NubiCard.vue'

const items = ref([
  { title: 'Card 1', description: 'Descripción 1' },
  { title: 'Card 2', description: 'Descripción 2' },
  { title: 'Card 3', description: 'Descripción 3' },
  { title: 'Card 4', description: 'Descripción 4' },
  { title: 'Card 5', description: 'Descripción 5' },
  { title: 'Card 6', description: 'Descripción 6' }
])
</script>

<style scoped>
.catalog-section { max-width: 1200px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
</style>
