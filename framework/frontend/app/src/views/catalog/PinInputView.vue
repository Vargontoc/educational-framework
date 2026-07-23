<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.pinInputs') }}</h2>
      <p class="section-description">
        Entrada de PIN numérico de 4 dígitos con feedback visual y modo oculto.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-center">
          <NubiPinInput v-model="pinValue" label="Introduce tu PIN" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Modo oculto</h3>
        <div class="demo-center">
          <NubiPinInput v-model="maskedPin" label="PIN de seguridad" :masked="true" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-center-column">
          <NubiPinInput label="Normal" model-value="" />
          <NubiPinInput label="Parcial" model-value="12" />
          <NubiPinInput label="Completado" model-value="1234" />
          <NubiPinInput label="Con error" model-value="1234" error="PIN incorrecto" />
          <NubiPinInput label="Disabled" model-value="1234" disabled />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Longitud personalizada</h3>
        <div class="demo-center">
          <NubiPinInput label="PIN de 6 dígitos" :pin-length="6" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiPinInput from '../../components/base/NubiPinInput.vue'

const pinValue = ref('')
const maskedPin = ref('')
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

.demo-center {
  display: flex;
  justify-content: center;
}

.demo-center-column {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xl);
  align-items: center;
}
</style>
