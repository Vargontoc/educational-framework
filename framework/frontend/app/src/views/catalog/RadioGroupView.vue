<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.radioGroups') }}</h2>
      <p class="section-description">
        Grupo de radio buttons para selección única entre opciones mutuamente excluyentes.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Vertical</h3>
        <NubiRadioGroup v-model="selectedColor" :options="colorOptions" label="Color favorito" direction="vertical" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Horizontal</h3>
        <NubiRadioGroup v-model="selectedSize" :options="sizeOptions" label="Tamaño" direction="horizontal" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Opciones simples</h3>
        <NubiRadioGroup :options="simpleOptions" label="Opciones simples" />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiRadioGroup :options="colorOptions" label="Normal" />
          <NubiRadioGroup :options="colorOptions" label="Disabled" disabled />
          <NubiRadioGroup :options="colorOptions" label="Con error" error="Selecciona una opción" />
          <NubiRadioGroup :options="colorOptions" label="Con hint" hint="Elige tu color preferido" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Opciones deshabilitadas</h3>
        <NubiRadioGroup :options="disabledOptions" label="Opciones parciales" />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiRadioGroup from '../../components/base/NubiRadioGroup.vue'

const selectedColor = ref<string | number | null>(null)
const selectedSize = ref<string | number | null>('md')

const colorOptions = [
  { value: 'red', label: 'Rojo' },
  { value: 'green', label: 'Verde' },
  { value: 'blue', label: 'Azul' }
]

const sizeOptions = [
  { value: 'sm', label: 'Pequeño' },
  { value: 'md', label: 'Mediano' },
  { value: 'lg', label: 'Grande' }
]

const simpleOptions = ['Opción A', 'Opción B', 'Opción C']

const disabledOptions = [
  { value: 'a', label: 'Opción A' },
  { value: 'b', label: 'Opción B (deshabilitada)', disabled: true },
  { value: 'c', label: 'Opción C' }
]
</script>

<style scoped>
.catalog-section {
  max-width: 600px;
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
</style>
