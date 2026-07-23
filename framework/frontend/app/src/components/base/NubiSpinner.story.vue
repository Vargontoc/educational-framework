<script setup lang="ts">
import NubiSpinner from './NubiSpinner.vue'
</script>

<template>
  <Story title="Base/NubiSpinner">
    <Variant title="Default">
      <NubiSpinner />
    </Variant>
    
    <Variant title="Sizes">
      <div style="display: flex; gap: 24px; align-items: center;">
        <NubiSpinner size="sm" />
        <NubiSpinner size="md" />
        <NubiSpinner size="lg" />
      </div>
    </Variant>
    
    <Variant title="With Label">
      <NubiSpinner :show-label="true" label="Cargando datos..." />
    </Variant>
    
    <Variant title="Overlay">
      <div style="position: relative; width: 300px; height: 200px; background: var(--nubi-bg-surface-secondary); border-radius: var(--nubi-radius-md);">
        <p style="padding: 16px;">Contenido detrás del overlay</p>
        <NubiSpinner overlay />
      </div>
    </Variant>
    
    <Variant title="Custom Label">
      <NubiSpinner label="Procesando..." :show-label="true" />
    </Variant>
  </Story>
</template>

<docs>
## NubiSpinner

Indicador de carga circular con animación suave.

### Características
- Animación de rotación continua
- 3 tamaños: sm, md, lg
- Modo overlay para cubrir contenido
- Label opcional visible
- Accesibilidad con role="status"

### Uso
```vue
<!-- Inline -->
<NubiSpinner size="md" />

<!-- Con label visible -->
<NubiSpinner :show-label="true" label="Cargando..." />

<!-- Overlay -->
<NubiSpinner overlay />
```
</docs>
