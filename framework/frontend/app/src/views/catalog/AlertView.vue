<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.alerts') }}</h2>
      <p class="section-description">
        Mensajes persistentes hasta cierre manual con tipos info, warning, error y success.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tipos</h3>
        <div class="demo-column">
          <NubiAlert type="info" title="Información" message="Este es un mensaje informativo." />
          <NubiAlert type="warning" title="Advertencia" message="Ten cuidado con esta acción." />
          <NubiAlert type="error" title="Error" message="Ha ocurrido un error inesperado." />
          <NubiAlert type="success" title="Éxito" message="La operación se completó correctamente." />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Sin título</h3>
        <div class="demo-column">
          <NubiAlert type="info" message="Mensaje informativo sin título." />
          <NubiAlert type="success" message="Operación completada." />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">No dismissibles</h3>
        <div class="demo-column">
          <NubiAlert type="info" title="Persistente" message="Esta alerta no se puede cerrar." :dismissible="false" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Dismissible (se pueden cerrar)</h3>
        <div class="demo-column">
          <NubiAlert v-if="showAlerts.info" type="info" title="Info" message="Ciérrame." @dismiss="showAlerts.info = false" />
          <NubiAlert v-if="showAlerts.warning" type="warning" title="Warning" message="Ciérrame." @dismiss="showAlerts.warning = false" />
          <NubiAlert v-if="showAlerts.error" type="error" title="Error" message="Ciérrame." @dismiss="showAlerts.error = false" />
          <NubiAlert v-if="showAlerts.success" type="success" title="Success" message="Ciérrame." @dismiss="showAlerts.success = false" />
          <NubiButton v-if="!hasVisibleAlerts" variant="secondary" @click="resetAlerts">Mostrar alertas</NubiButton>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiAlert from '../../components/base/NubiAlert.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const showAlerts = ref({ info: true, warning: true, error: true, success: true })

const hasVisibleAlerts = computed(() => 
  showAlerts.value.info || showAlerts.value.warning || showAlerts.value.error || showAlerts.value.success
)

function resetAlerts() {
  showAlerts.value = { info: true, warning: true, error: true, success: true }
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-column { display: flex; flex-direction: column; gap: var(--nubi-spacing-md); }
</style>
