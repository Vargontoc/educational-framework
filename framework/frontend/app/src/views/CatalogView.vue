<template>
  <CatalogLayout>
    <div class="catalog-home">
      <section class="catalog-section">
        <h2 class="section-title">{{ $t('views.catalog.designTokens') }}</h2>
        <p class="section-description">
          Visualización de los tokens de diseño del sistema: colores, tipografía, espaciado y más.
        </p>
        
        <div class="token-grid">
          <div class="token-card">
            <h3 class="token-title">{{ $t('views.catalog.colors') }}</h3>
            <div class="color-swatches">
              <div class="swatch" style="background-color: var(--nubi-color-primary)">
                <span>Primary</span>
              </div>
              <div class="swatch" style="background-color: var(--nubi-color-secondary)">
                <span>Secondary</span>
              </div>
              <div class="swatch" style="background-color: var(--nubi-color-accent)">
                <span>Accent</span>
              </div>
            </div>
          </div>
          
          <div class="token-card">
            <h3 class="token-title">{{ $t('views.catalog.typography') }}</h3>
            <div class="typography-samples">
              <p style="font-size: var(--nubi-font-size-xs)">Extra Small (xs)</p>
              <p style="font-size: var(--nubi-font-size-sm)">Small (sm)</p>
              <p style="font-size: var(--nubi-font-size-base)">Base (base)</p>
              <p style="font-size: var(--nubi-font-size-lg)">Large (lg)</p>
              <p style="font-size: var(--nubi-font-size-xl)">Extra Large (xl)</p>
            </div>
          </div>
          
          <div class="token-card">
            <h3 class="token-title">{{ $t('views.catalog.spacing') }}</h3>
            <div class="spacing-samples">
              <div class="spacing-item">
                <div class="spacing-box" style="width: var(--nubi-spacing-xs); height: var(--nubi-spacing-xs)"></div>
                <span>xs (4px)</span>
              </div>
              <div class="spacing-item">
                <div class="spacing-box" style="width: var(--nubi-spacing-sm); height: var(--nubi-spacing-sm)"></div>
                <span>sm (8px)</span>
              </div>
              <div class="spacing-item">
                <div class="spacing-box" style="width: var(--nubi-spacing-md); height: var(--nubi-spacing-md)"></div>
                <span>md (16px)</span>
              </div>
              <div class="spacing-item">
                <div class="spacing-box" style="width: var(--nubi-spacing-lg); height: var(--nubi-spacing-lg)"></div>
                <span>lg (24px)</span>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      <section class="catalog-section">
        <h2 class="section-title">{{ $t('views.catalog.theme') }}</h2>
        <p class="section-description">
          El sistema soporta tema claro y oscuro. Usa el botón en la barra lateral para alternar.
        </p>
        <div class="theme-demo">
          <div class="theme-card">
            <h3>{{ $t('views.catalog.currentTheme') }}</h3>
            <p>{{ getCurrentTheme() === 'dark' ? $t('views.catalog.darkMode') : $t('views.catalog.lightMode') }}</p>
          </div>
        </div>
      </section>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
/**
 * Vista principal del catálogo de componentes
 * 
 * Muestra los tokens de diseño y proporciona una base para
 * documentar componentes futuros
 */

import CatalogLayout from '../components/catalog/CatalogLayout.vue'
import { useTheme } from '../composables/useTheme'

const { getCurrentTheme } = useTheme()
</script>

<style scoped>
.catalog-home {
  max-width: 1200px;
}

.catalog-section {
  margin-bottom: var(--nubi-spacing-3xl);
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
  margin-bottom: var(--nubi-spacing-lg);
}

.token-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--nubi-spacing-lg);
}

.token-card {
  background-color: var(--nubi-bg-surface-secondary);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  padding: var(--nubi-spacing-lg);
}

.token-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-md);
}

.color-swatches {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.swatch {
  padding: var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-md);
  color: white;
  font-weight: var(--nubi-font-weight-medium);
}

.typography-samples {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.spacing-samples {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.spacing-item {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-md);
}

.spacing-box {
  background-color: var(--nubi-color-primary);
  border-radius: var(--nubi-radius-sm);
}

.theme-demo {
  display: flex;
  gap: var(--nubi-spacing-md);
}

.theme-card {
  background-color: var(--nubi-bg-surface-secondary);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  padding: var(--nubi-spacing-lg);
}
</style>
