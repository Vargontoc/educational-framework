<script setup lang="ts">
import NubiTooltip from './NubiTooltip.vue'
import NubiButton from './NubiButton.vue'
</script>

<template>
  <Story title="Base/NubiTooltip">
    <Variant title="Positions">
      <div style="display: flex; gap: 32px; padding: 80px; flex-wrap: wrap;">
        <NubiTooltip text="Tooltip arriba" position="top">
          <NubiButton variant="secondary">Top</NubiButton>
        </NubiTooltip>
        <NubiTooltip text="Tooltip abajo" position="bottom">
          <NubiButton variant="secondary">Bottom</NubiButton>
        </NubiTooltip>
        <NubiTooltip text="Tooltip izquierda" position="left">
          <NubiButton variant="secondary">Left</NubiButton>
        </NubiTooltip>
        <NubiTooltip text="Tooltip derecha" position="right">
          <NubiButton variant="secondary">Right</NubiButton>
        </NubiTooltip>
      </div>
    </Variant>
    
    <Variant title="Long Text">
      <div style="padding: 80px;">
        <NubiTooltip text="Este es un tooltip con texto más largo que muestra información adicional" position="top">
          <NubiButton variant="secondary">Pasa el ratón</NubiButton>
        </NubiTooltip>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiTooltip

Información contextual al hover/focus.

### Uso
```vue
<NubiTooltip text="Más información" position="top">
  <NubiButton>Hover me</NubiButton>
</NubiTooltip>
```
</docs>
