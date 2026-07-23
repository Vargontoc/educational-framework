<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.numberInputs') }}</h2>
      <p class="section-description">
        Campo de entrada numérico con botones de incremento/decremento y validación de rango.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiNumberInput v-model="basicValue" label="Cantidad" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con rango</h3>
        <div class="demo-column">
          <NubiNumberInput v-model="rangedValue" label="Edad" :min="0" :max="100" />
          <NubiNumberInput label="Porcentaje" :min="0" :max="100" :step="5" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiNumberInput label="Normal" :model-value="10" />
          <NubiNumberInput label="Disabled" :model-value="10" disabled />
          <NubiNumberInput label="Con error" error="El valor no es válido" />
          <NubiNumberInput label="Con hint" hint="Introduce un número entre 1 y 10" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiNumberInput from '../../components/base/NubiNumberInput.vue'

const basicValue = ref(5)
const rangedValue = ref(50)
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
