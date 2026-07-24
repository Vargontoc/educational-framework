<script setup lang="ts">
import NubiBadge from './NubiBadge.vue'
</script>

<template>
  <Story title="Base/NubiBadge">
    <Variant title="Variants">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiBadge variant="neutral" label="Neutral" />
        <NubiBadge variant="info" label="Info" />
        <NubiBadge variant="success" label="Success" />
        <NubiBadge variant="warning" label="Warning" />
        <NubiBadge variant="error" label="Error" />
      </div>
    </Variant>
    
    <Variant title="Sizes">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiBadge variant="info" label="Small" size="sm" />
        <NubiBadge variant="info" label="Medium" size="md" />
      </div>
    </Variant>
    
    <Variant title="With Icons">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiBadge variant="info" label="Info" icon="info" />
        <NubiBadge variant="success" label="Success" icon="check-circle" />
        <NubiBadge variant="warning" label="Warning" icon="alert-triangle" />
        <NubiBadge variant="error" label="Error" icon="alert-circle" />
      </div>
    </Variant>
    
    <Variant title="Slot Content">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiBadge variant="success">
          <strong>5</strong> nuevos
        </NubiBadge>
        <NubiBadge variant="info">
          v2.0.0
        </NubiBadge>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiBadge

Etiqueta pequeña para estados o contadores.

### Características
- 5 variantes: neutral, info, success, warning, error
- 2 tamaños: sm, md
- Icono opcional

### Uso
```vue
<NubiBadge variant="success" label="Activo" />
<NubiBadge variant="error" icon="alert-circle" label="Error" />
```
</docs>
