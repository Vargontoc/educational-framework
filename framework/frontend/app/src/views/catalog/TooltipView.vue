<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.tooltips') }}</h2>
      <p class="section-description">
        Información contextual al hover/focus con posiciones configurables.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Posiciones</h3>
        <div class="demo-tooltip-grid">
          <NubiTooltip text="Tooltip arriba" position="top">
            <NubiButton variant="secondary">Top</NubiButton>
          </NubiTooltip>
          <NubiTooltip text="Tooltip abajo" position="bottom">
            <NubiButton variant="secondary">Bottom</NubiButton>
          </NubiTooltip>
          <NubiTooltip text="Tooltip izquierda" position="left">
            <NubiButton variant="secondary">Left</NubiButton>
          </NubiTooltip>
          <NubiTooltip text="Tooltip derecha" position="right">
            <NubiButton variant="secondary">Right</NubiButton>
          </NubiTooltip>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Texto largo</h3>
        <div class="demo-row">
          <NubiTooltip text="Este es un tooltip con texto más largo que muestra información adicional sobre el elemento" position="top">
            <NubiButton variant="secondary">Pasa el ratón</NubiButton>
          </NubiTooltip>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">En diferentes elementos</h3>
        <div class="demo-row">
          <NubiTooltip text="Icono de ayuda" position="top">
            <NubiIconButton icon="help-circle" variant="secondary" />
          </NubiTooltip>
          <NubiTooltip text="Texto informativo" position="bottom">
            <span style="padding: 8px; border: 1px dashed var(--nubi-border-default); border-radius: var(--nubi-radius-md); cursor: help;">
              Elemento con tooltip
            </span>
          </NubiTooltip>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiTooltip from '../../components/base/NubiTooltip.vue'
import NubiButton from '../../components/base/NubiButton.vue'
import NubiIconButton from '../../components/base/NubiIconButton.vue'
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-row { display: flex; gap: var(--nubi-spacing-md); flex-wrap: wrap; align-items: center; }
.demo-tooltip-grid { display: grid; grid-template-columns: repeat(2, auto); gap: var(--nubi-spacing-2xl); justify-content: center; padding: var(--nubi-spacing-2xl); }
</style>
