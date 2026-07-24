<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.steppers') }}</h2>
      <p class="section-description">
        Navegación entre pasos de un formulario.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiStepper v-model="currentStep" :steps="steps">
          <template #default="{ currentStep: step }">
            <div class="step-content">
              <h3>Paso {{ step + 1 }}: {{ steps[step] }}</h3>
              <p>Contenido del paso {{ step + 1 }}</p>
            </div>
          </template>
        </NubiStepper>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Comenzando en paso 2</h3>
        <NubiStepper v-model="1" :steps="steps">
          <template #default="{ currentStep: step }">
            <div class="step-content">
              <h3>Paso {{ step + 1 }}: {{ steps[step] }}</h3>
              <p>Contenido del paso {{ step + 1 }}</p>
            </div>
          </template>
        </NubiStepper>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiStepper from '../../components/base/NubiStepper.vue'

const currentStep = ref(0)
const steps = ['Datos personales', 'Configuración', 'Confirmación']
</script>

<style scoped>
.catalog-section { max-width: 800px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.step-content { padding: var(--nubi-spacing-lg); background: var(--nubi-bg-surface-secondary); border-radius: var(--nubi-radius-lg); }
.step-content h3 { margin: 0 0 var(--nubi-spacing-sm) 0; }
.step-content p { margin: 0; }
</style>
