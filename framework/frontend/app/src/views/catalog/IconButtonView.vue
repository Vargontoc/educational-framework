<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.iconButtons') }}</h2>
      <p class="section-description">
        Botón de acción rápida con icono y tooltip.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Tamaños</h3>
        <div class="demo-row">
          <NubiIconButton icon="settings" label="Configuración" size="sm" />
          <NubiIconButton icon="settings" label="Configuración" size="md" />
          <NubiIconButton icon="settings" label="Configuración" size="lg" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Diferentes iconos</h3>
        <div class="demo-row">
          <NubiIconButton icon="home" label="Inicio" />
          <NubiIconButton icon="user" label="Perfil" />
          <NubiIconButton icon="settings" label="Configuración" />
          <NubiIconButton icon="bell" label="Notificaciones" />
          <NubiIconButton icon="search" label="Buscar" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-row">
          <NubiIconButton icon="settings" label="Normal" />
          <NubiIconButton icon="settings" label="Deshabilitado" :disabled="true" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con tooltip</h3>
        <div class="demo-row">
          <NubiIconButton 
            icon="settings" 
            label="Configuración" 
            tooltip="Abrir configuración"
          />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiIconButton from '../../components/base/NubiIconButton.vue'
</script>

<style scoped>
.catalog-section {
  max-width: 1200px;
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

.demo-row {
  display: flex;
  gap: var(--nubi-spacing-md);
  flex-wrap: wrap;
  align-items: center;
}
</style>
