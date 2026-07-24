<script setup lang="ts">
import NubiBackButton from './NubiBackButton.vue'
</script>

<template>
  <Story title="Base/NubiBackButton">
    <Variant title="With Label">
      <NubiBackButton :use-router="false" />
    </Variant>
    
    <Variant title="Icon Only">
      <NubiBackButton :show-label="false" :use-router="false" />
    </Variant>
    
    <Variant title="Custom Label">
      <NubiBackButton label="Ir al inicio" :use-router="false" />
    </Variant>
  </Story>
</template>

<docs>
## NubiBackButton

Botón de navegación hacia atrás con flecha y texto opcional.

### Uso
```vue
<NubiBackButton />
<NubiBackButton :show-label="false" />
<NubiBackButton label="Ir al inicio" to="/" />
```
</docs>
