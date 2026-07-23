<script setup lang="ts">
import NubiSkeleton from './NubiSkeleton.vue'
</script>

<template>
  <Story title="Base/NubiSkeleton">
    <Variant title="Line">
      <div style="display: flex; flex-direction: column; gap: 8px; width: 300px;">
        <NubiSkeleton variant="line" />
        <NubiSkeleton variant="line" width="80%" />
        <NubiSkeleton variant="line" width="60%" />
      </div>
    </Variant>
    
    <Variant title="Circle">
      <NubiSkeleton variant="circle" width="64px" />
    </Variant>
    
    <Variant title="Rectangle">
      <NubiSkeleton variant="rectangle" width="200px" height="120px" />
    </Variant>
    
    <Variant title="Card">
      <NubiSkeleton variant="card" width="300px" />
    </Variant>
    
    <Variant title="Card Layout Example">
      <div style="display: flex; gap: 16px; padding: 16px; border: 1px solid var(--nubi-border-default); border-radius: var(--nubi-radius-lg); width: 400px;">
        <NubiSkeleton variant="circle" width="64px" />
        <div style="flex: 1; display: flex; flex-direction: column; gap: 8px;">
          <NubiSkeleton variant="line" width="70%" />
          <NubiSkeleton variant="line" />
          <NubiSkeleton variant="line" width="50%" />
        </div>
      </div>
    </Variant>
    
    <Variant title="No Animation">
      <div style="display: flex; flex-direction: column; gap: 8px; width: 300px;">
        <NubiSkeleton variant="line" :animate="false" />
        <NubiSkeleton variant="line" width="80%" :animate="false" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiSkeleton

Placeholder animado para contenido en carga.

### Características
- Animación de pulso suave (shimmer)
- 4 variantes: line, circle, rectangle, card
- Tamaños personalizables
- Accesibilidad (aria-hidden)

### Variantes
- **line**: Línea de texto
- **circle**: Avatar o imagen circular
- **rectangle**: Bloque rectangular
- **card**: Card completa

### Uso
```vue
<!-- Líneas de texto -->
<NubiSkeleton variant="line" />
<NubiSkeleton variant="line" width="80%" />

<!-- Avatar -->
<NubiSkeleton variant="circle" width="64px" />

<!-- Card -->
<NubiSkeleton variant="card" width="300px" height="200px" />
```
</docs>
