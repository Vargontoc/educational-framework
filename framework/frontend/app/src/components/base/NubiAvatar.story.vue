<script setup lang="ts">
import NubiAvatar from './NubiAvatar.vue'
</script>

<template>
  <Story title="Base/NubiAvatar">
    <Variant title="Sizes">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiAvatar name="Ana García" size="sm" />
        <NubiAvatar name="Ana García" size="md" />
        <NubiAvatar name="Ana García" size="lg" />
      </div>
    </Variant>
    
    <Variant title="With Image">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiAvatar src="https://i.pravatar.cc/150?u=ana" name="Ana García" size="sm" />
        <NubiAvatar src="https://i.pravatar.cc/150?u=pedro" name="Pedro López" size="md" />
        <NubiAvatar src="https://i.pravatar.cc/150?u=maria" name="María Rodríguez" size="lg" />
      </div>
    </Variant>
    
    <Variant title="Fallback Initials">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiAvatar name="Ana García" size="md" />
        <NubiAvatar name="Pedro" size="md" />
        <NubiAvatar name="María Rodríguez López" size="md" />
        <NubiAvatar size="md" />
      </div>
    </Variant>
    
    <Variant title="Color Variations">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiAvatar name="Ana García" size="md" />
        <NubiAvatar name="Pedro López" size="md" />
        <NubiAvatar name="María Rodríguez" size="md" />
        <NubiAvatar name="Carlos Martínez" size="md" />
        <NubiAvatar name="Laura Sánchez" size="md" />
        <NubiAvatar name="Jorge Fernández" size="md" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiAvatar

Avatar circular/redondeado con fallback de iniciales.

### Características
- 3 tamaños: sm (32px), md (48px), lg (64px)
- Imagen circular con fallback automático
- Iniciales generadas desde el nombre
- Color de fondo basado en el nombre

### Uso
```vue
<NubiAvatar name="Ana García" size="md" />
<NubiAvatar src="/path/to/image" name="Ana García" size="lg" />
```
</docs>
