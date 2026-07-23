<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.checkboxes') }}</h2>
      <p class="section-description">
        Casilla de verificación con label claro y estados checked/unchecked/indeterminate.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiCheckbox v-model="checked" label="Acepto los términos y condiciones" />
          <NubiCheckbox label="Opción sin v-model" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiCheckbox :model-value="false" label="Unchecked" />
          <NubiCheckbox :model-value="true" label="Checked" />
          <NubiCheckbox :model-value="'indeterminate'" label="Indeterminate" />
          <NubiCheckbox :model-value="false" label="Disabled" disabled />
          <NubiCheckbox :model-value="true" label="Checked & Disabled" disabled />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiCheckbox from '../../components/base/NubiCheckbox.vue'

const checked = ref(false)
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
