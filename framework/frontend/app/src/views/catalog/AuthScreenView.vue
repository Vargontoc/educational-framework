<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.authScreens') }}</h2>
      <p class="section-description">
        Pantalla de autenticación con PIN.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Validación local (PIN: 1234)</h3>
        <div class="demo-auth">
          <NubiAuthScreen 
            expected-pin="1234" 
            @success="handleSuccess"
            @error="handleError"
            @forgot="handleForgot"
          />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Validación personalizada (PIN: 5678)</h3>
        <div class="demo-auth">
          <NubiAuthScreen 
            :validate-pin="customValidation"
            @success="handleSuccess"
          />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiAuthScreen from '../../components/base/NubiAuthScreen.vue'

function handleSuccess(pin: string) {
  alert(`PIN correcto: ${pin}`)
}

function handleError(pin: string) {
  console.log(`PIN incorrecto: ${pin}`)
}

function handleForgot() {
  alert('¿Olvidaste tu PIN?')
}

async function customValidation(pin: string): Promise<boolean> {
  return pin === '5678'
}
</script>

<style scoped>
.catalog-section { max-width: 600px; }
.section-title { font-size: var(--nubi-font-size-2xl); font-weight: var(--nubi-font-weight-bold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-sm); }
.section-description { font-size: var(--nubi-font-size-base); color: var(--nubi-text-secondary); margin-bottom: var(--nubi-spacing-xl); }
.component-demo { margin-bottom: var(--nubi-spacing-2xl); }
.subsection-title { font-size: var(--nubi-font-size-lg); font-weight: var(--nubi-font-weight-semibold); color: var(--nubi-text-primary); margin-bottom: var(--nubi-spacing-md); }
.demo-auth { height: 500px; border: var(--nubi-border-width) solid var(--nubi-border-default); border-radius: var(--nubi-radius-lg); overflow: hidden; }
</style>
