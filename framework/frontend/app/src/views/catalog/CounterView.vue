<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.counters') }}</h2>
      <p class="section-description">
        Muestra cantidad con modo estático o animado.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiCounter :value="0" />
          <NubiCounter :value="5" />
          <NubiCounter :value="42" />
          <NubiCounter :value="1000" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con icono</h3>
        <div class="demo-column">
          <NubiCounter :value="5" icon="users" />
          <NubiCounter :value="12" icon="shopping-cart" />
          <NubiCounter :value="3" icon="star" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con sufijo</h3>
        <div class="demo-column">
          <NubiCounter :value="5" suffix="usuarios" />
          <NubiCounter :value="12" suffix="items" />
          <NubiCounter :value="3" suffix="estrellas" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Animado</h3>
        <div class="demo-column">
          <NubiCounter :value="animatedCount" :animated="true" icon="trending-up" suffix="puntos" />
          <div class="demo-controls">
            <NubiButton variant="secondary" size="sm" @click="increment">Incrementar</NubiButton>
            <NubiButton variant="secondary" size="sm" @click="animatedCount = 0">Reiniciar</NubiButton>
          </div>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiCounter from '../../components/base/NubiCounter.vue'
import NubiButton from '../../components/base/NubiButton.vue'

const animatedCount = ref(0)

function increment() {
  animatedCount.value += Math.floor(Math.random() * 10) + 1
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-column { display: flex; flex-direction: column; gap: var(--nubi-spacing-md); }
.demo-controls { display: flex; gap: var(--nubi-spacing-sm); margin-top: var(--nubi-spacing-sm); }
</style>
