<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.lists') }}</h2>
      <p class="section-description">
        Lista de elementos apilados verticalmente.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básica</h3>
        <div class="demo-column">
          <NubiList :items="items" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con borde</h3>
        <div class="demo-column">
          <NubiList :items="items" :bordered="true" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Clickeable</h3>
        <div class="demo-column">
          <NubiList :items="items" :bordered="true" :clickable="true" @item-click="handleClick" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Slot personalizado</h3>
        <div class="demo-column">
          <NubiList :items="complexItems" :bordered="true">
            <template #item="{ item }">
              <div class="custom-item">
                <NubiAvatar :name="item.name" size="sm" />
                <div>
                  <div class="custom-item__name">{{ item.name }}</div>
                  <div class="custom-item__email">{{ item.email }}</div>
                </div>
              </div>
            </template>
          </NubiList>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Vacía</h3>
        <div class="demo-column">
          <NubiList :items="[]" :bordered="true" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiList from '../../components/base/NubiList.vue'
import NubiAvatar from '../../components/base/NubiAvatar.vue'

const items = ref([
  'Elemento 1',
  'Elemento 2',
  'Elemento 3'
])

const complexItems = ref([
  { name: 'Ana García', email: 'ana@example.com' },
  { name: 'Pedro López', email: 'pedro@example.com' },
  { name: 'María Rodríguez', email: 'maria@example.com' }
])

function handleClick(item: any) {
  alert(`Clic en: ${item}`)
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-column { display: flex; flex-direction: column; gap: var(--nubi-spacing-md); }
.custom-item { display: flex; align-items: center; gap: 12px; }
.custom-item__name { font-weight: 600; }
.custom-item__email { font-size: 14px; color: var(--nubi-text-secondary); }
</style>
