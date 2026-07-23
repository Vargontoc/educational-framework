<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.errorStates') }}</h2>
      <p class="section-description">
        Vista para mostrar errores con opción de reintentar.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <NubiErrorState />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con mensaje personalizado</h3>
        <NubiErrorState 
          title="Error de conexión"
          message="No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet."
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con detalles técnicos</h3>
        <NubiErrorState 
          title="Error al cargar datos"
          message="Ha ocurrido un error al cargar la información"
          details="Error 500: Internal Server Error at /api/v1/users"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Sin botón de reintentar</h3>
        <NubiErrorState 
          title="Acceso denegado"
          message="No tienes permisos para acceder a este recurso"
          :show-retry="false"
        />
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con label personalizado</h3>
        <NubiErrorState 
          title="Tiempo de espera agotado"
          message="La operación tardó demasiado en completarse"
          retry-label="Intentar de nuevo"
        />
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiErrorState from '../../components/base/NubiErrorState.vue'
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
