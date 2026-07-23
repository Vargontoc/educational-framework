<template>
  <CatalogLayout>
    <div class="catalog-section">
      <h2 class="section-title">{{ $t('views.catalog.textInputs') }}</h2>
      <p class="section-description">
        Campo de entrada de texto con validación en tiempo real, iconos y contador de caracteres.
      </p>

      <div class="component-demo">
        <h3 class="subsection-title">Básico</h3>
        <div class="demo-column">
          <NubiTextInput v-model="textValue" label="Nombre" placeholder="Escribe tu nombre" />
          <NubiTextInput label="Email" prefix-icon="mail" placeholder="correo@ejemplo.com" type="email" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Validación</h3>
        <div class="demo-column">
          <NubiTextInput label="Campo obligatorio" :required="true" placeholder="Requerido" />
          <NubiTextInput label="Con error" error="Este campo tiene un error" />
          <NubiTextInput label="Con hint" hint="Este es un mensaje de ayuda" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Contador de caracteres</h3>
        <div class="demo-column">
          <NubiTextInput v-model="counterValue" label="Descripción" :max-length="100" :show-counter="true" placeholder="Escribe una descripción..." />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Estados</h3>
        <div class="demo-column">
          <NubiTextInput label="Normal" placeholder="Estado normal" />
          <NubiTextInput label="Disabled" disabled placeholder="Deshabilitado" />
          <NubiTextInput label="Readonly" readonly placeholder="Solo lectura" />
        </div>
      </div>

      <div class="component-demo">
        <h3 class="subsection-title">Tipos de input</h3>
        <div class="demo-column">
          <NubiTextInput label="Text" type="text" placeholder="Texto" />
          <NubiTextInput label="Email" type="email" placeholder="correo@ejemplo.com" />
          <NubiTextInput label="Password" type="password" placeholder="Contraseña" />
          <NubiTextInput label="Tel" type="tel" placeholder="+34 600 000 000" />
          <NubiTextInput label="URL" type="url" placeholder="https://..." />
        </div>
      </div>
    </div>
  </CatalogLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CatalogLayout from '../../components/catalog/CatalogLayout.vue'
import NubiTextInput from '../../components/base/NubiTextInput.vue'

const textValue = ref('')
const counterValue = ref('')
</script>

<style scoped>
.catalog-section {
  max-width: 600px;
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

.demo-column {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}
</style>
