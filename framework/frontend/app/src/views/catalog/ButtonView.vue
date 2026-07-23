<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.buttons') }}</h2>
      <p class="section-description">
        Componente de botón con múltiples variantes y estados.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Variantes</h3>
        <div class="demo-row">
          <NubiButton variant="primary">Primario</NubiButton>
          <NubiButton variant="secondary">Secundario</NubiButton>
          <NubiButton variant="destructive">Destructivo</NubiButton>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Tamaños</h3>
        <div class="demo-row">
          <NubiButton size="sm">Pequeño</NubiButton>
          <NubiButton size="md">Mediano</NubiButton>
          <NubiButton size="lg">Grande</NubiButton>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-row">
          <NubiButton>Normal</NubiButton>
          <NubiButton :loading="true">Cargando</NubiButton>
          <NubiButton :disabled="true">Deshabilitado</NubiButton>
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con icono</h3>
        <div class="demo-row">
          <NubiButton variant="primary">
            <template #icon>
              <NubiIcon name="plus" :size="20" />
            </template>
            Agregar
          </NubiButton>
          <NubiButton variant="secondary">
            <template #icon>
              <NubiIcon name="edit" :size="20" />
            </template>
            Editar
          </NubiButton>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiButton from '../../components/base/NubiButton.vue'
import NubiIcon from '../../components/base/NubiIcon.vue'
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
