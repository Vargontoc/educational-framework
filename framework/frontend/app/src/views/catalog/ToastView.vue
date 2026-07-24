<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.toasts') }}</h2>
      <p class="section-description">
        Notificaciones temporales no intrusivas con sistema de cola.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tipos</h3>
        <div class="demo-row">
          <NubiButton variant="primary" @click="showSuccess">Success</NubiButton>
          <NubiButton variant="destructive" @click="showError">Error</NubiButton>
          <NubiButton variant="secondary" @click="showWarning">Warning</NubiButton>
          <NubiButton variant="secondary" @click="showInfo">Info</NubiButton>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Múltiples toasts</h3>
        <div class="demo-row">
          <NubiButton variant="primary" @click="showMultiple">Mostrar 3 toasts</NubiButton>
          <NubiButton variant="secondary" @click="clearAll">Limpiar todos</NubiButton>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Posiciones</h3>
        <div class="demo-row">
          <NubiButton variant="secondary" @click="showAtPosition('top-right')">Top Right</NubiButton>
          <NubiButton variant="secondary" @click="showAtPosition('top-left')">Top Left</NubiButton>
          <NubiButton variant="secondary" @click="showAtPosition('bottom-right')">Bottom Right</NubiButton>
          <NubiButton variant="secondary" @click="showAtPosition('bottom-left')">Bottom Left</NubiButton>
        </div>
      </div>

      <NubiToastContainer position="top-right" />
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiToastContainer from '../../components/base/NubiToastContainer.vue'
import NubiButton from '../../components/base/NubiButton.vue'
import { useToast } from '../../composables/useToast'

const toast = useToast()

function showSuccess() { toast.success('Operación completada correctamente') }
function showError() { toast.error('Ha ocurrido un error inesperado') }
function showWarning() { toast.warning('Ten cuidado con esta acción') }
function showInfo() { toast.info('Este es un mensaje informativo') }

function showMultiple() {
  toast.info('Primer mensaje')
  toast.success('Segundo mensaje')
  toast.warning('Tercer mensaje')
}

function clearAll() { toast.clearAll() }

function showAtPosition(_position: string) {
  toast.info(`Toast en ${_position}`)
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-row { display: flex; gap: var(--nubi-spacing-md); flex-wrap: wrap; align-items: center; }
</style>
