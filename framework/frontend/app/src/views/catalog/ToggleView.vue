<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.toggles') }}</h2>
      <p class="section-description">
        Interruptor on/off con animación suave para configuraciones frecuentes.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiToggle v-model="toggleOn" label="Notificaciones" />
          <NubiToggle v-model="toggleOff" label="Modo oscuro" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con estado visible</h3>
        <div class="demo-column">
          <NubiToggle v-model="toggleOn" label="Sonido" :show-status="true" />
          <NubiToggle v-model="toggleOff" label="Vibración" :show-status="true" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiToggle :model-value="false" label="Off" />
          <NubiToggle :model-value="true" label="On" />
          <NubiToggle :model-value="false" label="Disabled Off" disabled />
          <NubiToggle :model-value="true" label="Disabled On" disabled />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiToggle from '../../components/base/NubiToggle.vue'

const toggleOn = ref(true)
const toggleOff = ref(false)
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
