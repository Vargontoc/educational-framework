<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.emptyStates') }}</h2>
      <p class="section-description">
        Vista para cuando no hay datos que mostrar.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiEmptyState 
          title="No hay datos"
          description="No hay elementos para mostrar en este momento"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con icono</h3>
        <NubiEmptyState 
          icon="inbox"
          title="Bandeja vacía"
          description="No tienes mensajes nuevos"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con acción</h3>
        <NubiEmptyState 
          icon="file-plus"
          title="No hay documentos"
          description="Aún no has creado ningún documento"
          action-label="Crear documento"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con slot personalizado</h3>
        <NubiEmptyState 
          title="Búsqueda sin resultados"
          description="No se encontraron resultados para tu búsqueda"
        >
          <template #icon>
            <div style="font-size: 64px;">🔍</div>
          </template>
        </NubiEmptyState>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiEmptyState from '../../components/base/NubiEmptyState.vue'
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
</style>
