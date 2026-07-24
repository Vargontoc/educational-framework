<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.cards') }}</h2>
      <p class="section-description">
        Contenedor de contenido con estructura flexible.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básica</h3>
        <div class="demo-grid">
          <NubiCard title="Título de la tarjeta" description="Descripción de la tarjeta con información relevante." />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con imagen</h3>
        <div class="demo-grid">
          <NubiCard 
            image="https://picsum.photos/400/225" 
            image-alt="Imagen de ejemplo"
            title="Título con imagen" 
            description="Esta tarjeta tiene una imagen en la parte superior."
          />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Clickeable</h3>
        <div class="demo-grid">
          <NubiCard 
            title="Tarjeta clickeable" 
            description="Esta tarjeta es clickeable y tiene efecto hover."
            :clickable="true"
          />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Con acciones</h3>
        <div class="demo-grid">
          <NubiCard title="Tarjeta con acciones" description="Esta tarjeta tiene botones de acción en el footer.">
            <template #actions>
              <NubiButton variant="secondary" size="sm">Cancelar</NubiButton>
              <NubiButton variant="primary" size="sm">Aceptar</NubiButton>
            </template>
          </NubiCard>
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiCard from '../../components/base/NubiCard.vue'
import NubiButton from '../../components/base/NubiButton.vue'
</script>

<style scoped>
.catalog-section { max-width: 800px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: var(--nubi-spacing-md); }
</style>
