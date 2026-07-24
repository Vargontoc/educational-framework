<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.infoModals') }}</h2>
      <p class="section-description">
        Modal informativo con solo botón de cierre y scroll interno para contenido extenso.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiButton variant="primary" @click="showBasic = true">Abrir modal</NubiButton>
        
        <NubiInfoModal
          v-model="showBasic"
          title="Información"
          message="Este es un modal informativo con contenido de ayuda para el usuario."
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con contenido personalizado</h3>
        <NubiButton variant="secondary" @click="showCustom = true">Abrir con contenido</NubiButton>
        
        <NubiInfoModal v-model="showCustom" title="Ayuda">
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <p>Este modal tiene contenido personalizado usando slots.</p>
            <p>Puedes incluir cualquier contenido HTML dentro del modal.</p>
            <ul style="padding-left: 20px;">
              <li>Elemento 1</li>
              <li>Elemento 2</li>
              <li>Elemento 3</li>
            </ul>
          </div>
        </NubiInfoModal>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Contenido largo (scroll interno)</h3>
        <NubiButton variant="secondary" @click="showLong = true">Contenido largo</NubiButton>
        
        <NubiInfoModal v-model="showLong" title="Términos y condiciones">
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <p v-for="i in 20" :key="i">
              Párrafo {{ i }}: Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
              Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
              Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.
            </p>
          </div>
        </NubiInfoModal>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiInfoModal from '../../components/base/NubiInfoModal.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const showBasic = ref(false)
const showCustom = ref(false)
const showLong = ref(false)
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
</style>
