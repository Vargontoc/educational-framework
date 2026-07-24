<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.avatars') }}</h2>
      <p class="section-description">
        Avatar circular/redondeado con fallback de iniciales.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tamaños</h3>
        <div class="demo-row">
          <NubiAvatar name="Ana García" size="sm" />
          <NubiAvatar name="Ana García" size="md" />
          <NubiAvatar name="Ana García" size="lg" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con imagen</h3>
        <div class="demo-row">
          <NubiAvatar src="https://i.pravatar.cc/150?u=ana" name="Ana García" size="sm" />
          <NubiAvatar src="https://i.pravatar.cc/150?u=pedro" name="Pedro López" size="md" />
          <NubiAvatar src="https://i.pravatar.cc/150?u=maria" name="María Rodríguez" size="lg" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Fallback de iniciales</h3>
        <div class="demo-row">
          <NubiAvatar name="Ana García" size="md" />
          <NubiAvatar name="Pedro" size="md" />
          <NubiAvatar name="María Rodríguez López" size="md" />
          <NubiAvatar size="md" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Variaciones de color</h3>
        <div class="demo-row">
          <NubiAvatar name="Ana García" size="md" />
          <NubiAvatar name="Pedro López" size="md" />
          <NubiAvatar name="María Rodríguez" size="md" />
          <NubiAvatar name="Carlos Martínez" size="md" />
          <NubiAvatar name="Laura Sánchez" size="md" />
          <NubiAvatar name="Jorge Fernández" size="md" />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiAvatar from '../../components/base/NubiAvatar.vue'
</script>

<style scoped>
.catalog-section { max-width: 800px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-row { display: flex; gap: var(--nubi-spacing-md); align-items: center; flex-wrap: wrap; }
</style>
