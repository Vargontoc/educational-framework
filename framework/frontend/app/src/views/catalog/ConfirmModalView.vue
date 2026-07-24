<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.confirmModals') }}</h2>
      <p class="section-description">
        Diálogo de confirmación con overlay oscuro, título, mensaje y botones confirmar/cancelar.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiButton variant="primary" @click="showBasic = true">Abrir modal</NubiButton>
        <p v-if="lastAction" class="action-result">Última acción: {{ lastAction }}</p>
        
        <NubiConfirmModal
          v-model="showBasic"
          title="Confirmar acción"
          message="¿Estás seguro de que quieres continuar con esta acción?"
          @confirm="lastAction = 'Confirmado'"
          @cancel="lastAction = 'Cancelado'"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Destructivo</h3>
        <NubiButton variant="destructive" @click="showDestructive = true">Eliminar elemento</NubiButton>
        
        <NubiConfirmModal
          v-model="showDestructive"
          title="Eliminar elemento"
          message="Esta acción no se puede deshacer. ¿Estás seguro de que quieres eliminar este elemento?"
          confirm-text="Eliminar"
          confirm-variant="destructive"
          icon="trash-2"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con icono</h3>
        <NubiButton variant="secondary" @click="showIcon = true">Guardar cambios</NubiButton>
        
        <NubiConfirmModal
          v-model="showIcon"
          title="Guardar cambios"
          message="¿Quieres guardar los cambios realizados?"
          icon="save"
        />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiConfirmModal from '../../components/base/NubiConfirmModal.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const showBasic = ref(false)
const showDestructive = ref(false)
const showIcon = ref(false)
const lastAction = ref('')
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.action-result { margin-top: var(--nubi-spacing-sm); font-size: var(--nubi-font-size-sm); color: var(--nubi-text-secondary); }
</style>
