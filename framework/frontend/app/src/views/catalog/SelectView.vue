<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.selects') }}</h2>
      <p class="section-description">
        Selector de opción única con dropdown accesible y navegación por teclado.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiSelect v-model="selectedColor" :options="colorOptions" label="Color favorito" placeholder="Selecciona un color" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Opciones simples</h3>
        <div class="demo-column">
          <NubiSelect :options="simpleOptions" label="Opciones simples" placeholder="Elige una" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiSelect :options="colorOptions" label="Normal" placeholder="Selecciona..." />
          <NubiSelect :options="colorOptions" label="Disabled" disabled placeholder="Deshabilitado" />
          <NubiSelect :options="colorOptions" label="Con error" error="Este campo es obligatorio" />
          <NubiSelect :options="colorOptions" label="Con hint" hint="Selecciona tu color preferido" />
          <NubiSelect :options="[]" label="Sin opciones" placeholder="No hay opciones" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Obligatorio</h3>
        <div class="demo-column">
          <NubiSelect v-model="selectedAge" :options="ageOptions" label="Edad del niño" :required="true" placeholder="Selecciona edad" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiSelect from '../../components/base/NubiSelect.vue'

const selectedColor = ref<string | number | null>(null)
const selectedAge = ref<string | number | null>(null)

const colorOptions = [
  { value: 'red', label: 'Rojo' },
  { value: 'green', label: 'Verde' },
  { value: 'blue', label: 'Azul' },
  { value: 'yellow', label: 'Amarillo' }
]

const ageOptions = [
  { value: 3, label: '3 años' },
  { value: 4, label: '4 años' },
  { value: 5, label: '5 años' },
  { value: 6, label: '6 años' }
]

const simpleOptions = ['Opción A', 'Opción B', 'Opción C']
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
